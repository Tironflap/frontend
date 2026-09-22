# Frontend

**A highly customizable Android emulator launcher / frontend**

Turn your Android device (phone, tablet, or handheld) into a beautiful, fully themeable console-like experience for launching emulated games.

Inspired by the best of Daijishō, ES-DE, Beacon and Pegasus — but built from the ground up with **giant customization** as the #1 priority.

## Vision

- **Extreme theming** — almost every visual element is controllable via themes (colors, fonts, layouts, animations, transitions, particles, video backgrounds, music, etc.)
- **Multiple view modes** out of the box + fully custom layouts via theme JSON
- **Per-system and per-game overrides**
- Fast, modern, 100% Kotlin + Jetpack Compose
- Controller-first + excellent touch support
- Clean separation between core and themes so the community can create amazing themes

## Planned Features (Giant Customization Focus)

### Theming Engine
- JSON-based themes (easy to create & share)
- Full control over:
  - Colors, gradients, glassmorphism, blur
  - Typography & icon packs
  - Layouts (grid, list, carousel, console, immersive, free-form)
  - Animations & transitions (enter/exit, hover, focus, scroll)
  - Backgrounds (static, video, particle, shader)
  - Sounds & music per system / global
- Theme marketplace / GitHub theme repos later
- Live theme editor (long-term goal)

### Library & Systems
- Automatic ROM scanning + manual folders
- Metadata scraping (ScreenScraper, libretro, local cache)
- Box art, logos, fanart, videos, manuals
- Favorites, recently played, collections, tags
- Per-game emulator overrides & custom launch arguments
- Multi-disc / multi-file support

### Launching
- Intent-based launching of any installed emulator
- RetroArch core detection + standalone apps
- Custom command templates per system
- Android apps can also be added to the library

### Input & UX
- Full controller navigation (D-pad, analog, buttons)
- Touch gestures + on-screen overlays
- Customizable button mapping
- Optional Android launcher mode (replace home screen)

### Performance
- Efficient image loading & caching (Coil)
- Background scraping & indexing
- Smooth 60/120 fps UI even with large libraries

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: Clean-ish (data / domain / ui) + feature modules
- **Database**: Room
- **Images**: Coil
- **DI**: Hilt (or Koin)
- **Themes**: Custom JSON theme engine
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35+

## Project Status

**Early foundation** — architecture and theming engine scaffolding are being built.

This is not yet a usable frontend. The goal is to build a solid, highly extensible base first.

## Building

```bash
git clone https://github.com/Tironflap/frontend.git
cd frontend
# Open in Android Studio (Giraffe or newer recommended)
# Sync Gradle and run on device / emulator
```

Requirements:
- Android Studio
- JDK 17+
- Android SDK 35

## Contributing

Themes, ideas, code, and feedback are extremely welcome once the core stabilizes.

## License

TBD (likely GPL-3.0 or MPL-2.0 to keep it open and theme-friendly)

---

Made for people who want their emulator frontend to feel *theirs*.
