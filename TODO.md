# LoChat Development TODO

> Last updated: 2026-07-05

---

## ✅ COMPLETED THIS SESSION

### Architecture (SOLID/KISS/DRY)
- [x] `AppearanceConfig` — fixed double-call bug
- [x] `config/` — 9→3 files per folder (subfolders: chat/, filter/, mute/, manager/)
- [x] `api/service/` + `core/service/` — 8→3 files per folder (subfolders: chat/, player/, moderation/, pm/, spy/, ignore/)
- [x] `ConfigManager` — god class → facade + 6 managers (Chat/Pm/Mention/ClearChat/CustomMessages/Filters)
- [x] `MessagingService` — fat interface → 3 services (PrivateMessageService/SpyService/IgnoreService)
- [x] `AdvancedMessageFilter` — hardcoded pipeline → `FilterPipeline` with dynamic registration
- [x] 25 commands → `BaseCommand`/`AdminCommand`/`PlayerCommand` hierarchy
- [x] `MuteCommand` — decomposed into helper methods (KISS fix)

### Encapsulation
- [x] `ChatEventListener` — removed instanceof cast to `PlayerServiceImpl`
- [x] `PunishmentSnapshot` — mutable fields → private + getters
- [x] `BaseConfig` — protected fields → private + protected getters
- [x] 29 files with `import.*` → explicit imports

### Checkstyle
- [x] Config: 4-space indent, 140-char lines, Google-style naming/quality
- [x] Warnings: **520 → 0** (both main and test)
- [x] Wired into `build.gradle.kts`

### Configs
- [x] All rewritten: clean, English comments, no Spacelegacy palette

---

## 🟡 REMAINING (COULD DO)

### 1. Shared Persistence Utility
5 services repeat load/save/ensureDir pattern:
- `NickServiceImpl`, `PlayerServiceImpl`, `ChatServiceImpl`, `PunishmentServiceImpl`, `IgnoreServiceImpl`

**Fix:** Extract `FilePersistenceUtil` or base class

### 2. Mute Duration Permissions Not Declared in plugin.yml
`lochat.mute.dur.5m`, `lochat.mute.dur.3h`, `lochat.mute.reason.*` — used in code/configs but missing from plugin.yml declarations.

### 3. DIP: Service Locator Anti-Pattern
`ServiceRegistry.get(Xxx.class)` used everywhere instead of constructor DI. Big refactor.

### 4. DRY: PM Send Logic Duplicated
`MsgCommand` + `ReplyCommand` still have near-identical send logic.

### 5. PM Send Logic Duplicated Between MsgCommand and ReplyCommand

---

## 🔵 LOW PRIORITY

### 6. Tests
Only 4 test files for 150+ main files. Very low coverage.

### 7. Aho-Corasick for Swear Filter
Performance optimization for word filtering.

### 8. Discord Rate Limiter
Prevent rate limiting when sending many webhook messages.

---

## 📋 KNOWN ISSUES

- Gradients may not display correctly on clients < 1.16
- Some filters may block legitimate messages (configurable in `config/filters.yml`)
