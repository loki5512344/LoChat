# TODO — LoChat · Spacelegacy Edition

> Последнее обновление: 16.03.2026

---

## ✅ Все задачи выполнены!

### Завершённые задачи:
- ✅ P0/P1/P2/P3 баги исправлены
- ✅ RP-команды добавлены
- ✅ Цветовая гамма применена
- ✅ Структура команд переработана (SOLID, DRY, KISS)
- ✅ Удалён мёртвый код (custom/, social/)
- ✅ Хардкод строки вынесены в конфиги
- ✅ Кулдауны для RP-команд (/me, /try, /do)
- ✅ Двусторонний игнор в PM
- ✅ UnmuteCommand — строки в HardcodedMessages

---

## 📁 Финальная структура commands/

```
commands/
├── CommandManager.java          ← регистрация всех команд
├── CustomCommand.java
├── CustomCommandsCommand.java
│
├── admin/       Announce, ChatSpy, ClearChat, ClearChatConfig,
│                Discord, Hub, LoChat, ReloadConfig
├── base/        BaseCommand, PlayerCommand, AdminCommand
├── chat/        GlobalChatCommand, LocalChatCommand
├── messaging/   Msg, Reply, Ignore, Unignore, IgnoreList
├── moderation/  Mute, Unmute, MuteList, MuteHistory, MuteBlame
├── nick/        Nick, PlayerInfo
└── rp/          Me, Try, Do, Roll, RpUtil (с кулдаунами!)
```

---

## 🎯 Опциональные фичи (если захочешь)

- [ ] **FEAT: Команда /stats** — показывать статистику из `statistics.yml`
- [ ] **FEAT: Chat logging в файл** — `logs/chat-YYYY-MM-DD.log`
- [ ] **FEAT: AFK система** — автоматический статус AFK
- [ ] **FEAT: Emoji shortcuts** — :smile: → 😊
