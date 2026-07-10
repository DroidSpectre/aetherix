package com.aetherix.search;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private EditText etApiKey;
    private RadioGroup rgSearchDepth;
    private RadioButton rbBasic, rbAdvanced, rbFast, rbUltraFast;
    private RadioGroup rgMaxResults;
    private Switch swIncludeAnswer, swIncludeRawContent, swIncludeImages, swIncludeImageDesc, swIncludeFavicon;
    private EditText etDefaultTopic, etTimeRange, etCountry;
    private Button btnSave, btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        loadSettings();
        
        btnSave.setOnClickListener(v -> saveSettings());
        btnReset.setOnClickListener(v -> resetToDefaults());
    }

    private void initViews() {
        etApiKey = findViewById(R.id.etApiKey);
        etApiKey.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        rgSearchDepth = findViewById(R.id.rgSearchDepth);
        rbBasic = findViewById(R.id.rbBasic);
        rbAdvanced = findViewById(R.id.rbAdvanced);
        rbFast = findViewById(R.id.rbFast);
        rbUltraFast = findViewById(R.id.rbUltraFast);

        rgMaxResults = findViewById(R.id.rgMaxResults);

        swIncludeAnswer = findViewById(R.id.swIncludeAnswer);
        swIncludeRawContent = findViewById(R.id.swIncludeRawContent);
        swIncludeImages = findViewById(R.id.swIncludeImages);
        swIncludeImageDesc = findViewById(R.id.swIncludeImageDesc);
        swIncludeFavicon = findViewById(R.id.swIncludeFavicon);

        etDefaultTopic = findViewById(R.id.etDefaultTopic);
        etTimeRange = findViewById(R.id.etTimeRange);
        etCountry = findViewById(R.id.etCountry);

        btnSave = findViewById(R.id.btnSave);
        btnReset = findViewById(R.id.btnReset);
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("aetherix_prefs", MODE_PRIVATE);
        
        etApiKey.setText(prefs.getString("tavily_api_key", ""));

        String depth = prefs.getString("search_depth", "basic");
        switch (depth) {
            case "advanced": rbAdvanced.setChecked(true); break;
            case "fast": rbFast.setChecked(true); break;
            case "ultra-fast": rbUltraFast.setChecked(true); break;
            default: rbBasic.setChecked(true);
        }

        int maxRes = prefs.getInt("max_results", 5);
        switch (maxRes) {
            case 10: ((RadioButton) findViewById(R.id.rb10)).setChecked(true); break;
            case 15: ((RadioButton) findViewById(R.id.rb15)).setChecked(true); break;
            case 20: ((RadioButton) findViewById(R.id.rb20)).setChecked(true); break;
            default: ((RadioButton) findViewById(R.id.rb5)).setChecked(true);
        }

        swIncludeAnswer.setChecked(prefs.getBoolean("include_answer", true));
        swIncludeRawContent.setChecked(prefs.getBoolean("include_raw_content", false));
        swIncludeImages.setChecked(prefs.getBoolean("include_images", false));
        swIncludeImageDesc.setChecked(prefs.getBoolean("include_image_descriptions", false));
        swIncludeFavicon.setChecked(prefs.getBoolean("include_favicon", false));

        etDefaultTopic.setText(prefs.getString("default_topic", ""));
        etTimeRange.setText(prefs.getString("time_range", ""));
        etCountry.setText(prefs.getString("country", ""));
    }

    private void saveSettings() {
        SharedPreferences prefs = getSharedPreferences("aetherix_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String apiKey = etApiKey.getText().toString().trim();
        if (!apiKey.isEmpty()) {
            editor.putString("tavily_api_key", apiKey);
        }

        String depth = "basic";
        if (rbAdvanced.isChecked()) depth = "advanced";
        else if (rbFast.isChecked()) depth = "fast";
        else if (rbUltraFast.isChecked()) depth = "ultra-fast";
        editor.putString("search_depth", depth);

        int maxResults = 5;
        if (((RadioButton) findViewById(R.id.rb10)).isChecked()) maxResults = 10;
        else if (((RadioButton) findViewById(R.id.rb15)).isChecked()) maxResults = 15;
        else if (((RadioButton) findViewById(R.id.rb20)).isChecked()) maxResults = 20;
        editor.putInt("max_results", maxResults);

        editor.putBoolean("include_answer", swIncludeAnswer.isChecked());
        editor.putBoolean("include_raw_content", swIncludeRawContent.isChecked());
        editor.putBoolean("include_images", swIncludeImages.isChecked());
        editor.putBoolean("include_image_descriptions", swIncludeImageDesc.isChecked());
        editor.putBoolean("include_favicon", swIncludeFavicon.isChecked());

        editor.putString("default_topic", etDefaultTopic.getText().toString().trim());
        editor.putString("time_range", etTimeRange.getText().toString().trim());
        editor.putString("country", etCountry.getText().toString().trim());

        editor.apply();
        Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void resetToDefaults() {
        getSharedPreferences("aetherix_prefs", MODE_PRIVATE).edit().clear().apply();
        loadSettings();
        Toast.makeText(this, "Reset to defaults", Toast.LENGTH_SHORT).show();
    }
}