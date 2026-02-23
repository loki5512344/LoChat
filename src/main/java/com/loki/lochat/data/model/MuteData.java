package com.loki.lochat.data.model;

import java.util.UUID;

/**
 * Модель данных мута (Value Object)
 */
public class MuteData {
    private UUID uuid;
    private String playerName;
    public long endTime; // 0 = перманентный (public for JSON serialization)
    public String reason;
    public String mutedBy;
    public long mutedAt;
    
    public MuteData() {} // For JSON deserialization
    
    public MuteData(UUID uuid, String playerName, long endTime, String reason, String mutedBy) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.endTime = endTime;
        this.reason = reason;
        this.mutedBy = mutedBy;
        this.mutedAt = System.currentTimeMillis();
    }
    
    public MuteData(long endTime, String reason, String mutedBy, long mutedAt, String playerName) {
        this.endTime = endTime;
        this.reason = reason;
        this.mutedBy = mutedBy;
        this.mutedAt = mutedAt;
        this.playerName = playerName;
    }
    
    public UUID getUuid() { return uuid; }
    public String getPlayerName() { return playerName; }
    public long getEndTime() { return endTime; }
    public String getReason() { return reason; }
    public String getMutedBy() { return mutedBy; }
    public long getMutedAt() { return mutedAt; }
    public boolean isPermanent() { return endTime == 0; }
    
    /**
     * Запись истории мута
     */
    public static class MuteHistoryEntry {
        public String playerName;
        public long duration;
        public String reason;
        public String mutedBy;
        public long mutedAt;
        public boolean unmuted;
        public String unmutedBy;
        public long unmutedAt;
        
        public MuteHistoryEntry() {} // For JSON deserialization
        
        public MuteHistoryEntry(String playerName, long duration, String reason, 
                               String mutedBy, long mutedAt, boolean unmuted, 
                               String unmutedBy, long unmutedAt) {
            this.playerName = playerName;
            this.duration = duration;
            this.reason = reason;
            this.mutedBy = mutedBy;
            this.mutedAt = mutedAt;
            this.unmuted = unmuted;
            this.unmutedBy = unmutedBy;
            this.unmutedAt = unmutedAt;
        }
    }
}
