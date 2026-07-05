package com.loki.lochat.api.service.pm;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface PrivateMessageService {

    void setLastConversation(UUID player, UUID target);

    Optional<UUID> getLastConversation(UUID player);

    void removeConversation(UUID player);

    boolean hasConversation(UUID player);

    void sendPrivateMessage(CommandSender sender, Player target, String message);
}
