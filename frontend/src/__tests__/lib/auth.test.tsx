import { renderHook, act } from '@testing-library/react';
import { useAuthStore } from '@/lib/auth/AuthProvider';

describe('AuthStore', () => {
  beforeEach(() => {
    const { result } = renderHook(() => useAuthStore());
    act(() => { result.current.setUser(null); });
  });

  it('starts with no user', () => {
    const { result } = renderHook(() => useAuthStore());
    expect(result.current.user).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
  });

  it('sets user on login', () => {
    const { result } = renderHook(() => useAuthStore());
    const user = { id: '1', email: 'test@test.com', name: 'Test', role: 'FREE' as const, tier: 'FREE' as const, createdAt: new Date().toISOString() };
    act(() => { result.current.setUser(user); });
    expect(result.current.user).toEqual(user);
    expect(result.current.isAuthenticated).toBe(true);
  });
});
