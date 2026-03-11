package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.commands.custom.*;
import org.bukkit.command.CommandExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * Менеджер кастомных команд
 */
public class CustomCommandManager {

    private final LoChat plugin;
    private final Map<String, CommandExecutor> commands;

    public CustomCommandManager(LoChat plugin) {
        this.plugin = plugin;
        this.commands = new HashMap<>();
        registerCommands();
    }

    /**
     * Регистрирует все кастомные команды
     */
    private void registerCommands() {
        // Информационные команды
        commands.put("help", new HelpCommand(plugin));
        commands.put("rules", new RulesCommand(plugin));
        
        // Утилиты
        commands.put("coords", new CoordsCommand(plugin));
        commands.put("ping", new PingCommand(plugin));
        commands.put("stats", new StatsCommand(plugin));
        
        // Чат команды
        commands.put("me", new MeCommand(plugin));
        commands.put("sounds", new SoundsCommand(plugin));
        
        // TODO: Добавить остальные команды
        // commands.put("website", new WebsiteCommand(plugin));
        // commands.put("time", new TimeCommand(plugin));
        // commands.put("list", new ListCommand(plugin));
        // commands.put("top", new TopCommand(plugin));
        // commands.put("afk", new AfkCommand(plugin));
        // commands.put("back", new BackCommand(plugin));
        // commands.put("broadcast", new BroadcastCommand(plugin));
        // commands.put("maintenance", new MaintenanceCommand(plugin));
        // commands.put("motd", new MotdCommand(plugin));
        // commands.put("tps", new TpsCommand(plugin));
        // commands.put("memory", new MemoryCommand(plugin));
        // commands.put("version", new VersionCommand(plugin));
        // commands.put("reload", new ReloadCommand(plugin));
        // commands.put("automessage", new AutoMessageCommand(plugin));
    }

    /**
     * Регистрирует команды в Bukkit
     */
    public void registerBukkitCommands() {
        for (Map.Entry<String, CommandExecutor> entry : commands.entrySet()) {
            String commandName = entry.getKey();
            CommandExecutor executor = entry.getValue();
            
            if (plugin.getCommand(commandName) != null) {
                plugin.getCommand(commandName).setExecutor(executor);
                plugin.getLogger().info("Зарегистрирована кастомная команда: /" + commandName);
            } else {
                plugin.getLogger().warning("Не удалось зарегистрировать команду: /" + commandName + 
                    " (не найдена в plugin.yml)");
            }
        }
    }

    /**
     * Получить исполнителя команды
     */
    public CommandExecutor getCommandExecutor(String command) {
        return commands.get(command.toLowerCase());
    }

    /**
     * Проверить, является ли команда кастомной
     */
    public boolean isCustomCommand(String command) {
        return commands.containsKey(command.toLowerCase());
    }

    /**
     * Получить все зарегистрированные команды
     */
    public Map<String, CommandExecutor> getCommands() {
        return new HashMap<>(commands);
    }

    /**
     * Перезагрузить команды
     */
    public void reload() {
        commands.clear();
        registerCommands();
        registerBukkitCommands();
        plugin.getLogger().info("Кастомные команды перезагружены!");
    }
}