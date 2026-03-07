# FixedWidthReader

A desktop app for viewing fixed-width text files as a readable table — without modifying them.

Fixed-width files store data using column indexes instead of delimiters. They are difficult to inspect when debugging bad data. FixedWidthReader takes a simple YAML config that maps column names to their widths and renders the file as a formatted table.

---

## Download

Pre-built installers are produced automatically by GitHub Actions on every push to `main`.

**[Download the latest installer from the Actions tab](https://github.com/josh-wykell/FixedWidthReader/actions/workflows/build-installers.yml)**

1. Click the most recent successful workflow run
2. Scroll to the **Artifacts** section at the bottom
3. Download the installer for your platform:
   - `FixedWidthReader-macOS` → `.pkg` (double-click to install)
   - `FixedWidthReader-Windows` → `.exe` (double-click to install)

> **Note:** Artifacts are retained for 90 days per GitHub's default. If you need a permanent release, create a GitHub Release and attach the installer files.

---

## Features

- GUI file selector — no command line required
- Saves and manages named YAML configs across sessions
- Frozen row-number column stays visible during horizontal scrolling
- Alternating row colors for readability
- Click a column header to highlight it; click again to deselect
- Sortable columns; **Reset Sort** restores original file order
- Include/Exclude filter bar to narrow down rows by column value
- Read-only table — the source file is never modified
- Column widths driven by the YAML config

---

## YAML config format

Create a `.yaml` file mapping column names to their widths in characters:

```yaml
firstName: 10
lastName:  15
birthDate: 8
```

On first run you will be prompted to browse for both your fixed-width file and a config file. The config is saved by name so you can reuse it on future runs.

---

## Building from source

Requires JDK 17+. A Gradle wrapper is included.

```sh
./gradlew run           # run the app directly
./gradlew build         # compile and package
./gradlew jpackageApp   # build a native installer for your current OS
                        # output → fwreader/build/jpackage/
```
