# Checkstyle Configuration

Автоматическая проверка качества кода во время билда.

## Правила

### Размеры
- **Файл**: максимум 150 строк
- **Метод**: максимум 30 строк
- **Строка**: максимум 120 символов
- **Параметры**: максимум 5 параметров

### Сложность
- **Cyclomatic Complexity**: максимум 10
- **Вложенность if**: максимум 3 уровня
- **Вложенность try**: максимум 2 уровня

### Качество кода
- Нет дублирования строк (макс 2 повтора)
- Нет магических чисел (кроме -1, 0, 1, 2, 3, 4, 5, 10, 20, 100)
- Нет * импортов
- Нет неиспользуемых импортов
- Упрощение boolean выражений
- equals() и hashCode() вместе

### Форматирование
- Отступы: 4 пробела
- Скобки обязательны для if/for/while
- Один оператор на строку
- Пробелы вокруг операторов

## Запуск проверки

```bash
# Проверить весь проект
./gradlew check

# Проверить только LoChat
./gradlew checkstyleMain

# Проверить только LoHub
./gradlew :lohub:checkstyleMain

# Проверить только LoVelocity
./gradlew :lovelocity:checkstyleMain
```

## Отчеты

После проверки отчеты доступны в:
- `build/reports/checkstyle/main.html`
- `lohub/build/reports/checkstyle/main.html`
- `lovelocity/build/reports/checkstyle/main.html`

## Отключение проверки

Если нужно временно отключить проверку для конкретного файла:

```java
// CHECKSTYLE:OFF
public class MyClass {
    // код без проверки
}
// CHECKSTYLE:ON
```

Или для конкретной строки:

```java
private static final int MAGIC = 42; // CHECKSTYLE IGNORE MagicNumber
```

## Интеграция с IDE

### IntelliJ IDEA
1. Установить плагин "CheckStyle-IDEA"
2. Settings → Tools → Checkstyle
3. Добавить конфиг: `config/checkstyle/checkstyle.xml`
4. Активировать проверку в реальном времени

### VS Code
1. Установить расширение "Checkstyle for Java"
2. Настроить путь к конфигу в settings.json
