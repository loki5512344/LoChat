package com.loki.lohub.config;

import com.loki.lohub.LoHub;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigManager {

    private final LoHub plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration commands;
    private FileConfiguration serverSelector;
    private FileConfiguration data;
    private File messagesFile;
    private File commandsFile;
    private File serverSelectorFile;
    private File dataFile;

    public ConfigManager(LoHub plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadMessages();
        loadCommands();
        loadServerSelector();
        loadData();
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            try {
                messagesFile.getParentFile().mkdirs();
                InputStream in = plugin.getResource("messages.yml");
                if (in != null) {
                    Files.copy(in, messagesFile.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create messages.yml!");
                e.printStackTrace();
            }
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void loadCommands() {
        commandsFile = new File(plugin.getDataFolder(), "commands.yml");
        
        if (!commandsFile.exists()) {
            try {
                commandsFile.getParentFile().mkdirs();
                InputStream in = plugin.getResource("commands.yml");
                if (in != null) {
                    Files.copy(in, commandsFile.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create commands.yml!");
                e.printStackTrace();
            }
        }
        
        commands = YamlConfiguration.loadConfiguration(commandsFile);
    }

    private void loadServerSelector() {
        serverSelectorFile = new File(plugin.getDataFolder(), "serverselector.yml");
        
        if (!serverSelectorFile.exists()) {
            try {
                serverSelectorFile.getParentFile().mkdirs();
                InputStream in = plugin.getResource("serverselector.yml");
                if (in != null) {
                    Files.copy(in, serverSelectorFile.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create serverselector.yml!");
                e.printStackTrace();
            }
        }
        
        serverSelector = YamlConfiguration.loadConfiguration(serverSelectorFile);
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                InputStream in = plugin.getResource("data.yml");
                if (in != null) {
                    Files.copy(in, dataFile.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml!");
                e.printStackTrace();
            }
        }
        
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadMessages();
        loadCommands();
        loadServerSelector();
        loadData();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getCommands() {
        return commands;
    }

    public FileConfiguration getServerSelector() {
        return serverSelector;
    }

    public FileConfiguration getData() {
        return data;
    }

    public void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml!");
            e.printStackTrace();
        }
    }

    public String getMessage(String path) {
        String message = messages.getString(path, "&#FF6B6BMessage not found: " + path);
        String prefix = messages.getString("prefix", "&#00D4FF[LoHub] ");
        return message.replace("{prefix}", prefix);
    }

    public boolean isProtectionEnabled() {
        return config.getBoolean("protection.enabled", true);
    }

    public boolean isBlockBreakProtected() {
        return config.getBoolean("protection.block-break", true);
    }

    public boolean isBlockPlaceProtected() {
        return config.getBoolean("protection.block-place", true);
    }

    public boolean isPvPProtected() {
        return config.getBoolean("protection.pvp", true);
    }

    public boolean isItemDropProtected() {
        return config.getBoolean("protection.item-drop", true);
    }

    public boolean isItemPickupProtected() {
        return config.getBoolean("protection.item-pickup", true);
    }

    public boolean isFoodLevelProtected() {
        return config.getBoolean("protection.food-level", true);
    }

    public boolean isMobSpawningProtected() {
        return config.getBoolean("protection.mob-spawning", true);
    }
}
