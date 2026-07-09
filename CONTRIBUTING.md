# Contributing to Nobook

This fork tracks upstream `ycngmn/Nobook` from `feat/v2-safe-aggressive`. Active
work happens on that branch; everything lands via PR [#184](https://github.com/ycngmn/Nobook/pull/184)
into `ycngmn/Nobook:main` once approved.

The plan being executed (8 phases, 47 items) is canonical via `plan_read`; the
task state lives in `.scoped/tasks.json` (managed by the `scoped-tasks` MCP).

## TS port (Phase 2 — the reason `app/src/main/ts/` exists)

### Why port to TypeScript
1. **Generated types catch schema rotations as compile errors.**
   `res/raw/{graphql_ops,selectors,feed_schema}.json` (Phase 3) are converted to
   `app/src/main/ts/types/*.d.ts` via `json-schema-to-typescript`. When FB rotates
   the selector or GraphQL op-name set, your build fails loudly instead of silently
   shipping a script that selects into the void.
2. **Typed bridge contracts catch Kotlin ↔ JS drift.**
   `app/src/main/ts/bridges.d.ts` mirrors the four `@JavascriptInterface` Kotlin
   methods. If Kotlin changes a param name, type, or arity, tsc surfaces the
   mismatch at CI time rather than producing an `undefined bridge method` runtime
   error on Android.
3. **`tsc --noEmit` is a CI static catch.**
   `.github/workflows/ci.yml` runs it on every push + pull request. Lightweight
   enough to fit alongside the assemble pipeline.

### TS port RULE
Never sprinkle `any` or `HTMLElement` over `document.querySelectorAll` and call
it a port. Types must come from generated `.d.ts` files (Phase 3.4 `gen-types`)
or from `bridges.d.ts`'s global Namespace augmentation. Direct DOM typing is
wasted effort because the DOM types rotate with FB markup — the value is in
catching schema drift, not in rendering IDE hand-holding for code that's
inherently brittle against FB's bundle.

### Toolchain
- **TypeScript** `^5.7.0` + **esbuild** `^0.25.0` (declared in
  `app/src/main/ts/package.json`).
- **pnpm** `11.5.0` (declared as `packageManager:` field in `package.json` +
  pinned via `pnpm-lock.yaml` lockfile v9.0).
- **Node** `>= 22` (Termux env has 24; CI uses node 22).
- **esbuild postinstall** is authorized via `app/src/main/ts/pnpm-workspace.yaml`
  with `allowBuilds: { esbuild: true }` (pnpm 11 dropped `onlyBuiltDependencies`
  support in `package.json`).

### Adding a `.ts` script
1. Create `app/src/main/ts/<name>.ts`.
2. If the script needs a JS↔native bridge, add its ambient type to
   `app/src/main/ts/bridges.d.ts` first; match the Kotlin `@JavascriptInterface`
   signature verbatim.
3. `app/src/main/ts/esbuild.config.js` will pick the file up automatically. Its
   transpiled output lands in `app/src/main/res/raw/<name>.js` overwriting the
   pre-Phase-2.6 hand-authored `.js` of the same name.
4. Listing the resource id in `MainViewModel.kt`'s `Script(...)` registry is the
   only Kotlin-side wiring needed to inject the script at runtime.

### Running the toolchain manually
```sh
cd app/src/main/ts
pnpm install --no-frozen-lockfile   # idempotent once installed
node node_modules/typescript/bin/tsc --noEmit
node esbuild.config.js
```
Direct `node node_modules/<pkg>/bin/<cmd>` invocations bypass the `pnpm run`
implicit install wrapper which checks deps-status every invocation and errors
on ignored-builds if `pnpm-workspace.yaml` isn't honored (relevant for CI
machines without the worktree file).

## Gradle integration (Phase 2.2)

Three Exec tasks chained via `dependsOn`:
- `tsInstall`   → `pnpm install --no-frozen-lockfile --silent`
- `tsTypeCheck` → `node node_modules/typescript/bin/tsc --noEmit`
- `tsBundle`    → `node esbuild.config.js` (emits to `app/src/main/res/raw/*.js`)

`preBuild` depends on `tsBundle`. The chain is cross-platform Exec (no `sh -c`
wrapper) so the same build works on Linux, macOS, and Windows runners.

Today (Phase 2 done, Phase 2.6 pending) the chain is a no-op: no `.ts` sources
exist yet, so `esbuild.config.js` prints `No .ts source files to transpile.` and
exits 0. The existing 12 hand-authored `.js` files in `res/raw/` stay untouched
until Phase 2.6 ports them one-by-one with byte-equivalent transpile-only.

## Hard rule: never bypass WebView for FB GraphQL

FB/Meta's bot detection accumulates per-session signal:
- **TLS fingerprint (JA3)**: any non-standard client TLS profile raises a flag.
- **Behavioral ML**: mouse/scroll velocity, time-on-page, scroll position,
  viewport timing, click cadence. Trained classifier scores every session.
- **Surface match**: how closely the request matches what the bundled GraphQL
  client emits (header set, body shape, op-name selection).

Field-tested block rates (Clura 2026 research):
| Approach | Block rate |
|---|---|
| Python `requests` raw fetch | ~78% |
| Playwright headless | ~31% |
| Real Chrome browser (WebView-equivalent) | ~8% |

