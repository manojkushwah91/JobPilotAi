import type { ApplicationStatus } from '@/types';
import { cn } from '@/lib/utils/cn';
import { Badge } from '@/components/ui/badge';

const STATUS_CONFIG: Record<ApplicationStatus, { label: string; variant: 'default' | 'secondary' | 'destructive' | 'outline' | 'success' | 'warning'; color: string }> = {
  SAVED: { label: 'Saved', variant: 'outline', color: 'bg-gray-100 text-gray-700 border-gray-200' },
  APPLIED: { label: 'Applied', variant: 'secondary', color: 'bg-blue-100 text-blue-700 border-blue-200' },
  PHONE_SCREEN: { label: 'Phone Screen', variant: 'default', color: 'bg-indigo-100 text-indigo-700 border-indigo-200' },
  TECHNICAL_INTERVIEW: { label: 'Technical', variant: 'default', color: 'bg-purple-100 text-purple-700 border-purple-200' },
  ONSITE_INTERVIEW: { label: 'Onsite', variant: 'default', color: 'bg-pink-100 text-pink-700 border-pink-200' },
  OFFER: { label: 'Offer', variant: 'success', color: 'bg-green-100 text-green-700 border-green-200' },
  ACCEPTED: { label: 'Accepted', variant: 'success', color: 'bg-emerald-100 text-emerald-700 border-emerald-200' },
  REJECTED: { label: 'Rejected', variant: 'destructive', color: 'bg-red-100 text-red-700 border-red-200' },
  WITHDRAWN: { label: 'Withdrawn', variant: 'outline', color: 'bg-gray-100 text-gray-500 border-gray-200' },
};

interface StatusBadgeProps {
  status: ApplicationStatus;
  className?: string;
}

export function StatusBadge({ status, className }: StatusBadgeProps) {
  const config = STATUS_CONFIG[status];
  return (
    <Badge variant={config.variant} className={cn('text-xs', config.color, className)}>
      {config.label}
    </Badge>
  );
}

export { STATUS_CONFIG };
