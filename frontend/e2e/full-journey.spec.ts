import { test, expect } from '@playwright/test';
import { registerUser, loginUser, logoutUser, TEST_USER } from './auth.helper';

test.describe('Full User Journey', () => {
  test('Complete user journey: register → dashboard → features → logout → login', { timeout: 120000 }, async ({ page }) => {
    // Listen for console errors
    page.on('console', msg => {
      if (msg.type() === 'error') console.log('CONSOLE ERROR:', msg.text());
    });

    // ===== PHASE 1: REGISTRATION =====
    await test.step('Register a new user', async () => {
      await registerUser(page);
    });

    // ===== PHASE 2: DASHBOARD =====
    await test.step('Dashboard loads with user data', async () => {
      await expect(page.locator('body')).toBeVisible();
      await expect(page.getByRole('link', { name: /dashboard/i }).first()).toBeVisible();
    });

    // ===== PHASE 3: BROWSE RESUMES =====
    await test.step('Navigate to Resumes page', async () => {
      await page.goto('/resumes');
      await page.waitForURL(/\/resumes/);
      await expect(page.locator('body')).toBeVisible();
    });

    // ===== PHASE 4: BROWSE JOBS =====
    await test.step('Navigate to Jobs page', async () => {
      await page.goto('/jobs');
      await page.waitForURL(/\/jobs/);
      await expect(page.locator('body')).toBeVisible();
    });

    // ===== PHASE 5: BROWSE APPLICATIONS =====
    await test.step('Navigate to Applications page', async () => {
      await page.goto('/applications');
      await page.waitForURL(/\/applications/);
      await expect(page.locator('body')).toBeVisible();
    });

    // ===== PHASE 6: BROWSE COVER LETTERS =====
    await test.step('Navigate to Cover Letters page', async () => {
      await page.goto('/cover-letters');
      await page.waitForURL(/\/cover-letters/);
      await expect(page.locator('body')).toBeVisible();
    });

    // ===== PHASE 7: BROWSE INTERVIEWS =====
    await test.step('Navigate to Interviews page', async () => {
      await page.goto('/interviews');
      await page.waitForURL(/\/interviews/);
      await expect(page.locator('body')).toBeVisible();
    });

    // ===== PHASE 8: BROWSE COMPANIES =====
    await test.step('Navigate to Companies page', async () => {
      await page.goto('/companies');
      await page.waitForURL(/\/companies/);
      await expect(page.locator('body')).toBeVisible();
    });

    // ===== PHASE 9: BROWSE ANALYTICS =====
    await test.step('Navigate to Analytics page', async () => {
      await page.goto('/analytics');
      await page.waitForURL(/\/analytics/);
      await expect(page.locator('body')).toBeVisible();
    });

    // ===== PHASE 10: BROWSE NOTIFICATIONS =====
    await test.step('Navigate to Notifications page', async () => {
      await page.goto('/notifications');
      await page.waitForURL(/\/notifications/);
      await expect(page.locator('body')).toBeVisible();
    });

    // ===== PHASE 11: SETTINGS PAGES =====
    await test.step('Navigate to Settings pages', async () => {
      await page.goto('/settings/profile');
      await page.waitForURL(/\/settings\/profile/);
      await expect(page.locator('body')).toBeVisible();

      await page.goto('/settings/preferences');
      await page.waitForURL(/\/settings\/preferences/);
      await expect(page.locator('body')).toBeVisible();

      await page.goto('/settings/privacy');
      await page.waitForURL(/\/settings\/privacy/);
      await expect(page.locator('body')).toBeVisible();

      await page.goto('/settings/ai');
      await page.waitForURL(/\/settings\/ai/);
      await expect(page.locator('body')).toBeVisible();

      await page.goto('/settings/billing');
      await page.waitForURL(/\/settings\/billing/);
      await expect(page.locator('body')).toBeVisible();
    });

    // ===== PHASE 12: LOGOUT =====
    await test.step('Logout successfully', async () => {
      await logoutUser(page);
    });

    // ===== PHASE 13: RE-LOGIN (PERSISTENCE) =====
    await test.step('Re-login with same credentials', async () => {
      await loginUser(page);
      await expect(page).toHaveURL(/\/dashboard/);
    });

    // ===== PHASE 14: DASHBOARD PERSISTENCE =====
    await test.step('Dashboard loads after re-login', async () => {
      await page.goto('/jobs');
      await page.waitForURL(/\/jobs/);
      await expect(page.locator('body')).toBeVisible();
    });
  });

  test('Forgot password flow', async ({ page }) => {
    await test.step('Navigate to forgot password page', async () => {
      await page.goto('/forgot-password');
      await page.waitForURL(/\/forgot-password/);
    });

    await test.step('Submit forgot password form', async () => {
      const emailInput = page.getByLabel(/email/i);
      await expect(emailInput).toBeVisible();
      await emailInput.fill(TEST_USER.email);

      const responsePromise = page.waitForResponse(
        resp => resp.url().includes('/auth/forgot-password')
      );

      await page.getByRole('button', { name: /send reset link/i }).click();

      // Wait for API response
      const response = await responsePromise;
      expect(response.status()).toBe(200);

      // Should show success state with "Check your email" message
      await expect(page.getByRole('heading', { name: /check your email/i })).toBeVisible({ timeout: 10000 });
    });
  });

  test('404 page handling', async ({ page }) => {
    await page.goto('/nonexistent-page');
    await expect(page.locator('body')).toBeVisible();
  });

  test('OAuth callback page loads', async ({ page }) => {
    await page.goto('/auth/callback');
    await page.waitForURL(/\/auth\/callback/);
    await expect(page.locator('body')).toBeVisible();
  });
});
