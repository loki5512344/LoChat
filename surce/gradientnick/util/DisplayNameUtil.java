package ru.lovar.gradientnick.util;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import ru.lovar.gradientnick.GradientNick;
import ru.lovar.gradientnick.config.ConfigManager;
import ru.lovar.gradientnick.data.PlayerData;

/**
 * Утилита для обновления отображаемого имени игрока
 */
public final class DisplayNameUtil {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexCharacter('#')
            .hexColors()
            .build();

    private DisplayNameUtil() {}

    /**
     * Обновляет displayName и playerListName игрока
     */
    public static void updateDisplayName(GradientNick plugin, Player player, PlayerData data) {
        ConfigManager cfg = plugin.getConfigManager();
        
        // Определяем префикс: кастомный или групповой от LuckPerms
        String prefix = null;
        String prefixFormat = cfg.getPrefixFormat();
        
        if (data.isPrefixEnabled() && data.hasPrefix()) {
            // Кастомный префикс включён
            prefix = data.getPrefix();
        } else {
            // Кастомный выключен — берём групповой из LuckPerms
            String lpPrefix = plugin.getLuckPermsHook().getActivePrefix(player);
            if (lpPrefix != null && !lpPrefix.isEmpty()) {
                // Групповой префикс — применяем градиент на всё (префикс + ник)
                String displayName = buildWithLuckPermsPrefix(
                        lpPrefix, 
                        player.getName(), 
                        data.isColorEnabled() ? data.getColors() : null, 
                        cfg.isUseLegacyRgbFormat(),
                        cfg.isContinuousGradient()
                );
                var component = SERIALIZER.deserialize(displayName);
                player.displayName(component);
                player.playerListName(component);
                return;
            }
        }
        
        String displayName = GradientUtil.buildDisplayName(
                prefix,
                player.getName(),
                data.isColorEnabled() ? data.getColors() : null,
                cfg.isGradientOnPrefix(),
                cfg.isContinuousGradient(),
                prefixFormat,
                cfg.isUseLegacyRgbFormat()
        );
        var component = SERIALIZER.deserialize(displayName);
        player.displayName(component);
        player.playerListName(component);
    }

    /**
     * Строит displayName с префиксом от LuckPerms
     * Если есть цвета и continuousGradient — применяем единый градиент на всё
     */
    private static String buildWithLuckPermsPrefix(String lpPrefix, String nick, 
                                                    java.util.List<String> colors, 
                                                    boolean useLegacyFormat,
                                                    boolean continuousGradient) {
        if (colors == null || colors.isEmpty()) {
            // Нет цветов — просто префикс + ник
            return lpPrefix + nick;
        }
        
        if (continuousGradient) {
            // Единый градиент на префикс + ник
            // Убираем цветовые коды из LP префикса чтобы применить наш градиент
            String cleanPrefix = stripColors(lpPrefix);
            String fullText = cleanPrefix + nick;
            return GradientUtil.applyGradient(fullText, colors, useLegacyFormat);
        } else {
            // Градиент только на ник, префикс от LP оставляем
            return lpPrefix + GradientUtil.applyGradient(nick, colors, useLegacyFormat);
        }
    }

    /**
     * Убирает цветовые коды из строки
     */
    private static String stripColors(String text) {
        if (text == null) return null;
        // Убираем §x§R§R§G§G§B§B, §c, &c, &#RRGGBB форматы
        return text.replaceAll("(?i)(§x(§[0-9a-f]){6}|§[0-9a-fk-or]|&[0-9a-fk-or]|&#[0-9a-f]{6})", "");
    }

    /**
     * Строит цветной префикс для LuckPerms
     */
    public static String buildColoredPrefix(GradientNick plugin, PlayerData data) {
        if (!data.hasPrefix() || !data.isPrefixEnabled()) return null;
        
        ConfigManager cfg = plugin.getConfigManager();
        String prefix = cfg.getPrefixFormat().replace("{prefix}", data.getPrefix()).stripTrailing();
        
        return (data.hasColors() && data.isColorEnabled() && cfg.isGradientOnPrefix())
                ? GradientUtil.applyGradient(prefix, data.getColors(), cfg.isUseLegacyRgbFormat())
                : prefix;
    }
}
