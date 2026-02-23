package com.loki.lochat.managers;

import com.loki.lochat.LoChat;
import com.loki.lochat.util.FoliaUtil;
import com.loki.lochat.utils.ChatFormatter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoMessageManager {

    private final LoChat plugin;
    private final Random random = new Random();
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private boolean running = false;
    
    private Map<String, List<String>> messages;
    private List<String> messageOrder;
    private String prefix;
    private String mode;

    public AutoMessageManager(LoChat plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        File file = new File(plugin.getDataFolder(), "automessages.yml");
        if (!file.exists()) {
            plugin.saveResource("automessages.yml", false);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        // Настройки
        prefix = config.getString("settings.prefix", "&6[&eINFO&6]&r");
        mode = config.getString("settings.mode", "sequential").toLowerCase();
        
        // Загружаем сообщения
        messages = new LinkedHashMap<>();
        ConfigurationSection msgSection = config.getConfigurationSection("messages");
        
        if (msgSection != null) {
            for (String key : msgSection.getKeys(false)) {
                List<String> lines = new ArrayList<>();
                
                if (msgSection.isList(key)) {
                    lines.addAll(msgSection.getStringList(key));
                } else {
                    String single = msgSection.getString(key);
                    if (single != null) {
                        lines.add(single);
                    }
                }
                
                if (!lines.isEmpty()) {
                    messages.put(key, lines);
                }
            }
        }
        
        // Определяем порядок
        messageOrder = new ArrayList<>();
        
        if (mode.equals("custom")) {
            String orderStr = config.getString("settings.order", "");
            for (String item : orderStr.split(",")) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        int index = Integer.parseInt(trimmed) - 1;
                        List<String> keys = new ArrayList<>(messages.keySet());
                        if (index >= 0 && index < keys.size()) {
                            messageOrder.add(keys.get(index));
                        }
                    } catch (NumberFormatException e) {
                        if (messages.containsKey(trimmed)) {
                            messageOrder.add(trimmed);
                        }
                    }
                }
            }
        }
        
        if (messageOrder.isEmpty()) {
            messageOrder.addAll(messages.keySet());
        }
        
        plugin.getLogger().info("Загружено " + messages.size() + " автосообщений");
    }

    public void start() {
        if (!plugin.getConfigManager().isAutoMessagesEnabled()) {
            return;
        }

        if (messages == null || messages.isEmpty()) {
            plugin.getLogger().warning("Автосообщения не настроены!");
            return;
        }

        int intervalSeconds = plugin.getConfigManager().getAutoMessagesInterval();
        long intervalTicks = intervalSeconds * 20L;

        // Используем FoliaUtil для совместимости с Paper и Folia
        FoliaUtil.runTimerAsync(plugin, this::broadcastNextMessage, intervalTicks, intervalTicks);
        running = true;

        plugin.getLogger().info("Автосообщения запущены (интервал: " + intervalSeconds + " сек, режим: " + mode + ")");
    }

    public void stop() {
        running = false;
    }

    private void broadcastNextMessage() {
        if (messageOrder.isEmpty() || Bukkit.getOnlinePlayers().isEmpty()) {
            return;
        }

        String messageKey;
        
        if (mode.equals("random")) {
            messageKey = messageOrder.get(random.nextInt(messageOrder.size()));
        } else {
            int index = currentIndex.getAndUpdate(i -> (i + 1) % messageOrder.size());
            messageKey = messageOrder.get(index);
        }
        
        List<String> lines = messages.get(messageKey);
        if (lines == null || lines.isEmpty()) return;

        boolean isFirstLine = true;
        for (String line : lines) {
            // Заменяем плейсхолдеры
            String processed = line.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
            
            // Добавляем префикс к первой непустой строке
            Component formatted;
            if (isFirstLine && !line.trim().isEmpty()) {
                formatted = ChatFormatter.parse(prefix + " " + processed);
                isFirstLine = false;
            } else {
                formatted = ChatFormatter.parse(processed);
            }
            
            for (var player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(formatted);
            }
        }
    }

    public void reload() {
        stop();
        loadMessages();
        currentIndex.set(0);
        start();
    }
}
