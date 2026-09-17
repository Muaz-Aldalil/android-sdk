# Android Fix Prompts — سُبُل الهُدى

> Generated: 2026-09-16
> Source: full audit of `subul-alhuda-android` vs web SPA (`SH/`)
> Status: ACTIVE — one prompt per issue, ordered by dependency

## Dependency map

- `P3`, `P7` need `P2` · `P4` touches P2's repo · `P11` runs LAST
- `P1/P5/P6/P13/P14` independent · `P10` after UI settles · `P15` gates release

## Execution order

`P1 → P2 → (P3 ‖ P7) → P4 → P5/P6/P14 → P9 → P10 → P12/P13 → P11 → P15`

---

## P1 — Replace the dead YouTube player (independent, do first)

```
VideoPlayerScreen.kt (app/src/main/java/com/subulalhuda/ui/screens/) loads
android-youtube-player (com.pierfrancescosoffritti.androidyoutubeplayer:core
13.0.0, in gradle/libs.versions.toml and app/build.gradle.kts). Google has shut
down that player API, so it will not play.

Rewrite VideoPlayerScreen.kt to play the video in a WebView loading
https://www.youtube.com/embed/{videoId} with autoplay:
- javascriptEnabled = true, mediaPlaybackRequiresUserGesture = false (autoplay),
  DOM storage enabled.
- Show a loading spinner until onPageFinished (blank 16:9 box today).
- Keep the existing fallback EXACTLY: on failure, show a clickable box that
  opens https://www.youtube.com/watch?v={videoId} via an Intent (the
  ActivityNotFoundException catch must remain).
- Title bar must show a real lecture title when available (per P2 contract),
  never the raw videoId.

Then remove the library: delete the dependency from libs.versions.toml and
app/build.gradle.kts, and remove the proguard rules that reference it if any.
No new dependencies. No Media3, no third-party adapters.

Done: builds clean, no androidyoutubeplayer references remain, a known videoId
plays in-app, external fallback still works, back button returns to caller.
```

## P2 — Wire the dead YouTube data layer (do first; feeds all video UI)

```
In C:\Users\muaza\Desktop\Projects\subul-alhuda-android:
- YouTubeRepository (data/repository/YouTubeRepository.kt) is constructed in
  MainActivity.kt only when BuildConfig.YOUTUBE_API_KEY is non-blank (it is
  currently blank in local.properties) and is passed to HomeScreen/LecturesScreen,
  whose bodies NEVER call any of its methods. getUploadsPlaylistId,
  getRecentUploads, getVideoDetails, checkLiveStatus have zero consumers.
- All client calls (YouTubeApiClient.kt) are BLOCKING and must never run on the
  main thread.

Implement the data plumbing (UI screens are separate tasks P3/P7):
1. Add suspend wrappers on YouTubeRepository that dispatch the blocking calls
   on Dispatchers.IO (e.g. `suspend fun recentUploads(...) = withContext(IO) {...}`).
2. Make the repository a lazy process-scoped singleton (or keep it in
   MainActivity's remember scope) — do NOT call close() from onDestroy (it
   fires on rotation); nothing needs to call close() for this app's lifetime.
3. Wire the ONLY proof-of-use consumer now: SheikhProfileScreen.kt must call
   getVideoDetails(sheikh.videoIds) through the suspend wrapper and render the
   real video titles + publish dates instead of the raw videoId
   (SheikhProfileScreen.kt:140). Keep a graceful UI fallback when the API call
   fails or the key is blank.
4. Document (in the repo KDoc) that the API key stays in local.properties,
   must be separate from the web key, and must be restricted in Google Cloud to
   package com.subulalhuda + your signing SHA-1 before release.

Done: sheikh profile shows real titles; blocking calls never touch the main
thread; no dead suspend code; key + restriction steps documented.
```

## P3 — Rebuild the Lectures tab (needs P2)

```
LecturesScreen.kt is a stub: one static card "يمكنك تصفح دروس العلماء من صفحة
كلشيخ" (fix the missing space -> "كل شيخ") plus a FilterChip row whose
selectedCategory state changes nothing (verified: LecturesScreen.kt:35 is never
read past assignment).

Rebuild it as a real lectures list:
1. Load recent uploads through P2's suspend repository API; show loading
   (skeleton rows) and error+retry states.
2. Category chips must actually filter. Use the web's keyword logic
   (SH/src/pages/LecturesPage.jsx) AND fix the branch the web is missing:
   the maqamat category currently matches nothing.
3. Each row: thumbnail, title, date; tap -> video/{videoId} (route exists).
4. No new dependencies. Arabic first/last.

Done: chips change results, error/retry works, selectedCategory drives output,
no dead state.
```

