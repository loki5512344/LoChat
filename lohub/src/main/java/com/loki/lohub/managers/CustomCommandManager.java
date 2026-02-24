package com.loki.lohub.managers;

import com.loki.lohub.LoHub;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomCommandManager {

    private final LoHub plugin;
    private final Map<String, CustomCommand> commands = new HashMap<>();

    public CustomCommandManager(LoHub plugin) {
        this.plugin = plugin;
        loadCommands();
    }

    private void loadCommands() {
        commands.clear();
        ConfigurationSection section = plugin.getConfigManager().getCommands().getConfigurationSection("custom_commands");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection cmdSection = section.getConfigurationSection(key);
            if (cmdSection == null) {
                continue;
            }

            List<String> aliases = cmdSection.getStringList("aliases");
            String permission = cmdSection.getString("permission");
            List<String> actions = cmdSection.getStringList("actions");

            if (aliases.isEmpty() || actions.isEmpty()) {
                continue;
            }

            CustomCommand cmd = new CustomCommand(key, aliases, permission, actions);
            for (String alias : aliases) {
                commands.put(alias.toLowerCase(), cmd);
            }
        }
    }

    public CustomCommand getCommand(String alias) {
        return commands.get(alias.toLowerCase());
    }

    public boolean hasCommand(String alias) {
        return commands.containsKey(alias.toLowerCase());
    }

    public void reload() {
        loadCommands();
    }

    public record CustomCommand(String name, List<String> aliases, String permission, List<String> actions) {
    }
}
