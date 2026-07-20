# Deploying Blade Runner Server

The backend runs as a single Docker container behind Nginx Proxy Manager (NPM),
served at `https://bladerunner.mozzon.net`. NPM handles TLS; the container only
speaks plain HTTP on port 8080 inside the Docker network it shares with NPM.

## Prerequisites

- Docker + Docker Compose on the host
- Nginx Proxy Manager already running (in Docker) on the same host
- A DNS `A` record: `bladerunner.mozzon.net` → the host's public IP

## 1. Docker network shared with NPM

The container is reached by NPM through its container name, so both must sit on
the same Docker network. Nginx Proxy Manager already runs on `nginx-network`,
so the backend just joins it (see `PROXY_NETWORK` below). Nothing to create.

Confirm the network exists:

```bash
docker network ls | grep nginx-network
```

## 2. Configure the environment

```bash
cp .env.example .env
```

Fill in `.env`:

- `APP_URL=https://bladerunner.mozzon.net` — public URL; the worker posts scan
  results back here, and it is handed to the mobile app.
- `PROXY_NETWORK=nginx-network` — the network NPM runs on.
- `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`, `GITHUB_PAT`, `BACKEND_TOKEN`,
  `SONAR_TOKEN` — secrets.

`.env` is gitignored — never commit it.

## 3. Build and start

```bash
docker compose up --build -d
docker compose logs -f backend      # watch startup
```

The container exposes no host port: it is reachable only from the
`nginx-network`. Verify it is healthy:

```bash
docker inspect --format '{{.State.Health.Status}}' blade-runner-backend
```

## 4. Add the proxy host in NPM

In the NPM UI → **Hosts → Proxy Hosts → Add Proxy Host**:

- Domain Names: `bladerunner.mozzon.net`
- Scheme: `http`
- Forward Hostname / IP: `blade-runner-backend`
- Forward Port: `8080`
- **Websockets Support**: on (needed for the STOMP `/ws-metrics` channel)
- Block Common Exploits: on
- SSL tab: request a new Let's Encrypt certificate, Force SSL + HTTP/2

Then `https://bladerunner.mozzon.net/actuator/health` should return `{"status":"UP"}`.

## Updating

```bash
git pull
docker compose up --build -d
```

The H2 database lives in the named volume `backend_data` and survives rebuilds.

## Notes

- No host port is published. To expose 8080 directly for debugging, add
  `ports: ["8080:8080"]` to the `backend` service and point NPM at the host IP
  instead of the container name.
- `SPRING_PROFILES_ACTIVE=unsecured` and the H2 console (`/h2-console`) are meant
  for the test deployment. Disable the console before any long-lived exposure.
- The mobile app targets `APP_URL` in production builds; keep the two in sync.
