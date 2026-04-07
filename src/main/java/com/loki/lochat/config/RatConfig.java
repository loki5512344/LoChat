package com.loki.lochat.config;

import java.util.List;

/**
 * Централизованная конфигурация констант с поддержкой переменных окружения
 * KISS: все хардкод значения в одном месте
 */
public final class RatConfig {

    // ========== Nick validation ==========
    public static final int NICK_MIN_LENGTH = getEnvOrDefault("LOCHAT_NICK_MIN", 3);
    public static final int NICK_MAX_LENGTH = getEnvOrDefault("LOCHAT_NICK_MAX", 16);

    // ========== Roll limits ==========
    public static final int ROLL_DEFAULT_MAX = getEnvOrDefault("LOCHAT_ROLL_DEFAULT", 100);
    public static final int ROLL_ABSOLUTE_MAX = getEnvOrDefault("LOCHAT_ROLL_MAX", 1_000_000);

    // ========== Mute history ==========
    public static final int MAX_HISTORY_PER_PLAYER = getEnvOrDefault("LOCHAT_MUTE_HISTORY", 50);

    // ========== Time constants ==========
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final int SECONDS_IN_MINUTE = 60;
    public static final int MINUTES_IN_HOUR = 60;
    public static final int HOURS_IN_DAY = 24;

    // ========== Folia/Paper compatibility ==========
    public static final int TICKS_PER_SECOND = 20;
    public static final int MILLIS_PER_TICK = 50;

    // ========== Math constants ==========
    public static final int PERCENTAGE_MULTIPLIER = 100;

    // ========== GUI slots ==========
    public static final int GUI_CONFIRM_SLOT = 11;
    public static final int GUI_CANCEL_SLOT = 15;
    public static final int GUI_PREVIEW_SLOT = 13;

    // ========== LuckPerms ==========
    public static final int LUCKPERMS_PREFIX_PRIORITY = 100;

    // ========== Gradient presets ==========
    public static final List<String> PRESET_COLORS = List.of(
            "#FF0000", "#FF7F00", "#FFFF00", "#00FF00",
            "#0000FF", "#4B0082", "#9400D3", "#FF69B4",
            "#00FFFF", "#FFD700", "#FFFFFF", "#000000"
    );

    private RatConfig() {
    }

    /**
     * Получить значение из переменной окружения или вернуть дефолт
     */
    private static int getEnvOrDefault(String envKey, int defaultValue) {
        String envValue = System.getenv(envKey);
        if (envValue != null) {
            try {
                return Integer.parseInt(envValue);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
