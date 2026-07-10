package com.aetherix.search;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private EditText queryInput;
    private Button searchButton;
    private Button clearQueryButton;
    private Button settingsButton;
    private TextView resultsView;
    private ProgressBar loadingIndicator;
    private TextView statusView;
    private Spinner topicSpinner;
    private TavilyClient tavilyClient;

    private String selectedTopic = null;
    private String lastResultsText = "";
    private String lastStatusText = "Ready";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load API key from SharedPreferences (no .env)
        SharedPreferences prefs = getSharedPreferences("aetherix_prefs", MODE_PRIVATE);
        String apiKey = prefs.getString("tavily_api_key", "").trim();

        if (TextUtils.isEmpty(apiKey)) {
            Toast.makeText(this, "Please set your Tavily API key in Settings", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "API Key loaded successfully", Toast.LENGTH_SHORT).show();
        }

        tavilyClient = new TavilyClient(apiKey);

        // Initialize views
        queryInput = findViewById(R.id.queryInput);
        searchButton = findViewById(R.id.searchButton);
        clearQueryButton = findViewById(R.id.clearQueryButton);
        settingsButton = findViewById(R.id.settingsButton);
        resultsView = findViewById(R.id.resultsView);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        statusView = findViewById(R.id.statusView);
        topicSpinner = findViewById(R.id.topicSpinner);

        applyReadableColors();

        if (savedInstanceState != null) {
            lastResultsText = savedInstanceState.getString("lastResults", "");
            lastStatusText = savedInstanceState.getString("lastStatus", "Ready");
            resultsView.setText(lastResultsText);
            statusView.setText(lastStatusText);
        }

        setupUI();
        resultsView.setTextIsSelectable(true);
    }

    private void setupUI() {
        queryInput.setSingleLine(false);
        queryInput.setHorizontallyScrolling(false);
        queryInput.setMaxLines(5);
        queryInput.setMinLines(2);

        clearQueryButton.setOnClickListener(v -> {
            queryInput.setText("");
            queryInput.requestFocus();
        });

        settingsButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        String[] topics = new String[]{"All Topics", "General", "News", "Finance"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, topics);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        topicSpinner.setAdapter(adapter);

        topicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTopic = (position == 0) ? null :
                               (position == 1 ? "general" : position == 2 ? "news" : "finance");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTopic = null;
            }
        });

        searchButton.setOnClickListener(v -> {
            String query = queryInput.getText().toString().trim();
            if (TextUtils.isEmpty(query)) {
                Toast.makeText(MainActivity.this, "Please enter a search query", Toast.LENGTH_SHORT).show();
                return;
            }
            performSearch(query);
        });
    }

    private void applyReadableColors() {
        int primary = ContextCompat.getColor(this, R.color.textPrimary);
        int secondary = ContextCompat.getColor(this, R.color.textSecondary);
        int resultsBg = ContextCompat.getColor(this, R.color.resultsBackground);
        int inputBg = ContextCompat.getColor(this, R.color.inputBackground);

        queryInput.setTextColor(primary);
        queryInput.setHintTextColor(secondary);
        queryInput.setBackgroundColor(inputBg);

        statusView.setTextColor(secondary);
        resultsView.setTextColor(primary);
        resultsView.setBackgroundColor(resultsBg);
        topicSpinner.setBackgroundColor(inputBg);
    }

    private void performSearch(String query) {
        resultsView.setText("");
        statusView.setText("Searching...");
        loadingIndicator.setVisibility(View.VISIBLE);
        searchButton.setEnabled(false);

        TavilyClient.SearchParams params = buildSearchParamsFromSettings();

        tavilyClient.search(query, params, new TavilyClient.TavilyCallback() {
            @Override
            public void onSuccess(String searchResults, TavilySearchResult result) {
                lastResultsText = searchResults;
                lastStatusText = "Search complete • " + result.resultCount + " results • " + result.creditsUsed + " credit(s)";

                resultsView.setText(lastResultsText);
                statusView.setText(lastStatusText);
                loadingIndicator.setVisibility(View.GONE);
                searchButton.setEnabled(true);
            }

            @Override
            public void onError(String error, int errorCode) {
                lastResultsText = "❌ Error\n\n" + error;
                lastStatusText = "Search failed";

                resultsView.setText(lastResultsText);
                statusView.setText(lastStatusText);
                loadingIndicator.setVisibility(View.GONE);
                searchButton.setEnabled(true);
                Toast.makeText(MainActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private TavilyClient.SearchParams buildSearchParamsFromSettings() {
        SharedPreferences prefs = getSharedPreferences("aetherix_prefs", MODE_PRIVATE);
        TavilyClient.SearchParams.Builder builder = new TavilyClient.SearchParams.Builder();

        String depth = prefs.getString("search_depth", "basic");
        builder.setSearchDepth(depth);

        int maxResults = prefs.getInt("max_results", 5);
        builder.setMaxResults(maxResults);

        builder.setIncludeAnswer(prefs.getBoolean("include_answer", true));
        builder.setIncludeRawContent(prefs.getBoolean("include_raw_content", false));
        builder.setIncludeImages(prefs.getBoolean("include_images", false));
        builder.setIncludeImageDescriptions(prefs.getBoolean("include_image_descriptions", false));
        builder.setIncludeFavicon(prefs.getBoolean("include_favicon", false));

        String topicToUse = selectedTopic != null ? selectedTopic : prefs.getString("default_topic", null);
        if (topicToUse != null && !topicToUse.isEmpty()) {
            builder.setTopic(topicToUse);
        }

        builder.setTimeRange(prefs.getString("time_range", null));
        builder.setCountry(prefs.getString("country", null));

        return builder.build();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("lastResults", lastResultsText);
        outState.putString("lastStatus", lastStatusText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!lastResultsText.isEmpty()) {
            resultsView.setText(lastResultsText);
        }
    }
}