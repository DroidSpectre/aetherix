package com.aetherix.search;

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
    private TextView resultsView;
    private ProgressBar loadingIndicator;
    private TextView statusView;
    private Spinner topicSpinner;
    private TavilyClient tavilyClient;
    private String selectedTopic = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EnvLoader.load(this);
        String apiKey = EnvLoader.get("TAVILY_API_KEY");

        if (TextUtils.isEmpty(apiKey)) {
            Toast.makeText(this, "API key missing! Check assets/.env", Toast.LENGTH_LONG).show();
        }

        tavilyClient = new TavilyClient(apiKey);

        queryInput = findViewById(R.id.queryInput);
        searchButton = findViewById(R.id.searchButton);
        clearQueryButton = findViewById(R.id.clearQueryButton);
        resultsView = findViewById(R.id.resultsView);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        statusView = findViewById(R.id.statusView);
        topicSpinner = findViewById(R.id.topicSpinner);

        applyReadableColors();

        queryInput.setSingleLine(false);
        queryInput.setHorizontallyScrolling(false);
        queryInput.setMaxLines(5);
        queryInput.setMinLines(2);

        clearQueryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryInput.setText("");
                queryInput.requestFocus();
            }
        });

        String[] topics = new String[]{"All Topics", "General", "News", "Finance"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                topics
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        topicSpinner.setAdapter(adapter);

        topicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedTopic = null;
                } else if (position == 1) {
                    selectedTopic = "general";
                } else if (position == 2) {
                    selectedTopic = "news";
                } else if (position == 3) {
                    selectedTopic = "finance";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTopic = null;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = queryInput.getText().toString().trim();
                if (TextUtils.isEmpty(query)) {
                    Toast.makeText(MainActivity.this, "Please enter a search query", Toast.LENGTH_SHORT).show();
                    return;
                }
                performSearch(query);
            }
        });

        resultsView.setTextIsSelectable(true);
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

        TavilyClient.SearchParams params = new TavilyClient.SearchParams.Builder()
                .setSearchDepth("basic")
                .setMaxResults(10)
                .setIncludeAnswer(true)
                .setIncludeRawContent(false)
                .setTopic(selectedTopic)
                .build();

        tavilyClient.search(query, params, new TavilyClient.TavilyCallback() {
            @Override
            public void onSuccess(String searchResults, TavilySearchResult result) {
                resultsView.setText(searchResults);
                statusView.setText("Search complete • " + result.resultCount + " results • " + result.creditsUsed + " credit(s)");
                loadingIndicator.setVisibility(View.GONE);
                searchButton.setEnabled(true);
            }

            @Override
            public void onError(String error, int errorCode) {
                resultsView.setText("❌ Error: \n" + error);
                statusView.setText("Search failed");
                loadingIndicator.setVisibility(View.GONE);
                searchButton.setEnabled(true);
                Toast.makeText(MainActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}