# SaeLibVie

SaeLibVie is a shared library mod for NeoForge 1.21.1 (Java 21). It exists so that Breakin Blocks mods can stop re-implementing the same screens, overlays and helpers. It ships:

- A retained-mode **GUI toolkit** with themes, layouts, draggable and resizable windows, modal layers, context menus, a color picker, a multi-line text editor, HUD overlays with an in-game layout editor, and about forty ready-made widgets.
- A **utility layer**: an RGBA color value type with palettes, math and chunk helpers, a chat-format-code parser, custom named text colors, item and fluid keys, search term parsing, and network codec helpers.
- An **NBT editor** for items, block entities, entities and players, opened through `/saelibvie nbtedit`.
- **JEI hooks** so the recipe and usage keys work over any library screen, plus ghost ingredient drop targets.
- **Menu helpers**: typed `ContainerData` syncing, button id allocation, and a generic client-to-server menu action payload.

Version scheme is `<minecraft>-<mod>`, currently `1.21.1-1.0.0`. The jar is `saelibvie-1.21.1-1.0.0.jar`.

## Adding it to a mod

```gradle
repositories {
    maven { url = "https://maven.breakinblocks.com/releases" }
}
dependencies {
    implementation "com.breakinblocks.saelibvie:saelibvie:1.21.1-1.0.0"
}
```

Declare `saelibvie` as a required dependency in `neoforge.mods.toml`. JEI is optional; the plugin only activates when JEI is present.

## Building and running

```bash
./gradlew build          # jar in build/libs/
./gradlew test           # JUnit tests (foundation layer and widget dispatch)
./gradlew runClient      # dev client; set recipe_viewer=jei in gradle.properties to include JEI
./gradlew runServer      # dev server
```

Access transformers are in `META-INF/accesstransformer.cfg`, mixins in `saelibvie.mixins.json`. Both go through ModDevGradle and are declared in the mod metadata template under `src/main/templates`.

## Package map

| Package | Side | Contents |
| --- | --- | --- |
| `ui.core` | client | `Widget`, `Panel`, `UiRoot`, `Layout`, `LayoutData`, `Behavior`, `EditSession`, `LayerOptions`, `CursorType`, JEI ingredient types |
| `ui.layout` | client | `AbsoluteLayout`, `AnchorLayout`, `LinearLayout`, `GridLayout`, `FlowLayout` |
| `ui.behavior` | client | `DragBehavior`, `ResizeBehavior` |
| `ui.widget` | client | All widgets, overlays, dialogs, context menu, toasts, color picker |
| `ui.screen` | client | `SaeScreen`, `SaeContainerScreen`, `FormScreen`, `BusyScreen`, cursor applier |
| `ui.hud` | client | `HudRegistry`, `HudElement`, `RowsHudElement`, `HudLayer`, `HudEditScreen` |
| `ui.color` | client | `Theme`, `Themes`, `ColorToken`, `Skin`, `ThemeLoader`, `Colors` (packed int helpers) |
| `ui.render` | client | `UiGraphics`, `Painter` |
| `ui.geom` | client | `Rect`, `Insets`, `Size`, `Anchor`, `Align`, `Axis` |
| `ui.anim` | client | `AnimatedFloat`, `Easing` |
| `ui.util` | client | `ClientTasks`, `TextUtil`, `Modifiers`, `UiSounds`, `MenuButtons` |
| `color` | both | `Color`, `MutableColor`, `ColorTables` |
| `math` | both | `Bits`, `MathUtil`, `XZ`, `ChunkDimPos` |
| `text` | both | `FormatParser`, `StringUtil`, `TimeUtil`, `TooltipList`, `ComponentUtil`, custom text colors |
| `item` | both (`FluidTextures` client) | `ItemKey`, `FluidKey`, `ContainerKey`, `ItemStackSet`, `ModNames`, `CraftingRemainderSetter` |
| `util` | both | `Lazy`, `OptionalBoolean`, `ChainedBooleanSupplier`, `BooleanConsumer`, `MapUtil`, `SearchTerms`, `NbtUtil` |
| `net` | both | `NetUtil`, `SaeNetworking`, `UiActionPayload`, `UiActionHandler`, NBT edit payloads |
| `menu` | both | `MenuSync`, `ButtonIds` |
| `nbtedit` | screen client, handlers server | `NbtEditScreen`, `NbtEditInfo`, `NbtEditSessions`, `NbtResponseHandlers` |
| `command` | server | `/saelibvie nbtedit ...` |
| `integration.jei` | client | `SaeJeiPlugin` |
| `client` | client | `SaeLibVieClient`, `ClientUtil`, `CustomClickEvent`, `demo.DemoScreen` |
| `mixin` | both | Five small mixins (see below) |

