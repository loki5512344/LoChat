package com.loki.lochat.managers;

import com.loki.lochat.LoChat;
import com.loki.lochat.integrations.SkinsRestorerHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Менеджер для обработки голов игроков в чате
 * Поддерживает рисование головы из смайликов и отправку голов игроков
 */
public class HeadEmojiManager {

    private final LoChat plugin;
    private final SkinsRestorerHook skinsHook;
    
    // Паттерны для поиска голов игроков
    private static final Pattern HEAD_PATTERN = Pattern.compile(":head_([a-zA-Z0-9_]{3,16}):");
    private static final Pattern PLAYER_HEAD_PATTERN = Pattern.compile("@head\\s+([a-zA-Z0-9_]{3,16})");
    
    // ASCII арт для головы игрока (8x8 пикселей)
    private static final String[] HEAD_TEMPLATE = {
        "████████",
        "██░░░░██", 
        "██░██░██",
        "██░░░░██",
        "██░██░██",
        "██░░░░██",
        "██████████",
        "████████"
    };

    public HeadEmojiManager(LoChat plugin) {
        this.plugin = plugin;
        this.skinsHook = new SkinsRestorerHook(plugin);
    }

    /**
     * Обрабатывает сообщение и заменяет головы игроков
     */
    public Component processHeads(Component message, Player sender) {
        String plainText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(message);
        
        // Обрабатываем :head_nickname:
        plainText = processHeadEmojis(plainText);
        
        // Обрабатываем @head nickname
        plainText = processPlayerHeadCommands(plainText, sender);
        
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(plainText);
    }

    /**
     * Обрабатывает :head_nickname: паттерны
     */
    private String processHeadEmojis(String message) {
        Matcher matcher = HEAD_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String playerName = matcher.group(1);
            Player target = Bukkit.getPlayer(playerName);
            
            if (target != null && target.isOnline()) {
                String headEmoji = generateHeadEmoji(target);
                matcher.appendReplacement(result, headEmoji);
            } else {
                // Если игрок не найден, оставляем как есть
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    /**
     * Обрабатывает @head nickname команды
     */
    private String processPlayerHeadCommands(String message, Player sender) {
        Matcher matcher = PLAYER_HEAD_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String playerName = matcher.group(1);
            Player target = Bukkit.getPlayer(playerName);
            
            if (target != null && target.isOnline()) {
                String headDisplay = createClickableHead(target, sender);
                matcher.appendReplacement(result, headDisplay);
            } else {
                // Если игрок не найден, показываем ошибку
                matcher.appendReplacement(result, "§c[Игрок " + playerName + " не найден]");
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    /**
     * Генерирует ASCII арт головы игрока
     */
    private String generateHeadEmoji(Player player) {
        // Получаем доминирующий цвет скина (упрощенная версия)
        String skinColor = getSkinColor(player);
        String hairColor = getHairColor(player);
        
        StringBuilder head = new StringBuilder();
        head.append("§8[§r"); // Начало рамки
        
        // Генерируем упрощенную голову 3x3
        head.append(hairColor).append("███§r");
        head.append(skinColor).append("█").append("§0█").append(skinColor).append("█§r"); // Глаза
        head.append(skinColor).append("███§r");
        
        head.append("§8]§r"); // Конец рамки
        
        return head.toString();
    }

    /**
     * Создает кликабельную голову игрока
     */
    private String createClickableHead(Player target, Player sender) {
        String headUrl = skinsHook.getPlayerHeadUrl(target.getName());
        
        // Создаем компонент с hover и click событиями
        return "§6[§e" + target.getName() + " Head§6]§r";
    }

    /**
     * Получает цвет скина игрока (упрощенная версия)
     */
    private String getSkinColor(Player player) {
        // В реальной реализации здесь был бы анализ текстуры скина
        // Для демонстрации используем случайные цвета
        String[] skinColors = {"§f", "§7", "§6", "§c", "§e"};
        return skinColors[player.getName().hashCode() % skinColors.length];
    }

    /**
     * Получает цвет волос игрока (упрощенная версия)
     */
    private String getHairColor(Player player) {
        // В реальной реализации здесь был бы анализ текстуры скина
        String[] hairColors = {"§0", "§8", "§6", "§c", "§e", "§a"};
        return hairColors[(player.getName().hashCode() + 1) % hairColors.length];
    }

    /**
     * Создает Component с hover эффектом для головы игрока
     */
    public Component createHeadComponent(Player target) {
        String headUrl = skinsHook.getPlayerHeadUrl(target.getName());
        
        Component headText = Component.text("[" + target.getName() + " Head]")
                .color(NamedTextColor.GOLD)
                .hoverEvent(HoverEvent.showText(
                    Component.text("Голова игрока: " + target.getName())
                            .color(NamedTextColor.YELLOW)
                            .append(Component.newline())
                            .append(Component.text("Нажмите, чтобы получить информацию")
                                    .color(NamedTextColor.GRAY))
                ))
                .clickEvent(ClickEvent.runCommand("/lochat playerinfo " + target.getName()));
        
        return headText;
    }

    /**
     * Проверяет, включена ли функция голов игроков
     */
    public boolean isHeadEmojiEnabled() {
        return plugin.getConfigManager().getBoolean("head-emoji.enabled", true);
    }

    /**
     * Проверяет, может ли игрок использовать головы
     */
    public boolean canUseHeadEmoji(Player player) {
        return player.hasPermission("lochat.heademoji.use");
    }
}