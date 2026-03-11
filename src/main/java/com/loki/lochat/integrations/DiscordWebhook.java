package com.loki.lochat.integrations;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.loki.lochat.util.FoliaUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Класс для отправки сообщений в Discord через вебхуки
 */
public class DiscordWebhook {
    private final JavaPlugin plugin;
    private final String webhookUrl;
    private final String username;
    private final String avatarUrl;
    private final int timeout;
    private final int retryAttempts;
    private final long retryDelay;

    public DiscordWebhook(JavaPlugin plugin, String webhookUrl, String username, String avatarUrl, 
                         int timeout, int retryAttempts, long retryDelay) {
        this.plugin = plugin;
        this.webhookUrl = webhookUrl;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.timeout = timeout * 1000; // конвертируем в миллисекунды
        this.retryAttempts = retryAttempts;
        this.retryDelay = retryDelay;
    }

    /**
     * Отправить простое текстовое сообщение
     */
    public CompletableFuture<Boolean> sendMessage(String content) {
        JsonObject json = new JsonObject();
        json.addProperty("content", content);
        json.addProperty("username", username);
        json.addProperty("avatar_url", avatarUrl);
        
        return sendWebhook(json);
    }

    /**
     * Отправить embed сообщение
     */
    public CompletableFuture<Boolean> sendEmbed(String title, String description, String color, String thumbnailUrl) {
        JsonObject embed = new JsonObject();
        if (title != null) embed.addProperty("title", title);
        if (description != null) embed.addProperty("description", description);
        if (color != null) embed.addProperty("color", Integer.parseInt(color, 16));
        
        if (thumbnailUrl != null) {
            JsonObject thumbnail = new JsonObject();
            thumbnail.addProperty("url", thumbnailUrl);
            embed.add("thumbnail", thumbnail);
        }
        
        // Добавляем timestamp
        embed.addProperty("timestamp", java.time.Instant.now().toString());
        
        JsonArray embeds = new JsonArray();
        embeds.add(embed);
        
        JsonObject json = new JsonObject();
        json.add("embeds", embeds);
        json.addProperty("username", username);
        json.addProperty("avatar_url", avatarUrl);
        
        return sendWebhook(json);
    }

    /**
     * Отправить embed с кастомными полями
     */
    public CompletableFuture<Boolean> sendEmbedWithFields(String title, String description, String color, 
                                                         String thumbnailUrl, JsonArray fields) {
        JsonObject embed = new JsonObject();
        if (title != null) embed.addProperty("title", title);
        if (description != null) embed.addProperty("description", description);
        if (color != null) embed.addProperty("color", Integer.parseInt(color, 16));
        if (fields != null) embed.add("fields", fields);
        
        if (thumbnailUrl != null) {
            JsonObject thumbnail = new JsonObject();
            thumbnail.addProperty("url", thumbnailUrl);
            embed.add("thumbnail", thumbnail);
        }
        
        embed.addProperty("timestamp", java.time.Instant.now().toString());
        
        JsonArray embeds = new JsonArray();
        embeds.add(embed);
        
        JsonObject json = new JsonObject();
        json.add("embeds", embeds);
        json.addProperty("username", username);
        json.addProperty("avatar_url", avatarUrl);
        
        return sendWebhook(json);
    }

    /**
     * Отправить JSON в вебхук с повторными попытками
     */
    private CompletableFuture<Boolean> sendWebhook(JsonObject json) {
        return CompletableFuture.supplyAsync(() -> {
            for (int attempt = 1; attempt <= retryAttempts; attempt++) {
                try {
                    boolean success = sendWebhookSync(json.toString());
                    if (success) {
                        return true;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Discord webhook attempt " + attempt + " failed: " + e.getMessage());
                }
                
                // Ждем перед следующей попыткой (кроме последней)
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
        });
    }

    /**
     * Синхронная отправка в вебхук
     */
    private boolean sendWebhookSync(String jsonPayload) throws IOException {
        URL url = new URL(webhookUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "LoChat-Discord-Integration/1.0");
            connection.setDoOutput(true);
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            
            // Отправляем JSON
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == 204) {
                return true; // Успешная отправка
            } else if (responseCode == 429) {
                plugin.getLogger().warning("Discord webhook rate limited (429)");
                return false;
            } else {
                plugin.getLogger().warning("Discord webhook returned code: " + responseCode);
                return false;
            }
            
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Проверить валидность вебхука
     */
    public boolean isValid() {
        return webhookUrl != null && 
               webhookUrl.startsWith("https://discord.com/api/webhooks/") &&
               webhookUrl.length() > 50; // Минимальная длина валидного URL
    }
}