## GUI toolkit

### Core model

| Piece | Purpose |
| --- | --- |
| `Widget` | Base class. Bounds relative to its parent, `visible`/`enabled` plus `visibleWhen`/`enabledWhen` suppliers, focus, hover with enter/exit hooks, alpha, per-widget theme override, declarative tooltips, `Behavior` list, `cursor()` shape hint, `ingredientUnderMouse()` for JEI, double-click hook, `id` lookup via `find(id)`. |
| `Panel` | Ordered children, a `Layout`, padding, scale, clipping, scroll offsets, chrome (`NONE`, `WINDOW`, `PANEL`, `INSET`), optional header text, background and foreground painter callbacks, mouse capture on press, `bringToFront`, `packToContent`, dirty-layout tracking. |
| `UiRoot` | Top of a tree. Owns the theme, the focused widget (click on nothing clears focus, Tab and Shift+Tab cycle), screen size, the layer stack, double-click detection (300 ms, same button and widget), Shift+Enter routing to `AcceptsShiftEnter` widgets, context menus, cursor resolution, ghost target collection, and the single deferred tooltip per frame. |
| `Layout` | Strategy applied to a panel's children. Constraints sit on the child through `LayoutData` (anchor, offset, relative position and size, fixed size, weight, fill, cross alignment, margin, grid cell and span, line break). |
| `Behavior` | Composable input handler attached to any widget: tick, mouse, key, double click, paint overlay. |
| `UiGraphics` | Wrapper over `GuiGraphics` with a translate/scale stack, alpha stack, theme stack, local-to-screen scissor conversion, fills, gradients, outlines, bevels, sprites, atlas sprites (fluids), items, text helpers (fit with ellipsis, wrap, header text) and tooltip requests. |
| `Painter` | Theme-aware primitives: window, panel, panel header, inset, slot, button with `ButtonState`, scrollbar, title bar, checkbox, text field, progress, stat bar, scrim, focus ring, hover and selection tints, resize grip, separator, vanilla chest background. |

Coordinates are always local: a widget paints from `(0,0)` to `(width,height)` and receives mouse events in that space. Panels translate for their children, so nesting, scrolling and scaled panels work without any widget knowing where it sits on screen.

The default `AbsoluteLayout` leaves a child's own bounds alone unless the child was added with explicit `LayoutData` (fill, anchor, relative size), in which case it is placed like `AnchorLayout` would. A dragged window therefore keeps its position while the tab panel inside it fills the content area.

Layouts:

- `AnchorLayout`: anchor plus pixel offset, or relative position and size, per child.
- `LinearLayout`: vertical or horizontal, gap, weights sharing leftover space, cross alignment and fill.
- `GridLayout`: fixed cell size or equal split, optional explicit cell and span, `GridLayout.slots(cols)` for 18 px inventory grids.
- `FlowLayout`: wrapping rows with line alignment.

### Screens

