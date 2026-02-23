package com.loki.lohub.actions.impl;

import com.loki.lohub.actions.Action;
import com.loki.lohub.actions.ActionType;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundAction implements Action {

    private final Sound sound;
    private final float volume;
    private final float pitch;

    public SoundAction(String soundData) {
        String[] parts = soundData.split(";");

        Sound parsedSound;
        try {
            parsedSound = Sound.valueOf(parts[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            parsedSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        }

        this.sound = parsedSound;
        this.volume = parts.length > 1 ? parseFloat(parts[1], 1.0f) : 1.0f;
        this.pitch = parts.length > 2 ? parseFloat(parts[2], 1.0f) : 1.0f;
    }

    @Override
    public void execute(Player player) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    @Override
    public ActionType getType() {
        return ActionType.SOUND;
    }

    private float parseFloat(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
