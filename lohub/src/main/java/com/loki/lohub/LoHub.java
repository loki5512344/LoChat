package com.loki.lohub;

import com.loki.lohub.config.ConfigManager;
import com.loki.lohub.listeners.HubProtectionListener;
import com.loki.lohub.listeners.PlayerJoinListener;
import com.loki.lohub.managers.ActionManager;
import com.loki.lohub.managers.AnnouncementManager;
import com.loki.lohub.managers.CooldownManager;
import com.loki.lohub.managers.HotbarManager;
import com.loki.lohub.managers.PlayerHiderManager;
import com.loki.lohub.managers.RegionManager;
import com.loki.lohub.managers.ScoreboardManager;
import com.loki.lohub.managers.SpawnManager;
import com.loki.lohub.managers.TablistManager;
import com.loki.lohub.utils.PlaceholderUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class LoHub extends JavaPlugin {

    private ConfigManager configManager;
    private SpawnManager spawnManager;
    private RegionManager regionManager;
    private ActionManager actionManager;
    private CooldownManager cooldownManager;
    private HotbarManager hotbarManager;
    private PlayerHiderManager playerHiderManager;
    private ScoreboardManager scoreboardManager;
    private TablistManager tablistManager;
    private AnnouncementManager announcementManager;

    @Override
    public void onEnable() {
        getLogger().info("Loading LoHub v" + getDescription().getVersion());

        saveDefaultConfig();

        PlaceholderUtil.init();

        this.configManager = new ConfigManager(this);
        this.spawnManager = new SpawnManager(this);
        this.regionManager = new RegionManager(this);
        this.actionManager = new ActionManager(this);
        this.cooldownManager = new CooldownManager();
        this.hotbarManager = new HotbarManager(this);
        this.playerHiderManager = new PlayerHiderManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.tablistManager = new TablistManager(this);
        this.announcementManager = new AnnouncementManager(this);

        registerListeners();
        registerCommands();

        regionManager.createHubRegion();

        scoreboardManager.start();
        tablistManager.start();
        announcementManager.start();

        getLogger().info("LoHub successfully loaded!");
    }

    @Override
    public void onDisable() {
        scoreboardManager.stop();
        tablistManager.stop();
        announcementManager.stop();
        getLogger().info("LoHub disabled!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new HubProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new com.loki.lohub.listeners.HotbarListener(this), this);
        getServer().getPluginManager().registerEvents(new com.loki.lohub.listeners.LaunchpadListener(this), this);
        getServer().getPluginManager().registerEvents(new com.loki.lohub.listeners.DoubleJumpListener(this), this);
    }

    private void registerCommands() {
        getCommand("hub").setExecutor(new com.loki.lohub.commands.HubCommand(this));
        getCommand("sethub").setExecutor(new com.loki.lohub.commands.SetHubCommand(this));
        getCommand("lohub").setExecutor(new com.loki.lohub.commands.LoHubCommand(this));
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public HotbarManager getHotbarManager() {
        return hotbarManager;
    }

    public PlayerHiderManager getPlayerHiderManager() {
        return playerHiderManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public TablistManager getTablistManager() {
        return tablistManager;
    }

    public AnnouncementManager getAnnouncementManager() {
        return announcementManager;
    }
}