`SaeScreen` owns one `UiRoot` sized to the screen. Subclasses implement `build(UiRoot)`, which runs on every `init()` (including resize). It remembers the screen that was open before it (never the chat screen, and it looks through a `BusyScreen`), restores that screen and the raw cursor position on close, and offers `openIfNotCurrent()`, `openLater()`, `openAfter(Runnable)`, `returnToParent(boolean)`, `setParent(Screen)`. Escape and mouse button 3 go to the root first (a layer or context menu pops, a focused text field just loses focus) and then to `onEscape()`, which closes by default. `pauses`, `dimBackground` and `blurBackground` are fluent options.

`SaeContainerScreen<T>` does the same for `AbstractContainerScreen`. Its root covers the menu image rect; vanilla draws slots between the root's background pass and the layer and tooltip pass, so chrome sits under items while popups and tooltips sit above them. `installWindowBackground()` paints the themed window and title strip, `sendButton(id)` sends a vanilla menu button click.

`FormScreen` hosts a `FormWindow`: a top region (title, optional close button, optional search box), a scrolling main region and a bottom region with Accept and Cancel. Shift+Enter accepts, Escape runs the cancel path. `formBounds()` chooses the window size (75% by 90% of the screen by default).

`BusyScreen` is an optional busy overlay with thread-safe status lines and an indeterminate bar; `finish()` closes it from any thread.

```java
public class MyScreen extends SaeScreen {
    public MyScreen(Screen parent) {
        super(Component.literal("My Screen"), parent);
    }

    @Override
    protected void build(UiRoot root) {
        Window window = new Window(getTitle()).resizable(200, 120, 500, 400);
        window.setBounds(new Rect(0, 0, 260, 180));
        window.layout(LinearLayout.vertical(4));
        window.add(new Label(Component.literal("Hello")).title());
        window.add(new Button(Component.literal("Close"), window::close).size(60, 14));
        openCentered(window);
    }
}
```

```java
public class GrinderScreen extends SaeContainerScreen<GrinderMenu> {
    public GrinderScreen(GrinderMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 200, 220);
    }

    @Override
    protected void build(UiRoot root) {
        installWindowBackground();
        root.add(new SlotDecor(20, 139, 9, 3));
        root.add(new SlotDecor(20, 197, 9, 1));
        Panel process = new Panel(new Rect(8, 18, 112, 50)).chrome(Panel.Chrome.PANEL).header(Component.literal("Process"));
        process.add(new ProgressBar(new Rect(42, 26, 28, 9), menu::progress));
        process.add(new FluidGauge(new Rect(80, 14, 16, 32), menu::lubricant, 8000));
        root.add(process);
        root.add(new Graph(new Rect(118, 84, 68, 32), menu::power));
        root.add(new Button(Component.literal("Mode"), () -> sendButton(GrinderMenu.MODE_BUTTON)).bounds(8, 100, 50, 14));
    }
}
```

### Layers, dialogs and menus

- `root.pushLayer(widget)` opens a modal layer with scrim that closes on outside click and Escape; `pushLayer(widget, LayerOptions)` controls each flag and can attach an `EditSession`.
- `pushLayerAt(widget, anchor, dx, dy, options)` and `pushLayerAtMouse(widget, options)` place a layer next to a widget or at the mouse and keep it at least 10 px inside the screen.
- `EditSession` delivers one accept-or-cancel result exactly once; a layer that closes by any other route cancels its session.
- `Confirm.ask(root, title, description, answer)` is an in-screen yes/no dialog (Enter or Y, Escape or N).
- `root.openContextMenu(items)` and `openContextMenu(items, anchor)` show a `ContextMenu` built from `MenuItem` entries: titles, separators, actions with sprite or item icons, disabled rows, rows that ask for confirmation, rows that keep the menu open, and sub-menus. Menus split into columns when taller than the screen.
- `Toasts.info` and `Toasts.error` add themed toasts to the vanilla toast system.
- `TextOverlay` (single line with Accept and Cancel) and `TextAreaOverlay` (multi line, Shift+Enter accepts) are the standard editing popups.

### Widgets

