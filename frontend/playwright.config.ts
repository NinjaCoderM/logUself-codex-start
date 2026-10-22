import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  workers: 1,
  use: {
    baseURL: 'http://127.0.0.1:4200',
    channel: 'msedge',
    headless: true,
    screenshot: 'only-on-failure',
  },
});
