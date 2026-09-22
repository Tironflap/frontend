# Theme Engine Design (Giant Customization)

The core differentiator of this frontend is a powerful, community-friendly theme system.

## Goals

- Almost everything visual should be controllable by a theme
- Themes should be pure data (JSON + assets) so non-developers can create them
- Support both simple color swaps and radical layout changes
- Allow per-system and per-game theme overrides

## Proposed Theme Structure (v0)

```json
{
  "id": "neon-dreams",
  "name": "Neon Dreams",
  "author": "Someone",
  "version": 1,
  "colors": {
    "primary": "#7C4DFF",
    "secondary": "#03DAC6",
    "background": "#0D0D0F",
    "surface": "#16161A",
    "onBackground": "#E8E8E8",
    "accent": "#FF4081"
  },
  "typography": {
    "fontFamily": "default",
    "displaySize": 34,
    "titleSize": 20
  },
  "layout": {
    "home": "console",
    "library": "grid",
    "gridColumns": 4,
    "cardAspectRatio": 0.7,
    "showVideoPreview": true
  },
  "animations": {
    "focusScale": 1.08,
    "transitionDurationMs": 280,
    "scrollPhysics": "smooth"
  },
  "backgrounds": {
    "home": "assets/bg_video.mp4",
    "library": "assets/particles.json"
  },
  "sounds": {
    "select": "assets/select.wav",
    "confirm": "assets/confirm.wav"
  }
}
```

## Future

- Live theme preview
- Theme editor inside the app
- GitHub-based theme repository
- Shader / particle system support
- Full layout DSL (more advanced than simple grid/list)
