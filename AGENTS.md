# Project Instructions: Tariff Analyzer

## 1. Статус проекта
Tariff Analyzer находится в активной разработке. Цель продукта и бизнес-логика уточняются, но все новые изменения должны сохранять принципы Clean Architecture и оставлять бизнес-правила явными, проверяемыми и покрываемыми тестами.

Текущий приоритет - MVP для проверки корректности тарификации SMS-платформы:
- проверить, корректные ли цены указал представитель SMS-платформы;
- определить корректный тип трафика для каждого SMS-сообщения;
- сравнить исходный тип трафика из файла с рассчитанным корректным типом;
- сформировать обогащенный CSV и отдельный лог обработки.

## 2. Бизнес-документация
Перед изменением бизнес-логики, импорта/экспорта файлов, генерации конфигурации, сопоставления шаблонов, расчета тарифов или валидации нужно прочитать:
- [Бизнес-требования](.agents/business-requirements.md)
- [Синтаксис SMS-шаблонов](.agents/template-syntax.md)

Если реализация уточняет или меняет бизнес-правила, обновляй эти документы вместе с кодом.

## 3. MVP-сценарий
Приложение должно поддерживать базовый сценарий:
1. При первом использовании пользователь загружает два CSV-файла в кодировке Windows-1251:
   - `message_templates.csv`
   - `tariff.csv`
2. Приложение генерирует человекочитаемую JSON-конфигурацию в папке с исполняемым файлом.
3. При последующих запусках приложение автоматически загружает эту JSON-конфигурацию.
4. При запуске приложение валидирует конфигурацию и сообщает о синтаксических ошибках и отсутствующих обязательных полях с точным местом ошибки.
5. При стандартном использовании пользователь загружает большой файл сообщений, структурно аналогичный `sample_files/full_msg.csv`.
6. Приложение обрабатывает файл и создает новый CSV, сохраняя исходное содержимое и добавляя расчетные колонки.
7. Приложение создает отдельный лог обработки для неопределенных шаблонов, конфликтов шаблонов, ошибок валидации и проблем обработки строк.

Файлы в `sample_files/` являются только примерами. Реальные файлы выбираются пользователем через интерфейс приложения, но их формат и колонки ожидаются аналогичными примерам.

## 4. Обязательные выходные колонки
Обработанный CSV должен сохранить все исходные колонки и добавить в конец:
- `стоимость по текущему типу`
- `правильный тип`
- `стоимость по правильному типу`
- `наличие расхождение типа`
- `определен ли шаблон`
- `есть ли конфликт шаблонов`
- `ошибки обработки`

Выходной CSV должен сохранить разделитель, порядок строк, исходные значения и кодировку входного файла. Для текущих примеров это CSV с разделителем `;` и кодировкой Windows-1251.

