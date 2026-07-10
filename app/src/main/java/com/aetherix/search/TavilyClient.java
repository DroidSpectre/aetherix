package com.aetherix.search;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TavilyClient {
    private static final String TAG = "TavilyClient";
    private static final String BASE_URL = "https://api.tavily.com/search";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int MAX_QUERY_LENGTH = 400;
    private static final int MIN_QUERY_LENGTH = 1;
    private static final int REQUEST_TIMEOUT_SECONDS = 30;

    private final OkHttpClient client;
    private final Handler mainHandler;
    private final String apiKey;
    private final String sessionId;

    public interface TavilyCallback {
        void onSuccess(String searchResults, TavilySearchResult result);
        void onError(String error, int errorCode);
    }

    public TavilyClient(String apiKey) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.apiKey = apiKey;
        this.sessionId = UUID.randomUUID().toString();
        Log.d(TAG, "TavilyClient initialized with session: " + sessionId.substring(0, 8) + "...");
    }

    public void search(String query, final TavilyCallback callback) {
        search(query, new SearchParams.Builder().build(), callback);
    }

    public void search(String query, SearchParams params, final TavilyCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("Tavily API key not configured. Please go to Settings and enter your key.", 0);
            return;
        }

        if (query == null || query.trim().isEmpty()) {
            callback.onError("Query cannot be empty. Please enter a search term.", 0);
            return;
        }

        if (query.trim().length() < MIN_QUERY_LENGTH) {
            callback.onError("Query is too short. Minimum " + MIN_QUERY_LENGTH + " character(s) required.", 0);
            return;
        }

        final String optimizedQuery = optimizeQuery(query);
        Log.d(TAG, "Optimized query: " + optimizedQuery);

        try {
            JSONObject requestBody = buildRequestBody(optimizedQuery, params);
            RequestBody body = RequestBody.create(requestBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("X-Session-ID", sessionId)
                    .post(body)
                    .build();

            Log.d(TAG, "Executing search request...");

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Network failure: " + e.getMessage());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("Network error: " + e.getMessage() + 
                                ". Check your internet connection.", 0);
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseBody = response.body().string();
                        int statusCode = response.code();
                        
                        Log.d(TAG, "Response code: " + statusCode);
                        
                        if (response.isSuccessful()) {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            final TavilySearchResult result = parseSearchResult(jsonResponse);
                            final String formattedResults = formatSearchResults(jsonResponse, result);
                            
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(formattedResults, result);
                                }
                            });
                        } else {
                            final String errorMsg = parseErrorResponse(responseBody, statusCode);
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(errorMsg, statusCode);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage());
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError("JSON parsing error: " + e.getMessage(), 0);
                            }
                        });
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Request building error: " + e.getMessage());
            callback.onError("Request building error: " + e.getMessage(), 0);
        }
    }

    private JSONObject buildRequestBody(String query, SearchParams params) throws JSONException {
        JSONObject requestBody = new JSONObject();
        
        requestBody.put("query", query);
        requestBody.put("search_depth", params.searchDepth);
        requestBody.put("max_results", params.maxResults);
        requestBody.put("include_answer", params.includeAnswer);
        requestBody.put("include_raw_content", params.includeRawContent);
        requestBody.put("include_usage", true);
        
        if (params.topic != null && !params.topic.isEmpty()) {
            requestBody.put("topic", params.topic);
        }
        
        if (params.timeRange != null && !params.timeRange.isEmpty()) {
            requestBody.put("time_range", params.timeRange);
        }
        
        if (params.country != null && !params.country.isEmpty()) {
            requestBody.put("country", params.country);
        }
        
        if (params.includeDomains != null && params.includeDomains.length > 0) {
            JSONArray domains = new JSONArray();
            for (int i = 0; i < params.includeDomains.length; i = i + 1) {
                domains.put(params.includeDomains[i]);
            }
            requestBody.put("include_domains", domains);
        }
        
        if (params.excludeDomains != null && params.excludeDomains.length > 0) {
            JSONArray domains = new JSONArray();
            for (int i = 0; i < params.excludeDomains.length; i = i + 1) {
                domains.put(params.excludeDomains[i]);
            }
            requestBody.put("exclude_domains", domains);
        }
        
        if (params.includeImages) {
            requestBody.put("include_images", true);
            if (params.includeImageDescriptions) {
                requestBody.put("include_image_descriptions", true);
            }
        }
        
        if (params.includeFavicon) {
            requestBody.put("include_favicon", true);
        }
        
        JSONObject sessionParams = new JSONObject();
        sessionParams.put("session_id", sessionId);
        requestBody.put("params", sessionParams);
        
        return requestBody;
    }

    private String optimizeQuery(String query) {
        String trimmed = query.trim();
        
        if (trimmed.length() <= MAX_QUERY_LENGTH) {
            return trimmed;
        }
        
        int lastSpaceIndex = trimmed.substring(0, MAX_QUERY_LENGTH).lastIndexOf(' ');
        if (lastSpaceIndex > 50) {
            return trimmed.substring(0, lastSpaceIndex);
        }
        
        return trimmed.substring(0, MAX_QUERY_LENGTH) + "...";
    }

    private String parseErrorResponse(String responseBody, int statusCode) {
        String errorMsg = "Tavily API error (" + statusCode + ")";
        
        try {
            JSONObject errorJson = new JSONObject(responseBody);
            if (errorJson.has("detail")) {
                JSONObject detail = errorJson.getJSONObject("detail");
                if (detail.has("error")) {
                    errorMsg = errorMsg + ": " + detail.getString("error");
                }
            } else if (errorJson.has("error")) {
                errorMsg = errorMsg + ": " + errorJson.getString("error");
            }
        } catch (JSONException e) {
            errorMsg = errorMsg + ": " + responseBody;
        }
        
        if (statusCode == 401) {
            errorMsg = errorMsg + ". Check your API key in Settings.";
        } else if (statusCode == 429) {
            errorMsg = errorMsg + ". Rate limit exceeded. Please wait a moment.";
        } else if (statusCode == 432) {
            errorMsg = errorMsg + ". Usage limit exceeded. Upgrade your plan.";
        }
        
        return errorMsg;
    }

    private TavilySearchResult parseSearchResult(JSONObject response) throws JSONException {
        TavilySearchResult result = new TavilySearchResult();
        
        if (response.has("query")) {
            result.query = response.getString("query");
        }
        
        if (response.has("answer")) {
            result.answer = response.getString("answer");
        }
        
        if (response.has("response_time")) {
            result.responseTime = (float) response.getDouble("response_time");
        }
        
        if (response.has("request_id")) {
            result.requestId = response.getString("request_id");
        }
        
        if (response.has("usage")) {
            JSONObject usage = response.getJSONObject("usage");
            result.creditsUsed = usage.optInt("credits", 1);
        }
        
        if (response.has("images")) {
            JSONArray images = response.getJSONArray("images");
            result.images = new java.util.ArrayList<String>();
            for (int i = 0; i < images.length(); i = i + 1) {
                JSONObject img = images.getJSONObject(i);
                result.images.add(img.getString("url"));
            }
        }
        
        if (response.has("results")) {
            JSONArray results = response.getJSONArray("results");
            result.resultCount = results.length();
            result.items = new TavilySearchResult.TavilySearchItem[results.length()];
            
            double totalScore = 0;
            for (int i = 0; i < results.length(); i = i + 1) {
                JSONObject item = results.getJSONObject(i);
                TavilySearchResult.TavilySearchItem searchItem = new TavilySearchResult.TavilySearchItem();
                
                searchItem.title = item.optString("title", "Untitled");
                searchItem.url = item.optString("url", "");
                searchItem.content = item.optString("content", "");
                searchItem.score = item.optDouble("score", 0);
                searchItem.favicon = item.optString("favicon", null);
                
                if (item.has("images")) {
                    JSONArray itemImages = item.getJSONArray("images");
                    searchItem.images = new java.util.ArrayList<String>();
                    for (int j = 0; j < itemImages.length(); j = j + 1) {
                        JSONObject img = itemImages.getJSONObject(j);
                        searchItem.images.add(img.getString("url"));
                    }
                }
                
                result.items[i] = searchItem;
                totalScore = totalScore + searchItem.score;
            }
            
            if (result.resultCount > 0) {
                result.averageScore = totalScore / result.resultCount;
            }
        }
        
        return result;
    }

    private String formatSearchResults(JSONObject response, TavilySearchResult result) {
        StringBuilder output = new StringBuilder();
        
        output.append("🔍 Search Results:\n");
        output.append("══════════════════════════════\n\n");
        
        if (result.responseTime > 0) {
            output.append("⏱️ Response time: ")
                  .append(String.format("%.2f", result.responseTime))
                  .append("s\n");
        }
        
        if (result.requestId != null && !result.requestId.isEmpty()) {
            output.append("🆔 Request ID: ")
                  .append(result.requestId)
                  .append("\n");
        }
        
        output.append("\n");
        
        if (result.answer != null && !result.answer.isEmpty()) {
            output.append("💡 AI Answer:\n");
            output.append(result.answer).append("\n\n");
        }
        
        if (result.resultCount > 0) {
            output.append("📚 Sources (")
                  .append(result.resultCount)
                  .append(" found):\n\n");
            
            for (int i = 0; i < result.resultCount; i++) {
                TavilySearchResult.TavilySearchItem item = result.items[i];
                
                output.append(i + 1).append(". ")
                      .append(item.title)
                      .append("\n");
                
                if (item.url != null && !item.url.isEmpty()) {
                    output.append("   🔗 URL: ").append(item.url).append("\n");
                }
                
                if (item.score > 0) {
                    output.append("   ⭐ Score: ")
                          .append(String.format("%.3f", item.score))
                          .append("\n");
                }
                
                if (item.favicon != null && !item.favicon.isEmpty()) {
                    output.append("   🌐 Favicon: ").append(item.favicon).append("\n");
                }
                
                if (item.content != null && !item.content.isEmpty()) {
                    output.append("   📝 Content: ").append(item.content).append("\n");
                }
                
                output.append("\n");
            }
        } else {
            output.append("❌ No results found.\n\n");
        }
        
        output.append("══════════════════════════════\n");
        output.append("💰 Credits used: ").append(result.creditsUsed);
        
        if (result.resultCount > 0 && result.averageScore > 0) {
            output.append(" | Avg score: ")
                  .append(String.format("%.3f", result.averageScore));
        }
        
        return output.toString();
    }

    public String getSessionId() {
        return sessionId;
    }

    public static class SearchParams {
        public String searchDepth;
        public int maxResults;
        public boolean includeAnswer;
        public boolean includeRawContent;
        public String topic;
        public String timeRange;
        public String country;
        public String[] includeDomains;
        public String[] excludeDomains;
        public boolean includeImages;
        public boolean includeImageDescriptions;
        public boolean includeFavicon;

        private SearchParams(Builder builder) {
            this.searchDepth = builder.searchDepth;
            this.maxResults = builder.maxResults;
            this.includeAnswer = builder.includeAnswer;
            this.includeRawContent = builder.includeRawContent;
            this.topic = builder.topic;
            this.timeRange = builder.timeRange;
            this.country = builder.country;
            this.includeDomains = builder.includeDomains;
            this.excludeDomains = builder.excludeDomains;
            this.includeImages = builder.includeImages;
            this.includeImageDescriptions = builder.includeImageDescriptions;
            this.includeFavicon = builder.includeFavicon;
        }

        public static class Builder {
            private String searchDepth = "basic";
            private int maxResults = 5;
            private boolean includeAnswer = true;
            private boolean includeRawContent = false;
            private String topic = null;
            private String timeRange = null;
            private String country = null;
            private String[] includeDomains = null;
            private String[] excludeDomains = null;
            private boolean includeImages = false;
            private boolean includeImageDescriptions = false;
            private boolean includeFavicon = false;

            public Builder setSearchDepth(String depth) {
                if ("basic".equals(depth) || "advanced".equals(depth) || 
                    "fast".equals(depth) || "ultra-fast".equals(depth)) {
                    this.searchDepth = depth;
                }
                return this;
            }

            public Builder setMaxResults(int max) {
                if (max >= 0 && max <= 20) {
                    this.maxResults = max;
                }
                return this;
            }

            public Builder setIncludeAnswer(boolean include) {
                this.includeAnswer = include;
                return this;
            }

            public Builder setIncludeRawContent(boolean include) {
                this.includeRawContent = include;
                return this;
            }

            public Builder setTopic(String topic) {
                if ("general".equals(topic) || "news".equals(topic) || "finance".equals(topic)) {
                    this.topic = topic;
                }
                return this;
            }

            public Builder setTimeRange(String range) {
                if ("day".equals(range) || "d".equals(range) ||
                    "week".equals(range) || "w".equals(range) ||
                    "month".equals(range) || "m".equals(range) ||
                    "year".equals(range) || "y".equals(range)) {
                    this.timeRange = range;
                }
                return this;
            }

            public Builder setCountry(String country) {
                this.country = country;
                return this;
            }

            public Builder setIncludeDomains(String[] domains) {
                this.includeDomains = domains;
                return this;
            }

            public Builder setExcludeDomains(String[] domains) {
                this.excludeDomains = domains;
                return this;
            }

            public Builder setIncludeImages(boolean include) {
                this.includeImages = include;
                return this;
            }

            public Builder setIncludeImageDescriptions(boolean include) {
                this.includeImageDescriptions = include;
                return this;
            }

            public Builder setIncludeFavicon(boolean include) {
                this.includeFavicon = include;
                return this;
            }

            public SearchParams build() {
                return new SearchParams(this);
            }
        }
    }
}