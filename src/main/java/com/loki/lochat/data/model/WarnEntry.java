package com.loki.lochat.data.model;

/**
 * Запись о варне (для JSON)
 */
public class WarnEntry {
    public long time;
    public String moderator;
    public String reason;
    public boolean silent;

    public WarnEntry() {
    }

    public WarnEntry(long time, String moderator, String reason, boolean silent) {
        this.time = time;
        this.moderator = moderator;
        this.reason = reason;
        this.silent = silent;
    }
}
