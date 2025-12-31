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
 * Менеджер для управления TextDisplay над игроками
 */
public class TextDisplayManager {

    private final GradientModule module;
    private final Map<UUID, TextDisplay> playerDisplays = new ConcurrentHashMap<>();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public TextDisplayManager(GradientModule module) {
        this.module = module;
    }

    /**
     * Создает или обновляет TextDisplay над игроком
     */
    public void updatePlayerDisplay(Player player) {
        if (!module.getConfig().isUpdateDisplayName() || !module.getConfig().isUseTextDisplay()) {
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());
        
        // Получаем текст для отображения
        String displayText = getDisplayText(player, data);
        Component textComponent = MINI_MESSAGE.deserialize(displayText);

        TextDisplay display = playerDisplays.get(player.getUniqueId());
        
        if (display == null || !display.isValid()) {
            // Создаем новый TextDisplay
            createTextDisplay(player, textComponent);
        } else {
            // Обновляем существующий
            updateTextDisplay(display, player, textComponent);
        }
    }

    /**
     * Создает новый TextDisplay над игроком
     */
    private void createTextDisplay(Player player, Component textComponent) {
        // Удаляем старый display если есть
        removePlayerDisplay(player.getUniqueId());

        double height = module.getConfig().getTextDisplayHeight();
        float scale = module.getConfig().getTextDisplayScale();

        // Создаем новый TextDisplay
        TextDisplay display = player.getWorld().spawn(
            player.getLocation().add(0, height, 0), // Над головой игрока
            TextDisplay.class,
            entity -> {
                entity.text(textComponent);
                entity.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
                entity.setViewRange(64.0f); // Видимость на 64 блока
                entity.setSeeThrough(true); // Видно через стены
                entity.setShadowRadius(0.0f); // Без тени
                entity.setShadowStrength(0.0f); // Без тени
                
                // Настройка трансформации (размер текста)
                entity.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0), // Без смещения
                    new AxisAngle4f(0, 0, 0, 1), // Без поворота
                    new Vector3f(scale, scale, scale), // Размер текста
                    new AxisAngle4f(0, 0, 0, 1) // Без поворота после масштабирования
                ));
            }
        );

        // Привязываем TextDisplay к игроку как пассажира
        player.addPassenger(display);
        
        // Сохраняем в карте
        playerDisplays.put(player.getUniqueId(), display);
    }

    /**
     * Обновляет существующий TextDisplay
     */
    private void updateTextDisplay(TextDisplay display, Player player, Component textComponent) {
        display.text(textComponent);
        
        // Проверяем, что display все еще привязан к игроку
        if (!player.getPassengers().contains(display)) {
            player.addPassenger(display);
        }
    }

    /**
     * Получает текст для отображения над игроком
     */
    private String getDisplayText(Player player, GradientPlayerData data) {
        String prefix = null;
        String prefixFormat = module.getConfig().getPrefixFormat();
        
        // Определяем префикс: сначала кастомный, потом LuckPerms
        if (data.isPrefixEnabled() && data.hasPrefix()) {
            prefix = data.getPrefix();
        } else if (module.getLuckPermsHook().isEnabled()) {
            String lpPrefix = module.getLuckPermsHook().getActivePrefix(player);
            if (lpPrefix != null && !lpPrefix.isEmpty()) {
                // Для LuckPerms префикса используем специальную логику
                return buildWithLuckPermsPrefix(
                    lpPrefix, 
                    player.getName(), 
                    data.isColorEnabled() ? data.getColors() : null, 
                    module.getConfig().isUseLegacyRgbFormat(),
                    module.getConfig().isContinuousGradient()
                );
            }
        }
        
        // Строим display text с кастомным префиксом или без префикса
        String displayText = GradientUtil.buildDisplayName(
            prefix,
            player.getName(),
            data.isColorEnabled() ? data.getColors() : null,
            module.getConfig().isGradientOnPrefix(),
            module.getConfig().isContinuousGradient(),
            prefixFormat,
            false // Используем MiniMessage формат для TextDisplay
        );
        
        // Конвертируем &#RRGGBB в <#RRGGBB> для MiniMessage
        return convertToMiniMessageFormat(displayText);
    }

    /**
     * Строит текст с LuckPerms префиксом
     */
    private String buildWithLuckPermsPrefix(String lpPrefix, String nick, 
                                          java.util.List<String> colors, 
                                          boolean useLegacyFormat,
                                          boolean continuousGradient) {
        if (colors == null || colors.isEmpty()) {
            return lpPrefix + nick;
        }
        
        if (continuousGradient) {
            String cleanPrefix = stripColors(lpPrefix);
            String fullText = cleanPrefix + nick;
            return GradientUtil.applyGradient(fullText, colors, false); // Всегда MiniMessage формат для TextDisplay
        } else {
            return lpPrefix + GradientUtil.applyGradient(nick, colors, false); // Всегда MiniMessage формат для TextDisplay
        }
    }

    /**
     * Убирает цветовые коды из строки
     */
    private String stripColors(String text) {
        if (text == null) return "";
        return text.replaceAll("(?i)(§x(§[0-9a-f]){6}|§[0-9a-fk-or]|&[0-9a-fk-or]|&#[0-9a-f]{6}|<[^>]+>)", "");
    }

    /**
     * Возвращает текст как есть, так как градиенты уже в MiniMessage формате
     */
    private String convertToMiniMessageFormat(String text) {
        if (text == null) return "";
        return text;
    }

    /**
     * Удаляет TextDisplay игрока
     */
    public void removePlayerDisplay(UUID playerId) {
        TextDisplay display = playerDisplays.remove(playerId);
        if (display != null && display.isValid()) {
            display.remove();
        }
    }

    /**
     * Удаляет все TextDisplay
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
     * Обновляет позицию TextDisplay (вызывается при движении игрока)
     */
    public void updateDisplayPosition(Player player) {
        TextDisplay display = playerDisplays.get(player.getUniqueId());
        if (display != null && display.isValid()) {
            // TextDisplay автоматически следует за игроком как пассажир
            // Но можем обновить высоту если нужно
            if (!player.getPassengers().contains(display)) {
                player.addPassenger(display);
            }
        }
    }

    /**
     * Проверяет, есть ли у игрока TextDisplay
     */
    public boolean hasDisplay(UUID playerId) {
        TextDisplay display = playerDisplays.get(playerId);
        return display != null && display.isValid();
    }
}