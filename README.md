# LoChat

Продвинутый чат-плагин для Minecraft Folia 1.21.8 с интегрированным модулем градиентных ников.

**Автор:** loki

---

## Установка

1. Скачай `LoChat-1.0.0.jar` из `build/libs/`
2. Положи в папку `plugins/` сервера
3. Перезапусти сервер

---

## Функционал

### Чат
- Глобальный чат (`!сообщение` или `/g`)
- Локальный чат (радиус 100 блоков)
- Личные сообщения (`/msg`, `/reply`)
- Система игнорирования
- Автосообщения (многострочные, с порядком)
- Смайлики (200+ эмодзи)

### Модерация
- Мут система (`/mute`, `/unmute`)
- Фильтр мата (censor/block/warn)
- Анти-спам (CAPS, повторы, похожие сообщения)
- Кулдауны на сообщения
- Шпион режим (`/chatspy`)
- Очистка чата (`/clearchat`)

### Градиентные ники (интегрированный LoPreff)
- Кастомные градиенты на ник (`/color`)
- Кастомные префиксы (`/prefix`)
- Интеграция с LuckPerms
- Покупка за PlayerPoints (опционально)
- GUI подтверждения

---

## Команды

### Чат
| Команда | Описание | Право |
|---------|----------|-------|
| `!сообщение` | Глобальный чат | chat.global.use |
| `/g <msg>` | Глобальный чат | chat.global.use |
| `/globalchat toggle` | Вкл/выкл глобальный | chat.global.toggle |
| `/l <msg>` | Локальный чат | chat.local.use |
| `/msg <ник> <msg>` | Личное сообщение | chat.pm.use |
| `/reply <msg>` | Ответить | chat.pm.use |
| `/ignore <ник>` | Игнорировать | chat.pm.ignore |
| `/unignore <ник>` | Убрать из игнора | chat.pm.ignore |
| `/announce <msg>` | Объявление | chat.announce.use |

### Модерация
| Команда | Описание | Право |
|---------|----------|-------|
| `/chatspy` | Шпион режим | chat.spy |
| `/clearchat` | Очистить чат | chat.clear |
| `/mute <ник> <время>` | Замутить | chat.mute |
| `/unmute <ник>` | Размутить | chat.mute |

### Градиенты
| Команда | Описание | Право |
|---------|----------|-------|
| `/color <hex1> [hex2]...` | Установить градиент | gradient.color |
| `/color on/off/reset` | Управление цветом | gradient.color |
| `/color copy <ник>` | Скопировать цвет | gradient.color |
| `/prefix <текст>` | Установить префикс | gradient.prefix |
| `/prefix on/off/reset` | Управление префиксом | gradient.prefix |
| `/aprefix reload` | Перезагрузка | gradient.admin |
| `/aprefix info <ник>` | Инфо об игроке | gradient.admin |
| `/aprefix setcolor <ник> <цвета>` | Установить цвет | gradient.admin |
| `/aprefix setprefix <ник> <текст>` | Установить префикс | gradient.admin |

---

## Права

### Чат
| Право | По умолчанию |
|-------|--------------|
| chat.global.use | true |
| chat.global.toggle | true |
| chat.global.color | op |
| chat.local.use | true |
| chat.local.color | op |
| chat.pm.use | true |
| chat.pm.ignore | true |
| chat.mention.use | true |

### Модерация
| Право | По умолчанию |
|-------|--------------|
| chat.announce.use | op |
| chat.spy | op |
| chat.clear | op |
| chat.mute | op |
| chat.bypass.cooldown | op |
| chat.bypass.filter | op |
| chat.bypass.antispam | op |

### Градиенты
| Право | По умолчанию |
|-------|--------------|
| gradient.color | true |
| gradient.prefix | true |
| gradient.admin | op |
| gradient.bypass.cooldown | op |
| gradient.bypass.cost | op |

---

## PlaceholderAPI

```
%lochat_muted%           — замучен (true/false)
%lochat_mute_time%       — оставшееся время мута
%lochat_mute_reason%     — причина мута
%lochat_ignored_count%   — кол-во игнорируемых
%lochat_global_enabled%  — глобальный чат включен
%lochat_spy_enabled%     — шпион режим включен

# Градиенты
%lochat_gradient_full%   — полное имя (префикс + ник)
%lochat_gradient_name%   — только градиентный ник
%lochat_gradient_prefix% — только префикс
%lochat_full%            — алиас для gradient_full
%lochat_name%            — алиас для gradient_name
%lochat_prefix%          — алиас для gradient_prefix
```

---

## Конфигурационные файлы

