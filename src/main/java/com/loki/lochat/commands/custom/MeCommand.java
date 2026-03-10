package com.loki.lochat.commands.custom;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Команда действия от третьего лица
 */
public class MeCommand implements CommandExecutor {

    private final LoChat plugin;

    public MeCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatFormatter.parse("<red>Эта команда только для игроков!</red>"));
            return true;
        }

        if (!player.hasPermission("chat.me")) {
            player.sendMessage(ChatFormatter.parse("<red>У вас нет прав для использования этой команды!</red>"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatFormatter.parse("<red>Использование: /me <действие></red>"));
            return true;
        }

        // Проверяем мут
        if (plugin.getMuteService() != null && plugin.getMuteService().isMuted(player.getUniqueId())) {
            String muteMessage = plugin.getMessagesConfig().getString("mute.you-muted", 
                "<red>Вы замучены!</red>");
            player.sendMessage(ChatFormatter.parse(muteMessage));
            return true;
        }

        // Собираем действие из аргументов
        String action = String.join(" ", args);

        // Получаем отображаемое имя игрока (с градиентом если есть)
        String displayName = player.getDisplayName();
        if (plugin.getGradientModule() != null) {
            displayName = plugin.getGradientModule().getFormattedName(player);
        }

        // Форматируем сообщение действия
        String message = String.format("<gray>* %s %s</gray>", displayName, action);

        // Отправляем всем игрокам в радиусе (как локальный чат)
        int radius = plugin.getConfig().getInt("chat.local.radius", 100);
        
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getWorld().equals(player.getWorld()) &&
                onlinePlayer.getLocation().distance(player.getLocation()) <= radius) {
                onlinePlayer.sendMessage(ChatFormatter.parse(message));
            }
        }

        // Логируем действие
        plugin.getLogger().info(String.format("[ME] %s: %s", player.getName(), action));

        return true;
    }
}