# 🔧 Рефакторинг сервисов - уменьшение количества файлов

## Текущая ситуация
- **11 интерфейсов** в `api/service/`
- **12 реализаций** в `core/service/`
- **23 файла** только для сервисов
- Некоторые сервисы по 30 строк кода

## Проблема
Слишком много мелких файлов, которые можно объединить по функциональности.

---

## 🎯 План рефакторинга

### 1. Объединить простые сервисы в один

**Было:**
```
PMService.java (28 строк)
SpyService.java (69 строк)
IgnoreService.java (97 строк)
```

**Станет:**
```java
// MessagingService.java - всё что связано с общением
public interface MessagingService {
    // PM
    void setLastConversation(UUID sender, UUID receiver);
    UUID getLastConversation(UUID player);
    
    // Spy
    boolean toggleSpy(UUID player);
    boolean isSpying(UUID player);
    void broadcastPM(Player sender, Player receiver, String message);
    
    // Ignore
    void addIgnore(UUID player, UUID target);
    void removeIgnore(UUID player, UUID target);
    boolean isIgnoring(UUID player, UUID target);
    Set<UUID> getIgnoreList(UUID player);
}
```

**Экономия:** 3 интерфейса + 3 реализации = **6 файлов → 2 файла**

---

### 2. Объединить Mute + Punishment

**Было:**
```
MuteService.java + MuteServiceImpl.java (212 строк)
PunishmentService.java + PunishmentServiceImpl.java (173 строки)
MuteHistoryManager.java (77 строк)
```

**Станет:**
```java
// ModerationService.java - всё что связано с модерацией
public interface ModerationService {
    // Mutes
    void mute(UUID uuid, String name, long duration, String reason, String by);
    boolean unmute(UUID uuid, String by);
    boolean isMuted(UUID uuid);
    MuteData getMuteData(UUID uuid);
    
    // Bans
    void ban(UUID uuid, String name, long duration, String reason, String by);
    boolean unban(UUID uuid, String by);
    boolean isBanned(UUID uuid);
    BanData getBanData(UUID uuid);
    
    // Warns
    void warn(UUID uuid, String name, String reason, String by);
    List<WarnData> getWarns(UUID uuid);
    
    // History
    List<ModerationEntry> getHistory(UUID uuid);
    List<ModerationEntry> getHistoryByModerator(String name);
}
```

**Экономия:** 3 интерфейса + 3 реализации = **6 файлов → 2 файла**

---

### 3. Объединить Cooldown + PlayerData

**Было:**
```
CooldownService.java + CooldownServiceImpl.java (40 строк)
PlayerDataService.java + PlayerDataServiceImpl.java (102 строки)
```

**Станет:**
```java
// PlayerService.java - всё что связано с данными игрока
public interface PlayerService {
    // Cooldowns
    boolean hasCooldown(UUID player, String type);
    void setCooldown(UUID player, String type, long duration);
    long getRemainingCooldown(UUID player, String type);
    void clearCooldowns(UUID player);
    
    // Statistics
    void recordMessage(UUID player, String chatType);
    PlayerStats getStats(UUID player);
    void saveAll();
}
```

**Экономия:** 2 интерфейса + 2 реализации = **4 файла → 2 файла**

---

### 4. Оставить как есть (нормальный размер)

```
ChatService.java + ChatServiceImpl.java (114 строк) ✅
MessageService.java + MessageServiceImpl.java (36 строк) ✅
MentionService.java + MentionServiceImpl.java (104 строки) ✅
NickService.java + NickServiceImpl.java (155 строк) ✅
```

Эти сервисы имеют чёткую ответственность и нормальный размер.

---

## 📊 Результат рефакторинга

| До | После | Экономия |
|----|-------|----------|
| 11 интерфейсов | 7 интерфейсов | -4 |
| 12 реализаций | 8 реализаций | -4 |
| **23 файла** | **15 файлов** | **-8 файлов** |

---

## 🔨 Пошаговая реализация

### Шаг 1: Создать MessagingService

