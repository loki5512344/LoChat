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

/**
 * ╨Ь╨╡╨╜╨╡╨┤╨╢╨╡╤А ╨┤╨╗╤П ╤Г╨┐╤А╨░╨▓╨╗╨╡╨╜╨╕╤П TextDisplay ╨╜╨░╨┤ ╨╕╨│╤А╨╛╨║╨░╨╝╨╕
 */
public class TextDisplayManager {

    private final GradientModule module;
    private final Map<UUID, TextDisplay> playerDisplays = new ConcurrentHashMap<>();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public TextDisplayManager(GradientModule module) {
        this.module = module;
    }

    /**
     * ╨б╨╛╨╖╨┤╨░╨╡╤В ╨╕╨╗╨╕ ╨╛╨▒╨╜╨╛╨▓╨╗╤П╨╡╤В TextDisplay ╨╜╨░╨┤ ╨╕╨│╤А╨╛╨║╨╛╨╝
     */
    public void updatePlayerDisplay(Player player) {
        if (!module.getConfig().isUpdateDisplayName() || !module.getConfig().isUseTextDisplay()) {
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());
        
        // ╨Я╨╛╨╗╤Г╤З╨░╨╡╨╝ ╤В╨╡╨║╤Б╤В ╨┤╨╗╤П ╨╛╤В╨╛╨▒╤А╨░╨╢╨╡╨╜╨╕╤П
        String displayText = getDisplayText(player, data);
        Component textComponent = MINI_MESSAGE.deserialize(displayText);

        TextDisplay display = playerDisplays.get(player.getUniqueId());
        
        if (display == null || !display.isValid()) {
            // ╨б╨╛╨╖╨┤╨░╨╡╨╝ ╨╜╨╛╨▓╤Л╨╣ TextDisplay
            createTextDisplay(player, textComponent);
        } else {
            // ╨Ю╨▒╨╜╨╛╨▓╨╗╤П╨╡╨╝ ╤Б╤Г╤Й╨╡╤Б╤В╨▓╤Г╤О╤Й╨╕╨╣
            updateTextDisplay(display, player, textComponent);
        }
    }

    /**
     * ╨б╨╛╨╖╨┤╨░╨╡╤В ╨╜╨╛╨▓╤Л╨╣ TextDisplay ╨╜╨░╨┤ ╨╕╨│╤А╨╛╨║╨╛╨╝
     */
    private void createTextDisplay(Player player, Component textComponent) {
        // ╨г╨┤╨░╨╗╤П╨╡╨╝ ╤Б╤В╨░╤А╤Л╨╣ display ╨╡╤Б╨╗╨╕ ╨╡╤Б╤В╤М
        removePlayerDisplay(player.getUniqueId());

        double height = module.getConfig().getTextDisplayHeight();
        float scale = module.getConfig().getTextDisplayScale();
        boolean seeThrough = module.getConfig().isTextDisplaySeeThrough();

        // ╨б╨╛╨╖╨┤╨░╨╡╨╝ ╨╜╨╛╨▓╤Л╨╣ TextDisplay
        TextDisplay display = player.getWorld().spawn(
            player.getLocation().add(0, height, 0), // ╨Э╨░╨┤ ╨│╨╛╨╗╨╛╨▓╨╛╨╣ ╨╕╨│╤А╨╛╨║╨░
            TextDisplay.class,
            entity -> {
                entity.text(textComponent);
                entity.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
                entity.setViewRange(64.0f); // ╨Т╨╕╨┤╨╕╨╝╨╛╤Б╤В╤М ╨╜╨░ 64 ╨▒╨╗╨╛╨║╨░
                entity.setSeeThrough(seeThrough); // ╨Э╨░╤Б╤В╤А╨░╨╕╨▓╨░╨╡╨╝╨░╤П ╨▓╨╕╨┤╨╕╨╝╨╛╤Б╤В╤М ╤З╨╡╤А╨╡╨╖ ╤Б╤В╨╡╨╜╤Л
                entity.setShadowRadius(0.0f); // ╨С╨╡╨╖ ╤В╨╡╨╜╨╕
                entity.setShadowStrength(0.0f); // ╨С╨╡╨╖ ╤В╨╡╨╜╨╕
                
                // ╨Э╨░╤Б╤В╤А╨╛╨╣╨║╨░ ╤В╤А╨░╨╜╤Б╤Д╨╛╤А╨╝╨░╤Ж╨╕╨╕ (╤А╨░╨╖╨╝╨╡╤А ╤В╨╡╨║╤Б╤В╨░)
                entity.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0), // ╨С╨╡╨╖ ╤Б╨╝╨╡╤Й╨╡╨╜╨╕╤П
                    new AxisAngle4f(0, 0, 0, 1), // ╨С╨╡╨╖ ╨┐╨╛╨▓╨╛╤А╨╛╤В╨░
                    new Vector3f(scale, scale, scale), // ╨а╨░╨╖╨╝╨╡╤А ╤В╨╡╨║╤Б╤В╨░
                    new AxisAngle4f(0, 0, 0, 1) // ╨С╨╡╨╖ ╨┐╨╛╨▓╨╛╤А╨╛╤В╨░ ╨┐╨╛╤Б╨╗╨╡ ╨╝╨░╤Б╤И╤В╨░╨▒╨╕╤А╨╛╨▓╨░╨╜╨╕╤П
                ));
                
