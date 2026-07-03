'use client';

import { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Suspense } from 'react';

function OAuthCallbackInner() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const provider = searchParams.get('provider');
    const code = searchParams.get('code');
    if (!provider || !code) {
      setError('Invalid OAuth response');
      return;
    }

    const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';
    fetch(`${apiUrl}/auth/oauth/${provider}/callback?code=${encodeURIComponent(code)}&redirectUri=${encodeURIComponent(window.location.origin + '/auth/callback?provider=' + provider)}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
    })
      .then(res => res.json())
      .then(data => {
        if (data.data) {
          localStorage.setItem('accessToken', data.data.accessToken);
          localStorage.setItem('refreshToken', data.data.refreshToken);
          router.push('/dashboard');
        } else {
          setError(data.message || 'Authentication failed');
        }
      })
      .catch(() => setError('Network error during OAuth callback'));
  }, [searchParams, router]);

  if (error) {
    return <div className="flex items-center justify-center min-h-screen"><div className="text-red-500">{error}</div></div>;
  }

  return <div className="flex items-center justify-center min-h-screen"><div className="animate-spin h-8 w-8 border-4 border-primary border-t-transparent rounded-full" /></div>;
}

export default function OAuthCallbackPage() {
  return <Suspense fallback={<div className="flex items-center justify-center min-h-screen"><div className="animate-spin h-8 w-8 border-4 border-primary border-t-transparent rounded-full" /></div>}>
    <OAuthCallbackInner />
  </Suspense>;
}
