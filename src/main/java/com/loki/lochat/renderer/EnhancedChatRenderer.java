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
        processed = processUrls(processed);
        processed = processEmojis(processed);

        if (viewer instanceof Player vp) {
            processed = mentionHandler.processMentions(processed, source, vp);
        }

        // ✅ Получаем кастомный формат из конфига
        com.loki.lochat.config.AppearanceConfig appearanceCfg = cfg.getAppearanceConfig();
        String format = isGlobal ? appearanceCfg.getGlobalChatFormat() : appearanceCfg.getLocalChatFormat();
        
        // Строим компоненты
        Component emoji = buildEmojiComponent();
        Component prefix = buildPrefix();
        Component separator = buildSeparator();
        Component playerPrefix = buildPlayerPrefix(source);
        Component player = buildPlayerComponent(source);
        
        // Парсим формат и заменяем плейсхолдеры
        return buildFormattedMessage(format, emoji, prefix, separator, playerPrefix, player, processed);
    }
    
    /**
     * Строит финальное сообщение по формату с плейсхолдерами
     */
    private Component buildFormattedMessage(String format, Component emoji, Component prefix, 
                                           Component separator, Component playerPrefix, 
                                           Component player, Component message) {
        Component result = Component.empty();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(format);

        int lastEnd = 0;
        
        while (matcher.find()) {
            // Добавляем текст до плейсхолдера
            if (matcher.start() > lastEnd) {
                result = result.append(Component.text(format.substring(lastEnd, matcher.start())));
            }
            
            // Заменяем плейсхолдер
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
        
        // Добавляем оставшийся текст
        if (lastEnd < format.length()) {
            result = result.append(Component.text(format.substring(lastEnd)));
        }
        
        return result;
    }
    
    /**
     * Строит компонент эмодзи
     */
    private Component buildEmojiComponent() {
        com.loki.lochat.config.AppearanceConfig appearanceCfg = cfg.getAppearanceConfig();
        String emoji = isGlobal ? appearanceCfg.getGlobalEmoji() : appearanceCfg.getLocalEmoji();
        return emoji.isEmpty() ? Component.empty() : Component.text(emoji);
    }
    
    /**
     * Строит префикс игрока из LuckPerms/градиента
     */
    private Component buildPlayerPrefix(Player player) {
        // Получаем градиентный модуль
        com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
        com.loki.lochat.gradient.GradientModule gradientModule = loChat.getGradientModule();
        
        if (gradientModule != null) {
            com.loki.lochat.gradient.data.GradientPlayerData data = 
                gradientModule.getDataManager().getPlayerData(player.getUniqueId());
            
            if (data != null && data.isPrefixEnabled() && data.hasPrefix()) {
                String prefixText = data.getPrefix();
                if (data.hasColors() && data.isColorEnabled()) {
                    // Применяем градиент к префиксу
                    return buildGradientText(prefixText, data.getColors());
                }
                return Component.text(prefixText);
            }
        }
        
        return Component.empty();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Префикс
    // ──────────────────────────────────────────────────────────────────────────

    private Component buildPrefix() {
        // ✅ FIX: Получаем AppearanceConfig через ConfigManager
        com.loki.lochat.config.AppearanceConfig appearanceCfg = cfg.getAppearanceConfig();
        
        String text = isGlobal ? appearanceCfg.getGlobalText() : appearanceCfg.getLocalText();
        List<String> colors = isGlobal ? appearanceCfg.getGlobalColors() : appearanceCfg.getLocalColors();

        return buildGradientText(text, colors);
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
        com.loki.lochat.config.AppearanceConfig appearanceCfg = cfg.getAppearanceConfig();
        String text = isGlobal ? appearanceCfg.getGlobalSeparatorText() : appearanceCfg.getLocalSeparatorText();
        String color = isGlobal ? appearanceCfg.getGlobalSeparatorColor() : appearanceCfg.getLocalSeparatorColor();
        return Component.text(text, parseColor(color));
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Ник игрока с hover
    // ──────────────────────────────────────────────────────────────────────────

    private Component buildPlayerComponent(Player player) {
        Component displayName = player.displayName();

        // ✅ FIX: Получаем AppearanceConfig для проверки hover
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
        // ✅ FIX: Получаем AppearanceConfig для эмодзи
        com.loki.lochat.config.AppearanceConfig appearanceCfg = cfg.getAppearanceConfig();
        
        Component result = message;
        for (java.util.Map.Entry<String, String> e : appearanceCfg.getEmojis().entrySet()) {
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
