import { Page, expect } from '@playwright/test';

const TEST_USER = {
  name: 'QA Test User',
  email: `qa-test-${Date.now()}@jobpilot.dev`,
  password: 'TestPass123!',
};

export { TEST_USER };

export async function registerUser(page: Page) {
  await page.goto('/register');
  
  // Fill registration form
  await page.getByLabel(/full name/i).fill(TEST_USER.name);
  await page.getByLabel(/email/i).fill(TEST_USER.email);
  await page.getByLabel(/^password/i).fill(TEST_USER.password);
  await page.getByLabel(/confirm password/i).fill(TEST_USER.password);
  
  // Listen for the register API response
  const responsePromise = page.waitForResponse(
    resp => resp.url().includes('/auth/register') && resp.status() >= 200
  );
  
  // Submit
  await page.getByRole('button', { name: /create account/i }).click();
  
  // Wait for API response
  const response = await responsePromise;
  const responseBody = await response.json();
  
  if (!response.ok()) {
    throw new Error(`Registration failed: ${response.status()} ${JSON.stringify(responseBody)}`);
  }
  
  // Should redirect to dashboard after successful registration
  await page.waitForURL(/\/dashboard/, { timeout: 15000 });
  
  // Verify dashboard loaded with user info
  await expect(page).toHaveURL(/\/dashboard/);
}

export async function loginUser(page: Page, email?: string, password?: string) {
  await page.goto('/login');
  
  await page.getByLabel(/email/i).fill(email || TEST_USER.email);
  await page.getByLabel(/password/i).fill(password || TEST_USER.password);
  
  const responsePromise = page.waitForResponse(
    resp => resp.url().includes('/auth/login') && resp.status() >= 200
  );
  
  await page.getByRole('button', { name: /sign in/i }).click();
  
  const response = await responsePromise;
  if (!response.ok()) {
    throw new Error(`Login failed: ${response.status()}`);
  }
  
  await page.waitForURL(/\/dashboard/, { timeout: 15000 });
  await expect(page).toHaveURL(/\/dashboard/);
}

export async function logoutUser(page: Page) {
  // Click the avatar button to open the dropdown menu, then click "Log out"
  const logoutBtn = page.getByRole('button', { name: /log out/i });
  if (await logoutBtn.isVisible({ timeout: 2000 }).catch(() => false)) {
    await logoutBtn.click();
    await page.waitForURL(/\/login/, { timeout: 10000 });
    return;
  }
  // Try clicking the user menu trigger (the last button in the header area, typically the avatar)
  const headerButtons = page.locator('header button');
  const count = await headerButtons.count();
  if (count > 0) {
    await headerButtons.nth(count - 1).click();
    await page.waitForTimeout(300);
    // Now click "Log out" menu item
    const menuLogout = page.getByText('Log out');
    if (await menuLogout.isVisible({ timeout: 2000 }).catch(() => false)) {
      await menuLogout.click();
      await page.waitForURL(/\/login/, { timeout: 10000 });
      return;
    }
  }
  // Fallback: clear auth state and navigate
  await page.evaluate(() => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    document.cookie = 'accessToken=; path=/; max-age=0';
  });
  await page.goto('/login');
  await page.waitForURL(/\/login/, { timeout: 10000 });
}
