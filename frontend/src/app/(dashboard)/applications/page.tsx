'use client';

import { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { apiPut } from '@/lib/api/client';
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
import { Plus, LayoutGrid, List, ClipboardList, Sparkles } from 'lucide-react';
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
  const queryClient = useQueryClient();
  const [viewMode, setViewMode] = useState<'kanban' | 'table'>('kanban');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [createOpen, setCreateOpen] = useState(false);
  const [newJobId, setNewJobId] = useState('');
  const [newStatus, setNewStatus] = useState<ApplicationStatus>('SAVED');

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

  const applications = data?.data ?? [];

  const handleStatusChange = async (id: string, newStatus: ApplicationStatus) => {
    try {
      await apiPut(API.applications.status(id), { status: newStatus });
      queryClient.invalidateQueries({ queryKey: ['applications'] });
      toast.success('Status updated');
    } catch {
      toast.error('Failed to update status');
    }
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

  const stats = {
    total: applications.length,
    active: applications.filter(a => !['REJECTED', 'ACCEPTED', 'WITHDRAWN'].includes(a.status)).length,
    interviews: applications.filter(a => ['PHONE_SCREEN', 'TECHNICAL_INTERVIEW', 'ONSITE_INTERVIEW'].includes(a.status)).length,
    offers: applications.filter(a => a.status === 'OFFER').length,
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-mesh p-6">
        <div className="relative z-10">
          <div className="flex items-center gap-2 mb-1">
            <Sparkles className="h-4 w-4 text-primary" />
            <span className="text-xs font-medium text-primary">Pipeline View</span>
          </div>
          <h1 className="text-3xl font-bold tracking-tight mb-1">Applications</h1>
          <p className="text-muted-foreground">
            {stats.total} total • {stats.active} active • {stats.interviews} in interviews • {stats.offers} offers
          </p>
        </div>
        <div className="absolute -right-16 -top-16 h-48 w-48 rounded-full bg-primary/5 blur-3xl" />
      </div>

      {/* Toolbar */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          {viewMode === 'table' && (
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger className="w-40 h-10 glass border-border/50">
                <SelectValue placeholder="All Statuses" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Statuses</SelectItem>
                {Object.entries(STATUS_LABELS).map(([key, label]) => (
                  <SelectItem key={key} value={key}>{label}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
        </div>
        <div className="flex items-center gap-2">
          <div className="flex items-center gap-1 rounded-lg border border-border/50 bg-background/50 p-1">
            <Button
              variant={viewMode === 'kanban' ? 'secondary' : 'ghost'}
              size="icon"
              className="h-9 w-9"
              onClick={() => setViewMode('kanban')}
            >
              <LayoutGrid className="h-4 w-4" />
            </Button>
            <Button
              variant={viewMode === 'table' ? 'secondary' : 'ghost'}
              size="icon"
              className="h-9 w-9"
              onClick={() => setViewMode('table')}
            >
              <List className="h-4 w-4" />
            </Button>
          </div>
          <Button onClick={() => setCreateOpen(true)} className="bg-gradient-primary hover:opacity-90 gap-2">
            <Plus className="h-4 w-4" />
            Log Application
          </Button>
        </div>
      </div>

      {/* Content */}
      {isLoading ? (
        viewMode === 'kanban' ? (
          <div className="flex gap-4 overflow-x-auto pb-4">
            {COLUMNS.map((s) => (
              <div key={s} className="w-64 shrink-0 space-y-3">
                <div className="skeleton-premium h-6 w-24 rounded-lg" />
                {Array.from({ length: 3 }).map((_, i) => (
                  <div key={i} className="skeleton-premium h-28 w-full rounded-xl" />
                ))}
              </div>
            ))}
          </div>
        ) : (
          <div className="space-y-2">
            {Array.from({ length: 5 }).map((_, i) => (
              <div key={i} className="skeleton-premium h-12 w-full rounded-lg" />
            ))}
          </div>
        )
      ) : filtered.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 glass rounded-xl">
          <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-muted/50">
            <ClipboardList className="h-8 w-8 text-muted-foreground" />
          </div>
          <h3 className="mb-2 text-lg font-medium">
            {applications.length === 0 ? 'No applications yet' : 'No applications match your filters'}
          </h3>
          <p className="mb-6 text-sm text-muted-foreground text-center max-w-sm">
            {applications.length === 0
              ? 'Start applying to jobs and track your progress here'
              : 'Try adjusting your filters'}
          </p>
          {applications.length === 0 && (
            <Button onClick={() => setCreateOpen(true)} className="bg-gradient-primary hover:opacity-90">
              <Plus className="mr-2 h-4 w-4" />
              Log First Application
            </Button>
          )}
        </div>
      ) : viewMode === 'kanban' ? (
        <div className="flex gap-4 overflow-x-auto pb-4 scrollbar-premium">
          {COLUMNS.map((status, i) => (
            <div key={status} className="animate-fade-in" style={{ animationDelay: `${i * 50}ms` }}>
              <KanbanColumn
                status={status}
                applications={groupedByStatus[status]}
                onStatusChange={handleStatusChange}
              />
            </div>
          ))}
        </div>
      ) : (
        <div className="glass rounded-xl overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow className="border-border/50">
                <TableHead>Status</TableHead>
                <TableHead>Applied</TableHead>
                <TableHead>Updated</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filtered.map((app, i) => (
                <TableRow key={app.id} className="border-border/30 hover:bg-muted/30 animate-fade-in" style={{ animationDelay: `${i * 30}ms` }}>
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
                    <Button variant="ghost" size="sm" asChild className="hover-lift">
                      <Link href={`/applications/${app.id}`}>View</Link>
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      {/* Create Dialog */}
      <Dialog open={createOpen} onOpenChange={setCreateOpen}>
        <DialogContent className="glass-strong">
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
              className="bg-gradient-primary hover:opacity-90"
            >
              {createMutation.isPending ? 'Logging...' : 'Log Application'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
