package com.loki.lohub.managers;

import com.loki.lohub.LoHub;
import com.loki.lohub.common.ConfigHelper;
import com.loki.lohub.common.TextFormatter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class TablistManager {

    private static final String CONFIG_PATH = "tablist";
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();

    private final LoHub plugin;
    private int taskId = -1;

    public TablistManager(LoHub plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!ConfigHelper.isEnabled(plugin.getConfig(), CONFIG_PATH)) {
            return;
        }

        if (shouldRefresh()) {
            int rate = plugin.getConfig().getInt(CONFIG_PATH + ".refresh.rate", 400);
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
        if (!ConfigHelper.isEnabled(plugin.getConfig(), CONFIG_PATH)) {
            return;
        }

        updateTablist(player);
    }

    private void updateTablist(Player player) {
        String header = formatLines(CONFIG_PATH + ".header", player);
        String footer = formatLines(CONFIG_PATH + ".footer", player);

        player.sendPlayerListHeaderAndFooter(
                SERIALIZER.deserialize(header),
                SERIALIZER.deserialize(footer)
        );
    }

    private String formatLines(String path, Player player) {
        List<String> lines = plugin.getConfig().getStringList(path);
        return TextFormatter.joinLines(lines, player, "\n");
    }

    private void updateAll() {
        Bukkit.getOnlinePlayers().forEach(this::updateTablist);
    }

    private boolean shouldRefresh() {
        return plugin.getConfig().getBoolean(CONFIG_PATH + ".refresh.enabled", true);
    }
}
