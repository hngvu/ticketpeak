# ADR-005 — Area Creation Removed từ Manifest Editor UI

**Date:** 2026-06  
**Status:** Accepted  
**Decider:** Hoàng Vũ  
**Plan:** 033-remove-area-object-creation-editor-web.md

---

## Context

Ops manifest editor có UI để create GA areas, RS areas, và decorative objects (stage box, text, shapes) trực tiếp từ sidebar. Sau Plan 031/032, GA areas không resize được đầy đủ và UX tạo area bị phức tạp. Quyết định đơn giản hóa editor.

## Decision

**Xóa toàn bộ creation UI** khỏi editor. Areas và objects được tạo qua seeding script hoặc ngoài editor. Editor chỉ còn: **select, drag, resize, edit properties, delete** các area/object đã tồn tại.

Tabs còn lại: Levels, Sections (lookup tables). Tabs Areas và Objects bị xóa.

## Consequences

- Editor đơn giản hơn, ít bug hơn
- Workflow tạo area mới: dùng seeding script → rồi vào editor để adjust vị trí
- `createArea`, `addStageObject`, `addLabelObject`, `addShapeObject` factory functions bị xóa — không recreate

## Reversal Condition

Nếu product yêu cầu in-editor area creation trở lại, cần plan riêng với acceptance criteria cho resize behavior trên tất cả 4 cạnh.
