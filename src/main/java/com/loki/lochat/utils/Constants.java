package com.loki.lochat.utils;

/**
 * Константы плагина (только неизменяемые системные значения)
 */
public final class Constants {

    // Время (системные константы)
    public static final long MILLIS_IN_SECOND = 1000L;
    public static final int SECONDS_IN_MINUTE = 60;
    public static final int MINUTES_IN_HOUR = 60;
    public static final int HOURS_IN_DAY = 24;
    
    // Folia/Paper совместимость (системные константы)
    public static final int TICKS_PER_SECOND = 20;
    public static final int MILLIS_PER_TICK = 50;
    
    // Математические константы
    public static final int PERCENTAGE_MULTIPLIER = 100;
    
    // LuckPerms приоритет (системная константа)
    public static final int LUCKPERMS_PREFIX_PRIORITY = 100;

    private Constants() {
    }
}