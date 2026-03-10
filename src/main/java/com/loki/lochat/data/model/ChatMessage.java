package com.loki.lochat.data.model;

import org.bukkit.entity.Player;

/**
 * Модель сообщения чата (Value Object)
 */
public class ChatMessage {
    private final Player sender;
    private final boolean isGlobal;
    private final long timestamp;
    private String content;

    private ChatMessage(Player sender, String content, boolean isGlobal) {
        this.sender = sender;
        this.content = content;
        this.isGlobal = isGlobal;
        this.timestamp = System.currentTimeMillis();
    }

    public static ChatMessage create(Player sender, String rawMessage) {
        // С "!" = глобальный чат
        // Без "!" = локальный чат
        boolean isGlobal = rawMessage.startsWith("!");

        String content = isGlobal ?
                rawMessage.substring(1).trim() :
                rawMessage.trim();

        return new ChatMessage(sender, content, isGlobal);
    }

    public Player getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getChatType() {
        return isGlobal ? "global" : "local";
    }
}