## P4 — Make caching honest (or delete it)

```
Verify first, then pick exactly one:
- YouTubeApiClient.kt sends plain GETs with no CacheControl (verified). The
  YouTube Data API responses are generally NOT http-cacheable, so the OkHttp
  disk cache in CacheManager.kt (10MB, createCachedClient) almost never stores
  them, and the documented 24h/7d/5m TTLs and the "7-day TTL in HTTP cache"
  comment on YouTubeRepository.kt:43 are false. The only real cache is the
  in-memory @Volatile uploadsPlaylistId, which has NO TTL.

Option A (recommended for this traffic): DELETE the caching layer — CacheManager
helpers (videoMetadataCacheControl, playlistIdCacheControl,
liveStatusCacheControl, clearCache) plus the OkHttp .cache() call. Keep only
the in-memory playlistId memo (rename/comment it correctly: process-lifetime,
no TTL). With the 10k-units/day default quota and a handful of users, the
caching adds nothing for this scale.

Option B (only if you expect heavy daily use): implement an explicit TTL cache
mirroring the web's cacheService.js (prefix key + expiry JSON in
SharedPreferences/disk), keyed by endpoint+params, TTLs 24h/7d/5m as documented,
read-check TTL on every call.

Do not ship Option "A+B" or the current fiction.

Done: no code claims a TTL/disk cache that does not exist; the chosen option is
verifiable (option B: second call within TTL does not hit the network).
```

## P5 — Make the matching game playable

```
KidsGameScreen.kt MatchingGame (verified: KidsGameScreen.kt:145-178):
- renders each pair read-only with a "←" arrow — nothing is interactive.
- "المطابقات: $matchedCount / N" always shows 0 because matchedCount is
  remember'd but never modified.

Rebuild into the web's matching game (SH/src/pages/KidsGamePage.jsx) as
Compose state:
1. Left column fixed order; right column exported values shuffled initially
   (unbiased Fisher-Yates — reuse a single shared shuffle util, don't add copy).
2. Tap a left item, then a right item -> pair them; re-tapping either side
   replaces that side's match.
3. "تحقق من الإجابات" appears only when all pairs are matched; on tap, show
   each pair green/red per correctness (success/error tokens), show score,
   and "العب مرة أخرى" reshuffles and resets.
4. Keep state in rememberSaveable where cheap (process-death resilience).
5. Mathematical correctness: counting matched vs correct, enabling/disabling
   already-used right items exactly mirror the web's non-broken parts — do not
   copy its bugs.

Done: a user can play to a real result and replay; no dead matchedCount.
```

## P6 — Quiz: setup, shuffle, and a working result flow

```
QuizScreen.kt runs all 30 questions in JSON order — no difficulty, no shuffle,
no setup. QuizResultScreen.kt (verified) shows score/%/message and a single
"العودة للاختبارات"; from the quiz itself, "انتهت اللعبة"-style final state is
absent. The web flow (SH/src/pages/QuizDetailPage.jsx) has this shape — but do
NOT copy its bugs (side-effect-in-render setTimeout, a dead `type` ternary, the
"show all questions at once" layout; that layout is worse on mobile).

Add (Compose-native, past scoring logic untouched):
1. A setup stage: difficulty (easy/medium/hard, filter by question.difficulty)
   and a question count (cap at available). Keep it a simple modal/scaffold, no
   slider if a stepper is simpler.
2. Fisher–Yates shuffle of the chosen subset.
3. Result screen additions: correct/wrong counts, "إعادة الاختبار" (same
   settings, reshuffled) and "اختبار جديد" (back to setup), keeping the
   existing score/percent. The score ring from the web is OPTIONAL — skip it if
   it adds more code than value.
4. Guard the progress bar against a zero-question divide-by-zero.
5. Progress in rememberSaveable (process death currently loses mid-quiz state).

Done: setup -> shuffle -> quiz -> result -> replay/new chain works in both
directions; no dead final-state button.
```

## P7 — Home screen: real content, accurate to the web (needs P2)

```
HomeScreen.kt (verified) gaps vs web HomePage.jsx:
- No video section; youtubeRepository param unused.
- No live indicator; hero has no verse dots/manual selection and only one CTA
  wired (hero click -> lectures).
- Announcement cards only handle sheikhId; announcement.link / linkLabel / date
  are ignored (web links every announcement per its `link` field).
- The 8s rotation while(true) loop never pauses (LaunchedEffect) — fine but add
  a lifecycle-aware pause if easy.
- Badge colors hardcode hex (HomeScreen.kt:210-214, incl. #2563EB) — these must
  use theme tokens (see P10) EXCEPT the semantic live-red, which the lock
  permits.

Implement: a small "آخر الدروس" section fed by P2 (loading/error states);
route announcements by their `link` field (internal routes -> navigation,
https:// -> ACTION_VIEW) and render date/linkLabel when present; verse dots to
switch verses manually; a second CTA/tile ("اختبر نفسك" -> interactive,
"تواصل معنا" -> contact) replacing the single-link QuickLinksRow; live banner
from P2. Skip the live banner if the API key is not configured yet (guard, don't
show a spinner).

Done: every Home section renders real data or a valid empty state; no square
dead card remains.
```

