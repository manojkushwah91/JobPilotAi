'use client';

import type { ApplicationStatus, StatusChange } from '@/types';
import { StatusBadge } from './StatusBadge';
import { cn } from '@/lib/utils/cn';

interface StatusTimelineProps {
  history: StatusChange[];
}

export function StatusTimeline({ history }: StatusTimelineProps) {
  if (!history || history.length === 0) {
    return (
      <div className="py-8 text-center text-sm text-muted-foreground">
        No status changes recorded yet
      </div>
    );
  }

  const sorted = [...history].sort(
    (a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
  );

  return (
    <div className="relative space-y-0">
      {sorted.map((change, idx) => {
        const isLast = idx === sorted.length - 1;

        return (
          <div key={idx} className="relative flex gap-4 pb-6">
            <div className="flex flex-col items-center">
              <div className={cn(
                'z-10 flex h-3 w-3 shrink-0 rounded-full border-2',
                idx === 0 ? 'border-primary bg-primary' : 'border-muted-foreground bg-background'
              )} />
              {!isLast && <div className="mt-0 h-full w-px bg-border" />}
            </div>
            <div className="flex-1 pb-2">
              <div className="flex items-center gap-2">
                <StatusBadge status={change.to as ApplicationStatus} className="text-[10px]" />
                {change.from && (
                  <span className="text-xs text-muted-foreground">
                    from <StatusBadge status={change.from as ApplicationStatus} className="text-[10px]" />
                  </span>
                )}
              </div>
              <p className="mt-1 text-xs text-muted-foreground">
                {new Date(change.timestamp).toLocaleString()}
                {change.changedBy && ` · by ${change.changedBy}`}
              </p>
            </div>
          </div>
        );
      })}
    </div>
  );
}
