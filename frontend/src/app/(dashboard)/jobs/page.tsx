'use client';

import { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useApiQuery } from '@/lib/hooks/useQuery';
import { apiPost } from '@/lib/api/client';
import { API } from '@/lib/api/endpoints';
import type { JobListing, Pagination } from '@/types';
import { JobCard } from '@/components/features/jobs/JobCard';
import { JobFilters } from '@/components/features/jobs/JobFilters';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Skeleton } from '@/components/ui/skeleton';
import { toast } from 'sonner';
import { Search, LayoutGrid, List, Bookmark, Sparkles, SlidersHorizontal, X } from 'lucide-react';
import Link from 'next/link';

interface JobsResponse {
  content: JobListing[];
  pagination: Pagination;
}

export default function JobsPage() {
  const queryClient = useQueryClient();
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [page, setPage] = useState(0);
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState({
    keyword: '',
    location: '',
    employmentType: [] as string[],
    experienceLevel: [] as string[],
    salaryMin: '',
    salaryMax: '',
    postedWithin: '',
  });

  const params: Record<string, unknown> = { page, size: 12 };
  if (filters.keyword) params.keyword = filters.keyword;
  if (filters.location) params.location = filters.location;
  if (filters.employmentType.length) params.employmentType = filters.employmentType.join(',');
  if (filters.experienceLevel.length) params.experienceLevel = filters.experienceLevel.join(',');
  if (filters.salaryMin) params.salaryMin = filters.salaryMin;
  if (filters.salaryMax) params.salaryMax = filters.salaryMax;
  if (filters.postedWithin) params.postedWithin = filters.postedWithin;

  const queryKey = ['jobs', filters.keyword, filters.location, ...filters.employmentType, ...filters.experienceLevel, filters.salaryMin, filters.salaryMax, filters.postedWithin];

  const { data, isLoading, isError } = useApiQuery<JobsResponse>(queryKey, API.jobs.search, params);

  const handleSave = async (id: string) => {
    try {
      await apiPost(API.jobs.save(id));
      queryClient.invalidateQueries({ queryKey: ['jobs'] });
      toast.success('Job saved');
    } catch {
      toast.error('Failed to save job');
    }
  };

  const jobs = data?.data?.content ?? [];
  const pagination = data?.data?.pagination;
  const totalPages = pagination?.totalPages ?? 0;

  const resetFilters = () => {
    setFilters({ keyword: '', location: '', employmentType: [], experienceLevel: [], salaryMin: '', salaryMax: '', postedWithin: '' });
    setPage(0);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-mesh p-6">
        <div className="relative z-10">
          <div className="flex items-center gap-2 mb-1">
            <Sparkles className="h-4 w-4 text-primary" />
            <span className="text-xs font-medium text-primary">AI-Powered Search</span>
          </div>
          <h1 className="text-3xl font-bold tracking-tight mb-1">Job Search</h1>
          <p className="text-muted-foreground">Find your next opportunity • {jobs.length} jobs found</p>
        </div>
        <div className="absolute -right-16 -top-16 h-48 w-48 rounded-full bg-primary/5 blur-3xl" />
      </div>

      {/* Search Bar */}
      <div className="glass rounded-xl p-4">
        <div className="flex flex-col gap-3 sm:flex-row">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Job title, keyword, or company"
              value={filters.keyword}
              onChange={(e) => { setFilters({ ...filters, keyword: e.target.value }); setPage(0); }}
              className="h-11 pl-10 bg-background/50 border-border/50 focus:border-primary/50"
            />
          </div>
          <div className="relative w-full sm:w-48">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Location"
              value={filters.location}
              onChange={(e) => { setFilters({ ...filters, location: e.target.value }); setPage(0); }}
              className="h-11 pl-10 bg-background/50 border-border/50 focus:border-primary/50"
            />
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant={showFilters ? 'default' : 'outline'}
              size="sm"
              className="gap-2 h-11"
              onClick={() => setShowFilters(!showFilters)}
            >
              <SlidersHorizontal className="h-4 w-4" />
              Filters
            </Button>
            <div className="flex items-center gap-1 rounded-lg border border-border/50 bg-background/50 p-1">
              <Button
                variant={viewMode === 'grid' ? 'secondary' : 'ghost'}
                size="icon"
                className="h-9 w-9"
                onClick={() => setViewMode('grid')}
              >
                <LayoutGrid className="h-4 w-4" />
              </Button>
              <Button
                variant={viewMode === 'list' ? 'secondary' : 'ghost'}
                size="icon"
                className="h-9 w-9"
                onClick={() => setViewMode('list')}
              >
                <List className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex gap-6">
        {/* Filters Sidebar */}
        {showFilters && (
          <div className="hidden w-56 shrink-0 lg:block animate-slide-in-left">
            <div className="sticky top-6 glass rounded-xl p-4">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-sm font-medium">Filters</h3>
                <Button variant="ghost" size="sm" className="h-6 px-2 text-xs" onClick={resetFilters}>
                  Reset
                </Button>
              </div>
              <JobFilters filters={filters} onChange={(f) => { setFilters(f); setPage(0); }} onReset={resetFilters} />
            </div>
          </div>
        )}

        {/* Job Grid */}
        <div className="flex-1">
          {isLoading ? (
            <div className={viewMode === 'grid' ? 'grid gap-4 sm:grid-cols-2' : 'space-y-3'}>
              {Array.from({ length: 6 }).map((_, i) => (
                <div key={i} className="skeleton-premium h-52 rounded-xl" />
              ))}
            </div>
          ) : jobs.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-20 glass rounded-xl">
              <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-muted/50">
                <Search className="h-8 w-8 text-muted-foreground" />
              </div>
              <h3 className="mb-2 text-lg font-medium">No jobs found</h3>
              <p className="mb-6 text-sm text-muted-foreground text-center max-w-sm">
                Try adjusting your search criteria or filters to find more opportunities
              </p>
              <Button onClick={resetFilters} className="bg-gradient-primary hover:opacity-90">
                Clear Filters
              </Button>
            </div>
          ) : (
            <>
              <div className={viewMode === 'grid' ? 'grid gap-4 sm:grid-cols-2' : 'space-y-3'}>
                {jobs.map((job, i) => (
                  <div key={job.id} className="animate-fade-in" style={{ animationDelay: `${i * 50}ms` }}>
                    <JobCard job={job} onSave={handleSave} />
                  </div>
                ))}
              </div>

              {totalPages > 1 && (
                <div className="mt-8 flex items-center justify-center gap-2">
                  <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage(page - 1)}>
                    Previous
                  </Button>
                  {Array.from({ length: Math.min(totalPages, 5) }).map((_, i) => (
                    <Button
                      key={i}
                      variant={page === i ? 'default' : 'outline'}
                      size="sm"
                      className={`h-9 w-9 p-0 ${page === i ? 'bg-gradient-primary shadow-glow' : ''}`}
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
