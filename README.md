# 🎮 LoChat - Advanced Chat Plugin

Продвинутый чат плагин для Paper/Folia серверов с градиентными никами, модерацией и гибкой системой конфигурации.

## Сборка и локальный тестовый сервер

```bash
./gradlew runServer
```

Gradle задача `runServer` ([run-paper](https://github.com/jpenilla/run-paper)) скачивает Paper **1.21.4** (как `paper-api` в `build.gradle.kts`), собирает JAR плагина и кладёт его в `run/plugins`. Первый запуск долгий из‑за загрузки; остановка — `stop` в консоли сервера.

## ✨ Основные возможности

### 💬 Чат система
- **Глобальный чат** - общение между всеми игроками
- **Локальный чат** - общение в радиусе
- **Личные сообщения** - PM система с ответами
- **Игнорирование** - блокировка нежелательных игроков
- **Упоминания** - @player, @everyone, @here, @role
- **Hover/Click события** - интерактивные сообщения

### 🎨 Градиентные ники
- Кастомные градиентные цвета (до 7 цветов)
- Готовые пресеты (огонь, океан, радуга и т.д.)
- Кастомные префиксы
- Интеграция с LuckPerms
- Система покупки через PlayerPoints
- Кулдауны и ограничения

### 🛡️ Модерация
- **Мут система** с историей
- Временные и постоянные муты
- Скрытые муты (silent)
- Готовые причины мутов
- Эскалация наказаний
- Автоматические муты

### 🔍 Фильтры
- Фильтр мата (настраиваемый словарь)
- Антиспам и антифлуд
- CAPS фильтр
- URL и IP фильтр
- Фильтр рекламы
- Фильтр повторов

### 🎯 Кастомные команды
- /help, /rules, /discord - информационные
- /coords, /ping, /stats - утилиты
- /me - действия от третьего лица
- /sounds - управление звуками
- Полностью настраиваемые в конфиге

### 🔊 Звуковые эффекты
- Звуки для PM и упоминаний
- Звуки для мутов/размутов
- Звуки для градиентов
- Звуки для команд
- Персональные настройки звуков

### 📊 Статистика
- Статистика сообщений
- Топ игроков по активности
- Статистика мутов
- Статистика фильтров
- Автоматическая очистка старых данных

## 📁 Структура проекта

```
LoChat/
├── src/main/
│   ├── java/com/loki/lochat/
│   │   ├── api/                          # API интерфейсы
│   │   │   ├── filter/                   # Фильтры сообщений
│   │   │   └── service/                  # Сервисы (Chat, Mute, PM и т.д.)
│   │   ├── commands/                     # Команды плагина
│   │   │   ├── custom/                   # Кастомные команды
│   │   │   │   ├── HelpCommand.java
│   │   │   │   ├── RulesCommand.java
│   │   │   │   ├── DiscordCommand.java
│   │   │   │   ├── CoordsCommand.java
│   │   │   │   ├── PingCommand.java
│   │   │   │   ├── StatsCommand.java
│   │   │   │   ├── MeCommand.java
│   │   │   │   └── SoundsCommand.java
│   │   │   ├── CustomCommandManager.java
│   │   │   ├── MuteCommand.java
│   │   │   ├── UnmuteCommand.java
│   │   │   └── ... (другие команды)
│   │   ├── config/                       # Конфигурация
│   │   ├── core/                         # Реализации
│   │   │   ├── filter/                   # Реализации фильтров
│   │   │   │   ├── AdvancedMessageFilter.java
│   │   │   │   ├── CapsFilter.java
│   │   │   │   ├── CooldownFilter.java
│   │   │   │   └── MuteFilter.java
│   │   │   ├── registry/                 # ServiceRegistry (DI)
│   │   │   └── service/                  # Реализации сервисов
│   │   │       ├── ChatServiceImpl.java
│   │   │       ├── MuteServiceImpl.java
│   │   │       ├── PMServiceImpl.java
│   │   │       └── ... (другие сервисы)
│   │   ├── data/                         # Модели данных
│   │   │   └── model/
│   │   │       ├── ChatMessage.java
│   │   │       └── MuteData.java
│   │   ├── gradient/                     # Градиентные ники
│   │   │   ├── commands/                 # Команды градиентов
│   │   │   ├── config/                   # Конфиг градиентов
│   │   │   ├── data/                     # Данные градиентов
│   │   │   ├── gui/                      # GUI для градиентов
│   │   │   ├── hooks/                    # LuckPerms интеграция
│   │   │   ├── listeners/                # Слушатели градиентов
│   │   │   └── util/                     # Утилиты градиентов
│   │   ├── integrations/                 # Интеграции с плагинами
│   │   │   ├── PlaceholderAPIHook.java
│   │   │   ├── LibertyBansHook.java
│   │   │   └── SkinsRestorerHook.java
│   │   ├── listener/                     # Слушатели событий
│   │   ├── managers/                     # Менеджеры
│   │   ├── util/                         # Утилиты (Folia)
│   │   ├── utils/                        # Утилиты (Formatter, Mention)
│   │   └── LoChat.java                   # Главный класс
│   │
│   └── resources/                        # Ресурсы
│       ├── config/                       # Конфигурационные файлы
│       │   ├── chat-formats.yml          # Форматы чата
│       │   ├── filters.yml               # Настройки фильтров
│       │   ├── gradient-config.yml       # Конфиг градиентов
│       │   ├── automessages.yml          # Автосообщения
│       │   ├── mute-config.yml           # Система мутов
│       │   ├── sounds.yml                # Звуковые эффекты
│       │   ├── database.yml              # База данных
│       │   ├── integrations.yml          # Интеграции
│       │   └── custom-commands.yml       # Кастомные команды
│       │
│       ├── data/                         # Файлы данных
│       │   ├── data.yml                  # Основные данные
│       │   ├── players.yml               # Данные игроков
│       │   ├── gradients.yml             # Градиентные ники
│       │   ├── mutes.yml                 # Муты и история
│       │   ├── ignores.yml               # Игнорирование
│       │   ├── statistics.yml            # Статистика
│       │   └── filter-words.json         # Запрещенные слова
│       │
│       ├── config.yml                    # Основной конфиг
│       ├── messages.yml                  # Сообщения
│       └── plugin.yml                    # Конфиг плагина
│
├── build.gradle.kts                      # Gradle конфиг
└── README.md                             # Этот файл
```

## 🚀 Быстрый старт

### Установка

1. Скачайте последнюю версию из [Releases](https://codeberg.org/loki5512344/Lochat/releases)
2. Поместите `LoChat-1.5.5-1.20.1.jar` в папку `plugins/`
3. Перезапустите сервер
4. Настройте конфиги в `plugins/LoChat/`

### Первая настройка

1. **Основной конфиг** (`config.yml`):
   - Настройте радиус локального чата
   - Включите/отключите нужные функции
   - Настройте Discord и Website ссылки

2. **Сообщения** (`messages.yml`):
   - Измените сообщения на свой язык
   - Настройте цвета и форматирование

3. **Фильтры** (`config/filters.yml`):
   - Настройте фильтр мата
   - Добавьте свои запрещенные слова в `data/filter-words.json`
   - Настройте антиспам и другие фильтры

4. **Градиенты** (`config/gradient-config.yml`):
   - Настройте стоимость градиентов
   - Добавьте свои пресеты
   - Настройте интеграцию с PlayerPoints

## � Сборка из исходников

```bash
# Клонировать репозиторий
git clone https://codeberg.org/loki5512344/Lochat.git
cd Lochat

# Собрать проект
./gradlew build

# Jar файл будет в build/libs/
```

## 📦 Зависимости

### Обязательные
- **Java 21+**
- **Paper 1.20.1+** или **Folia**

### Опциональные
- **PlaceholderAPI** - для плейсхолдеров
- **LuckPerms** - для градиентных ников и прав
- **PlayerPoints** - для покупки градиентов
- **LibertyBans** - синхронизация мутов
- **SkinsRestorer** - поддержка скинов

## 📝 Команды

### Чат команды
- `/g <сообщение>` - глобальный чат
- `/l <сообщение>` - локальный чат
- `/msg <игрок> <сообщение>` - личное сообщение
- `/reply <сообщение>` - ответить на ЛС
- `/ignore <игрок>` - игнорировать игрока
- `/unignore <игрок>` - разигнорировать
- `/ignorelist` - список игнорируемых

### Градиенты
- `/color <hex1> [hex2] ...` - установить градиент
- `/prefix <текст>` - установить префикс
- `/aprefix <команда>` - админские команды градиентов

### Модерация
- `/lmute <игрок> [время] [-s] [причина]` - замутить
- `/lunmute <игрок> [-s]` - размутить
- `/lmutelist` - список мутов
- `/lmutehistory <игрок>` - история мутов
- `/lmuteblame <модератор>` - муты модератора

### Кастомные команды
- `/help` - помощь
- `/rules` - правила
- `/discord` - Discord сервера
- `/coords` - координаты
- `/ping [игрок]` - пинг
- `/stats [игрок]` - статистика
- `/me <действие>` - действие от 3-го лица
- `/sounds [on|off]` - управление звуками

### Админские
- `/lochat reload` - перезагрузить конфиг
- `/clearchat` - очистить чат
- `/chatspy` - режим шпиона
- `/announce <сообщение>` - объявление

## 🎨 Примеры использования

### Градиентный ник
```
/color #FF0000 #00FF00 #0000FF
```
Создаст радужный градиент от красного через зеленый к синему.

### Кастомный префикс
```
/prefix [VIP]
```
Установит префикс `[VIP]` перед ником.

### Настройка системы оплаты градиентов

#### Вариант 1: Через PlayerPoints (платно)
```yaml
# config/gradient-config.yml
gradient:
  price-per-color: 50          # 50 поинтов за каждый цвет
  prefix-price: 500            # 500 поинтов за префикс
  use-permission-instead-of-cost: false
```

Игроки платят поинтами за установку градиентов.

#### Вариант 2: Через пермишены (бесплатно)
```yaml
# config/gradient-config.yml
gradient:
  price-per-color: 0           # Бесплатно
  prefix-price: 0              # Бесплатно
  use-permission-instead-of-cost: true
```

Выдайте игрокам права:
```
/lp user <игрок> permission set gradient.color true
/lp user <игрок> permission set gradient.prefix true
/lp user <игрок> permission set gradient.bypass.cost true
```

Или через группу:
```
/lp group vip permission set gradient.color true
/lp group vip permission set gradient.prefix true
/lp group vip permission set gradient.bypass.cost true
```

### Основные пермишены градиентов

| Пермишен | Описание |
|----------|----------|
| `gradient.color` | Доступ к команде /color |
| `gradient.prefix` | Доступ к команде /prefix |
| `gradient.bypass.cost` | Бесплатное использование (обход стоимости) |
| `gradient.bypass.cooldown` | Обход кулдауна |
| `gradient.admin` | Админские команды /aprefix |
| `gradient.colors.5` | Максимум 5 цветов в градиенте |
| `gradient.colors.10` | Максимум 10 цветов в градиенте |
| `gradient.colors.unlimited` | Неограниченное количество цветов |

### Текущие цены (по умолчанию)

- **Градиент**: 50 поинтов за каждый цвет
  - 1 цвет = 50 поинтов
  - 2 цвета = 100 поинтов
  - 3 цвета = 150 поинтов
  - и т.д.

- **Префикс**: 500 поинтов (одноразовая покупка)

Цены настраиваются в `config/gradient-config.yml`

### Мут игрока
```
/lmute Player123 1h Спам в чате
```
Замутит игрока на 1 час с причиной "Спам в чате".

### Скрытый мут
```
/lmute Player123 30m -s Нарушение правил
```
Замутит игрока скрытно (другие игроки не увидят уведомление).

## 🔌 API для разработчиков

### Получение сервисов
```java
// Получить сервис чата
ChatService chatService = ServiceRegistry.getChatService();

// Отправить глобальное сообщение
chatService.sendGlobalMessage(player, "Привет!");

// Получить сервис мутов
MuteService muteService = ServiceRegistry.getMuteService();

// Проверить мут
if (muteService.isMuted(player.getUniqueId())) {
    // Игрок замучен
}
```

### Создание кастомного фильтра
```java
public class MyFilter implements MessageFilter {
    @Override
    public FilterResult filter(Player player, String message) {
        if (message.contains("запрещено")) {
            return FilterResult.block("Сообщение заблокировано");
        }
        return FilterResult.allow();
    }
    
    @Override
    public String getName() {
        return "MyFilter";
    }
    
    @Override
    public int getPriority() {
        return 10;
    }
}

// Регистрация
FilterManager.registerFilter(new MyFilter());
```

## 🐛 Известные проблемы

- Градиенты могут не отображаться в некоторых версиях клиента
- Фильтры могут блокировать легитимные сообщения (настраивается)

## 📊 Статистика проекта

- **Версия**: 1.5.5-1.20.1
- **Коммитов**: 80+
- **Строк кода**: 9000+
- **Конфигов**: 10+
- **Команд**: 30+

## 🤝 Вклад в проект

Проект находится в активной разработке. Pull requests приветствуются!

1. Fork репозитория
2. Создайте ветку для фичи (`git checkout -b feature/amazing-feature`)
3. Commit изменения (`git commit -m 'Add amazing feature'`)
4. Push в ветку (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## 📄 Лицензия

Proprietary - Все права защищены

## 👥 Автор

**Loki Development Team**
- Codeberg: [@loki5512344](https://codeberg.org/loki5512344)

## 🔗 Ссылки

- [Codeberg Repository](https://codeberg.org/loki5512344/Lochat)
- [Issues](https://codeberg.org/loki5512344/Lochat/issues)


---

⭐ Если проект понравился, поставьте звезду на Codeberg!