## P8 — (merged into P2) — no separate prompt needed

Print the sheikh card count as the real `videoIds.size` with proper Arabic
pluralization (فيديو/فيديوهات, dual form where applicable). Do NOT mirror the
web's unbacked `lectureCount` figures.

## P9 — Search results must navigate

```
SearchScreen.kt (verified: lines 100-116) renders quiz and game ListItems with
no click handler — tapping does nothing; only sheikh rows navigate (line 93).
The onVideoClick param has no use in this screen.

Wire quiz rows -> quiz/{quizId} and game rows -> game/{gameId} (routes exist in
NavGraph.kt). Remove the dead onVideoClick param from SearchScreen and NavGraph.
Arabic section headers and empty states unchanged.

Done: every result row in all three sections navigates to a real screen.
```

## P10 — Enforce the locked design system

```
The Android theme tokens in ui/theme (Color.kt, Theme.kt, Type.kt) already
match SH/.design-lock.md exactly. Fix how they're CONSUMED (do not change
token values):
1. Every Card across screens uses the default M3 surface elevation (shadow).
   Set elevation = 0.dp (or use Surface with a 1dp border in the lock's border
   color #E8E5DE / dark #2a2a2a). Verified surfaces: HomeScreen cards (hero,
   announcement, sheikh, quick-link), SheikhsScreen, SheikhProfileScreen,
   InteractiveScreen, KidsGameScreen.
2. Hero card radius is 12.dp (HomeScreen.kt:124); the lock says 8px cards /
   4px inputs. Align shapes.
3. Hardcoded badge hexes in HomeScreen.kt:210-214 (#DC2626, #16A34A, #D4A017,
   #2563EB, Color.Gray) must come from theme tokens (semantic green/red only
   for live/new; event/upcoming use the gold accent; drop the blue). Sweep ALL
   screens for other hardcoded Color(...) literals.
4. RTL: app content is Arabic-only; the manifest only sets supportsRtl and the
   UI mirrors the DEVICE locale (English-locale phones render LTR Arabic).
   DECISION LOCKED: force RTL app-wide (content is Arabic-only).
5. themes.xml has no values-night variant and hardcoded light status bar:
   dark-mode cold start flashes light. Add a night theme variant.
6. Keep: Noto Kufi/Amiri pairing, heading weights <=500, no emojis/gradients.

Done: one visual regression pass shows no shadows, no stray hexes, RTL on
English-locale devices, no dark-mode flash. Token values untouched.
```

## P11 — Cleanup dead code (LAST — after P2/P3/P7/P9 remove what they use)

```
Remaining verified dead code after the earlier tasks run:
1. Screen.SheikhVideos route (ui/navigation/Screen.kt:41-43) — declared, never
   registered in NavGraph, never navigated. Delete.
2. SubulApp empty Application subclass — delete it, its manifest reference, and
   then the manifest's android:name entry.
3. Unused color tokens in ui/theme/Color.kt (Header*/Hero* series) — delete.
4. Any youtubeRepository/onVideoClick params still unused after P2/P3/P7/P9.
5. Duplicated Json config: ContentReader.kt defines a Json instance that
   ContentRepository.kt re-creates privately (ContentRepository.kt:18-22) —
   unify to one instance.
6. Hardcoded "1.0.0" in SettingsScreen.kt:54 (verified) and AboutScreen — use
   BuildConfig.VERSION_NAME.
7. Do NOT add YouTubeRepository.close() calls (P2 already resolved this).
8. Scope-check first: grep each candidate before deleting to confirm zero
   remaining references after P2/P3/P7/P9 land.

Done: `./gradlew assembleDebug` and `./gradlew lint` pass with no NEW warnings,
and a second grep confirms each deletion had zero live references.
```

## P12 — Harden CI

