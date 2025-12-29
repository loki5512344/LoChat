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
- `gradient-config.yml` — настройки градиентов
- `gradient-messages.yml` — сообщения градиентов
- `ignores.yml` — сохранённые игноры
- `mutes.yml` — активные муты
- `gradient-data.yml` — данные градиентов (или SQLite)

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
