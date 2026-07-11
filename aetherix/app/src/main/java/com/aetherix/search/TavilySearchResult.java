package com.aetherix.search;

import java.util.List;

public class TavilySearchResult {
    public String answer;
    public String query;
    public int resultCount;
    public double averageScore;
    public int creditsUsed;
    public float responseTime;
    public String requestId;
    public List<String> images;
    public TavilySearchItem[] items;

    public static class TavilySearchItem {
        public String title;
        public String url;
        public String content;
        public double score;
        public String favicon;
        public List<String> images;

        public TavilySearchItem() {
        }
    }

    public TavilySearchResult() {
        this.answer = null;
        this.query = null;
        this.resultCount = 0;
        this.averageScore = 0.0;
        this.creditsUsed = 0;
        this.responseTime = 0.0f;
        this.requestId = null;
        this.images = null;
        this.items = null;
    }
}