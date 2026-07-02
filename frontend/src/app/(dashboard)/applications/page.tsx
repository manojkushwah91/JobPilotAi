'use client';

import { useState } from 'react';
import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import type { Application, ApplicationStatus } from '@/types';
import { KanbanColumn } from '@/components/features/applications/KanbanColumn';
import { StatusBadge } from '@/components/features/applications/StatusBadge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Skeleton } from '@/components/ui/skeleton';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter,
} from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table';
import { toast } from 'sonner';
import { Plus, LayoutGrid, List } from 'lucide-react';
import Link from 'next/link';

const COLUMNS: ApplicationStatus[] = [
  'SAVED', 'APPLIED', 'PHONE_SCREEN', 'TECHNICAL_INTERVIEW',
  'ONSITE_INTERVIEW', 'OFFER', 'ACCEPTED', 'REJECTED',
];

const STATUS_LABELS: Record<ApplicationStatus, string> = {
  SAVED: 'Saved', APPLIED: 'Applied', PHONE_SCREEN: 'Phone Screen',
  TECHNICAL_INTERVIEW: 'Technical', ONSITE_INTERVIEW: 'Onsite',
  OFFER: 'Offer', ACCEPTED: 'Accepted', REJECTED: 'Rejected', WITHDRAWN: 'Withdrawn',
};

