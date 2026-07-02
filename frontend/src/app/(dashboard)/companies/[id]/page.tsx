'use client';

import { useParams, useRouter } from 'next/navigation';
import { useApiQuery } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Separator } from '@/components/ui/separator';
import { CompanyHeader } from '@/components/features/companies/CompanyHeader';
import { SalaryChart } from '@/components/features/companies/SalaryChart';
import { ArrowLeft, DollarSign, TrendingUp, Building2, RefreshCw } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import type { Company } from '@/types';

export default function CompanyDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id as string;

  const { data: res, isLoading, isError, refetch } = useApiQuery<Company>(
    ['companies', 'detail', id],
    API.companies.detail(id)
  );

  const { data: techStackRes } = useApiQuery<string[]>(
    ['companies', 'techStack', id],
    API.companies.techStack(id)
  );

  const { data: hiringTrendsRes } = useApiQuery<{ date: string; count: number }[]>(
    ['companies', 'hiringTrends', id],
    API.companies.hiringTrends(id)
  );

  if (isLoading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-8 w-32" />
        <div className="flex items-start gap-6">
          <Skeleton className="h-20 w-20 rounded-xl" />
          <div className="flex-1 space-y-2">
            <Skeleton className="h-8 w-64" />
            <Skeleton className="h-4 w-48" />
            <div className="flex gap-2">
              <Skeleton className="h-6 w-20 rounded-full" />
              <Skeleton className="h-6 w-24 rounded-full" />
            </div>
          </div>
        </div>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {Array.from({ length: 3 }).map((_, i) => (
            <Card key={i}>
              <CardContent className="p-4">
                <Skeleton className="h-4 w-16" />
                <Skeleton className="mt-1 h-6 w-24" />
              </CardContent>
            </Card>
          ))}
        </div>
        <div className="grid gap-6 md:grid-cols-2">
          <Skeleton className="h-80" />
          <Skeleton className="h-80" />
        </div>
      </div>
    );
  }

  if (isError) {
    return (
      <Card>
        <CardContent className="flex flex-col items-center gap-4 py-12">
          <Building2 className="h-12 w-12 text-destructive" />
          <p className="text-destructive">Failed to load company details</p>
          <Button variant="outline" onClick={() => refetch()} className="gap-2">
            <RefreshCw className="h-4 w-4" />
            Retry
          </Button>
        </CardContent>
      </Card>
    );
  }

  const company = res!.data;
  const techStack = techStackRes?.data ?? company.techStack;
  const hiringTrends = hiringTrendsRes?.data ?? [];

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" onClick={() => router.push('/companies')} className="gap-2">
        <ArrowLeft className="h-4 w-4" />
        Back to Companies
      </Button>

      <CompanyHeader company={company} />

      <Separator />

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-lg">
            <Building2 className="h-5 w-5" />
            Technology Stack
          </CardTitle>
        </CardHeader>
        <CardContent>
          {techStack.length > 0 ? (
            <div className="flex flex-wrap gap-2">
              {techStack.map((tech) => (
                <Badge key={tech} variant="secondary">{tech}</Badge>
              ))}
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">No technology stack information available.</p>
          )}
        </CardContent>
      </Card>

      <div className="grid gap-6 md:grid-cols-2">
        <SalaryChart salaryData={company.salaryData} />

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-lg">
              <TrendingUp className="h-5 w-5" />
              Hiring Trends
            </CardTitle>
          </CardHeader>
          <CardContent>
            {hiringTrends.length > 0 ? (
              <div className="h-80">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={hiringTrends} margin={{ top: 10, right: 30, left: 0, bottom: 20 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                    <XAxis dataKey="date" tick={{ fontSize: 12 }} />
                    <YAxis tick={{ fontSize: 12 }} />
                    <Tooltip
                      contentStyle={{
                        background: 'hsl(var(--popover))',
                        border: '1px solid hsl(var(--border))',
                        borderRadius: 'var(--radius)',
                      }}
                    />
                    <Line type="monotone" dataKey="count" stroke="hsl(var(--primary))" strokeWidth={2} dot={{ r: 4 }} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            ) : (
              <p className="text-sm text-muted-foreground">No hiring trend data available.</p>
            )}
          </CardContent>
        </Card>
      </div>

      {company.fundingRounds.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-lg">
              <DollarSign className="h-5 w-5" />
              Funding Rounds
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {company.fundingRounds.map((round, i) => (
                <div key={i} className="flex items-center justify-between rounded-lg border p-4">
                  <div>
                    <p className="font-medium">{round.stage}</p>
                    <p className="text-sm text-muted-foreground">
                      {new Date(round.date).toLocaleDateString()} · {round.investors.join(', ')}
                    </p>
                  </div>
                  <p className="text-lg font-semibold">
                    ${(round.amount / 1_000_000).toFixed(1)}M
                  </p>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
