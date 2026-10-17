# ✈️ Airline Data Visualization Software (Java OOP)

An interactive, modern desktop application for loading, analyzing, filtering, and visualizing airline datasets using core **Object-Oriented Programming (OOP)** principles in Java.

![Java Version](https://img.shields.io/badge/Java-17%2B-blue.svg)
![Architecture](https://img.shields.io/badge/Architecture-MVC%20%2B%20Observer-orange.svg)
![UI](https://img.shields.io/badge/GUI-Java%20Swing%20(Modern)-green.svg)
![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)

---

## 🌟 Key Features

- **📊 Executive Dashboard**: 8 real-time KPI statistic cards (Total Flights, Passengers, Revenue, Airlines, Airports, Average Delay, Cancelled Flights, On-Time Punctuality %).
- **📁 Dataset Management (CRUD)**:
  - Import CSV datasets with robust column mapping and error handling.
  - Sortable and searchable `JTable` with color-coded status badges.
  - Full record creation, editing, and deletion with field validation.
  - Export filtered or complete datasets to CSV.
- **📈 Interactive Visualizations**:
  - **Bar Charts**: Flights by Airline, Passengers by Airline, Revenue by Airline, Top 10 Busiest Routes.
  - **Pie / Donut Charts**: Flight Status Distribution, Passenger Market Share, Cancelled vs Completed.
  - **Trend Line Charts**: Daily Flights Volume, Revenue Trend, Average Delay Trajectory.
  - **Comparison Charts**: Side-by-side multi-metric benchmark (Volume vs Delay vs Punctuality).
  - **PNG Image Export**: Save any rendered chart directly to disk.
- **📑 Operational Analytics**: Airline performance scorecards, route rankings, and hub statistics.
- **ℹ️ Built-in Documentation**: Complete OOP architecture guide embedded in the application.

---

## 🏗️ Project Architecture (MVC)

```text
src/
 └── main/
      ├── java/
      │    └── com/airdata/
      │         ├── main/           # Application entry point (AirlineApp.java)
      │         ├── model/          # Domain Entities (Flight, Airline, Airport, Route, Status)
      │         ├── service/        # Business Logic (CSV Load/Export, Filter, Statistics)
      │         ├── controller/     # AppController & Observer Listeners
      │         ├── view/           # MainFrame & Card Containers
      │         │    ├── components/# StatCards, ModernButtons, FilterPanel, SearchField
      │         │    ├── chart/     # Custom 2D Vector Chart Engines (Bar, Pie, Line, Comparison)
      │         │    └── panels/    # Dashboard, Dataset, Visualization, Analytics, About
      │         └── util/           # DateUtils, FormatUtils, ThemeConstants, Exceptions
      └── resources/
           └── sample_airline_data.csv
```

---

## 💻 How to Run the Project

### Option 1: One-Click Scripts (PowerShell / Windows Batch)

**PowerShell:**
```powershell
# Compile the project
.\build.ps1

# Launch the application
.\run.ps1
```

**Command Prompt (cmd.exe):**
```cmd
# Compile the project
build.bat

# Launch the application
run.bat
```

### Option 2: Pure Java Terminal Commands

```bash
# 1. Compile all Java source files
javac -encoding UTF-8 -d bin src/main/java/com/airdata/*/*.java src/main/java/com/airdata/*/*/*.java

# 2. Copy sample dataset to classpath
copy src\main\resources\* bin\

# 3. Launch the application
java -cp bin com.airdata.main.AirlineApp
```

### Option 3: Standard Maven Build

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.airdata.main.AirlineApp"
```

---

## 🧪 Running the Verification Test Suite

An automated test suite is included to verify CSV parsing, statistical calculations, stream filtering, CRUD operations, and OOP polymorphism:

```powershell
# Compile and run test suite
javac -encoding UTF-8 -cp bin -d bin src/test/java/com/airdata/test/AppVerificationTest.java
java -cp bin com.airdata.test.AppVerificationTest
```

---

## 📚 Complete Project Documentation

Read the full 19-section academic documentation report in [DOCUMENTATION.md](file:///C:/Users/srushti/.gemini/antigravity/scratch/airline-data-visualization/DOCUMENTATION.md).
