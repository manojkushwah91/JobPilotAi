'use client';

import { useRouter } from 'next/navigation';
import { useApiQuery } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Users, Flag, ScrollText, BarChart3, RefreshCw } from 'lucide-react';
import type { AdminMetrics } from '@/types';

export default function AdminPage() {
  const router = useRouter();

  const { data: res, isLoading, isError, refetch } = useApiQuery<AdminMetrics>(
    ['admin', 'metrics'],
    API.admin.metrics
  );

  const quickLinks = [
    { label: 'User Management', icon: Users, href: '/admin/users', desc: 'Manage users, roles, and permissions' },
    { label: 'Feature Flags', icon: Flag, href: '/admin/feature-flags', desc: 'Toggle feature flags on/off' },
    { label: 'Audit Logs', icon: ScrollText, href: '/admin/audit-logs', desc: 'View system audit trail' },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Admin Dashboard</h1>
        <p className="text-muted-foreground">System administration and management</p>
      </div>

      {isLoading ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <Card key={i}>
              <CardContent className="p-6">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="mt-2 h-8 w-16" />
              </CardContent>
            </Card>
          ))}
        </div>
      ) : isError ? (
        <Card>
          <CardContent className="flex flex-col items-center gap-4 py-12">
            <p className="text-destructive">Failed to load admin metrics</p>
            <Button variant="outline" onClick={() => refetch()} className="gap-2">
              <RefreshCw className="h-4 w-4" />
              Retry
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Total Users</p>
                  <p className="text-3xl font-bold">{res!.data.totalUsers}</p>
                </div>
                <Users className="h-8 w-8 text-primary" />
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Active Subscriptions</p>
                  <p className="text-3xl font-bold">{res!.data.activeSubscriptions}</p>
                </div>
                <BarChart3 className="h-8 w-8 text-primary" />
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">API Usage (month)</p>
                  <p className="text-3xl font-bold">{res!.data.monthlyApiUsage.toLocaleString()}</p>
                </div>
                <BarChart3 className="h-8 w-8 text-primary" />
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">New Users (month)</p>
                  <p className="text-3xl font-bold">{res!.data.newUsersThisMonth}</p>
                </div>
                <Users className="h-8 w-8 text-primary" />
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      <div>
        <h2 className="mb-4 text-xl font-semibold">Quick Links</h2>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {quickLinks.map((link) => (
            <Card
              key={link.href}
              className="cursor-pointer transition-all hover:shadow-md"
              onClick={() => router.push(link.href)}
            >
              <CardHeader>
                <CardTitle className="flex items-center gap-2 text-lg">
                  <link.icon className="h-5 w-5 text-primary" />
                  {link.label}
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">{link.desc}</p>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
}
