from pathlib import Path
import pandas as pd


RAW_DATA_PATH = Path(
    "ai_module/data/raw/dataset.csv"
)

PROCESSED_DATA_PATH = Path(
    "ai_module/data/processed/dataset_processed.csv"
)

ML_DATA_PATH = Path(
    "ai_module/data/processed/dataset_ml_final.csv"
)


def load_raw_dataset() -> pd.DataFrame:
    """Charge le dataset brut."""
    return pd.read_csv(RAW_DATA_PATH)


def load_processed_dataset() -> pd.DataFrame:
    """Charge le dataset déjà préparé."""
    return pd.read_csv(PROCESSED_DATA_PATH)


def load_ml_dataset() -> pd.DataFrame:
    """Charge le dataset final destiné au Machine Learning."""
    return pd.read_csv(ML_DATA_PATH)


def validate_target(df: pd.DataFrame) -> None:
    """Vérifie que la variable cible default existe et est exploitable."""

    if "default" not in df.columns:
        raise ValueError(
            "La colonne cible 'default' est absente du dataset ML."
        )

    if df["default"].isna().any():
        raise ValueError(
            "La colonne 'default' contient des valeurs manquantes."
        )

    values = set(df["default"].unique())

    if not values.issubset({0, 1}):
        raise ValueError(
            "La colonne 'default' doit contenir uniquement 0 et 1."
        )


def validate_dataset(df: pd.DataFrame) -> None:
    """Vérifications générales du dataset."""

    if df.empty:
        raise ValueError("Le dataset est vide.")

    validate_target(df)


if __name__ == "__main__":
    df = load_ml_dataset()

    validate_dataset(df)

    print("Préprocessing : OK")
    print("Dimensions :", df.shape)
    print("Target :", df["default"].value_counts().to_dict())
    print("Colonnes :", df.columns.tolist())
