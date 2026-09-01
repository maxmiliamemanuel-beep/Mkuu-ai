# MKUU AI

A production-oriented Android AI assistant with a server-side AI gateway. The Android app never contains a provider key: configure `backendUrl` through Gradle (`./gradlew assembleDebug -PbackendUrl=https://api.yourdomain.example/`) and deploy `backend/` with secrets injected by your host.

## Capabilities
- Compose Material 3 chat, dark/light-ready theme, multiline composer, streaming/stop controls, copy/regenerate actions and polished empty state.
- Room-backed conversations and messages survive restarts.
- HTTPS-only Android networking and configurable backend URL.
- SSE chat proxy plus protected image endpoint, strict input validation, payload limits, security headers, and optional origin allowlist.

## Run the backend
```bash
cd backend
cp .env.example .env # set real provider credentials in host environment
npm install
npm run dev
```

Use a TLS-terminating production deployment; do not use HTTP or commit `.env` files.
