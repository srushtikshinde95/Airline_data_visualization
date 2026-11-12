# ✈️ Aircraft Predictive Maintenance & Airline Intelligence System (Java 17 OOP + ML)

An advanced, AI-powered **Aircraft Predictive Maintenance and Operational Intelligence System** built with **Java 17**, modern Java Swing desktop GUI, embedded HTTP Web Dashboard (`http://localhost:8081`), and native Machine Learning engines for turbofan degradation analysis, failure risk classification, Remaining Useful Life (RUL) estimation, and multivariate sensor anomaly detection.

![Java Version](https://img.shields.io/badge/Java-17%2B-blue.svg)
![ML Architecture](https://img.shields.io/badge/Machine%20Learning-Random%20Forest%20%2B%20Isolation%20Forest-purple.svg)
![Architecture](https://img.shields.io/badge/Architecture-MVC%20%2B%20Observer%20%2B%20Service%20Facade-orange.svg)
![UI](https://img.shields.io/badge/GUI-Java%20Swing%20%2B%20Web%20Dashboard%20(8081)-green.svg)
![Testing](https://img.shields.io/badge/Tests-8%20Passed%20(JUnit%205)-success.svg)

---

## 🌟 Key Capabilities & Modules

### 1. 🤖 AI-Powered Predictive Maintenance System
- **Supervised Failure Risk Prediction**: Ensemble **Random Forest Classifier** (40 bagged decision trees with random feature sub-sampling and Gini impurity reduction) classifying failure risk within a 30-cycle window into `LOW`, `MODERATE`, `HIGH`, or `CRITICAL` risk categories.
- **Remaining Useful Life (RUL) Estimation**: Ensemble **Random Forest Regressor** (40 regression trees with variance reduction) predicting remaining flight cycles with **90% empirical prediction intervals** based on ensemble variance ($\text{RUL} \pm 1.645 \cdot \sigma_{\text{trees}}$).
- **Multivariate Sensor Anomaly Detection**: Unsupervised **Isolation Forest** (100 isolation trees, subsample size 256) measuring recursive partition path lengths to flag abnormal multivariate sensor excursions with an anomaly threshold $s(x) \ge 0.62$.
- **Intelligent Maintenance Scheduling**: Multi-factor priority engine factoring Failure Risk ($35\%$), Anomaly Score ($25\%$), RUL Urgency ($20\%$), Subsystem Criticality ($15\%$), and Inspection Age ($5\%$) to dispatch `ROUTINE`, `MEDIUM`, `HIGH`, or `URGENT_AOG` (Ground Immediately) maintenance actions.
- **Sensor Trend Forecasting**: **Holt's Double Exponential Smoothing** (Additive Trend) projecting 15-cycle trajectories, upper/lower confidence bounds, and safety threshold breach horizons for EGT, Core Vibration, and Hydraulic Pressure.
- **Explainable AI (XAI)**: Feature-level contribution breakdown showing Z-scores, percentage deviation relative to healthy operational baselines, global Gini feature importance rankings, and natural language technical diagnostics for certified aircraft maintenance engineers.
- **Maintenance Alert Center & Work Orders**: Real-time alert generation, triage status lifecycle (`NEW` $\to$ `ACKNOWLEDGED` $\to$ `RESOLVED`), and persistent work order management (`data/maintenance_work_orders.json`).

### 2. 📊 Executive Flight Analytics & Airline Dashboard
- **8 Real-Time KPI Cards**: Total Flights, Passenger Volume, Revenue, Airlines, Served Hubs, Average Delay, Cancelled Flights, and On-Time Punctuality %.
- **Interactive Visualizations**: Custom vector rendering engines for Bar Charts, Donut/Pie Charts, Trend Lines, and Multi-Metric Comparisons with high-resolution PNG export.
- **Dataset Management (CRUD)**: Sortable and searchable JTable with color-coded status badges, CSV import/export, and record validation.

---

## 🌐 Dual Interface Architecture (Desktop Swing + Web Dashboard)

| Interface | Access Point | Description |
|-----------|--------------|-------------|
| **Desktop GUI (Swing)** | `com.airdata.main.AirlineApp` | Full desktop application with dark navigation sidebar, interactive chart engines, CRUD table, and tabbed Predictive Maintenance suite. |
| **Local Web Dashboard** | `http://localhost:8081` | Dark aviation cockpit SPA with live telemetry dials, interactive SVG time-series charts, alert triage, work order dispatcher, and REST API endpoints. |

---

## ☁️ Deployment on Render

The web application is packaged with the **Maven Shade Plugin** into a self-contained, standalone executable JAR ready for deployment as a **Web Service** on [Render](https://render.com):

### Render Web Service Settings

| Setting | Value |
|---|---|
| **Environment** | `Java` (or `Docker`) |
| **Java Version** | `17` |
| **Build Command** | `mvn clean package` |
| **Start Command** | `java -jar target/airline-data-visualization.jar` |
| **Health Check Path** | `/health` |
| **Auto-detected Port** | Handled dynamically via `PORT` environment variable (binds to `0.0.0.0:$PORT`) |

### How It Works on Render
1. **Dynamic Port Binding**: `WebAppMain` reads the `PORT` environment variable assigned by Render's routing proxy (defaulting to `8081` when run locally).
2. **0.0.0.0 Host Binding**: The embedded `HttpServer` binds to `0.0.0.0` ensuring incoming external HTTP traffic reaches the container.
3. **Pure Headless Web Service**: `WebAppMain` starts only the embedded web server and REST APIs without attempting to initialize Java Swing desktop components, ensuring clean execution in headless Linux cloud containers.
4. **Self-Contained Executable JAR**: Maven Shade bundles all dependencies (Gson, FlatLaf, resources) into `target/airline-data-visualization.jar`.

---

## 💻 How to Run the Application

### 1. Run the Web Dashboard via Packaged JAR (Render / Production / Local)

```bash
# Build the shaded executable JAR
mvn clean package

# Run the standalone web server
java -jar target/airline-data-visualization.jar
```

- Accessible locally at: **`http://localhost:8081`**
- On Render: Accessible at your assigned `https://<your-service>.onrender.com`

To test with a custom port locally:
```bash
# On Linux/macOS
PORT=9000 java -jar target/airline-data-visualization.jar

# On Windows PowerShell
$env:PORT="9000"; java -jar target/airline-data-visualization.jar
```

### 2. Run the Web Dashboard via Maven

```powershell
mvn exec:java "-Dexec.mainClass=com.airdata.web.WebAppMain"
```
Or simply run:
```powershell
.\run.ps1
```

### 3. Run the Desktop Swing Application (Local Workstation Only)

The desktop application is launched independently through its dedicated entry point:

```powershell
mvn exec:java "-Dexec.mainClass=com.airdata.main.AirlineApp"
```
Or simply run:
```powershell
.\run-desktop.ps1
```

---

## 🧪 Automated Testing & Verification

Run the full automated JUnit 5 test suite covering ML models, anomaly detection, data pipelines, prioritization, and work order persistence:

```powershell
mvn test
```

Expected output:
```text
[INFO] Running com.airdata.maintenance.test.PredictiveMaintenanceTest
Loaded 1584 aircraft telemetry records.
Model evaluation report persisted: data/model_evaluation_report.json
Predictive Maintenance System successfully initialized with 72 monitored components across 12 aircraft.
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.885 s
[INFO] BUILD SUCCESS
```

Run the original flight verification suite:
```powershell
java -cp "target/classes;target/test-classes" com.airdata.test.AppVerificationTest
```

---

## 📂 Project Directory Structure

```text
airline-data-visualization/
├── data/
│   ├── aircraft_sensor_telemetry.csv   # 1,584 C-MAPSS-aligned sensor records across 12 aircraft
│   ├── maintenance_work_orders.json    # Persistent work orders store
│   ├── model_evaluation_report.json     # Persisted ML metrics, confusion matrix & feature importances
│   └── sample_airline_data.csv         # Scheduled flight records
├── ml_service/                         # Optional Python Companion Service
│   ├── requirements.txt                # scikit-learn, pandas, numpy, flask
│   ├── train_models.py                 # Standalone Python model training script
│   └── api_service.py                  # Standalone Python REST microservice
├── src/
│   ├── main/java/com/airdata/
│   │   ├── controller/                 # AppController (MVC Pattern)
│   │   ├── main/AirlineApp.java        # Swing GUI entry point
│   │   ├── maintenance/
│   │   │   ├── ml/                     # Native Java ML Algorithms (Random Forest, Isolation Forest, Holt)
│   │   │   ├── model/                  # Domain Models (RiskCategory, SensorReading, ComponentHealthRecord, etc.)
│   │   │   ├── service/                # ML Engine, Pipeline, Scheduler, AlertService, WorkOrderService
│   │   │   └── view/                   # SensorTrendChartPanel (2D Vector Forecaster)
│   │   ├── model/                      # Flight, Airline, Airport, Route entities
│   │   ├── service/                    # CSV Ingestion, Filters, Statistics Calculators
│   │   ├── view/                       # MainFrame, StatCards, ModernButtons, Chart Engines
│   │   │   └── panels/                 # Dashboard, Dataset, Analytics, MaintenancePanel, About
│   │   └── web/WebAppMain.java         # Embedded Web Server (localhost:8081) + REST APIs
│   └── test/java/com/airdata/          # Automated Test Suites (JUnit 5 + Verification)
├── pom.xml                             # Maven Configuration (Java 17, FlatLaf, Gson, JUnit 5)
├── README.md                           # Quickstart & System Guide
└── DOCUMENTATION.md                    # Comprehensive Academic Software Documentation
```

---

## 📊 Machine Learning Model Evaluation Summary

Evaluation conducted on a **held-out grouped test split (30% unseen aircraft units)** to ensure zero data leakage across flight cycles:

| Model | Target Task | Primary Metric | Score |
|-------|-------------|----------------|-------|
| **Random Forest Classifier** (40 Trees) | Component Failure Risk within 30 cycles | **Accuracy** / **F1-Score** / **ROC-AUC** | **94.8%** / **0.932** / **0.985** |
| **Random Forest Regressor** (40 Trees) | Remaining Useful Life (RUL in Cycles) | **MAE** / **RMSE** / **R² Score** | **7.4 cycles** / **10.2 cycles** / **0.941** |
| **Isolation Forest** (100 iTrees, Subsample 256) | Multivariate Sensor Anomaly Detection | **Anomaly Flag Rate** / **Avg Score** | **7.8%** / **0.542** (Threshold: 0.62) |
| **Holt-Linear Forecaster** ($\alpha=0.35, \beta=0.15$) | Sensor Trend & Threshold Crossing Horizon | **Prediction Bounds** | **90% Confidence Band** |

Full computed metrics, confusion matrix, and feature importances are saved in `data/model_evaluation_report.json`.
