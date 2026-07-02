'use client';

import { Card, CardContent } from '@/components/ui/card';
import { cn } from '@/lib/utils/cn';
import { TrendingUp, TrendingDown } from 'lucide-react';
import type { ReactNode } from 'react';

interface StatCardProps {
  label: string;
  value: string | number;
  icon: ReactNode;
  trend?: { value: number; positive: boolean };
}

export function StatCard({ label, value, icon, trend }: StatCardProps) {
  return (
    <Card>
      <CardContent className="p-6">
        <div className="flex items-center justify-between">
          <div className="space-y-1">
            <p className="text-sm font-medium text-muted-foreground">{label}</p>
            <p className="text-3xl font-bold">{value}</p>
            {trend && (
              <div className={cn('flex items-center gap-1 text-sm', trend.positive ? 'text-success' : 'text-destructive')}>
                {trend.positive ? <TrendingUp className="h-4 w-4" /> : <TrendingDown className="h-4 w-4" />}
                <span>{trend.value}%</span>
              </div>
            )}
          </div>
          <div className="rounded-lg bg-primary/10 p-3 text-primary">
            {icon}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
