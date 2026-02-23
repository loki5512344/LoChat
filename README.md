# 🎮 Loki Network Plugins

Набор плагинов для Minecraft серверов: чат, хаб и прокси.

## 📦 Модули

### 1. LoChat - Чат плагин (Paper/Folia)
Продвинутый чат с градиентными никами, модерацией и фильтрами.

**Основные функции:**
- Глобальный/локальный чат
- PM система с игнором
- Мут система с историей
- Фильтры (мат, спам, CAPS, URL, IP)
- Градиентные ники (LuckPerms)
- Markdown форматирование
- Упоминания (@player, @everyone, @here, @role)
- 300+ эмодзи

### 2. LoHub - Hub плагин (Paper/Folia)
Плагин для лобби сервера с защитой мира и интерактивными элементами.

**Основные функции:**
- Spawn система с телепортом
- WorldGuard регионы (авто-создание)
- Защита мира (блоки, PvP, мобы)
- Launchpad и Double Jump
- Player Hider
- Custom Join Items
- Action система для гибкой настройки

### 3. LoVelocity - Velocity плагин
Простой плагин для прокси с командой /hub.

**Основные функции:**
- Команда /hub для телепорта на lobby
- Кастомные сообщения
- PlaceholderAPI поддержка

## 🏗️ Структура проекта

```
LoChat/                          # Root проекта
│
├── src/main/                    # LoChat (основной модуль)
│   ├── java/com/loki/lochat/
│   │   ├── api/                 # API интерфейсы
│   │   │   ├── filter/          # Фильтры сообщений
│   │   │   └── service/         # Сервисы (Chat, Mute, PM и т.д.)
│   │   ├── commands/            # Команды плагина
│   │   ├── config/              # Конфигурация
│   │   ├── core/                # Реализации
│   │   │   ├── filter/          # Реализации фильтров
│   │   │   ├── registry/        # ServiceRegistry (DI)
│   │   │   └── service/         # Реализации сервисов
│   │   ├── gradient/            # Градиентные ники
│   │   │   ├── commands/        # Команды градиентов
│   │   │   ├── config/          # Конфиг градиентов
│   │   │   ├── hook/            # LuckPerms интеграция
│   │   │   └── util/            # Утилиты градиентов
│   │   ├── listener/            # Слушатели событий
│   │   ├── util/                # Утилиты (Folia)
│   │   ├── utils/               # Утилиты (Formatter, Mention)
│   │   └── LoChat.java          # Главный класс
│   └── resources/               # Конфиги
│       ├── config.yml           # Основной конфиг
│       ├── messages.yml         # Сообщения
│       ├── chat.yml             # Настройки чата
│       ├── gradient-config.yml  # Конфиг градиентов
│       ├── gradient-messages.yml
│       ├── custom-commands.yml
│       ├── automessages.yml
│       └── plugin.yml
│
├── lohub/                       # LoHub модуль
│   ├── src/main/
│   │   ├── java/com/loki/lohub/
│   │   │   ├── actions/         # Action система
│   │   │   │   ├── impl/        # Реализации действий
│   │   │   │   ├── Action.java
│   │   │   │   └── ActionType.java
│   │   │   ├── commands/        # Команды
│   │   │   │   ├── HubCommand.java
│   │   │   │   ├── SetHubCommand.java
│   │   │   │   └── LoHubCommand.java
│   │   │   ├── config/          # Конфигурация
│   │   │   │   └── ConfigManager.java
│   │   │   ├── listeners/       # Слушатели
│   │   │   │   ├── HubProtectionListener.java
│   │   │   │   ├── PlayerJoinListener.java
│   │   │   │   ├── LaunchpadListener.java
│   │   │   │   └── DoubleJumpListener.java
│   │   │   ├── managers/        # Менеджеры
│   │   │   │   ├── SpawnManager.java
│   │   │   │   ├── RegionManager.java
│   │   │   │   ├── ActionManager.java
│   │   │   │   ├── CooldownManager.java
│   │   │   │   ├── HotbarManager.java
│   │   │   │   └── PlayerHiderManager.java
│   │   │   ├── utils/           # Утилиты
│   │   │   │   ├── ItemBuilder.java
│   │   │   │   ├── TextUtil.java
│   │   │   │   └── PlaceholderUtil.java
│   │   │   └── LoHub.java       # Главный класс
│   │   └── resources/           # Конфиги
│   │       ├── config.yml
│   │       └── plugin.yml
│   └── build.gradle.kts         # Gradle конфиг
│
├── lovelocity/                  # LoVelocity модуль
│   ├── src/main/
│   │   ├── java/com/loki/lovelocity/
│   │   │   ├── commands/
│   │   │   │   └── HubCommand.java
│   │   │   └── LoVelocity.java
│   │   └── resources/
│   │       └── velocity-plugin.json
│   └── build.gradle.kts
│
├── build.gradle.kts             # Root Gradle (Kotlin DSL)
├── settings.gradle.kts          # Настройки проектов
├── gradle.properties            # Gradle свойства
├── TODO.md                      # Список задач
└── README.md                    # Этот файл
```

## 🚀 Сборка

```bash
# Собрать все модули
./gradlew build

# Собрать только LoChat
./gradlew :build

# Собрать только LoHub
./gradlew :lohub:build

# Собрать только LoVelocity
./gradlew :lovelocity:build

# Без тестов
./gradlew build -x test
```

## 📦 Артефакты

После сборки jar файлы находятся в:
- `build/libs/LoChat-1.5.5-1.20.1.jar`
- `lohub/build/libs/LoHub-1.5.5-1.20.1.jar`
- `lovelocity/build/libs/LoVelocity-1.5.5.jar`

## 🔧 Требования

- **Java 21+**
- **Paper 1.20.1+** (для LoChat и LoHub)
- **Velocity 3.3.0+** (для LoVelocity)
- **Gradle 9.1+**

## 📝 Зависимости

### LoChat
- PlaceholderAPI (опционально)
- LuckPerms (опционально)
- PlayerPoints (опционально)

### LoHub
- WorldGuard 7.0.9+ (опционально)
- PlaceholderAPI (опционально)

### LoVelocity
- Нет зависимостей

## 🎯 Статус разработки

- ✅ **LoChat** - Готов к использованию
- 🔄 **LoHub** - В разработке (21% готово)
- ✅ **LoVelocity** - Готов к использованию

Подробности в [TODO.md](TODO.md)

## 📄 Лицензия

Proprietary - Все права защищены

## 👥 Авторы

Loki Development Team
