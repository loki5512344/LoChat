package com.loki.lochat.gradient.commands.handlers.prefix;

import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.util.DisplayNameUtil;
import com.loki.lochat.utils.platform.FoliaUtil;
import org.bukkit.entity.Player;

/**
 * Обработчик команды /prefix off
 */
public class PrefixOffHandler implements PrefixSubCommandHandler {

    private final GradientModule module;

    public PrefixOffHandler(GradientModule module) {
        this.module = module;
    }

    @Override
    public boolean handle(Player player, String[] args) {
        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());
        data.setPrefixEnabled(false);
        saveAndUpdate(player, data);
        module.getMessages().send(player, "prefix-disabled");
        return true;
    }

    private void saveAndUpdate(Player player, GradientPlayerData data) {
        FoliaUtil.runEntityTask(module.getPlugin(), player,
                () -> DisplayNameUtil.updateDisplayName(module, player, data));
        FoliaUtil.runAsync(module.getPlugin(),
                () -> module.getDataManager().savePlayerData(player.getUniqueId()));
    }
}
