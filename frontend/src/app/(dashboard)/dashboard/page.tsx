'use client';

import { useState, useEffect } from 'react';
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
  Briefcase,
  Sparkles,
  Zap,
  ArrowRight,
  Bot,
  CircleDot,
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
    <div className="space-y-8">
      {/* Hero Header */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-mesh p-8">
        <div className="relative z-10">
          <div className="flex items-center gap-2 mb-2">
            <Sparkles className="h-5 w-5 text-primary animate-pulse" />
            <span className="text-sm font-medium text-primary">AI Agent Active</span>
          </div>
          <h1 className="text-4xl font-bold tracking-tight mb-2">
            {greeting},{' '}
            <span className="text-gradient">
              {user?.name?.split(' ')[0] ?? 'there'}
            </span>
          </h1>
          <p className="text-muted-foreground text-lg">
            Your AI agent is working on <span className="text-foreground font-medium">3 applications</span> today.
          </p>
        </div>
        <div className="absolute -right-20 -top-20 h-64 w-64 rounded-full bg-primary/5 blur-3xl" />
        <div className="absolute -left-10 -bottom-10 h-40 w-40 rounded-full bg-primary/10 blur-2xl" />
      </div>

      {/* Agent Status Bar */}
      <Card className="glass animate-fade-in">
        <CardContent className="flex items-center justify-between p-4">
          <div className="flex items-center gap-4">
            <div className="relative">
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
                <Bot className="h-5 w-5 text-primary" />
              </div>
              <span className="absolute -bottom-0.5 -right-0.5 h-3 w-3 rounded-full bg-success border-2 border-background">
                <span className="absolute inset-0 rounded-full bg-success animate-ping opacity-75" />
              </span>
            </div>
            <div>
              <p className="text-sm font-medium">Agent Runtime</p>
              <p className="text-xs text-muted-foreground">
                Scoring jobs from Indeed • 12 candidates analyzed
              </p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
              <CircleDot className="h-3 w-3 text-success" />
              <span>Running</span>
            </div>
            <Link href="/agent-chat">
              <Button size="sm" className="gap-2 bg-gradient-primary hover:opacity-90">
                <Zap className="h-3.5 w-3.5" />
                View Agent
                <ArrowRight className="h-3.5 w-3.5" />
              </Button>
            </Link>
          </div>
        </CardContent>
      </Card>

      {/* Stats Grid */}
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

      {/* Main Content Grid */}
      <div className="grid gap-6 lg:grid-cols-3">
        {/* Activity Feed */}
        <Card className="lg:col-span-2 card-premium">
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="text-lg">Recent Activity</CardTitle>
            <Button variant="ghost" size="sm" className="text-xs text-muted-foreground">
              View all
              <ArrowRight className="ml-1 h-3 w-3" />
            </Button>
          </CardHeader>
          <CardContent>
            <RecentActivity
              activities={(activityData?.data?.activities ?? []) as { id: string; type: 'resume' | 'application' | 'interview' | 'company' | 'job'; title: string; description: string; timestamp: string }[]}
              loading={activityLoading}
            />
          </CardContent>
        </Card>

        {/* Quick Actions */}
        <Card className="card-premium">
          <CardHeader>
            <CardTitle className="text-lg">Quick Actions</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            <Link href="/resumes">
              <Button variant="ghost" className="w-full justify-start gap-3 h-12 hover-lift">
                <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary/10">
                  <FileText className="h-4 w-4 text-primary" />
                </div>
                <div className="text-left">
                  <p className="text-sm font-medium">Upload Resume</p>
                  <p className="text-xs text-muted-foreground">Auto-parse with AI</p>
                </div>
              </Button>
            </Link>
            <Link href="/jobs">
              <Button variant="ghost" className="w-full justify-start gap-3 h-12 hover-lift">
                <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-success/10">
                  <Search className="h-4 w-4 text-success" />
                </div>
                <div className="text-left">
                  <p className="text-sm font-medium">Search Jobs</p>
                  <p className="text-xs text-muted-foreground">AI-scored matches</p>
                </div>
              </Button>
            </Link>
            <Link href="/agent-chat">
              <Button variant="ghost" className="w-full justify-start gap-3 h-12 hover-lift">
                <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-warning/10">
                  <Mic className="h-4 w-4 text-warning" />
                </div>
                <div className="text-left">
                  <p className="text-sm font-medium">Agent Chat</p>
                  <p className="text-xs text-muted-foreground">Talk to your AI agent</p>
                </div>
              </Button>
            </Link>
            <Link href="/applications">
              <Button variant="ghost" className="w-full justify-start gap-3 h-12 hover-lift">
                <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-info/10">
                  <TrendingUp className="h-4 w-4 text-info" />
                </div>
                <div className="text-left">
                  <p className="text-sm font-medium">Applications</p>
                  <p className="text-xs text-muted-foreground">Track all submissions</p>
                </div>
              </Button>
            </Link>
          </CardContent>
        </Card>
      </div>

      {/* Skill Insights */}
      <Card className="card-premium">
        <CardHeader>
          <CardTitle className="text-lg">Skill Insights</CardTitle>
        </CardHeader>
        <CardContent>
          {analyticsLoading ? (
            <div className="space-y-3">
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-3/4" />
            </div>
          ) : (
            <div className="flex flex-wrap gap-2">
              {['React', 'TypeScript', 'Node.js', 'Python', 'AWS', 'Docker', 'PostgreSQL', 'Spring Boot'].map(
                (skill, i) => (
                  <span
                    key={skill}
                    className={`inline-flex items-center rounded-full border px-3 py-1 text-xs font-medium transition-all hover:scale-105 ${
                      i < 4
                        ? 'text-success border-success/30 bg-success/10 hover:bg-success/20'
                        : i < 6
                        ? 'text-warning border-warning/30 bg-warning/10 hover:bg-warning/20'
                        : 'text-muted-foreground border-border bg-muted/50 hover:bg-muted'
                    }`}
                  >
                    {skill}
                  </span>
                )
              )}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
