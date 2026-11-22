package com.example.demointent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class TextAnalyzerActivity extends AppCompatActivity {

    public static final String EXTRA_TEXT_INPUT = "com.example.demointent.EXTRA_TEXT_INPUT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_text_analyzer);

        TextView tvOriginalText = findViewById(R.id.tvOriginalText);
        TextView tvCharCount = findViewById(R.id.tvCharCount);
        TextView tvWordCount = findViewById(R.id.tvWordCount);
        TextView tvVowelCount = findViewById(R.id.tvVowelCount);
        TextView tvReadTime = findViewById(R.id.tvReadTime);
        TextView tvUppercase = findViewById(R.id.tvUppercase);
        TextView tvReversed = findViewById(R.id.tvReversed);
        MaterialButton btnBackAnalyzer = findViewById(R.id.btnBackAnalyzer);

        Intent intent = getIntent();
        String rawText = intent.getStringExtra(EXTRA_TEXT_INPUT);
        String text = rawText != null ? rawText : "";

        tvOriginalText.setText(text);

        // Analysis logic
        int charCount = text.length();
        int wordCount = countWords(text);
        int vowelCount = countVowels(text);
        String readTimeStr = estimateReadTime(wordCount);

        tvCharCount.setText(String.format(Locale.getDefault(), "%d", charCount));
        tvWordCount.setText(String.format(Locale.getDefault(), "%d", wordCount));
        tvVowelCount.setText(String.format(Locale.getDefault(), "%d", vowelCount));
        tvReadTime.setText(readTimeStr);

        tvUppercase.setText(text.toUpperCase(Locale.getDefault()));
        tvReversed.setText(new StringBuilder(text).reverse().toString());

        btnBackAnalyzer.setOnClickListener(v -> finish());
    }

    private int countWords(String input) {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        return trimmed.split("\\s+").length;
    }

    private int countVowels(String input) {
        int count = 0;
        String lower = input.toLowerCase(Locale.getDefault());
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' ||
                c == 'á' || c == 'é' || c == 'í' || c == 'ó' || c == 'ú') {
                count++;
            }
        }
        return count;
    }

    private String estimateReadTime(int words) {
        if (words <= 0) return "0 seg";
        double seconds = (words / 200.0) * 60.0;
        if (seconds < 5) {
            return "< 5 seg";
        }
        return String.format(Locale.getDefault(), "%.0f seg", seconds);
    }
}
