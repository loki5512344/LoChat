# План миграции команд на новую архитектуру

> **Статус (актуально для репозитория):** централизованная регистрация в `CommandManager` и раскладка по пакетам **уже внедрены**. Ниже — что сделано и что остаётся опциональной доработкой.

**Что осталось по плану:** миграция команд **завершена**. Дальше — по желанию: перевести оставшиеся `CommandExecutor` на `PlayerCommand`/`AdminCommand`, добавить тесты/ручной регресс, короткую доку «как добавить команду».

## Что уже сделано

### Архитектура
- `commands/base/BaseCommand`, `PlayerCommand`, `AdminCommand`
- `CommandManager` — единая точка регистрации (`reg` / `regTab` / `registerBaseCommand`)

### Пакеты (фактическая структура)
| Пакет | Назначение |
|--------|------------|
| `commands/chat/` | глобальный / локальный чат (`g`, `l`) |
| `commands/messaging/` | ЛС, игнор (`msg`, `reply`, `ignore`, `unignore`, `ignorelist`) |
| `commands/moderation/` | муты, варны, баны (`lmute`, `lunmute`, `lmutelist`, `lmutehistory`, `lmuteblame`, `warn`, `silentwarn`, `lban`, `lunban`) |
| `commands/nick/` | ник, инфо (`nick`, `playerinfo`) |
| `commands/admin/` | админ-утилиты (`announce`, `chatspy`, `clearchat`, `clearchatconfig`, `lochat`, `lochatreload`, `discordadmin`); в репозитории также есть `HubCommand.java` (проверьте `plugin.yml`, если нужна команда хаба) |
| `commands/rp/` | RP (`me`, `try`, `do`, `roll`) |
| Корень `commands/` | `CustomCommandsCommand`, `CustomCommand` |

Папки `social/` в проекте нет — социальные команды лежат в `messaging/` и `nick/`.

### Регистрация в `CommandManager` (выполнено)
- [x] Этап 1 — чат: `GlobalChatCommand`, `LocalChatCommand`
- [x] Этап 2 — ЛС: `MsgCommand`, `ReplyCommand`
- [x] Этап 3 — социальное: `IgnoreCommand`, `UnignoreCommand`, `IgnoreListCommand`, `NickCommand` (+ `PlayerInfoCommand`)
- [x] Этап 4 — модерация: все перечисленные в таблице пакета `moderation/`
- [x] Этап 5 — админ: `AnnounceCommand`, `ClearChatCommand`, `ClearChatConfigCommand`, `ChatSpyCommand`, `LoChatCommand`, `ReloadConfigCommand`, `DiscordCommand`
- [x] Этап 6 — `CustomCommandsCommand` и движок кастомных команд

## Что ещё можно сделать (не блокирует работу)

### Унификация базового класса
Многие команды по-прежнему реализуют `CommandExecutor` напрямую, а не `PlayerCommand` / `AdminCommand`. Имеет смысл постепенно переводить на базовые классы там, где это убирает дублирование проверок и сообщений.

### Тесты и ручная проверка
- [ ] Стабильные сценарии для приоритетных команд (чат, ЛС, муты) — ручные или автотесты
- [ ] Проверка TabCompleter на зарегистрированных командах с `regTab`

### Документация
- [ ] Короткий `CONTRIBUTING` или раздел в README: как добавить команду через `CommandManager` и `plugin.yml`

## Процесс миграции одной команды (если рефакторите дальше)

### 1. Новая команда на базовом классе
```java
public class NewCommand extends PlayerCommand {
    public NewCommand(LoChat plugin) {
        super(plugin);
    }

    @Override
    protected boolean executePlayerCommand(Player player, Command command,
            String label, String[] args) {
        return true;
    }
}
```

### 2. Проверки
- Поведение и права
- Сообщения из `MessagesConfig` / конфигов
- TabCompleter при необходимости

### 3. Регистрация
В `CommandManager`: `reg("name", new NewCommand(plugin))` или `regTab`, плюс запись в `plugin.yml`.

### 4. Удаление старого кода
После замены — удалить старый класс и неиспользуемые импорты.

## Чек-лист для команды

- [ ] Корректный базовый класс или явный `CommandExecutor` с единым стилем обработки ошибок
- [ ] Права из `plugin.yml`
- [ ] Сообщения из конфигурации
- [ ] Валидация аргументов
- [ ] TabCompleter при необходимости

## Ожидаемые эффекты от доведения до единого стиля

- Меньше дублирования проверок отправителя и прав
- Проще добавлять новые команды
- Проще сопровождать код при едином паттерне

---

**Следующие шаги (по желанию):** выбрать 2–3 самых «шумных» по коду команды → перевести на `PlayerCommand`/`AdminCommand` → добавить минимальные тесты или чек-лист ручной регрессии.
