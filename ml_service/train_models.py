"""
Aircraft Predictive Maintenance - Python Model Training & Evaluation Script
Trains Random Forest Classifier (Failure Risk), Random Forest Regressor (RUL),
and Isolation Forest (Anomaly Detection) on aerospace turbofan telemetry data.
"""

import os
import json
from datetime import datetime

def main():
    try:
        import pandas as pd
        import numpy as np
        from sklearn.ensemble import RandomForestClassifier, RandomForestRegressor, IsolationForest
        from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, roc_auc_score, mean_absolute_error, mean_squared_error, r2_score
    except ImportError as e:
        print(f"[ERROR] Required Python packages missing: {e}")
        print("Install via: pip install -r requirements.txt")
        return

    csv_path = os.path.join("data", "aircraft_sensor_telemetry.csv")
    if not os.path.exists(csv_path):
        print(f"[ERROR] Telemetry dataset not found at {csv_path}")
        return

    print(f"Loading telemetry dataset from {csv_path}...")
    df = pd.read_csv(csv_path)
    print(f"Loaded {len(df)} records across {df['aircraft_id'].nunique()} aircraft.")

    feature_cols = [
        "egt_celsius", "core_vibration_ips", "fan_vibration_ips", "oil_pressure_psi",
        "oil_temp_celsius", "fuel_flow_pph", "hydraulic_pressure_psi", "hydraulic_temp_celsius",
        "brake_temp_celsius", "apu_egt_celsius", "bleed_pressure_psi", "n1_speed_pct",
        "n2_speed_pct", "cycles_since_last_maint", "total_operating_hours"
    ]

    # Grouped split by Aircraft ID to avoid data leakage
    aircraft_ids = df['aircraft_id'].unique()
    np.random.seed(42)
    np.random.shuffle(aircraft_ids)
    split_idx = int(len(aircraft_ids) * 0.70)
    train_ac = set(aircraft_ids[:split_idx])

    train_df = df[df['aircraft_id'].isin(train_ac)]
    test_df = df[~df['aircraft_id'].isin(train_ac)]

    X_train = train_df[feature_cols].values
    y_class_train = train_df['failure_within_30_cycles'].values
    y_reg_train = train_df['actual_rul_cycles'].values

    X_test = test_df[feature_cols].values
    y_class_test = test_df['failure_within_30_cycles'].values
    y_reg_test = test_df['actual_rul_cycles'].values

    print("\n--- Training Machine Learning Models ---")
    clf = RandomForestClassifier(n_estimators=40, max_depth=10, min_samples_split=4, random_state=42)
    clf.fit(X_train, y_class_train)

    reg = RandomForestRegressor(n_estimators=40, max_depth=12, min_samples_split=4, random_state=88)
    reg.fit(X_train, y_reg_train)

    # Train Isolation Forest on healthy records
    healthy_mask = (y_reg_train > 30)
    iso = IsolationForest(n_estimators=100, max_samples=256, contamination=0.08, random_state=101)
    iso.fit(X_train[healthy_mask])

    print("\n--- Evaluating Models on Unseen Test Aircraft ---")
    y_class_pred = clf.predict(X_test)
    y_class_prob = clf.predict_proba(X_test)[:, 1]

    acc = accuracy_score(y_class_test, y_class_pred)
    prec = precision_score(y_class_test, y_class_pred, zero_division=0)
    rec = recall_score(y_class_test, y_class_pred, zero_division=0)
    f1 = f1_score(y_class_test, y_class_pred, zero_division=0)
    roc_auc = roc_auc_score(y_class_test, y_class_prob)

    y_reg_pred = reg.predict(X_test)
    mae = mean_absolute_error(y_reg_test, y_reg_pred)
    rmse = np.sqrt(mean_squared_error(y_reg_test, y_reg_pred))
    r2 = r2_score(y_reg_test, y_reg_pred)

    anom_scores = -iso.score_samples(X_test)
    flagged = (iso.predict(X_test) == -1)

    print(f"Classifier Accuracy: {acc * 100:.2f}% | Precision: {prec * 100:.2f}% | Recall: {rec * 100:.2f}% | F1: {f1:.4f} | ROC-AUC: {roc_auc:.4f}")
    print(f"Regressor RUL MAE:   {mae:.2f} cycles | RMSE: {rmse:.2f} cycles | R²: {r2:.4f}")
    print(f"Isolation Forest:   {np.sum(flagged)} anomalies detected ({np.mean(flagged) * 100:.1f}%)")

    importances = dict(zip(feature_cols, clf.feature_importances_))
    sorted_importances = dict(sorted(importances.items(), key=lambda item: item[1], reverse=True))

    report = {
        "framework": "scikit-learn (Python Companion)",
        "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "trainingRecords": len(train_df),
        "testRecords": len(test_df),
        "classification": {
            "accuracy": float(acc),
            "precision": float(prec),
            "recall": float(rec),
            "f1Score": float(f1),
            "rocAuc": float(roc_auc)
        },
        "regression": {
            "rulMae": float(mae),
            "rulRmse": float(rmse),
            "rulR2": float(r2)
        },
        "anomalyDetection": {
            "flaggedCount": int(np.sum(flagged)),
            "anomalyRate": float(np.mean(flagged) * 100)
        },
        "featureImportances": sorted_importances
    }

    out_file = os.path.join("data", "python_model_evaluation.json")
    with open(out_file, "w") as f:
        json.dump(report, f, indent=2)

    print(f"\nEvaluation metrics saved to {out_file}")

if __name__ == "__main__":
    main()
