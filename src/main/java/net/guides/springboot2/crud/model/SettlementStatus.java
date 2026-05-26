package net.guides.springboot2.crud.model;

/**
 * Settlement status for overtime entries.
 * PENDING = not yet settled, SETTLED = included in a payroll run.
 * Once SETTLED, entries are immutable.
 */
public enum SettlementStatus {
    PENDING,
    SETTLED
}
