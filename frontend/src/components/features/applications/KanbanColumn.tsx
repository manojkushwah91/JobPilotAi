'use client';

import type { ApplicationStatus } from '@/types';
import { cn } from '@/lib/utils/cn';
import { ApplicationCard } from './ApplicationCard';

interface Application {
  id: string;
  jobListingId: string;
  status: ApplicationStatus;
  statusHistory: { from: string; to: string; changedBy: string; timestamp: string }[];
  notes: { id: string; content: string; category: string; createdAt: string }[];
  appliedAt: string;
  updatedAt: string;
}

interface KanbanColumnProps {
  status: ApplicationStatus;
  applications: Application[];
  onStatusChange?: (id: string, newStatus: ApplicationStatus) => void;
  getJobTitle?: (id: string) => string;
  getCompanyName?: (id: string) => string;
}

const STATUS_LABELS: Record<ApplicationStatus, string> = {
  SAVED: 'Saved',
  APPLIED: 'Applied',
  PHONE_SCREEN: 'Phone Screen',
  TECHNICAL_INTERVIEW: 'Technical',
  ONSITE_INTERVIEW: 'Onsite',
  OFFER: 'Offer',
  ACCEPTED: 'Accepted',
  REJECTED: 'Rejected',
  WITHDRAWN: 'Withdrawn',
};

export function KanbanColumn({ status, applications, onStatusChange, getJobTitle, getCompanyName }: KanbanColumnProps) {
  const isEmpty = applications.length === 0;

  return (
    <div className="flex h-full w-64 shrink-0 flex-col">
      <div className="mb-3 flex items-center justify-between px-1">
        <div className="flex items-center gap-2">
          <h3 className="text-sm font-medium">{STATUS_LABELS[status]}</h3>
          <span className="flex h-5 min-w-5 items-center justify-center rounded-full bg-muted px-1.5 text-xs text-muted-foreground">
            {applications.length}
          </span>
        </div>
      </div>
      <div className={cn('flex-1 space-y-2 rounded-lg border-2 border-dashed p-2', isEmpty ? 'border-muted' : 'border-transparent')}>
        {isEmpty ? (
          <div className="flex h-20 items-center justify-center">
            <p className="text-xs text-muted-foreground">Drop cards here</p>
          </div>
        ) : (
          applications.map((app) => (
            <div key={app.id} className="relative group">
              <ApplicationCard
                application={app}
                jobTitle={getJobTitle?.(app.jobListingId)}
                companyName={getCompanyName?.(app.jobListingId)}
              />
              {onStatusChange && (
                <select
                  value={app.status}
                  onChange={(e) => onStatusChange(app.id, e.target.value as ApplicationStatus)}
                  className="absolute -right-1 -top-1 h-5 w-5 cursor-pointer rounded-full border bg-background text-[8px] opacity-0 group-hover:opacity-100 transition-opacity"
                  title="Change status"
                >
                  {Object.entries(STATUS_LABELS).map(([key, label]) => (
                    <option key={key} value={key}>{label}</option>
                  ))}
                </select>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}
