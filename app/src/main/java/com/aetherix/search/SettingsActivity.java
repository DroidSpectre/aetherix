package com.aetherix.search;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private EditText etApiKey, etCountry, etIncludeDomains, etExcludeDomains;
    private SwitchCompat swAutoMode, swIncludeAnswer, swIncludeRawContent, swIncludeImages,
            swIncludeImageDesc, swIncludeFavicon;
    private RadioGroup rgSearchDepth, rgMaxResults;
    private Spinner spinnerTopic, spinnerTimeRange;
    private LinearLayout manualSettingsContainer;
    private Button btnSave, btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        setupSpinners();
        loadSettings();

        swAutoMode.setOnCheckedChangeListener((buttonView, isChecked) -> updateUIState(isChecked));

        btnSave.setOnClickListener(v -> saveSettings());
        btnReset.setOnClickListener(v -> resetToDefaults());
    }

    private void initViews() {
        etApiKey = findViewById(R.id.etApiKey);
        swAutoMode = findViewById(R.id.swAutoMode);
        manualSettingsContainer = findViewById(R.id.manualSettingsContainer);

        rgSearchDepth = findViewById(R.id.rgSearchDepth);
        rgMaxResults = findViewById(R.id.rgMaxResults);

        swIncludeAnswer = findViewById(R.id.swIncludeAnswer);
        swIncludeRawContent = findViewById(R.id.swIncludeRawContent);
        swIncludeImages = findViewById(R.id.swIncludeImages);
        swIncludeImageDesc = findViewById(R.id.swIncludeImageDesc);
        swIncludeFavicon = findViewById(R.id.swIncludeFavicon);

        spinnerTopic = findViewById(R.id.spinnerTopic);
        spinnerTimeRange = findViewById(R.id.spinnerTimeRange);

        etCountry = findViewById(R.id.etCountry);
        etIncludeDomains = findViewById(R.id.etIncludeDomains);
        etExcludeDomains = findViewById(R.id.etExcludeDomains);

        btnSave = findViewById(R.id.btnSave);
        btnReset = findViewById(R.id.btnReset);
    }

    private void setupSpinners() {
        String[] topics = {"None", "General", "News", "Finance"};
        ArrayAdapter<String> topicAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, topics);
        topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTopic.setAdapter(topicAdapter);

        String[] timeRanges = {"None", "Day", "Week", "Month", "Year"};
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeRanges);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeRange.setAdapter(timeAdapter);
    }

    private void updateUIState(boolean autoEnabled) {
        manualSettingsContainer.setAlpha(autoEnabled ? 0.5f : 1.0f);
        for (int i = 0; i < manualSettingsContainer.getChildCount(); i++) {
            View child = manualSettingsContainer.getChildAt(i);
            if (child != null) child.setEnabled(!autoEnabled);
        }
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("aetherix_prefs", MODE_PRIVATE);
        
        etApiKey.setText(prefs.getString("tavily_api_key", ""));

        boolean autoMode = prefs.getBoolean("auto_mode", true);
        swAutoMode.setChecked(autoMode);
        updateUIState(autoMode);

        // Depth
        String depth = prefs.getString("search_depth", "basic");
        switch (depth) {
            case "advanced": ((RadioButton) findViewById(R.id.rbAdvanced)).setChecked(true); break;
            case "fast": ((RadioButton) findViewById(R.id.rbFast)).setChecked(true); break;
            case "ultra-fast": ((RadioButton) findViewById(R.id.rbUltraFast)).setChecked(true); break;
            default: ((RadioButton) findViewById(R.id.rbBasic)).setChecked(true);
        }

        // Max Results
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

        // Spinners
        String topic = prefs.getString("default_topic", "None");
        int topicPos = ((ArrayAdapter<String>) spinnerTopic.getAdapter()).getPosition(topic);
        spinnerTopic.setSelection(Math.max(0, topicPos));

        String timeRange = prefs.getString("time_range", "None");
        int timePos = ((ArrayAdapter<String>) spinnerTimeRange.getAdapter()).getPosition(timeRange);
        spinnerTimeRange.setSelection(Math.max(0, timePos));

        etCountry.setText(prefs.getString("country", ""));
        etIncludeDomains.setText(prefs.getString("include_domains", ""));
        etExcludeDomains.setText(prefs.getString("exclude_domains", ""));
    }

    private void saveSettings() {
        SharedPreferences prefs = getSharedPreferences("aetherix_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String apiKey = etApiKey.getText().toString().trim();
        if (!apiKey.isEmpty()) editor.putString("tavily_api_key", apiKey);

        // ═══════════════════════════════════════════════════════════════
        // FIX: Always save the auto_mode flag AND all manual params.
        // Previously, manual params were only saved when auto was OFF,
        // leaving stale values in SharedPreferences.
        // ═══════════════════════════════════════════════════════════════
        editor.putBoolean("auto_mode", swAutoMode.isChecked());

        // Always save ALL manual params so they are preserved when user
        // switches back from Auto mode. Tavily docs say these are
        // always sent (some required, some optional).

        // Depth
        String depth = "basic";
        if (((RadioButton) findViewById(R.id.rbAdvanced)).isChecked()) depth = "advanced";
        else if (((RadioButton) findViewById(R.id.rbFast)).isChecked()) depth = "fast";
        else if (((RadioButton) findViewById(R.id.rbUltraFast)).isChecked()) depth = "ultra-fast";
        editor.putString("search_depth", depth);

        // Max Results
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

        String topic = spinnerTopic.getSelectedItem().toString();
        if (!"None".equals(topic)) editor.putString("default_topic", topic.toLowerCase());
        else editor.remove("default_topic");

        String timeRange = spinnerTimeRange.getSelectedItem().toString();
        if (!"None".equals(timeRange)) editor.putString("time_range", timeRange.toLowerCase());
        else editor.remove("time_range");

        editor.putString("country", etCountry.getText().toString().trim());
        editor.putString("include_domains", etIncludeDomains.getText().toString().trim());
        editor.putString("exclude_domains", etExcludeDomains.getText().toString().trim());

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
