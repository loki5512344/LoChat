package com.loki.lochat.integrations;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.loki.lochat.integrations.discord.DiscordRateLimiter;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Отправка сообщений в Discord через вебхуки.
 * Все запросы идут в выделенный daemon-поток LoChat-Discord,
 * а не в ForkJoinPool.commonPool().
 */
public class DiscordWebhook {

    private final JavaPlugin plugin;
    private final String webhookUrl;
    private final String username;
    private final String avatarUrl;
    private final int timeout;
    private final int retryAttempts;
    private final long retryDelay;
    private final DiscordRateLimiter rateLimiter;

    // Выделенный пул — не засоряем ForkJoinPool.commonPool()
    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            r -> {
                Thread t = new Thread(r, "LoChat-Discord");
                t.setDaemon(true);
                return t;
            }
    );

    @SuppressWarnings("checkstyle:ParameterNumber")
    public DiscordWebhook(JavaPlugin plugin, String webhookUrl, String username, String avatarUrl,
                          int timeout, int retryAttempts, long retryDelay,
                          double maxRequestsPerSecond, int rateLimitBurst) {
        this.plugin = plugin;
        this.webhookUrl = webhookUrl != null ? webhookUrl.trim() : null;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.timeout = timeout * 1000;
        this.retryAttempts = retryAttempts;
        this.retryDelay = retryDelay;
        this.rateLimiter = new DiscordRateLimiter(maxRequestsPerSecond, rateLimitBurst);
    }

    // ── Публичные методы отправки ─────────────────────────────────────────────

    public CompletableFuture<Boolean> sendMessage(String content) {
        JsonObject json = new JsonObject();
        json.addProperty("content", content);
        json.addProperty("username", username);
        json.addProperty("avatar_url", avatarUrl);
        return sendWebhook(json);
    }

    public CompletableFuture<Boolean> sendEmbed(String title, String description, String color, String thumbnailUrl) {
        return sendWebhook(buildEmbed(title, description, color, thumbnailUrl, null));
    }

    public CompletableFuture<Boolean> sendEmbedWithFields(String title, String description, String color,
                                                          String thumbnailUrl, JsonArray fields) {
        return sendWebhook(buildEmbed(title, description, color, thumbnailUrl, fields));
    }

    /** Остановить executor при выключении плагина */
    public void shutdown() {
        executor.shutdown();
    }

    // ── Внутренние методы ─────────────────────────────────────────────────────

    private JsonObject buildEmbed(String title, String description, String color,
                                  String thumbnailUrl, JsonArray fields) {
        JsonObject embed = new JsonObject();
        if (title != null) {
            embed.addProperty("title", title);
        }
        if (description != null) {
            embed.addProperty("description", description);
        }
        if (color != null) {
            embed.addProperty("color", Integer.parseInt(color, 16));
        }
        if (fields != null) {
            embed.add("fields", fields);
        }
        if (thumbnailUrl != null) {
            JsonObject thumb = new JsonObject();
            thumb.addProperty("url", thumbnailUrl);
            embed.add("thumbnail", thumb);
        }
        embed.addProperty("timestamp", java.time.Instant.now().toString());

        JsonArray embeds = new JsonArray();
        embeds.add(embed);

        JsonObject json = new JsonObject();
        json.add("embeds", embeds);
        json.addProperty("username", username);
        json.addProperty("avatar_url", avatarUrl);
        return json;
    }

    /**
     * Отправляет JSON в вебхук асинхронно через выделенный executor.
     */
    private CompletableFuture<Boolean> sendWebhook(JsonObject json) {
        return CompletableFuture.supplyAsync(() -> {
            for (int attempt = 1; attempt <= retryAttempts; attempt++) {
                rateLimiter.waitIfNeeded();
                try {
                    if (sendWebhookSync(json.toString())) {
                        return true;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Discord webhook attempt " + attempt + " failed: " + e.getMessage());
                }
                if (attempt < retryAttempts) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            plugin.getLogger().severe("Failed to send Discord webhook after " + retryAttempts + " attempts");
            return false;
        }, executor); // ← передаём свой executor, не commonPool
    }

    private boolean sendWebhookSync(String jsonPayload) throws IOException {
        URL url = URI.create(webhookUrl.trim()).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "LoChat-Discord-Integration/1.0");
            conn.setDoOutput(true);
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            if (code == 204) {
                return true;
            }
            if (code == 429) {
                plugin.getLogger().warning("Discord webhook rate limited (429)");
                return false;
            }
            plugin.getLogger().warning("Discord webhook returned code: " + code);
            return false;
        } finally {
            conn.disconnect();
        }
    }

    public boolean isValid() {
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            return false;
        }
        String url = webhookUrl.trim();
        if (!url.startsWith("https://discord.com/api/webhooks/") &&
            !url.startsWith("https://discordapp.com/api/webhooks/")) {
            return false;
        }
        if (url.length() < 50) {
            return false;
        }
        try {
            URI.create(url);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
