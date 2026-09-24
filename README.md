# Campus Lost & Found

A full-stack web app where students report lost or found items on campus, search live listings,
and safely claim items through owner-approved verification with an automatic matching engine.

## What it does?

Every week students across campus lose IDs, water bottles, laptops and keys and the people who
find them have nowhere common to hand them over. Existing solutions are WhatsApp groups and through word of mouth that the lost item is shared or requested. Campus Lost & Found gives the campus one searchable, trust-based
platform: found items are automatically matched against open lost reports and claiming an item
requires answering a private verification question that only the true owner is likely to answer 
so items get back to the right person without anyone's contact info going public.

## Features

- JWT register/login restricted to college email domains
- Post LOST / FOUND reports (category, location, date, optional image URL)
- Public browse with filters (type, category, location, date range) + pagination
- Auto-matching engine: weighted rule-based scoring 
- Claim flow with a private verification question + owner approve/reject
- Approving a claim auto-rejects competing pending claims and locks the item
- "My Reports": edit, close and review claims on your own posts
- Dockerized backend + Postgres

## Tech stack

| Layer    | Choices |
|----------|---------|
| Backend  | Java 21, Spring Boot 3, Spring Data JPA, Spring Security + JWT (stateless), Bean Validation |
| Database | PostgreSQL |
| Frontend | React, Vite|
| API docs | springdoc-openapi (Swagger UI) |
| DevOps   | Docker + docker-compose  |

## How the matching engine works 

When a new report is posted, it is compared against every open report of the opposite type
(LOST ↔ FOUND) and scored out of 100:

| Signal | Points | Why |
|--------|--------|-----|
| Same category | +40 | A lost "Electronics" item like earbuds rarely matches a found "Books" item |
| Shared words in location | +20 | "Library" vs "Library entrance" and also "Way to library" should count |
| Description keyword overlap (Jaccard similarity ≥ 0.15 on significant words) | +30 | Shared distinctive words ("silver", "macbook") are strong evidence |
| Event dates within 3 days | +10 | Losses and finds cluster in time |

Score ≥ 50 -> a `Match` row is created and shown to the owner as a **"Possible match found"**
banner. Three rules: an item never matches itself or the same user's other items;
duplicate pairs are never created twice; matches involving closed/claimed items stop showing
as active.

## Claim & verification flow

1. A finder posts a FOUND item with a **private verification question** (e.g. "What colour is the
   sticker?"). The question and submitted answers are **never** included in public API responses.
2. A claimant submits an identifying answer.
3. The owner reviews claims + answers and approves or rejects.
4. Approval sets the item to `CLAIMED` and **auto-rejects every other pending claim** on that
   item.

## REST API

| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/auth/register` | Public (college email only) |
| POST | `/api/auth/login` | Public -> returns JWT |
| POST | `/api/items` | Auth |
| GET  | `/api/items` | Public (filters: type, category, location, from, to, page, size) |
| GET  | `/api/items/{id}` | Public (hides the verification question) |
| PUT  | `/api/items/{id}` | Owner |
| DELETE | `/api/items/{id}` | Owner |
| GET  | `/api/items/{id}/matches` | Owner |
| GET  | `/api/items/mine` | Auth |
| POST | `/api/items/{id}/claims` | Auth (not own item; item must be OPEN/MATCHED) |
| GET  | `/api/items/{id}/claims` | Owner |
| PUT  | `/api/claims/{id}/approve` · `/reject` | Owner |

## Running locally

**Prerequisites:** JDK 21+, Docker Desktop, Node 18+.

```bash
# 1. Backend + Postgres in one command
docker compose up --build
#    → API on http://localhost:8080  (Swagger at /swagger-ui.html)

# 2. Frontend (second terminal)
cd frontend
npm install
npm run dev
#    → UI on http://localhost:5173
