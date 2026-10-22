import { expect, test } from '@playwright/test';

test('Ticket anlegen, Sport erfassen, Summe und Neuladen prüfen', async ({ page }) => {
  const title = `Browsercheck ${Date.now()}`;
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Zeit für einen Überblick.' })).toBeVisible();
  await page.getByLabel('Titel', { exact: true }).fill(title);
  await page.getByRole('button', { name: '+ Ticket anlegen' }).click();
  await expect(page.getByRole('heading', { name: title, exact: true })).toBeVisible();
  await page.getByLabel('Datum', { exact: true }).fill('2026-10-22');
  await page.getByLabel('Sportart', { exact: true }).fill('Laufen');
  await page.getByLabel('Dauer in Minuten').fill('0');
  await expect(page.getByRole('button', { name: 'Erfassung speichern' })).toBeDisabled();
  await page.getByLabel('Dauer in Minuten').fill('120');
  await page.getByRole('button', { name: 'Erfassung speichern' }).click();
  await expect(page.getByRole('status')).toHaveText('Sporterfassung gespeichert.');
  await expect(page.locator('.total strong')).toHaveText('2 h');
  await page.getByLabel('Dauer in Minuten').fill('30');
  await page.getByRole('button', { name: 'Erfassung speichern' }).click();
  await expect(page.locator('.total strong')).toHaveText('2 h 30 min');
  await expect(page.locator('.record')).toHaveCount(2);
  await expect(page.locator('.record').first()).toContainText('22.10.2026');
  await page.reload();
  await page.getByRole('button', { name: new RegExp(title) }).click();
  await expect(page.locator('.total strong')).toHaveText('2 h 30 min');
  await expect(page.locator('.record')).toHaveCount(2);
  await page.screenshot({ path: 'test-results/desktop.png', fullPage: true });
  await page.setViewportSize({ width: 390, height: 844 });
  await expect(page.getByRole('button', { name: 'Erfassung speichern' })).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(
    true,
  );
  await page.screenshot({ path: 'test-results/mobile.png', fullPage: true });
});

test('API-Ausfall wird verständlich angezeigt und Wiederholen ist möglich', async ({ page }) => {
  await page.route('**/api/**', (route) => route.abort());
  await page.goto('/');
  await expect(page.getByRole('alert')).toContainText('Server ist nicht erreichbar');
  await expect(page.getByRole('button', { name: 'Erneut laden' })).toBeEnabled();
  await page.unroute('**/api/**');
  await page.getByRole('button', { name: 'Erneut laden' }).click();
  await expect(page.getByRole('heading', { name: 'Was steht an?' })).toBeVisible();
  await expect(page.getByRole('alert')).toHaveCount(0);
});
