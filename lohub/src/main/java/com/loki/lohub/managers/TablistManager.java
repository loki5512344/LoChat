package com.loki.lohub.managers;

import com.loki.lohub.LoHub;
import com.loki.lohub.utils.PlaceholderUtil;
import com.loki.lohub.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class TablistManager {

    private final LoHub plugin;
    private int taskId = -1;

    public TablistManager(LoHub plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("tablist.enabled", false)) {
            return;
        }

        if (plugin.getConfig().getBoolean("tablist.refresh.enabled", true)) {
            int rate = plugin.getConfig().getInt("tablist.refresh.rate", 400);
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::updateAll, 0L, rate);
        }
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    public void setTablist(Player player) {
        if (!plugin.getConfig().getBoolean("tablist.enabled", false)) {
            return;
        }

        updateTablist(player);
    }

    private void updateTablist(Player player) {
        List<String> headerLines = plugin.getConfig().getStringList("tablist.header");
        List<String> footerLines = plugin.getConfig().getStringList("tablist.footer");

        StringBuilder header = new StringBuilder();
        for (int i = 0; i < headerLines.size(); i++) {
            String line = PlaceholderUtil.parse(headerLines.get(i), player);
            header.append(TextUtil.colorize(line));
            if (i < headerLines.size() - 1) {
                header.append("\n");
            }
        }

        StringBuilder footer = new StringBuilder();
        for (int i = 0; i < footerLines.size(); i++) {
            String line = PlaceholderUtil.parse(footerLines.get(i), player);
            footer.append(TextUtil.colorize(line));
            if (i < footerLines.size() - 1) {
                footer.append("\n");
            }
        }

        player.sendPlayerListHeaderAndFooter(
                Component.text(header.toString()),
                Component.text(footer.toString())
        );
    }

    private void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTablist(player);
        }
    }
}
