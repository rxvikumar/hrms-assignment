package net.guides.springboot2.crud.model;

/**
 * Designation enum for construction workers.
 * Business constraint: these are the only valid roles on a construction site.
 */
public enum Designation {
    MASON,
    ELECTRICIAN,
    PLUMBER,
    SUPERVISOR,
    HELPER
}
