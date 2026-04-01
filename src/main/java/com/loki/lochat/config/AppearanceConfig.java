package com.loki.lochat.config;

import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Конфигурация внешнего вида чата (config/appearance.yml)
 */
public class AppearanceConfig extends BaseConfig {
    private final Map<String, String> emojiCache = new HashMap<>();
    
    public AppearanceConfig(Plugin plugin) {
        super((org.bukkit.plugin.java.JavaPlugin) plugin, "appearance.yml", true); // true = в папке config/
        // init() вызывается отдельно в методе init()
    }
    
    /**
     * Инициализировать конфигурацию (вызывать после создания объекта)
     */
    public void init() {
        super.init(); // ✅ Вызываем init() родителя
        loadEmojiCache();
    }
    
    @Override
    protected void onLoad() {
        loadEmojiCache();
    }
    
    /**
     * Загрузить кэш эмодзи
     */
    private void loadEmojiCache() {
        emojiCache.clear();
        if (config.isConfigurationSection("emojis")) {
            for (String key : config.getConfigurationSection("emojis").getKeys(false)) {
                emojiCache.put(key, config.getString("emojis." + key));
            }
        }
    }
    
    // ========== ПРЕФИКСЫ ==========
    
    /**
     * Получить эмодзи для глобального чата
     */
    public String getGlobalEmoji() {
        return config.getString("prefixes.global.emoji", "🌍");
    }
    
    /**
     * Получить текст префикса глобального чата
     */
    public String getGlobalText() {
        return config.getString("prefixes.global.text", "GLOBAL");
    }
    
    /**
     * Получить цвета градиента для глобального чата
     */
    public List<String> getGlobalColors() {
        return config.getStringList("prefixes.global.colors");
    }
    
    /**
     * Получить разделитель для глобального чата
     */
    public String getGlobalSeparatorText() {
        return config.getString("prefixes.global.separator.text", " ▶ ");
    }
    
    /**
     * Получить цвет разделителя для глобального чата
     */
    public String getGlobalSeparatorColor() {
        return config.getString("prefixes.global.separator.color", "#FFD700");
    }
    
    /**
     * Получить эмодзи для локального чата
     */
    public String getLocalEmoji() {
        return config.getString("prefixes.local.emoji", "");
    }
    
    /**
     * Получить текст префикса локального чата
     */
    public String getLocalText() {
        return config.getString("prefixes.local.text", "LOCAL");
    }
    
    /**
     * Получить цвета градиента для локального чата
     */
    public List<String> getLocalColors() {
        return config.getStringList("prefixes.local.colors");
    }
    
    /**
     * Получить разделитель для локального чата
     */
    public String getLocalSeparatorText() {
        return config.getString("prefixes.local.separator.text", " ▶ ");
    }
    
    /**
     * Получить цвет разделителя для локального чата
     */
    public String getLocalSeparatorColor() {
        return config.getString("prefixes.local.separator.color", "#FFFFFF");
    }

    /**
     * Получить цвет текста сообщений в глобальном чате (дефолт)
     */
    public String getGlobalMessageColor() {
        return config.getString("prefixes.global.message-color", "#FFFFFF");
    }

    /**
     * Получить цвет текста сообщений в локальном чате (дефолт)
     */
    public String getLocalMessageColor() {
        return config.getString("prefixes.local.message-color", "#FFFFFF");
    }
    
    // ========== ФОРМАТ СООБЩЕНИЙ ==========
    
    /**
     * Получить формат сообщения для глобального чата
     */
    public String getGlobalChatFormat() {
        return config.getString("chat-format.global", "{emoji} {prefix} {separator} {player} : {message}");
    }
    
    /**
     * Получить формат сообщения для локального чата
     */
    public String getLocalChatFormat() {
        return config.getString("chat-format.local", "{emoji} {prefix} {separator} {player} : {message}");
    }
    
    // ========== HOVER ЭФФЕКТЫ ==========
    
    /**
     * Включены ли hover эффекты
     */
    public boolean isHoverEnabled() {
        return config.getBoolean("hover.enabled", true);
    }
    
    /**
     * Получить формат hover подсказки
     */
    public List<String> getHoverFormat() {
        return config.getStringList("hover.format");
    }
    
    // ========== ЭМОДЗИ ==========
    
    /**
     * Получить все эмодзи замены
     */
    public Map<String, String> getEmojis() {
        return new HashMap<>(emojiCache);
    }
    
    /**
     * Получить замену для эмодзи
     */
    public String getEmoji(String key) {
        return emojiCache.get(key);
    }
    
    // ========== НАСТРОЙКИ ЧАТА ==========
    
    /**
     * Получить радиус локального чата
     */
    public int getLocalRadius() {
        return config.getInt("chat.local_radius", 100);
    }
    
    /**
     * Получить количество строк для очистки чата
     */
    public int getClearLines() {
        return config.getInt("chat.clear_lines", 100);
    }
    
    /**
     * Получить минимальную длину сообщения
     */
    public int getMinMessageLength() {
        return config.getInt("chat.min_message_length", 1);
    }
    
    /**
     * Получить максимальную длину сообщения
     */
    public int getMaxMessageLength() {
        return config.getInt("chat.max_message_length", 2000);
    }
    
    /**
     * Получить цвет "никто не услышал"
     */
    public String getNobodyHeardColor() {
        return config.getString("chat.colors.nobody_heard", "#FFA726");
    }
    
    /**
     * Получить цвет ошибки
     */
    public String getErrorColor() {
        return config.getString("chat.colors.error", "#FF6B6B");
    }
    
    /**
     * Получить цвет успеха
     */
    public String getSuccessColor() {
        return config.getString("chat.colors.success", "#6BCB77");
    }
    
    /**
     * Получить цвет предупреждения
     */
    public String getWarningColor() {
        return config.getString("chat.colors.warning", "#FFA726");
    }
    
    /**
     * Получить цвет информации
     */
    public String getInfoColor() {
        return config.getString("chat.colors.info", "#87CEEB");
    }
    
    // ========== DISCORD ==========
    
    /**
     * Получить эмодзи для Discord событий
     */
    public String getDiscordEmoji(String event) {
        return config.getString("discord.emojis." + event, "");
    }
    
    /**
     * Получить URL аватара по умолчанию
     */
    public String getDefaultAvatarUrl() {
        return config.getString("discord.avatar_urls.default", "https://mc-heads.net/avatar/minecraft/64");
    }
    
    /**
     * Получить URL аватара игрока
     */
    public String getPlayerAvatarUrl() {
        return config.getString("discord.avatar_urls.player", "https://mc-heads.net/avatar/{player}/64");
    }
    
    /**
     * Получить цвет embed по умолчанию
     */
    public String getDefaultEmbedColor() {
        return config.getString("discord.default_embed_color", "5865F2");
    }
    
    /**
     * Получить заголовок события Discord
     */
    public String getDiscordEventTitle(String event) {
        return config.getString("discord.event_titles." + event, "");
    }
}