```java
// api/service/MessagingService.java
public interface MessagingService {
    // PM
    void setLastConversation(UUID sender, UUID receiver);
    UUID getLastConversation(UUID player);
    
    // Spy
    boolean toggleSpy(UUID player);
    boolean isSpying(UUID player);
    void broadcastPM(Player sender, Player receiver, String message);
    void sendToSpies(Player sender, Component message, boolean isGlobal);
    void removeSpy(UUID player);
    
    // Ignore
    void addIgnore(UUID player, UUID target);
    void removeIgnore(UUID player, UUID target);
    boolean isIgnoring(UUID player, UUID target);
    Set<UUID> getIgnoreList(UUID player);
    void save();
}

// core/service/MessagingServiceImpl.java
public class MessagingServiceImpl implements MessagingService {
    private final JavaPlugin plugin;
    private final MessageConfig messageConfig;
    
    // PM state
    private final Map<UUID, UUID> lastConversation = new ConcurrentHashMap<>();
    
    // Spy state
    private final Set<UUID> spyEnabled = ConcurrentHashMap.newKeySet();
    
    // Ignore state
    private final Map<UUID, Set<UUID>> ignoreMap = new ConcurrentHashMap<>();
    
    public MessagingServiceImpl(JavaPlugin plugin, MessageConfig messageConfig) {
        this.plugin = plugin;
        this.messageConfig = messageConfig;
        loadIgnoreData();
    }
    
    // Реализация всех методов...
}
```

### Шаг 2: Обновить ServiceRegistry

```java
// Было
register(PMService.class, new PMServiceImpl());
register(SpyService.class, new SpyServiceImpl(messageConfig));
register(IgnoreService.class, new IgnoreServiceImpl(plugin));

// Стало
MessagingService messaging = new MessagingServiceImpl(plugin, messageConfig);
register(MessagingService.class, messaging);

// Для обратной совместимости (опционально)
register(PMService.class, messaging);
register(SpyService.class, messaging);
register(IgnoreService.class, messaging);
```

### Шаг 3: Удалить старые файлы

```bash
# Удалить интерфейсы
rm src/main/java/com/loki/lochat/api/service/PMService.java
rm src/main/java/com/loki/lochat/api/service/SpyService.java
rm src/main/java/com/loki/lochat/api/service/IgnoreService.java

# Удалить реализации
rm src/main/java/com/loki/lochat/core/service/PMServiceImpl.java
rm src/main/java/com/loki/lochat/core/service/SpyServiceImpl.java
rm src/main/java/com/loki/lochat/core/service/IgnoreServiceImpl.java
```

---

## ⚠️ Альтернативный подход (если не хочешь ломать API)

Можно оставить старые интерфейсы, но сделать их делегатами:

```java
// Старый интерфейс остаётся
public interface PMService {
    void setLastConversation(UUID sender, UUID receiver);
    UUID getLastConversation(UUID player);
}

// Но реализация делегирует в MessagingService
public class PMServiceAdapter implements PMService {
    private final MessagingService messaging;
    
    public PMServiceAdapter(MessagingService messaging) {
        this.messaging = messaging;
    }
    
    @Override
    public void setLastConversation(UUID sender, UUID receiver) {
        messaging.setLastConversation(sender, receiver);
    }
    
    @Override
    public UUID getLastConversation(UUID player) {
        return messaging.getLastConversation(player);
    }
}
```

Это позволит:
- Сохранить обратную совместимость
- Уменьшить количество реализаций (3 → 1)
- Постепенно мигрировать код на новый API

---

## 🎯 Рекомендация

**Начни с MessagingService** - это самое простое объединение:
1. PM, Spy, Ignore логически связаны (общение между игроками)
2. Все три сервиса маленькие (28-97 строк)
3. Объединение даст ~200 строк - нормальный размер

**Потом ModerationService** - это сложнее:
1. Mute + Punishment + History = ~460 строк
2. Нужно будет рефакторить внутреннюю структуру
3. Но логически это одна область (модерация)

**PlayerService оставь на потом** - там всё норм.

---

## 📝 Итого

Объединение сервисов:
- ✅ Уменьшит количество файлов с 23 до 15
- ✅ Упростит навигацию по коду
- ✅ Сгруппирует связанную функциональность
- ⚠️ Потребует обновления кода, который использует старые сервисы

Начни с MessagingService - это быстро и безопасно.
