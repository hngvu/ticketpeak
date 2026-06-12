# ADR-002 — Section Replaces RSArea Naming

**Date:** 2026-06  
**Status:** Accepted  
**Decider:** Hoàng Vũ

---

## Context

Data model ban đầu dùng `RSArea` (Reserved Seating Area) như một entity riêng biệt. Khi mở rộng model để support cả GA và RS trong cùng một manifest, naming này gây nhầm lẫn: `RSArea` vừa là một "section" của venue vừa là một "area type".

## Decision

`Section` là khái niệm lookup (tên khu vực: Section A, Section B, Balcony Left) thuộc về `Level`. `RS Area` và `GA Area` là các entity riêng biệt đều có thể map vào một `Section`. Không dùng `RSArea` như tên entity nữa.

## Model sau refactor

```
Level → Section → [RS Area | GA Area]
PriceLevel → [RS Area | GA Area]
RS Area → Row → Seat
GA Area → (capacity only, no rows/seats)
```

## Consequences

- Naming rõ ràng hơn — `Section` = khu vực địa lý, `Area` = block ghế cụ thể
- API paths dùng `/rs-areas` và `/ga-areas` (không phải `/sections`)
- Mọi agent phải dùng naming mới; không tạo lại `RSArea` entity

## Reversal Condition

Không đảo ngược quyết định này nếu không có migration plan đầy đủ.
