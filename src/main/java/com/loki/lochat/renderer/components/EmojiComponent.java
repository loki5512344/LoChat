package com.loki.lochat.renderer.components;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.configuration.ConfigurationSection;

public class EmojiComponent {
    
    private EmojiComponent() {
    }

    public static Component build(ConfigurationSection emojiSection) {
        if (emojiSection == null || !emojiSection.getBoolean("enabled", false)) {
            return Component.empty();
        }

        String text = emojiSection.getString("text", "");
        String color = emojiSection.getString("color", "#FFFFFF");

        return Component.text(text)
            .color(TextColor.fromHexString(color));
    }

    public static Component processEmojis(Component message) {
        if (!(message instanceof TextComponent textComp)) {
            return message;
        }

        String content = textComp.content();
        content = content.replace(":heart:", "❤")
                        .replace(":star:", "⭐")
                        .replace(":fire:", "🔥")
                        .replace(":check:", "✓")
                        .replace(":cross:", "✗");

        return textComp.content(content);
    }
}
