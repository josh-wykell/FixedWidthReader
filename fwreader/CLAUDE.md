# FixedWidthReader

A Groovy Swing desktop app that parses fixed-width text files and displays them in a table.

## What it does

1. Prompts user to select a fixed-width data file
2. Prompts user to select or create a named YAML config (saved configs are remembered across sessions)
3. Parses the fixed-width file using column widths defined in the config
4. Displays the result in a scrollable JTable with frozen row-number column, alternating row colors, column highlighting, sortable headers, and include/exclude filters

## Project structure

```
src/main/groovy/fwreader/app/
├── Main.groovy              # Entry point — orchestrates the three steps above
├── Reader.groovy            # Parses fixed-width file using YAML config
└── gui/
    ├── InputView.groovy     # Multi-step Swing UI for file/config selection
    └── TableView.groovy     # Swing JTable display of parsed data
```

## InputView launch flow

1. **Always shown** — FW file chooser (Browse + Next)
2. **Config selection** — branches based on saved configs:
   - No saved configs → config chooser (Browse + name field + Done)
   - Saved configs exist → list of named configs + "New Config" option

New configs are copied into `~/.fwreader/configs/` and registered by user-given name.
The manifest is stored at `~/.fwreader/fwFileManifest.ser` (`Map<String, File>`: name → copied File).

## YAML config format

Maps column names to their fixed widths (in characters):

```yaml
firstName: 10
lastName:  15
birthDate: 8
```

The `Reader` extracts fields by slicing each line with `substring(start, start + width)`.

## TableView features

- Frozen `#` row-number column (stays visible during horizontal scroll)
- Alternating row fill colors for readability
- Column highlight on header click (click again to deselect)
- Sortable columns (click header); **Reset Sort** button restores original order
- Filter bar: choose column, Include/Exclude mode, text field, Apply/Clear
- Read-only cells
- Column widths driven by YAML config character widths
- "Open New File..." menu item opens another file in a new window

## Build & run

A Gradle wrapper is present — use `./gradlew`:

```sh
./gradlew run           # run the app
./gradlew build         # compile and package
./gradlew test          # run tests (JUnit 5, no tests written yet)
./gradlew jpackageApp   # build macOS .pkg installer → build/jpackage/
```

## Tech stack

- Groovy 4.0.14
- Swing (Java standard library) for GUI
- SnakeYAML 1.29 for config parsing
- Shadow plugin 8.1.1 (fat JAR for packaging)
- Gradle build system
- JUnit 5 (test dependency present, no tests yet)

## Known issues / in-progress

- `settings.gradle` has `rootProject.name = 'untitled'` — likely should be renamed
- No tests exist yet
- `src/main/resources/data/fwFileManifest.ser` is a leftover artifact — no longer used, can be deleted
