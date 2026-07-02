'use client';

import { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import type { JobListing, JobListing as JobDetail } from '@/types';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { MatchScoreCard } from '@/components/features/jobs/MatchScoreCard';
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter,
} from '@/components/ui/dialog';
import { toast } from 'sonner';
import { ArrowLeft, MapPin, Briefcase, Clock, Building2, ExternalLink, Bookmark, Send, Sparkles } from 'lucide-react';

function formatSalary(salary: { min: number; max: number; currency: string }) {
  const fmt = new Intl.NumberFormat('en-US', { style: 'currency', currency: salary.currency, maximumFractionDigits: 0 });
  return `${fmt.format(salary.min)} - ${fmt.format(salary.max)}`;
}

function timeAgo(dateStr: string) {
  const diff = Date.now() - new Date(dateStr).getTime();
  const days = Math.floor(diff / 86400000);
  if (days === 0) return 'Today';
  if (days === 1) return 'Yesterday';
  return `${days}d ago`;
}

interface MatchDetail {
  matchScore: number;
  matchedSkills: string[];
  missingSkills: string[];
}

export default function JobDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id as string;

  const [applyOpen, setApplyOpen] = useState(false);
  const [coverLetter, setCoverLetter] = useState('');
  const [isSaved, setIsSaved] = useState(false);

  const { data, isLoading, isError } = useApiQuery<JobDetail>(['job', id], API.jobs.detail(id));

  const matchMutation = useApiMutation<MatchDetail, void>('POST', API.jobs.matchDetail(id), {
    onSuccess: () => toast.success('Match analysis complete'),
    onError: () => toast.error('Failed to analyze match'),
  });

  const saveMutation = useApiMutation<void, void>('POST', API.jobs.save(id), {
    onSuccess: () => {
      setIsSaved(true);
      toast.success('Job saved');
    },
    onError: () => toast.error('Failed to save job'),
  });

  const applyMutation = useApiMutation<void, { coverLetter?: string }>('POST', API.applications.create, {
    onSuccess: () => {
      toast.success('Application submitted');
      setApplyOpen(false);
    },
    onError: () => toast.error('Failed to submit application'),
  });

  const job = data?.data;

  if (isLoading) {
    return (
      <div className="p-6">
        <Skeleton className="mb-4 h-8 w-16" />
        <div className="flex gap-6">
          <div className="flex-1 space-y-4">
            <Skeleton className="h-8 w-96" />
            <Skeleton className="h-4 w-64" />
            <Skeleton className="h-32 w-full rounded-xl" />
            <Skeleton className="h-48 w-full rounded-xl" />
          </div>
          <Skeleton className="h-96 w-80 rounded-xl" />
        </div>
      </div>
    );
  }

  if (isError || !job) {
    return (
      <div className="flex flex-col items-center justify-center py-20">
        <p className="text-destructive">Failed to load job details</p>
        <Button variant="outline" className="mt-4" onClick={() => router.push('/jobs')}>
          Back to Jobs
        </Button>
      </div>
    );
  }

  return (
    <div className="p-6">
      <button
        onClick={() => router.back()}
        className="mb-6 flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to jobs
      </button>

      <div className="flex gap-6">
        <div className="flex-1 space-y-6">
          <div className="flex items-start gap-4">
            <Avatar className="h-14 w-14 rounded-xl">
              {job.companyLogoUrl ? <AvatarImage src={job.companyLogoUrl} alt={job.companyName} /> : null}
              <AvatarFallback className="rounded-xl bg-primary/10 text-lg font-medium text-primary">
                {job.companyName?.charAt(0)?.toUpperCase() ?? '?'}
              </AvatarFallback>
            </Avatar>
            <div className="flex-1">
              <h1 className="text-2xl font-bold">{job.title}</h1>
              <p className="text-lg text-muted-foreground">{job.companyName}</p>
              <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-sm text-muted-foreground">
                <span className="flex items-center gap-1"><MapPin className="h-4 w-4" />{job.location}</span>
                <span className="flex items-center gap-1"><Briefcase className="h-4 w-4" />{job.employmentType?.replace(/_/g, ' ')}</span>
                <span className="flex items-center gap-1"><Clock className="h-4 w-4" />{timeAgo(job.postedAt)}</span>
                {job.salary && <span className="flex items-center gap-1 font-medium text-primary">{formatSalary(job.salary)}</span>}
              </div>
            </div>
            <div className="flex shrink-0 gap-2">
              <Button variant="outline" size="sm" onClick={() => { if (!isSaved) saveMutation.mutate(); }}>
                <Bookmark className={isSaved ? 'mr-1.5 h-4 w-4 fill-primary text-primary' : 'mr-1.5 h-4 w-4'} />
                {isSaved ? 'Saved' : 'Save'}
              </Button>
              <Button size="sm" onClick={() => setApplyOpen(true)}>
                <Send className="mr-1.5 h-4 w-4" />
                Apply Now
              </Button>
            </div>
          </div>

          <Card>
            <CardHeader>
              <CardTitle className="text-base">Job Description</CardTitle>
            </CardHeader>
            <CardContent>
              {(job as any).description ? (
                <div className="prose prose-sm max-w-none text-sm text-muted-foreground" dangerouslySetInnerHTML={{ __html: (job as any).description }} />
              ) : (
                <p className="text-sm text-muted-foreground">No description available</p>
              )}
            </CardContent>
          </Card>

          {(job as any).requirements?.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Requirements</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="list-disc space-y-1 pl-4 text-sm text-muted-foreground">
                  {(job as any).requirements?.map((req: string, i: number) => (
                    <li key={i}>{req}</li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          )}

          {(job as any).responsibilities?.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Responsibilities</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="list-disc space-y-1 pl-4 text-sm text-muted-foreground">
                  {(job as any).responsibilities?.map((r: string, i: number) => (
                    <li key={i}>{r}</li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          )}

          {(job as any).benefits?.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Benefits</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="list-disc space-y-1 pl-4 text-sm text-muted-foreground">
                  {(job as any).benefits?.map((b: string, i: number) => (
                    <li key={i}>{b}</li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          )}

          {job.skills?.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Skills</CardTitle>
              </CardHeader>
              <CardContent className="flex flex-wrap gap-2">
                {job.skills.map((s) => (
                  <Badge key={s} variant="secondary">{s}</Badge>
                ))}
              </CardContent>
            </Card>
          )}
        </div>

        <div className="w-80 shrink-0 space-y-4">
          <MatchScoreCard
            matchScore={job.matchScore ?? 0}
            matchedSkills={[]}
            missingSkills={[]}
          />

          <Button
            variant="outline"
            className="w-full"
            onClick={() => matchMutation.mutate()}
            disabled={matchMutation.isPending}
          >
            <Sparkles className="mr-2 h-4 w-4" />
            {matchMutation.isPending ? 'Analyzing...' : 'AI Match Analysis'}
          </Button>

          <Card>
            <CardHeader>
              <CardTitle className="text-sm">Company</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="flex items-center gap-3">
                <Avatar className="h-10 w-10 rounded-lg">
                  {job.companyLogoUrl ? <AvatarImage src={job.companyLogoUrl} alt={job.companyName} /> : null}
                  <AvatarFallback className="rounded-lg bg-primary/10 text-sm font-medium text-primary">
                    {job.companyName?.charAt(0)?.toUpperCase() ?? '?'}
                  </AvatarFallback>
                </Avatar>
                <div>
                  <p className="font-medium">{job.companyName}</p>
                  <p className="text-xs text-muted-foreground">{job.employmentType}</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      <Dialog open={applyOpen} onOpenChange={setApplyOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Apply to {job.title}</DialogTitle>
            <DialogDescription>
              {job.companyName} - {job.location}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium">Cover Letter (optional)</label>
              <textarea
                className="mt-1 min-h-[150px] w-full rounded-md border border-input bg-transparent p-3 text-sm"
                placeholder="Write a brief cover letter..."
                value={coverLetter}
                onChange={(e) => setCoverLetter(e.target.value)}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setApplyOpen(false)}>Cancel</Button>
            <Button
              onClick={() => applyMutation.mutate({ coverLetter: coverLetter || undefined })}
              disabled={applyMutation.isPending}
            >
              {applyMutation.isPending ? 'Submitting...' : 'Submit Application'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
