'use client';

import { cn } from '@/lib/utils/cn';
import { Skeleton } from '@/components/ui/skeleton';
import { FileText, Send, Mic, Building2, Briefcase, type LucideIcon } from 'lucide-react';

interface Activity {
  id: string;
  type: 'resume' | 'application' | 'interview' | 'company' | 'job';
  title: string;
  description: string;
  timestamp: string;
}

interface RecentActivityProps {
  activities?: Activity[];
  loading?: boolean;
}

const activityIcons: Record<Activity['type'], LucideIcon> = {
  resume: FileText,
  application: Send,
  interview: Mic,
  company: Building2,
  job: Briefcase,
};

const activityColors: Record<Activity['type'], string> = {
  resume: 'bg-blue-500/10 text-blue-500',
  application: 'bg-green-500/10 text-green-500',
  interview: 'bg-purple-500/10 text-purple-500',
  company: 'bg-orange-500/10 text-orange-500',
  job: 'bg-cyan-500/10 text-cyan-500',
};

export default function RecentActivity({ activities, loading }: RecentActivityProps) {
  const items = activities ?? [];

  if (loading) {
    return (
      <div className="space-y-4">
        {Array.from({ length: 5 }).map((_, i) => (
          <div key={i} className="flex items-start gap-3">
            <Skeleton className="h-10 w-10 rounded-lg" />
            <div className="flex-1 space-y-2">
              <Skeleton className="h-4 w-40" />
              <Skeleton className="h-3 w-56" />
              <Skeleton className="h-3 w-20" />
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-8 text-center">
        <Briefcase className="h-12 w-12 text-muted-foreground/50" />
        <p className="mt-2 text-sm font-medium text-muted-foreground">No recent activity</p>
        <p className="text-xs text-muted-foreground/60">Your recent actions will appear here</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {items.map((activity) => {
        const Icon = activityIcons[activity.type];
        return (
          <div key={activity.id} className="flex items-start gap-3">
            <div className={cn('flex h-10 w-10 items-center justify-center rounded-lg', activityColors[activity.type])}>
              <Icon className="h-5 w-5" />
            </div>
            <div className="flex-1 space-y-1">
              <p className="text-sm font-medium leading-none">{activity.title}</p>
              <p className="text-sm text-muted-foreground">{activity.description}</p>
              <p className="text-xs text-muted-foreground/60">{activity.timestamp}</p>
            </div>
          </div>
        );
      })}
    </div>
  );
}
