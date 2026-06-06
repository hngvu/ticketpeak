# 033 — Remove Area/Object Creation from Manifest Editor — Web

## Description

The ops manifest editor currently lets organizers create GA areas, RS areas, and decorative layout objects (stage boxes, text labels, rect/circle shapes) directly from the sidebar. After a previous bug report (GA areas could not be resized on all four edges — `feat(api/offer)` 031/032 left only the bounding-box drag working), and to keep the editor focused on the core seating workflow, the **creation** UI for areas and objects will be removed.

Existing areas and objects already on the manifest will still render on the canvas and remain selectable / movable / resizable / deletable. Only the **creation entry points** are removed.

## Scope

In scope:
- Sidebar "Areas" tab creation form (GA / RS toggles, name + capacity / rows + seats-per-row + layout shape).
- Sidebar "Objects" tab creation buttons (Add Stage Box, Add Text Label, Add Rectangle, Add Circle) and the related form fields for creating them.
- Canvas toolbar "Add Area" button.
- `activeTab` values `areas` and `objects` (and the `Levels / Sections / Areas / Objects` tab bar).
- Factory functions: `createArea`, `addStageObject`, `addLabelObject`, `addShapeObject`.
- The corresponding "no selection" empty-state hint under the Areas tab (the "Select an area on the canvas" placeholder).

Out of scope (kept intact):
- Canvas rendering of existing GA / RS areas and layout objects.
- Selection, drag, resize, and delete of existing areas / objects.
- Per-area / per-object property editing forms that already show when an item is selected (x, y, width, height, text, color, opacity, curvature slider, etc.).
- Levels, Sections, and Price Levels tabs and their creation flows.
- The Floor-Edit mode itself (still needed for dragging / resizing existing areas and objects).
- `saveLayout` upsert logic for areas and objects (no API change).

## Acceptance Criteria

- [ ] The sidebar tab bar no longer shows "Areas" or "Objects" — only the tabs that remain in scope (e.g. Levels, Sections, and anything else still present).
- [ ] There is no UI path in the editor to create a new GA area, RS area, stage box, text label, rect shape, or circle shape.
- [ ] The canvas toolbar no longer shows the "Add Area" button.
- [ ] An area or object that already exists on the manifest still renders on the canvas, can be selected, dragged, resized via the Konva Transformer, edited through its sidebar form, and deleted.
- [ ] `saveLayout` still persists moves, resizes, and edits to existing areas and objects.
- [ ] The page compiles with `npm run check` and `npm run lint` in `web/` with no new errors.

## Status

`done`

## Outcome

- Removed the `Areas` and `Objects` tab buttons from the editor sidebar (only `Levels` and `Sections` remain visible).
- Deleted the area creation form (RS/GA toggles, name/level/capacity/rows/seats/shape inputs) and the area list from the `Areas` tab body. The two existing-area editing panels (Reserved Seating + GA) are still rendered when an item is selected.
- Deleted the description text, the four `Add Stage Box / Add Text Label / Add Rectangle Shape / Add Circle Shape` buttons, and the current-objects list from the `Objects` tab body. The existing-object editing panel (position, size, color, opacity, text, font size, delete) is still rendered when an item is selected.
- Removed the `Add Area` button from the canvas toolbar.
- Removed factory functions `createArea`, `addStageObject`, `addLabelObject`, `addShapeObject`, and the now-unused `buildRectangularRows` helper.
- Removed the now-unused state: `areaNameInput`, `areaType`, `areaLevelId`, `areaPriceLevelId`, `areaCapacity`.
- Removed the empty `ADD SEATING BLOCK POPUP DIALOG` placeholder at the end of the file.
- File diff: -984 / +597 (387 net lines removed) in `+page.svelte`.
- `npm run check` reports 0 errors (36 pre-existing warnings, none from this change). `npm run lint` is clean for the edited file (2 unrelated files have pre-existing Prettier issues).