## 5. Tech Stack
- **Multiplatform:** Kotlin Multiplatform (KMP)
- **UI:** Compose Multiplatform (Desktop focus)
- **Design System:** Material 3 + [MaterialKolor](https://github.com/jordond/MaterialKolor) (dynamic themes)
- **Navigation:** Navigation 3 (`androidx.navigation3`)
- **Dependency Injection:** Koin (`io.insert-koin:koin-compose`)
- **Concurrency:** Kotlin Coroutines
- **Lifecycle:** AndroidX Lifecycle ViewModel (KMP version)
- **Data & Utils:**
    - Serialization: `kotlinx.serialization`
    - Date/Time: `kotlinx.datetime`
    - Settings: `multiplatform-settings`
    - Logging: Kermit

## 6. Архитектурные принципы

### Clean Architecture
Проект разделен на логические слои:
1. **Presentation Layer:** Compose-экраны, ViewModel, UI state, взаимодействие с file picker.
2. **Domain Layer:** use case, domain model, repository interface, правила валидации, сопоставление шаблонов, расчет тарифов. Domain layer не должен зависеть от UI и data-фреймворков.
3. **Data Layer:** CSV reader/writer, JSON persistence, реализации repository, доступ к файловой системе, local settings.

Бизнес-логика должна находиться в Domain layer. CSV-парсинг, JSON-сериализация и UI state не должны владеть правилами тарификации или сопоставления шаблонов.

### MVI
Для управления состоянием в Presentation layer используется MVI:
- **State:** единый источник истины для UI, обычно immutable data class.
- **Action:** события от пользователя или системы, обычно sealed interface.
- **Effect / Event:** одноразовые события, например навигация, диалоги, уведомления.

Для каждого экрана используется шаблон из 4 файлов:
- `[Feature]Screen.kt` - route-обертка, подключение ViewModel, сбор state/effect и навигационные callback.
- `[Feature]ScreenContent.kt` - stateless Compose UI, принимает только state и callbacks/actions.
- `[Feature]ViewModel.kt` - обработка action, обновление state и отправка effect.
- `[Feature]Contract.kt` - `State`, `Action`, `Effect`.

Composable-функции должны быть декомпозированы: одна composable - один файл. В `*ScreenContent.kt` оставляй только верхнюю stateless content-функцию экрана, а screen-specific UI-блоки выноси в подпакет `composables` рядом с экраном. Shared composable размещай в `sharedComposables` или `:designSystem` по назначению, также по одному composable на файл.

ViewModel экранов наследуются от `BaseViewModel<State, Action, Effect>`, если нет сильной причины для исключения. `BaseViewModel` отвечает за единый публичный API `state`, `effect`, `onAction(...)` и базовое Kermit-логирование action/state/effect. Навигация и snackbar/dialog события должны идти через `Effect`, а не выполняться напрямую из `ScreenContent`.

### Design System
В проекте используется Material 3. Готовые компоненты Material 3 имеют приоритет над кастомными: перед добавлением своего компонента нужно проверить список в [.agents/material3-components.md](.agents/material3-components.md).

Для иконок Material Symbols используется `composablehorizons/compose-icons`; перед добавлением или заменой иконок нужно прочитать [.agents/compose-icons.md](.agents/compose-icons.md) и проверить фактические имена иконок, не угадывая их.

Кастомные переиспользуемые UI-компоненты размещаются в отдельном модуле `:designSystem`. Они не должны содержать бизнес-логику, file picker, навигацию или обращения к data/domain слоям. Screen-specific layout допускается держать рядом с экраном в `sharedUI`.

Реализация темы приложения, цветовые схемы, дизайн-токены и переиспользуемые UI primitives должны находиться в `:designSystem`. Product/domain настройки темы, например выбранный пользователем `ThemeMode`, остаются в `sharedUI` и передаются в дизайн-систему как простые UI-параметры.

### Navigation
Навигация строится через Navigation 3 (`androidx.navigation3`). Route-ключи приложения должны быть явно типизированы, например sealed interface/data object, и храниться отдельно от UI content. Переходы инициируются из ViewModel через `Effect`, а изменение back stack выполняет route-обертка или корневой навигационный контейнер.

### Settings
Пользовательские настройки хранятся через `multiplatform-settings` в Data layer. Presentation обращается к настройкам через domain-интерфейс repository. Для настроек, влияющих на UI сразу после изменения, repository может отдавать `StateFlow`, синхронизированный с сохраненным значением.

### Large File Processing
Файл сообщений может быть больше 1 ГБ. Реализация должна обрабатывать его потоково и не загружать весь файл в память. Долгие операции должны отдавать прогресс, поддерживать отмену и возвращать ошибки через use case и UI state.

## 7. Development Guidelines For AI Agent
- **Common First:** бизнес-код и общий UI размещай в `sharedUI/src/commonMain`, если нет сильной причины для платформенной специфики.
- **Strict Typing:** избегай `Any`; используй явные domain model, value object, sealed class и sealed interface для состояний и результатов.
- **Dependency Injection:** регистрируй новые зависимости в Koin-модулях проекта.
- **Resources:** для строк, иконок и шрифтов используй Compose Resources, если это соответствует текущей структуре проекта.
- **Design System:** сначала используй готовые Material 3 компоненты из [.agents/material3-components.md](.agents/material3-components.md); для Material Symbols Icons следуй [.agents/compose-icons.md](.agents/compose-icons.md); кастомные переиспользуемые компоненты добавляй в `:designSystem`.
- **Composable Decomposition:** каждая composable должна находиться в отдельном файле; для screen-specific composable используй подпакет `composables` внутри пакета экрана.
- **Desktop Compatibility:** при изменениях в `commonMain` учитывай Desktop-поведение и работу с большими локальными файлами.
- **Streaming by Default:** для `full_msg.csv` и аналогичных файлов проектируй API так, чтобы обработка могла идти построчно.
- **Naming:** экраны называй `[Feature]Screen.kt`, content-файлы - `[Feature]ScreenContent.kt`, ViewModel - `[Feature]ViewModel.kt`, MVI-контракты - `[Feature]Contract.kt`.
