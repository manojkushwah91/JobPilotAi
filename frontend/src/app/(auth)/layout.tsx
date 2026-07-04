import { AuthProvider } from '@/lib/auth/AuthProvider';
import { QueryClientProvider } from '@/lib/providers/QueryClientProvider';
import { Toaster } from '@/components/ui/toast';
import type { ReactNode } from 'react';

export default function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <QueryClientProvider>
      <AuthProvider>
        <div className="flex min-h-screen items-center justify-center bg-muted/30 px-4 py-12">
          {children}
        </div>
        <Toaster />
      </AuthProvider>
    </QueryClientProvider>
  );
}
