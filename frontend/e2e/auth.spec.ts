import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {
  test('shows validation errors on empty form', async ({ page }) => {
    await page.goto('/login');
    // Click Sign In with empty fields - triggers client-side validation
    await page.getByRole('button', { name: /sign in/i }).click();
    // Wait a moment for React to process and re-render
    await page.waitForTimeout(500);
    // Check for validation errors
    await expect(page.getByText('Email is required')).toBeVisible({ timeout: 5000 });
    await expect(page.getByText('Password is required')).toBeVisible({ timeout: 5000 });
  });

  test('shows error on wrong credentials', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel(/email/i).fill('wrong@test.com');
    await page.getByLabel(/password/i).fill('wrongpassword');
    
    // Listen for API response
    const responsePromise = page.waitForResponse(
      resp => resp.url().includes('/auth/login')
    );
    await page.getByRole('button', { name: /sign in/i }).click();
    
    // Wait for the API call
    const response = await responsePromise;
    expect(response.status()).toBe(401);
    
    // Toast should show error message
    await expect(page.getByText(/invalid|error|failed/i).or(page.locator('[role="status"]')).first()).toBeVisible({ timeout: 10000 });
  });
});
