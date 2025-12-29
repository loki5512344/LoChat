package com.loki.lochat.managers;

import com.loki.lochat.LoChat;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MentionManager {

    private final LoChat plugin;
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");
    
    // Хранит для какого игрока какие части сообщения выделить
    private final Map<Player, Set<String>> highlightedNicks = new HashMap<>();

    public MentionManager(LoChat plugin) {
        this.plugin = plugin;
    }

    /**
     * Обрабатывает упоминания в сообщении
     * Возвращает сообщение с подсвеченными никами
     */
    public String processMentions(String message, Set<Player> mentionedPlayers) {
        if (!plugin.getConfigManager().isMentionsEnabled()) {
            return message;
        }

        highlightedNicks.clear();
        String highlight = plugin.getConfigManager().getMentionHighlight();
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

    /**
     * Создаёт персонализированное сообщение для игрока
     * Если его ник упомянут — выделяет его особым образом
     */
    public String getPersonalizedMessage(String message, Player viewer) {
        if (!plugin.getConfigManager().isMentionsEnabled()) {
            return message;
        }

        String viewerName = viewer.getName();
        String result = message;
        
        // Проверяем упоминание через @ник
        String highlight = plugin.getConfigManager().getMentionHighlight();
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
            String selfHighlight = plugin.getConfigManager().getSelfMentionHighlight();
            result = nickMatcher.replaceAll(selfHighlight.replace("{player}", viewerName));
        }

        return result;
    }

    /**
     * Проверяет, упомянут ли игрок в сообщении (по нику без @)
     */
    public boolean isPlayerMentioned(String message, Player player) {
        String name = player.getName();
        Pattern pattern = Pattern.compile("(?i)\\b" + Pattern.quote(name) + "\\b");
        return pattern.matcher(message).find();
    }

    /**
     * Уведомляет упомянутых игроков звуком
     */
    public void notifyMentioned(Set<Player> players) {
        if (!plugin.getConfigManager().isMentionSoundEnabled()) {
            return;
        }

        String soundName = plugin.getConfigManager().getMentionSoundType();
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

    /**
     * Уведомляет игрока если его ник упомянут (без @)
     */
    public void notifyIfMentioned(String message, Player viewer, Player sender) {
        if (viewer.equals(sender)) return;
        if (!plugin.getConfigManager().isMentionSoundEnabled()) return;
        
        if (isPlayerMentioned(message, viewer)) {
            String soundName = plugin.getConfigManager().getMentionSoundType();
            try {
                Sound sound = Sound.valueOf(soundName);
                viewer.playSound(viewer.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
