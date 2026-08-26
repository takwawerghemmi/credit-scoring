import pandas as pd

from sklearn.metrics import (
    roc_auc_score,
    precision_score,
    recall_score,
    f1_score,
)


def evaluate_model(model, X_test, y_test):
    """
    Évalue un modèle de classification.
    """

    prediction = model.predict(X_test)
    probability = model.predict_proba(X_test)[:, 1]

    return {
        "AUC": roc_auc_score(y_test, probability),
        "Precision": precision_score(
            y_test,
            prediction,
            zero_division=0,
        ),
        "Recall": recall_score(
            y_test,
            prediction,
            zero_division=0,
        ),
        "F1": f1_score(
            y_test,
            prediction,
            zero_division=0,
        ),
    }


def build_comparison_table(results):
    """
    Transforme les résultats des modèles
    en DataFrame trié par AUC.
    """

    df = pd.DataFrame(results)

    if df.empty:
        raise ValueError(
            "Aucun résultat de modèle à comparer."
        )

    return df.sort_values(
        "AUC",
        ascending=False,
    ).reset_index(drop=True)
