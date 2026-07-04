import { test, expect } from '@playwright/test';

test.describe('Navigation', () => {
  test('login page shows OAuth buttons', async ({ page }) => {
    await page.goto('/login');
    await expect(page.getByRole('button', { name: /google/i })).toBeVisible();
    await expect(page.getByRole('button', { name: /linkedin/i })).toBeVisible();
    await expect(page.getByRole('button', { name: /github/i })).toBeVisible();
  });

  test('forgot password link exists on login page', async ({ page }) => {
    await page.goto('/login');
    await expect(page.getByRole('link', { name: /forgot password/i })).toBeVisible();
  });

  test('settings page redirects to login when unauthenticated', async ({ page }) => {
    await page.goto('/settings/profile');
    await expect(page).toHaveURL(/\/login/);
  });
});
