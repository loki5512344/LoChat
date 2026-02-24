package com.loki.lohub.features;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FireworkHandler {

    private static final Map<String, Color> COLOR_MAP = new HashMap<>();
    private static final String CONFIG_PATH = "join_settings.firework";

    static {
        COLOR_MAP.put("AQUA", Color.AQUA);
        COLOR_MAP.put("BLACK", Color.BLACK);
        COLOR_MAP.put("BLUE", Color.BLUE);
        COLOR_MAP.put("FUCHSIA", Color.FUCHSIA);
        COLOR_MAP.put("GRAY", Color.GRAY);
        COLOR_MAP.put("GREEN", Color.GREEN);
        COLOR_MAP.put("LIME", Color.LIME);
        COLOR_MAP.put("MAROON", Color.MAROON);
        COLOR_MAP.put("NAVY", Color.NAVY);
        COLOR_MAP.put("OLIVE", Color.OLIVE);
        COLOR_MAP.put("ORANGE", Color.ORANGE);
        COLOR_MAP.put("PURPLE", Color.PURPLE);
        COLOR_MAP.put("RED", Color.RED);
        COLOR_MAP.put("SILVER", Color.SILVER);
        COLOR_MAP.put("TEAL", Color.TEAL);
        COLOR_MAP.put("WHITE", Color.WHITE);
        COLOR_MAP.put("YELLOW", Color.YELLOW);
    }

    private FireworkHandler() {
    }

    public static void spawn(Player player, FileConfiguration config) {
        if (!config.getBoolean(CONFIG_PATH + ".enabled", false)) {
            return;
        }

        if (shouldSkip(player, config)) {
            return;
        }

        Location loc = player.getLocation();
        Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = firework.getFireworkMeta();

        meta.addEffect(buildEffect(config));
        meta.setPower(config.getInt(CONFIG_PATH + ".power", 1));
        firework.setFireworkMeta(meta);
    }

    private static boolean shouldSkip(Player player, FileConfiguration config) {
        boolean firstJoinOnly = config.getBoolean(CONFIG_PATH + ".first_join_only", true);
        return firstJoinOnly && player.hasPlayedBefore();
    }

    private static FireworkEffect buildEffect(FileConfiguration config) {
        FireworkEffect.Type type = getType(config);
        List<Color> colors = getColors(config);

        return FireworkEffect.builder()
                .with(type)
                .withColor(colors)
                .flicker(config.getBoolean(CONFIG_PATH + ".flicker", true))
                .trail(config.getBoolean(CONFIG_PATH + ".trail", true))
                .build();
    }

    private static FireworkEffect.Type getType(FileConfiguration config) {
        String typeStr = config.getString(CONFIG_PATH + ".type", "BALL_LARGE");
        try {
            return FireworkEffect.Type.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return FireworkEffect.Type.BALL_LARGE;
        }
    }

    private static List<Color> getColors(FileConfiguration config) {
        List<Color> colors = new ArrayList<>();
        for (String colorStr : config.getStringList(CONFIG_PATH + ".colors")) {
            Color color = COLOR_MAP.get(colorStr.toUpperCase());
            if (color != null) {
                colors.add(color);
            }
        }

        if (colors.isEmpty()) {
            colors.add(Color.AQUA);
        }

        return colors;
    }
}
