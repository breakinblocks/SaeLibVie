# Changelog

## 1.0.0 for Minecraft 1.21.1

First release.

- GUI toolkit: retained widget tree with local coordinates, themes driven by color tokens, five layouts,
  draggable and resizable windows, modal and popup layers, context menus, toasts, confirm dialogs and a
  color picker.
- Around forty widgets, including buttons, text fields, a multi-line text area, lists, paged grids, scroll
  panels, tab panels, progress bars, fluid gauges, graphs, entity views and a 3D viewport.
- Screen bases for plain screens, container screens and three-region forms, with parent-screen navigation,
  cursor shapes, double-click detection and deferred tooltips.
- HUD element registry with an in-game layout editor, saved to `config/saelibvie/hud.json`.
- Themes from code, resource packs and `config/saelibvie/themes.json`, switchable with `/saelibvie theme`.
- Utility layer: RGBA color type with palettes, math and chunk helpers, chat-format-code parser, custom
  named text colors, item and fluid keys, search terms and network codec helpers.
- NBT editor for items, block entities, entities and players through `/saelibvie nbtedit`, with a public
  response handler registry.
- JEI integration: ingredient under the mouse on any library screen, plus ghost ingredient targets.
- Menu helpers: typed `ContainerData` syncing, button id allocation and a generic menu action payload.
