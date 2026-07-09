---
status: in-progress
phase: 3
updated: 2026-07-09
---

# Nobook Safe-Aggressive Fork — Implementation Plan v2

## Goal
Eliminate user-reported scroll jank / cold-launch gray-screen / tab-switch buffer in Nobook by migrating ~20 of 23 behaviors to native APIs + network-level filtering + one-shot CSS, while staying on stock Chromium WebView and **never bypassing the WebView's authenticated session for Facebook GraphQL**.

The canonical plan was originally saved via `plan_save` and tracked via `scoped-tasks` MCP; both were removed 2026-07-09 (scoped-tasks) and lost across sessions (plan_save was session-scoped). This file is the single durable canonical home.

## Context & Decisions
| Decision | Rationale | Source |
|--------|-----------|--------|
| Lift Brave's `adblock-rust` via Android JNI wrapper for native adblock | ~5.7 µs/request, off-main-thread `shouldInterceptRequest`; deletes `adblock.js` observer storm; mature engine | b3 research (Brave, matan-h/AdblockAndroid, Bilal393/Adblock-Webview, Nora) |
| Adopt `androidx.webkit:1.13.0` and gate every feature on `WebViewFeature.isFeatureSupported(...)` | Use latest WebView features across OS versions; negligible cost; documented knob set (back-forward cache, off-screen pre-raster, algorithmic darkening, requested-with-header allowlist, safe-browsing disable) directly addresses user symptoms | b4 androidx.webkit research |
| Pre-warm WebView in `Application.onCreate` via `androidx.startup` Initializer | Eliminates cold-launch gray-screen; low breakage risk; opt-out toggle for users worried about battery | b5 Tier B7 |
| Drop `SCRIPT_SRC` GitHub-raw fetch from `fetchScripts.kt` | Per-page main-thread network round-trip was a jank contributor; packaged `res/raw/*.js` is source-of-truth for the fork | b2 perf diagnosis |
| Use `IntersectionObserver` `rootMargin: "300% 0px 300% 0px"` override at document_start | Direct fix for the "scroll-stop at ~9 entries" symptom; opt-in ("Aggressive feed prefetch") because ~1.5× bandwidth on continuous scroll | b5 accepted plan + scroll-fix investigation |
| Inspect FB's bundle HAR-with-content + decode `com.facebook.katana.apk` `assets/graph_metadata.bin` (`flatc`) | Canonical selector set + `fb_api_req_friendly_name` enumeration replace 60+ inlined `querySelectorAll` strings and ad-hoc op-name guessing; centralizes FB-specific knowledge into registry JSON files | b6 (CajuM 2019, AlterLab 2026, dev.to/vhub 2026, Taprun 2026, Clura 2026) |
| Patch `RelayPrefetchedStreamCache` at document_start before React hydration | Lets `hide_suggested/hide_reels/hide_stories/hide_pymk/hide_groups/sponsored` run BEFORE React renders → eliminates 5 cosmetic observers + 1 MutationObserver at initial render | b7 AlterLab writeup |
| Reasonable NOT to `data-ft` → native regex for sponsored detection | Replace 60-line lang-suffix sponsored-blocker with `JSON.parse(node.dataset.ft).<sponsored-flag>` single dispatch; field name discovered via HAR | b7 |
| Reasonable NOT to bypass WebView for FB GraphQL | Meta's behavioral ML (Clura TLS/JA3 + mouse/scroll + time-on-page) gives Python ~78% / Playwright ~31% / Real Chrome ~8% block rates; bypassing WebView burns the user's authenticated session | b7 Clura |
| Reasonable NOT to swap engines (GeckoView, Crosswalk, CEF, Bromite, custom Chromium, TWA) | GeckoView ~half load speed on heavy sites; Crosswalk EOL 2017; CEF desktop; Bromite needs root + signature-spoof ROM; TWA requires Digital Asset Links (user doesn't own m.facebook.com) | b4 |
| Reasonable NOT to rewrite to `mbasic.facebook.com` | ~10× faster HTML-only path but loses half the features (no reactions-popover, stories, inline media viewer) — user-visible regression too high | b5 Tier B4 |
| Reasonable NOT to do hide-WebView-as-backend | Most native feel + 60 fps scrolling but brittleness against FB markup drift per release; Phase 3.7 RelayPrefetchedStreamCache patcher is the safe partial realization | b1 + b7 |
| Reasonable NOT to do WASM-Rust DOM matching | DOM is reachable only via JS bindings with marshalling cost per call; WASM wins on CPU-bound work (crypto / codecs / parsers, e.g. AdBlock regex engine) and loses on DOM I/O | b3 + b5 |
| Reasonable NOT to switch to TS for perf | TS compiles to JS and the jank is a design problem (8+ unbounded body-observers on a churn-heavy SPA) not code-quality; TS is for maintainability only | b3 |
| KEEP base64 `Clipboard` and `Download` bridges (`FileReader.readAsDataURL(blob)`) | Native URL-only flows would lose FB-session cookies the WebView has; v2 plan correction vs the earlier v1 draft that proposed rewriting as native `ActionMode`/`ClipboardManager` URL-only flow | b7 (Taprun + Clura session-rule) |
| Reasonable NOT to run `./gradlew qualityCheck` | Too expensive; Nobook has no Spotless/Detekt; useful gates are `sh gradlew :app:compileDebugKotlin` and `sh gradlew :app:lintDebug` ordered only | b17 discovery |

## Phase 1: Foundation [COMPLETE] — task 1c301c66 (scoped-tasks archive)
Five substantive items done + 1 design-choice deferral + 1 verification-only.

- [x] **1.1 Add deps** — `androidx.webkit:webkit:1.13.0`, `androidx.startup:startup-runtime:1.2.0`, `androidx.metrics:metrics-performance:1.0.0` (groupId renamed at stable; pre-2024 `androidx.metrics.performance:performance-jankstats:1.0.0-beta02` no longer exists on Maven). Commit `cb61615`.
- [x] **1.2 Wire JankStats in NobookWV.kt → `baselines/baseline_v1.txt`** — `JankStatsTracker.attach(activity)` returns `() -> Unit` no-op detach (createAndTrack auto-starts in stable 1.0.0 — `trackEnabled` API removed). DisposableEffect(activity) keys lifecycle. Commit `e8da633`.
- [x] **1.3 Wire androidx.webkit feature gates** — `setBackForwardCacheEnabled(true)`, `setOffscreenPreRaster(true)`, `setAlgorithmicDarkeningAllowed(true)`, `setRequestedWithHeaderOriginAllowList({facebook.com, messenger.com})`, `setSafeBrowsingEnabled(false)`, `setRendererPriorityPolicy(RENDERER_PRIORITY_IMPORTANT, false)`. Render-process-gating (`WebViewRenderProcessClient` wiring) deferred — API surface drift in androidx.webkit 1.13.0 made first compile fail. Commit `44fe475`.
- [x] **1.4 Drop `SCRIPT_SRC` GitHub-raw fetch** — `fetchScripts.kt` rewritten; `suspend` modifier dropped (no IO); `Script` data class slimmed to `(isEnabled, resourceId)` (removed scriptTitle). Commit `33af0a9`.
- **1.5 Native adblock via `adblock-rust` ↔ DEFERRED** — design-choice session: (a) lift `matan-h/AdblockAndroid` JNI + NDK (multi-day), (b) pure-Kotlin regex matcher reading `assets/filters/easylist_basic.txt` (~200 LOC), (c) Brave `adblock-rust` crate via `cargo-ndk` + AAR (multi-day). Existing `ExternalRequestInterceptor.kt:18` regex is HOST ALLOWLIST (FB main frames to WebView; off-loads off-domain to ACTION_VIEW) NOT an adblock filter. Sponsored cards in organic GraphQL can't be network-blocked — cosmetic-blocking fate is Phase 3.
- [x] **1.6 Add `WebViewInitializer.kt` via androidx.startup** — pre-warms WebView engine + CookieManager on app-launch background thread via `Handler(mainLooper).post { ... }`. Registered via `InitializationProvider` meta-data in `AndroidManifest.xml`. Commit `206d31e`.
- [x] **1.7 Add `Fast cold-launch` opt-in toggle** — `SettingsDataStore.FAST_COLD_LAUNCH` key (default `true`); `SettingsViewModel.fastColdLaunch` stateIn flow + setter; `WebViewInitializer` short-circuits on `toggle != true`. Commit `206d31e`.
- [x] **1.8 Verify interceptor regex still passes FB GraphQL endpoints** — `https?://(?!(?:l|lm\.)[^/]*(?:facebook|messenger)\.com/.*` is host-allowlist matching `m.`/`www.`/`mbasic.` FB GraphQL (`/api/graphql/`, `/graphqlbatch/` → ALLOWED); `l.` (link shortener) + `lm.` (messaging redirect) excluded as off-FB navigation. No code change.

## Phase 2: TS Port Tooling [COMPLETE] — task aedb860f (scoped-tasks archive)
Five substantive items done + 2 deferrals.

- [x] **2.1 TS scaffolding** — `app/src/main/ts/{package.json,tsconfig.json,esbuild.config.js,pnpm-lock.yaml,pnpm-workspace.yaml,bridges.d.ts,.gitignore}`. Toolchain: `typescript ^5.7.0` + `esbuild ^0.25.0` + `pnpm 11.5.0` + node 22. pnpm 11 dropped `package.json` `onlyBuiltDependencies` field — esbuild postinstall authorized via `pnpm-workspace.yaml` `allowBuilds: {esbuild: true}`. Commit `25725d7`.
- [x] **2.2 Gradle `bundleScripts` chain** — three Exec tasks `tsInstall → tsTypeCheck → tsBundle → preBuild`. Cross-platform (no `sh -c` wrapper); no-op until first `.ts` lands (esbuild prints "No .ts source files to transpile." and exits 0). Commit `92184fc`.
- [x] **2.3 `tsc --noEmit` CI gate** — `.github/workflows/ci.yml` single job `ts-typecheck`; triggers `push` on `[main, 'feat/**']` + `pull_request`. Commit `9689bb0`.
- **2.4 `gen-types` via `json-schema-to-typescript` ↔ DEFERRED** — blocked on Phase 3.1 (graphql_ops.json) + Phase 3.3 (selectors.json, feed_schema.json); gen-types is no-op without JSON inputs.
- [x] **2.5 `bridges.d.ts`** — ambient declarations for 4 `@JavascriptInterface` methods + global Window augmentations. Matched against Kotlin source: `NobookSettings.onSettingsToggle()` (zero-arg), `ThemeChange.onThemeColorChanged(string?`), `DownloadBridge.downloadBase64File(base64Data, mimeType)`, `ClipboardBridge.copyImageToClipboard(base64Data, mimeType)`. Last one corrected from v1's 1-arg shape per Kotlin source. Commit `25725d7`.
- **2.6 12 `.js`→`.ts` byte-equivalent port ↔ DEFERRED** — substantial per-file work (~12 ports × read + transcribe + verify byte-equivalent esbuild output); deserves dedicated session.
- [x] **2.7 `CONTRIBUTING.md`** (236 lines) — documents scope-of-fork, TS port RULE (don't sprinkle `any`/`HTMLElement` over `document.querySelectorAll` — types must come from `.d.ts`), toolchain quirks, gradle integration, hard rule **NEVER bypass WebView for FB GraphQL** (Clura 2026 block-rate table), refresh workflow placeholder (≥3-monthly cadence), build commands for Termux (`sh gradlew :app:compileDebugKotlin`, `:app:lintDebug`, `:app:assembleDebug` — NEVER `qualityCheck`, no Spotless/Detekt), PR workflow. Commit `98954b3`.

## Phase 3: Data-layer + registry [BLOCKED ON ENV INPUTS]
Crown-jewel phase. **To unlock: stage `com.facebook.katana.apk` (~200MB) + HAR-with-content from desktop Chrome DevTools in `scripts/inputs/` (create dir first).**

- [ ] **3.1 `scripts/regenerate_gql_ops.py`** — CajuM FlatBuffers decode pipeline; reads `assets/graph_metadata.bin` from inside `com.facebook.katana.apk`, emits `app/src/main/res/raw/graphql_ops.json` (authoritative GraphQL op-name list). Requires `flatc` (Google FlatBuffers compiler) on PATH. Blocked on APK download.
- [ ] **3.2 `scripts/inspect_bundle.sh`** — requires HAR capture from desktop Chrome DevTools (mobile UA loading `m.facebook.com`); greps for `IntersectionObserver(`, `RelayPrefetchedStreamCache`, `fb_api_req_friendly_name`, `data-sigil`. Blocked on HAR capture (not authorable as autonomous agent).
- [ ] **3.3 Emit `res/raw/selectors.json` + `res/raw/feed_schema.json`** — populated from 3.2's HAR analysis. Canonical selector set (`data-sigil`/`data-ft` field names) + `data-ft` sponsored-flag + `mf_story_key`. Blocks Phase 3.4 + 3.7 selector refinement.
- [ ] **3.4 Run `pnpm gen-types`** to refresh `app/src/main/ts/types/*.d.ts` from `res/raw/{graphql_ops,selectors,feed_schema}.json` via `json-schema-to-typescript`. Deferred from Phase 2.4; blocks on 3.1+3.3.
- [ ] **3.5 `scripts/refresh_fb_metadata.sh`** — one-shot ≥3-monthly driver orchestrating APK download + flatc decode + HAR capture prompts + selectors.json regeneration + gen-types. Blocks on 3.1 + 3.2 + 3.3.
- [ ] **3.6 Extend `ExternalRequestInterceptor.kt` with POST-body `fb_api_req_friendly_name` 404-filter** — allowlist `PolarisFeed`/`MBKNNotifications`/`Messaging`/`Profile`; 404 everything else (Stories, Reels, Watch, Marketplace, Gaming, Sponsored, Ads, NFT). Reads POST body via `shouldInterceptRequest`. Massive bandwidth + JS exec drop. Risk: doc_id drift over time → refresh workflow (3.5). Blocks on 3.1's op-name output.
- [ ] **3.7 Author `app/src/main/ts/feed_hydrate.ts`** (crown jewel per (b7) AlterLab) — walks `<script type="application/json" data-content-len="…">` tags at document_start, finds `require[i][0] === "RelayPrefetchedStreamCache"`, descends to `req[3][1].__bbox.result.data`, mutates the data array per user toggles BEFORE React renders. DRAFT authorable without 3.3 outputs (selector refinement requires 3.3). Inject via `WebViewCompat.addDocumentStartJavaScriptCompat(view, js, "*")`.
- [ ] **3.8 Update `MainViewModel.kt` Script list (L42-53)** — drop 5 `hide_*.js` and add `feed_hydrate.js` always-on. Blocks on 3.7 landing first.

## Phase 4: Surviving DOM observer + CSS radix [PENDING]
- [ ] **4.1 Author `app/src/main/ts/adblock-cosmetic.ts`** — single scoped `MutationObserver` on `div[role=feed] subtree:true` + `requestAnimationFrame` debounce + `data-nb-hidden` early-bail flag.
- [ ] **4.2 Replace 60+ lang-suffix sponsored-blocker** with `JSON.parse(node.dataset.ft).<sponsored-flag>` one-line dispatch (field from 3.3 feed_schema.json).
- [ ] **4.3 Author `app/src/main/ts/cosmetic.ts`** — `<style id="nb-cosmetic">` one-shot per `onPageFinished`. Includes Tier A3 global anim/transition strip (`*, *::before, *::after, ::backdrop { animation: none !important; transition: none !important; scroll-behavior: auto !important; }`) + `sticky_navbar` + `amoled_black` + `login-button-hide` + `tap-highlight` + `loading-overlay` + `3rd-button removal` + `user-select: text`.
- [ ] **4.4 Trim `scripts.ts`** — DELETE banner watcher, third-child remover, text-select span-scan, loading-overlay, bottom-banner. RETAIN `backHandlerNB` (scripts.js:120), `SettingsBridge` invoke (scripts.js:301), `ThemeBridge` notify (scripts.js:340 — converted to one-shot Phase 4.5), download-bridge initializer (scripts.js:350-357 wraps `FileReader.readAsDataURL` + `DownloadBridge.downloadBase64File`).
- [ ] **4.5 Author `app/src/main/ts/theme_color.ts`** — one-shot `document.querySelector('meta[name=theme-color]')` extractor on `onPageFinished`, no observer.
- [ ] **4.6 Audit `copy_to_clipboard.ts` for flexbox-order scrambling** — Taprun pattern (`unscrambleByCssOrder(el)` 10-line sort by computed CSS `order` for single-char author spans). Reads hydration JSON.value, not `textContent`. Author `app/src/main/ts/lib/unscrambleByCssOrder.ts` fallback.

## Phase 5: Native toggle migrations (KEEP base64 bridge) [PENDING]
- [ ] **5.1 Delete `pinch_to_zoom.ts`** → `WebSettings.setBuiltInZoomControls(true)` + `setUseWideViewPort(true)` + INVERT gate in `SettingsViewModel.kt` (currently `!pinchToZoom`).
- [ ] **5.2 Ensure native `setUserAgentString(DESKTOP_USER_AGENT)` path** — `desktopLayout` toggle (already native).
- [ ] **5.3 KEEP base64 `ClipboardBridge.copyImageToClipboard(base64, mimeType)` pattern** — does NOT rewrite as native ActionMode/ClipboardManager URL-only flow (FileReader.readAsDataURL preserves FB-session cookies; v2 plan correction vs v1).
- [ ] **5.4 KEEP base64 `DownloadBridge.downloadBase64File(base64, mimeType)` pattern** — same rationale.
- [ ] **5.5 Optional `NativeMediaViewerActivity.kt`** — Compose full-screen image/video viewer (open question, may defer).
- [ ] **5.6 Extend `ExternalRequestInterceptor.kt` for image-resolution downgrade (Tier B6)** — filter `scontent-*.cdninstagram.com/...?stp=...&oh=...` URL to smallest placeholder; full-res on tap-to-native-viewer.

## Phase 6: Aggressive feed prefetch [PENDING]
- [ ] **6.1 Author `app/src/main/ts/prefetch.ts`** — ~15-line `IntersectionObserver` override `rootMargin: "300% 0px 300% 0px"` at document_start; injected via `WebViewCompat.addDocumentStartJavaScriptCompat(view, js, "*")`.
- [ ] **6.2 Opt-in setting `Aggressive feed prefetch`** in `SettingsViewModel.kt` (default off — bandwidth ~1.5× on continuous scroll).
- [ ] **6.3 Re-measure with JankStats** → `baselines/baseline_v2.txt` + `baselines/perf_report_v1.md`. Validate scroll-stop at ~9 entries symptom gone (target: seamless pagination to ~30+ entries). Compare pre/post against `baselines/baseline_v1.txt`.
- [ ] **6.4 Full regression via `AdblockTest.kt` Playwright JVM harness**.

## Phase 7: Refresh tooling + contributing docs [PENDING]
- [ ] **7.1 Document refresh workflow in `CONTRIBUTING.md`** — ≥3-monthly `scripts/refresh_fb_metadata.sh` driver (placeholder line already written in current CONTRIBUTING.md).
- [ ] **7.2 Hard rule in `AGENTS.md`** — NEVER bypass WebView for FB GraphQL (Clura behavioral ML — TLS/JA3 + mouse/scroll + time-on-page; Python 78% / Playwright 31% / Chrome 8% block rates; WebView session inherits human session).
- [ ] **7.3 Document test commands in `AGENTS.md`** — `sh gradlew :app:compileDebugKotlin`, `:app:lintDebug` ordered; NEVER `qualityCheck`; no Spotless/Detekt.
- [ ] **7.4 Wire `AdblockTest.kt` Playwright harness to refresh workflow via GH Actions secrets** (FACEBOOK_USERNAME/PASSWORD).
- [ ] **7.5 Final QC re-measure + manual QA through 13 user-facing toggles.**

## Phase 8: Optional Tier-C Snapshot-feed-from-last-run [DEFERRED]
Only consider if Phase 1–7 leave the cold-launch perceptual gap unacceptable.
- [ ] **8.1 Persist `RelayPrefetchedStreamCache.__bbox.result.data` to `app/cache/feed_snapshot.json`** via new `SnapshotBridge.persistFeed(json)` `@JavascriptInterface`; extend `feed_hydrate.ts` to hand the parsed hydration JSON to native after every successful feed load.
- [ ] **8.2 Read snapshot on cold launch while pre-warm WebView navigates** — render native Compose feed cards (author names from hydration JSON `.value` — Taprun-safe — never `textContent`).
- [ ] **8.3 Fade-over Compose → live WebView on `onPageFinished`** — accept ~3-5 s staleness, NEVER native HTTP fetch to FB (per Phase 7.2 behavioral-ML rule).

## Rejected (NOT in scope — see Context & Decisions table)
- Engine swap (GeckoView / Crosswalk / CEF / Bromite / custom Chromium / TWA)
- `mbasic.facebook.com` forced URL rewrite
- Hide-WebView-as-backend (Phase 3.7 RelayPrefetchedStreamCache patcher is the safe partial realization)
- WASM-Rust DOM matching
- Native HTTP OkHttp to FB GraphQL endpoints
- Writing as native ActionMode/ClipboardManager URL-only flow (would lose FB-session cookies)

## Hard rules (don't violate)
- **ALWAYS `sh gradlew <args>` on Termux** — gradlew lacks exec bit on this clone. NEVER `./gradlew qualityCheck` (too expensive — Nobook build has no Spotless or Detekt gradle plugins configured, only `compileDebugKotlin` and `lintDebug` are useful).
- **KEEP base64 `Clipboard` and `Download` bridges** — `FileReader.readAsDataURL(blob)` preserves FB-session cookies; native URL-only ClipboardManager flows would lose them.
- **TS port ONLY valuable if types generate from `res/raw/*.json` via `json-schema-to-typescript`** — sprinkling `any` on `document.querySelectorAll` is wasted effort (TS port RULE in CONTRIBUTING.md).
- **NEVER bypass WebView for FB GraphQL** — Meta's behavioral ML (Clura TLS/JA3 + mouse/scroll + time-on-page) blocks non-WebView request paths.
- **Don't split files to dodge linter thresholds** — refactor the actual code structure instead; prefer fewer cohesive files over many small ones.
- **Centralize FB mapping data into `res/raw/*.json` registry files** (Phase 3 deliverable) — don't scatter inline Java string selectors across .ts files.
- **Refresh cadence ≥3-monthly** via `scripts/refresh_fb_metadata.sh` (Phase 3.5 / 7.1).

## JS surface corrections to remember
- 12 `.js` files in `res/raw/` (not 11 — actual filename `pinch_to_zoom.js`).
- Inline JS at `NobookWV.kt:91` (`backHandlerNB()` read-back returning `"false"|"exit"|"scrolling"|"top"`); defined at `scripts.js:120`.
- 4 `@JavascriptInterface` bridges registered at `NobookWV.kt` onCreated: `SettingsBridge` (`NobookSettings.kt`), `ThemeBridge` (`ThemeChange.kt`), `DownloadBridge` (`DownloadBridge.kt:21`), `ClipboardBridge` (`ClipboardBridge.kt:20`).
- `scripts.js` is BOTH cosmetic observer layer AND JS↔native bridge router.
- Test infra: `AdblockTest.kt` Playwright JVM test validates `scripts.js` + `adblock.js` against live FB.

## Notes
- 2026-07-09: scoped-tasks MCP state removed (commit `0ae8b99`) per user direction. Per-session tracking moved to built-in `todowrite`.
- 2026-07-09: `plan_save` content was session-scoped; PLAN.md is now the canonical durable home.
- 2026-07-09: Forked ycngmn/Nobook to S0methingSomething/Nobook; PR #184 open at https://github.com/ycngmn/Nobook/pull/184 against `ycngmn/Nobook:main`, base `main`, head `S0methingSomething:feat/v2-safe-aggressive`.
- 2026-07-09: Phase 1 + Phase 2 substantive work committed (~9 commits: `cb61615` deps(1.1) → `e8da633` perf(1.2) JankStats → `44fe475` feat(1.3) webkit gates → `33af0a9` perf(1.4) drop SCRIPT_SRC → `206d31e` feat(1.6+1.7) WebViewInitializer+toggle → `25725d7` feat(2.1+2.5) TS scaffolding → `92184fc` feat(2.2) gradle bundleScripts → `9689bb0` ci(2.3) tsc gate → `98954b3` docs(2.7) CONTRIBUTING.md).
- Phase 3 unlocks on environmental inputs: (a) `com.facebook.katana.apk` (~200MB) for CajuM FlatBuffers decode (3.1), (b) HAR-with-content from desktop Chrome DevTools with mobile UA loading `m.facebook.com` for bundle inspection (3.2).
