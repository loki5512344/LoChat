package com.loki.lochat.renderer;

import com.loki.lochat.utils.MentionHandler;
import com.loki.lochat.utils.TextFormatter;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Рендерер чата с поддержкой глобального/локального режимов
 */
public class EnhancedChatRenderer implements ChatRenderer {
    private final JavaPlugin plugin;
    private final Player sender;
    private final Component originalMessage;
    private final boolean isGlobal;
    private final MentionHandler mentionHandler;

    public EnhancedChatRenderer(JavaPlugin plugin, Player sender, Component originalMessage, boolean isGlobal) {
        this.plugin = plugin;
        this.sender = sender;
        this.originalMessage = originalMessage;
        this.isGlobal = isGlobal;
        this.mentionHandler = new MentionHandler(plugin);
    }

    @Override
    public Component render(Player source, Component sourceDisplayName, Component message, Audience viewer) {
        // Красивые префиксы с градиентами и эмодзи
        Component prefixComponent;
        if (isGlobal) {
            // 🌍 GLOBAL с градиентом от золотого к оранжевому
            prefixComponent = Component.text("🌍 ")
                    .append(Component.text("G").color(net.kyori.adventure.text.format.TextColor.fromHexString("#FFD700")))
                    .append(Component.text("L").color(net.kyori.adventure.text.format.TextColor.fromHexString("#FFA500")))
                    .append(Component.text("O").color(net.kyori.adventure.text.format.TextColor.fromHexString("#FF8C00")))
                    .append(Component.text("B").color(net.kyori.adventure.text.format.TextColor.fromHexString("#FF7F50")))
                    .append(Component.text("A").color(net.kyori.adventure.text.format.TextColor.fromHexString("#FF6347")))
                    .append(Component.text("L").color(net.kyori.adventure.text.format.TextColor.fromHexString("#FF4500")));
        } else {
            // LOCAL с градиентом от голубого к синему (без эмодзи)
            prefixComponent = Component.text("L").color(net.kyori.adventure.text.format.TextColor.fromHexString("#87CEEB"))
                    .append(Component.text("O").color(net.kyori.adventure.text.format.TextColor.fromHexString("#87CEFA")))
                    .append(Component.text("C").color(net.kyori.adventure.text.format.TextColor.fromHexString("#00BFFF")))
                    .append(Component.text("A").color(net.kyori.adventure.text.format.TextColor.fromHexString("#1E90FF")))
                    .append(Component.text("L").color(net.kyori.adventure.text.format.TextColor.fromHexString("#4169E1")));
        }
        
        // Используем displayName игрока напрямую (он уже Component с градиентом)
        Component playerComponent = createPlayerComponent(source, source.displayName());
        Component processedMessage = message;

        processedMessage = TextFormatter.formatMarkdown(processedMessage);
        processedMessage = processUrls(processedMessage);
        processedMessage = processEmojis(processedMessage);

        if (viewer instanceof Player viewerPlayer) {
            processedMessage = mentionHandler.processMentions(processedMessage, source, viewerPlayer);
        }

        // Красивое разделение с градиентными стрелками
        Component separator = isGlobal ? 
            Component.text(" ▶ ", net.kyori.adventure.text.format.TextColor.fromHexString("#FFD700")) :
            Component.text(" ▶ ", net.kyori.adventure.text.format.TextColor.fromHexString("#FFFFFF"));

        return prefixComponent
                .append(separator)
                .append(playerComponent)
                .append(Component.text(": ", net.kyori.adventure.text.format.NamedTextColor.WHITE))
                .append(processedMessage);
    }