                // ╨Ю╤В╨║╨╗╤О╤З╨░╨╡╨╝ ╤Б╨║╤А╤Л╤В╨╕╨╡ ╨┐╤А╨╕ ╨┐╤А╨╕╤Б╨╡╨┤╨░╨╜╨╕╨╕
                entity.setVisibleByDefault(true);
            }
        );

        // ╨Я╤А╨╕╨▓╤П╨╖╤Л╨▓╨░╨╡╨╝ TextDisplay ╨║ ╨╕╨│╤А╨╛╨║╤Г ╨║╨░╨║ ╨┐╨░╤Б╤Б╨░╨╢╨╕╤А╨░
        player.addPassenger(display);
        
        // ╨б╨╛╤Е╤А╨░╨╜╤П╨╡╨╝ ╨▓ ╨║╨░╤А╤В╨╡
        playerDisplays.put(player.getUniqueId(), display);
    }

    /**
     * ╨Ю╨▒╨╜╨╛╨▓╨╗╤П╨╡╤В ╤Б╤Г╤Й╨╡╤Б╤В╨▓╤Г╤О╤Й╨╕╨╣ TextDisplay
     */
    private void updateTextDisplay(TextDisplay display, Player player, Component textComponent) {
        display.text(textComponent);
        
        // ╨Я╤А╨╛╨▓╨╡╤А╤П╨╡╨╝, ╤З╤В╨╛ display ╨▓╤Б╨╡ ╨╡╤Й╨╡ ╨┐╤А╨╕╨▓╤П╨╖╨░╨╜ ╨║ ╨╕╨│╤А╨╛╨║╤Г
        if (!player.getPassengers().contains(display)) {
            player.addPassenger(display);
        }
    }

    /**
     * ╨Я╨╛╨╗╤Г╤З╨░╨╡╤В ╤В╨╡╨║╤Б╤В ╨┤╨╗╤П ╨╛╤В╨╛╨▒╤А╨░╨╢╨╡╨╜╨╕╤П ╨╜╨░╨┤ ╨╕╨│╤А╨╛╨║╨╛╨╝
     */
    private String getDisplayText(Player player, GradientPlayerData data) {
        String format = module.getConfig().getTextDisplayFormat();
        
        // ╨Я╨╛╨╗╤Г╤З╨░╨╡╨╝ ╨║╨╛╨╝╨┐╨╛╨╜╨╡╨╜╤В╤Л
        String prefix = getPlayerPrefix(player, data);
        String playerName = getPlayerName(player, data);
        
        // ╨Ч╨░╨╝╨╡╨╜╤П╨╡╨╝ ╨┐╨╗╨╡╨╣╤Б╤Е╨╛╨╗╨┤╨╡╤А╤Л
        String result = format
                .replace("{prefix}", prefix != null ? prefix : "")
                .replace("{player}", playerName)
                .replace("{name}", playerName); // ╨Р╨╗╤М╤В╨╡╤А╨╜╨░╤В╨╕╨▓╨╜╤Л╨╣ ╨┐╨╗╨╡╨╣╤Б╤Е╨╛╨╗╨┤╨╡╤А
        
        // ╨Ъ╨╛╨╜╨▓╨╡╤А╤В╨╕╤А╤Г╨╡╨╝ legacy ╤Д╨╛╤А╨╝╨░╤В╤Л ╨▓ MiniMessage
        return convertLegacyFormats(result);
    }

    /**
     * ╨Ъ╨╛╨╜╨▓╨╡╤А╤В╨╕╤А╤Г╨╡╤В legacy ╤Д╨╛╤А╨╝╨░╤В╤Л ╤Ж╨▓╨╡╤В╨╛╨▓ ╨▓ MiniMessage ╤Д╨╛╤А╨╝╨░╤В
     */
    private String convertLegacyFormats(String message) {
        if (message == null) return "";
        
        // ╨Ъ╨╛╨╜╨▓╨╡╤А╤В╨╕╤А╤Г╨╡╨╝ &#RRGGBB ╨▓ <#RRGGBB>
        message = message.replaceAll("&#([0-9a-fA-F]{6})", "<#$1>");
        
        // ╨Ъ╨╛╨╜╨▓╨╡╤А╤В╨╕╤А╤Г╨╡╨╝ #RRGGBB ╨▓ <#RRGGBB> (╤В╨╛╨╗╤М╨║╨╛ ╨╡╤Б╨╗╨╕ ╨╜╨╡ ╨▓╨╜╤Г╤В╤А╨╕ ╤В╨╡╨│╨╛╨▓)
        message = message.replaceAll("(?<!<)#([0-9a-fA-F]{6})(?![^<]*>)", "<#$1>");
        
        return message;
    }

    /**
     * ╨Я╨╛╨╗╤Г╤З╨░╨╡╤В ╨┐╤А╨╡╤Д╨╕╨║╤Б ╨╕╨│╤А╨╛╨║╨░
     */
    private String getPlayerPrefix(Player player, GradientPlayerData data) {
        String prefixFormat = module.getConfig().getPrefixFormat();
        
        // ╨Ю╨┐╤А╨╡╨┤╨╡╨╗╤П╨╡╨╝ ╨┐╤А╨╡╤Д╨╕╨║╤Б: ╤Б╨╜╨░╤З╨░╨╗╨░ ╨║╨░╤Б╤В╨╛╨╝╨╜╤Л╨╣, ╨┐╨╛╤В╨╛╨╝ LuckPerms
        if (data.isPrefixEnabled() && data.hasPrefix()) {
            String prefix = prefixFormat.replace("{prefix}", data.getPrefix());
            // ╨Я╤А╨╕╨╝╨╡╨╜╤П╨╡╨╝ ╨│╤А╨░╨┤╨╕╨╡╨╜╤В ╤В╨╛╨╗╤М╨║╨╛ ╨╡╤Б╨╗╨╕ ╤Ж╨▓╨╡╤В╨░ ╨▓╨║╨╗╤О╤З╨╡╨╜╤Л
            if (data.isColorEnabled() && data.hasColors() && module.getConfig().isGradientOnPrefix()) {
                return GradientUtil.applyGradient(prefix, data.getColors(), false); // MiniMessage ╤Д╨╛╤А╨╝╨░╤В
            }
            return prefix;
        } else if (module.getLuckPermsHook().isEnabled()) {
            String lpPrefix = module.getLuckPermsHook().getActivePrefix(player);
            if (lpPrefix != null && !lpPrefix.isEmpty()) {
                // ╨Ф╨╗╤П LuckPerms ╨┐╤А╨╡╤Д╨╕╨║╤Б╨░ ╨┐╤А╨╕╨╝╨╡╨╜╤П╨╡╨╝ ╨│╤А╨░╨┤╨╕╨╡╨╜╤В ╤В╨╛╨╗╤М╨║╨╛ ╨╡╤Б╨╗╨╕ ╨▓╨║╨╗╤О╤З╨╡╨╜╨╛
                if (data.isColorEnabled() && data.hasColors() && module.getConfig().isGradientOnLuckPermsPrefix()) {
                    String cleanPrefix = stripColors(lpPrefix);
                    return GradientUtil.applyGradient(cleanPrefix, data.getColors(), false);
                }
                return lpPrefix;
            }
        }
        
        return "";
    }

    /**
     * ╨Я╨╛╨╗╤Г╤З╨░╨╡╤В ╨╕╨╝╤П ╨╕╨│╤А╨╛╨║╨░ ╤Б ╨│╤А╨░╨┤╨╕╨╡╨╜╤В╨╛╨╝
     */
    private String getPlayerName(Player player, GradientPlayerData data) {
        // ╨Я╤А╨╕╨╝╨╡╨╜╤П╨╡╨╝ ╨│╤А╨░╨┤╨╕╨╡╨╜╤В ╤В╨╛╨╗╤М╨║╨╛ ╨╡╤Б╨╗╨╕ ╤Ж╨▓╨╡╤В╨░ ╨▓╨║╨╗╤О╤З╨╡╨╜╤Л
        if (data.isColorEnabled() && data.hasColors()) {
            return GradientUtil.applyGradient(player.getName(), data.getColors(), false); // MiniMessage ╤Д╨╛╤А╨╝╨░╤В
        }
        return player.getName();
    }

    /**
     * ╨г╨▒╨╕╤А╨░╨╡╤В ╤Ж╨▓╨╡╤В╨╛╨▓╤Л╨╡ ╨║╨╛╨┤╤Л ╨╕╨╖ ╤Б╤В╤А╨╛╨║╨╕
     */
    private String stripColors(String text) {
        if (text == null) return "";
        return text.replaceAll("(?i)(┬зx(┬з[0-9a-f]){6}|┬з[0-9a-fk-or]|&[0-9a-fk-or]|&#[0-9a-f]{6}|<[^>]+>)", "");
    }

    /**
     * ╨г╨┤╨░╨╗╤П╨╡╤В TextDisplay ╨╕╨│╤А╨╛╨║╨░
     */
    public void removePlayerDisplay(UUID playerId) {
        TextDisplay display = playerDisplays.remove(playerId);
        if (display != null && display.isValid()) {
            display.remove();
        }
    }

    /**
     * ╨г╨┤╨░╨╗╤П╨╡╤В ╨▓╤Б╨╡ TextDisplay
     */
    public void removeAllDisplays() {
        for (TextDisplay display : playerDisplays.values()) {
            if (display != null && display.isValid()) {
                display.remove();
            }
        }
        playerDisplays.clear();
    }

    /**
     * ╨Ю╨▒╨╜╨╛╨▓╨╗╤П╨╡╤В ╨┐╨╛╨╖╨╕╤Ж╨╕╤О TextDisplay (╨▓╤Л╨╖╤Л╨▓╨░╨╡╤В╤Б╤П ╨┐╤А╨╕ ╨┤╨▓╨╕╨╢╨╡╨╜╨╕╨╕ ╨╕╨│╤А╨╛╨║╨░)
     */
    public void updateDisplayPosition(Player player) {
        TextDisplay display = playerDisplays.get(player.getUniqueId());
        if (display != null && display.isValid()) {
            // TextDisplay ╨░╨▓╤В╨╛╨╝╨░╤В╨╕╤З╨╡╤Б╨║╨╕ ╤Б╨╗╨╡╨┤╤Г╨╡╤В ╨╖╨░ ╨╕╨│╤А╨╛╨║╨╛╨╝ ╨║╨░╨║ ╨┐╨░╤Б╤Б╨░╨╢╨╕╤А
            // ╨Э╨╛ ╨╝╨╛╨╢╨╡╨╝ ╨╛╨▒╨╜╨╛╨▓╨╕╤В╤М ╨▓╤Л╤Б╨╛╤В╤Г ╨╡╤Б╨╗╨╕ ╨╜╤Г╨╢╨╜╨╛
            if (!player.getPassengers().contains(display)) {
                player.addPassenger(display);
            }
        }
    }

    /**
     * ╨Я╤А╨╛╨▓╨╡╤А╤П╨╡╤В, ╨╡╤Б╤В╤М ╨╗╨╕ ╤Г ╨╕╨│╤А╨╛╨║╨░ TextDisplay
     */
    public boolean hasDisplay(UUID playerId) {
        TextDisplay display = playerDisplays.get(playerId);
        return display != null && display.isValid();
    }

    /**
     * Скрывает TextDisplay игрока (при приседании)
     */
    public void hidePlayerDisplay(UUID playerId) {
        TextDisplay display = playerDisplays.get(playerId);
        if (display != null && display.isValid()) {
            display.setViewRange(0.0f);
        }
    }

    /**
     * Показывает TextDisplay игрока (когда перестает приседать)
     */
    public void showPlayerDisplay(UUID playerId) {
        TextDisplay display = playerDisplays.get(playerId);
        if (display != null && display.isValid()) {
            display.setViewRange(64.0f);
        }
    }
}
