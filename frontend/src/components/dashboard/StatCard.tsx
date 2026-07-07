'use client';

import { useEffect, useState } from 'react';
import { cn } from '@/lib/utils/cn';
import { TrendingUp, TrendingDown, type LucideIcon } from 'lucide-react';

interface StatCardProps {
  icon: LucideIcon;
  label: string;
  value: string | number;
  trend?: 'up' | 'down';
  trendValue?: string;
  loading?: boolean;
  delay?: number;
}

export default function StatCard({ icon: Icon, label, value, trend, trendValue, loading, delay = 0 }: StatCardProps) {
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => setMounted(true), delay);
    return () => clearTimeout(timer);
  }, [delay]);

  if (loading) {
    return (
      <div className="card-premium p-6">
        <div className="animate-pulse space-y-3">
          <div className="h-10 w-10 rounded-xl bg-muted" />
          <div className="h-4 w-24 rounded bg-muted" />
          <div className="h-8 w-16 rounded bg-muted" />
        </div>
      </div>
    );
  }

  return (
    <div
      className={cn(
        'card-premium p-6 transition-all duration-500',
        mounted ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-2'
      )}
    >
      <div className="flex items-center justify-between mb-4">
        <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-primary/10 transition-colors group-hover:bg-primary/20">
          <Icon className="h-5 w-5 text-primary" />
        </div>
        {trend && (
          <div
            className={cn(
              'flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium',
              trend === 'up'
                ? 'bg-success/10 text-success'
                : 'bg-destructive/10 text-destructive'
            )}
          >
            {trend === 'up' ? (
              <TrendingUp className="h-3 w-3" />
            ) : (
              <TrendingDown className="h-3 w-3" />
            )}
            {trendValue}
          </div>
        )}
      </div>
      <div>
        <p className="text-sm text-muted-foreground mb-1">{label}</p>
        <p className="text-3xl font-bold tracking-tight">{value}</p>
      </div>
    </div>
  );
}
