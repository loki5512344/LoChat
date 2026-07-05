package com.loki.lochat.data.model;

import java.util.UUID;

/**
 * Активная блокировка
 */
public class BanRecord {
    public UUID uuid;
    public String playerName;
    public String reason;
    public String bannedBy;
    /** 0 = перманент; иначе timestamp окончания бана */
    public long until;
    public long createdAt;

    public BanRecord() {
    }

    public BanRecord(UUID uuid, String playerName, String reason, String bannedBy, long until, long createdAt) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.reason = reason;
        this.bannedBy = bannedBy;
        this.until = until;
        this.createdAt = createdAt;
    }

    public boolean isPermanent() {
        return until == 0;
    }

    public boolean isExpired() {
        if (isPermanent()) {
            return false;
        }
        return System.currentTimeMillis() >= until;
    }
}