Controls: `Button` (label, sprite, item or fluid icon, left and right press, `selectedWhen`, flat mode, hand cursor), `ToggleButton`, `CycleButton<T>`, `ConfirmButton` (two stage with timeout), `MultiStateButton`, `Checkbox` (radio variant, `RadioGroup`), `Slider`, `NumberStepper` (base/shift/ctrl steps or a custom `StepFunction`), `SegmentedControl<T>`, `Dropdown<T>`, `TabPanel`.

Text: `TextField` (filter, validator with invalid color, hint, max length, dirty tracking, `seed()` that refuses to clobber typing, wheel value hook, commit on Enter and focus loss, Escape clears focus, I-beam cursor), `IntField` (bounded, snaps out-of-range text, wheel steps), `SearchBox`, `TextArea` (vanilla `MultilineTextField` model, click and drag selection, double-click word selection, keeps its caret visible inside a `ScrollPanel`), `Label`, `TextBlock` (wrapped).

Display: `KeyValueRows`, `ProgressBar` (procedural or textured, both axes, blocked overlay), `StatBar`, `FluidGauge`, `Graph` (ring buffer, auto scaled), `ItemView`, `Image`, `Separator`, `Spacer`, `SlotDecor`, `SlotOverlay` (tint locked slots), `HoverArea`.

Containers: `Panel`, `ScrollPanel` (one axis, `reserveBarSpace` policy, `reveal(rect)`), `FilteredListPanel` (substring filter over child labels with `visibleCount()`), `CollapsibleGroups` (grouped rows with headers, expand and collapse all, `+`/`=`/`-` hotkeys, collapse state kept across rebuilds), `ListView<T>` (virtualised rows, filter, selection, row tooltips, double click, keyboard), `PagedGrid<T>` (row-scrolling grid with selection, double click, fluid cell renderer, JEI ingredient lookup), `Window` (title bar, close, collapse, drag, resize), `FormWindow`.

3D: `EntityView` (mouse follow, auto spin, or orbit drag) and `Viewport3D` (scissored viewport with orbit and zoom; `Viewport3D.ofBlock` renders a block state).

Color: `ColorPicker` (hue/saturation disc, brightness bar, optional alpha bar with checkerboard, hex field, palette menu, 16 swatches, recent colors, live preview, Shift+Enter accepts and Escape restores the original) with `Palette` presets for chat colors, dye colors, Nord, reds, greens and blues.

Every value-driven widget reads live state through suppliers; most offer `bind(supplier, consumer)` for two-way use.

### Behaviors

```java
panel.behavior(DragBehavior.create().handle(w -> new Rect(0, 0, w.width(), 12)).snap(4));
panel.behavior(ResizeBehavior.create().min(80, 60).max(400, 300));
```

Both clamp to the parent, raise the widget on grab, and call back on start, drag and end. Any widget can add its own `Behavior` implementation.

### Themes

Every color comes from a `ColorToken` looked up on the active `Theme`. Themes also carry integer metrics (border, header height, title bar height, padding, gap, row height, button height, scrollbar width, slot size), flags (text shadow, uppercase headers), an opacity, a `FLAT` or `BEVEL` style, and an optional `Skin` that maps chrome keys to nine-slice sprites. Built-in themes: `saelibvie:dark` (default), `midnight`, `blood`, `vanilla`.

```java
Theme mine = Themes.DARK.derive(MyMod.id("mine"))
        .color(ColorToken.ACCENT, 0xFFE0B458)
        .color(ColorToken.HEADER_BG, 0xFF3A2A10)
        .build();
Themes.register(mine);
```

Use a theme per root (`new UiRoot(theme)` or the `SaeScreen` constructor), per widget (`widget.theme(theme)`), or globally with `/saelibvie theme <id>`.

Resource packs add or override themes at `assets/<namespace>/saelibvie/themes/<name>.json`:

