package com.loki.lochat.config;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Конфигурация системных сообщений (config/messages.yml)
 */
public class MessagesConfig extends BaseConfig {

    public MessagesConfig(JavaPlugin plugin) {
        super(plugin, "messages.yml", true); // true = в папке config/
        init(); // ✅ Явная инициализация
    }

    // ── Общие сообщения ────────────────────────────────────────────────────────
    
    public String getNoPermission() {
        return config.getString("general.no_permission", "&#CF6679Нет прав!");
    }

    public String getPlayerOnly() {
        return config.getString("general.player_only", "&#CF6679Только для игроков!");
    }

    public String getPlayerNotFound() {
        return config.getString("general.player_not_found", "&#CF6679Игрок не найден!");
    }

    // ── Кулдаун ────────────────────────────────────────────────────────────────
    
    public String getCooldownMessage() {
        return config.getString("commands.cooldown.message", "&#B798A8Подождите &#7858E9{remaining} &#B798A8сек");
    }

    // ── Локальный чат ──────────────────────────────────────────────────────────
    
    public String getNobodyHeard() {
        return config.getString("local_chat.nobody_heard", "&#9878C9Вас никто не услышал &#B798A8— рядом нет игроков");
    }

    // ── Мут ────────────────────────────────────────────────────────────────────
    
    public String getMutedMessage() {
        return config.getString("mute.muted_message", "&#CF6679Вы замучены! &#B798A8Причина: &#7858E9{reason}&#B798A8. Осталось: &#7858E9{time}");
    }

    public String getMutedPermanent() {
        return config.getString("mute.muted_permanent", "&#CF6679Вы замучены навсегда! &#B798A8Причина: &#7858E9{reason}");
    }

    public String getPlayerMuted() {
        return config.getString("mute.player_muted", "&#9878C9Игрок &#7858E9{player} &#9878C9замучен на &#7858E9{time}&#9878C9. Причина: &#B798A8{reason}");
    }

    public String getPlayerMutedPermanent() {
        return config.getString("mute.player_muted_permanent", "&#9878C9Игрок &#7858E9{player} &#9878C9замучен навсегда. Причина: &#B798A8{reason}");
    }

    public String getPlayerMutedSilent() {
        return config.getString("mute.player_muted_silent", "&#B798A8[&#9878C9ТИХИЙ МУТ&#B798A8] &#7858E9{player} &#B798A8замучен на &#7858E9{time}");
    }

    public String getPlayerUnmuted() {
        return config.getString("mute.player_unmuted", "&#9878C9Игрок &#7858E9{player} &#9878C9размучен");
    }

    public String getPlayerNotMuted() {
        return config.getString("mute.not_muted", "&#B798A8Игрок &#7858E9{player} &#B798A8не замучен");
    }

    public String getPlayerNotMutedSimple() {
        return config.getString("mute.player_not_muted_simple", "&#CF6679Игрок не замучен!");
    }

    public String getInvalidTime() {
        return config.getString("mute.invalid_time", "&#CF6679Неверный формат времени!");
    }

    public String getAlreadyMuted() {
        return config.getString("mute.already_muted", "&#CF6679Игрок уже замучен!");
    }

    public String getNoSilentUnmutePermission() {
        return config.getString("mute.no_silent_unmute_permission", "&#CF6679Нет права на тихий размут!");
    }

    public String getNoSilentMutePermission() {
        return config.getString("commands.mute.no_silent_permission", "&#CF6679Нет права на тихий мут");
    }

    public String getMuteUsage() {
        return config.getString("commands.mute.usage", "&#B798A8Использование: &#7858E9/mute <ник> [время] [-s] [причина]");
    }

    public String getMuteTimeHelp() {
        return config.getString("commands.mute.time_help", "&#9878C9Время: &#7858E91d&#9878C9, &#7858E92h&#9878C9, &#7858E930m&#9878C9, &#7858E960s&#9878C9, &#7858E9perm");
    }

    public String getMuteSilentHelp() {
        return config.getString("commands.mute.silent_help", "&#B798A8-s &#9878C9— тихий мут (право &#7858E9lochat.mute.silent&#9878C9)");
    }

    public String getDefaultMuteReason() {
        return config.getString("commands.mute.default_reason", "Без причины");
    }

    public String getDefaultMuteDuration() {
        return config.getString("commands.mute.default_duration", "7d");
    }

    // ── ЛС ─────────────────────────────────────────────────────────────────────
    
    public String getNoReplyTarget() {
        return config.getString("private_messages.no_reply_target", "&#CF6679Нет цели для ответа");
    }

