package com.loki.lochat.listener;

import com.loki.lochat.api.service.MessageService;
import com.loki.lochat.core.registry.ServiceRegistry;
import com.loki.lochat.util.FoliaUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Слушатель событий чата с Paper Chat API
 * Поддерживает hover tooltips, click actions, mentions
 */
public class ChatEventListener implements Listener {
    private final JavaPlugin plugin;
    private final MessageService messageService;
    
    public ChatEventListener(JavaPlugin plugin, ServiceRegistry registry) {
        this.plugin = plugin;
        this.messageService = registry.get(MessageService.class);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        
        // Проверяем фильтры (мут, кулдаун)
        boolean allowed = messageService.processMessage(sender, message);
        
        if (!allowed) {
            event.setCancelled(true);
            return;
        }
        
        // Устанавливаем кастомный рендерер
        event.renderer(new EnhancedChatRenderer(sender, event.message()));
    }
    
    /**
     * Продвинутый рендерер чата с hover/click/mentions
     */
    private class EnhancedChatRenderer implements ChatRenderer {
        private final Player sender;
        private final Component originalMessage;
        
        public EnhancedChatRenderer(Player sender, Component originalMessage) {
            this.sender = sender;
            this.originalMessage = originalMessage;
        }
        
        @Override
        public Component render(Player source, Component sourceDisplayName, Component message, Audience viewer) {
            // Получаем конфиг формата
            String format = plugin.getConfig().getString("chat.global.format", "{prefix} {player}: {message}");
            String prefix = plugin.getConfig().getString("chat.global.prefix", "[G]");
            
            // Создаём компонент с hover и click
            Component playerComponent = createPlayerComponent(source, sourceDisplayName);
            
            // Обрабатываем сообщение
            Component processedMessage = message;
            
            // 1. Обрабатываем URL (делаем кликабельными)
            processedMessage = processUrls(processedMessage);
            
            // 2. Обрабатываем эмодзи шорткоды (:smile: -> 😊)
            processedMessage = processEmojis(processedMessage);
            
            // 3. Обрабатываем упоминания если viewer это Player
            if (viewer instanceof Player viewerPlayer) {
                processedMessage = processMentions(processedMessage, viewerPlayer);
            }
            
            // Собираем финальное сообщение
            return Component.text(prefix + " ")
                    .append(playerComponent)
                    .append(Component.text(": "))
                    .append(processedMessage);
        }
        
        /**
         * Создаёт компонент имени игрока с hover и click
         */
        private Component createPlayerComponent(Player player, Component displayName) {
            // Проверяем включен ли hover
            if (!plugin.getConfig().getBoolean("chat.hover.enabled", true)) {
                return displayName.clickEvent(ClickEvent.suggestCommand("/msg " + player.getName() + " "));
            }
            
            // Получаем формат из конфига
            java.util.List<String> hoverLines = plugin.getConfig().getStringList("chat.hover.format");
            if (hoverLines.isEmpty()) {
                // Дефолтный формат если конфиг пустой
                hoverLines = java.util.Arrays.asList(
                    "<gray>Игрок: <white>{player}",
                    "<gray>Мир: <green>{world}",
                    "",
                    "<yellow>▶ Клик: написать ЛС"
                );
            }
            
            // Строим hover text
            net.kyori.adventure.text.TextComponent.Builder hoverBuilder = Component.text();
            for (int i = 0; i < hoverLines.size(); i++) {
                String line = hoverLines.get(i);
                
                // Заменяем плейсхолдеры
                line = line.replace("{player}", player.getName())
                           .replace("{world}", player.getWorld().getName())
                           .replace("{ping}", String.valueOf(player.getPing()))
                           .replace("{gamemode}", player.getGameMode().name())
                           .replace("{health}", String.valueOf(Math.round(player.getHealth())))
                           .replace("{food}", String.valueOf(player.getFoodLevel()));
                
                // Парсим MiniMessage
                Component lineComponent = com.loki.lochat.utils.ChatFormatter.parse(line);
                hoverBuilder.append(lineComponent);
                
                // Добавляем перенос строки если не последняя
                if (i < hoverLines.size() - 1) {
                    hoverBuilder.append(Component.newline());
                }
            }
            
            // Click: открываем /msg
            return displayName
                    .hoverEvent(HoverEvent.showText(hoverBuilder.build()))
                    .clickEvent(ClickEvent.suggestCommand("/msg " + player.getName() + " "));
        }
        
