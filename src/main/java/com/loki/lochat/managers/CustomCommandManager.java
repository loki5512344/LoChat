package com.loki.lochat.managers;

import com.loki.lochat.LoChat;
import com.loki.lochat.commands.CustomCommand;
import com.loki.lochat.utils.ChatFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomCommandManager {

    private final LoChat plugin;
    private final Map<String, CustomCommandData> commands;
    private File commandsFile;
    private FileConfiguration commandsConfig;

    public CustomCommandManager(LoChat plugin) {
        this.plugin = plugin;
        this.commands = new HashMap<>();
        loadCommands();
    }

    private void loadCommands() {
        commandsFile = new File(plugin.getDataFolder(), "custom-commands.yml");
        if (!commandsFile.exists()) {
            plugin.saveResource("custom-commands.yml", false);
        }
        
        commandsConfig = YamlConfiguration.loadConfiguration(commandsFile);
        
        // Очищаем старые команды
        commands.clear();
        
        // Загружаем команды из конфига
        for (String commandName : commandsConfig.getKeys(false)) {
            ConfigurationSection section = commandsConfig.getConfigurationSection(commandName);
            if (section == null) continue;
            
            CustomCommandData data = new CustomCommandData(
                    commandName,
                    section.getString("permission"),
                    section.getString("message", ""),
                    section.getStringList("aliases"),
                    section.getString("type", "chat"),
                    section.getString("target", "sender")
            );
            
            commands.put(commandName, data);
            
            // Регистрируем команду
            registerCommand(data);
        }
        
        plugin.getLogger().info("Загружено " + commands.size() + " кастомных команд");
    }

    private void registerCommand(CustomCommandData data) {
        try {
            // Получаем CommandMap через рефлексию
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            
            // Создаем и регистрируем команду
            CustomCommand command = new CustomCommand(data.name, this);
            commandMap.register("lochat", command);
            
            // Регистрируем алиасы
            for (String alias : data.aliases) {
                CustomCommand aliasCommand = new CustomCommand(alias, this);
                commandMap.register("lochat", aliasCommand);
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка регистрации команды " + data.name + ": " + e.getMessage());
        }
    }

    public void reload() {
        loadCommands();
    }

    public boolean executeCustomCommand(String commandName, Player player, String[] args) {
        CustomCommandData data = commands.get(commandName);
        if (data == null) {
            // Проверяем алиасы
            for (CustomCommandData cmd : commands.values()) {
                if (cmd.aliases.contains(commandName)) {
                    data = cmd;
                    break;
                }
            }
        }
        
        if (data == null) return false;
        
        // Проверяем права
        if (data.permission != null && !player.hasPermission(data.permission)) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getNoPermission()));
            return true;
        }
        
        // Обрабатываем плейсхолдеры
        String message = processPlaceholders(data.message, player, args);
        
        // Выполняем команду в зависимости от типа
        switch (data.type.toLowerCase()) {
            case "chat" -> sendChatMessage(data.target, player, message);
            case "broadcast" -> sendBroadcast(data.target, message);
            case "title" -> sendTitle(data.target, player, message);
            case "actionbar" -> sendActionBar(data.target, player, message);
            default -> sendChatMessage(data.target, player, message);
        }
        
        return true;
    }

    private String processPlaceholders(String message, Player player, String[] args) {
        String result = message;
        
        // Базовые плейсхолдеры
        result = result.replace("{player}", player.getName());
        result = result.replace("{x}", String.valueOf(player.getLocation().getBlockX()));
        result = result.replace("{y}", String.valueOf(player.getLocation().getBlockY()));
        result = result.replace("{z}", String.valueOf(player.getLocation().getBlockZ()));
        result = result.replace("{world}", player.getWorld().getName());
        
        // Аргументы команды
        for (int i = 0; i < args.length; i++) {
            result = result.replace("{arg" + i + "}", args[i]);
        }
        result = result.replace("{args}", String.join(" ", args));
        
        // PlaceholderAPI если доступен
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                // Используем безопасный способ вызова PlaceholderAPI
                Class<?> papiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                java.lang.reflect.Method setPlaceholdersMethod = papiClass.getMethod("setPlaceholders", org.bukkit.entity.Player.class, String.class);
                result = (String) setPlaceholdersMethod.invoke(null, player, result);
            } catch (Exception e) {
                // Если PlaceholderAPI недоступен или произошла ошибка, просто пропускаем
                plugin.getLogger().warning("Ошибка при обработке PlaceholderAPI плейсхолдеров: " + e.getMessage());
            }
        }
        
        return result;
    }

    private void sendChatMessage(String target, Player sender, String message) {
        Component component = ChatFormatter.parse(message);
        
        switch (target.toLowerCase()) {
            case "sender" -> sender.sendMessage(component);
            case "all" -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(component);
                }
            }
            default -> {
                if (target.startsWith("permission:")) {
                    String permission = target.substring(11);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission(permission)) {
                            player.sendMessage(component);
                        }
                    }
                } else {
                    sender.sendMessage(component);
                }
            }
        }
    }

    private void sendBroadcast(String target, String message) {
        Component component = ChatFormatter.parse(message);
        
        switch (target.toLowerCase()) {
            case "all" -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(component);
                }
            }
            default -> {
                if (target.startsWith("permission:")) {
                    String permission = target.substring(11);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission(permission)) {
                            player.sendMessage(component);
                        }
                    }
                }
            }
        }
    }

    private void sendTitle(String target, Player sender, String message) {
        String[] parts = message.split("\\n", 2);
        Component title = ChatFormatter.parse(parts[0]);
        Component subtitle = parts.length > 1 ? ChatFormatter.parse(parts[1]) : Component.empty();
        
        Title titleObj = Title.title(title, subtitle, 
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500)));
        
        switch (target.toLowerCase()) {
            case "sender" -> sender.showTitle(titleObj);
            case "all" -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.showTitle(titleObj);
                }
            }
            default -> {
                if (target.startsWith("permission:")) {
                    String permission = target.substring(11);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission(permission)) {
                            player.showTitle(titleObj);
                        }
                    }
                } else {
                    sender.showTitle(titleObj);
                }
            }
        }
    }

    private void sendActionBar(String target, Player sender, String message) {
        Component component = ChatFormatter.parse(message);
        
        switch (target.toLowerCase()) {
            case "sender" -> sender.sendActionBar(component);
            case "all" -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendActionBar(component);
                }
            }
            default -> {
                if (target.startsWith("permission:")) {
                    String permission = target.substring(11);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission(permission)) {
                            player.sendActionBar(component);
                        }
                    }
                } else {
                    sender.sendActionBar(component);
                }
            }
        }
    }

    public Map<String, CustomCommandData> getCommands() {
        return commands;
    }

    public static class CustomCommandData {
        public final String name;
        public final String permission;
        public final String message;
        public final List<String> aliases;
        public final String type;
        public final String target;

        public CustomCommandData(String name, String permission, String message, 
                               List<String> aliases, String type, String target) {
            this.name = name;
            this.permission = permission;
            this.message = message;
            this.aliases = aliases;
            this.type = type;
            this.target = target;
        }
    }
}