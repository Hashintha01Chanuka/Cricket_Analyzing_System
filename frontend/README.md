# Cricket Analytics — Frontend

React + Redux Toolkit frontend, talking to the platform exclusively through the
API Gateway (never to an individual service's own port).

## Design notes

- **Everything goes through the gateway.** `src/api/client.js` is the one
  place the frontend knows a backend address — `http://localhost:8080` in
  dev. No page or component ever imports a service's port directly. If a
  service's internal port changes, or the whole backend moves behind a real
  domain later, this is the only file that changes.
- **JWT lives in `localStorage` and Redux.** The axios interceptor in
  `client.js` reads it fresh on every request (not captured once at
  startup), so a login/logout mid-session is picked up immediately without
  a page reload. A 401 response clears the stored session automatically.
- **`RoleGuard` is a UI convenience, not a security boundary** — it hides
  buttons/forms a user's role can't use so they're not confronted with a
  403 after clicking. The actual enforcement is entirely server-side
  (`@PreAuthorize` in each Spring service, covered by real tests there). If
  `RoleGuard` were deleted outright, no unauthorized action could still
  succeed — worth saying exactly that if asked "where's your authorization
  enforced" in an interview.
- **The live match view polls, it doesn't hold a Kafka connection of its
  own.** `MatchDetailPage` polls Stats Service's `/live` endpoint (the one
  backed by the Kafka-consumed `LiveMatchState` table) every 5 seconds
  while a match is `LIVE`. The frontend doesn't need to know Kafka exists —
  it's just reading a REST endpoint that happens to be kept fresh by events
  behind the scenes.
- **Design system**: a stadium-under-floodlights palette (deep turf green,
  chalk white, floodlight gold, worn-ball red) with `Oswald` for headings/
  scores, `IBM Plex Sans` for UI text, and `IBM Plex Mono` for every numeric
  stat. The one deliberately bold element is the match `Scoreboard`
  component, styled as an actual digital scoreboard panel — everything else
  stays quiet by comparison so that panel reads as the focal point.

## Setup

```bash
cd frontend
npm install
cp .env.example .env   # only needed if the gateway isn't on localhost:8080
npm run dev
```

Runs on `http://localhost:5173`. Requires the API Gateway (and the services
behind it) running — see the gateway's own README for how to bring those up.

**I was not able to run `npm install` or build this myself** — the sandbox
this was built in has no npm registry access at all (every package request
returns 403, not just missing ones). This code has not been executed or
type-checked. Run `npm install && npm run dev` and treat the first error, if
any, as the starting point — this is hand-written against React 18 / Redux
Toolkit 2.x APIs I'm confident in, but there's a real chance of a small typo
surfacing (an import path, a prop name) that only shows up at runtime.

## Structure

```
src/
├── api/            One file per backend service, all through client.js
├── store/          Redux Toolkit slices — one per domain (auth, matches, players, stats, notifications)
├── components/      Shared UI: Navbar, Scoreboard, AddBallForm, RoleGuard, ProtectedRoute
├── pages/           One component per route
└── styles/          tokens.css (design system variables), components.css, pages.css
```

## Pages and roles

| Route | Who sees it | Notes |
|---|---|---|
| `/matches` | Everyone | Public read, matches the backend's public GET endpoints |
| `/matches/new` | ADMIN only (link hidden otherwise) | Backend rejects non-admins regardless |
| `/matches/:id` | Everyone | Scoreboard + win probability calculator are public; the "record a ball" form only renders for ADMIN/SCORER |
| `/players`, `/players/:id`, `/leaderboard` | Everyone | All public reads |
| `/players/new` | ADMIN only | Same pattern as match creation |
| `/login`, `/register` | Everyone | Registration always creates a VIEWER, matching the backend |

## What's not built yet

- No dedicated "my notifications" or "mark all read" flow — `NotificationsFeed`
  is scoped to one match only.
- No admin UI for promoting a user's role (`PATCH /users/{id}/role` exists on
  the backend but has no frontend page) — currently something you'd do with
  curl/Postman.
- No dark mode, no mobile-specific layout beyond basic responsive grid
  collapse.
