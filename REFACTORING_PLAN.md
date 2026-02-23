# План рефакторинга LoChat

## Цель
Убрать legacy код, перевести всё на новую архитектуру (SOLID + DI через ServiceRegistry)

---

## Текущая структура

### ✅ Новая архитектура (уже готово)
```
src/main/java/com/loki/lochat/
├── api/                    # Интерфейсы сервисов
│   ├── service/
│   │   ├── ChatService.java
│   │   ├── MuteService.java
│   │   ├── AntiSpamService.java
│   │   ├── CooldownService.java
│   │   ├── MessageService.java
│   │   └── PlayerDataService.java
│   └── filter/
│       └── MessageFilter.java
├── core/                   # Реализации
│   ├── service/
│   │   ├── ChatServiceImpl.java
│   │   ├── MuteServiceImpl.java
│   │   ├── AntiSpamServiceImpl.java
│   │   ├── CooldownServiceImpl.java
│   │   ├── MessageServiceImpl.java
│   │   └── PlayerDataServiceImpl.java
│   ├── filter/
│   │   ├── MuteFilter.java
│   │   ├── CooldownFilter.java
│   │   ├── SpamFilter.java
│   │   └── WordFilter.java
│   └── registry/
│       └── ServiceRegistry.java
└── listener/               # Новые слушатели
    ├── ChatEventListener.java (35 строк)
    └── PlayerEventListener.java (23 строки)
```

### ❌ Legacy код (нужно удалить/переписать)
```
src/main/java/com/loki/lochat/
├── managers/               # Старые менеджеры
│   ├── MuteManager.java (409 строк) ❌
│   ├── ChatManager.java (131 строк) ❌
│   ├── AntiSpamManager.java ❌
│   ├── CooldownManager.java ❌
│   ├── PMManager.java ❌
│   ├── IgnoreManager.java ❌
│   ├── SpyManager.java ❌
│   ├── MentionManager.java (123 строки) ❌
│   ├── AutoMessageManager.java (159 строк) ⚠️
│   └── CustomCommandManager.java (269 строк) ⚠️
└── listeners/
    └── ChatListener.java (185 строк) ❌
```

---

## Этап 1: Анализ зависимостей

### 1.1 Проверить что использует legacy менеджеры
```bash
# Найти все вызовы getMuteManager(), getChatManager() и т.д.
grep -r "getMuteManager\|getChatManager\|getAntiSpamManager" src/main/java/com/loki/lochat/commands/
```

### 1.2 Составить список команд для миграции
- [x] ChatEventListener - уже использует ServiceRegistry
- [ ] MuteCommand - использует MuteManager
- [ ] UnmuteCommand - использует MuteManager
- [ ] MuteListCommand - использует MuteManager
- [ ] MuteHistoryCommand - использует MuteManager
- [ ] MuteBlameCommand - использует MuteManager
- [ ] GlobalChatCommand - использует ChatManager
- [ ] LocalChatCommand - использует ChatManager
- [ ] MsgCommand - использует PMManager, IgnoreManager
- [ ] ReplyCommand - использует PMManager, IgnoreManager
- [ ] IgnoreCommand - использует IgnoreManager
- [ ] UnignoreCommand - использует IgnoreManager
- [ ] ChatSpyCommand - использует SpyManager

---

## Этап 2: Создание новых сервисов

### 2.1 PMService (личные сообщения)
```java
// api/service/PMService.java
public interface PMService {
    void sendPrivateMessage(Player sender, Player receiver, String message);
    Optional<UUID> getLastConversation(UUID player);
    void setLastConversation(UUID player, UUID target);
}

// core/service/PMServiceImpl.java
public class PMServiceImpl implements PMService {
    private final Map<UUID, UUID> conversations = new ConcurrentHashMap<>();
    // ...
}
```

### 2.2 IgnoreService (игнорирование)
```java
// api/service/IgnoreService.java
public interface IgnoreService {
    boolean isIgnoring(UUID player, UUID target);
    void addIgnore(UUID player, UUID target);
    void removeIgnore(UUID player, UUID target);
    int getIgnoredCount(UUID player);
}

// core/service/IgnoreServiceImpl.java
public class IgnoreServiceImpl implements IgnoreService {
    private final Map<UUID, Set<UUID>> ignoreMap = new ConcurrentHashMap<>();
    // ...
}
```

### 2.3 SpyService (шпионаж за чатом)
```java
// api/service/SpyService.java
public interface SpyService {
    boolean toggleSpy(UUID player);
    boolean isSpying(UUID player);
    void sendToSpies(Player sender, Object message, boolean isGlobal);
}

// core/service/SpyServiceImpl.java
public class SpyServiceImpl implements SpyService {
    private final Set<UUID> spies = ConcurrentHashMap.newKeySet();
    // ...
}
```

### 2.4 MentionService (упоминания)
```java
// api/service/MentionService.java
public interface MentionService {
    String processMentions(String message, Set<Player> mentioned);
    void notifyMentioned(Set<Player> players);
}

// core/service/MentionServiceImpl.java
public class MentionServiceImpl implements MentionService {
    // ...
}
```

---

## Этап 3: Миграция команд

### 3.1 Mute команды
**Приоритет: ВЫСОКИЙ** (используют MuteManager 409 строк)

```java
// Было:
public class MuteCommand {
    private final LoChat plugin;
    // plugin.getMuteManager().mute(...)
}

// Станет:
public class MuteCommand {
    private final MuteService muteService;
    
    public MuteCommand(LoChat plugin) {
        this.muteService = plugin.getServiceRegistry().get(MuteService.class);
    }
}
```

