# ADR-001 — Convex Hull Geometry cho RS Area Border

**Date:** 2026-06  
**Status:** Accepted  
**Decider:** Hoàng Vũ

---

## Context

Khi render border của RS area trên Konva.js canvas, cách ban đầu dùng polygon cố định (bounding box của area). Vấn đề: ghế bị overflow ra ngoài border khi area có curvature hoặc ghế ở đầu/cuối row bị lệch.

## Decision

Dùng **convex hull** tính từ vị trí thực tế của từng seat (`positionX`, `positionY`) thay vì polygon hardcoded. Border được recompute sau mỗi lần thay đổi layout.

## Consequences

- Border luôn khớp với seat positions thực tế, không bao giờ overflow
- Chi phí tính toán tăng nhẹ (O(n log n) per area) nhưng chấp nhận được ở quy mô hiện tại
- Không cần lưu border coordinates vào DB — tính lại từ seats mỗi lần render

## Reversal Condition

Nếu số seat vượt 5,000 và convex hull recompute gây frame drop, cân nhắc cache hull geometry phía server.
