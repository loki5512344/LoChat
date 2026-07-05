package com.loki.lochat.api.service;

import com.loki.lochat.data.model.BanRecord;
import com.loki.lochat.data.model.WarnEntry;

import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;

/**
 * Варны и баны LoChat
 */
public interface PunishmentService {

    void addWarn(UUID uuid, String playerName, String moderator, String reason, boolean silent);

    List<WarnEntry> getWarns(UUID uuid);

    int getWarnCount(UUID uuid);

    /**
     * @param durationMs длительность; 0 = навсегда
     */
    void ban(UUID uuid, String playerName, long durationMs, String reason, String bannedBy);

    boolean unban(UUID uuid);

    boolean isBanned(UUID uuid);

    BanRecord getActiveBan(UUID uuid);

    /** Сообщение при кике (MiniMessage + legacy &#) */
    Component buildBanKickMessage(BanRecord ban);

    void save();

    void reload();
}
