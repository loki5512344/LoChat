package com.loki.lohub.managers;

import com.loki.lohub.LoHub;
import com.loki.lohub.common.ConfigHelper;
import com.loki.lohub.common.TextFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private static final String CONFIG_PATH = "scoreboard";
    private static final String OBJECTIVE_NAME = "lohub";
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();

    private final LoHub plugin;
    private final Map<UUID, Scoreboard> scoreboards = new HashMap<>();
    private int taskId = -1;

    public ScoreboardManager(LoHub plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!ConfigHelper.isEnabled(plugin.getConfig(), CONFIG_PATH)) {
            return;
        }

        if (shouldRefresh()) {
            int rate = plugin.getConfig().getInt(CONFIG_PATH + ".refresh.rate", 200);
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::updateAll, 0L, rate);
        }
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        scoreboards.clear();
    }

    public void setScoreboard(Player player, boolean delayed) {
        if (!ConfigHelper.isEnabled(plugin.getConfig(), CONFIG_PATH)) {
            return;
        }

        if (delayed) {
            int delay = plugin.getConfig().getInt(CONFIG_PATH + ".display_delay.server_enter", 60);
            Bukkit.getScheduler().runTaskLater(plugin, () -> createScoreboard(player), delay);
        } else {
            createScoreboard(player);
        }
    }

    private void createScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy", Component.text(""));

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        updateScoreboard(player, scoreboard, objective);

        player.setScoreboard(scoreboard);
        scoreboards.put(player.getUniqueId(), scoreboard);
    }

    private void updateScoreboard(Player player, Scoreboard scoreboard, Objective objective) {
        updateTitle(player, objective);
        updateLines(player, scoreboard, objective);
    }

    private void updateTitle(Player player, Objective objective) {
        String title = plugin.getConfig().getString(CONFIG_PATH + ".title", "");
        objective.displayName(SERIALIZER.deserialize(TextFormatter.format(title, player)));
    }

    private void updateLines(Player player, Scoreboard scoreboard, Objective objective) {
        List<String> lines = plugin.getConfig().getStringList(CONFIG_PATH + ".lines");
        int score = lines.size();

        for (String line : lines) {
            String formatted = TextFormatter.format(line, player);
            String teamName = "line_" + score;

            Team team = getOrCreateTeam(scoreboard, teamName);
            team.prefix(SERIALIZER.deserialize(formatted));
            objective.getScore(teamName).setScore(score);
            score--;
        }
    }

    private Team getOrCreateTeam(Scoreboard scoreboard, String teamName) {
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            team.addEntry(teamName);
        }
        return team;
    }

    private void updateAll() {
        scoreboards.forEach((uuid, scoreboard) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
                if (objective != null) {
                    updateScoreboard(player, scoreboard, objective);
                }
            }
        });
    }

    public void removePlayer(Player player) {
        scoreboards.remove(player.getUniqueId());
    }

    private boolean shouldRefresh() {
        return plugin.getConfig().getBoolean(CONFIG_PATH + ".refresh.enabled", true);
    }
}
