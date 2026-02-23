package com.loki.lohub.actions;

public enum ActionType {
    MESSAGE,
    BROADCAST,
    TITLE,
    ACTIONBAR,
    SOUND,
    COMMAND,
    CONSOLE,
    GAMEMODE,
    EFFECT,
    CLOSE,
    UNKNOWN;

    public static ActionType fromString(String type) {
        try {
            return valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
