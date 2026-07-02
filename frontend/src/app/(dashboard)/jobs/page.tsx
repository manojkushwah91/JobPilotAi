'use client';

import { useState } from 'react';
import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import type { JobListing, Pagination } from '@/types';
import { JobCard } from '@/components/features/jobs/JobCard';
import { JobFilters } from '@/components/features/jobs/JobFilters';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Skeleton } from '@/components/ui/skeleton';
import { toast } from 'sonner';
import { Search, LayoutGrid, List, Bookmark } from 'lucide-react';
import Link from 'next/link';

interface JobsResponse {
  content: JobListing[];
  pagination: Pagination;
}

export default function JobsPage() {
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState({
    keyword: '',
    location: '',
    employmentType: [] as string[],
    experienceLevel: [] as string[],
    salaryMin: '',
    salaryMax: '',
    postedWithin: '',
  });

  const params: Record<string, unknown> = {
    page,
    size: 12,
  };
  if (filters.keyword) params.keyword = filters.keyword;
  if (filters.location) params.location = filters.location;
  if (filters.employmentType.length) params.employmentType = filters.employmentType.join(',');
  if (filters.experienceLevel.length) params.experienceLevel = filters.experienceLevel.join(',');
  if (filters.salaryMin) params.salaryMin = filters.salaryMin;
  if (filters.salaryMax) params.salaryMax = filters.salaryMax;
  if (filters.postedWithin) params.postedWithin = filters.postedWithin;

  const queryKey = ['jobs', filters.keyword, filters.location, ...filters.employmentType, ...filters.experienceLevel, filters.salaryMin, filters.salaryMax, filters.postedWithin];

  const { data, isLoading, isError } = useApiQuery<JobsResponse>(
    queryKey,
    API.jobs.search,
    params
  );

  const saveMutation = useApiMutation<void, void>('POST', '', {
    onSuccess: () => toast.success('Job saved'),
    onError: () => toast.error('Failed to save job'),
  });

  const handleSave = (id: string) => {
    saveMutation.mutate();
  };

  const jobs = data?.data?.content ?? [];
  const pagination = data?.data?.pagination;
  const totalPages = pagination?.totalPages ?? 0;

  const resetFilters = () => {
    setFilters({ keyword: '', location: '', employmentType: [], experienceLevel: [], salaryMin: '', salaryMax: '', postedWithin: '' });
    setPage(0);
  };

  if (isError) {
    return (
      <div className="flex flex-col items-center justify-center py-20">
        <p className="text-destructive">Failed to load jobs</p>
        <Button variant="outline" className="mt-4" onClick={() => window.location.reload()}>Try Again</Button>
      </div>
    );
  }

  return (
    <div className="p-6">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Job Search</h1>
          <p className="text-sm text-muted-foreground">Find your next opportunity</p>
        </div>
        <Link href="/jobs/saved">
          <Button variant="outline" size="sm">
            <Bookmark className="mr-1.5 h-4 w-4" />
            Saved Jobs
          </Button>
        </Link>
      </div>

      <div className="mb-6 flex flex-col gap-4 sm:flex-row">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Job title, keyword, or company"
            value={filters.keyword}
            onChange={(e) => { setFilters({ ...filters, keyword: e.target.value }); setPage(0); }}
            className="pl-9"
          />
        </div>
        <div className="relative w-full sm:w-48">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Location"
            value={filters.location}
            onChange={(e) => { setFilters({ ...filters, location: e.target.value }); setPage(0); }}
            className="pl-9"
          />
        </div>
        <div className="flex items-center gap-1 rounded-md border p-1">
          <Button
            variant={viewMode === 'grid' ? 'secondary' : 'ghost'}
            size="icon"
            className="h-8 w-8"
            onClick={() => setViewMode('grid')}
          >
            <LayoutGrid className="h-4 w-4" />
          </Button>
          <Button
            variant={viewMode === 'list' ? 'secondary' : 'ghost'}
            size="icon"
            className="h-8 w-8"
            onClick={() => setViewMode('list')}
          >
            <List className="h-4 w-4" />
          </Button>
        </div>
      </div>

      <div className="flex gap-6">
        <div className="hidden w-56 shrink-0 lg:block">
          <div className="sticky top-6">
            <JobFilters filters={filters} onChange={(f) => { setFilters(f); setPage(0); }} onReset={resetFilters} />
          </div>
        </div>

        <div className="flex-1">
          {isLoading ? (
            <div className={viewMode === 'grid' ? 'grid gap-4 sm:grid-cols-2' : 'space-y-3'}>
              {Array.from({ length: 6 }).map((_, i) => (
                <Skeleton key={i} className="h-52 rounded-xl" />
              ))}
            </div>
          ) : jobs.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-20">
              <Search className="mb-4 h-16 w-16 text-muted-foreground" />
              <h3 className="mb-2 text-lg font-medium">No jobs found</h3>
              <p className="mb-6 text-sm text-muted-foreground">
                Try adjusting your search criteria or filters
              </p>
              <Button onClick={resetFilters}>Clear Filters</Button>
            </div>
          ) : (
            <>
              <div className={viewMode === 'grid' ? 'grid gap-4 sm:grid-cols-2' : 'space-y-3'}>
                {jobs.map((job) => (
                  <JobCard key={job.id} job={job} onSave={handleSave} />
                ))}
              </div>

              {totalPages > 1 && (
                <div className="mt-6 flex items-center justify-center gap-2">
                  <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage(page - 1)}>
                    Previous
                  </Button>
                  {Array.from({ length: Math.min(totalPages, 5) }).map((_, i) => (
                    <Button
                      key={i}
                      variant={page === i ? 'default' : 'outline'}
                      size="sm"
                      className="h-8 w-8 p-0"
                      onClick={() => setPage(i)}
                    >
                      {i + 1}
                    </Button>
                  ))}
                  <Button variant="outline" size="sm" disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>
                    Next
                  </Button>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
