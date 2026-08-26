import pandas as pd


FEATURE_COLUMNS = [
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


def split_features_and_target(
    df: pd.DataFrame,
):
    """
    Sépare les variables explicatives de la variable cible.
    """

    missing = [
        col
        for col in FEATURE_COLUMNS
        if col not in df.columns
    ]

    if missing:
        raise ValueError(
            f"Features manquantes dans le dataset : {missing}"
        )

    if "default" not in df.columns:
        raise ValueError(
            "La colonne 'default' est absente."
        )

    X = df[FEATURE_COLUMNS].copy()
    y = df["default"].copy()

    return X, y


def validate_features(X: pd.DataFrame) -> None:
    """
    Vérifie l'ordre et le nombre des features.
    """

    if list(X.columns) != FEATURE_COLUMNS:
        raise ValueError(
            "L'ordre des features ne correspond pas "
            "à FEATURE_COLUMNS."
        )

    if len(X.columns) != 9:
        raise ValueError(
            "Le modèle doit utiliser exactement 9 features."
        )


if __name__ == "__main__":
    from preprocessing import load_ml_dataset

    df = load_ml_dataset()

    X, y = split_features_and_target(df)

    validate_features(X)

    print("Feature engineering : OK")
    print("Nombre de features :", len(X.columns))
    print("Features :")
    for i, feature in enumerate(X.columns, start=1):
        print(f"{i}. {feature}")

    print("\nTarget :", y.value_counts().to_dict())
