package com.airdata.model;

import java.util.Objects;

/**
 * Domain entity representing an Airport.
 * Demonstrates:
 * 1. Encapsulation (Private fields with getter/setter methods)
 * 2. Inheritance (Extends BaseEntity)
 * 3. Polymorphism & Method Overriding (toString, equals, getDisplayName)
 */
public class Airport extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String iataCode;
    private String name;
    private String city;
    private String country;

    public Airport() {
        super();
    }

    public Airport(String iataCode) {
        this(iataCode, iataCode, "", "");
    }

    public Airport(String iataCode, String name, String city, String country) {
        super(iataCode);
        this.iataCode = (iataCode != null) ? iataCode.trim().toUpperCase() : "";
        this.name = name;
        this.city = city;
        this.country = country;
    }

    public String getIataCode() {
        return iataCode;
    }

    public void setIataCode(String iataCode) {
        this.iataCode = (iataCode != null) ? iataCode.trim().toUpperCase() : "";
        this.id = this.iataCode;
        markUpdated();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        markUpdated();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
        markUpdated();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
        markUpdated();
    }

    @Override
    public String getDisplayName() {
        if (city != null && !city.isEmpty()) {
            return iataCode + " - " + city;
        }
        return iataCode;
    }

    @Override
    public String toString() {
        return iataCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airport airport = (Airport) o;
        return Objects.equals(iataCode, airport.iataCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iataCode);
    }
}
