# LoChat

> **Advanced chat plugin for Paper/Folia servers**  
> **Продвинутый чат плагин для Paper/Folia серверов**

[![Build](https://github.com/loki5512344/LoChat/actions/workflows/build.yml/badge.svg)](https://github.com/loki5512344/LoChat/actions/workflows/build.yml)
[![License: GPLv3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![](https://img.shields.io/badge/Paper-1.21.4-blue)](https://papermc.io)

---

<div align="center">

**[EN](#english) · [RU](#russian)**

</div>

---

<a name="english"></a>

## 🇬🇧 English

### Overview

LoChat is a powerful Minecraft chat plugin for Paper 1.21+ and Folia servers. It features gradient nicknames, advanced moderation, flexible configuration, Discord integration, and extensive API.

### Features

#### Chat System
- **Global chat** — cross-server player communication
- **Local chat** — radius-based proximity chat
- **Private messages** — PM system with reply support
- **Ignore system** — block unwanted players
- **Mentions** — `@player`, `@everyone`, `@here`, `@role`
- **Hover/Click events** — interactive messages
- **Custom join/quit/death messages** with weapon name

#### Gradient Nicknames & Prefixes
- Custom gradient colors (up to 7 colors)
- Built-in presets (fire, ocean, rainbow, etc.)
- Custom prefixes and suffixes
- LuckPerms integration (gradient prefixes/suffixes)
- PlayerPoints purchase system
- Cooldowns and limits

#### Moderation
- Mute system with history
- Ban system with duration and reason
- Warn system
- Temporary and permanent punishments
- Silent mutes
- Punishment escalation

#### Chat Filters
- Swear filter with bypass protection (x→x, o→o, etc.)
- Anti-spam and anti-flood
- CAPS filter
- URL and IP filter
- Hidden link filter (discord.gg, bit.ly, etc.)
- Repeated character filter
- Configurable bad words list

#### RP Commands
- `/me` — third-person actions
- `/do` — action descriptions
- `/try` — chance-based actions
- `/roll` — dice roll
- Configurable radius and cooldowns

#### Sound Effects
- PM and mention sounds
- Mute/unmute sounds
- Gradient sounds
- Command sounds
- Per-player sound settings

#### Discord Integration
- Webhook message relay
- Join/quit/death events
- Custom formats and colors
- Player avatars
- Message filtering

### Quick Start

#### Installation

1. Download the latest release
2. Place the JAR in `plugins/`
3. Restart the server
4. Configure in `plugins/LoChat/`

#### Building from Source

```bash
git clone https://github.com/loki5512344/LoChat.git
cd LoChat
./gradlew build
# JAR will be in build/libs/
```

#### Local Test Server

```bash
./gradlew runServer
```

Uses [run-paper](https://github.com/jpenilla/run-paper) — downloads Paper 1.21.4, builds the plugin, and places it in `run/plugins/`.

### Dependencies

**Required:** Java 21+, Paper 1.21.1+ or Folia

**Optional:** PlaceholderAPI, LuckPerms, PlayerPoints, LibertyBans, SkinsRestorer

### Commands

#### Chat
| Command | Description |
|---------|-------------|
| `/g <message>` | Global chat |
| `/l <message>` | Local chat |
| `/msg <player> <message>` | Private message |
| `/reply <message>` | Reply to PM |
| `/ignore <player>` | Ignore player |
| `/unignore <player>` | Unignore player |
| `/ignorelist` | List ignored players |

#### Gradients
| Command | Description |
|---------|-------------|
| `/color <hex1> [hex2] ...` | Set gradient |
| `/prefix <text>` | Set prefix |
| `/aprefix <command>` | Admin gradient commands |

#### Moderation
| Command | Description |
|---------|-------------|
| `/lmute <player> [time] [-s] [reason]` | Mute player |
| `/lunmute <player> [-s]` | Unmute player |
| `/lban <player> [time] [reason]` | Ban player |
| `/lunban <player>` | Unban player |
| `/lwarn <player> [reason]` | Warn player |
| `/lmutelist` | List mutes |
| `/lmutehistory <player>` | Mute history |

#### RP
| Command | Description |
|---------|-------------|
| `/me <action>` | Third-person action |
| `/do <description>` | Action description |
| `/try <attempt>` | Chance attempt |
| `/roll [number]` | Dice roll |

#### Admin
| Command | Description |
|---------|-------------|
| `/lochat reload` | Reload config |
| `/clearchat` | Clear chat |
| `/chatspy` | Spy mode |
| `/announce <message>` | Announcement |

### Time Formats

- `s` — seconds (`30s` = 30 seconds)
- `m` — minutes (`15m` = 15 minutes)
- `h` — hours (`2h` = 2 hours)
- `d` — days (`7d` = 7 days)
- `perm` or `0` — permanent

### Placeholders

**Chat:** `{player}`, `{player_prefix}`, `{player_suffix}`, `{message}`, `{emoji}`, `{prefix}`, `{separator}`

**Hover:** `{player}`, `{world}`, `{ping}`, `{gamemode}`, `{health}`, `{food}`

**Death:** `{player}`, `{killer}`, `{weapon}`, `{death_message}`

### Permissions

#### Gradients
- `gradient.color` — access to `/color`
- `gradient.prefix` — access to `/prefix`
- `gradient.bypass.cost` — free usage
- `gradient.bypass.cooldown` — bypass cooldown
- `gradient.admin` — admin commands

#### Chat
- `lochat.chat.colors` — colored messages
- `lochat.chat.global` — global chat
- `lochat.chat.local` — local chat
- `lochat.pm` — private messages

#### Moderation
- `lochat.mute` — mute players
- `lochat.mute.silent` — silent mutes
- `lochat.ban` — ban players
- `lochat.warn` — warn players

#### Filter Bypass
- `lochat.bypass.swear` — bypass swear filter
- `lochat.bypass.spam` — bypass anti-spam
- `lochat.bypass.flood` — bypass anti-flood
- `lochat.bypass.caps` — bypass CAPS filter
- `lochat.bypass.urlfilter` — bypass URL filter
- `lochat.bypass.cooldown` — bypass cooldown

### API

```java
// Get ServiceRegistry
ServiceRegistry registry = plugin.getServiceRegistry();

// Send global message
ChatService chatService = registry.get(ChatService.class);
chatService.sendGlobalMessage(player, "Hello!");

// Create custom filter
public class MyFilter implements MessageFilter {
    @Override
    public FilterResult filter(Player player, String message) {
        if (message.contains("badword")) {
            return FilterResult.blocked("Message blocked");
        }
        return FilterResult.ok(message);
    }
}
```

### Known Issues

- Gradients may not display correctly on clients < 1.16
- Some filters may block legitimate messages (configurable in `config/filters.yml`)

### License

[GNU General Public License v3.0](LICENSE)

---

<a name="russian"></a>

## 🇷🇺 Русский

### Обзор

LoChat — это мощный чат-плагин для Minecraft Paper 1.21+ и Folia серверов. Включает градиентные ники, продвинутую модерацию, гибкую конфигурацию, Discord интеграцию и обширный API.

### Основные возможности

#### Система чата
- **Глобальный чат** — общение между всеми игроками
- **Локальный чат** — общение в радиусе
- **Личные сообщения** — PM система с ответами
- **Игнорирование** — блокировка нежелательных игроков
- **Упоминания** — `@player`, `@everyone`, `@here`, `@role`
- **Hover/Click события** — интерактивные сообщения
- **Кастомные сообщения входа/выхода/смерти** с названием оружия

#### Градиентные ники и префиксы
- Кастомные градиентные цвета (до 7 цветов)
- Готовые пресеты (огонь, океан, радуга и т.д.)
- Кастомные префиксы и суффиксы
- Интеграция с LuckPerms (префиксы и суффиксы с градиентом)
- Система покупки через PlayerPoints
- Кулдауны и ограничения

#### Модерация
- Мут система с историей
- Бан система с временем и причиной
- Варны (предупреждения)
- Временные и постоянные наказания
- Скрытые муты (silent)
- Эскалация наказаний

#### Фильтры чата
- Фильтр мата с защитой от обхода (х→x, о→o и т.д.)
- Антиспам и антифлуд
- CAPS фильтр
- URL и IP фильтр
- Фильтр скрытых ссылок (discord.gg, bit.ly и т.д.)
- Фильтр повторяющихся символов
- Настраиваемый список запрещённых слов

#### RP команды
- `/me` — действия от третьего лица
- `/do` — описание действий
- `/try` — попытка действия с шансом
- `/roll` — бросок кубика
- Настраиваемый радиус и кулдауны

#### Звуковые эффекты
- Звуки для PM и упоминаний
- Звуки для мутов/размутов
- Звуки для градиентов
- Звуки для команд
- Персональные настройки звуков

#### Discord интеграция
- Webhook для отправки сообщений
- События входа/выхода/смерти
- Кастомные форматы и цвета
- Аватары игроков
- Фильтрация сообщений

### Быстрый старт

#### Установка

1. Скачайте последнюю версию из Releases
2. Поместите JAR в папку `plugins/`
3. Перезапустите сервер
4. Настройте конфиги в `plugins/LoChat/`

#### Сборка из исходников

```bash
git clone https://github.com/loki5512344/LoChat.git
cd LoChat
./gradlew build
# Jar файл будет в build/libs/
```

#### Локальный тестовый сервер

```bash
./gradlew runServer
```

Использует [run-paper](https://github.com/jpenilla/run-paper) — скачивает Paper 1.21.4, собирает плагин и помещает его в `run/plugins/`.

### Зависимости

**Обязательные:** Java 21+, Paper 1.21.1+ или Folia

**Опциональные:** PlaceholderAPI, LuckPerms, PlayerPoints, LibertyBans, SkinsRestorer

### Команды

#### Чат
| Команда | Описание |
|---------|----------|
| `/g <сообщение>` | Глобальный чат |
| `/l <сообщение>` | Локальный чат |
| `/msg <игрок> <сообщение>` | Личное сообщение |
| `/reply <сообщение>` | Ответить на ЛС |
| `/ignore <игрок>` | Игнорировать игрока |
| `/unignore <игрок>` | Разигнорировать |
| `/ignorelist` | Список игнорируемых |

#### Градиенты
| Команда | Описание |
|---------|----------|
| `/color <hex1> [hex2] ...` | Установить градиент |
| `/prefix <текст>` | Установить префикс |
| `/aprefix <команда>` | Админские команды градиентов |

#### Модерация
| Команда | Описание |
|---------|----------|
| `/lmute <игрок> [время] [-s] [причина]` | Замутить |
| `/lunmute <игрок> [-s]` | Размутить |
| `/lban <игрок> [время] [причина]` | Забанить |
| `/lunban <игрок>` | Разбанить |
| `/lwarn <игрок> [причина]` | Выдать варн |
| `/lmutelist` | Список мутов |
| `/lmutehistory <игрок>` | История мутов |

#### RP
| Команда | Описание |
|---------|----------|
| `/me <действие>` | Действие от 3-го лица |
| `/do <описание>` | Описание действия |
| `/try <попытка>` | Попытка с шансом |
| `/roll [число]` | Бросок кубика |

#### Админские
| Команда | Описание |
|---------|----------|
| `/lochat reload` | Перезагрузить конфиг |
| `/clearchat` | Очистить чат |
| `/chatspy` | Режим шпиона |
| `/announce <сообщение>` | Объявление |

### Форматы времени

- `s` — секунды (`30s` = 30 секунд)
- `m` — минуты (`15m` = 15 минут)
- `h` — часы (`2h` = 2 часа)
- `d` — дни (`7d` = 7 дней)
- `perm` или `0` — навсегда

### Плейсхолдеры

**Чат:** `{player}`, `{player_prefix}`, `{player_suffix}`, `{message}`, `{emoji}`, `{prefix}`, `{separator}`

**Hover:** `{player}`, `{world}`, `{ping}`, `{gamemode}`, `{health}`, `{food}`

**Смерть:** `{player}`, `{killer}`, `{weapon}`, `{death_message}`

### Пермишены

#### Градиенты
- `gradient.color` — доступ к `/color`
- `gradient.prefix` — доступ к `/prefix`
- `gradient.bypass.cost` — бесплатное использование
- `gradient.bypass.cooldown` — обход кулдауна
- `gradient.admin` — админские команды

#### Чат
- `lochat.chat.colors` — цветные сообщения
- `lochat.chat.global` — глобальный чат
- `lochat.chat.local` — локальный чат
- `lochat.pm` — личные сообщения

#### Модерация
- `lochat.mute` — мутить игроков
- `lochat.mute.silent` — скрытые муты
- `lochat.ban` — банить игроков
- `lochat.warn` — выдавать варны

#### Обход фильтров
- `lochat.bypass.swear` — обход мат-фильтра
- `lochat.bypass.spam` — обход антиспама
- `lochat.bypass.flood` — обход антифлуда
- `lochat.bypass.caps` — обход CAPS фильтра
- `lochat.bypass.urlfilter` — обход URL фильтра
- `lochat.bypass.cooldown` — обход кулдауна

### API

```java
// Получить ServiceRegistry
ServiceRegistry registry = plugin.getServiceRegistry();

// Отправить глобальное сообщение
ChatService chatService = registry.get(ChatService.class);
chatService.sendGlobalMessage(player, "Привет!");

// Создание кастомного фильтра
public class MyFilter implements MessageFilter {
    @Override
    public FilterResult filter(Player player, String message) {
        if (message.contains("запрещено")) {
            return FilterResult.blocked("Сообщение заблокировано");
        }
        return FilterResult.ok(message);
    }
}
```

### Известные проблемы

- Градиенты могут некорректно отображаться в старых версиях клиента (< 1.16)
- Некоторые фильтры могут блокировать легитимные сообщения (настраивается в `config/filters.yml`)

### Лицензия

[GNU General Public License v3.0](LICENSE)

---

## Project Statistics | Статистика проекта

| Metric | Value |
|--------|-------|
| Version | 1.5.5 |
| Java files | 150+ |
| Commands | 30+ |
| Config files | 10+ |

## Links | Ссылки

- [Codeberg Repository](https://codeberg.org/loki5512344/Lochat)
- [Issues](https://codeberg.org/loki5512344/Lochat/issues)
- [Contributing Guide](CONTRIBUTING.md)
