-- ============================================================
-- Inventory invariant hardening
-- ============================================================


ALTER TABLE inventory_ga
    ADD CONSTRAINT ck_inventory_ga_non_negative_and_balanced
    CHECK (
        total >= 0
        AND available >= 0
        AND held >= 0
        AND sold >= 0
        AND available + held + sold = total
    );
