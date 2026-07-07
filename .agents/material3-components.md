# Material 3 Components

This is a practical reference for the main public `androidx.compose.material3` composables. It intentionally excludes tokens, defaults objects, theme internals, and low-level overload details.

## Project Usage Rule

Tariff Analyzer uses Material 3 as the default design system foundation. Agents should prefer ready-made Material 3 components from this document before adding custom UI.

Custom reusable components belong in the `:designSystem` module and must stay presentation-only:
- no business logic;
- no navigation;
- no file picker or filesystem access;
- no domain/data repository dependencies.

Screen-specific layout that is not reusable can stay near the screen in `sharedUI`.

## Buttons

- `Button`: Standard filled action button.
- `ElevatedButton`: Filled button with elevation.
- `FilledTonalButton`: Filled button with a toned secondary emphasis.
- `OutlinedButton`: Button with an outline and lower emphasis.
- `TextButton`: Lowest-emphasis text action button.
- `IconButton`: Icon-only action button.
- `FilledIconButton`: Filled icon-only action button.
- `FilledTonalIconButton`: Toned icon-only action button.
- `OutlinedIconButton`: Outlined icon-only action button.
- `FloatingActionButton`: Primary floating action button.
- `SmallFloatingActionButton`: Compact FAB variant.
- `LargeFloatingActionButton`: Large FAB variant.

## Cards

- `Card`: Standard surface card.
- `ElevatedCard`: Card with elevation.
- `OutlinedCard`: Card with an outline.

## Selection And Choice

- `Checkbox`: Binary selection control.
- `TriStateCheckbox`: Selection control with indeterminate state.
- `RadioButton`: Single-choice selection control.
- `Switch`: On/off toggle control.
- `Slider`: Single-value continuous control.
- `RangeSlider`: Two-handle range control.
- `SegmentedButton`: Segmented single-choice or multi-choice control.
- `SingleChoiceSegmentedButtonRow`: Row for one selected segment.
- `MultiChoiceSegmentedButtonRow`: Row for multiple selected segments.
- `AssistChip`: Lightweight helper chip.
- `FilterChip`: Filter selection chip.
- `InputChip`: Editable input chip.
- `SuggestionChip`: Suggestion chip for lightweight actions.
- `Badge`: Small status or count indicator.

## Inputs

- `TextField`: Filled text input.
- `OutlinedTextField`: Outlined text input.
- `SearchBar`: Search input surface.
- `DockedSearchBar`: Anchored search input variant.

## Pickers And Dialogs

- `AlertDialog`: Standard confirmation dialog.
- `BasicAlertDialog`: Low-level dialog container for custom content.
- `DatePickerDialog`: Dialog wrapper for date picking.
- `DatePicker`: Date selection component.
- `DateRangePicker`: Date range selection component.
- `TimePicker`: Time selection component.

## Navigation

- `NavigationBar`: Bottom navigation container.
- `NavigationBarItem`: Item inside a bottom navigation bar.
- `NavigationRail`: Vertical navigation container for wider layouts.
- `NavigationRailItem`: Item inside a navigation rail.
- `NavigationDrawer`: Standard navigation drawer container.
- `ModalNavigationDrawer`: Drawer that overlays content.
- `DismissibleNavigationDrawer`: Drawer that can be swiped away.
- `PermanentNavigationDrawer`: Always-visible drawer.
- `NavigationDrawerItem`: Item inside a navigation drawer.

## App Bars

- `TopAppBar`: Standard top app bar.
- `CenterAlignedTopAppBar`: Center-aligned top bar.
- `MediumTopAppBar`: Medium-height top bar.
- `LargeTopAppBar`: Large top bar.
- `BottomAppBar`: Bottom app bar for primary actions.
- `FlexibleBottomAppBar`: More adaptive bottom app bar variant.

## Surfaces And Layout

- `Divider`: Visual separator.
- `ListItem`: Standard row for lists and settings.
- `Snackbar`: Brief transient feedback message.
- `SnackbarHost`: Host for snackbar display.
- `TooltipBox`: Container for tooltip content and anchoring.
- `PlainTooltip`: Simple tooltip content.
- `RichTooltip`: Rich tooltip content with more structure.
- `ModalBottomSheet`: Modal sheet that overlays the screen.
- `BottomSheetScaffold`: Scaffold with persistent bottom sheet support.

## Tabs

- `Tab`: Standard tab.
- `LeadingIconTab`: Tab with a leading icon.
- `PrimaryTabRow`: Primary tab row container.
- `SecondaryTabRow`: Secondary tab row container.
- `TabRow`: Fixed tab row.
- `ScrollableTabRow`: Scrollable tab row.

## Sources

- [Android Developers package summary](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary)
- Context7 was used to cross-check the current package summary and keep this list aligned with the live API surface.
