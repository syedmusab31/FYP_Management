import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:3301',
        changeOrigin: true,
        secure: false,
      },
      '/uploads': { // Proxy uploads folder too if needed
        target: 'http://localhost:3301',
        changeOrigin: true,
        secure: false,
      }
    },
  },
})
