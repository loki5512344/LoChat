package com.loki.lochat.gradient.hooks;

import com.loki.lochat.gradient.util.GradientConstants;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Хук для LuckPerms
 */
public class GradientLuckPermsHook {

    private LuckPerms luckPerms;
    private boolean enabled;

    public GradientLuckPermsHook(JavaPlugin plugin) {
        this.enabled = false;

        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            try {
                this.luckPerms = LuckPermsProvider.get();
                this.enabled = true;
                plugin.getLogger().info("Gradient: LuckPerms подключен!");
            } catch (Exception e) {
                plugin.getLogger().warning("Gradient: Не удалось подключить LuckPerms: " + e.getMessage());
            }
        }
    }

    public boolean isEnabled() {
        return enabled && luckPerms != null;
    }

    public void setPrefix(Player player, String prefix) {
        if (!isEnabled()) return;

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        user.data().clear(node ->
                node instanceof PrefixNode && ((PrefixNode) node).getPriority() == GradientConstants.LUCKPERMS_PREFIX_PRIORITY
        );

        if (prefix != null && !prefix.isEmpty()) {
            PrefixNode prefixNode = PrefixNode.builder(prefix, GradientConstants.LUCKPERMS_PREFIX_PRIORITY).build();
            user.data().add(prefixNode);
        }

        luckPerms.getUserManager().saveUser(user);
    }

    public void setPrefix(UUID uuid, String prefix) {
        if (!isEnabled()) return;

        luckPerms.getUserManager().loadUser(uuid).thenAccept(user -> {
            if (user == null) return;

            user.data().clear(node ->
                    node instanceof PrefixNode && ((PrefixNode) node).getPriority() == GradientConstants.LUCKPERMS_PREFIX_PRIORITY
            );

            if (prefix != null && !prefix.isEmpty()) {
                PrefixNode prefixNode = PrefixNode.builder(prefix, GradientConstants.LUCKPERMS_PREFIX_PRIORITY).build();
                user.data().add(prefixNode);
            }

            luckPerms.getUserManager().saveUser(user);
        });
    }

    public void removePrefix(Player player) {
        setPrefix(player, null);
    }

    public void removePrefix(UUID uuid) {
        setPrefix(uuid, null);
    }

    public String getActivePrefix(Player player) {
        if (!isEnabled()) return null;

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return null;

        CachedMetaData metaData = user.getCachedData().getMetaData();
        return metaData.getPrefix();
    }
}
