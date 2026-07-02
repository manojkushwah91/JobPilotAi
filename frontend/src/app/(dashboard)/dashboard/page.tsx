'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useAuth } from '@/lib/auth/AuthProvider';
import { useApiQuery } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import StatCard from '@/components/dashboard/StatCard';
import RecentActivity from '@/components/dashboard/RecentActivity';
import {
  FileText,
  Search,
  Mic,
  Send,
  FileCheck,
  TrendingUp,
  Users,
  Briefcase,
} from 'lucide-react';
import type { ApplicationFunnel } from '@/types';

export default function DashboardPage() {
  const { user } = useAuth();
  const [greeting] = useState(() => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  });

  const { data: analyticsData, isLoading: analyticsLoading } = useApiQuery<ApplicationFunnel>(
    ['analytics', 'funnel'],
    API.analytics.applicationFunnel,
    undefined,
    { retry: false }
  );

  const { data: activityData, isLoading: activityLoading } = useApiQuery<{ activities: unknown[] }>(
    ['analytics', 'activity'],
    API.analytics.activity,
    undefined,
    { retry: false }
  );

  const stats = {
    applications: analyticsData?.data?.byStatus
      ? Object.values(analyticsData.data.byStatus).reduce((a, b) => a + b, 0)
      : 12,
    interviews: analyticsData?.data?.byStatus?.['TECHNICAL_INTERVIEW'] ?? 3,
    resumeScore: 85,
    jobMatches: 24,
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          {greeting}, {user?.name?.split(' ')[0] ?? 'there'}
        </h1>
        <p className="text-muted-foreground">Here&apos;s your career overview today.</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          icon={Send}
          label="Applications"
          value={stats.applications}
          trend="up"
          trendValue="+12%"
          loading={analyticsLoading}
        />
        <StatCard
          icon={Mic}
          label="Interviews"
          value={stats.interviews}
          trend="up"
          trendValue="+2"
          loading={analyticsLoading}
        />
        <StatCard
          icon={FileCheck}
          label="Resume Score"
          value={`${stats.resumeScore}/100`}
          trend={stats.resumeScore >= 80 ? 'up' : 'down'}
          trendValue={stats.resumeScore >= 80 ? 'Good' : 'Needs work'}
        />
        <StatCard
          icon={Briefcase}
          label="Job Matches"
          value={stats.jobMatches}
          trend="up"
          trendValue="+8 new"
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Recent Activity</CardTitle>
          </CardHeader>
          <CardContent>
            <RecentActivity
              activities={(activityData?.data as { activities?: unknown[] })?.activities as any[] | undefined}
              loading={activityLoading}
            />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Quick Actions</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <Link href="/resumes">
              <Button variant="outline" className="w-full justify-start gap-3 h-12">
                <FileText className="h-5 w-5 text-primary" />
                <div className="text-left">
                  <p className="text-sm font-medium">Create Resume</p>
                  <p className="text-xs text-muted-foreground">Build an ATS-optimized resume</p>
                </div>
              </Button>
            </Link>
            <Link href="/jobs">
              <Button variant="outline" className="w-full justify-start gap-3 h-12">
                <Search className="h-5 w-5 text-primary" />
                <div className="text-left">
                  <p className="text-sm font-medium">Search Jobs</p>
                  <p className="text-xs text-muted-foreground">Find your next opportunity</p>
                </div>
              </Button>
            </Link>
            <Link href="/interviews">
              <Button variant="outline" className="w-full justify-start gap-3 h-12">
                <Mic className="h-5 w-5 text-primary" />
                <div className="text-left">
                  <p className="text-sm font-medium">Practice Interview</p>
                  <p className="text-xs text-muted-foreground">AI-powered interview practice</p>
                </div>
              </Button>
            </Link>
            <Link href="/analytics">
              <Button variant="outline" className="w-full justify-start gap-3 h-12">
                <TrendingUp className="h-5 w-5 text-primary" />
                <div className="text-left">
                  <p className="text-sm font-medium">View Analytics</p>
                  <p className="text-xs text-muted-foreground">Track your career metrics</p>
                </div>
              </Button>
            </Link>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Skill Insights</CardTitle>
        </CardHeader>
        <CardContent>
          {analyticsLoading ? (
            <div className="space-y-3">
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-3/4" />
              <Skeleton className="h-4 w-5/6" />
            </div>
          ) : (
            <div className="flex flex-wrap gap-3">
              <span className="inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold text-success border-success/30 bg-success/10">
                React
              </span>
              <span className="inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold text-success border-success/30 bg-success/10">
                TypeScript
              </span>
              <span className="inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold text-success border-success/30 bg-success/10">
                Node.js
              </span>
              <span className="inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold text-warning border-warning/30 bg-warning/10">
                Python
              </span>
              <span className="inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold text-warning border-warning/30 bg-warning/10">
                AWS
              </span>
              <span className="inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold text-destructive border-destructive/30 bg-destructive/10">
                Docker
              </span>
              <span className="inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold text-destructive border-destructive/30 bg-destructive/10">
                Kubernetes
              </span>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
