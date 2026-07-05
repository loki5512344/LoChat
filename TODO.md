# LoChat Development TODO

> Last updated: 2026-07-05

---

## ✅ COMPLETED

### Memory Leaks
- [x] AutoMessageManager: save task reference, cancel in `stop()`
- [x] Event listeners: `HandlerList.unregisterAll(this)` in `onDisable()`
- [x] MuteHistoryManager: MAX_HISTORY_PER_PLAYER = 50

### Race Conditions
- [x] AsyncChatEvent: try-catch for Location in async
- [x] ConfigManager: `volatile` + `synchronized reload()`

### Compilation Fixes
- [x] EnhancedChatRenderer: `appearanceCfg` → `cfg.getAppearanceConfig()`
- [x] Replace deprecated `Registry.SOUNDS.match()` with `Registry.SOUNDS.get(NamespacedKey)`
- [x] Remove unused imports
- [x] Remove redundant `TabCompleter` from `RollCommand`

### Config Refactor
- [x] BaseConfig base class for all configs
- [x] MessagesConfig (renamed from HardcodedMessages)
- [x] Clean config.yml: 300+ → 44 lines
- [x] Thread-safety: volatile + synchronized

### Main Class Refactor
- [x] PluginInitializer — init logic
- [x] PluginShutdown — shutdown logic
- [x] LoChat: 185 → 108 lines

### Documentation
- [x] README bilingual (EN + RU)
- [x] CONTRIBUTING.md created
- [x] CODE_OF_CONDUCT.md bilingual
- [x] SECURITY.md bilingual
- [x] GitHub issue/PR templates bilingual
- [x] Config comments in English, no Spacelegacy palette

### Checkstyle
- [x] Google Java Style config (2-space indent, 100-char lines, Javadoc)
- [x] Checkstyle plugin wired into build.gradle.kts

### Cleanup
- [x] Removed `libs/lolib-3.0.0.jar` (unused)
- [x] Removed CODE_OF_CONDUCT.md, SECURITY.md
- [x] Added `bin/` to .gitignore
- [x] Configs rewritten: clean, English, no Russian/Spacelegacy

---

## 🟡 CODE QUALITY ISSUES (HIGH PRIORITY)

### 1. Max 3 Files Per Folder

| Folder | Files | Fix |
|--------|-------|-----|
| `config/` | **9** ⚠️ | Split into `config/chat/`, `config/mute/`, `config/filter/`, `config/general/` |
| `api/service/` | **8** ⚠️ | Keep interfaces flat or split into `api/service/chat/`, `api/service/mute/` |
| `core/service/` | **8** ⚠️ | Mirror the api split |

### 2. DRY: `AppearanceConfig.loadEmojiCache()` Called Twice

**File:** `config/AppearanceConfig.java:23-31`
- `init()` calls `super.init()` which calls `onLoad()` which calls `loadEmojiCache()`
- Then `init()` calls `loadEmojiCache()` again
- **Fix:** Remove the duplicate call in `init()`

### 3. SRP: `ConfigManager.java` (278 lines)

God class with 60+ methods across 10+ concerns:
- Config orchestration
- Chat settings (global, local, PM)
- Mention settings
- Clear chat state mutation
- Announcement settings
- Join/quit/death message settings

**Fix:** Split into `ChatConfig`, `PmConfig`, `MentionConfig`, `ClearChatConfig`, `AnnouncementConfig`, `CustomMessagesConfig`

### 4. OCP: `AdvancedMessageFilter.filterMessage()`

Hardcoded pipeline of 7 filters. Adding a new filter requires modifying this method.

**Fix:** Dynamic `List<Filter>` pipeline with registration

### 5. ISP: `MessagingService` (fat interface)

16 methods across 4 concerns: PM + Spy + Ignore + Persistence

**Fix:** Split into `PrivateMessageService`, `SpyService`, `IgnoreService`, `PersistableService`

### 6. DIP: Service Locator Anti-Pattern

`ServiceRegistry.get(Xxx.class)` used everywhere instead of constructor DI.

**Fix:** Pass required services via constructor parameters

### 7. DRY: 12 Commands Duplicate Constructor Pattern

Commands like `MsgCommand`, `ReplyCommand`, `IgnoreCommand`, `MuteCommand`, `BanCommand` etc. don't use `BaseCommand` hierarchy and repeat the same field/constructor/permission pattern.

**Fix:** Migrate all to `PlayerCommand` / `AdminCommand`

### 8. DRY: PM Send Logic Duplicated

`MsgCommand.java:48-59` and `ReplyCommand.java:47-57` have identical PM send code.

**Fix:** Extract to shared method/service

### 9. KISS: `MuteCommand.onCommand()` (~100 lines)

High complexity: argument parsing, flags, fallback logic, permission checks, broadcast logic, voice-mute.

**Fix:** Decompose into `MuteRequest` parser + `MuteExecutor` service

### 10. OOP: Leaked Internals

- `LoChat.getInstance()` — singleton abuse
- `LoChat` exposes 7+ internal components via public getters
- `ChatEventListener` casts to concrete `PlayerServiceImpl`
- `BaseConfig` has `protected` mutable fields

---

## 🔵 MEDIUM PRIORITY

### 11. Dual Message Configs

`MessageConfig.java` (root, path-based) and `MessagesConfig.java` (config/, typed getters) overlap.

**Fix:** Unify into single `MessageConfig`

### 12. Filter Classes Don't Share Interface

`core/filter/filters/*.java` are standalone. `CooldownFilter`/`MuteFilter` implement `MessageFilter` from api.

**Fix:** Make all filters implement `MessageFilter`

### 13. 5+ Services Repeat Load/Save/Persistence

`NickServiceImpl`, `PlayerServiceImpl`, `ChatServiceImpl`, `PunishmentServiceImpl` all have their own file I/O.

**Fix:** Shared persistence utility

### 14. Mute Duration Permissions Not Declared in plugin.yml

`lochat.mute.dur.5m`, `lochat.mute.dur.3h`, `lochat.mute.reason.*` — used in code/configs but missing from plugin.yml.

---

## ⚪ LOW PRIORITY

### 15. Performance: Aho-Corasick for Swear Filter

### 16. Discord Rate Limiter

### 17. Tests: Only 4 test files (150 main files)

### 18. `.project`, `.classpath`, `.settings/` should be gitignored

---

## 📋 KNOWN ISSUES

- Gradients may not display correctly on clients < 1.16
- Some filters may block legitimate messages (configurable in `config/filters.yml`)
