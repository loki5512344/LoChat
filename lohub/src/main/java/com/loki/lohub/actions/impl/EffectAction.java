package com.loki.lohub.actions.impl;

import com.loki.lohub.actions.Action;
import com.loki.lohub.actions.ActionType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EffectAction implements Action {

    private final PotionEffectType effectType;
    private final int amplifier;
    private final int duration;

    public EffectAction(String effectData) {
        String[] parts = effectData.split(";");

        PotionEffectType parsed;
        try {
            parsed = PotionEffectType.getByName(parts[0].toUpperCase());
        } catch (Exception e) {
            parsed = PotionEffectType.SPEED;
        }

        this.effectType = parsed != null ? parsed : PotionEffectType.SPEED;
        this.amplifier = parts.length > 1 ? parseInt(parts[1], 0) : 0;
        this.duration = parts.length > 2 ? parseInt(parts[2], 999999) : 999999;
    }

    @Override
    public void execute(Player player) {
        player.addPotionEffect(new PotionEffect(effectType, duration * 20, amplifier, false, false));
    }

    @Override
    public ActionType getType() {
        return ActionType.EFFECT;
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
