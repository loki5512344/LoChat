package ru.lovar.gradientnick.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.lovar.gradientnick.GradientNick;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final GradientNick plugin;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private Connection sqliteConnection;
    private boolean useSqlite;

    public DataManager(GradientNick plugin) {
        this.plugin = plugin;
        this.useSqlite = plugin.getConfigManager().getStorageType().equalsIgnoreCase("SQLITE");
        
        if (useSqlite) {
            initSqlite();
        } else {
            loadYamlData();
        }
    }

    private void initSqlite() {
        try {
            File dbFile = new File(plugin.getDataFolder(), plugin.getConfigManager().getSqliteFile());
            sqliteConnection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            
            try (Statement stmt = sqliteConnection.createStatement()) {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS player_data (
                        uuid TEXT PRIMARY KEY,
                        prefix TEXT,
                        colors TEXT,
                        color_enabled INTEGER DEFAULT 1,
                        prefix_enabled INTEGER DEFAULT 1,
                        prefix_purchased INTEGER DEFAULT 0,
                        last_color_change INTEGER DEFAULT 0,
                        last_prefix_change INTEGER DEFAULT 0
                    )
                """);
            }
            plugin.getLogger().info("SQLite база данных инициализирована.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка инициализации SQLite: " + e.getMessage());
            useSqlite = false;
        }
    }

    private void loadYamlData() {
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) return;
        
        FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        for (String uuidStr : data.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                PlayerData playerData = new PlayerData(uuid);
                playerData.setPrefix(data.getString(uuidStr + ".prefix"));
                playerData.setColors(data.getStringList(uuidStr + ".colors"));
                playerData.setColorEnabled(data.getBoolean(uuidStr + ".color-enabled", true));
                playerData.setPrefixEnabled(data.getBoolean(uuidStr + ".prefix-enabled", true));
                playerData.setPrefixPurchased(data.getBoolean(uuidStr + ".prefix-purchased", false));
                playerData.setLastColorChange(data.getLong(uuidStr + ".last-color-change", 0));
                playerData.setLastPrefixChange(data.getLong(uuidStr + ".last-prefix-change", 0));
                cache.put(uuid, playerData);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::loadPlayerData);
    }

    private PlayerData loadPlayerData(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        
        if (useSqlite && sqliteConnection != null) {
            try (PreparedStatement stmt = sqliteConnection.prepareStatement(
                    "SELECT prefix, colors, color_enabled, prefix_enabled, prefix_purchased, last_color_change, last_prefix_change FROM player_data WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    data.setPrefix(rs.getString("prefix"));
                    String colorsStr = rs.getString("colors");
                    if (colorsStr != null && !colorsStr.isEmpty()) {
                        data.setColors(List.of(colorsStr.split(",")));
                    }
                    data.setColorEnabled(rs.getBoolean("color_enabled"));
                    data.setPrefixEnabled(rs.getBoolean("prefix_enabled"));
                    data.setPrefixPurchased(rs.getBoolean("prefix_purchased"));
                    data.setLastColorChange(rs.getLong("last_color_change"));
                    data.setLastPrefixChange(rs.getLong("last_prefix_change"));
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Ошибка загрузки данных игрока: " + e.getMessage());
            }
        }
        
        return data;
    }

    public void savePlayerData(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) return;
        
        if (useSqlite && sqliteConnection != null) {
            try (PreparedStatement stmt = sqliteConnection.prepareStatement(
                    "INSERT OR REPLACE INTO player_data (uuid, prefix, colors, color_enabled, prefix_enabled, prefix_purchased, last_color_change, last_prefix_change) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, data.getPrefix());
                stmt.setString(3, data.hasColors() ? String.join(",", data.getColors()) : "");
                stmt.setBoolean(4, data.isColorEnabled());
                stmt.setBoolean(5, data.isPrefixEnabled());
                stmt.setBoolean(6, data.isPrefixPurchased());
                stmt.setLong(7, data.getLastColorChange());
                stmt.setLong(8, data.getLastPrefixChange());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Ошибка сохранения данных игрока: " + e.getMessage());
            }
        }
    }

    public void saveAll() {
        if (useSqlite) {
            for (UUID uuid : cache.keySet()) {
                savePlayerData(uuid);
            }
        } else {
            saveYamlData();
        }
    }

    private void saveYamlData() {
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        FileConfiguration data = new YamlConfiguration();
        
        for (Map.Entry<UUID, PlayerData> entry : cache.entrySet()) {
            String path = entry.getKey().toString();
            PlayerData pd = entry.getValue();
            data.set(path + ".prefix", pd.getPrefix());
            data.set(path + ".colors", pd.getColors());
            data.set(path + ".color-enabled", pd.isColorEnabled());
            data.set(path + ".prefix-enabled", pd.isPrefixEnabled());
            data.set(path + ".prefix-purchased", pd.isPrefixPurchased());
            data.set(path + ".last-color-change", pd.getLastColorChange());
            data.set(path + ".last-prefix-change", pd.getLastPrefixChange());
        }
        
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Ошибка сохранения data.yml: " + e.getMessage());
        }
    }
}