    private Component createPlayerComponent(Player player, Component displayName) {
        if (!plugin.getConfig().getBoolean("chat.hover.enabled", true)) {
            return displayName.clickEvent(ClickEvent.suggestCommand("/msg " + player.getName() + " "));
        }

        java.util.List<String> hoverLines = plugin.getConfig().getStringList("chat.hover.format");
        if (hoverLines.isEmpty()) {
            hoverLines = java.util.Arrays.asList(
                    "<gradient:#FFD700:#FFA500><bold>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</bold></gradient>",
                    "<gradient:#87CEEB:#4169E1><bold>👤 Информация об игроке</bold></gradient>",
                    "",
                    "<gray>🏷️ Имя: <white><bold>{player}</bold>",
                    "<gray>📶 Пинг: <yellow><bold>{ping}ms</bold>",
                    "<gray>❤️ Здоровье: <red><bold>{health}/20</bold>",
                    "<gray>🍖 Голод: <gold><bold>{food}/20</bold>",
                    "",
                    "<gradient:#FFD700:#FFA500><bold>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</bold></gradient>",
                    "<yellow>💬 <bold>Клик:</bold> <gray>написать личное сообщение"
            );
        }

        net.kyori.adventure.text.TextComponent.Builder hoverBuilder = Component.text();
        for (int i = 0; i < hoverLines.size(); i++) {
            String line = hoverLines.get(i);

            line = line.replace("{player}", player.getName())
                    .replace("{world}", player.getWorld().getName())
                    .replace("{ping}", String.valueOf(player.getPing()))
                    .replace("{gamemode}", getGameModeDisplay(player.getGameMode()))
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

    /**
     * Получить красивое отображение игрового режима
     */
    private String getGameModeDisplay(org.bukkit.GameMode gameMode) {
        return switch (gameMode) {
            case SURVIVAL -> "Выживание";
            case CREATIVE -> "Творчество";
            case ADVENTURE -> "Приключение";
            case SPECTATOR -> "Наблюдатель";
        };
    }

    private Component processUrls(Component message) {
        String plainText = PlainTextComponentSerializer.plainText().serialize(message);
        // Fixed regex: escape the dash in character class to avoid illegal range
        String urlPattern = "(https?://[\\w\\-.~:/?#\\[\\]@!$&'()*+,;=%]+)";
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
        
        // Основные символы
        result = result.replaceText(builder -> builder.matchLiteral(":heart:").replacement("♥"));
        result = result.replaceText(builder -> builder.matchLiteral(":star:").replacement("★"));
        result = result.replaceText(builder -> builder.matchLiteral(":check:").replacement("✓"));
        result = result.replaceText(builder -> builder.matchLiteral(":cross:").replacement("✗"));
        result = result.replaceText(builder -> builder.matchLiteral(":arrow:").replacement("→"));
        result = result.replaceText(builder -> builder.matchLiteral(":skull:").replacement("☠"));
        result = result.replaceText(builder -> builder.matchLiteral(":note:").replacement("♪"));
        result = result.replaceText(builder -> builder.matchLiteral(":sun:").replacement("☀"));
        result = result.replaceText(builder -> builder.matchLiteral(":moon:").replacement("☽"));
        result = result.replaceText(builder -> builder.matchLiteral(":snowflake:").replacement("❄"));
        result = result.replaceText(builder -> builder.matchLiteral(":fire:").replacement("▲"));
        
        // Minecraft символы
        result = result.replaceText(builder -> builder.matchLiteral(":diamond:").replacement("◆"));
        result = result.replaceText(builder -> builder.matchLiteral(":sword:").replacement("⚔"));
        result = result.replaceText(builder -> builder.matchLiteral(":pickaxe:").replacement("⛏"));
        result = result.replaceText(builder -> builder.matchLiteral(":bow:").replacement("⌐"));
        result = result.replaceText(builder -> builder.matchLiteral(":shield:").replacement("▣"));
        result = result.replaceText(builder -> builder.matchLiteral(":potion:").replacement("⚗"));
        result = result.replaceText(builder -> builder.matchLiteral(":book:").replacement("▤"));
        result = result.replaceText(builder -> builder.matchLiteral(":crown:").replacement("♔"));
        result = result.replaceText(builder -> builder.matchLiteral(":gem:").replacement("◊"));
        result = result.replaceText(builder -> builder.matchLiteral(":emerald:").replacement("◈"));
        result = result.replaceText(builder -> builder.matchLiteral(":gold:").replacement("◉"));
        result = result.replaceText(builder -> builder.matchLiteral(":iron:").replacement("◎"));
        
        // Дополнительные символы
        result = result.replaceText(builder -> builder.matchLiteral(":thumbsup:").replacement("▲"));
        result = result.replaceText(builder -> builder.matchLiteral(":thumbsdown:").replacement("▼"));
        result = result.replaceText(builder -> builder.matchLiteral(":clap:").replacement("※"));
        result = result.replaceText(builder -> builder.matchLiteral(":wave:").replacement("~"));
        result = result.replaceText(builder -> builder.matchLiteral(":peace:").replacement("✌"));
        result = result.replaceText(builder -> builder.matchLiteral(":ok:").replacement("◯"));
        result = result.replaceText(builder -> builder.matchLiteral(":facepalm:").replacement("⌐_⌐"));
        result = result.replaceText(builder -> builder.matchLiteral(":shrug:").replacement("¯\\_(ツ)_/¯"));
        result = result.replaceText(builder -> builder.matchLiteral(":thinking:").replacement("?"));
        result = result.replaceText(builder -> builder.matchLiteral(":laugh:").replacement("xD"));
        result = result.replaceText(builder -> builder.matchLiteral(":cry:").replacement(":'("));
        result = result.replaceText(builder -> builder.matchLiteral(":rage:").replacement(">:("));
        result = result.replaceText(builder -> builder.matchLiteral(":cool:").replacement("B)"));
        result = result.replaceText(builder -> builder.matchLiteral(":wink:").replacement(";)"));
        
        return result;
    }
}