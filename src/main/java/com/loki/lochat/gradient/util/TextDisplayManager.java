package com.loki.lochat.gradient.util;

import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.data.GradientPlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TextDisplayManager {

    private final GradientModule module;
    private final Map<UUID, TextDisplay> playerDisplays = new ConcurrentHashMap<>();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public TextDisplayManager(GradientModule module) {
        this.module = module;
    }
    public void updatePlayerDisplay(Player player) {
        if (!module.getConfig().isUpdateDisplayName() || !module.getConfig().isUseTextDisplay()) {
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());
        String displayText = getDisplayText(player, data);
        Component textComponent = MINI_MESSAGE.deserialize(displayText);

        TextDisplay display = playerDisplays.get(player.getUniqueId());
        
        if (display == null || !display.isValid()) {
            createTextDisplay(player, textComponent);
        } else {
            updateTextDisplay(display, player, textComponent);
        }
    }
    private void createTextDisplay(Player player, Component textComponent) {
        removePlayerDisplay(player.getUniqueId());

        double height = module.getConfig().getTextDisplayHeight();
        float scale = module.getConfig().getTextDisplayScale();
        boolean seeThrough = module.getConfig().isTextDisplaySeeThrough();

        TextDisplay display = player.getWorld().spawn(
            player.getLocation().add(0, height, 0),
            TextDisplay.class,
            entity -> {
                entity.text(textComponent);
                entity.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
                entity.setViewRange(64.0f);
                entity.setSeeThrough(seeThrough);
                entity.setShadowRadius(0.0f);
                entity.setShadowStrength(0.0f);
                entity.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 0, 1),
                    new Vector3f(scale, scale, scale),
                    new AxisAngle4f(0, 0, 0, 1)
                ));
                entity.setVisibleByDefault(true);
            }
        );

        player.addPassenger(display);
        playerDisplays.put(player.getUniqueId(), display);
    }
    private void updateTextDisplay(TextDisplay display, Player player, Component textComponent) {
        display.text(textComponent);
        if (!player.getPassengers().contains(display)) {
            player.addPassenger(display);
        }
    }
    private String getDisplayText(Player player, GradientPlayerData data) {
        String format = module.getConfig().getTextDisplayFormat();
        String prefix = getPlayerPrefix(player, data);
        String playerName = getPlayerName(player, data);
        String result = format
                .replace("{prefix}", prefix != null ? prefix : "")
                .replace("{player}", playerName)
                .replace("{name}", playerName);
        return convertLegacyFormats(result);
    }
    private String convertLegacyFormats(String message) {
        if (message == null) return "";
        message = message.replaceAll("&#([0-9a-fA-F]{6})", "<#$1>");
        message = message.replaceAll("(?<!<)#([0-9a-fA-F]{6})(?![^<]*>)", "<#$1>");
        return message;
    }
    private String getPlayerPrefix(Player player, GradientPlayerData data) {
        String prefixFormat = module.getConfig().getPrefixFormat();
        if (data.isPrefixEnabled() && data.hasPrefix()) {
            String prefix = prefixFormat.replace("{prefix}", data.getPrefix());
            if (data.isColorEnabled() && data.hasColors() && module.getConfig().isGradientOnPrefix()) {
                return GradientUtil.applyGradient(prefix, data.getColors(), false);
            }
            return prefix;
        } else if (module.getLuckPermsHook().isEnabled()) {
            String lpPrefix = module.getLuckPermsHook().getActivePrefix(player);
            if (lpPrefix != null && !lpPrefix.isEmpty()) {
                if (data.isColorEnabled() && data.hasColors() && module.getConfig().isGradientOnLuckPermsPrefix()) {
                    String cleanPrefix = stripColors(lpPrefix);
                    return GradientUtil.applyGradient(cleanPrefix, data.getColors(), false);
                }
                return lpPrefix;
            }
        }
        return "";
    }
    private String getPlayerName(Player player, GradientPlayerData data) {
        if (data.isColorEnabled() && data.hasColors()) {
            return GradientUtil.applyGradient(player.getName(), data.getColors(), false);
        }
        return player.getName();
    }
    private String stripColors(String text) {
        if (text == null) return "";
        return text.replaceAll("(?i)(┬зx(┬з[0-9a-f]){6}|┬з[0-9a-fk-or]|&[0-9a-fk-or]|&#[0-9a-f]{6}|<[^>]+>)", "");
    }
    public void removePlayerDisplay(UUID playerId) {
        TextDisplay display = playerDisplays.remove(playerId);
        if (display != null && display.isValid()) {
            display.remove();
        }
    }
    public void removeAllDisplays() {
        for (TextDisplay display : playerDisplays.values()) {
            if (display != null && display.isValid()) {
                display.remove();
            }
        }
        playerDisplays.clear();
    }
    public void updateDisplayPosition(Player player) {
        TextDisplay display = playerDisplays.get(player.getUniqueId());
        if (display != null && display.isValid()) {
            if (!player.getPassengers().contains(display)) {
                player.addPassenger(display);
            }
        }
    }
    public boolean hasDisplay(UUID playerId) {
        TextDisplay display = playerDisplays.get(playerId);
        return display != null && display.isValid();
    }

    public void hidePlayerDisplay(UUID playerId) {
        TextDisplay display = playerDisplays.get(playerId);
        if (display != null && display.isValid()) {
            display.setViewRange(0.0f);
        }
    }

    public void showPlayerDisplay(UUID playerId) {
        TextDisplay display = playerDisplays.get(playerId);
        if (display != null && display.isValid()) {
            display.setViewRange(64.0f);
        }
    }
}
