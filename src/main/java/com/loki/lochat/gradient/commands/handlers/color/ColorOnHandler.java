package com.loki.lochat.gradient.commands.handlers.color;

import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.util.DisplayNameUtil;
import com.loki.lochat.utils.platform.FoliaUtil;

import org.bukkit.entity.Player;

/**
 * Обработчик команды /color on
 */
public class ColorOnHandler implements SubCommandHandler {

    private final GradientModule module;

    public ColorOnHandler(GradientModule module) {
        this.module = module;
    }

    @Override
    public boolean handle(Player player, String[] args) {
        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());

        if (!data.hasColors()) {
            module.getMessages().send(player, "color-no-colors");
            return true;
        }

        data.setColorEnabled(true);
        saveAndUpdate(player, data);
        module.getMessages().send(player, "color-enabled");
        return true;
    }

    private void saveAndUpdate(Player player, GradientPlayerData data) {
        FoliaUtil.runEntityTask(module.getPlugin(), player,
                () -> DisplayNameUtil.updateDisplayName(module, player, data));
        FoliaUtil.runAsync(module.getPlugin(),
                () -> module.getDataManager().savePlayerData(player.getUniqueId()));
    }
}
