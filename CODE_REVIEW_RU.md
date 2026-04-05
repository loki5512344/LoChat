# 🔍 Code Review: Проблемы архитектуры (SOLID, DRY, KISS)

## 🔴 Критические проблемы

### 1. DRY: Дублирование кода в конфигах
**Файлы:** `AppearanceConfig.java`, `MessagesConfig.java`, `FiltersConfig.java`

**Проблема:**
- Каждый конфиг содержит 30-50 геттеров с одинаковой структурой
- ~300 строк повторяющегося кода
- При изменении логики нужно править в 3 местах

**Решение:**
```java
// Создать базовый класс с generic методами
public abstract class BaseConfig {
    protected String getString(String path, String def) { ... }
    protected int getInt(String path, int def) { ... }
    protected boolean getBoolean(String path, boolean def) { ... }
}

// Использовать в конфигах
public class MessagesConfig extends BaseConfig {
    public String getNoPermission() {
        return getString("general.no_permission", "&#CF6679Нет прав!");
    }
}
```

---

### 2. SRP: ConfigManager делает слишком много
**Файл:** `ConfigManager.java`

**Проблема:**
- Управляет 5 конфигами
- Содержит логику обновления версий
- Имеет 30+ геттеров для настроек
- Нарушает Single Responsibility Principle

**Решение:**
```java
// Разделить на отдельные классы
class ConfigManager {
    private final AppearanceConfig appearance;
    private final MessagesConfig messages;
    // Только управление конфигами
}

class ConfigUpdater {
    // Логика обновления версий
    void updateConfig(FileConfiguration config) { ... }
}
```

---

### 3. KISS: ColorConverter неэффективен
**Файл:** `ColorConverter.java`

**Проблема:**
```java
private static String convertAmpersandColors(String message) {
    return message
        .replaceAll("&0", "<black>")
        .replaceAll("&1", "<dark_blue>")
        // ... 16 вызовов replaceAll
}
```
- Каждый `replaceAll` компилирует regex
- O(n * 16) сложность
- Дублирование для `§` и `&`

**Решение:**
```java
private static final Map<String, String> COLOR_MAP = Map.ofEntries(
    Map.entry("&0", "<black>"),
    Map.entry("§0", "<black>"),
    Map.entry("&1", "<dark_blue>"),
    Map.entry("§1", "<dark_blue>")
    // ...
);

private static String convertColors(String message) {
    for (Map.Entry<String, String> entry : COLOR_MAP.entrySet()) {
        message = message.replace(entry.getKey(), entry.getValue());
    }
    return message;
}
```

---

### 4. DRY: Дублирование проверок permissions
**Файл:** `AdvancedMessageFilter.java`

**Проблема:**
```java
if (!player.hasPermission("lochat.bypass.swear") && !player.hasPermission("lochat.bypass.filter"))
if (!player.hasPermission("lochat.bypass.flood") && !player.hasPermission("lochat.bypass.filter"))
if (!player.hasPermission("lochat.bypass.spam") && !player.hasPermission("lochat.bypass.filter"))
```

**Решение:**
```java
private boolean canBypassFilter(Player player, String filterType) {
    return player.hasPermission("lochat.bypass." + filterType) 
        || player.hasPermission("lochat.bypass.filter");
}

// Использование
if (!canBypassFilter(player, "swear")) {
    FilterResult swear = swearFilter.filter(player, message);
    // ...
}
```

---

### 5. OCP: EnhancedChatRenderer не расширяем
**Файл:** `EnhancedChatRenderer.java`

**Проблема:**
```java
switch (placeholder) {
    case "emoji" -> result = result.append(emoji);
    case "prefix" -> result = result.append(prefix);
    // Добавление нового плейсхолдера требует изменения кода
}
```

**Решение:**
```java
// Strategy pattern для плейсхолдеров
interface PlaceholderResolver {
    Component resolve(Player player, boolean isGlobal);
}

Map<String, PlaceholderResolver> resolvers = Map.of(
    "emoji", (p, g) -> buildEmojiComponent(g),
    "prefix", (p, g) -> buildPrefix(g),
    "player", (p, g) -> buildPlayerComponent(p)
);

// Использование
Component resolved = resolvers.getOrDefault(placeholder, 
    (p, g) -> Component.text("{" + placeholder + "}"))
    .resolve(player, isGlobal);
```

---

### 6. ISP: MuteServiceImpl не реализован полностью
**Файл:** `MuteServiceImpl.java`

**Проблема:**
```java
@Override
public void save() {
    // TODO: Implement save to file
}

private void load() {
    // TODO: Implement load from file
}
```

**Решение:**
- Либо реализовать методы
- Либо разделить интерфейс на `MuteService` и `PersistentMuteService`

---

### 7. Отсутствует зависимость VoiceChat
**Файл:** `MuteServiceImpl.java`

**Проблема:**
```java
import de.maxhenkel.voicechat.api.BukkitVoicechatService; // ❌ Не найден
```

