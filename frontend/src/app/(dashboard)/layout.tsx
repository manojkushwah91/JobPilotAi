'use client';

import { useState } from 'react';
import { AuthProvider } from '@/lib/auth/AuthProvider';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Sidebar from '@/components/dashboard/Sidebar';
import TopBar from '@/components/dashboard/TopBar';
import { Toaster } from '@/components/ui/toast';
import { CommandPalette } from '@/components/layout/CommandPalette';

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [queryClient] = useState(() => new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 60_000,
        retry: 1,
        refetchOnWindowFocus: false,
      },
    },
  }));

  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <div className="flex min-h-screen bg-background">
          <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
          <div className="flex flex-1 flex-col">
            <TopBar onMenuClick={() => setSidebarOpen(true)} />
            <main className="flex-1 p-4 lg:p-6 scrollbar-premium">
              {children}
            </main>
          </div>
        </div>
        <CommandPalette />
        <Toaster />
      </AuthProvider>
    </QueryClientProvider>
  );
}
