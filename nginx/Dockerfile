# =============================================================================
# nginx image — serves the React SPA + reverse-proxies /api to the monolith
# =============================================================================
# REQUIRES the frontend to be pre-built on the host:
#   cd frontend && npm install && npm run build
# (frontend/dist/ is .gitignored — same host-build pattern as the JAR.)
#
# Built from the repo root so the COPY can reach frontend/dist/ and nginx/.
# =============================================================================
FROM nginx:1.27-alpine
COPY frontend/dist/ /usr/share/nginx/html/
COPY nginx/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