        /**
         * Обрабатывает упоминания (@player) в сообщении
         */
        private Component processMentions(Component message, Player viewer) {
            String plainText = PlainTextComponentSerializer.plainText().serialize(message);
            
            // Проверяем упоминание текущего игрока
            if (plainText.toLowerCase().contains("@" + viewer.getName().toLowerCase())) {
                // Подсвечиваем упоминание
                Component highlighted = message.replaceText(builder -> 
                    builder.matchLiteral("@" + viewer.getName())
                           .replacement(Component.text("@" + viewer.getName(), NamedTextColor.YELLOW, TextDecoration.BOLD))
                );
                
                // Воспроизводим звук уведомления
                FoliaUtil.runEntityTask(plugin, viewer, () -> {
                    viewer.playSound(viewer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
                });
                
                return highlighted;
            }
            
            return message;
        }
        
        /**
         * Обрабатывает URL в сообщении (делает кликабельными)
         */
        private Component processUrls(Component message) {
            String plainText = PlainTextComponentSerializer.plainText().serialize(message);
            
            // Простой regex для URL
            String urlPattern = "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(urlPattern);
            java.util.regex.Matcher matcher = pattern.matcher(plainText);
            
            Component result = message;
            while (matcher.find()) {
                String url = matcher.group(1);
                // Делаем URL кликабельным с hover
                Component urlComponent = Component.text(url, NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                        .hoverEvent(HoverEvent.showText(Component.text("Открыть: " + url, NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.openUrl(url));
                
                result = result.replaceText(builder -> 
                    builder.matchLiteral(url).replacement(urlComponent)
                );
            }
            
            return result;
        }
        
        /**
         * Обрабатывает эмодзи шорткоды (:smile: -> ☺)
         * Использует только MC-совместимые Unicode символы
         */
        private Component processEmojis(Component message) {
            Component result = message;
            
            // MC-совместимые Unicode эмодзи
            result = result.replaceText(builder -> builder.matchLiteral(":smile:").replacement("☺"));
            result = result.replaceText(builder -> builder.matchLiteral(":heart:").replacement("♥"));
            result = result.replaceText(builder -> builder.matchLiteral(":fire:").replacement("※"));
            result = result.replaceText(builder -> builder.matchLiteral(":star:").replacement("★"));
            result = result.replaceText(builder -> builder.matchLiteral(":check:").replacement("✓"));
            result = result.replaceText(builder -> builder.matchLiteral(":cross:").replacement("✗"));
            result = result.replaceText(builder -> builder.matchLiteral(":skull:").replacement("☠"));
            result = result.replaceText(builder -> builder.matchLiteral(":arrow:").replacement("→"));
            result = result.replaceText(builder -> builder.matchLiteral(":note:").replacement("♪"));
            result = result.replaceText(builder -> builder.matchLiteral(":sun:").replacement("☀"));
            result = result.replaceText(builder -> builder.matchLiteral(":moon:").replacement("☾"));
            result = result.replaceText(builder -> builder.matchLiteral(":peace:").replacement("☮"));
            result = result.replaceText(builder -> builder.matchLiteral(":yin:").replacement("☯"));
            result = result.replaceText(builder -> builder.matchLiteral(":flower:").replacement("✿"));
            result = result.replaceText(builder -> builder.matchLiteral(":snowflake:").replacement("❄"));
            result = result.replaceText(builder -> builder.matchLiteral(":lightning:").replacement("⚡"));
            result = result.replaceText(builder -> builder.matchLiteral(":crown:").replacement("♔"));
            result = result.replaceText(builder -> builder.matchLiteral(":sword:").replacement("⚔"));
            result = result.replaceText(builder -> builder.matchLiteral(":shield:").replacement("⛨"));
            result = result.replaceText(builder -> builder.matchLiteral(":diamond:").replacement("◆"));
            
            return result;
        }
    }
}
