**Aetherix** is a lightweight Android application that provides a clean, native interface for performing intelligent web searches powered by the [Tavily AI Search API](https://tavily.com/).

### Features

- **Simple & Focused Search UI**: Multi-line query input, topic filtering (General, News, Finance), and a clean results display.
- **Tavily Integration**: Full-featured client supporting search depth, max results, answer generation, topic filtering, and more.
- **Robust Error Handling**: Network issues, API errors, rate limits, and missing configuration are handled gracefully with user-friendly messages.
- **Environment-based Configuration**: API key loaded securely from `assets/.env` (not committed to git).
- **Modern Android Practices**: Uses OkHttp for networking, proper threading, JSON parsing, and a readable dark/light-friendly color scheme.
- **ProGuard Ready**: Includes basic rules for release builds.

### Screenshots / Demo

*(Add screenshots of the app UI here once you have them — MainActivity layout shows title, query box, topic spinner, search button, and scrollable results.)*

### Project Structure

```
aetherix/
├── app/
│   ├── module.toml              # Build configuration (custom Android module format)
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
│       │   └── values/ (colors, strings, etc.)
│       └── assets/.env.example   # (recommended)
├── .gitignore
└── .platform/                   # Build/cache artifacts
```

### Setup & Running

1. **Clone the repo**
   ```bash
   git clone https://github.com/DroidSpectre/aetherix.git
   cd aetherix
   ```

2. **Get a Tavily API Key**
   - Sign up at [tavily.com](https://tavily.com/)
   - Copy your API key

3. **Configure the API Key**
   - Create `app/src/main/assets/.env` (it will be ignored by git)
   - Add:
     ```
     TAVILY_API_KEY=tvly-your-key-here
     ```

4. **Build & Run**
   - This app was buillt using "CodeAssist - Android IDE" from the Google PlayStore. The latest update uses a Maven build system (as I understand it), and a custom module.toml instead of build.gradle. 

### Tech Stack

- **Language**: Java
- **UI**: Android XML layouts + AppCompat
- **Networking**: OkHttp 4
- **Search Engine**: Tavily AI Search API
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 24 (Android 7.0)

### Future Improvements

- Image result support in UI
- Settings page for in-app adjustment of search parqameters
- Search history
- Share results
- Offline caching
- Better theming / Material 3
- Export results (PDF/text)
- Voice input

### Contributing

Feel free to open issues or PRs! Especially welcome:
- UI/UX enhancements
- Additional Tavily parameters (time range, domain filters, etc.)
- Build system improvements

---

**Made with ❤️ by DroidSpectre**

---

## License

This project is licensed under the [MIT License](LICENSE) — see the LICENSE file for details.

**Note**: Aetherix is an independent open-source client. Use of the Tavily API is subject to [Tavily's Terms of Service](https://tavily.com/terms) and requires your own API key.