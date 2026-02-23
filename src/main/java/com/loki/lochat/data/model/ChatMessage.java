package com.loki.lochat.data.model;

import org.bukkit.entity.Player;

/**
 * Модель сообщения чата (Value Object)
 */
public class ChatMessage {
    private final Player sender;
    private String content;
    private final boolean isGlobal;
    private final long timestamp;
    
    private ChatMessage(Player sender, String content, boolean isGlobal) {
        this.sender = sender;
        this.content = content;
        this.isGlobal = isGlobal;
        this.timestamp = System.currentTimeMillis();
    }
    
    public static ChatMessage create(Player sender, String rawMessage) {
        // Определяем тип чата
        boolean isGlobal = rawMessage.startsWith("!") && 
                          sender.hasPermission("chat.global.use");
        
        String content = isGlobal ? 
            rawMessage.substring(1).trim() : 
            rawMessage.trim();
        
        return new ChatMessage(sender, content, isGlobal);
    }
    
    public Player getSender() { return sender; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isGlobal() { return isGlobal; }
    public long getTimestamp() { return timestamp; }
    public String getChatType() { return isGlobal ? "global" : "local"; }
}
