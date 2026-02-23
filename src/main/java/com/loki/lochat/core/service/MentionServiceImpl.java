package com.loki.lochat.core.service;

import com.loki.lochat.api.service.MentionService;
import com.loki.lochat.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Реализация сервиса упоминаний
 */
public class MentionServiceImpl implements MentionService {
    private final ConfigManager configManager;
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");
    
    // Хранит для какого игрока какие части сообщения выделить
    private final Map<Player, Set<String>> highlightedNicks = new HashMap<>();

    public MentionServiceImpl(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public String processMentions(String message, Set<Player> mentionedPlayers) {
        if (!configManager.isMentionsEnabled()) {
            return message;
        }

        highlightedNicks.clear();
        String highlight = configManager.getMentionHighlight();
        Matcher matcher = MENTION_PATTERN.matcher(message);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String name = matcher.group(1);
            Player mentioned = Bukkit.getPlayer(name);

            if (mentioned != null && mentioned.isOnline()) {
                mentionedPlayers.add(mentioned);
                String replacement = highlight.replace("{player}", mentioned.getName());
                matcher.appendReplacement(result, replacement);
                
                // Запоминаем что этот игрок упомянут
                highlightedNicks.computeIfAbsent(mentioned, k -> new HashSet<>()).add(mentioned.getName());
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    @Override
    public String getPersonalizedMessage(String message, Player viewer) {
        if (!configManager.isMentionsEnabled()) {
            return message;
        }

        String viewerName = viewer.getName();
        String result = message;
        
        // Проверяем упоминание через @ник
        String highlight = configManager.getMentionHighlight();
        String highlightedName = highlight.replace("{player}", viewerName);
        
        // Если в сообщении есть выделенный ник этого игрока — добавляем дополнительное выделение
        if (result.contains(highlightedName)) {
            // Уже выделено через processMentions, добавляем эффект
            String extraHighlight = "<bold>" + highlightedName + "</bold>";
            result = result.replace(highlightedName, extraHighlight);
        }
        
        // Также проверяем просто ник в сообщении (без @)
        // Регистронезависимый поиск
        Pattern nickPattern = Pattern.compile("(?i)\\b" + Pattern.quote(viewerName) + "\\b");
        Matcher nickMatcher = nickPattern.matcher(result);
        
        if (nickMatcher.find()) {
            // Ник найден в сообщении — выделяем для этого игрока
            String selfHighlight = configManager.getSelfMentionHighlight();
            result = nickMatcher.replaceAll(selfHighlight.replace("{player}", viewerName));
        }

        return result;
    }

    @Override
    public boolean isPlayerMentioned(String message, Player player) {
        String name = player.getName();
        Pattern pattern = Pattern.compile("(?i)\\b" + Pattern.quote(name) + "\\b");
        return pattern.matcher(message).find();
    }

    @Override
    public void notifyMentioned(Set<Player> players) {
        if (!configManager.isMentionSoundEnabled()) {
            return;
        }

        String soundName = configManager.getMentionSoundType();
        Sound sound;
        try {
            sound = Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            sound = Sound.BLOCK_NOTE_BLOCK_PLING;
        }

        for (Player player : players) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }

    @Override
    public void notifyIfMentioned(String message, Player viewer, Player sender) {
        if (viewer.equals(sender)) return;
        if (!configManager.isMentionSoundEnabled()) return;
        
        if (isPlayerMentioned(message, viewer)) {
            String soundName = configManager.getMentionSoundType();
            try {
                Sound sound = Sound.valueOf(soundName);
                viewer.playSound(viewer.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
