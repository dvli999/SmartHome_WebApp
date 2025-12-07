import sys
import pandas as pd
import joblib
import os

# --- SCRIPT CONFIGURATION ---
# These filenames should match the files in your /ml directory
MODEL_FILENAME = 'rf_modele_energie.pkl'
SCALER_FILENAME = 'scaler_energie.pkl'
FEATURES_FILENAME = 'features_energie.pkl'


def predict_energy(heure, jour, weekend):
    """
    Predicts energy consumption using a pre-trained model.
    This function is designed to be highly robust.
    """
    try:
        # --- Robust File Path Logic ---
        # This finds the files next to the script, no matter where you run it from.
        script_dir = os.path.dirname(os.path.abspath(__file__))
        model_path = os.path.join(script_dir, MODEL_FILENAME)
        scaler_path = os.path.join(script_dir, SCALER_FILENAME)
        features_path = os.path.join(script_dir, FEATURES_FILENAME)

        # --- File Existence Check ---
        for path in [model_path, scaler_path, features_path]:
            if not os.path.exists(path):
                # This is a critical error, print it to stderr for Java to see
                print(f"FATAL ERROR: Required file not found at '{path}'", file=sys.stderr)
                # Fallback to simulation
                return simulate_prediction(heure, jour, weekend)

        # --- Load Model and Scaler ---
        rf_model = joblib.load(model_path)
        scaler = joblib.load(scaler_path)
        features = joblib.load(features_path)

        # --- Prepare Data for Prediction ---
        input_data = pd.DataFrame([{
            'heure_jour': heure,
            'jour_semaine': jour,
            'weekend': weekend
        }])

        # Ensure the columns are in the correct order
        input_data = input_data[features]

        # Scale the features
        X_futur = scaler.transform(input_data)

        # --- Make Prediction ---
        prediction = rf_model.predict(X_futur)[0]

        return round(prediction, 1)

    except Exception as e:
        # Print any other error to stderr for debugging
        print(f"ERROR during prediction: {e}", file=sys.stderr)
        # Fallback to simulation if the model fails for any reason
        return simulate_prediction(heure, jour, weekend)


def simulate_prediction(heure, jour, weekend):
    """
    Simulates energy prediction as a fallback.
    """
    base = 35.0
    if 0 <= heure < 6: consumption = base - 10
    elif 6 <= heure < 9: consumption = base + 15
    elif 9 <= heure < 17: consumption = base + 5
    elif 17 <= heure < 22: consumption = base + 25
    else: consumption = base
    if weekend == 1 and 9 <= heure < 17: consumption += 10
    consumption += (jour % 3) * 2
    return round(consumption, 1)


def main():
    """
    Main entry point. Parses arguments, calls prediction, and prints output.
    """
    # Add a debug print to show exactly what arguments the script received
    print(f"DEBUG: Script received arguments: {sys.argv}", file=sys.stderr)

    # --- Argument Parsing and Validation ---
    if len(sys.argv) < 4:
        print("Usage: python predict_ml.py <heure> <jour> <weekend>", file=sys.stderr)
        sys.exit(1)

    try:
        heure = int(sys.argv[1])
        jour = int(sys.argv[2])
        weekend = int(sys.argv[3])

        if not (0 <= heure <= 23): raise ValueError("Hour must be between 0-23")
        if not (1 <= jour <= 7): raise ValueError("Day must be between 1-7")
        if weekend not in [0, 1]: raise ValueError("Weekend must be 0 or 1")

        # --- Get Prediction ---
        prediction = predict_energy(heure, jour, weekend)

        # --- Print Formatted Output for Java ---
        # This is the line your Java code reads
        print(f"Predicted energy consumption: {prediction} kWh")

    except ValueError as e:
        print(f"FATAL ERROR: Invalid input argument. {e}", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"FATAL ERROR: An unexpected error occurred in main: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
