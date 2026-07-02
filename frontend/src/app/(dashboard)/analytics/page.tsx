'use client';

import { useState } from 'react';
import { useApiQuery } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { StatCard } from '@/components/features/analytics/StatCard';
import { FunnelChart } from '@/components/features/analytics/FunnelChart';
import { TrendChart } from '@/components/features/analytics/TrendChart';
import { PieChart } from '@/components/features/analytics/PieChart';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Briefcase, Users, BarChart3, Cpu, TrendingUp, RefreshCw } from 'lucide-react';
import type { AnalyticsOverview } from '@/types';

type DateRange = '7d' | '30d' | '90d';

export default function AnalyticsPage() {
  const [dateRange, setDateRange] = useState<DateRange>('30d');

  const { data: overviewRes, isLoading, isError, refetch } = useApiQuery<AnalyticsOverview>(
    ['analytics', 'overview', dateRange],
    API.analytics.overview,
    { period: dateRange }
  );

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div>
          <Skeleton className="h-8 w-48" />
          <Skeleton className="mt-1 h-4 w-64" />
        </div>
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
        <div className="grid gap-6 md:grid-cols-2">
          <Skeleton className="h-80" />
          <Skeleton className="h-80" />
        </div>
        <div className="grid gap-6 md:grid-cols-2">
          <Skeleton className="h-80" />
          <Skeleton className="h-80" />
        </div>
        <Skeleton className="h-64" />
      </div>
    );
  }

  if (isError) {
    return (
      <Card>
        <CardContent className="flex flex-col items-center gap-4 py-12">
          <BarChart3 className="h-12 w-12 text-destructive" />
          <p className="text-destructive">Failed to load analytics</p>
          <Button variant="outline" onClick={() => refetch()} className="gap-2">
            <RefreshCw className="h-4 w-4" />
            Retry
          </Button>
        </CardContent>
      </Card>
    );
  }

  const overview = overviewRes!.data;

  const isEmpty =
    overview.totalApplications === 0 &&
    overview.totalInterviews === 0 &&
    overview.jobOffers === 0 &&
    overview.aiCalls === 0;

  if (isEmpty) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Analytics</h1>
          <p className="text-muted-foreground">Track your job search performance</p>
        </div>
        <Card>
          <CardContent className="flex flex-col items-center gap-4 py-12">
            <TrendingUp className="h-12 w-12 text-muted-foreground" />
            <div className="text-center">
              <p className="text-lg font-medium">Start using JobPilot to see your analytics</p>
              <p className="text-sm text-muted-foreground">Your job search statistics and insights will appear here</p>
            </div>
            <Button variant="outline" onClick={() => refetch()}>Refresh</Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const aiUsageData = Object.entries(overview.aiUsageByUseCase ?? {}).map(([name, value]) => ({ name, value }));

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Analytics</h1>
          <p className="text-muted-foreground">Track your job search performance</p>
        </div>
        <Tabs value={dateRange} onValueChange={(v) => setDateRange(v as DateRange)}>
          <TabsList>
            <TabsTrigger value="7d">Last 7 days</TabsTrigger>
            <TabsTrigger value="30d">Last 30 days</TabsTrigger>
            <TabsTrigger value="90d">Last 90 days</TabsTrigger>
          </TabsList>
        </Tabs>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          label="Total Applications"
          value={overview.totalApplications}
          icon={<Briefcase className="h-5 w-5" />}
        />
        <StatCard
          label="Active Users"
          value={overview.activeUsers}
          icon={<Users className="h-5 w-5" />}
        />
        <StatCard
          label="Jobs Tracked"
          value={overview.jobsTracked}
          icon={<BarChart3 className="h-5 w-5" />}
        />
        <StatCard
          label="AI Calls"
          value={overview.aiCalls}
          icon={<Cpu className="h-5 w-5" />}
        />
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <FunnelChart data={overview.applicationsByStatus} title="Application Funnel" />
        <TrendChart data={overview.applicationsOverTime} title="Applications Over Time" />
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <PieChart data={overview.jobsBySource} title="Jobs by Source" />
        {aiUsageData.length > 0 && (
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">AI Usage Breakdown</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={aiUsageData} margin={{ top: 10, right: 30, left: 0, bottom: 20 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                    <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                    <YAxis tick={{ fontSize: 12 }} />
                    <Tooltip
                      contentStyle={{ background: 'hsl(var(--popover))', border: '1px solid hsl(var(--border))', borderRadius: 'var(--radius)' }}
                    />
                    <Bar dataKey="value" fill="hsl(var(--primary))" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </CardContent>
          </Card>
        )}
      </div>

      {overview.monthlyTrends.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Monthly Trends</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Month</TableHead>
                  <TableHead>Applications</TableHead>
                  <TableHead>Interviews</TableHead>
                  <TableHead>AI Calls</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {overview.monthlyTrends.map((row) => (
                  <TableRow key={row.month}>
                    <TableCell className="font-medium">{row.month}</TableCell>
                    <TableCell>{row.applications}</TableCell>
                    <TableCell>{row.interviews}</TableCell>
                    <TableCell>{row.aiCalls}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      {overview.skillGaps && overview.skillGaps.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Skill Gap Analysis</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart
                  data={overview.skillGaps.map((s) => ({ name: s.skill, demand: s.demandLevel === 'HIGH' ? 100 : s.demandLevel === 'MEDIUM' ? 60 : 30, proficiency: s.proficiency }))}
                  margin={{ top: 10, right: 30, left: 0, bottom: 20 }}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                  <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 12 }} />
                  <Tooltip
                    contentStyle={{ background: 'hsl(var(--popover))', border: '1px solid hsl(var(--border))', borderRadius: 'var(--radius)' }}
                  />
                  <Bar dataKey="demand" name="Market Demand" fill="hsl(var(--primary))" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="proficiency" name="Your Proficiency" fill="hsl(var(--success))" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
