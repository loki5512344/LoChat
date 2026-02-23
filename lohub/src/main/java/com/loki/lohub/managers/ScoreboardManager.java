package com.loki.lohub.managers;

import com.loki.lohub.LoHub;
import com.loki.lohub.utils.PlaceholderUtil;
import com.loki.lohub.utils.TextUtil;
import net.kyori.adventure.text.Component;
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

    private final LoHub plugin;
    private final Map<UUID, Scoreboard> scoreboards = new HashMap<>();
    private int taskId = -1;

    public ScoreboardManager(LoHub plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", false)) {
            return;
        }

        if (plugin.getConfig().getBoolean("scoreboard.refresh.enabled", true)) {
            int rate = plugin.getConfig().getInt("scoreboard.refresh.rate", 200);
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
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", false)) {
            return;
        }

        if (delayed) {
            int delay = plugin.getConfig().getInt("scoreboard.display_delay.server_enter", 60);
            Bukkit.getScheduler().runTaskLater(plugin, () -> createScoreboard(player), delay);
        } else {
            createScoreboard(player);
        }
    }

    private void createScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("lohub", "dummy", Component.text(""));

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        updateScoreboard(player, scoreboard, objective);

        player.setScoreboard(scoreboard);
        scoreboards.put(player.getUniqueId(), scoreboard);
    }

    private void updateScoreboard(Player player, Scoreboard scoreboard, Objective objective) {
        String title = plugin.getConfig().getString("scoreboard.title", "");
        title = PlaceholderUtil.parse(title, player);
        objective.displayName(Component.text(TextUtil.colorize(title)));

        List<String> lines = plugin.getConfig().getStringList("scoreboard.lines");
        int score = lines.size();

        for (String line : lines) {
            line = PlaceholderUtil.parse(line, player);
            line = TextUtil.colorize(line);

            String teamName = "line_" + score;
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
                team.addEntry(teamName);
            }

            team.prefix(Component.text(line));
            objective.getScore(teamName).setScore(score);
            score--;
        }
    }

    private void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = scoreboards.get(player.getUniqueId());
            if (scoreboard != null) {
                Objective objective = scoreboard.getObjective("lohub");
                if (objective != null) {
                    updateScoreboard(player, scoreboard, objective);
                }
            }
        }
    }

    public void removePlayer(Player player) {
        scoreboards.remove(player.getUniqueId());
    }
}
