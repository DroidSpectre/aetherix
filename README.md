
# Aetherix

Aetherix is a lightweight Android app with a clean, 
native interface for intelligent web search powered 
by the [Tavily AI Search API](https://tavily.com/).

## Features

- Simple and focused search UI with multi-line query input.
- Topic filtering for General, News, and Finance searches.
- Fast results with answer generation and source links.
- Robust handling for network issues, API errors, rate limits,
 and missing configuration.
- Secure API key loading from `assets/.env`.
- ProGuard-ready for release builds.

## Screenshots

Add screenshots here once available.

## Tech Stack

- Language: Java
- UI: Android XML layouts + AppCompat
- Networking: OkHttp 4
- Search engine: Tavily AI Search API
- Min SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)

## Project Structure

```text
aetherix/
├── app/
│   ├── module.toml
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/aetherix/search/
│       │   ├── MainActivity.java
│       │   ├── TavilyClient.java
│       │   ├── TavilySearchResult.java
│       │   └── EnvLoader.java
│       ├── res/
│       │   ├── layout/activity_main.xml
│       │   └── values/
│       └── assets/.env.example
├── .gitignore
└── .platform/
```

## Setup

1. Clone the repository.
   ```bash
   git clone https://github.com/DroidSpectre/aetherix.git
   cd aetherix
   ```

2. Get a Tavily API key.
   - Sign up at [tavily.com](https://tavily.com/).
   - Copy your API key.

3. Configure the API key.
   - Create `app/src/main/assets/.env`.
   - Add the following:
     ```env
     TAVILY_API_KEY=tvly-your-key-here
     ```

4. Build and run.
   - This project uses a custom `module.toml` setup instead 
   of a standard Gradle project.
   - Import the `app/` directory into Android Studio or your 
   preferred Android IDE and build from there. I used 
   CodeAssist - Android IDE.

## Future Improvements

- Image result support in the UI.
- Search history.
- Share results.
- Offline caching.
- Better theming with Material 3.
- Export results as PDF or text.
- Voice input.
- Settings for search parameters such as time range 
  and domain filters.

## Contributing

Contributions are welcome.

Especially useful areas include:
- UI and UX improvements.
- Additional Tavily parameters.
- Build system improvements.
- Bug fixes.

## License

This project is licensed under the [MIT License](LICENSE).

Use of the Tavily API requires your own API key and is 
subject to [Tavily’s Terms of Service]
(https://tavily.com/terms) and [Privacy Policy]
(https://tavily.com/privacy).

## Disclaimer

Aetherix is an independent open-source client and is not 
officially affiliated with or endorsed by Tavily. The 
app is provided “as is” without warranty.

---

Made with ❤️ by [DroidSpectre]
(https://github.com/DroidSpectre)
