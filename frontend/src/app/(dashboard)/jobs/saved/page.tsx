'use client';

import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import type { JobListing } from '@/types';
import { JobCard } from '@/components/features/jobs/JobCard';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { toast } from 'sonner';
import { ArrowLeft, Bookmark } from 'lucide-react';
import Link from 'next/link';

export default function SavedJobsPage() {
  const { data, isLoading, isError } = useApiQuery<JobListing[]>(
    ['saved-jobs'],
    API.jobs.saved
  );

  const unsaveMutation = useApiMutation<void, void>('DELETE', '', {
    onSuccess: () => toast.success('Job removed from saved'),
    onError: () => toast.error('Failed to remove job'),
  });

  const handleUnsave = (id: string) => {
    unsaveMutation.mutate();
  };

  const jobs = data?.data ?? [];

  if (isError) {
    return (
      <div className="flex flex-col items-center justify-center py-20">
        <p className="text-destructive">Failed to load saved jobs</p>
        <Button variant="outline" className="mt-4" onClick={() => window.location.reload()}>Try Again</Button>
      </div>
    );
  }

  return (
    <div className="p-6">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <Link href="/jobs" className="text-muted-foreground hover:text-foreground">
              <ArrowLeft className="h-4 w-4" />
            </Link>
            <h1 className="text-2xl font-bold">Saved Jobs</h1>
          </div>
          <p className="text-sm text-muted-foreground">Jobs you&apos;ve bookmarked for later</p>
        </div>
      </div>

      {isLoading ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className="h-52 rounded-xl" />
          ))}
        </div>
      ) : jobs.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20">
          <Bookmark className="mb-4 h-16 w-16 text-muted-foreground" />
          <h3 className="mb-2 text-lg font-medium">No saved jobs</h3>
          <p className="mb-6 text-sm text-muted-foreground">
            Save jobs while searching to view them here
          </p>
          <Link href="/jobs">
            <Button>Browse Jobs</Button>
          </Link>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {jobs.map((job) => (
            <JobCard key={job.id} job={job} onSave={handleUnsave} isSaved />
          ))}
        </div>
      )}
    </div>
  );
}
