# Agentic Example

This repository is an example Spring Boot application demonstrating a Domain-Driven Design (DDD) approach for an AI/agentic system.

Purpose of this project:
- Show a clear separation of concerns across `domain`, `application`, and `infrastructure` layers.
- Demonstrate integrations for Chat, RAG (retrieval-augmented generation), and PDF ingestion.

Technologies
- Java 17+
- Spring Boot
- PostgreSQL + pgvector (vector store)
- OpenAI and Google GenAI integrations
- Reactor (`Flux`) for Server-Sent Events (SSE) streaming

Project structure (DDD-aligned)
- `domain/` — domain models and abstract ports.
- `application/` — use-case services and application logic (e.g. `ChatService`, `PdfIngestService`).
- `infrastructure/` — adapters and implementations (REST controllers, PDF splitter, external API adapters, vector store config).

Mapping examples:
- `src/main/java/com/nhmk/agentic_example/domain` — domain models and port interfaces.
- `src/main/java/com/nhmk/agentic_example/application` — application services (use case implementations).
- `src/main/java/com/nhmk/agentic_example/infrastructure/rest` — REST controllers and mappers.

Main endpoints
- `POST /api/chat` — send a prompt and receive a synthesized response.
- `GET  /api/chat/rag?question=...` — RAG endpoint (retrieval + generation).
- `POST /api/chat/stream` — SSE streaming endpoint for real-time responses.
- `POST /api/rag/ingest` — upload a PDF as multipart (`file`) to split into chunks and ingest.

Configuration / Environment variables
- Database: `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` (see `src/main/resources/application.yml`).
- OpenAI key: `SPRING_AI_OPENAI_API_KEY`
- Google GenAI key: `SPRING_AI_GOOGLE_GENAI_API_KEY`
- Google Search: `GOOGLE_SEARCH_API_KEY` and `GOOGLE_SEARCH_CX`
- News API: `NEWS_API_KEY`
- Exchange Rate API: `EXCHANGE_RATE_API_KEY`

Important: the `application.yml` file in the repository contains sample default values. Override these with real secrets via environment variables for any non-local environment.

Run locally
1. Prepare a PostgreSQL database (e.g. `agenticdb`) and update `application.yml` or set environment variables.
2. Set required API keys as environment variables.
3. Start the app with Gradle:

```bash
./gradlew bootRun
```

Or build and run the jar:

```bash
./gradlew clean bootJar
java -jar build/libs/agentic-example.jar
```

Prompts
- Templates are stored in `src/main/resources/prompts` (StringTemplate files) and `system.st`.

Design notes & DDD guidance
- This README documents the DDD layering used in the codebase so contributors can place business logic in `domain`/`application` and keep adapters in `infrastructure`.
- When adding features, create a new domain port if needed and implement it in the appropriate layer (application or infrastructure) depending on whether it is pure business logic or an external adapter.

Support & development
- To add features: define ports in `domain`, implement use-cases in `application`, and provide adapters in `infrastructure`.

Maintainer
- dua75

---
Version: README updated to English to match the codebase (updated: 2026-01-03)
