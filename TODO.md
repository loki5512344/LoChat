# LoChat - Статус проекта

## ✅ Готово

### Чат
- [x] Глобальный чат (`!сообщение`, `/g`)
- [x] Локальный чат (100 блоков)
- [x] PM система (`/msg`, `/reply`)
- [x] Игнор (`/ignore`, `/unignore`) — сохраняется в файл
- [x] Упоминания @ник со звуком

### Модерация
- [x] Мут система (`/mute`, `/unmute`) — сохраняется в файл
- [x] Фильтр мата (censor/block/warn)
- [x] Анти-спам (CAPS, повторы, похожие)
- [x] Кулдауны на сообщения
- [x] Чат-шпион (`/chatspy`)
- [x] Очистка чата (`/clearchat`)

### Градиентные ники (LoPreff интеграция)
- [x] Команды `/color`, `/prefix`
- [x] Градиенты на ники и префиксы
- [x] Интеграция с LuckPerms
- [x] PlayerPoints поддержка
- [x] TAB плагин совместимость (`%lochat_full%`)
- [x] Админ команды `/aprefix`

### Дополнительно
- [x] Цвет чата (`/chatcolor`)
- [x] Смайлики (300+ эмодзи)
- [x] Кастомные команды
- [x] Автосообщения

### Система
- [x] Объявления (Title + ActionBar)
- [x] PlaceholderAPI интеграция
- [x] HEX цвета `&#RRGGBB` и `§x§...`
- [x] MiniMessage форматирование
- [x] Folia совместимость
- [x] Горячая перезагрузка

---

## 📊 PlaceholderAPI

```
%lochat_muted%         — замучен (true/false)
%lochat_mute_time%     — время мута
%lochat_mute_reason%   — причина мута
%lochat_ignored_count% — кол-во игнорируемых
%lochat_global_enabled% — глобальный чат вкл
%lochat_last_pm%       — последний собеседник
%lochat_spy_enabled%   — шпион режим
%lochat_full%          — префикс + градиентный ник (для TAB)
%lochat_name%          — только градиентный ник
%lochat_prefix%        — только префикс с градиентом
%lochat_lp_prefix%     — LuckPerms префикс
```

---

## 📁 Файлы данных

- `ignores.yml` — список игнорируемых игроков
- `mutes.yml` — активные муты
- `chatcolors.yml` — цвета чата игроков
- `gradient-data.yml` — данные градиентных ников

---

## 🔑 Права

| Право | Описание |
|-------|----------|
| chat.global.use | Глобальный чат |
| chat.global.color | Цвета в глобальном чате |
| chat.local.use | Локальный чат |
| chat.local.color | Цвета в локальном чате |
| chat.pm.use | Личные сообщения |
| chat.pm.ignore | Игнор игроков |
| chat.announce.use | Объявления |
| chat.spy | Шпион режим |
| chat.clear | Очистка чата |
| chat.mute | Мут игроков |
| chat.bypass.cooldown | Обход кулдауна |
| chat.bypass.filter | Обход фильтра |
| chat.bypass.antispam | Обход анти-спама |
| lochat.chatcolor | Цвет чата |
| gradient.color | Градиентные цвета |
| gradient.prefix | Кастомные префиксы |
| gradient.admin | Админ команды градиентов |
| lochat.admin | Админ команды плагина |

---

## 🎯 Возможные улучшения

- [ ] GUI для настройки градиентов
- [ ] Анимированные градиенты
- [ ] Больше форматов времени в муте
- [ ] Статистика сообщений
- [ ] Бэкап системы данных
