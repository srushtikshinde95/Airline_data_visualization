package com.airdata.model;

import java.util.Objects;

/**
 * Domain entity representing an Airline operator.
 * Demonstrates:
 * 1. Encapsulation & Data Validation
 * 2. Inheritance (Extends BaseEntity)
 * 3. Method Overriding
 */
public class Airline extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String code;
    private String name;
    private String country;
    private int fleetSize;

    public Airline() {
        super();
    }

    public Airline(String name) {
        this(name != null && name.length() >= 2 ? name.substring(0, 2).toUpperCase() : "XX", name, "Global", 0);
    }

    public Airline(String code, String name, String country, int fleetSize) {
        super(code != null ? code : name);
        this.code = code;
        this.name = (name != null) ? name.trim() : "";
        this.country = country;
        this.fleetSize = fleetSize;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        this.id = code;
        markUpdated();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = (name != null) ? name.trim() : "";
        markUpdated();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
        markUpdated();
    }

    public int getFleetSize() {
        return fleetSize;
    }

    public void setFleetSize(int fleetSize) {
        this.fleetSize = fleetSize;
        markUpdated();
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airline airline = (Airline) o;
        return Objects.equals(name, airline.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
