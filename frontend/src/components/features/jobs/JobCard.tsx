import Link from 'next/link';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import type { JobListing } from '@/types';
import { cn } from '@/lib/utils/cn';
import { Bookmark, MapPin, Briefcase, Clock } from 'lucide-react';

function formatSalary(salary: Record<string, unknown>) {
  if (typeof salary.min === 'number' && typeof salary.max === 'number' && typeof salary.currency === 'string') {
    const fmt = new Intl.NumberFormat('en-US', { style: 'currency', currency: salary.currency, maximumFractionDigits: 0 });
    return `${fmt.format(salary.min)} - ${fmt.format(salary.max)}`;
  }
  if (typeof salary.text === 'string') return salary.text;
  return '';

}
function formatLocation(location: Record<string, unknown>) {
  if (typeof location.text === 'string') return location.text;
  const parts = [location.city, location.state, location.country].filter((p): p is string => typeof p === 'string');
  return parts.join(', ') || 'Remote';
}

function timeAgo(dateStr: string) {
  const diff = Date.now() - new Date(dateStr).getTime();
  const days = Math.floor(diff / 86400000);
  if (days === 0) return 'Today';
  if (days === 1) return 'Yesterday';
  return `${days}d ago`;
}

function getMatchColor(score: number) {
  if (score >= 80) return 'text-success';
  if (score >= 60) return 'text-warning';
  return 'text-muted-foreground';
}

interface JobCardProps {
  job: JobListing;
  onSave?: (id: string) => void;
  isSaved?: boolean;
}

export function JobCard({ job, onSave, isSaved }: JobCardProps) {
  const initials = job.companyName?.charAt(0)?.toUpperCase() ?? '?';

  return (
    <Link href={`/jobs/${job.id}`}>
      <Card className="h-full cursor-pointer transition-shadow hover:shadow-md">
        <CardHeader className="pb-3">
          <div className="flex items-start gap-3">
            <Avatar className="h-10 w-10 rounded-lg">
              {job.companyLogoUrl ? <AvatarImage src={job.companyLogoUrl} alt={job.companyName} /> : null}
              <AvatarFallback className="rounded-lg bg-primary/10 text-sm font-medium text-primary">{initials}</AvatarFallback>
            </Avatar>
            <div className="flex-1 min-w-0">
              <CardTitle className="text-base leading-tight">{job.title}</CardTitle>
              <p className="text-sm text-muted-foreground">{job.companyName}</p>
            </div>
            {job.matchScore != null && (
              <div className="flex shrink-0 flex-col items-center">
                <div className={cn('text-lg font-bold', getMatchColor(job.matchScore))}>{job.matchScore}%</div>
                <span className="text-[10px] text-muted-foreground">match</span>
              </div>
            )}
          </div>
        </CardHeader>
        <CardContent className="pb-2">
          <div className="mb-2 flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
            <span className="flex items-center gap-1">
              <MapPin className="h-3 w-3" />
              {job.location ? formatLocation(job.location) : 'Remote'}
            </span>
            <span className="flex items-center gap-1">
              <Briefcase className="h-3 w-3" />
              {job.employmentType?.replace('_', ' ')}
            </span>
            <span className="flex items-center gap-1">
              <Clock className="h-3 w-3" />
              {timeAgo(job.postedAt)}
            </span>
          </div>
          {job.salary && (
            <p className="mb-2 text-sm font-medium text-primary">{formatSalary(job.salary)}</p>
          )}
          {job.skills && job.skills.length > 0 && (
            <div className="flex flex-wrap gap-1">
              {job.skills.slice(0, 4).map((skill) => (
                <Badge key={skill} variant="secondary" className="text-[10px]">{skill}</Badge>
              ))}
              {job.skills.length > 4 && (
                <Badge variant="outline" className="text-[10px]">+{job.skills.length - 4}</Badge>
              )}
            </div>
          )}
        </CardContent>
        <CardFooter className="pt-1">
          <div className="flex w-full items-center justify-between">
            <span className="text-xs text-muted-foreground">{timeAgo(job.postedAt)}</span>
            {onSave && (
              <Button
                variant="ghost"
                size="icon"
                className="h-7 w-7"
                onClick={(e) => { e.preventDefault(); e.stopPropagation(); onSave(job.id); }}
              >
                <Bookmark className={cn('h-4 w-4', isSaved && 'fill-primary text-primary')} />
              </Button>
            )}
          </div>
        </CardFooter>
      </Card>
    </Link>
  );
}