**Решение:**
```gradle
// build.gradle.kts
dependencies {
    compileOnly("de.maxhenkel.voicechat:voicechat-api:2.5.0")
}
```

Или сделать опциональной зависимостью:
```java
private void initVoiceChat() {
    try {
        Class.forName("de.maxhenkel.voicechat.api.BukkitVoicechatService");
        // Инициализация
    } catch (ClassNotFoundException e) {
        plugin.getLogger().info("VoiceChat not found, voice mute disabled");
    }
}
```

---

### 8. SRP: ChatEventListener слишком сложный
**Файл:** `ChatEventListener.java`

**Проблема:**
- Метод `onChat` содержит 80+ строк
- Обрабатывает: режим чата, фильтрацию, радиус, Discord, статистику
- Нарушает Single Responsibility

**Решение:**
```java
class ChatEventListener {
    private final ChatModeHandler modeHandler;
    private final MessageFilterHandler filterHandler;
    private final LocalChatHandler localHandler;
    private final DiscordHandler discordHandler;
    private final StatsHandler statsHandler;
    
    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        String message = extractMessage(event);
        
        // Делегируем обработку
        ChatMode mode = modeHandler.determineMode(message);
        if (!filterHandler.filter(sender, message)) {
            event.setCancelled(true);
            return;
        }
        
        if (mode == ChatMode.LOCAL) {
            localHandler.handleLocal(event, sender);
        }
        
        discordHandler.sendToDiscord(sender, message, mode);
        statsHandler.recordMessage(sender, mode);
    }
}
```

---

### 9. DRY: Дублирование конвертации цветов
**Файл:** `ColorConverter.java`

**Проблема:**
```java
private static String convertSectionColors(String message) { ... }
private static String convertAmpersandColors(String message) { ... }
```
Идентичная логика, только разные символы.

**Решение:**
```java
private static String convertColors(String message, char symbol) {
    return message
        .replaceAll(symbol + "0", "<black>")
        .replaceAll(symbol + "1", "<dark_blue>")
        // ...
}

public static String convertLegacyFormats(String message) {
    message = convertColors(message, '§');
    message = convertColors(message, '&');
    return message;
}
```

---

### 10. DIP: Прямая зависимость от LoChat
**Файлы:** `EnhancedChatRenderer.java`, `ChatEventListener.java`, и др.

**Проблема:**
```java
com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
```
Нарушает Dependency Inversion Principle.

**Решение:**
```java
// Создать интерфейс
interface ChatPlugin {
    ConfigManager getConfigManager();
    GradientModule getGradientModule();
    DiscordIntegration getDiscordIntegration();
}

// LoChat реализует интерфейс
class LoChat extends JavaPlugin implements ChatPlugin { ... }

// Использование
class EnhancedChatRenderer {
    private final ChatPlugin plugin;
    
    public EnhancedChatRenderer(ChatPlugin plugin, boolean isGlobal) {
        this.plugin = plugin;
        // Нет каста
    }
}
```

---

## 📊 Статистика проблем

| Принцип | Нарушений | Критичность |
|---------|-----------|-------------|
| DRY     | 4         | 🔴 Высокая  |
| SRP     | 3         | 🔴 Высокая  |
| KISS    | 2         | 🟡 Средняя  |
| OCP     | 1         | 🟡 Средняя  |
| DIP     | 1         | 🟡 Средняя  |
| ISP     | 1         | 🟢 Низкая   |

---

## ✅ Что сделано хорошо

1. **ServiceRegistry** - правильная реализация DI
2. **Разделение на модули** - gradient, integrations, core
3. **Использование интерфейсов** - api.service.*
4. **PluginInitializer/PluginShutdown** - разделение логики инициализации
5. **BaseConfig** - базовый класс для конфигов (уже есть!)

---

## 🎯 Приоритеты исправления

### Высокий приоритет
1. Исправить отсутствующую зависимость VoiceChat
2. Упростить ColorConverter (KISS)
3. Вынести проверки permissions в отдельный метод (DRY)

### Средний приоритет
4. Разделить ChatEventListener на handler'ы (SRP)
5. Убрать касты к LoChat через интерфейс (DIP)
6. Сделать EnhancedChatRenderer расширяемым (OCP)

### Низкий приоритет
7. Реализовать save/load в MuteServiceImpl (ISP)
8. Упростить ConfigManager (SRP)
9. Объединить методы конвертации цветов (DRY)

---

## 📝 Итого

Проект имеет хорошую базовую архитектуру (ServiceRegistry, модули, интерфейсы), но есть проблемы с:
- **Дублированием кода** (DRY) - особенно в конфигах и фильтрах
- **Слишком большими классами** (SRP) - ConfigManager, ChatEventListener
- **Неэффективными алгоритмами** (KISS) - ColorConverter

Рекомендую начать с исправления критических проблем (VoiceChat, ColorConverter, permissions), затем постепенно рефакторить остальное.
