# Checkstyle Configuration | Конфигурация Checkstyle

This project uses **Google Java Style** for code quality enforcement.
Проект использует **Google Java Style** для контроля качества кода.

---

## 🇬🇧 English

### Rules

Based on [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html):

| Category | Rule | Value |
|----------|------|-------|
| **Indentation** | Basic offset | 2 spaces |
| **Indentation** | Continuation indent | 4 spaces |
| **Line length** | Max characters | 100 |
| **File length** | Max lines | 600 |
| **Method length** | Max lines | 60 |
| **Complexity** | Cyclomatic complexity | ≤ 15 |
| **Parameters** | Max per method | 7 |
| **Nested if** | Max depth | 3 |
| **Nested try** | Max depth | 2 |

### Key Requirements

- **Spaces only** — no tabs
- **Braces** — required for `if`, `for`, `while`, `do`
- **Imports** — no wildcards, no unused imports, grouped ordering
- **Javadoc** — required on all public/protected methods and types
- **Naming** — camelCase, no abbreviations (except allowed: IO, URL, HTML, etc.)

### Running

```bash
# Full check
./gradlew check

# Only LoChat
./gradlew checkstyleMain
```

### Reports

After check, reports are available at:
- `build/reports/checkstyle/main.html`

### IntelliJ IDEA Setup

1. Install "CheckStyle-IDEA" plugin
2. Settings → Tools → Checkstyle
3. Add configuration: `config/checkstyle/checkstyle.xml`

---

## 🇷🇺 Русский

### Правила

Основаны на [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html):

| Категория | Правило | Значение |
|-----------|---------|----------|
| **Отступы** | Основной | 2 пробела |
| **Отступы** | Перенос строки | 4 пробела |
| **Длина строки** | Максимум | 100 символов |
| **Длина файла** | Максимум | 600 строк |
| **Длина метода** | Максимум | 60 строк |
| **Сложность** | Цикломатическая | ≤ 15 |
| **Параметры** | Макс. на метод | 7 |
| **Вложенность if** | Макс. глубина | 3 |
| **Вложенность try** | Макс. глубина | 2 |

### Ключевые требования

- **Только пробелы** — никаких табуляций
- **Скобки** — обязательны для `if`, `for`, `while`, `do`
- **Импорты** — без *-импортов, без неиспользуемых, группировка
- **Javadoc** — обязателен для всех public/protected методов и типов
- **Именование** — camelCase, без сокращений (кроме разрешённых: IO, URL, HTML и т.д.)

### Запуск

```bash
# Полная проверка
./gradlew check

# Только LoChat
./gradlew checkstyleMain
```

### Отчёты

После проверки отчёты доступны в:
- `build/reports/checkstyle/main.html`

### Настройка IntelliJ IDEA

1. Установите плагин "CheckStyle-IDEA"
2. Settings → Tools → Checkstyle
3. Добавьте конфигурацию: `config/checkstyle/checkstyle.xml`

### Disabling Checks | Отключение проверок

```java
// CHECKSTYLE:OFF
public class MyClass {
    // code without checks
}
// CHECKSTYLE:ON

// Per line:
private static final int MAGIC = 42; // CHECKSTYLE IGNORE MagicNumber
```
