package com.loki.lochat.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFormatter {
    
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("\\*(.+?)\\*");
    private static final Pattern UNDERLINE_PATTERN = Pattern.compile("__(.+?)__");
    private static final Pattern STRIKETHROUGH_PATTERN = Pattern.compile("~~(.+?)~~");
    private static final Pattern CODE_PATTERN = Pattern.compile("`(.+?)`");
    
    public static Component formatMarkdown(Component message) {
        String text = PlainTextComponentSerializer.plainText().serialize(message);
        
        text = formatBold(text);
        text = formatItalic(text);
        text = formatUnderline(text);
        text = formatStrikethrough(text);
        text = formatCode(text);
        
        return ChatFormatter.parse(text);
    }
    
    private static String formatBold(String text) {
        Matcher matcher = BOLD_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String content = matcher.group(1);
            matcher.appendReplacement(result, "<bold>" + content + "</bold>");
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    private static String formatItalic(String text) {
        Matcher matcher = ITALIC_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String content = matcher.group(1);
            if (!content.startsWith("*") && !content.endsWith("*")) {
                matcher.appendReplacement(result, "<italic>" + content + "</italic>");
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    private static String formatUnderline(String text) {
        Matcher matcher = UNDERLINE_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String content = matcher.group(1);
            matcher.appendReplacement(result, "<underlined>" + content + "</underlined>");
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    private static String formatStrikethrough(String text) {
        Matcher matcher = STRIKETHROUGH_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String content = matcher.group(1);
            matcher.appendReplacement(result, "<strikethrough>" + content + "</strikethrough>");
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    private static String formatCode(String text) {
        Matcher matcher = CODE_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String content = matcher.group(1);
            matcher.appendReplacement(result, "&#888888`" + content + "`");
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
}
