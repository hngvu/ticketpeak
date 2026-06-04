# 032 � Manifest RS Area Workflow Enhancement � Web

## Description

Improve the ops manifest area editor so Reserved Seating (RS) areas start from a generated rectangular block, then support faster refinement with keyboard shortcuts, brush-based seat styling, and a direct curvature control. The goal is to make area layout creation feel closer to a Figma-style editing flow: create a usable block first, then fine-tune it with mouse and keyboard.

## Acceptance Criteria

- [x] Creating an RS area requires row count and seats-per-row inputs before generation.
- [x] The initial RS area renders as a rectangular block that users can drag and then refine.
- [x] Canvas shortcuts work for selection, pan, eraser, and delete without breaking existing controls.
- [x] Brush mode can apply seat styling across multiple seats with a mouse drag.
- [x] A curvature slider updates the selected RS area in real time.
- [x] Existing GA area creation and manifest save flows continue to work.
- [x] Relevant web checks pass after implementation.

## Status

`done`

## Outcome

- **RS area creation now embeds block generation**: When creating an RS area, the form includes inline inputs for number of rows, seats per row, row prefix, seat start number, and layout shape. The rectangular block (with support for Rectangle, Trapezoid, Diamond, and Staggered shapes) is generated immediately upon clicking "Create Area", eliminating the previous two-step workflow (create empty area → generate block separately).
- **Curvature slider added**: When clicking an RS area block, a real-time slider with range -200 to 200 appears in the right sidebar, allowing users to bend the seating block up or down with immediate visual feedback via the existing `applyCurvature()` function.
- **Keyboard shortcut labels**: The canvas toolbar now displays shortcut key hints (V for Select, Space for Pan, B for Brush, E for Eraser) on each tool button, making the Figma-style shortcuts discoverable. A "Del to remove" hint appears when seats are selected.
- **Brush mode price selector**: When Brush tool is active, a price level dropdown appears in the toolbar to let users choose which ticket tier to paint with.
- **Toolbar expansion**: Brush and Eraser tools are now directly accessible from the main canvas toolbar (previously only via keyboard shortcuts).
