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

### Система
- [x] Автосообщения
- [x] Объявления (Title + ActionBar)
- [x] PlaceholderAPI интеграция
- [x] HEX цвета `&#RRGGBB`
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
```

---

## 📁 Файлы данных

- `ignores.yml` — список игнорируемых игроков
- `mutes.yml` — активные муты

---

## 🔑 Права

| Право | Описание |
|-------|----------|
| chat.global.use | Глобальный чат |
| chat.local.use | Локальный чат |
| chat.pm.use | Личные сообщения |
| chat.pm.ignore | Игнор игроков |
| chat.announce.use | Объявления |
| chat.spy | Шпион режим |
| chat.clear | Очистка чата |
| chat.mute | Мут игроков |
| chat.bypass.cooldown | Обход кулдауна |
| chat.bypass.filter | Обход фильтра |
| chat.bypass.antispam | Обход анти-спама |
