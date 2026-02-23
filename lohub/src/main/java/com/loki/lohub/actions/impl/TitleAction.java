package com.loki.lohub.actions.impl;

import com.loki.lohub.actions.Action;
import com.loki.lohub.actions.ActionType;
import com.loki.lohub.utils.PlaceholderUtil;
import com.loki.lohub.utils.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;

public class TitleAction implements Action {
    
    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;
    
    public TitleAction(String titleData) {
        String[] parts = titleData.split(";");
        
        this.title = parts.length > 0 ? parts[0] : "";
        this.subtitle = parts.length > 1 ? parts[1] : "";
        this.fadeIn = parts.length > 2 ? parseInt(parts[2], 10) : 10;
        this.stay = parts.length > 3 ? parseInt(parts[3], 40) : 40;
        this.fadeOut = parts.length > 4 ? parseInt(parts[4], 10) : 10;
    }
    
    @Override
    public void execute(Player player) {
        String parsedTitle = PlaceholderUtil.parse(title, player);
        String parsedSubtitle = PlaceholderUtil.parse(subtitle, player);
        
        Component titleComponent = Component.text(TextUtil.colorize(parsedTitle));
        Component subtitleComponent = Component.text(TextUtil.colorize(parsedSubtitle));
        
        Title.Times times = Title.Times.times(
            Duration.ofMillis(fadeIn * 50L),
            Duration.ofMillis(stay * 50L),
            Duration.ofMillis(fadeOut * 50L)
        );
        
        Title title = Title.title(titleComponent, subtitleComponent, times);
        player.showTitle(title);
    }
    
    @Override
    public ActionType getType() {
        return ActionType.TITLE;
    }
    
    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
