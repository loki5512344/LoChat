package com.loki.lochat.listener;

import com.loki.lochat.api.service.MessageService;
import com.loki.lochat.core.filter.AdvancedMessageFilter;
import com.loki.lochat.core.registry.ServiceRegistry;
import com.loki.lochat.utils.MentionHandler;
import com.loki.lochat.utils.TextFormatter;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatEventListener implements Listener {
    private final JavaPlugin plugin;
    private final MessageService messageService;
    private final AdvancedMessageFilter advancedFilter;
    private final MentionHandler mentionHandler;

    public ChatEventListener(JavaPlugin plugin, ServiceRegistry registry) {
        this.plugin = plugin;
        this.messageService = registry.get(MessageService.class);
        this.advancedFilter = new AdvancedMessageFilter(plugin.getConfig());
        this.mentionHandler = new MentionHandler(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        boolean allowed = messageService.processMessage(sender, message);

        if (!allowed) {
            event.setCancelled(true);
            return;
        }

        AdvancedMessageFilter.FilterResult filterResult = advancedFilter.filterMessage(sender, message);

        if (!filterResult.allowed()) {
            event.setCancelled(true);
            sender.sendMessage(com.loki.lochat.utils.ChatFormatter.parse(filterResult.blockReason()));
            return;
        }

        String filteredMessage = filterResult.filteredMessage();
        
        // Определяем тип чата
        boolean isGlobal = message.startsWith("!");
        
        event.message(com.loki.lochat.utils.ChatFormatter.parse(filteredMessage));

        event.renderer(new EnhancedChatRenderer(sender, event.message(), isGlobal));
    }

    private class EnhancedChatRenderer implements ChatRenderer {
        private final Player sender;
        private final Component originalMessage;
        private final boolean isGlobal;

        public EnhancedChatRenderer(Player sender, Component originalMessage, boolean isGlobal) {
            this.sender = sender;
            this.originalMessage = originalMessage;
            this.isGlobal = isGlobal;
        }

        @Override
        public Component render(Player source, Component sourceDisplayName, Component message, Audience viewer) {
            // Префикс в зависимости от типа чата
            Component prefixComponent;
            if (isGlobal) {
                // [G] с желтой буквой G
                prefixComponent = Component.text("[")
                        .color(net.kyori.adventure.text.format.NamedTextColor.GRAY)
                        .append(Component.text("G").color(net.kyori.adventure.text.format.NamedTextColor.GOLD))
                        .append(Component.text("]").color(net.kyori.adventure.text.format.NamedTextColor.GRAY));
            } else {
                // [L] с голубой буквой L
                prefixComponent = Component.text("[")
                        .color(net.kyori.adventure.text.format.NamedTextColor.GRAY)
                        .append(Component.text("L").color(net.kyori.adventure.text.format.NamedTextColor.AQUA))
                        .append(Component.text("]").color(net.kyori.adventure.text.format.NamedTextColor.GRAY));
            }
            
            Component playerComponent = createPlayerComponent(source, sourceDisplayName);
            Component processedMessage = message;

            processedMessage = TextFormatter.formatMarkdown(processedMessage);
            processedMessage = processUrls(processedMessage);
            processedMessage = processEmojis(processedMessage);

            if (viewer instanceof Player viewerPlayer) {
                processedMessage = mentionHandler.processMentions(processedMessage, source, viewerPlayer);
            }

            return prefixComponent
                    .append(Component.text(" "))
                    .append(playerComponent)
                    .append(Component.text(": ").color(net.kyori.adventure.text.format.NamedTextColor.GRAY))
                    .append(processedMessage);
        }

        private Component createPlayerComponent(Player player, Component displayName) {
            if (!plugin.getConfig().getBoolean("chat.hover.enabled", true)) {
                return displayName.clickEvent(ClickEvent.suggestCommand("/msg " + player.getName() + " "));
            }

            java.util.List<String> hoverLines = plugin.getConfig().getStringList("chat.hover.format");
            if (hoverLines.isEmpty()) {
                hoverLines = java.util.Arrays.asList(
                        "<gray>Игрок: <white>{player}",
                        "<gray>Мир: <green>{world}",
                        "",
                        "<yellow>▶ Клик: написать ЛС"
                );
            }

            net.kyori.adventure.text.TextComponent.Builder hoverBuilder = Component.text();
            for (int i = 0; i < hoverLines.size(); i++) {
                String line = hoverLines.get(i);

                line = line.replace("{player}", player.getName())
                        .replace("{world}", player.getWorld().getName())
                        .replace("{ping}", String.valueOf(player.getPing()))
                        .replace("{gamemode}", player.getGameMode().name())
                        .replace("{health}", String.valueOf(Math.round(player.getHealth())))
                        .replace("{food}", String.valueOf(player.getFoodLevel()));

                Component lineComponent = com.loki.lochat.utils.ChatFormatter.parse(line);
                hoverBuilder.append(lineComponent);

                if (i < hoverLines.size() - 1) {
                    hoverBuilder.append(Component.newline());
                }
            }

            return displayName
                    .hoverEvent(HoverEvent.showText(hoverBuilder.build()))
                    .clickEvent(ClickEvent.suggestCommand("/msg " + player.getName() + " "));
        }

        private Component processUrls(Component message) {
            String plainText = PlainTextComponentSerializer.plainText().serialize(message);
            // Fixed regex: escape the dash in character class to avoid illegal range
            String urlPattern = "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(urlPattern);
            java.util.regex.Matcher matcher = pattern.matcher(plainText);

            Component result = message;
            while (matcher.find()) {
                String url = matcher.group(1);
                Component urlComponent = Component.text(url, NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                        .hoverEvent(HoverEvent.showText(Component.text("Открыть: " + url, NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.openUrl(url));

                result = result.replaceText(builder ->
                        builder.matchLiteral(url).replacement(urlComponent)
                );
            }

            return result;
        }

        private Component processEmojis(Component message) {
            Component result = message;
            result = result.replaceText(builder -> builder.matchLiteral(":heart:").replacement("❤"));
            result = result.replaceText(builder -> builder.matchLiteral(":star:").replacement("★"));
            result = result.replaceText(builder -> builder.matchLiteral(":check:").replacement("✔"));
            result = result.replaceText(builder -> builder.matchLiteral(":cross:").replacement("✖"));
            result = result.replaceText(builder -> builder.matchLiteral(":arrow:").replacement("→"));
            result = result.replaceText(builder -> builder.matchLiteral(":skull:").replacement("☠"));
            result = result.replaceText(builder -> builder.matchLiteral(":note:").replacement("♪"));
            result = result.replaceText(builder -> builder.matchLiteral(":sun:").replacement("☀"));
            result = result.replaceText(builder -> builder.matchLiteral(":moon:").replacement("☽"));
            result = result.replaceText(builder -> builder.matchLiteral(":snowflake:").replacement("❄"));
            result = result.replaceText(builder -> builder.matchLiteral(":fire:").replacement("🔥"));
            result = result.replaceText(builder -> builder.matchLiteral(":diamond:").replacement("◆"));
            result = result.replaceText(builder -> builder.matchLiteral(":sword:").replacement("⚔"));
            result = result.replaceText(builder -> builder.matchLiteral(":pickaxe:").replacement("⛏"));
            result = result.replaceText(builder -> builder.matchLiteral(":bow:").replacement("🏹"));
            result = result.replaceText(builder -> builder.matchLiteral(":shield:").replacement("🛡"));
            result = result.replaceText(builder -> builder.matchLiteral(":potion:").replacement("🧪"));
            result = result.replaceText(builder -> builder.matchLiteral(":book:").replacement("📖"));
            result = result.replaceText(builder -> builder.matchLiteral(":crown:").replacement("👑"));
            result = result.replaceText(builder -> builder.matchLiteral(":gem:").replacement("💎"));
            return result;
        }
    }
}