export default function ApplicationsPage() {
  const [viewMode, setViewMode] = useState<'kanban' | 'table'>('kanban');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [createOpen, setCreateOpen] = useState(false);
  const [newJobId, setNewJobId] = useState('');
  const [newStatus, setNewStatus] = useState<ApplicationStatus>('SAVED');
  const [companyFilter, setCompanyFilter] = useState('');

  const { data, isLoading, isError } = useApiQuery<Application[]>(['applications'], API.applications.list);

  const createMutation = useApiMutation<Application, { jobListingId: string; status: ApplicationStatus }>(
    'POST', API.applications.create, {
      onSuccess: () => {
        toast.success('Application logged');
        setCreateOpen(false);
        setNewJobId('');
        setNewStatus('SAVED');
      },
      onError: () => toast.error('Failed to log application'),
    }
  );

  const updateStatusMutation = useApiMutation<Application, { status: ApplicationStatus }>('PUT', '', {
    onSuccess: () => toast.success('Status updated'),
    onError: () => toast.error('Failed to update status'),
  });

  const applications = data?.data ?? [];

  const handleStatusChange = (id: string, newStatus: ApplicationStatus) => {
    updateStatusMutation.mutate({ status: newStatus });
  };

  const filtered = applications.filter((app) => {
    if (statusFilter !== 'all' && app.status !== statusFilter) return false;
    return true;
  });

  const groupedByStatus = COLUMNS.reduce(
    (acc, status) => {
      acc[status] = filtered.filter((app) => app.status === status);
      return acc;
    },
    {} as Record<ApplicationStatus, Application[]>
  );

  if (isError) {
    return (
      <div className="flex flex-col items-center justify-center py-20">
        <p className="text-destructive">Failed to load applications</p>
        <Button variant="outline" className="mt-4" onClick={() => window.location.reload()}>Try Again</Button>
      </div>
    );
  }

  return (
    <div className="p-6">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Applications</h1>
          <p className="text-sm text-muted-foreground">Track your job applications</p>
        </div>
        <div className="flex items-center gap-2">
          <div className="flex items-center gap-1 rounded-md border p-1">
            <Button
              variant={viewMode === 'kanban' ? 'secondary' : 'ghost'}
              size="icon"
              className="h-8 w-8"
              onClick={() => setViewMode('kanban')}
            >
              <LayoutGrid className="h-4 w-4" />
            </Button>
            <Button
              variant={viewMode === 'table' ? 'secondary' : 'ghost'}
              size="icon"
              className="h-8 w-8"
              onClick={() => setViewMode('table')}
            >
              <List className="h-4 w-4" />
            </Button>
          </div>
          <Button onClick={() => setCreateOpen(true)}>
            <Plus className="mr-2 h-4 w-4" />
            Log Application
          </Button>
        </div>
      </div>

      {viewMode === 'table' && (
        <div className="mb-4 flex items-center gap-2">
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-40">
              <SelectValue placeholder="All Statuses" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Statuses</SelectItem>
              {Object.entries(STATUS_LABELS).map(([key, label]) => (
                <SelectItem key={key} value={key}>{label}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      )}

      {isLoading ? (
        viewMode === 'kanban' ? (
          <div className="flex gap-4 overflow-x-auto pb-4">
            {COLUMNS.map((s) => (
              <div key={s} className="w-64 shrink-0 space-y-3">
                <Skeleton className="h-6 w-24" />
                {Array.from({ length: 3 }).map((_, i) => (
                  <Skeleton key={i} className="h-28 w-full rounded-xl" />
                ))}
              </div>
            ))}
          </div>
        ) : (
          <div className="space-y-2">
            {Array.from({ length: 5 }).map((_, i) => (
              <Skeleton key={i} className="h-12 w-full rounded-lg" />
            ))}
          </div>
        )
      ) : filtered.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20">
          <LayoutGrid className="mb-4 h-16 w-16 text-muted-foreground" />
          <h3 className="mb-2 text-lg font-medium">
            {applications.length === 0 ? 'Start applying to track your applications' : 'No applications match your filters'}
          </h3>
          <p className="mb-6 text-sm text-muted-foreground">
            {applications.length === 0
              ? 'Log your first application to begin tracking'
              : 'Try adjusting your filters'}
          </p>
          {applications.length === 0 && (
            <Button onClick={() => setCreateOpen(true)}>
              <Plus className="mr-2 h-4 w-4" />
              Log Application
            </Button>
          )}
        </div>
      ) : viewMode === 'kanban' ? (
        <div className="flex gap-4 overflow-x-auto pb-4">
          {COLUMNS.map((status) => (
            <KanbanColumn
              key={status}
              status={status}
              applications={groupedByStatus[status]}
              onStatusChange={handleStatusChange}
            />
          ))}
        </div>
      ) : (
        <div className="rounded-lg border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Status</TableHead>
                <TableHead>Applied</TableHead>
                <TableHead>Updated</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filtered.map((app) => (
                <TableRow key={app.id}>
                  <TableCell>
                    <Link href={`/applications/${app.id}`} className="flex items-center gap-2">
                      <StatusBadge status={app.status} />
                    </Link>
                  </TableCell>
                  <TableCell className="text-sm">
                    {new Date(app.appliedAt).toLocaleDateString()}
                  </TableCell>
                  <TableCell className="text-sm text-muted-foreground">
                    {new Date(app.updatedAt).toLocaleDateString()}
                  </TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" asChild>
                      <Link href={`/applications/${app.id}`}>View</Link>
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      <Dialog open={createOpen} onOpenChange={setCreateOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Log Application</DialogTitle>
            <DialogDescription>Track a new job application</DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="jobId">Job Listing ID</Label>
              <Input
                id="jobId"
                placeholder="Enter job listing ID"
                value={newJobId}
                onChange={(e) => setNewJobId(e.target.value)}
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="status">Current Status</Label>
              <Select value={newStatus} onValueChange={(v) => setNewStatus(v as ApplicationStatus)}>
                <SelectTrigger className="mt-1">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(STATUS_LABELS).map(([key, label]) => (
                    <SelectItem key={key} value={key}>{label}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCreateOpen(false)}>Cancel</Button>
            <Button
              onClick={() => createMutation.mutate({ jobListingId: newJobId, status: newStatus })}
              disabled={!newJobId.trim() || createMutation.isPending}
            >
              {createMutation.isPending ? 'Logging...' : 'Log Application'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
