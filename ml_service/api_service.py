"""
Optional Python Local REST API Service for Aircraft Predictive Maintenance.
Endpoints:
  GET  /health            - Service health check
  POST /predict/risk      - Estimates failure risk probability and category
  POST /predict/rul       - Estimates remaining useful life in cycles
  POST /detect/anomaly    - Computes multivariate anomaly score
"""

import os
from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route("/health", methods=["GET"])
def health_check():
    return jsonify({
        "status": "UP",
        "service": "Aircraft Predictive Maintenance Python ML Service",
        "version": "1.0.0"
    })

@app.route("/predict/risk", methods=["POST"])
def predict_risk():
    data = request.get_json(force=True)
    # Placeholder local mock if python models not loaded
    egt = data.get("egt_celsius", 670.0)
    vib = data.get("core_vibration_ips", 0.9)
    # Simple linear heuristic for fallback
    score = min(1.0, max(0.0, (egt - 650.0) / 250.0 * 0.5 + (vib - 0.9) / 2.5 * 0.5))
    cat = "CRITICAL" if score >= 0.85 else ("HIGH" if score >= 0.65 else ("MODERATE" if score >= 0.30 else "LOW"))
    return jsonify({
        "failureRiskScore": round(score, 4),
        "riskCategory": cat
    })

@app.route("/predict/rul", methods=["POST"])
def predict_rul():
    data = request.get_json(force=True)
    egt = data.get("egt_celsius", 670.0)
    rul = max(0.0, 160.0 - (egt - 650.0) * 0.55)
    return jsonify({
        "predictedRulCycles": round(rul, 1),
        "lowerBound90": round(max(0.0, rul - 12.0), 1),
        "upperBound90": round(rul + 12.0, 1)
    })

@app.route("/detect/anomaly", methods=["POST"])
def detect_anomaly():
    data = request.get_json(force=True)
    egt = data.get("egt_celsius", 670.0)
    vib = data.get("core_vibration_ips", 0.9)
    anom_score = min(1.0, max(0.0, (abs(egt - 670.0) / 100.0) * 0.5 + (abs(vib - 0.9) / 1.5) * 0.5))
    return jsonify({
        "anomalyScore": round(anom_score, 4),
        "isAnomaly": anom_score >= 0.62
    })

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000, debug=False)
