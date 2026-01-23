package com.loki.lochat.integrations;

import com.loki.lochat.LoChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * Интеграция с SkinsRestorer для получения скинов игроков
 * Использует рефлексию для совместимости без прямой зависимости
 */
public class SkinsRestorerHook {

    private final LoChat plugin;
    private Object skinsAPI;
    private Method getSkinDataMethod;
    private boolean enabled = false;

    public SkinsRestorerHook(LoChat plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        try {
            Plugin skinsPlugin = Bukkit.getPluginManager().getPlugin("SkinsRestorer");
            if (skinsPlugin == null || !skinsPlugin.isEnabled()) {
                plugin.getLogger().info("SkinsRestorer не найден");
                return;
            }

            // Пытаемся получить API через рефлексию
            Class<?> apiClass = Class.forName("net.skinsrestorer.api.SkinsRestorerAPI");
            Method getApiMethod = apiClass.getMethod("getApi");
            skinsAPI = getApiMethod.invoke(null);
            
            getSkinDataMethod = skinsAPI.getClass().getMethod("getSkinData", String.class);
            
            enabled = true;
            plugin.getLogger().info("SkinsRestorer подключен!");
        } catch (Exception e) {
            plugin.getLogger().info("SkinsRestorer не найден или несовместимая версия");
            enabled = false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Получает текстуру скина игрока
     * @param player игрок
     * @return URL текстуры или null
     */
    public String getPlayerSkinTexture(Player player) {
        if (!enabled) return null;

        try {
            Object property = getSkinDataMethod.invoke(skinsAPI, player.getName());
            if (property != null) {
                Method getValueMethod = property.getClass().getMethod("getValue");
                return (String) getValueMethod.invoke(property);
            }
        } catch (Exception e) {
            // Игнорируем ошибки получения скина
        }
        return null;
    }

    /**
     * Получает URL головы игрока для отображения
     * @param playerName имя игрока
     * @return URL головы
     */
    public String getPlayerHeadUrl(String playerName) {
        // Используем стандартные сервисы для получения голов
        return "https://minotar.net/helm/" + playerName + "/8.png";
    }
}