package com.loki.lochat.renderer.components;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;

public class GradientBuilder {
    
    public static Component build(String text, List<String> colors) {
        if (colors == null || colors.size() < 2) {
            return Component.text(text);
        }

        TextColor[] gradientColors = colors.stream()
            .map(TextColor::fromHexString)
            .toArray(TextColor[]::new);

        TextComponent.Builder builder = Component.text();
        int len = text.length();

        for (int i = 0; i < len; i++) {
            float t = (float) i / (len - 1);
            int segmentIndex = (int) (t * (gradientColors.length - 1));
            float segmentT = (t * (gradientColors.length - 1)) - segmentIndex;

            TextColor from = gradientColors[segmentIndex];
            TextColor to = gradientColors[Math.min(segmentIndex + 1, gradientColors.length - 1)];
            TextColor blended = blend(from, to, segmentT);

            builder.append(Component.text(text.charAt(i)).color(blended));
        }

        return builder.build();
    }

    private static TextColor blend(TextColor from, TextColor to, float t) {
        int r = (int) (from.red() * (1 - t) + to.red() * t);
        int g = (int) (from.green() * (1 - t) + to.green() * t);
        int b = (int) (from.blue() * (1 - t) + to.blue() * t);
        return TextColor.color(clamp(r), clamp(g), clamp(b));
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
