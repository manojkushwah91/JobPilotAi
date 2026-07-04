import { test, expect } from '@playwright/test';

test.describe('Smoke Tests', () => {
  test('login page loads', async ({ page }) => {
    await page.goto('/login');
    await expect(page.getByRole('heading', { name: /welcome back/i })).toBeVisible();
    await expect(page.getByLabel(/email/i)).toBeVisible();
    await expect(page.getByLabel(/password/i)).toBeVisible();
  });

  test('dashboard redirects to login when unauthenticated', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/\/login/);
  });

  test('register page has link to login', async ({ page }) => {
    await page.goto('/register');
    await expect(page.getByRole('link', { name: /sign in/i })).toBeVisible();
  });

  test('landing page loads with features', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('heading', { name: /career operating system/i })).toBeVisible();
    await expect(page.getByText(/AI Resume Studio/)).toBeVisible();
    await expect(page.getByText(/Job Discovery/)).toBeVisible();
  });
});
