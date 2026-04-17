# orientdb-scala

Toy Scala 2 + cats-effect + http4s application for experimenting with OrientDB and live queries.

## What it does

- `POST /entity` with payload `{ "id": 100 }` inserts an entity into OrientDB class `Entity`.
- `DELETE /entity/{id}` deletes the entity by `id`.
- `GET /entity` upgrades to a websocket and streams live-query events:
  - `{ "add": 100 }`
  - `{ "remove": 100 }`

A small browser client is included at `/` to manually test the API and websocket stream.

## Local toy environment

Start OrientDB with Docker:

```bash
docker compose up -d
```

Run the Scala app (requires sbt + JDK 17+):

```bash
sbt run
```

The app defaults to:

- HTTP: `http://localhost:8080`
- OrientDB URL: `remote:localhost`
- OrientDB root user/password: `root` / `rootpwd`
- DB name: `toy`
- DB user/password: `root` / `rootpwd`

You can override with environment variables:

- `ORIENTDB_URL`
- `ORIENTDB_SERVER_USER`
- `ORIENTDB_SERVER_PASSWORD`
- `ORIENTDB_DATABASE`
- `ORIENTDB_DB_USER`
- `ORIENTDB_DB_PASSWORD`
- `HTTP_HOST`
- `HTTP_PORT`

## Quick manual test

1. Open `http://localhost:8080`
2. Click **POST /entity**
3. Click **DELETE /entity/:id**
4. Observe websocket events in the page log

## Tests

```bash
sbt test
```
