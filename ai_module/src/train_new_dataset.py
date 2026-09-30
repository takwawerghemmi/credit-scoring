import os
import time
import joblib
import pandas as pd

from sklearn.model_selection import train_test_split
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from sklearn.pipeline import Pipeline

from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier

from sklearn.metrics import (
    roc_auc_score,
    precision_score,
    recall_score,
    f1_score
)


from catboost import CatBoostClassifier
from lightgbm import LGBMClassifier


# ============================================================
# CONFIGURATION
# ============================================================

DATA_PATH = "../data_new/prof_dataset.csv"
OUTPUT_DIR = "../models/nouveau_dataset"

os.makedirs(OUTPUT_DIR, exist_ok=True)


# ============================================================
# 1. CHARGEMENT DU DATASET
# ============================================================

print("=" * 70)
print("CREDIT SCORING - NOUVEAU DATASET")
print("=" * 70)

df = pd.read_csv(DATA_PATH)

print("\nDataset chargé.")
print("Dimensions :", df.shape)

print("\nColonnes :")
print(df.columns.tolist())

print("\nPremières lignes :")
print(df.head())

print("\nValeurs manquantes :")
print(df.isnull().sum())


# ============================================================
# 2. VERIFICATION DE LA CIBLE
# ============================================================

if "default" not in df.columns:
    raise ValueError(
        "ERREUR : la colonne 'default' n'existe pas dans le dataset."
    )

print("\nRépartition de default :")
print(df["default"].value_counts())

print("\nPourcentage :")
print(df["default"].value_counts(normalize=True) * 100)


# ============================================================
# 3. SUPPRESSION DES COLONNES INUTILES / FUITE DE DONNEES
# ============================================================


columns_to_remove = [
    "id",
    "client_id",
    "proba_default",
    "credit_score",
    "risk_level",
    "status",
    "created_at"
]

columns_to_remove = [
    col for col in columns_to_remove
    if col in df.columns
]

if columns_to_remove:
    print("\nColonnes supprimées :")
    print(columns_to_remove)

    df = df.drop(columns=columns_to_remove)


# ============================================================
# 4. SEPARATION X / y
# ============================================================

X = df.drop(columns=["default"])
y = df["default"]

print("\n" + "=" * 70)
print("VARIABLES ML")
print("=" * 70)

print("\nVariables explicatives :")
print(X.columns.tolist())

print("\nVariable cible : default")


# ============================================================
# 5. TRAIN / TEST
# ============================================================

X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.20,
    random_state=42,
    stratify=y
)

print("\nTaille train :", X_train.shape)
print("Taille test  :", X_test.shape)


# ============================================================
# 6. IDENTIFICATION DES VARIABLES
# ============================================================

numeric_features = X.select_dtypes(
    include=["int64", "float64"]
).columns.tolist()

categorical_features = X.select_dtypes(
    include=["object"]
).columns.tolist()

print("\nVariables numériques :")
print(numeric_features)

print("\nVariables catégorielles :")
print(categorical_features)


# ============================================================
# 7. PREPROCESSING
# ============================================================

preprocessor = ColumnTransformer(
    transformers=[
        (
            "num",
            StandardScaler(),
            numeric_features
        ),
        (
            "cat",
            OneHotEncoder(handle_unknown="ignore"),
            categorical_features
        )
    ]
)


# ============================================================
# 8. LOGISTIC REGRESSION
# ============================================================

print("\n" + "=" * 70)
print("1 - LOGISTIC REGRESSION")
print("=" * 70)

start_time = time.time()

logistic_model = Pipeline([
    ("preprocessor", preprocessor),
    (
        "classifier",
        LogisticRegression(
            max_iter=1000,
            class_weight="balanced",
            random_state=42
        )
    )
])

logistic_model.fit(X_train, y_train)

logistic_time = time.time() - start_time

print("Logistic Regression terminée.")
print("Temps :", round(logistic_time, 3), "secondes")


# ============================================================
# 9. RANDOM FOREST
# ============================================================

print("\n" + "=" * 70)
print("2 - RANDOM FOREST")
print("=" * 70)

start_time = time.time()

random_forest_model = Pipeline([
    ("preprocessor", preprocessor),
    (
        "classifier",
        RandomForestClassifier(
            n_estimators=300,
            max_depth=8,
            class_weight="balanced",
            random_state=42
        )
    )
])

random_forest_model.fit(X_train, y_train)

random_forest_time = time.time() - start_time

print("Random Forest terminée.")
print("Temps :", round(random_forest_time, 3), "secondes")


# ============================================================
# 10. PREPARATION CATBOOST
# ============================================================

X_cat = X.copy()

for col in X_cat.select_dtypes(
    include=["object"]
).columns:

    X_cat[col] = X_cat[col].fillna("Unknown")


