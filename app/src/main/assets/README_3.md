Fix 1:

File - 	What changed	

`activity_settings.xml`	- Added `layout_width`/`layout_height` to all 8 `RadioButton` elements; replaced `<Switch>` with `<androidx.appcompat.widget.SwitchCompat>`	

`SettingsActivity.java`	- Changed `import android.widget.Switch` → `import androidx.appcompat.widget.SwitchCompat`; changed all `Switch` field types to `SwitchCompat`	

.....

Fix 2:

File	- Change	

TavilyClient.java	- Added `autoParameters` boolean to `SearchParams`. When `true`, sends `"auto_parameters": true` to Tavily API and skips sending `search_depth`, `topic`, `time_range`, `country`, `include/exclude_domains`. When `false`, sends everything manually.	

MainActivity.java	- Reads `auto_mode` from prefs. If `true` → `setAutoParameters(true)` + only sends `max_results`, `include_answer`, `include_raw_content` (required per Tavily docs), plus images/favicon (response-size related). If `false` → sends all manual params.	

SettingsActivity.java	- Always saves ALL manual params (previously only saved when auto was OFF, leaving stale values). The `auto_mode` flag alone controls behavior at request time.	

...

Scenario	- What Gets Sent to Tavily	

Auto Mode ON - `auto_parameters: true`, `max_results`, `include_answer`, `include_raw_content`, `include_images` (if on), `include_favicon` (if on), plus any topic override from MainActivity spinner	


Auto Mode OFF - `auto_parameters: false`, `search_depth`, `max_results`, `include_answer`, `include_raw_content`, `topic`, `time_range`, `country`, `include/exclude_domains`, `include_images`, `include_favicon` — everything manually configured	

...

The key fix: SettingsActivity now always saves all params. The  auto_mode  boolean alone controls whether Tavily receives  auto_parameters: true  or explicit manual values. No more stale settings leaking through.

.....

When Auto Mode is ON:
- `auto_parameters: true` is sent to Tavily

- Tavily auto-configures: `search_depth`, `topic`, `time_range`, `country`, `include_domains`, `exclude_domains`

- Aetherix app still controls: `max_results`, `include_answer`, `include_raw_content`, `include_images`, `include_image_descriptions`, `include_favicon`

- The MainActivity topic spinner can override Tavily's auto topic selection

When Auto Mode is OFF:
- `auto_parameters: false` is sent to Tavily

- Every parameter comes from your Settings screen exactly as configured

- No automation — full manual control

---

Parameter Flow Table

Parameter -	Auto Mode ON	- Auto Mode OFF	
`auto_parameters`	- `true` ← new	- `false` ← new
	
`query`	- user input	- user input	

`max_results`-	from Settings	- from Settings	

`include_answer`	- from Settings	- from Settings	

`include_raw_content`	- from Settings	- from Settings	

`include_images`	- from Settings	- from Settings
	
`include_image_descriptions`	- from Settings	- from Settings	

`include_favicon`	- from Settings	- from Settings	

`topic`	- spinner override only	- spinner or Settings default	

`search_depth`	- Tavily decides	- from Settings
	
`time_range`	- Tavily decides	- from Settings	

`country`	- Tavily decides	- from Settings	

`include_domains`	- not sent	- from Settings	

`exclude_domains`	- not sent	- from Settings	

---

What Changed vs. Original

File	- Original - Bug	Fix Applied	

SettingsActivity	- Manual params only saved when Auto=OFF → stale values persisted	- Now always saves all params; `auto_mode` flag alone controls behavior	

MainActivity	- Never checked `auto_mode`; always sent explicit `search_depth`	- Now branches on `auto_mode`: auto=true sends `auto_parameters=true`, auto=false sends all manual params	

TavilyClient	- No `auto_parameters` field existed	- Added `autoParameters` to `SearchParams` + conditional JSON building	

---

One Important Note

Tavily's `auto_parameters` may automatically select `search_depth="advanced"` when it thinks it'll improve results. Advanced depth costs 2 API credits instead of 1. 