- `config.yml` — основные настройки чата
- `messages.yml` — сообщения чата
- `automessages.yml` — автосообщения
- `emojis.yml` — смайлики
- `custom-commands.yml` — кастомные команды
- `gradient-config.yml` — настройки градиентов
- `gradient-messages.yml` — сообщения градиентов
- `ignores.yml` — сохранённые игноры
- `mutes.yml` — активные муты
- `gradient-data.yml` — данные градиентов (или SQLite)

---

## Кастомные команды

LoChat позволяет создавать собственные команды через файл `custom-commands.yml`. После создания команды выполните `/lochat reload` для применения изменений.

### Структура команды

```yaml
command-name:
  permission: "lochat.command.name"  # Право для использования (опционально)
  message: "Текст сообщения"         # Сообщение которое отправится
  aliases: ["alias1", "alias2"]      # Алиасы команды (опционально)
  type: "chat"                       # Тип: chat, broadcast, title, actionbar
  target: "sender"                   # Кому отправить: sender, all, permission:право
```

### Типы команд

- **chat** — отправляет сообщение в чат
- **broadcast** — отправляет сообщение всем игрокам
- **title** — показывает title (первая строка — заголовок, вторая — подзаголовок, разделяются `\n`)
- **actionbar** — показывает сообщение в actionbar

### Целевая аудитория (target)

- **sender** — только отправителю команды
- **all** — всем игрокам на сервере
- **permission:право** — только игрокам с указанным правом (например: `permission:lochat.admin`)

### Плейсхолдеры

В сообщениях можно использовать следующие плейсхолдеры:

- `{player}` — имя игрока
- `{x}`, `{y}`, `{z}` — координаты игрока
- `{world}` — название мира
- `{arg0}`, `{arg1}`, `{arg2}` — аргументы команды (нумерация с 0)
- `{args}` — все аргументы через пробел

Также поддерживаются плейсхолдеры PlaceholderAPI (если установлен).

### Примеры

#### Простая команда помощи
```yaml
help:
  message: "&6Добро пожаловать на сервер!\n&#808080Используйте /rules для просмотра правил"
  aliases: ["помощь", "хелп"]
  type: "chat"
  target: "sender"
```

#### Команда с координатами
```yaml
coords:
  message: "Ваши координаты: <green>{x}</green>, <green>{y}</green>, <green>{z}</green> в мире <yellow>{world}</yellow>"
  aliases: ["координаты", "pos"]
  type: "actionbar"
  target: "sender"
```

#### Команда для админов (broadcast)
```yaml
restart-warning:
  permission: "lochat.admin"
  message: "<red><bold>ВНИМАНИЕ!</bold></red> <yellow>Сервер будет перезагружен через 5 минут!</yellow>"
  aliases: ["рестарт", "reboot"]
  type: "broadcast"
  target: "all"
```

#### Команда с title
```yaml
welcome:
  message: "&6Добро пожаловать!\n&#808080Наслаждайтесь игрой!"
  aliases: ["привет"]
  type: "title"
  target: "sender"
```

#### Команда только для VIP
```yaml
vip-info:
  permission: "lochat.vip"
  message: "<gold>VIP информация:</gold>\n<yellow>Вы имеете доступ к специальным командам!</yellow>"
  type: "chat"
  target: "permission:lochat.vip"
```

### Форматирование цветов

В сообщениях можно использовать:
- **MiniMessage теги**: `<red>`, `<bold>`, `<#FF0000>`
- **Legacy коды**: `&a`, `&l`, `&c`
- **HEX цвета**: `#FF0000` (автоматически конвертируется)

### Важные замечания

1. После изменения `custom-commands.yml` выполните `/lochat reload`
2. Имена команд должны быть уникальными
3. Если команда уже существует в Minecraft/Bukkit, она будет перезаписана
4. Для title используйте `\n` для разделения заголовка и подзаголовка
5. PlaceholderAPI плейсхолдеры работают только если установлен PlaceholderAPI

---

## Форматирование цветов

### MiniMessage
```
<red>Красный</red>
<gradient:red:blue>Градиент</gradient>
```

### HEX цвета
```
&#FF0000Красный
&#3BA8FFГолубой
```

### Legacy коды
```
&aЗелёный
&lЖирный
```

---

## Требования

- Folia 1.21.8+ / Paper 1.21+
- Java 21

**Опционально:**
- PlaceholderAPI — для плейсхолдеров
- PlayerPoints — для покупки градиентов
- LuckPerms — для префиксов в табе

---

## Сборка

```bash
./gradlew build
```

Jar файл: `build/libs/LoChat-1.0.0.jar`
