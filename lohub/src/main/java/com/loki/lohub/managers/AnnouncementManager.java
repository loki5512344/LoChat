package com.loki.lohub.managers;

import com.loki.lohub.LoHub;
import com.loki.lohub.utils.PlaceholderUtil;
import com.loki.lohub.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementManager {

    private final LoHub plugin;
    private final List<Announcement> announcements = new ArrayList<>();
    private int currentIndex = 0;
    private int taskId = -1;

    public AnnouncementManager(LoHub plugin) {
        this.plugin = plugin;
        loadAnnouncements();
    }

    private void loadAnnouncements() {
        announcements.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("announcements.announcements");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            List<String> messages = section.getStringList(key);
            if (!messages.isEmpty()) {
                announcements.add(new Announcement(key, messages));
            }
        }
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("announcements.enabled", false)) {
            return;
        }

        if (announcements.isEmpty()) {
            return;
        }

        int delay = plugin.getConfig().getInt("announcements.delay", 60) * 20;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::broadcast, delay, delay);
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private void broadcast() {
        int requiredPlayers = plugin.getConfig().getInt("announcements.required_players", 1);
        if (Bukkit.getOnlinePlayers().size() < requiredPlayers) {
            return;
        }

        if (announcements.isEmpty()) {
            return;
        }

        Announcement announcement = announcements.get(currentIndex);
        currentIndex = (currentIndex + 1) % announcements.size();

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String message : announcement.messages()) {
                String formatted = PlaceholderUtil.parse(message, player);
                player.sendMessage(Component.text(TextUtil.colorize(formatted)));
            }

            if (plugin.getConfig().getBoolean("announcements.sound.enabled", true)) {
                playSound(player);
            }
        }
    }

    private void playSound(Player player) {
        String soundStr = plugin.getConfig().getString("announcements.sound.value", "BLOCK_NOTE_BLOCK_PLING");
        float volume = (float) plugin.getConfig().getDouble("announcements.sound.volume", 1.0);
        float pitch = (float) plugin.getConfig().getDouble("announcements.sound.pitch", 1.0);

        try {
            Sound sound = Sound.valueOf(soundStr);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + soundStr);
        }
    }

    public void reload() {
        stop();
        loadAnnouncements();
        start();
    }

    private record Announcement(String name, List<String> messages) {
    }
}
