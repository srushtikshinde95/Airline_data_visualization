package com.airdata.service;

import com.airdata.model.Flight;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Concrete implementation of IDataFilter using Java Streams and Functional Predicates.
 * Demonstrates:
 * 1. Polymorphic Predicate Composition
 * 2. High performance in-memory streaming
 */
public class DataFilter implements IDataFilter {

    @Override
    public List<Flight> applyFilter(List<Flight> sourceFlights, FilterCriteria criteria) {
        if (sourceFlights == null || sourceFlights.isEmpty()) {
            return Collections.emptyList();
        }
        if (criteria == null || criteria.isDefault()) {
            return sourceFlights;
        }

        Predicate<Flight> compositePredicate = flight -> true;

        // Airline Filter
        if (criteria.getAirline() != null && !criteria.getAirline().equalsIgnoreCase("ALL")) {
            compositePredicate = compositePredicate.and(f ->
                    f.getAirline().getName().equalsIgnoreCase(criteria.getAirline().trim()));
        }

        // Origin Airport Filter
        if (criteria.getOrigin() != null && !criteria.getOrigin().equalsIgnoreCase("ALL")) {
            compositePredicate = compositePredicate.and(f ->
                    f.getOrigin().getIataCode().equalsIgnoreCase(criteria.getOrigin().trim()));
        }

        // Destination Airport Filter
        if (criteria.getDestination() != null && !criteria.getDestination().equalsIgnoreCase("ALL")) {
            compositePredicate = compositePredicate.and(f ->
                    f.getDestination().getIataCode().equalsIgnoreCase(criteria.getDestination().trim()));
        }

        // Start Date Filter
        if (criteria.getStartDate() != null) {
            compositePredicate = compositePredicate.and(f ->
                    f.getDepartureDate() != null && !f.getDepartureDate().isBefore(criteria.getStartDate()));
        }

        // End Date Filter
        if (criteria.getEndDate() != null) {
            compositePredicate = compositePredicate.and(f ->
                    f.getDepartureDate() != null && !f.getDepartureDate().isAfter(criteria.getEndDate()));
        }

        // Flight Status Filter
        if (criteria.getStatus() != null) {
            compositePredicate = compositePredicate.and(f -> f.getStatus() == criteria.getStatus());
        }

        // Minimum Delay Filter
        if (criteria.getMinDelayMinutes() >= 0) {
            compositePredicate = compositePredicate.and(f -> f.getDelayMinutes() >= criteria.getMinDelayMinutes());
        }

        // Search Query (matches across Flight ID, Flight Number, Airline, Origin, Destination, Cancellation reason)
        String query = criteria.getSearchQuery();
        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim().toLowerCase();
            compositePredicate = compositePredicate.and(f ->
                    f.getFlightId().toLowerCase().contains(q) ||
                    f.getFlightNumber().toLowerCase().contains(q) ||
                    f.getAirline().getName().toLowerCase().contains(q) ||
                    f.getOrigin().getIataCode().toLowerCase().contains(q) ||
                    f.getDestination().getIataCode().toLowerCase().contains(q) ||
                    f.getCancellationReason().toLowerCase().contains(q)
            );
        }

        return sourceFlights.stream()
                .filter(compositePredicate)
                .collect(Collectors.toList());
    }
}
