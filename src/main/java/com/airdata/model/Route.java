package com.airdata.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Domain class representing a flight route connecting two airports.
 * Demonstrates:
 * 1. Composition / Aggregation (Has an Origin Airport and Destination Airport)
 * 2. Encapsulation
 * 3. Immutable Value Object Pattern
 */
public class Route implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Airport origin;
    private final Airport destination;
    private final double distanceMiles;

    public Route(Airport origin, Airport destination, double distanceMiles) {
        this.origin = origin != null ? origin : new Airport("UNK");
        this.destination = destination != null ? destination : new Airport("UNK");
        this.distanceMiles = Math.max(0, distanceMiles);
    }

    public Airport getOrigin() {
        return origin;
    }

    public Airport getDestination() {
        return destination;
    }

    public double getDistanceMiles() {
        return distanceMiles;
    }

    public String getRouteCode() {
        return origin.getIataCode() + " -> " + destination.getIataCode();
    }

    @Override
    public String toString() {
        return getRouteCode() + " (" + (int) distanceMiles + " mi)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Objects.equals(origin, route.origin) &&
               Objects.equals(destination, route.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination);
    }
}
