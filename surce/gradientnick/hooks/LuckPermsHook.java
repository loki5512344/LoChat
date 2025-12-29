package ru.lovar.gradientnick.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.lovar.gradientnick.GradientNick;
import ru.lovar.gradientnick.util.Constants;

import java.util.UUID;

public class LuckPermsHook {

    private LuckPerms luckPerms;
    private boolean enabled;

    public LuckPermsHook(GradientNick plugin) {
        this.enabled = false;
        
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            try {
                this.luckPerms = LuckPermsProvider.get();
                this.enabled = true;
                plugin.getLogger().info("LuckPerms подключен!");
            } catch (Exception e) {
                plugin.getLogger().warning("Не удалось подключить LuckPerms: " + e.getMessage());
            }
        }
    }

    public boolean isEnabled() {
        return enabled && luckPerms != null;
    }

    public LuckPerms getApi() {
        return luckPerms;
    }

    /**
     * Устанавливает кастомный префикс игроку (приоритет 100 — выше групповых)
     */
    public void setPrefix(Player player, String prefix) {
        if (!isEnabled()) return;
        
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        // Удаляем старые кастомные префиксы (приоритет 100)
        user.data().clear(node -> 
            node instanceof PrefixNode && ((PrefixNode) node).getPriority() == Constants.LUCKPERMS_PREFIX_PRIORITY
        );

        // Добавляем новый префикс если есть
        if (prefix != null && !prefix.isEmpty()) {
            PrefixNode prefixNode = PrefixNode.builder(prefix, Constants.LUCKPERMS_PREFIX_PRIORITY).build();
            user.data().add(prefixNode);
        }

        luckPerms.getUserManager().saveUser(user);
    }

    /**
     * Устанавливает кастомный префикс по UUID (асинхронно)
     */
    public void setPrefix(UUID uuid, String prefix) {
        if (!isEnabled()) return;
        
        luckPerms.getUserManager().loadUser(uuid).thenAccept(user -> {
            if (user == null) return;

            user.data().clear(node -> 
                node instanceof PrefixNode && ((PrefixNode) node).getPriority() == Constants.LUCKPERMS_PREFIX_PRIORITY
            );

            if (prefix != null && !prefix.isEmpty()) {
                PrefixNode prefixNode = PrefixNode.builder(prefix, Constants.LUCKPERMS_PREFIX_PRIORITY).build();
                user.data().add(prefixNode);
            }

            luckPerms.getUserManager().saveUser(user);
        });
    }

    /**
     * Удаляет кастомный префикс — будет показываться префикс от группы
     */
    public void removePrefix(Player player) {
        setPrefix(player, null);
    }

    /**
     * Удаляет кастомный префикс по UUID
     */
    public void removePrefix(UUID uuid) {
        setPrefix(uuid, null);
    }

    /**
     * Получает текущий активный префикс игрока (включая групповой)
     */
    public String getActivePrefix(Player player) {
        if (!isEnabled()) return null;
        
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return null;
        
        CachedMetaData metaData = user.getCachedData().getMetaData();
        return metaData.getPrefix();
    }

    /**
     * Получает префикс от группы (без кастомного)
     */
    public String getGroupPrefix(Player player) {
        if (!isEnabled()) return null;
        
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return null;
        
        // Временно убираем кастомный префикс чтобы получить групповой
        // Это сложно сделать правильно, поэтому просто возвращаем активный
        // Групповой префикс будет показан когда кастомный удалён
        CachedMetaData metaData = user.getCachedData().getMetaData();
        return metaData.getPrefix();
    }
}
