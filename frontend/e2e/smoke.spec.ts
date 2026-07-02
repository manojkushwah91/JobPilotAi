import { test, expect } from '@playwright/test';

test.describe('Smoke Tests', () => {
  test('login page loads', async ({ page }) => {
    await page.goto('/login');
    await expect(page.getByRole('heading', { name: /sign in/i })).toBeVisible();
    await expect(page.getByLabel(/email/i)).toBeVisible();
    await expect(page.getByLabel(/password/i)).toBeVisible();
  });

  test('dashboard redirects to login when unauthenticated', async ({ page }) => {
    await page.goto('/resumes');
    await expect(page).toHaveURL(/\/login/);
  });

  test('register page has link to login', async ({ page }) => {
    await page.goto('/register');
    await expect(page.getByRole('link', { name: /sign in/i })).toBeVisible();
  });

  test('page has proper title', async ({ page }) => {
    await page.goto('/login');
    await expect(page).toHaveTitle(/JobPilot/);
  });
});
