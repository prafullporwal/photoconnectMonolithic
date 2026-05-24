import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// Vite dev server for the PhotoConnect SPA (monolith edition).
//
//   * Tailwind v4 plugs in directly — no postcss/tailwind config files needed,
//     all of Tailwind's setup is the `@import "tailwindcss"` in index.css.
//
//   * The /api proxy forwards every front-end fetch to the monolithic Spring
//     Boot app on :8080. In the microservices version this was the api-gateway;
//     in the monolith it is the single application server. Same port, same
//     /api/v1/* paths — no SPA code changes were needed.
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    allowedHosts: ['.trycloudflare.com', 'localhost'],
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // Forward /<bucket>/** to MinIO so root-relative media URLs work via
      // Cloudflare tunnel (where localhost:9000 is unreachable from the browser).
      '/photoconnect-media': {
        target: 'http://localhost:9000',
        changeOrigin: true,
      },
    },
  },
})
