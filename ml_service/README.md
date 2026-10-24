# 🐍 Python Companion ML Service & Model Training

This directory contains optional Python tools to train and evaluate equivalent machine learning models using `scikit-learn` on the aircraft sensor telemetry dataset.

> **Note:** The Java application embeds a high-performance pure-Java ML engine (Random Forest Classifier, Regressor, Isolation Forest, and Holt-Winters forecaster). Running this Python service is completely **optional** and serves as a companion tool for data science exploration, validation, or portfolio demonstration.

---

## 🛠️ Setup (Windows PowerShell)

```powershell
# 1. Navigate to ml_service directory
cd C:\Users\srushti\.gemini\antigravity\scratch\airline-data-visualization\ml_service

# 2. (Optional) Create Python virtual environment
python -m venv venv
.\venv\Scripts\Activate.ps1

# 3. Install dependencies
pip install -r requirements.txt
```

---

## 🚀 Running Model Training

Train the Random Forest Classifier, Regressor, and Isolation Forest on `data/aircraft_sensor_telemetry.csv`:

```powershell
python train_models.py
```

Outputs evaluation metrics to `data/python_model_evaluation.json`.

---

## 🌐 Running Local REST Service

```powershell
python api_service.py
```

Endpoints:
- `GET  http://127.0.0.1:5000/health`
- `POST http://127.0.0.1:5000/predict/risk`
- `POST http://127.0.0.1:5000/predict/rul`
- `POST http://127.0.0.1:5000/detect/anomaly`
