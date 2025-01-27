///<reference types="vitest" />

import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    test: {
        environment: "jsdom",
    },
    server: {
        proxy: {
            "/todo": {
                target: "http://localhost:8080",
                changeOrigin: true,
            },
        },
    },
});
