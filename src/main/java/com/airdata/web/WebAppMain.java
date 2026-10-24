package com.airdata.web;

import com.airdata.main.AirlineApp;
import com.airdata.maintenance.model.*;
import com.airdata.maintenance.service.MaintenanceCoordinator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Embedded Web Application Server serving the Aircraft Predictive Maintenance Dashboard
 * and REST APIs at http://localhost:8081, synchronized with the Java Swing desktop application.
 */
public class WebAppMain {

    public static final int PORT = 8081;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws Exception {
        // Initialize the Predictive Maintenance Coordinator
        MaintenanceCoordinator coordinator = MaintenanceCoordinator.getInstance();

        // 1. Start Embedded HTTP Server
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", new DashboardHtmlHandler());
        server.createContext("/api/fleet", new FleetApiHandler());
        server.createContext("/api/alerts", new AlertsApiHandler());
        server.createContext("/api/alerts/acknowledge", new AcknowledgeAlertApiHandler());
        server.createContext("/api/alerts/resolve", new ResolveAlertApiHandler());
        server.createContext("/api/work-orders", new WorkOrdersApiHandler());
        server.createContext("/api/work-orders/status", new WorkOrderStatusApiHandler());
        server.createContext("/api/model-performance", new ModelPerformanceApiHandler());
        server.createContext("/api/forecast", new ForecastApiHandler());
        server.createContext("/api/retrain", new RetrainApiHandler());

        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        System.out.println("==========================================================");
        System.out.println("  AIRCRAFT PREDICTIVE MAINTENANCE SYSTEM - WEB DASHBOARD  ");
        System.out.println("  Accessible locally at: http://localhost:" + PORT);
        System.out.println("==========================================================");

        // 2. Launch the Swing Desktop Application in background EDT
        new Thread(() -> {
            try {
                AirlineApp.main(new String[0]);
            } catch (Exception e) {
                System.err.println("Note: Headless or Swing startup notification: " + e.getMessage());
            }
        }).start();
    }

    // ==============================================================
    // HTML DASHBOARD HANDLER (SPA with Dark Aviation Cockpit Theme)
    // ==============================================================
    static class DashboardHtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
                return;
            }

            String html = generateDashboardHtml();
            sendResponse(exchange, 200, "text/html; charset=UTF-8", html);
        }

        private String generateDashboardHtml() {
            return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>AirData Predictive Maintenance Intelligence System</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;600&display=swap" rel="stylesheet">
  <style>
    :root {
      --bg-dark: #0b1329;
      --bg-surface: #111c44;
      --bg-card: #1b2559;
      --bg-elevated: #243072;
      --primary: #38bdf8;
      --primary-hover: #0284c7;
      --accent: #6366f1;
      --text-main: #f8fafc;
      --text-muted: #94a3b8;
      --border: #2b3674;
      --risk-low: #10b981;
      --risk-mod: #f59e0b;
      --risk-high: #f97316;
      --risk-crit: #ef4444;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
      background: var(--bg-dark);
      color: var(--text-main);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    header {
      background: var(--bg-surface);
      border-bottom: 1px solid var(--border);
      padding: 16px 28px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      position: sticky;
      top: 0;
      z-index: 50;
    }
    .brand-title {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 1.25rem;
      font-weight: 700;
      color: var(--text-main);
    }
    .brand-title span { color: var(--primary); }
    .badge-tag {
      background: rgba(56, 189, 248, 0.15);
      color: var(--primary);
      border: 1px solid rgba(56, 189, 248, 0.3);
      padding: 3px 8px;
      border-radius: 999px;
      font-size: 0.75rem;
      font-weight: 600;
    }
    .nav-tabs {
      display: flex;
      background: var(--bg-surface);
      border-bottom: 1px solid var(--border);
      padding: 0 28px;
      gap: 8px;
    }
    .tab-btn {
      background: none;
      border: none;
      color: var(--text-muted);
      padding: 14px 18px;
      font-size: 0.88rem;
      font-weight: 600;
      cursor: pointer;
      border-bottom: 2px solid transparent;
      transition: all 0.2s;
    }
    .tab-btn:hover { color: var(--text-main); }
    .tab-btn.active {
      color: var(--primary);
      border-bottom-color: var(--primary);
    }
    main {
      flex: 1;
      padding: 24px 28px;
      max-width: 1600px;
      width: 100%;
      margin: 0 auto;
    }
    .kpi-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }
    .kpi-card {
      background: var(--bg-card);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 18px 20px;
      position: relative;
      overflow: hidden;
    }
    .kpi-card::before {
      content: '';
      position: absolute;
      top: 0; left: 0; bottom: 0; width: 4px;
      background: var(--primary);
    }
    .kpi-card.alert-card::before { background: var(--risk-crit); }
    .kpi-card.warn-card::before { background: var(--risk-mod); }
    .kpi-title { font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 6px; }
    .kpi-value { font-size: 1.85rem; font-weight: 700; color: #fff; font-family: 'JetBrains Mono', monospace; }
    .kpi-sub { font-size: 0.8rem; color: var(--text-muted); margin-top: 4px; }
    
    .panel-card {
      background: var(--bg-card);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 20px;
      margin-bottom: 20px;
    }
    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }
    .panel-title { font-size: 1.1rem; font-weight: 700; }
    
    .filter-bar {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      margin-bottom: 16px;
      align-items: center;
    }
    select, input, button {
      background: var(--bg-surface);
      border: 1px solid var(--border);
      color: var(--text-main);
      padding: 8px 14px;
      border-radius: 8px;
      font-size: 0.88rem;
      outline: none;
    }
    select:focus, input:focus { border-color: var(--primary); }
    .btn-primary {
      background: var(--primary);
      color: #0b1329;
      font-weight: 600;
      border: none;
      cursor: pointer;
    }
    .btn-primary:hover { background: var(--primary-hover); }
    .btn-secondary {
      background: var(--bg-elevated);
      color: var(--text-main);
      border: 1px solid var(--border);
      cursor: pointer;
    }
    .btn-secondary:hover { background: var(--border); }
    
    table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.88rem;
    }
    th {
      text-align: left;
      padding: 12px 14px;
      background: var(--bg-surface);
      color: var(--text-muted);
      font-weight: 600;
      border-bottom: 1px solid var(--border);
    }
    td {
      padding: 12px 14px;
      border-bottom: 1px solid rgba(43, 54, 116, 0.4);
    }
    tr:hover { background: rgba(56, 189, 248, 0.05); }
    
    .badge {
      display: inline-block;
      padding: 4px 8px;
      border-radius: 6px;
      font-size: 0.75rem;
      font-weight: 700;
      text-align: center;
    }
    .badge-low { background: rgba(16, 185, 129, 0.15); color: var(--risk-low); border: 1px solid rgba(16, 185, 129, 0.3); }
    .badge-mod { background: rgba(245, 158, 11, 0.15); color: var(--risk-mod); border: 1px solid rgba(245, 158, 11, 0.3); }
    .badge-high { background: rgba(249, 115, 22, 0.15); color: var(--risk-high); border: 1px solid rgba(249, 115, 22, 0.3); }
    .badge-crit { background: rgba(239, 68, 68, 0.2); color: var(--risk-crit); border: 1px solid rgba(239, 68, 68, 0.4); animation: pulse 2s infinite; }
    
    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.7; }
    }
    
    .deep-dive-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
    }
    @media (max-width: 1024px) {
      .deep-dive-grid { grid-template-columns: 1fr; }
    }
    .sensor-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 12px;
    }
    .sensor-box {
      background: var(--bg-surface);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 12px;
    }
    .sensor-box.alert-sensor {
      border-color: var(--risk-crit);
      background: rgba(239, 68, 68, 0.08);
    }
    .sensor-name { font-size: 0.75rem; color: var(--text-muted); }
    .sensor-val { font-size: 1.25rem; font-weight: 700; font-family: 'JetBrains Mono', monospace; margin: 4px 0; }
    .sensor-norm { font-size: 0.72rem; color: var(--text-muted); }
    
    .xai-bar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px 10px;
      background: var(--bg-surface);
      border-radius: 6px;
      margin-bottom: 8px;
      font-size: 0.82rem;
    }
    .xai-bar.outlier { border-left: 3px solid var(--risk-crit); }
    
    .chart-container {
      background: var(--bg-surface);
      border-radius: 8px;
      padding: 16px;
      margin-top: 12px;
    }
    svg text { font-family: 'Inter', sans-serif; }
    
    .modal {
      display: none;
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(11, 19, 41, 0.8);
      z-index: 100;
      align-items: center;
      justify-content: center;
    }
    .modal-content {
      background: var(--bg-card);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 24px;
      width: 100%;
      max-width: 550px;
    }
    .form-group { margin-bottom: 14px; }
    .form-group label { display: block; font-size: 0.82rem; margin-bottom: 5px; color: var(--text-muted); }
    .form-group input, .form-group select, .form-group textarea { width: 100%; }
  </style>