```json
{
  "parent": "saelibvie:dark",
  "style": "flat",
  "opacity": 0.95,
  "colors": { "accent": "#FF35F1D7", "window_bg": "#F016191C" },
  "metrics": { "header_height": 11, "uppercase_headers": true },
  "skin": { "button": "minecraft:widget/button" }
}
```

Players can override any theme in `config/saelibvie/themes.json` (`default`, `opacity`, and an `overrides` map keyed by theme id).

### HUD elements

```java
HudRegistry.register(MyMod.id("altar_info"),
        new RowsHudElement<>(110, 2, RowsHudElement.lookedAtBlockEntity(AltarBlockEntity.class))
                .row(a -> Component.literal("Tier " + a.getTier()))
                .row(a -> Component.literal(a.getBlood() + " / " + a.getCapacity())),
        HudRegistry.Placement.at(0.01f, 0.30f));
```

Any `HudElement` (a `Panel` with a `shouldRender` gate) can be registered with a default placement: an anchor, a fractional screen position, a pixel offset, a scale and an enabled flag. Placements are saved to `config/saelibvie/hud.json`. `/saelibvie hud` or the "Edit HUD Layout" key opens the editor: drag to move, right-click to toggle, scroll to scale, with Defaults, Save and Cancel.

### Cursor shapes, tasks and double clicks

`Widget.cursor()` returns a `CursorType` hint; text fields request an I-beam, buttons a hand, scrollbars and window edges the resize shapes. A client tick hook applies the resolved shape through GLFW and resets it when the screen closes. `ClientTasks.later(Runnable)` queues work for the next client tick in insertion order. `UiRoot` detects double clicks and routes them to `Behavior.mouseDoubleClicked` and then `Widget.onMouseDoubleClicked`.

## Menu helpers

`MenuSync` wraps `ContainerData` with typed values: 32-bit ints split across two slots, fixed-point floats, booleans, enums, bitmasks and fluid stacks. Build it once in the menu constructor with `MenuSync.forSide(level.isClientSide())`, add values, and pass `sync.data()` to `addDataSlots`.

`ButtonIds` allocates vanilla button ids (`ids.next()`, `ids.range(6)`, `ids.range(Direction.class)`) so the client and `clickMenuButton` share the same id space. `MenuButtons.send(menu, id)` sends one.

For richer actions, implement `UiActionHandler` on the menu and call `SaeNetworking.sendAction(menu, action, value, text)` from the client. The payload carries the container id and is only delivered to the matching open menu.

## Utility layer

- `color.Color` is an immutable RGBA value type with an `EMPTY` singleton (any fully transparent color collapses to it), canonical black and white alpha tables, `fromString` (`#RRGGBB`, `#AARRGGBB`, named words such as `light_blue`), `fromJson`, JSON, string and stream codecs, `withTint`, `lerp`, `addBrightness`, HSB conversion and a `MutableColor` subclass. `ColorTables` holds the 16 chat formatting colors and a 256 color palette.
- `math.Bits` (bit flags, packing, big-endian readers and writers, UUID lists), `XZ` and `ChunkDimPos` (chunk and region coordinates, region file names), `MathUtil` (interpolation, mapping, modulo, square spiral iteration).
- `text.FormatParser` parses `&`-style format codes with `&#RRGGBB`, the `&z` rainbow code and `{substitutions}`; `StringUtil` (id normalisation, number formatting with `K`/`M`/`B`, snake and camel case, property parsing), `TimeUtil`, `TooltipList`, `ComponentUtil` (`hotkeyTooltip`, `translatedDimension`, `withLinks`). Custom named text colors are collected through `RegisterTextColorsEvent` and resolve in JSON text; the library registers `saelibvie:rainbow`.
- `item.ItemKey`, `FluidKey` and `ContainerKey` are hash keys (item and components, fluid and components, double chest halves in either order); `ItemStackSet` is an insertion-ordered set keyed that way; `ModNames` resolves display names by namespace; `FluidTextures` looks up still sprites and tints; `CraftingRemainderSetter.set(item, remainder)` assigns a crafting remainder after registration.
- `util.Lazy`, `OptionalBoolean`, `ChainedBooleanSupplier`, `BooleanConsumer`, `MapUtil`, `SearchTerms` (`@mod`, `#tag` and plain terms, all must match), `NbtUtil.getSizeInBytes`.
- `net.NetUtil`: `registerC2S`/`registerS2C` (server-side S2C registration without a client handler), channel-aware `sendTo`/`sendToAll`, an enum stream codec, and 7, 8 and 9 argument composite stream codecs.
- `client.ClientUtil`: `IS_CLIENT_OP`, `runLater`, `execClientCommand`, `handleClick(scheme, path)` for `http`, `https`, `file`, `command`, `static_method` and custom schemes (`CustomClickEvent`), and `registryAccess()`.

