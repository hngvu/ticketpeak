# 031 — Manifest Area Editor — Web

## Description

Build an editable area layer inside the ops manifest editor so organizers can visually position and adjust GA/RS areas on the seating canvas to match venue diagrams like the provided reference image.

The current manifest screen already shows venue lookup data and seating content, but area-level visual editing is not exposed as a focused workflow. This plan adds direct manipulation for areas on the canvas and the supporting controls needed to keep their coordinates, sizing, and labels in sync with the manifest data.

## Acceptance Criteria

- [ ] Ops manifest editor shows GA and RS areas as editable visual blocks on the canvas.
- [ ] Users can select an area and edit its key properties from the UI.
- [ ] Users can drag areas on the canvas and persist the updated coordinates.
- [ ] Users can adjust area dimensions or shape where applicable, without breaking existing seats.
- [ ] Area labels and colors reflect the underlying manifest lookup data.
- [ ] The editor remains usable when a manifest has no areas yet.
- [ ] Existing manifest and seat editing flows continue to work after the change.

## Status

`done`

## Outcome

Implemented editable area geometry in the ops manifest editor. Area blocks now support persisted `x/y/width/height`, canvas selection, drag positioning, and form-based updates. The backend now upserts areas, rows, and seats, and preserves seat inventory status during layout saves. Validation passed with `npm run check` in `web/` and `./mvnw -q clean -Dtest=VenueControllerIT test` in `api/`.
