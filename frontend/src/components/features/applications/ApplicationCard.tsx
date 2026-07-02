import type { Application } from '@/types';
import { StatusBadge, STATUS_CONFIG } from './StatusBadge';
import { Card, CardContent } from '@/components/ui/card';
import { cn } from '@/lib/utils/cn';
import { Clock } from 'lucide-react';

interface ApplicationCardProps {
  application: Application;
  jobTitle?: string;
  companyName?: string;
}

function daysSince(dateStr: string) {
  const diff = Date.now() - new Date(dateStr).getTime();
  return Math.floor(diff / 86400000);
}

export function ApplicationCard({ application, jobTitle, companyName }: ApplicationCardProps) {
  const days = daysSince(application.appliedAt);
  const config = STATUS_CONFIG[application.status];

  return (
    <Card className={cn('cursor-pointer transition-shadow hover:shadow-md', config.color.replace('text-', 'border-l-4 border-l-'))}>
      <CardContent className="p-3">
        <div className="mb-2">
          <StatusBadge status={application.status} className="mb-2" />
          <h4 className="text-sm font-medium leading-tight">{jobTitle ?? 'Untitled Position'}</h4>
          <p className="text-xs text-muted-foreground">{companyName ?? 'Unknown Company'}</p>
        </div>
        <div className="flex items-center gap-1 text-xs text-muted-foreground">
          <Clock className="h-3 w-3" />
          <span>{days}d ago</span>
          <span className="mx-1">·</span>
          <span>Updated {daysSince(application.updatedAt)}d ago</span>
        </div>
      </CardContent>
    </Card>
  );
}