    public String getPlayerOffline() {
        return config.getString("private_messages.player_offline", "&#CF6679Игрок &#7858E9{player} &#CF6679не в сети");
    }

    public String getCannotMessageSelf() {
        return config.getString("private_messages.cannot_message_self", "&#CF6679Нельзя писать самому себе");
    }

    // ── Игнор ──────────────────────────────────────────────────────────────────
    
    public String getPlayerIgnored() {
        return config.getString("ignore.player_ignored", "&#9878C9Игрок &#7858E9{player} &#9878C9добавлен в игнор");
    }

    public String getPlayerUnignored() {
        return config.getString("ignore.player_unignored", "&#9878C9Игрок &#7858E9{player} &#9878C9убран из игнора");
    }

    public String getAlreadyIgnored() {
        return config.getString("ignore.already_ignored", "&#B798A8Игрок &#7858E9{player} &#B798A8уже в игноре");
    }

    public String getNotIgnored() {
        return config.getString("ignore.not_ignored", "&#B798A8Игрок &#7858E9{player} &#B798A8не в игноре");
    }

    public String getCannotIgnoreSelf() {
        return config.getString("ignore.cannot_ignore_self", "&#CF6679Нельзя игнорировать самого себя");
    }

    public String getIgnoreListEmpty() {
        return config.getString("ignore.ignore_list_empty", "&#B798A8Список игнора пуст");
    }

    public String getIgnoreListHeader() {
        return config.getString("ignore.ignore_list_header", "&#B798A8Игнорируемые игроки&#9878C9:");
    }

    // ── Модерация (варны / баны) ────────────────────────────────────────────────

    public String getModerationDefaultReason() {
        return config.getString("moderation.default_reason", "Без причины");
    }

    public String getModerationDurationPermanent() {
        return config.getString("moderation.duration_permanent", "навсегда");
    }

    /**
     * MiniMessage + legacy &# цвета; плейсхолдеры {reason},{moderator},{duration},{player}; перевод строки \\n
     */
    public String getBanKickMessage() {
        return config.getString("moderation.ban_kick_message", defaultBanKick());
    }

    private String defaultBanKick() {
        return "<gradient:#FF5555:#8B0000><bold>═══════ ДОСТУП ЗАПРЕЩЁН ═══════</bold></gradient>\n"
                + "<gray>Вы заблокированы на этом сервере.</gray>\n"
                + "<white>Причина: <gradient:#7858E9:#B798A8>{reason}</gradient></white>\n"
                + "<gray>Модератор: <white>{moderator}</white></gray>\n"
                + "<gray>Срок: <#7858E9>{duration}</#7858E9></gray>\n"
                + "<gradient:#FF5555:#8B0000><bold>════════════════════════</bold></gradient>";
    }

    public String getWarnReceived() {
        return config.getString("moderation.warn_received", "&#CF6679⚠ Вам выдано предупреждение! &#B798A8Причина: &#7858E9{reason}");
    }

    public String getWarnStaffConfirm() {
        return config.getString("moderation.warn_staff_confirm", "&#9878C9Игроку &#7858E9{player} &#9878C9выдан варн. &#B798A8Причина: &#7858E9{reason}");
    }

    public String getSilentWarnStaff() {
        return config.getString("moderation.silent_warn_staff", "&#B798A8[&#9878C9ТИХИЙ ВАРН&#B798A8] &#7858E9{player}&#9878C9: &#B798A8{reason}");
    }

    public String getBanUsage() {
        return config.getString("moderation.ban_usage", "&#B798A8Использование: &#7858E9/lban <ник> [время|perm] [причина]");
    }

    public String getBanConfirm() {
        return config.getString("moderation.ban_confirm", "&#9878C9Игрок &#7858E9{player} &#9878C9забанен. &#B798A8Срок: &#7858E9{duration}");
    }

    public String getUnbanConfirm() {
        return config.getString("moderation.unban_confirm", "&#9878C9Бан снят с &#7858E9{player}");
    }

    public String getNotBanned() {
        return config.getString("moderation.not_banned", "&#CF6679Игрок не забанен!");
    }

    public String getAlreadyBanned() {
        return config.getString("moderation.already_banned", "&#CF6679Игрок уже забанен!");
    }

    public String getWarnUsage() {
        return config.getString("moderation.warn_usage", "&#B798A8Использование: &#7858E9/warn <ник> [причина]");
    }

    public String getSilentWarnUsage() {
        return config.getString("moderation.silent_warn_usage", "&#B798A8Использование: &#7858E9/silentwarn <ник> [причина]");
    }
}
