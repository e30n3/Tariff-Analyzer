# Compose Icons

В проекте используется `composablehorizons/compose-icons` для иконок Material Symbols.

## Подключенный набор

По умолчанию используй подключенный Compose Multiplatform набор:

```kotlin
import com.composables.icons.materialsymbols.MaterialSymbols
```

Иконки берутся из `MaterialSymbols.Rounded`.

## Обязательное правило

Не угадывай Kotlin-имя иконки по названию из Google Fonts, Material Symbols или дизайна.

Перед добавлением новой иконки проверь фактическое имя в одном из источников:
- IDE autocomplete;
- сгенерированный API библиотеки в Gradle cache;
- классы внутри jar-файла артефакта `icons-material-symbols-rounded-cmp-jvm`.

Это важно, потому что библиотека сохраняет имена Material Symbols в стиле `snake_case`, а не всегда генерирует привычный `PascalCase`.

Примеры:

```kotlin
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Arrow_back
import com.composables.icons.materialsymbols.rounded.Settings

Icon(
    imageVector = MaterialSymbols.Rounded.Settings,
    contentDescription = stringResource(Res.string.settings_title),
)

Icon(
    imageVector = MaterialSymbols.Rounded.Arrow_back,
    contentDescription = stringResource(Res.string.back),
)
```

## Проверка через консоль

Если IDE/autocomplete недоступны, сначала найди jar:

```sh
find ~/.gradle/caches/modules-2/files-2.1/com.composables/icons-material-symbols-rounded-cmp-jvm -name "*.jar"
```

Затем проверь наличие класса и точное имя:

```sh
jar tf <path-to-icons-material-symbols-rounded-cmp-jvm.jar> | rg 'Arrow_back|Settings|Dark_mode|Upload_file'
```

Для проверки extension property можно использовать `javap`:

```sh
javap -classpath <path-to-icons-material-symbols-rounded-cmp-jvm.jar> com.composables.icons.materialsymbols.rounded.Arrow_backKt
```

Ожидаемый API выглядит так:

```text
getArrow_back(com.composables.icons.materialsymbols.MaterialSymbols$Rounded)
```