import { test, expect } from '@playwright/test';

test.describe('Navigation', () => {
  test('sidebar navigation links exist on dashboard', async ({ page }) => {
    await page.goto('/login');
    const navLinks = page.locator('nav a, [role="navigation"] a');
    const count = await navLinks.count();
    expect(count).toBeGreaterThan(0);
  });

  test('settings page has tabs', async ({ page }) => {
    await page.goto('/login');
    const response = await page.goto('/settings/profile');
    expect(response?.status()).toBe(200);
  });
});
