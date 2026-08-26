import pandas as pd


FEATURE_ORDER = [
    "Duration in months",
    "Credit amount",
    "Purpose of the credit",
    "Present employment(years)",
    "personal_status",
    "Age in years",
    "Job",
    "Number of people being liable to provide maintenance for",
    "Telephone",
]


def build_prediction_features(data: dict) -> pd.DataFrame:
    """
    Construit les features utilisées par le modèle ML
    dans le même ordre que lors de l'entraînement.
    """

    row = {
        "Duration in months": data["duration_months"],
        "Credit amount": data["credit_amount"],
        "Purpose of the credit": data["purpose"],
        "Present employment(years)": data["employment_years"],
        "personal_status": data["personal_status"],
        "Age in years": data["age"],
        "Job": data["job"],
        "Number of people being liable to provide maintenance for":
            data["dependents"],
        "Telephone": data["telephone"],
    }

    return pd.DataFrame([row], columns=FEATURE_ORDER)
