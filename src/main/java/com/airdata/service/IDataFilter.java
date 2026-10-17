package com.airdata.service;

import com.airdata.model.Flight;
import com.airdata.model.FlightStatus;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface defining contract for multi-criteria dataset filtering operations.
 */
public interface IDataFilter {
    List<Flight> applyFilter(List<Flight> sourceFlights, FilterCriteria criteria);

    /**
     * Filter criteria container encapsulating all filter fields.
     */
    class FilterCriteria {
        private String airline = "ALL";
        private String origin = "ALL";
        private String destination = "ALL";
        private LocalDate startDate;
        private LocalDate endDate;
        private FlightStatus status = null; // null means ALL
        private String searchQuery = "";
        private int minDelayMinutes = -1;

        public String getAirline() { return airline; }
        public void setAirline(String airline) { this.airline = airline; }

        public String getOrigin() { return origin; }
        public void setOrigin(String origin) { this.origin = origin; }

        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

        public FlightStatus getStatus() { return status; }
        public void setStatus(FlightStatus status) { this.status = status; }

        public String getSearchQuery() { return searchQuery; }
        public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }

        public int getMinDelayMinutes() { return minDelayMinutes; }
        public void setMinDelayMinutes(int minDelayMinutes) { this.minDelayMinutes = minDelayMinutes; }

        public boolean isDefault() {
            return (airline == null || airline.equalsIgnoreCase("ALL")) &&
                   (origin == null || origin.equalsIgnoreCase("ALL")) &&
                   (destination == null || destination.equalsIgnoreCase("ALL")) &&
                   startDate == null && endDate == null && status == null &&
                   (searchQuery == null || searchQuery.trim().isEmpty()) &&
                   minDelayMinutes <= -1;
        }

        public void reset() {
            this.airline = "ALL";
            this.origin = "ALL";
            this.destination = "ALL";
            this.startDate = null;
            this.endDate = null;
            this.status = null;
            this.searchQuery = "";
            this.minDelayMinutes = -1;
        }
    }
}