X_cat_train, X_cat_test, y_cat_train, y_cat_test = train_test_split(
    X_cat,
    y,
    test_size=0.20,
    random_state=42,
    stratify=y
)

cat_features = X_cat.select_dtypes(
    include=["object"]
).columns.tolist()

cat_indices = [
    X_cat.columns.get_loc(col)
    for col in cat_features
]


# ============================================================
# 11. CATBOOST
# ============================================================

print("\n" + "=" * 70)
print("3 - CATBOOST")
print("=" * 70)

start_time = time.time()

catboost_model = CatBoostClassifier(
    iterations=500,
    learning_rate=0.05,
    depth=6,
    eval_metric="AUC",
    auto_class_weights="Balanced",
    verbose=100,
    random_seed=42
)

catboost_model.fit(
    X_cat_train,
    y_cat_train,
    cat_features=cat_indices
)

catboost_time = time.time() - start_time

print("CatBoost terminée.")
print("Temps :", round(catboost_time, 3), "secondes")


# ============================================================
# 12. LIGHTGBM
# ============================================================


print("\n" + "=" * 70)
print("4 - LIGHTGBM")
print("=" * 70)

start_time = time.time()

lightgbm_model = Pipeline([
    ("preprocessor", preprocessor),
    (
        "classifier",
        LGBMClassifier(
            objective="binary",
            learning_rate=0.05,
            n_estimators=500,
            num_leaves=31,
            class_weight="balanced",
            random_state=42,
            verbosity=-1
        )
    )
])

lightgbm_model.fit(X_train, y_train)

lightgbm_time = time.time() - start_time

print("LightGBM terminée.")
print("Temps :", round(lightgbm_time, 3), "secondes")


# ============================================================
# 13. FONCTION D'EVALUATION
# ============================================================

def evaluate_model(
    name,
    model,
    X_eval,
    y_eval,
    training_time
):

    y_proba = model.predict_proba(X_eval)[:, 1]

    y_pred = (y_proba >= 0.5).astype(int)

    return {
        "Model": name,

        "AUC": roc_auc_score(
            y_eval,
            y_proba
        ),

        "Precision": precision_score(
            y_eval,
            y_pred,
            zero_division=0
        ),

        "Recall": recall_score(
            y_eval,
            y_pred,
            zero_division=0
        ),

        "F1": f1_score(
            y_eval,
            y_pred,
            zero_division=0
        ),

        "Training_Time_Seconds": training_time
    }


# ============================================================
# 14. EVALUATION DES 4 MODELES
# ============================================================

results = []


results.append(
    evaluate_model(
        "Logistic Regression",
        logistic_model,
        X_test,
        y_test,
        logistic_time
    )
)


results.append(
    evaluate_model(
        "Random Forest",
        random_forest_model,
        X_test,
        y_test,
        random_forest_time
    )
)


results.append(
    evaluate_model(
        "CatBoost",
        catboost_model,
        X_cat_test,
        y_cat_test,
        catboost_time
    )
)


results.append(
    evaluate_model(
        "LightGBM",
        lightgbm_model,
        X_test,
        y_test,
        lightgbm_time
    )
)


# ============================================================
# 15. TABLEAU DE COMPARAISON
# ============================================================

results_df = pd.DataFrame(results)

print("\n")
print("=" * 70)
print("COMPARAISON DES 4 MODELES")
print("=" * 70)

print(
    results_df.to_string(index=False)
)


# ============================================================
# 16. SAUVEGARDE DES RESULTATS
# ============================================================

comparison_path = os.path.join(
    OUTPUT_DIR,
    "model_comparison_new_dataset.csv"
)

results_df.to_csv(
    comparison_path,
    index=False
)

print("\nRésultats sauvegardés ici :")
print(comparison_path)


# ============================================================
# 17. SAUVEGARDE DES MODELES
# ============================================================

joblib.dump(
    logistic_model,
    os.path.join(
        OUTPUT_DIR,
        "logistic_regression_new.joblib"
    )
)

joblib.dump(
    random_forest_model,
    os.path.join(
        OUTPUT_DIR,
        "random_forest_new.joblib"
    )
)

joblib.dump(
    lightgbm_model,
    os.path.join(
        OUTPUT_DIR,
        "lightgbm_new.joblib"
    )
)

catboost_model.save_model(
    os.path.join(
        OUTPUT_DIR,
        "catboost_new.cbm"
    )
)


# ============================================================
# 18. FIN
# ============================================================

print("\n")
print("=" * 70)
print("TERMINE")
print("=" * 70)

print("Les 4 modèles ont été entraînés.")
print("Les résultats ont été sauvegardés.")
print("Les modèles ont été sauvegardés.")

print("\nDossier :")
print(OUTPUT_DIR)