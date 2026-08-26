from pathlib import Path

import joblib
import pandas as pd
from ai_module.src.feature_builder import build_prediction_features

from fastapi import FastAPI
from catboost import Pool
from pydantic import BaseModel


MODEL_PATH = Path("ai_module/models/best_model.joblib")

model = joblib.load(MODEL_PATH)

app = FastAPI(
    title="Credit Scoring ML API",
    version="2.0.0"
)


class CreditRequest(BaseModel):
    duration_months: int
    credit_amount: float
    purpose: str
    employment_years: str
    personal_status: str
    age: int
    job: str
    dependents: int
    telephone: str


@app.get("/")
def root():
    return {
        "status": "OK",
        "service": "Credit Scoring ML API",
        "model": "CatBoost"
    }


def explain_prediction(data):
    preprocessor = model.named_steps["preprocessor"]
    cat_model = model.named_steps["model"]

    processed_data = preprocessor.transform(data)

    feature_names = preprocessor.get_feature_names_out()

    pool = Pool(
        processed_data,
        feature_names=list(feature_names)
    )

    shap_values = cat_model.get_feature_importance(
        type="ShapValues",
        data=pool
    )

    values = shap_values[0][:-1]

    ranked = sorted(
        zip(feature_names, values),
        key=lambda x: abs(x[1]),
        reverse=True
    )[:3]

    factors = []

    for feature, value in ranked:

        if value > 0:
            impact = "defavorable"
        elif value < 0:
            impact = "favorable"
        else:
            impact = "neutre"

        clean_feature = (
            feature
            .replace("num__", "")
            .replace("cat__", "")
        )

        factors.append({
            "feature": clean_feature,
            "impact": impact,
            "contribution": round(float(value), 4)
        })

    return factors


@app.post("/predict")
def predict(request: CreditRequest):

    raw_data = {
        "duration_months": request.duration_months,
        "credit_amount": request.credit_amount,
        "purpose": request.purpose,
        "employment_years": request.employment_years,
        "personal_status": request.personal_status,
        "age": request.age,
        "job": request.job,
        "dependents": request.dependents,
        "telephone": request.telephone
    }

    data = build_prediction_features(raw_data)

    probability_default = float(
        model.predict_proba(data)[0][1]
    )

    prediction = int(
        model.predict(data)[0]
    )

    if probability_default < 0.15:
        risk_level = "FAIBLE"
    elif probability_default < 0.40:
        risk_level = "MOYEN"
    else:
        risk_level = "ELEVE"

    credit_score = int(
        round((1 - probability_default) * 1000)
    )

    factors = explain_prediction(data)

    return {
        "prediction": prediction,
        "probability_default": round(
            probability_default,
            4
        ),
        "risk_level": risk_level,
        "credit_score": credit_score,
        "factors": factors
    }