```
.github/workflows/build.yml currently: installs its own Gradle (ignores the
committed wrapper), runs only assembleDebug, zero tests (no test source sets
exist), no lint, no PR trigger, no dependency caching.

Upgrade it:
1. Use the committed wrapper: `./gradlew assembleDebug` etc. (do NOT run
   `gradlew wrapper` inside CI — it mutates the repo).
2. Add a PR trigger and keep push:main.
3. Add actions/setup-java, sdkmanager only what AGP 9.3 needs (already correct),
   and actions/cache for ~/.gradle/caches and project .gradle.
4. Add testDebugUnitTest AND assembleRelease (unsigned) to the same job or a
   second job.
5. Add lintDebug, but FIRST run it locally against the current tree and
   establish a baseline — a fresh lint gate against an un-linted codebase will
   fail on pre-existing warnings, not on your change. Decide: async warnings
   vs fail-on-new.
6. Add ONE minimal unit test first (pure logic, e.g. correct-answer resolution
   or match evaluation) so testDebugUnitTest has something deterministic to run.
   No test framework beyond what libs.versions.toml already declares.
7. Keep shell $?/tee build.log failure reporting that exists today.

Done: PRs run lint+unit+debug+release through the wrapper with cached deps; no
secrets in logs (key stays in local.properties, signing stays local).
```

## P13 — Content cleanup + re-sync, single source of truth

```
Verified parity: 6 of 7 Android JSON assets (assets/content/*.json) are
byte-identical to the web constants (SH/src/constants/), except social_links
adds an iconKey. Two corruption/typo fix-ups are needed IN BOTH copies, plus a
hard boundary:

1. fiqh-basics Q10 explanation: the WEB has a Korean "순" (should be "صرف").
   The ANDROID quizzes.json is already correct — propagate the fix back into
   SH/src/constants/QUIZZES.js.
2. kids games Q8: "ج̣زء عم" carries a stray combining diacritic (U+0323) in
   BOTH files — remove it in both.
3. Typo-level fixes in both (only these, nothing religious): "فيه7 آيات" ->
   "فيها 7 آيات"; "المأمون" -> "المأموم".
4. After fixing, run a diff/verification script comparing every JSON against
   its JS source and require zero non-cosmetic diffs. Commit that script so
   future copy jobs can re-run it.
5. HARD RULE: do not touch factual/religious content — e.g. the Asr-start-time
   answer, the "first ghazwa" answer, or the contradictory congregation-prayer
   rulings (islam-pillars Q15 vs fiqh-basics Q18/Q20). List these for the
   project owner to verify with a mufti; an agent must NOT edit rulings.

Done: both corrupted chars and both typos gone in both copies; diff script is
committed and clean; a written list of rulings pending owner verification.
```

## P14 — Finish the صواب/خطأ kids game

```
KidsGameScreen.kt TfGame (verified: KidsGameScreen.kt:129-140): on the last
question the button reads "انتهت اللعبة — النتيجة: X/N" but onClick does
nothing. There is no result screen and no replay.

Add: when the quiz is exhausted, show a result view (score, "العب مرة أخرى"
which reshuffles and resets — reuse the shuffle + a per-question toggle so the
same questions replay in new order if the data allows). Also fix the answer
coloring: current code colors the correct button green AND the wrong button red
for EVERY answered question (lines 95-98), regardless of what the user picked.
Match the web (KidsGamePage.jsx): correct option always green, the
selected-wrong option red, everything else neutral. Use error/tertiary tokens.
Keep progress in rememberSaveable.

Done: game leads to a working result + replay; coloring only highlights the
selected answer when it's wrong.
```

## P15 — Release readiness (gates shipping)

```
Blocked on real values that don't exist yet (all verified blank/absent):
1. local.properties has blank RELEASE_STORE_FILE/PASSWORD/KEY_ALIAS/KEY_PASSWORD
   and a blank YOUTUBE_API_KEY — a release build is currently impossible and
   every YouTube screen is offline.
2. The YouTube key must be a NEW key (separate from the web's VITE_YOUTUBE_API_KEY),
   restricted in Google Cloud Console to the Android package com.subulalhuda +
   the signing keystore SHA-1 (an unrestricted key embedded in an APK is
   extractable and quota-burnable).
3. VideoPlayerScreen is currently broken in production (see P1) — by the time
   these are filled, P1 must be in.
4. AndroidManifest.xml: add dataExtractionRules/fullBackupContent or an android:
   allowBackup = "false" decision now that single-Activity content app has
   nothing to back up but cache.
5. Smoke-test the MINIFIED release build (isMinifyEnabled + shrinkResources) on
   a device/emulator before release — nothing in CI currently builds or runs it.

Deliverable: a short release checklist doc (checked-off) covering keystore, key
restriction, P1 verification, backup rules, and a manual minified-release smoke
test on API 26 and 36.

Done: a signing-enabled assembleRelease installs and runs on device; YouTube
works in release builds; backup policy set.
```