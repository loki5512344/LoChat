# 🚀 Loki Plugins - TODO

## 📦 Проекты

### 1. LoChat - Чат плагин (Paper/Folia)
**Статус:** ✅ Готов к использованию

#### Функции
- ✅ Глобальный/локальный чат
- ✅ PM система с игнором
- ✅ Мут система с историей
- ✅ Фильтры (мат, спам, CAPS)
- ✅ Градиентные ники (LuckPerms интеграция)
- ✅ Смайлики (300+ эмодзи)
- ✅ PlaceholderAPI
- ✅ Markdown форматирование (**bold**, *italic*, ~~strike~~, `code`)
- ✅ Упоминания (@player, @everyone, @here, @role:permission)
- ✅ Фильтры URL/IP/повторов

---

### 2. LoHub - Hub плагин (Paper/Folia)
**Статус:** 🔄 В разработке (21% готово)

#### ✅ Готово
- ✅ Spawn система с телепортом
- ✅ WorldGuard регионы (авто-создание)
- ✅ Защита мира (блоки, PvP, мобы)
- ✅ Команды /hub, /sethub, /lohub
- ✅ Базовые утилиты (ItemBuilder, TextUtil, PlaceholderUtil)

#### 🔄 В работе - ЭТАП 2: Action система
- [ ] Action интерфейс и ActionType enum
- [ ] MessageAction - отправка сообщений
- [ ] SoundAction - звуки
- [ ] TitleAction - title/subtitle
- [ ] CommandAction - команды от игрока
- [ ] ConsoleAction - команды от консоли
- [ ] GamemodeAction - смена режима
- [ ] EffectAction - эффекты
- [ ] ActionManager - парсинг и выполнение

#### 📋 Запланировано
**ЭТАП 3: Join система**
- [ ] PlayerJoinListener
- [ ] Телепорт на спавн при входе
- [ ] Хил и очистка эффектов
- [ ] Кастомные сообщения входа/выхода
- [ ] Интеграция с Action системой

**ЭТАП 4: Custom Join Items**
- [ ] HotbarManager
- [ ] Выдача предметов при входе
- [ ] Обработка кликов (actions)
- [ ] Защита от выбрасывания

**ЭТАП 5: Launchpad**
- [ ] LaunchpadListener
- [ ] Прыжковые площадки (плита + редстоун)

**ЭТАП 6: Double Jump**
- [ ] DoubleJumpListener
- [ ] CooldownManager
- [ ] Двойной прыжок в воздухе

**ЭТАП 7: Player Hider**
- [ ] PlayerHiderManager
- [ ] Скрытие/показ игроков
- [ ] Предмет для переключения

**ЭТАП 8-10: Опционально**
- [ ] Scoreboard модуль
- [ ] Tablist модуль
- [ ] Announcements модуль

---

### 3. LoVelocity - Velocity плагин
**Статус:** ✅ Готов

#### Функции
- ✅ Команда /hub для телепорта на lobby/hub сервер
- ✅ Кастомные сообщения
- ✅ Поддержка PlaceholderAPI

---

## 🏗️ Структура проекта

```
LoChat/                          # Root проекта
├── src/main/                    # LoChat (основной модуль)
│   ├── java/com/loki/lochat/
│   │   ├── api/                 # API интерфейсы
│   │   ├── commands/            # Команды
│   │   ├── config/              # Конфигурация
│   │   ├── core/                # Реализации
│   │   ├── gradient/            # Градиентные ники
│   │   ├── listener/            # Слушатели событий
│   │   ├── utils/               # Утилиты
│   │   └── LoChat.java          # Главный класс
│   └── resources/               # Конфиги
│
├── lohub/                       # LoHub модуль
│   ├── src/main/
│   │   ├── java/com/loki/lohub/
│   │   │   ├── actions/         # Action система
│   │   │   ├── commands/        # Команды
│   │   │   ├── config/          # Конфигурация
│   │   │   ├── listeners/       # Слушатели
│   │   │   ├── managers/        # Менеджеры
│   │   │   ├── utils/           # Утилиты
│   │   │   └── LoHub.java       # Главный класс
│   │   └── resources/           # Конфиги
│   └── build.gradle.kts         # Gradle конфиг
│
├── lovelocity/                  # LoVelocity модуль
│   ├── src/main/
│   │   └── java/com/loki/lovelocity/
│   └── build.gradle.kts         # Gradle конфиг
│
├── build.gradle.kts             # Root Gradle (Kotlin DSL)
├── settings.gradle.kts          # Настройки проектов
└── TODO.md                      # Этот файл
```

---

## 🎯 Текущий фокус

**LoHub - ЭТАП 2: Action система**

Создание гибкой системы действий для настройки поведения через конфиг:

```yaml
actions:
  - "[MESSAGE] &aПривет, %player%!"
  - "[SOUND] ENTITY_PLAYER_LEVELUP"
  - "[TITLE] &b&lДобро пожаловать;&fНа сервер;1;3;1"
  - "[COMMAND] spawn"
  - "[CONSOLE] give %player% diamond 1"
  - "[GAMEMODE] survival"
  - "[EFFECT] SPEED;1"
```

---

## 📝 Правила разработки

1. **Никакого русского текста в Java файлах** - только в конфигах
2. **Цвета только в формате `&#RRGGBB`** - унифицированный стиль
3. **SOLID принципы** - чистая архитектура
4. **Классы до 100-150 строк** - читаемость кода
5. **Gradle Kotlin DSL** - современный подход
6. **Без лишних MD файлов** - только TODO.md и docs/

---

## 🔧 Технологии

- **Java 17+**
- **Paper API 1.20.1**
- **Velocity API 3.3.0**
- **Gradle 9.1+ (Kotlin DSL)**
- **WorldGuard 7.0.9**
- **PlaceholderAPI 2.11.6**
- **LuckPerms API**
