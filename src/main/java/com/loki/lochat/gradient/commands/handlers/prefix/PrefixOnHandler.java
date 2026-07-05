package com.loki.lochat.gradient.commands.handlers.prefix;

import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.util.DisplayNameUtil;
import com.loki.lochat.utils.platform.FoliaUtil;

import org.bukkit.entity.Player;

/**
 * Обработчик команды /prefix on
 */
public class PrefixOnHandler implements PrefixSubCommandHandler {

    private final GradientModule module;

    public PrefixOnHandler(GradientModule module) {
        this.module = module;
    }

    @Override
    public boolean handle(Player player, String[] args) {
        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());

        if (!data.hasPrefix()) {
            module.getMessages().send(player, "prefix-no-prefix");
            return true;
        }

        data.setPrefixEnabled(true);
        saveAndUpdate(player, data);
        module.getMessages().send(player, "prefix-enabled");
        return true;
    }

    private void saveAndUpdate(Player player, GradientPlayerData data) {
        FoliaUtil.runEntityTask(module.getPlugin(), player,
                () -> DisplayNameUtil.updateDisplayName(module, player, data));
        FoliaUtil.runAsync(module.getPlugin(),
                () -> module.getDataManager().savePlayerData(player.getUniqueId()));
    }
}
