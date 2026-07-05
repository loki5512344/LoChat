package com.loki.lochat.translate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class TranslationService {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final Logger logger;
    private final HttpClient httpClient;
    private final Gson gson;
    private final Map<String, Map<String, String>> cache = new ConcurrentHashMap<>();
    private final String endpoint;
    private final String apiKey;
    private final boolean enabled;

    public TranslationService(String endpoint, String apiKey, boolean enabled, Logger logger) {
        this.endpoint = endpoint != null ? endpoint.replaceAll("/+$", "") : "";
        this.apiKey = apiKey;
        this.enabled = enabled;
        this.logger = logger;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        this.gson = new Gson();
    }

    public boolean isEnabled() {
        return enabled && !endpoint.isBlank();
    }

    public String translate(String text, String targetLanguage) {
        if (!isEnabled() || text == null || text.isBlank()) {
            return text;
        }

        String langCode = localeToLanguageCode(targetLanguage);
        String cacheKey = text.intern();

        Map<String, String> langMap = cache.get(cacheKey);
        if (langMap != null) {
            String cached = langMap.get(langCode);
            if (cached != null) {
                return cached;
            }
        }

        try {
            String translation = fetchTranslation(text, langCode);
            cache.computeIfAbsent(cacheKey, k -> new ConcurrentHashMap<>()).put(langCode, translation);
            return translation;
        } catch (Exception e) {
            logger.warning("Translation failed for \"" + abbreviate(text) + "\" to " + langCode + ": " + e.getMessage());
            return text;
        }
    }

    private String fetchTranslation(String text, String targetLang) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("q", text);
        body.addProperty("source", "auto");
        body.addProperty("target", targetLang);
        body.addProperty("format", "text");
        if (apiKey != null && !apiKey.isBlank()) {
            body.addProperty("api_key", apiKey);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint + "/translate"))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API returned " + response.statusCode() + ": " + response.body());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return json.get("translatedText").getAsString();
    }

    public static String localeToLanguageCode(String locale) {
        if (locale == null || locale.isBlank()) {
            return "en";
        }
        String lang = locale.split("_")[0].toLowerCase(Locale.ROOT);
        return switch (lang) {
            case "zh" -> "zh-CN";
            case "zh-tw", "zh-hk" -> "zh-TW";
            default -> lang;
        };
    }

    public void clearCache() {
        cache.clear();
    }

    private static String abbreviate(String text) {
        return text.length() > 40 ? text.substring(0, 37) + "..." : text;
    }
}
