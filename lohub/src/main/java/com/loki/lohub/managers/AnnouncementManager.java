package com.loki.lohub.managers;

import com.loki.lohub.LoHub;
import com.loki.lohub.common.ConfigHelper;
import com.loki.lohub.common.TextFormatter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementManager {

    private static final String CONFIG_PATH = "announcements";

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
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(CONFIG_PATH + ".announcements");
        if (section == null) {
            return;
        }

        section.getKeys(false).forEach(key -> {
            List<String> messages = section.getStringList(key);
            if (!messages.isEmpty()) {
                announcements.add(new Announcement(key, messages));
            }
        });
    }

    public void start() {
        if (!ConfigHelper.isEnabled(plugin.getConfig(), CONFIG_PATH)) {
            return;
        }

        if (announcements.isEmpty()) {
            return;
        }

        int delay = plugin.getConfig().getInt(CONFIG_PATH + ".delay", 60) * 20;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::broadcast, delay, delay);
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private void broadcast() {
        if (!hasEnoughPlayers()) {
            return;
        }

        if (announcements.isEmpty()) {
            return;
        }

        Announcement announcement = getNextAnnouncement();
        Bukkit.getOnlinePlayers().forEach(player -> sendAnnouncement(player, announcement));
    }

    private boolean hasEnoughPlayers() {
        int required = plugin.getConfig().getInt(CONFIG_PATH + ".required_players", 1);
        return Bukkit.getOnlinePlayers().size() >= required;
    }

    private Announcement getNextAnnouncement() {
        Announcement announcement = announcements.get(currentIndex);
        currentIndex = (currentIndex + 1) % announcements.size();
        return announcement;
    }

    private void sendAnnouncement(Player player, Announcement announcement) {
        announcement.messages().forEach(message -> {
            String formatted = TextFormatter.format(message, player);
            player.sendMessage(Component.text(formatted));
        });

        if (shouldPlaySound()) {
            playSound(player);
        }
    }

    private boolean shouldPlaySound() {
        return plugin.getConfig().getBoolean(CONFIG_PATH + ".sound.enabled", true);
    }

    private void playSound(Player player) {
        String soundStr = plugin.getConfig().getString(CONFIG_PATH + ".sound.value", "BLOCK_NOTE_BLOCK_PLING");
        float volume = (float) plugin.getConfig().getDouble(CONFIG_PATH + ".sound.volume", 1.0);
        float pitch = (float) plugin.getConfig().getDouble(CONFIG_PATH + ".sound.pitch", 1.0);

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