## NBT editor and commands

`/saelibvie nbtedit block <pos>`, `entity <entity>`, `player <player>` and `item` (all permission level 2) collect the target's data and info lines on the server, remember the session per player, and open `NbtEditScreen` on the client. The screen is a three-region form at 75% by 90% of the screen: tree rows (10 px, indented, per-type sprite, item stack preview for compounds that decode as item stacks), a toolbar with delete, rename, edit value and per-type add buttons, and copy, collapse all and expand all on the right. Leaf values are edited in a text overlay and written back as the same tag type, clamped to that type's range. `+`, `=`, `-` and Ctrl+C are hotkeys.

On accept the client sends the edited compound back (if under 30000 bytes); the server checks permission level 2 and that the info matches the open session, then dispatches on `info.type` to `NbtResponseHandlers`. Built-in handlers cover `item`, `block`, `player` and `entity`; other mods register their own type strings with `NbtResponseHandlers.register`. `NbtEditInfo` builds info compounds and the blue `key: value` info lines in the same format so mods can open the editor for their own targets.

## JEI integration

`SaeJeiPlugin` registers a global GUI handler that turns `UiRoot.ingredientUnderMouse()` into a clickable ingredient (item stacks and fluid stacks), so JEI's recipe and usage keys work over `ItemView`, `FluidGauge`, `PagedGrid` cells and NBT rows that look like item stacks. It also registers a ghost ingredient handler for every widget implementing `GhostIngredientTarget`.

## Mixins and access transformers

- `ResourceLocationMixin`: when `StringUtil.ignoreResourceLocationErrors` is set, the namespace and path validation predicates accept any string.
- `TextColorMixin`: `TextColor.parseColor` consults the custom color registry first.
- `CompoundContainerAccessor`: exposes the two halves of a double chest container.
- `ItemCraftingRemainderMixin`: makes the crafting remainder settable.
- `MultilineTextFieldAccessor` (client): exposes the selection cursor used by `TextArea`.

Access transformer entries open `MultilineTextField$StringView`, make `TextColor` extendable with its constructor and `NAMED_COLORS` accessible, and expose `Minecraft.clientTickCount` for the rainbow color phase.

## Commands

Client: `/saelibvie demo` (a screen that exercises every widget), `/saelibvie hud`, `/saelibvie theme <id>`, `/saelibvie themes`, `/saelibvie reload` (theme config and HUD layout).

Server: `/saelibvie nbtedit block|entity|player|item`.

## Files written on disk

- `config/saelibvie/themes.json`: default theme, global opacity, per-theme overrides.
- `config/saelibvie/hud.json`: HUD element placements.

## Layout of this repository

```
src/main/java/com/breakinblocks/saelibvie/   sources (see package map)
src/main/resources/assets/saelibvie/         lang file and NBT type sprites
src/main/resources/META-INF/                 access transformer
src/main/resources/saelibvie.mixins.json     mixin config
src/main/templates/META-INF/                 neoforge.mods.toml template (expanded at build time)
src/test/java/                               JUnit tests
```
