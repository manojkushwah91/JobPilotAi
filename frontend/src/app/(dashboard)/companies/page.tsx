'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useApiQuery } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Skeleton } from '@/components/ui/skeleton';
import { CompanyCard } from '@/components/features/companies/CompanyCard';
import { Search, Building2, RefreshCw } from 'lucide-react';
import type { Company } from '@/types';

export default function CompaniesPage() {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');

  const { data: res, isLoading, isError, refetch } = useApiQuery<Company[]>(
    ['companies', 'search', debouncedQuery],
    API.companies.search,
    debouncedQuery ? { query: debouncedQuery } : undefined
  );

  const companies = res?.data ?? [];

  const handleSearch = () => {
    setDebouncedQuery(searchQuery);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Companies</h1>
        <p className="text-muted-foreground">Discover and research companies</p>
      </div>

      <div className="flex gap-2">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search companies by name, industry..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={handleKeyDown}
            className="pl-10"
          />
        </div>
        <Button onClick={handleSearch}>Search</Button>
      </div>

      {isLoading ? (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <Card key={i}>
              <CardContent className="p-6">
                <div className="flex items-start gap-4">
                  <Skeleton className="h-12 w-12 rounded-lg" />
                  <div className="flex-1 space-y-2">
                    <Skeleton className="h-5 w-3/4" />
                    <Skeleton className="h-4 w-1/2" />
                  </div>
                </div>
                <div className="mt-4 flex gap-1">
                  <Skeleton className="h-5 w-16 rounded-full" />
                  <Skeleton className="h-5 w-20 rounded-full" />
                  <Skeleton className="h-5 w-14 rounded-full" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : isError ? (
        <Card>
          <CardContent className="flex flex-col items-center gap-4 py-12">
            <p className="text-destructive">Failed to load companies</p>
            <Button variant="outline" onClick={() => refetch()} className="gap-2">
              <RefreshCw className="h-4 w-4" />
              Retry
            </Button>
          </CardContent>
        </Card>
      ) : companies.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center gap-4 py-12">
            <Building2 className="h-12 w-12 text-muted-foreground" />
            <div className="text-center">
              <p className="text-lg font-medium">No companies found</p>
              <p className="text-sm text-muted-foreground">
                {debouncedQuery ? 'Try a different search term' : 'Start searching for companies'}
              </p>
            </div>
            {!debouncedQuery && (
              <p className="text-sm text-muted-foreground">Use the search bar above to discover companies</p>
            )}
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {companies.map((company) => (
            <CompanyCard
              key={company.id}
              company={company}
              onSelect={(id) => router.push(`/companies/${id}`)}
            />
          ))}
        </div>
      )}
    </div>
  );
}
