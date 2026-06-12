# ADR-004 — Konva.js cho Seat Map Canvas

**Date:** 2026-06  
**Status:** Accepted  
**Decider:** Hoàng Vũ

---

## Context

Ops manifest editor cần interactive canvas với drag, resize, brush paint, keyboard shortcuts, và real-time curvature preview. Plain SVG inline trong Svelte không đủ để handle interactive transformations và Transformer handles.

## Decision

Dùng **Konva.js** cho tất cả canvas rendering trong ops manifest editor. Consumer-facing event seat map (read-only) có thể dùng SVG đơn giản hơn.

## Consequences

- Konva.js hỗ trợ Transformer, drag, hit detection out of the box
- Performance boundary: ~5,000 seats trước khi cần virtualization hoặc WebGL
- Svelte + Konva.js cần manual lifecycle management (destroy stage on component unmount)
- Không dùng thư viện wrapper — import Konva trực tiếp

## Performance Limit

Nếu manifest cần >5,000 seats, cần đánh giá:
1. Canvas-level virtualization (chỉ render viewport)
2. Hoặc chuyển sang WebGL (PixiJS, Three.js)

Đừng implement feature mới nếu nó làm tăng số lần iterate toàn bộ seat list synchronously.