**Файлы для миграции:**
- [ ] MuteCommand.java
- [ ] UnmuteCommand.java
- [ ] MuteListCommand.java
- [ ] MuteHistoryCommand.java
- [ ] MuteBlameCommand.java

### 3.2 Chat команды
**Приоритет: ВЫСОКИЙ**

```java
// Было:
plugin.getChatManager().sendGlobalMessage(...)

// Станет:
ChatService chatService = plugin.getServiceRegistry().get(ChatService.class);
chatService.sendGlobalMessage(...)
```

**Файлы для миграции:**
- [ ] GlobalChatCommand.java
- [ ] LocalChatCommand.java

### 3.3 PM команды
**Приоритет: СРЕДНИЙ**

**Файлы для миграции:**
- [ ] MsgCommand.java
- [ ] ReplyCommand.java

### 3.4 Ignore команды
**Приоритет: СРЕДНИЙ**

**Файлы для миграции:**
- [ ] IgnoreCommand.java
- [ ] UnignoreCommand.java

### 3.5 Spy команды
**Приоритет: НИЗКИЙ**

**Файлы для миграции:**
- [ ] ChatSpyCommand.java

---

## Этап 4: Удаление legacy кода

### 4.1 Удалить старые менеджеры
После миграции всех команд:
```bash
rm src/main/java/com/loki/lochat/managers/MuteManager.java
rm src/main/java/com/loki/lochat/managers/ChatManager.java
rm src/main/java/com/loki/lochat/managers/AntiSpamManager.java
rm src/main/java/com/loki/lochat/managers/CooldownManager.java
rm src/main/java/com/loki/lochat/managers/PMManager.java
rm src/main/java/com/loki/lochat/managers/IgnoreManager.java
rm src/main/java/com/loki/lochat/managers/SpyManager.java
rm src/main/java/com/loki/lochat/managers/MentionManager.java
```

### 4.2 Удалить старый ChatListener
```bash
rm src/main/java/com/loki/lochat/listeners/ChatListener.java
```

### 4.3 Очистить LoChat.java
Удалить все legacy геттеры:
```java
// Удалить:
public PMManager getPmManager() { return pmManager; }
public IgnoreManager getIgnoreManager() { return ignoreManager; }
public SpyManager getSpyManager() { return spyManager; }
public MuteManager getMuteManager() { return muteManager; }
public ChatManager getChatManager() { return chatManager; }
public AntiSpamManager getAntiSpamManager() { return antiSpamManager; }
public CooldownManager getCooldownManager() { return cooldownManager; }
public MentionManager getMentionManager() { return mentionManager; }
```

---

## Этап 5: Оптимизация больших классов

### 5.1 ConfigManager (270 строк)
**Проблема:** Слишком много методов

**Решение:** Разбить на категории
```java
// config/ChatConfig.java - настройки чата
// config/MuteConfig.java - настройки мутов
// config/SpamConfig.java - настройки антиспама
```

### 5.2 CustomCommandManager (269 строк)
**Проблема:** Много логики в одном классе

**Решение:** Вынести парсинг команд
```java
// commands/custom/CustomCommandParser.java
// commands/custom/CustomCommandExecutor.java
```

### 5.3 GradientModule (249 строк)
**Статус:** Оставить как есть (это отдельный модуль)

### 5.4 AutoMessageManager (159 строк)
**Статус:** Оставить как есть (нормальный размер)

---

## Этап 6: Тестирование

### 6.1 Написать тесты для новых сервисов
```java
// test/java/com/loki/lochat/core/service/
├── PMServiceImplTest.java
├── IgnoreServiceImplTest.java
├── SpyServiceImplTest.java
└── MentionServiceImplTest.java
```

### 6.2 Интеграционные тесты
```java
// test/java/com/loki/lochat/integration/
└── ChatFlowTest.java - тест полного флоу отправки сообщения
```

---

## Этап 7: Финальная проверка

### 7.1 Проверить размеры классов
```bash
# Все классы должны быть < 150 строк (кроме модулей)
find src/main/java -name "*.java" -exec wc -l {} \; | sort -rn | head -20
```

### 7.2 Проверить архитектуру
- [ ] Все сервисы через интерфейсы
- [ ] Все зависимости через ServiceRegistry
- [ ] Нет прямых вызовов new Service()
- [ ] Все команды используют DI

### 7.3 Проверить производительность
- [ ] Нет утечек памяти
- [ ] Нет блокирующих операций в главном потоке
- [ ] Folia совместимость

---

## Метрики успеха

### До рефакторинга
- Главный класс: 166 строк
- Legacy менеджеры: 8 классов, ~1500 строк
- Старый ChatListener: 185 строк
- Дублирование кода: высокое
- Тестовое покрытие: 52 теста

### После рефакторинга (цель)
- Главный класс: <100 строк
- Новые сервисы: 8 интерфейсов + 8 реализаций, ~800 строк
- Новые слушатели: 2 класса, ~60 строк
- Дублирование кода: минимальное
- Тестовое покрытие: >80 тестов
- Все классы: <150 строк

---

## Порядок выполнения

1. ✅ Создать новую архитектуру (api + core + registry)
2. ✅ Мигрировать ChatEventListener
3. ✅ Удалить эмодзи
4. **→ Создать PMService, IgnoreService, SpyService, MentionService**
5. **→ Мигрировать все команды на новые сервисы**
6. **→ Удалить legacy менеджеры**
7. **→ Оптимизировать большие классы**
8. **→ Написать тесты**
9. **→ Финальная проверка**

---

## Примечания

- Миграция должна быть постепенной (по одной команде)
- После каждого шага - компиляция и тесты
- Сохранять обратную совместимость до полной миграции
- Документировать изменения в CHANGELOG.md
