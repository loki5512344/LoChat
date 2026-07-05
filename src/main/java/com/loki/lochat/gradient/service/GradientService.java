package com.loki.lochat.gradient.service;

import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.util.GradientUtil;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * Сервис для применения градиентов - единая точка входа
 */
public class GradientService {
    
    private GradientService() {
    }

    /**
     * Применяет градиент к тексту если у игрока есть цвета
     */
    public static String applyGradient(Player player, String text, GradientPlayerData data) {
        if (data == null || !data.hasColors() || !data.isColorEnabled()) {
            return text;
        }
        
        return GradientUtil.applyGradient(text, data.getColors(), true);
    }
    
    /**
     * Применяет градиент к тексту с указанными цветами
     */
    public static String applyGradient(String text, List<String> colors) {
        if (colors == null || colors.isEmpty()) {
            return text;
        }
        
        return GradientUtil.applyGradient(text, colors, true);
    }
    
    /**
     * Применяет градиент в формате для TAB плагина
     */
    public static String applyGradientForTab(String text, List<String> colors) {
        if (colors == null || colors.isEmpty()) {
            return text;
        }
        
        return GradientUtil.applyGradientTabFormat(text, colors);
    }
    
    /**
     * Убирает все цветовые коды из текста
     */
    public static String stripColors(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        return text.replaceAll("(?i)(§x(§[0-9a-f]){6}|§[0-9a-fk-or]|&[0-9a-fk-or]|&#[0-9a-f]{6}|<[^>]+>)", "");
    }
}