**The WebView session inherits the user's auth cookies + their JIT-rendered
React/Relay session, which Meta's risk system scores as low-risk because the
call surface matches what FB's own bundle emitted.**

Any code that opens its own HTTPS channel to FB GraphQL endpoints (native OkHttp
fetch, snapshot feeder, ad probe, refresh tooling, anything) will burn that
session — Meta raises a red flag immediately on the first request.

**Forbidden in this fork:**
- Native `OkHttpClient` calls to `m.facebook.com/api/graphql/` or
  `/graphqlbatch/` or any `*.facebook.com/graphql*` path.
- Snapshot feeders that re-fetch FB content outside the WebView.
- Ad probes / screenshot tools that issue FB requests.

**Allowed:**
- `shouldInterceptRequest` on the existing WebView: the request is already
  inside Meta's trust envelope when the interceptor sees it; blocking or
  rewriting a request in-flight does NOT burn the session.
- DOM scraping via the `evaluateJavascript` bridge: the JS runs inside the
  trusted WebView's own renderer, in the same JS context as FB's bundle.

## Refresh workflow (≥3-monthly cadence)

FB markup + GraphQL op-names rotate roughly quarterly. The refresh tooling
(Phase 7.1) is a one-shot `scripts/refresh_fb_metadata.sh` driver that:
1. Re-downloads a current `com.facebook.katana.apk`.
2. Decodes `assets/graph_metadata.bin` (FlatBuffers) via
   `scripts/regenerate_gql_ops.py` (Phase 3.1, CajuM pipeline).
3. Captures a fresh HAR from `m.facebook.com` (mobile UA).
4. Diffs selectors + op-names vs previous run.
5. Propagates any changes to `res/raw/{graphql_ops,selectors,feed_schema}.json`
   and regenerates `app/src/main/ts/types/*.d.ts`.

Treat as a ≥3-monthly manual task (~1 human-hour per release). The two JSON
files update the entire corpus of FB-specific knowledge for both the native
interceptor (Kotlin) and the cosmetic JS layer (TS) at the same time.

## Build commands (Termux + Linux)

The Android Gradle build scripts accept the standard AGP task set. Nobook
configures **no Spotless and no Detekt gradle plugins**. Useful lints:

```sh
sh gradlew :app:compileDebugKotlin    # compile-only check (~20-30s)
sh gradlew :app:lintDebug             # AGP lint
sh gradlew :app:assembleDebug         # full APK (triggered by preBuild which runs tsBundle)
```

NEVER run `./gradlew qualityCheck` — too expensive; Nobook hasn't configured
it and upstream uses it for a full QA pass that's overkill in CI iterations.

### Gradle wrapper invocation on this Termux clone

`gradlew` lacks the executable bit on this Termux clone, so prefix every
invocation with `sh gradlew <args>` instead of `./gradlew <args>`:

```sh
sh gradlew :app:compileDebugKotlin
```

Every Gradle run takes ~20-80s depending on cache state. Batch iteration
through CI-style output is more reliable than running gradlew serially in
interactive shells.

## PR workflow

1. **Fork** `S0methingSomething/Nobook` from `ycngmn/Nobook` (or push to the
   shared `S0methingSomething` org fork).
2. **Branch** off `feat/v2-safe-aggressive` for plan v2 work, or off `main` for
   small fixes.
3. **Commit messages** follow `type(phase.item): subject` format:
   - `feat(2.2): Gradle bundleScripts task chain ...`
   - `perf(1.2): wire JankStats in NobookWV → baselines/baseline_v1.txt`
   - `deps(1.1): add androidx.webkit + androidx.startup + jankstats`
4. **Commit body** references PR #184 + `plan v2 (canonical via plan_save)` +
   the relevant `scoped-tasks` task/item IDs.
5. **Squash** commits into one per phase-item when merging.
6. **Push** to your fork; open a PR against `ycngmn/Nobook:main` or against
   `S0methingSomething:feat/v2-safe-aggressive` for incremental review.

## Scope of the v2 plan (what this fork deliberately does NOT change)

- **No engine swap** — stock `android.webkit.WebView`. GeckoView halves
  page-load speed on heavy sites; there is no production system-WebView
  replacement (Bromite is root-only, Crosswalk EOL'd, CEF is desktop-only,
  TWA disqualifies unowned origins).
- **No mbasic rewrite** — too destructive to the action surface (no
  reactions-popover, no stories, no inline media viewer). The aggressive
  gain on load speed is not worth losing half the user features.
- **No full hide-WebView-as-backend** — the (b1)/m0008 "junky" scrape
  approach is brittle against every FB markup rotation. Phase 3.7 reuses
  its most valuable idea (RelayPrefetchedStreamCache hydration-data
  patcher) at `document_start` WITHOUT needing to scrape the rendered DOM
  surface, which is the safe partial realization.
- **No WASM-Rust DOM** — DOM is only reachable via JS bindings with
  marshalling cost per JS-DOM call. A hot observer firing hundreds of
  times per second across the WASM↔JS boundary is slower than the same
  observer in JS; WASM wins on compute (crypto, codecs, parsers), loses
  on DOM I/O.
- **No native HTTP GraphQL** — see the Hard Rule above.
