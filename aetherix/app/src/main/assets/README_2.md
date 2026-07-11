Latest version adds a Settings page within the app to conrol search parameters.

Search Parameters available:
    
    Tavily Search Depth Settings 
                & 
        Configuration Guide 
           
            Written by: 
           DroidSpectre
           Lumo (Proton)
           July 9, 2026

Based on the Tavily documentation, here's a comprehensive breakdown of the search settings and their optimal use cases:

Search Depth Settings

Tavily offers four search depth options that control the balance between latency and relevance:

Depth - Latency - Relevance - Content Type - Best Use Case

ultra-fast -	Lowest - Lower	 - Content - Real-time applications where speed is absolutely critical (chat interfaces, instant autocomplete)

fast - Low - Good - Chunks - Quick, targeted snippets when latency matters more than perfect relevance (live dashboards, notifications)

basic - Medium - High - Content - 	General-purpose searches; balanced quality without advanced processing overhead (most common choice)

advanced - Higher - Highest - Chunks -	Complex queries seeking specific, detailed information (research, fact-checking, competitive analysis)

Key Distinction:

Content type = NLP-based page summary for general context

Chunks = Short, reranked snippets aligned precisely with your query

Other Configuration Settings

Here are the additional parameters available (NOTE: Not all parameters are implemented within this app at this time.)

Parameter -	Description -	Best Use

max_results	- Cap returned results (up to 20); default is 5	- Limit costs for simple queries; increase to 10–20 for comprehensive research

include_answer	- Get LLM-generated short answer from results	- When you need direct answers without processing raw search results

include_raw_content - 	Return full page text in markdown format - 	Deep content analysis, extracting specific information from sources

include_domains / exclude_domains - 	Filter by specific websites	 - Focus on authoritative sources (e.g., include sec.gov, reuters.com) or avoid low-quality sites

time_range - Filter by recency: day, week, month, year - News, trending topics, or time-sensitive research

start_date / end_date - Specific date range (YYYY-MM-DD) - Historical research or precise time windows

topic - Filter by category: general, news, finance - News-focused queries benefit from news topic (includes published_date metadata)

auto_parameters - Auto-configures settings based on query intent - Let Tavily optimize for you; may default to advanced for complex queries

country - Prioritize content from selected country - 	Region-specific queries

include_images -Add query-related image URLs - Visual-rich queries (products, places, events)

include_image_descriptions - Get descriptive text for each image - Accessibility or image-based analysis

include_favicon - Include site favicon URLs - Brand recognition or domain verification

exact_match	- Enforce exact keyword matching - Precise queries where synonym expansion is unwanted

Example Practical Recommendations

For Everyday Applications:

{
  "query": "Your search query",
  "search_depth": "basic",
  "max_results": 5
}

For Research & Analysis:

{
  "query": "Specific detailed question",
  "search_depth": "advanced",
  "max_results": 10,
  "include_raw_content": true,
  "chunks_per_source": 3
}

For Breaking News:

{
  "query": "Latest event coverage",
  "search_depth": "fast",
  "topic": "news",
  "time_range": "week"
}

For Speed-Critical Apps:

{
  "query": "Quick lookup",
  "search_depth": "ultra-fast",
  "max_results": 3
}

Important Notes

Query length: Keep under 400 characters; break complex queries into sub-queries

Credit usage: advanced searches consume more credits due to deeper crawling

Auto-optimization: With auto_parameters=true, Tavily may automatically select advanced for complex queries to improve results

Cost-speed-relevance tradeoff: You cannot maximize all three simultaneously; choose based on your priority
