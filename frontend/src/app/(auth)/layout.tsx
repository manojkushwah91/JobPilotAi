import { AuthProvider } from '@/lib/auth/AuthProvider';
import { QueryClientProvider } from '@/lib/providers/QueryClientProvider';
import { Toaster } from '@/components/ui/toast';
import Link from 'next/link';
import type { ReactNode } from 'react';

export default function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <QueryClientProvider>
      <AuthProvider>
        <div className="relative flex min-h-screen overflow-hidden">
          <div className="absolute inset-0 bg-gradient-hero" />
          <div className="absolute inset-0 bg-gradient-mesh opacity-60" />
          <div className="absolute -left-40 -top-40 h-80 w-80 rounded-full bg-primary/10 blur-3xl" />
          <div className="absolute -right-40 top-1/3 h-96 w-96 rounded-full bg-violet-500/10 blur-3xl" />
          <div className="absolute -bottom-40 left-1/3 h-80 w-80 rounded-full bg-amber-500/5 blur-3xl" />

          <div className="relative z-10 flex w-full flex-col">
            <div className="flex h-16 items-center px-6">
              <Link href="/" className="flex items-center gap-2.5 group">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-primary shadow-glow transition-shadow group-hover:shadow-glow-lg">
                  <svg className="h-4 w-4 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
                  </svg>
                </div>
                <span className="text-sm font-bold tracking-tight">JobPilot <span className="text-primary">AI</span></span>
              </Link>
            </div>
            <div className="flex flex-1 items-center justify-center px-4 py-12">
              <div className="glass-strong w-full max-w-md rounded-2xl border-border/50 p-8 shadow-glow">
                {children}
              </div>
            </div>
          </div>
        </div>
        <Toaster />
      </AuthProvider>
    </QueryClientProvider>
  );
}