</head>
<body>

  <header>
    <div class="brand-title">
      ✈️ <span>AirData</span> Predictive Maintenance Intelligence System
      <span class="badge-tag">AI / ML Engine Active</span>
    </div>
    <div style="display: flex; gap: 12px; align-items: center;">
      <button class="btn-secondary" onclick="retrainModels()">🔄 Retrain Models</button>
      <button class="btn-primary" onclick="openWorkOrderModal()">+ New Work Order</button>
    </div>
  </header>

  <nav class="nav-tabs">
    <button class="tab-btn active" onclick="switchTab('fleet')">Fleet Health & Risk</button>
    <button class="tab-btn" onclick="switchTab('deep-dive')">Component Deep-Dive & XAI</button>
    <button class="tab-btn" onclick="switchTab('forecast')">Sensor Trend Forecaster</button>
    <button class="tab-btn" onclick="switchTab('alerts')">Alert Center</button>
    <button class="tab-btn" onclick="switchTab('work-orders')">Work Orders</button>
    <button class="tab-btn" onclick="switchTab('performance')">ML Model Evaluation</button>
  </nav>

  <main>
    <!-- Top 5 KPIs -->
    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-title">Monitored Fleet</div>
        <div class="kpi-value" id="kpi-aircraft">12</div>
        <div class="kpi-sub">Commercial Aircraft Units</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-title">Monitored Subsystems</div>
        <div class="kpi-value" id="kpi-components">72</div>
        <div class="kpi-sub">Active Telemetry Channels</div>
      </div>
      <div class="kpi-card alert-card">
        <div class="kpi-title">High / Critical Risk</div>
        <div class="kpi-value" id="kpi-high-risk" style="color: var(--risk-crit);">--</div>
        <div class="kpi-sub">Requires Line Inspection</div>
      </div>
      <div class="kpi-card warn-card">
        <div class="kpi-title">Sensor Anomalies</div>
        <div class="kpi-value" id="kpi-anomalies" style="color: var(--risk-mod);">--</div>
        <div class="kpi-sub">Isolation Forest Excursions</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-title">Active Alerts</div>
        <div class="kpi-value" id="kpi-alerts">--</div>
        <div class="kpi-sub">Open Triage Items</div>
      </div>
    </div>

    <!-- TAB 1: FLEET HEALTH & RISK -->
    <section id="tab-fleet" class="tab-content">
      <div class="panel-card">
        <div class="panel-header">
          <div class="panel-title">Fleet Risk Matrix & Predictive Failure Scores</div>
          <div class="filter-bar" style="margin-bottom:0;">
            <select id="fleet-filter-ac" onchange="renderFleetTable()"><option value="ALL">All Aircraft</option></select>
            <select id="fleet-filter-comp" onchange="renderFleetTable()"><option value="ALL">All Components</option></select>
            <select id="fleet-filter-risk" onchange="renderFleetTable()">
              <option value="ALL">All Risk Levels</option>
              <option value="CRITICAL">Critical</option>
              <option value="HIGH">High</option>
              <option value="MODERATE">Moderate</option>
              <option value="LOW">Low</option>
            </select>
          </div>
        </div>
        <div style="overflow-x: auto;">
          <table>
            <thead>
              <tr>
                <th>Aircraft ID</th>
                <th>Model</th>
                <th>Component</th>
                <th>Failure Risk</th>
                <th>Risk Category</th>
                <th>Est. RUL</th>
                <th>Anomaly Score</th>
                <th>Priority</th>
                <th>Recommended Action</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody id="fleet-table-body">
              <tr><td colspan="10" style="text-align:center; padding: 20px;">Loading telemetry & ML predictions...</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>

    <!-- TAB 2: COMPONENT DEEP-DIVE & XAI -->
    <section id="tab-deep-dive" class="tab-content" style="display: none;">
      <div class="panel-card">
        <div class="panel-header">
          <div class="panel-title">Component Telemetry Deep-Dive & Explainable AI (XAI)</div>
          <div class="filter-bar" style="margin-bottom: 0;">
            <select id="deep-ac-select" onchange="renderDeepDive()"></select>
            <select id="deep-comp-select" onchange="renderDeepDive()"></select>
            <button class="btn-primary" onclick="createWorkOrderForCurrent()">Dispatch Work Order</button>
          </div>
        </div>
        <div class="deep-dive-grid">
          <div>
            <h4 style="margin-bottom: 12px; color: var(--primary);">Real-Time Sensor Readings</h4>
            <div class="sensor-grid" id="deep-sensor-grid"></div>
          </div>
          <div>
            <h4 style="margin-bottom: 12px; color: var(--primary);">Explainable AI (XAI) Feature Importance & Outliers</h4>
            <div id="deep-xai-list"></div>
            <div style="margin-top: 16px; background: var(--bg-surface); padding: 14px; border-radius: 8px; border: 1px solid var(--border);">
              <div style="font-weight: 600; font-size: 0.88rem; margin-bottom: 6px; color: var(--text-main);">Diagnostics Narrative:</div>
              <p id="deep-narrative" style="font-size: 0.82rem; color: var(--text-muted); line-height: 1.5;"></p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- TAB 3: FORECASTING -->
    <section id="tab-forecast" class="tab-content" style="display: none;">
      <div class="panel-card">
        <div class="panel-header">
          <div class="panel-title">Sensor Trend Analysis & Holt-Linear Degradation Projection</div>
          <div class="filter-bar" style="margin-bottom: 0;">
            <select id="fc-ac-select" onchange="renderForecast()"></select>
            <select id="fc-comp-select" onchange="renderForecast()"></select>
            <select id="fc-sensor-select" onchange="renderForecast()">
              <option value="egt_celsius">Exhaust Gas Temp (EGT)</option>
              <option value="core_vibration_ips">Core Vibration (in/s)</option>
              <option value="oil_pressure_psi">Oil Pressure (PSI)</option>
              <option value="hydraulic_pressure_psi">Hydraulic Pressure (PSI)</option>
              <option value="brake_temp_celsius">Brake Temperature (°C)</option>
            </select>
          </div>
        </div>
        <div id="forecast-breach-banner" style="padding: 10px 14px; border-radius: 8px; margin-bottom: 12px; font-size: 0.88rem; font-weight: 600;"></div>
        <div class="chart-container" id="forecast-chart-container">
          <svg id="forecast-svg" width="100%" height="340"></svg>
        </div>
      </div>
    </section>

    <!-- TAB 4: ALERT CENTER -->
    <section id="tab-alerts" class="tab-content" style="display: none;">
      <div class="panel-card">
        <div class="panel-header">
          <div class="panel-title">Maintenance Alert Center & Incident Triage</div>
        </div>
        <div style="overflow-x: auto;">
          <table>
            <thead>
              <tr>
                <th>Alert ID</th>
                <th>Severity</th>
                <th>Aircraft</th>
                <th>Component</th>
                <th>Title & Description</th>
                <th>Supporting Evidence</th>
                <th>Status</th>
                <th>Timestamp</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody id="alert-table-body"></tbody>
          </table>
        </div>
      </div>
    </section>

    <!-- TAB 5: WORK ORDERS -->
    <section id="tab-work-orders" class="tab-content" style="display: none;">
      <div class="panel-card">
        <div class="panel-header">
          <div class="panel-title">Aircraft Maintenance Work Orders (Persistent Storage)</div>
          <button class="btn-primary" onclick="openWorkOrderModal()">+ New Work Order</button>
        </div>
        <div style="overflow-x: auto;">
          <table>
            <thead>
              <tr>
                <th>WO ID</th>
                <th>Aircraft ID</th>
                <th>Component</th>
                <th>Priority</th>
                <th>Status</th>
                <th>Assigned Lead</th>
                <th>Created</th>
                <th>Task Description & Remarks</th>
                <th>Update</th>
              </tr>
            </thead>
            <tbody id="wo-table-body"></tbody>
          </table>
        </div>
      </div>
    </section>

    <!-- TAB 6: MODEL PERFORMANCE -->
    <section id="tab-performance" class="tab-content" style="display: none;">
      <div class="panel-card">
        <div class="panel-header">
          <div class="panel-title">Machine Learning Model Performance & Evaluation Metrics</div>
          <button class="btn-secondary" onclick="retrainModels()">Retrain on Telemetry</button>
        </div>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 16px; margin-bottom: 24px;">
          <div class="kpi-card">
            <div class="kpi-title">Random Forest Classifier (Failure Risk)</div>
            <div style="margin-top: 8px; font-size: 0.88rem; line-height: 1.8;">
              <div>Accuracy: <strong id="m-acc" style="color: var(--primary);">--</strong></div>
              <div>Precision: <strong id="m-prec">--</strong></div>
              <div>Recall: <strong id="m-rec">--</strong></div>
              <div>F1-Score: <strong id="m-f1">--</strong></div>
              <div>ROC-AUC: <strong id="m-auc">--</strong></div>
            </div>
          </div>
          <div class="kpi-card">
            <div class="kpi-title">Random Forest Regressor (RUL)</div>
            <div style="margin-top: 8px; font-size: 0.88rem; line-height: 1.8;">
              <div>Mean Absolute Error (MAE): <strong id="m-mae" style="color: var(--primary);">--</strong></div>
              <div>Root Mean Squared Error: <strong id="m-rmse">--</strong></div>
              <div>R² Determination: <strong id="m-r2">--</strong></div>
              <div>Confidence Bounds: <strong>90% Empirical Interval</strong></div>
            </div>
          </div>
          <div class="kpi-card">
            <div class="kpi-title">Isolation Forest (Anomaly Detection)</div>
            <div style="margin-top: 8px; font-size: 0.88rem; line-height: 1.8;">
              <div>Flagged Anomalies: <strong id="m-anom-count" style="color: var(--risk-mod);">--</strong></div>
              <div>Anomaly Detection Rate: <strong id="m-anom-rate">--</strong></div>
              <div>Average Anomaly Score: <strong id="m-anom-avg">--</strong></div>
              <div>Threshold: <strong>s(x) &gt;= 0.62</strong></div>
            </div>
          </div>
        </div>

        <div style="margin-top: 16px;">
          <h4 style="margin-bottom: 10px; color: var(--primary);">Global Gini Feature Importance Ranking:</h4>
          <div id="m-feature-importance"></div>
        </div>

        <div style="margin-top: 24px; background: var(--bg-surface); padding: 16px; border-radius: 8px; border: 1px solid var(--border);">
          <h4 style="color: var(--text-main); margin-bottom: 8px;">Documented Engineering Limitations & Operating Disclaimers:</h4>
          <ul id="m-limitations" style="font-size: 0.82rem; color: var(--text-muted); padding-left: 20px; line-height: 1.6;"></ul>
        </div>
      </div>
    </section>
  </main>

  <!-- Work Order Modal -->
  <div class="modal" id="wo-modal">
    <div class="modal-content">
      <h3 style="margin-bottom: 16px;">Create Maintenance Work Order</h3>
      <div class="form-group">
        <label>Aircraft ID</label>
        <input type="text" id="wo-ac" value="AC-738-01">
      </div>
      <div class="form-group">
        <label>Component</label>
        <select id="wo-comp">
          <option value="ENGINE_1">Turbofan Engine 1</option>
          <option value="ENGINE_2">Turbofan Engine 2</option>
          <option value="HYDRAULICS">Main Hydraulics</option>
          <option value="LANDING_GEAR">Landing Gear</option>
          <option value="APU">Auxiliary Power Unit</option>
          <option value="ELECTRICAL">Electrical System</option>
        </select>
      </div>
      <div class="form-group">
        <label>Priority</label>
        <select id="wo-prio">
          <option value="ROUTINE">Routine Line Check</option>
          <option value="MEDIUM">Elevated Maintenance</option>
          <option value="HIGH">Priority Inspection (48h)</option>
          <option value="URGENT_AOG">Urgent (AOG Warning)</option>
        </select>
      </div>
      <div class="form-group">
        <label>Task Title</label>
        <input type="text" id="wo-title" value="Borescope Inspection & Sensor Review">
      </div>
      <div class="form-group">
        <label>Recommended Action</label>
        <input type="text" id="wo-action" value="Inspect HPT nozzles and check bearing vibration play.">
      </div>
      <div class="form-group">
        <label>Assigned Lead Technician</label>
        <input type="text" id="wo-tech" value="Lead Insp. Vance">
      </div>
      <div class="form-group">
        <label>Notes</label>
        <textarea id="wo-notes" rows="2" style="background:var(--bg-surface); color:#fff; border:1px solid var(--border); border-radius:6px; padding:6px;">Initiated from predictive analytics trigger.</textarea>
      </div>
      <div style="display:flex; justify-content: flex-end; gap: 10px; margin-top: 16px;">
        <button class="btn-secondary" onclick="closeWorkOrderModal()">Cancel</button>
        <button class="btn-primary" onclick="submitWorkOrder()">Create Work Order</button>
      </div>
    </div>
  </div>

  <script>
    let fleetData = [];
    let alertsData = [];
    let workOrdersData = [];
    let perfData = null;

    async function init() {
      await loadAllData();
      populateDropdowns();
      renderFleetTable();
      renderDeepDive();
      renderForecast();
      renderAlertsTable();
      renderWorkOrdersTable();
      renderPerformance();
    }

    async function loadAllData() {
      try {
        const [fleetRes, alertsRes, woRes, perfRes] = await Promise.all([
          fetch('/api/fleet').then(r => r.json()),
          fetch('/api/alerts').then(r => r.json()),
          fetch('/api/work-orders').then(r => r.json()),
          fetch('/api/model-performance').then(r => r.json())
        ]);
        fleetData = fleetRes;
        alertsData = alertsRes;
        workOrdersData = woRes;
        perfData = perfRes;
        updateKpis();
      } catch (e) {
        console.error("Failed to load API data", e);
      }
    }

    function updateKpis() {
      const acSet = new Set(fleetData.map(d => d.aircraftId));
      document.getElementById('kpi-aircraft').innerText = acSet.size;
      document.getElementById('kpi-components').innerText = fleetData.length;

      const highRisk = fleetData.filter(d => d.riskCategory === 'HIGH' || d.riskCategory === 'CRITICAL').length;
      document.getElementById('kpi-high-risk').innerText = highRisk;

      const anomalies = fleetData.filter(d => d.anomalyDetected).length;
      document.getElementById('kpi-anomalies').innerText = anomalies;

      const activeAlerts = alertsData.filter(a => a.status !== 'RESOLVED').length;
      document.getElementById('kpi-alerts').innerText = activeAlerts;
    }

    function populateDropdowns() {
      const acSet = Array.from(new Set(fleetData.map(d => d.aircraftId))).sort();
      const compSet = Array.from(new Set(fleetData.map(d => d.component))).sort();

      const fleetAc = document.getElementById('fleet-filter-ac');
      const deepAc = document.getElementById('deep-ac-select');
      const fcAc = document.getElementById('fc-ac-select');

      acSet.forEach(ac => {
        fleetAc.innerHTML += `<option value="${ac}">${ac}</option>`;
        deepAc.innerHTML += `<option value="${ac}">${ac}</option>`;
        fcAc.innerHTML += `<option value="${ac}">${ac}</option>`;
      });

      const fleetComp = document.getElementById('fleet-filter-comp');
      const deepComp = document.getElementById('deep-comp-select');
      const fcComp = document.getElementById('fc-comp-select');

      compSet.forEach(c => {
        fleetComp.innerHTML += `<option value="${c}">${c}</option>`;
        deepComp.innerHTML += `<option value="${c}">${c}</option>`;
        fcComp.innerHTML += `<option value="${c}">${c}</option>`;
      });
    }

    function renderFleetTable() {
      const acF = document.getElementById('fleet-filter-ac').value;
      const compF = document.getElementById('fleet-filter-comp').value;
      const riskF = document.getElementById('fleet-filter-risk').value;

      let filtered = fleetData;
      if (acF !== 'ALL') filtered = filtered.filter(d => d.aircraftId === acF);
      if (compF !== 'ALL') filtered = filtered.filter(d => d.component === compF);
      if (riskF !== 'ALL') filtered = filtered.filter(d => d.riskCategory === riskF);

      const tbody = document.getElementById('fleet-table-body');
      tbody.innerHTML = '';

      filtered.forEach(d => {
        const badgeClass = d.riskCategory === 'CRITICAL' ? 'badge-crit' :
                          (d.riskCategory === 'HIGH' ? 'badge-high' :
                          (d.riskCategory === 'MODERATE' ? 'badge-mod' : 'badge-low'));

        tbody.innerHTML += `
          <tr>
            <td><strong>${d.aircraftId}</strong></td>
            <td>${d.aircraftModel}</td>
            <td>${d.component}</td>
            <td>${(d.failureRiskScore * 100).toFixed(1)}%</td>
            <td><span class="badge ${badgeClass}">${d.riskCategory}</span></td>
            <td><strong>${Math.round(d.predictedRulCycles)}</strong> cycles</td>
            <td>${d.anomalyScore.toFixed(3)} ${d.anomalyDetected ? '⚠️' : ''}</td>
            <td><span class="badge" style="background:rgba(99,102,241,0.2); color:#818cf8;">${d.priority}</span></td>
            <td style="max-width:300px; font-size:0.8rem; color:var(--text-muted);">${d.recommendedAction}</td>
            <td><button class="btn-secondary" style="padding:4px 8px; font-size:0.75rem;" onclick="inspectComponent('${d.aircraftId}', '${d.component}')">Inspect</button></td>
          </tr>
        `;
      });
    }

    function inspectComponent(ac, comp) {
      document.getElementById('deep-ac-select').value = ac;
      document.getElementById('deep-comp-select').value = comp;
      switchTab('deep-dive');
      renderDeepDive();
    }

    function renderDeepDive() {
      const ac = document.getElementById('deep-ac-select').value;
      const comp = document.getElementById('deep-comp-select').value;
      const rec = fleetData.find(d => d.aircraftId === ac && d.component === comp);
      if (!rec) return;

      const r = rec.latestReading;
      const grid = document.getElementById('deep-sensor-grid');
      grid.innerHTML = `
        <div class="sensor-box ${r.egtCelsius > 880 ? 'alert-sensor' : ''}">
          <div class="sensor-name">Exhaust Gas Temp (EGT)</div>
          <div class="sensor-val">${r.egtCelsius.toFixed(1)} °C</div>
          <div class="sensor-norm">Nominal: 650-800 °C</div>
        </div>
        <div class="sensor-box ${r.coreVibrationIps > 2.5 ? 'alert-sensor' : ''}">
          <div class="sensor-name">Core Vibration (RMS)</div>
          <div class="sensor-val">${r.coreVibrationIps.toFixed(2)} in/s</div>
          <div class="sensor-norm">Nominal: &lt; 1.8 in/s</div>
        </div>
        <div class="sensor-box ${r.fanVibrationIps > 2.0 ? 'alert-sensor' : ''}">
          <div class="sensor-name">Fan Vibration</div>
          <div class="sensor-val">${r.fanVibrationIps.toFixed(2)} in/s</div>
          <div class="sensor-norm">Nominal: &lt; 1.5 in/s</div>
        </div>
        <div class="sensor-box ${r.oilPressurePsi < 38 ? 'alert-sensor' : ''}">
          <div class="sensor-name">Oil Pressure</div>
          <div class="sensor-val">${r.oilPressurePsi.toFixed(1)} PSI</div>
          <div class="sensor-norm">Nominal: 45-65 PSI</div>
        </div>
        <div class="sensor-box ${r.oilTempCelsius > 135 ? 'alert-sensor' : ''}">
          <div class="sensor-name">Oil Temperature</div>
          <div class="sensor-val">${r.oilTempCelsius.toFixed(1)} °C</div>
          <div class="sensor-norm">Nominal: 90-120 °C</div>
        </div>
        <div class="sensor-box ${r.hydraulicPressurePsi < 2700 ? 'alert-sensor' : ''}">
          <div class="sensor-name">Hydraulic Pressure</div>
          <div class="sensor-val">${Math.round(r.hydraulicPressurePsi)} PSI</div>
          <div class="sensor-norm">Nominal: 2950-3050 PSI</div>
        </div>
      `;

      const xaiList = document.getElementById('deep-xai-list');
      xaiList.innerHTML = '';
      (rec.featureContributions || []).forEach(fc => {
        const sign = fc.deviationPercentage >= 0 ? '+' : '';
        xaiList.innerHTML += `
          <div class="xai-bar ${fc.isDeviating ? 'outlier' : ''}">
            <div>
              <strong>${fc.displayName}</strong>: ${fc.currentValue.toFixed(2)} ${fc.unit}
              <span style="color:var(--text-muted); font-size:0.75rem;">(${sign}${fc.deviationPercentage.toFixed(1)}%, ${fc.zScore.toFixed(1)}σ)</span>
            </div>
            <div>Importance: ${(fc.importanceWeight * 100).toFixed(1)}%</div>
          </div>
        `;
      });

      document.getElementById('deep-narrative').innerText = rec.diagnosticExplanation + " Action: " + rec.recommendedAction;
    }

    async function renderForecast() {
      const ac = document.getElementById('fc-ac-select').value;
      const comp = document.getElementById('fc-comp-select').value;
      const sensor = document.getElementById('fc-sensor-select').value;
      if (!ac || !comp || !sensor) return;

      try {
        const res = await fetch(`/api/forecast?aircraftId=${ac}&component=${comp}&sensor=${sensor}&horizon=15`);
        const tf = await res.json();

        const banner = document.getElementById('forecast-breach-banner');
        if (tf.thresholdBreached) {
          banner.style.background = 'rgba(239, 68, 68, 0.2)';
          banner.style.color = '#ef4444';
          banner.innerText = `CRITICAL WARNING: Forecast projects ${tf.sensorDisplayName} will breach threshold (${tf.criticalThreshold} ${tf.unit}) in ~${tf.estimatedCyclesToBreach} cycles!`;
        } else {
          banner.style.background = 'rgba(16, 185, 129, 0.15)';
          banner.style.color = '#10b981';
          banner.innerText = `STABLE: Trend for ${tf.sensorDisplayName} remains within certified safety limits over the 15-cycle forecast horizon.`;
        }

        drawForecastSvg(tf);
      } catch (e) {
        console.error("Forecast failed", e);
      }
    }

    function drawForecastSvg(tf) {
      const svg = document.getElementById('forecast-svg');
      const w = svg.clientWidth || 900;
      const h = 340;
      const pad = { top: 40, right: 30, bottom: 40, left: 60 };
      const pw = w - pad.left - pad.right;
      const ph = h - pad.top - pad.bottom;

      const hC = tf.historicalCycles || [];
      const hV = tf.historicalValues || [];
      const fC = tf.forecastCycles || [];
      const fV = tf.forecastValues || [];
      const uB = tf.upperConfidence || [];
      const lB = tf.lowerConfidence || [];
      const thresh = tf.criticalThreshold;

      if (hV.length === 0) return;

      const allCycles = hC.concat(fC);
      const minC = Math.min(...allCycles);
      const maxC = Math.max(...allCycles);

      const allVals = hV.concat(fV).concat(uB).concat(lB).concat([thresh]);
      let minV = Math.min(...allVals);
      let maxV = Math.max(...allVals);
      const vMargin = (maxV - minV) * 0.1;
      minV -= vMargin; maxV += vMargin;

      const mapX = c => pad.left + ((c - minC) / (maxC - minC)) * pw;
      const mapY = v => pad.top + ph * (1 - (v - minV) / (maxV - minV));

      let svgHtml = '';

      // Grid Lines
      for (let i = 0; i <= 5; i++) {
        const val = minV + (i / 5) * (maxV - minV);
        const y = mapY(val);
        svgHtml += `<line x1="${pad.left}" y1="${y}" x2="${pad.left + pw}" y2="${y}" stroke="rgba(255,255,255,0.08)" />`;
        svgHtml += `<text x="${pad.left - 8}" y="${y + 4}" fill="#94a3b8" font-size="11" text-anchor="end">${val.toFixed(1)}</text>`;
      }

      // Threshold Line
      const threshY = mapY(thresh);
      svgHtml += `<line x1="${pad.left}" y1="${threshY}" x2="${pad.left + pw}" y2="${threshY}" stroke="#ef4444" stroke-width="1.8" stroke-dasharray="6,4" />`;
      svgHtml += `<text x="${pad.left + pw - 10}" y="${threshY - 6}" fill="#ef4444" font-size="11" text-anchor="end" font-weight="600">Threshold: ${thresh} ${tf.unit}</text>`;

      // 90% Confidence Interval Band
      if (fV.length > 0) {
        let bandPath = `M ${mapX(hC[hC.length - 1])} ${mapY(hV[hV.length - 1])} `;
        for (let i = 0; i < fC.length; i++) bandPath += `L ${mapX(fC[i])} ${mapY(uB[i])} `;
        for (let i = fC.length - 1; i >= 0; i--) bandPath += `L ${mapX(fC[i])} ${mapY(lB[i])} `;
        bandPath += 'Z';
        svgHtml += `<path d="${bandPath}" fill="rgba(56, 189, 248, 0.15)" />`;
      }

      // Historical Path
      let histPath = '';
      hC.forEach((c, idx) => {
        const cmd = idx === 0 ? 'M' : 'L';
        histPath += `${cmd} ${mapX(c)} ${mapY(hV[idx])} `;
      });
      svgHtml += `<path d="${histPath}" fill="none" stroke="#38bdf8" stroke-width="2.2" />`;

      // Forecast Path
      if (fC.length > 0) {
        let fcPath = `M ${mapX(hC[hC.length - 1])} ${mapY(hV[hV.length - 1])} `;
        fC.forEach((c, idx) => {
          fcPath += `L ${mapX(c)} ${mapY(fV[idx])} `;
        });
        svgHtml += `<path d="${fcPath}" fill="none" stroke="#f97316" stroke-width="2.2" stroke-dasharray="5,3" />`;
      }

      // Cycle Dots
      hC.forEach((c, idx) => {
        svgHtml += `<circle cx="${mapX(c)}" cy="${mapY(hV[idx])}" r="3" fill="#38bdf8" />`;
      });
      fC.forEach((c, idx) => {
        svgHtml += `<circle cx="${mapX(c)}" cy="${mapY(fV[idx])}" r="3" fill="#f97316" />`;
      });

      svg.innerHTML = svgHtml;
    }

    function renderAlertsTable() {
      const tbody = document.getElementById('alert-table-body');
      tbody.innerHTML = '';
      alertsData.forEach(a => {
        const sevColor = a.severity === 'CRITICAL' ? 'color:#ef4444;' : (a.severity === 'WARNING' ? 'color:#f59e0b;' : 'color:#38bdf8;');
        tbody.innerHTML += `
          <tr>
            <td><strong>${a.alertId}</strong></td>
            <td><strong style="${sevColor}">${a.severity}</strong></td>
            <td>${a.aircraftId}</td>
            <td>${a.component}</td>
            <td><strong>${a.title}</strong><div style="font-size:0.75rem; color:var(--text-muted);">${a.description}</div></td>
            <td style="font-size:0.8rem;">${a.supportingEvidence}</td>
            <td><span class="badge ${a.status === 'RESOLVED' ? 'badge-low' : 'badge-mod'}">${a.status}</span></td>
            <td>${a.createdAt ? a.createdAt.replace('T', ' ').substring(0, 16) : ''}</td>
            <td>
              ${a.status === 'NEW' ? `<button class="btn-secondary" style="padding:3px 6px; font-size:0.75rem;" onclick="acknowledgeAlert('${a.alertId}')">Ack</button>` : ''}
              ${a.status !== 'RESOLVED' ? `<button class="btn-primary" style="padding:3px 6px; font-size:0.75rem;" onclick="resolveAlert('${a.alertId}')">Resolve</button>` : ''}
            </td>
          </tr>
        `;
      });
    }

    async function acknowledgeAlert(id) {
      await fetch('/api/alerts/acknowledge?alertId=' + id, { method: 'POST' });
      await loadAllData();
      renderAlertsTable();
    }

    async function resolveAlert(id) {
      const notes = prompt("Enter resolution notes:", "Completed inspection and verified clearances.");
      if (notes) {
        await fetch(`/api/alerts/resolve?alertId=${id}&notes=${encodeURIComponent(notes)}`, { method: 'POST' });
        await loadAllData();
        renderAlertsTable();
      }
    }

    function renderWorkOrdersTable() {
      const tbody = document.getElementById('wo-table-body');
      tbody.innerHTML = '';
      workOrdersData.forEach(wo => {
        tbody.innerHTML += `
          <tr>
            <td><strong>${wo.workOrderId}</strong></td>
            <td>${wo.aircraftId}</td>
            <td>${wo.component}</td>
            <td><span class="badge" style="background:rgba(249,115,22,0.15); color:#f97316;">${wo.priority}</span></td>
            <td><span class="badge" style="background:rgba(56,189,248,0.15); color:#38bdf8;">${wo.status}</span></td>
            <td>${wo.assignedTechnician}</td>
            <td>${wo.createdAt ? wo.createdAt.replace('T', ' ').substring(0, 16) : ''}</td>
            <td style="max-width:300px; font-size:0.8rem;"><strong>${wo.title}</strong>: ${wo.technicianNotes || ''}</td>
            <td>
              ${wo.status !== 'COMPLETED' ? `<button class="btn-secondary" style="padding:3px 6px; font-size:0.75rem;" onclick="updateWoStatus('${wo.workOrderId}', 'COMPLETED')">Complete</button>` : '✓'}
            </td>
          </tr>
        `;
      });
    }

    async function updateWoStatus(id, st) {
      await fetch(`/api/work-orders/status?workOrderId=${id}&status=${st}`, { method: 'POST' });
      await loadAllData();
      renderWorkOrdersTable();
    }

    function renderPerformance() {
      if (!perfData) return;
      document.getElementById('m-acc').innerText = (perfData.classificationAccuracy * 100).toFixed(2) + '%';
      document.getElementById('m-prec').innerText = (perfData.precision * 100).toFixed(2) + '%';
      document.getElementById('m-rec').innerText = (perfData.recall * 100).toFixed(2) + '%';
      document.getElementById('m-f1').innerText = perfData.f1Score.toFixed(4);
      document.getElementById('m-auc').innerText = perfData.rocAuc.toFixed(4);

      document.getElementById('m-mae').innerText = perfData.rulMeanAbsoluteError.toFixed(2) + ' cycles';
      document.getElementById('m-rmse').innerText = perfData.rulRootMeanSquaredError.toFixed(2) + ' cycles';
      document.getElementById('m-r2').innerText = perfData.rulR2Score.toFixed(4);

      document.getElementById('m-anom-count').innerText = perfData.detectedAnomalyCount;
      document.getElementById('m-anom-rate').innerText = perfData.anomalyRatePct.toFixed(1) + '%';
      document.getElementById('m-anom-avg').innerText = perfData.avgAnomalyScore.toFixed(3);

      const fDiv = document.getElementById('m-feature-importance');
      fDiv.innerHTML = '';
      for (const [key, val] of Object.entries(perfData.featureImportanceRankings || {})) {
        const pct = (val * 100).toFixed(1);
        fDiv.innerHTML += `
          <div style="display:flex; align-items:center; gap:12px; margin-bottom:8px; font-size:0.82rem;">
            <div style="width:180px;">${key}</div>
            <div style="flex:1; background:var(--bg-surface); height:8px; border-radius:4px; overflow:hidden;">
              <div style="background:var(--primary); width:${pct}%; height:100%;"></div>
            </div>
            <div style="width:50px; text-align:right;">${pct}%</div>
          </div>
        `;
      }

      const lUl = document.getElementById('m-limitations');
      lUl.innerHTML = '';
      (perfData.documentedLimitations || []).forEach(lim => {
        lUl.innerHTML += `<li>${lim}</li>`;
      });
    }

    async function retrainModels() {
      if (!confirm("Retrain Random Forest Classifier, Regressor, and Isolation Forest on telemetry dataset?")) return;
      await fetch('/api/retrain', { method: 'POST' });
      await loadAllData();
      renderPerformance();
      alert("Models retrained and evaluated successfully!");
    }

    function switchTab(tabId) {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-content').forEach(c => c.style.display = 'none');

      document.querySelector(`[onclick="switchTab('${tabId}')"]`).classList.add('active');
      document.getElementById('tab-' + tabId).style.display = 'block';

      if (tabId === 'forecast') renderForecast();
    }

    function openWorkOrderModal() { document.getElementById('wo-modal').style.display = 'flex'; }
    function closeWorkOrderModal() { document.getElementById('wo-modal').style.display = 'none'; }

    function createWorkOrderForCurrent() {
      document.getElementById('wo-ac').value = document.getElementById('deep-ac-select').value;
      document.getElementById('wo-comp').value = document.getElementById('deep-comp-select').value;
      openWorkOrderModal();
    }

    async function submitWorkOrder() {
      const body = {
        aircraftId: document.getElementById('wo-ac').value,
        component: document.getElementById('wo-comp').value,
        priority: document.getElementById('wo-prio').value,
        title: document.getElementById('wo-title').value,
        inspection: document.getElementById('wo-action').value,
        technician: document.getElementById('wo-tech').value,
        notes: document.getElementById('wo-notes').value
      };
      await fetch('/api/work-orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      closeWorkOrderModal();
      await loadAllData();
      renderWorkOrdersTable();
      switchTab('work-orders');
    }

    window.onload = init;
  </script>
</body>
</html>
            """;
        }
    }

    // ==============================================================
    // REST API HANDLERS
    // ==============================================================
    static class FleetApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            MaintenanceCoordinator mc = MaintenanceCoordinator.getInstance();
            String json = gson.toJson(mc.getFleetHealthRecords());
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    static class AlertsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            MaintenanceCoordinator mc = MaintenanceCoordinator.getInstance();
            String json = gson.toJson(mc.getAlertService().getAllAlerts());
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    static class AcknowledgeAlertApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> q = parseQueryParams(exchange.getRequestURI().getQuery());
            String alertId = q.get("alertId");
            if (alertId != null) {
                MaintenanceCoordinator.getInstance().getAlertService().acknowledgeAlert(alertId, "Web Supervisor");
                sendResponse(exchange, 200, "application/json", "{\"status\":\"ok\"}");
            } else {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"Missing alertId\"}");
            }
        }
    }

    static class ResolveAlertApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> q = parseQueryParams(exchange.getRequestURI().getQuery());
            String alertId = q.get("alertId");
            String notes = q.getOrDefault("notes", "Resolved via Web Triage.");
            if (alertId != null) {
                MaintenanceCoordinator.getInstance().getAlertService().resolveAlert(alertId, "Web Inspector", notes);
                sendResponse(exchange, 200, "application/json", "{\"status\":\"ok\"}");
            } else {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"Missing alertId\"}");
            }
        }
    }

    static class WorkOrdersApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            MaintenanceCoordinator mc = MaintenanceCoordinator.getInstance();
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<?, ?> map = gson.fromJson(body, Map.class);
                String ac = (String) map.get("aircraftId");
                String compStr = (String) map.get("component");
                String prioStr = (String) map.get("priority");
                String title = (String) map.get("title");
                String insp = (String) map.get("inspection");
                String tech = (String) map.get("technician");
                String notes = (String) map.get("notes");

                AircraftComponentType comp = AircraftComponentType.fromString(compStr);
                MaintenancePriority prio = MaintenancePriority.valueOf(prioStr);

                MaintenanceWorkOrder wo = mc.getWorkOrderService().createWorkOrder(
                        ac, comp, prio, "WEB-DISPATCH", title, insp, tech, notes
                );
                sendResponse(exchange, 201, "application/json", gson.toJson(wo));
            } else {
                String json = gson.toJson(mc.getWorkOrderService().getAllWorkOrders());
                sendResponse(exchange, 200, "application/json", json);
            }
        }
    }

    static class WorkOrderStatusApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> q = parseQueryParams(exchange.getRequestURI().getQuery());
            String woId = q.get("workOrderId");
            String statusStr = q.get("status");
            if (woId != null && statusStr != null) {
                MaintenanceWorkOrder.WorkOrderStatus st = MaintenanceWorkOrder.WorkOrderStatus.valueOf(statusStr);
                MaintenanceCoordinator.getInstance().getWorkOrderService().updateStatus(woId, st, "Updated via Web Console");
                sendResponse(exchange, 200, "application/json", "{\"status\":\"ok\"}");
            } else {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"Missing parameters\"}");
            }
        }
    }

    static class ModelPerformanceApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            MaintenanceCoordinator mc = MaintenanceCoordinator.getInstance();
            String json = gson.toJson(mc.getEvaluationReport());
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    static class ForecastApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> q = parseQueryParams(exchange.getRequestURI().getQuery());
            String ac = q.getOrDefault("aircraftId", "AC-738-01");
            String compStr = q.getOrDefault("component", "ENGINE_1");
            String sensor = q.getOrDefault("sensor", "egt_celsius");
            int horizon = Integer.parseInt(q.getOrDefault("horizon", "15"));

            AircraftComponentType comp = AircraftComponentType.fromString(compStr);
            TrendForecast tf = MaintenanceCoordinator.getInstance().forecastSensorTrend(ac, comp, sensor, horizon);
            sendResponse(exchange, 200, "application/json", gson.toJson(tf));
        }
    }

    static class RetrainApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            MaintenanceCoordinator.getInstance().initializeSystem();
            sendResponse(exchange, 200, "application/json", "{\"status\":\"retrained\"}");
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                           URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }
}
