package com.airdata.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract Base Class representing generic persistent entities.
 * Demonstrates:
 * 1. Abstraction (cannot be directly instantiated)
 * 2. Encapsulation (protected/private state with public accessors)
 * 3. Inheritance (serves as the superclass for domain models)
 */
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    // Default constructor
    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Parameterized constructor (Constructor Overloading)
    public BaseEntity(String id) {
        this();
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void markUpdated() {
        this.updatedAt = LocalDateTime.now();
    }

    // Abstract method to be implemented by child classes (Abstraction & Polymorphism)
    public abstract String getDisplayName();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
