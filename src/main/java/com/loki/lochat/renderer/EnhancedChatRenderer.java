package com.loki.lochat.renderer;

import com.loki.lochat.renderer.components.*;
import com.loki.lochat.utils.MentionHandler;
import com.loki.lochat.utils.TextFormatter;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Рендерер чата — глобальный и локальный режимы
 */
public class EnhancedChatRenderer implements ChatRenderer {

    private static final TextColor FALLBACK_COLOR = NamedTextColor.WHITE;

    private final JavaPlugin plugin;
    private final boolean isGlobal;
    private final MentionHandler mentionHandler;
    private final com.loki.lochat.config.ConfigManager cfg;

    public EnhancedChatRenderer(JavaPlugin plugin, boolean isGlobal) {
        this.plugin = plugin;
        this.isGlobal = isGlobal;
        this.mentionHandler = new MentionHandler(plugin);

        com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
        this.cfg = loChat.getConfigManager();
    }

    @Override
    public Component render(Player source, Component sourceDisplayName, Component message, Audience viewer) {
        // Обрабатываем сообщение
        Component processed = message;
        processed = TextFormatter.formatMarkdown(processed);
        processed = UrlProcessor.process(processed);
        processed = EmojiComponent.processEmojis(processed);

        if (viewer instanceof Player vp) {
            processed = mentionHandler.processMentions(processed, source, vp);
        }

        // Получаем формат из конфига
        com.loki.lochat.config.AppearanceConfig appearanceCfg = cfg.getAppearanceConfig();
        String format = isGlobal ? appearanceCfg.getGlobalChatFormat() : appearanceCfg.getLocalChatFormat();
        
        // Строим компоненты
        Component emoji = buildEmojiComponent();
        Component prefix = buildPrefix();
        Component separator = buildSeparator();
        Component playerPrefix = buildPlayerPrefix(source);
        Component player = buildPlayerComponent(source);
        
        return buildFormattedMessage(format, emoji, prefix, separator, playerPrefix, player, processed);
    }
    
    private Component buildFormattedMessage(String format, Component emoji, Component prefix, 
                                           Component separator, Component playerPrefix, 
                                           Component player, Component message) {
        Component result = Component.empty();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(format);

        int lastEnd = 0;
        
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                result = result.append(Component.text(format.substring(lastEnd, matcher.start())));
            }
            
            String placeholder = matcher.group(1);
            switch (placeholder) {
                case "emoji" -> result = result.append(emoji);
                case "prefix" -> result = result.append(prefix);
                case "separator" -> result = result.append(separator);
                case "player_prefix" -> result = result.append(playerPrefix);
                case "player" -> result = result.append(player);
                case "message" -> result = result.append(message);
                default -> result = result.append(Component.text("{" + placeholder + "}"));
            }
            
            lastEnd = matcher.end();
        }
        
        if (lastEnd < format.length()) {
            result = result.append(Component.text(format.substring(lastEnd)));
        }
        
        return result;
    }
    
    private Component buildEmojiComponent() {
        com.loki.lochat.config.AppearanceConfig appearanceCfg = cfg.getAppearanceConfig();
        String emoji = isGlobal ? appearanceCfg.getGlobalEmoji() : appearanceCfg.getLocalEmoji();
        return emoji.isEmpty() ? Component.empty() : Component.text(emoji);
    }
    
    private Component buildPlayerPrefix(Player player) {
        com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
        com.loki.lochat.gradient.GradientModule gradientModule = loChat.getGradientModule();
        
        if (gradientModule != null && gradientModule.isEnabled()) {
            String prefix = gradientModule.getPrefix(player);
            if (!prefix.isEmpty()) {
                return com.loki.lochat.utils.ChatFormatter.parse(prefix);
            }
        }
        
        return Component.empty();
    }

    private Component buildPrefix() {
        com.loki.lochat.config.AppearanceConfig appearanceCfg = cfg.getAppearanceConfig();
        
        String text = isGlobal ? appearanceCfg.getGlobalText() : appearanceCfg.getLocalText();
        List<String> colors = isGlobal ? appearanceCfg.getGlobalColors() : appearanceCfg.getLocalColors();

        return GradientBuilder.build(text, colors);
    }

    private Component buildSeparator() {
        com.loki.lochat.config.AppearanceConfig appearanceCfg = cfg.getAppearanceConfig();
        String text = isGlobal ? appearanceCfg.getGlobalSeparatorText() : appearanceCfg.getLocalSeparatorText();
        String color = isGlobal ? appearanceCfg.getGlobalSeparatorColor() : appearanceCfg.getLocalSeparatorColor();
        return Component.text(text, parseColor(color));
    }

    private Component buildPlayerComponent(Player player) {
        Component displayName = player.displayName();

        com.loki.lochat.config.AppearanceConfig appearanceCfg = cfg.getAppearanceConfig();
        
        if (!appearanceCfg.isHoverEnabled()) {
            return displayName.clickEvent(ClickEvent.suggestCommand("/msg " + player.getName() + " "));
        }

        List<String> hoverLines = appearanceCfg.getHoverFormat();
        if (hoverLines.isEmpty()) {
            hoverLines = List.of(
                "&#7858E9▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&#B798A8✦ &f{player}",
                "",
                "&#9878C9⏱ &fПинг: &#7858E9{ping}ms",
                "&#9878C9♥ &fЗдоровье: &#7858E9{health}/20",
                "&#9878C9🍖 &fГолод: &#7858E9{food}/20",
                "",
                "&#7858E9▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&#B798A8✦ &fНажми чтобы написать"
            );
        }
        
        net.kyori.adventure.text.TextComponent.Builder hoverBuilder = Component.text();

        for (int i = 0; i < hoverLines.size(); i++) {
            String line = hoverLines.get(i)
                    .replace("{player}",   player.getName())
                    .replace("{world}",    player.getWorld().getName())
                    .replace("{ping}",     String.valueOf(player.getPing()))
                    .replace("{gamemode}", player.getGameMode().name())
                    .replace("{health}",   String.valueOf(Math.round(player.getHealth())))
                    .replace("{food}",     String.valueOf(player.getFoodLevel()));

            hoverBuilder.append(com.loki.lochat.utils.ChatFormatter.parse(line));
            if (i < hoverLines.size() - 1) hoverBuilder.append(Component.newline());
        }

        return displayName
                .hoverEvent(HoverEvent.showText(hoverBuilder.build()))
                .clickEvent(ClickEvent.suggestCommand("/msg " + player.getName() + " "));
    }

    private TextColor parseColor(String hex) {
        if (hex == null || hex.isBlank()) return FALLBACK_COLOR;
        String n = hex.startsWith("#") ? hex : "#" + hex;
        TextColor c = TextColor.fromHexString(n);
        return c != null ? c : FALLBACK_COLOR;
    }
}
