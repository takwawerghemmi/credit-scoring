import time
from pathlib import Path

import joblib
import pandas as pd

from sklearn.model_selection import train_test_split
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder
from sklearn.pipeline import Pipeline
from sklearn.impute import SimpleImputer
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from ai_module.src.evaluate import evaluate_model

from catboost import CatBoostClassifier
from lightgbm import LGBMClassifier

DATA = "ai_module/data/processed/dataset_ml_final.csv"
OUT = Path("ai_module/models")
OUT.mkdir(parents=True, exist_ok=True)

df = pd.read_csv(DATA)

X = df.drop(columns=["default"])
y = df["default"]

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.20, random_state=42, stratify=y
)

cat_cols = X_train.select_dtypes(include=["object"]).columns.tolist()
num_cols = X_train.select_dtypes(exclude=["object"]).columns.tolist()

preprocessor = ColumnTransformer([
    ("num", SimpleImputer(strategy="median"), num_cols),
    ("cat", Pipeline([
        ("imputer", SimpleImputer(strategy="most_frequent")),
        ("onehot", OneHotEncoder(handle_unknown="ignore", sparse_output=False))
    ]), cat_cols)
])

models = {
    "Logistic Regression": LogisticRegression(max_iter=5000, solver="liblinear", random_state=42),
    "Random Forest": RandomForestClassifier(
        n_estimators=300, random_state=42, n_jobs=-1
    ),
    "CatBoost": CatBoostClassifier(
        iterations=300, depth=6, learning_rate=0.05,
        verbose=False, random_seed=42
    ),
    "LightGBM": LGBMClassifier(
        n_estimators=300, learning_rate=0.05,
        random_state=42, verbosity=-1
    )
}

results = []
trained = {}

for name, model in models.items():
    print("\n" + "=" * 50)
    print(name)
    print("=" * 50)

    start = time.time()

    pipe = Pipeline([
        ("preprocessor", preprocessor),
        ("model", model)
    ])

    pipe.fit(X_train, y_train)

    metrics = evaluate_model(
        pipe,
        X_test,
        y_test
    )

    results.append({
        "model": name,
        **metrics,
        "TrainingTime": time.time() - start
    })

    trained[name] = pipe

results_df = pd.DataFrame(results).sort_values("AUC", ascending=False)

print("\n" + "=" * 60)
print("COMPARAISON DES MODELES")
print("=" * 60)
print(results_df.to_string(index=False))

best_name = results_df.iloc[0]["model"]
best_model = trained[best_name]

print("\nMEILLEUR MODELE :", best_name)

results_df.to_csv(OUT / "model_comparison.csv", index=False)
joblib.dump(best_model, OUT / "best_model.joblib")
joblib.dump(
    {
        "model_name": best_name,
        "features": list(X.columns),
        "categorical_features": cat_cols,
        "numeric_features": num_cols
    },
    OUT / "model_metadata.joblib"
)

print("\nFICHIERS CREES :")
print("ai_module/models/model_comparison.csv")
print("ai_module/models/best_model.joblib")
print("ai_module/models/model_metadata.joblib")
