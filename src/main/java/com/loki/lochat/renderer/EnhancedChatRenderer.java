package com.loki.lochat.renderer;

import com.loki.lochat.utils.MentionHandler;
import com.loki.lochat.utils.TextFormatter;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Рендерер чата — глобальный и локальный режимы.
 * Цвета PREFIX и текста сообщения полностью настраиваются через chat-appearance.yml.
 */
public class EnhancedChatRenderer implements ChatRenderer {

    private static final TextColor FALLBACK_COLOR = NamedTextColor.WHITE;

    private final JavaPlugin plugin;
    private final Player sender;
    private final boolean isGlobal;
    private final MentionHandler mentionHandler;
    private final com.loki.lochat.config.AppearanceConfig cfg;
    private final com.loki.lochat.config.HardcodedMessages hm;

    public EnhancedChatRenderer(JavaPlugin plugin, Player sender, Component originalMessage, boolean isGlobal) {
        this.plugin = plugin;
        this.sender = sender;
        this.isGlobal = isGlobal;
        this.mentionHandler = new MentionHandler(plugin);

        com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
        this.cfg = loChat.getConfigManager().getAppearanceConfig();
        this.hm = loChat.getConfigManager().getHardcodedMessages();
    }

    @Override
    public Component render(Player source, Component sourceDisplayName, Component message, Audience viewer) {
        Component prefix    = buildPrefix();
        Component separator = buildSeparator();
        Component player    = buildPlayerComponent(source);

        Component processed = message;
        processed = TextFormatter.formatMarkdown(processed);
        processed = processUrls(processed);
        processed = processEmojis(processed);

        if (viewer instanceof Player vp) {
            processed = mentionHandler.processMentions(processed, source, vp);
        }

        return prefix
                .append(separator)
                .append(player)
                .append(Component.text(": ", NamedTextColor.WHITE))
                .append(processed);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Префикс
    // ──────────────────────────────────────────────────────────────────────────

    private Component buildPrefix() {
        String emoji  = isGlobal ? cfg.getGlobalEmoji()  : cfg.getLocalEmoji();
        String text   = isGlobal ? cfg.getGlobalText()   : cfg.getLocalText();
        List<String> colors = isGlobal ? cfg.getGlobalColors() : cfg.getLocalColors();

        Component prefix = Component.empty();

        if (!emoji.isEmpty()) {
            prefix = prefix.append(Component.text(emoji + " "));
        }

        prefix = prefix.append(buildGradientText(text, colors));
        return prefix;
    }

    /**
     * Строит компонент с плавным градиентом.
     * colors — список hex-цветов (минимум 1).
     * Если цветов меньше чем букв — интерполируем плавно между соседними цветами.
     * Если цветов больше чем букв — берём по одному на букву.
     */
    private Component buildGradientText(String text, List<String> colors) {
        if (text.isEmpty()) return Component.empty();

        if (colors.isEmpty()) {
            return Component.text(text, FALLBACK_COLOR);
        }

        // Один цвет — просто красим весь текст
        if (colors.size() == 1) {
            return Component.text(text, parseColor(colors.get(0)));
        }

        char[] chars = text.toCharArray();
        int len = chars.length;
        Component result = Component.empty();

        for (int i = 0; i < len; i++) {
            // Нормализованная позиция 0..1
            float t = len == 1 ? 0f : (float) i / (len - 1);

            // Какой сегмент градиента
            float scaled = t * (colors.size() - 1);
            int seg = (int) scaled;
            float frac = scaled - seg;

            // Зажимаем в пределах списка
            if (seg >= colors.size() - 1) {
                seg = colors.size() - 2;
                frac = 1f;
            }

            TextColor from = parseColor(colors.get(seg));
            TextColor to   = parseColor(colors.get(seg + 1));
            TextColor blended = blend(from, to, frac);

            result = result.append(Component.text(String.valueOf(chars[i]), blended));
        }

        return result;
    }

    /** Линейная интерполяция между двумя TextColor */
    private static TextColor blend(TextColor from, TextColor to, float t) {
        int r = Math.round(from.red()   + t * (to.red()   - from.red()));
        int g = Math.round(from.green() + t * (to.green() - from.green()));
        int b = Math.round(from.blue()  + t * (to.blue()  - from.blue()));
        return TextColor.color(clamp(r), clamp(g), clamp(b));
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    // ──────────────────────────────────────────────────────────────────────────
    //  Разделитель
    // ──────────────────────────────────────────────────────────────────────────

    private Component buildSeparator() {
        String text  = isGlobal ? cfg.getGlobalSeparatorText()  : cfg.getLocalSeparatorText();
        String color = isGlobal ? cfg.getGlobalSeparatorColor() : cfg.getLocalSeparatorColor();
        return Component.text(text, parseColor(color));
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Ник игрока с hover
    // ──────────────────────────────────────────────────────────────────────────

    private Component buildPlayerComponent(Player player) {
        Component displayName = player.displayName();

        if (!cfg.isHoverEnabled()) {
            return displayName.clickEvent(ClickEvent.suggestCommand("/msg " + player.getName() + " "));
        }

        List<String> hoverLines = cfg.getHoverFormat();
        net.kyori.adventure.text.TextComponent.Builder hoverBuilder = Component.text();

        for (int i = 0; i < hoverLines.size(); i++) {
            String line = hoverLines.get(i)
                    .replace("{player}",   player.getName())
                    .replace("{world}",    player.getWorld().getName())
                    .replace("{ping}",     String.valueOf(player.getPing()))
                    .replace("{gamemode}", hm.getGamemodeName(player.getGameMode().name().toLowerCase()))
                    .replace("{health}",   String.valueOf(Math.round(player.getHealth())))
                    .replace("{food}",     String.valueOf(player.getFoodLevel()));

            hoverBuilder.append(com.loki.lochat.utils.ChatFormatter.parse(line));
            if (i < hoverLines.size() - 1) hoverBuilder.append(Component.newline());
        }

        return displayName
                .hoverEvent(HoverEvent.showText(hoverBuilder.build()))
                .clickEvent(ClickEvent.suggestCommand("/msg " + player.getName() + " "));
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  URL и эмодзи
    // ──────────────────────────────────────────────────────────────────────────

    private Component processUrls(Component message) {
        String plain = PlainTextComponentSerializer.plainText().serialize(message);
        java.util.regex.Pattern pattern =
                java.util.regex.Pattern.compile("(https?://[\\w\\-.~:/?#\\[\\]@!$&'()*+,;=%]+)");
        java.util.regex.Matcher matcher = pattern.matcher(plain);

        Component result = message;
        while (matcher.find()) {
            String url = matcher.group(1);
            Component urlComp = Component.text(url, NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                    .hoverEvent(HoverEvent.showText(Component.text("Открыть: " + url, NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.openUrl(url));
            result = result.replaceText(b -> b.matchLiteral(url).replacement(urlComp));
        }
        return result;
    }

    private Component processEmojis(Component message) {
        Component result = message;
        for (java.util.Map.Entry<String, String> e : cfg.getEmojis().entrySet()) {
            result = result.replaceText(b -> b.matchLiteral(e.getKey()).replacement(e.getValue()));
        }
        return result;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Утилиты
    // ──────────────────────────────────────────────────────────────────────────

    private TextColor parseColor(String hex) {
        if (hex == null || hex.isBlank()) return FALLBACK_COLOR;
        String n = hex.startsWith("#") ? hex : "#" + hex;
        TextColor c = TextColor.fromHexString(n);
        return c != null ? c : FALLBACK_COLOR;
    }
}
