package com.example.sumasapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.MathContext;

public class MainActivity extends AppCompatActivity {

    private TextInputLayout inputLayoutNum1;
    private TextInputLayout inputLayoutNum2;
    private TextInputEditText editTextNum1;
    private TextInputEditText editTextNum2;
    private TextView tvResultValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Apply Window Insets for Edge-to-Edge display
        View mainScrollView = findViewById(R.id.mainScrollView);
        if (mainScrollView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainScrollView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Initialize Views
        inputLayoutNum1 = findViewById(R.id.inputLayoutNum1);
        inputLayoutNum2 = findViewById(R.id.inputLayoutNum2);
        editTextNum1 = findViewById(R.id.editTextNum1);
        editTextNum2 = findViewById(R.id.editTextNum2);
        MaterialButton btnCalculate = findViewById(R.id.btnCalculate);
        MaterialButton btnClear = findViewById(R.id.btnClear);
        tvResultValue = findViewById(R.id.tvResultValue);
        ImageButton btnCopyResult = findViewById(R.id.btnCopyResult);

        // Set up Listeners
        btnCalculate.setOnClickListener(v -> calculateSum());
        btnClear.setOnClickListener(v -> clearAll());
        btnCopyResult.setOnClickListener(v -> copyResultToClipboard());

        // IME Action Done on second input field
        editTextNum2.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                calculateSum();
                return true;
            }
            return false;
        });

        // Clear error text on user input
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                inputLayoutNum1.setError(null);
                inputLayoutNum2.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editTextNum1.addTextChangedListener(textWatcher);
        editTextNum2.addTextChangedListener(textWatcher);
    }

    /**
     * Performs the sum calculation with full validation and precision handling.
     */
    private void calculateSum() {
        hideKeyboard();
        inputLayoutNum1.setError(null);
        inputLayoutNum2.setError(null);

        String strNum1 = editTextNum1.getText() != null ? editTextNum1.getText().toString().trim() : "";
        String strNum2 = editTextNum2.getText() != null ? editTextNum2.getText().toString().trim() : "";

        boolean isValid = true;

        if (strNum1.isEmpty()) {
            inputLayoutNum1.setError(getString(R.string.error_campo_requerido));
            isValid = false;
        }

        if (strNum2.isEmpty()) {
            inputLayoutNum2.setError(getString(R.string.error_campo_requerido));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        try {
            BigDecimal n1 = new BigDecimal(strNum1);
            BigDecimal n2 = new BigDecimal(strNum2);
            BigDecimal sum = n1.add(n2, MathContext.DECIMAL128);

            // Format result cleanly: strip trailing zeros for integer-like values
            String formattedResult;
            if (sum.compareTo(BigDecimal.ZERO) == 0) {
                formattedResult = "0";
            } else {
                formattedResult = sum.stripTrailingZeros().toPlainString();
            }

            tvResultValue.setText(formattedResult);

        } catch (NumberFormatException e) {
            if (!isValidNumber(strNum1)) {
                inputLayoutNum1.setError(getString(R.string.error_numero_invalido));
            }
            if (!isValidNumber(strNum2)) {
                inputLayoutNum2.setError(getString(R.string.error_numero_invalido));
            }
        }
    }

    private boolean isValidNumber(String str) {
        try {
            new BigDecimal(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clears all inputs and resets the calculation state.
     */
    private void clearAll() {
        editTextNum1.setText("");
        editTextNum2.setText("");
        inputLayoutNum1.setError(null);
        inputLayoutNum2.setError(null);
        tvResultValue.setText(getString(R.string.resultado_placeholder));
        editTextNum1.requestFocus();
    }

    /**
     * Copies the current result value to system clipboard.
     */
    private void copyResultToClipboard() {
        String result = tvResultValue.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Resultado SumasApp", result);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, getString(R.string.copiado_al_portapapeles), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Soft keyboard helper method.
     */
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
