'use client';

import { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import type { Application, ApplicationNote, ApplicationStatus } from '@/types';
import { StatusBadge, STATUS_CONFIG } from '@/components/features/applications/StatusBadge';
import { StatusTimeline } from '@/components/features/applications/StatusTimeline';
import { ApplicationNoteCard } from '@/components/features/applications/ApplicationNote';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Skeleton } from '@/components/ui/skeleton';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter,
} from '@/components/ui/dialog';
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select';
import { toast } from 'sonner';
import { ArrowLeft, Send, Clock, Sparkles } from 'lucide-react';

const APP_STATUSES: ApplicationStatus[] = [
  'SAVED', 'APPLIED', 'PHONE_SCREEN', 'TECHNICAL_INTERVIEW',
  'ONSITE_INTERVIEW', 'OFFER', 'ACCEPTED', 'REJECTED', 'WITHDRAWN',
];

const STATUS_LABELS: Record<ApplicationStatus, string> = {
  SAVED: 'Saved', APPLIED: 'Applied', PHONE_SCREEN: 'Phone Screen',
  TECHNICAL_INTERVIEW: 'Technical', ONSITE_INTERVIEW: 'Onsite',
  OFFER: 'Offer', ACCEPTED: 'Accepted', REJECTED: 'Rejected', WITHDRAWN: 'Withdrawn',
};

export default function ApplicationDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id as string;

  const [statusOpen, setStatusOpen] = useState(false);
  const [newStatus, setNewStatus] = useState<ApplicationStatus>('APPLIED');
  const [statusNote, setStatusNote] = useState('');
  const [noteContent, setNoteContent] = useState('');
  const [noteCategory, setNoteCategory] = useState('general');

  const { data, isLoading, isError } = useApiQuery<Application>(['application', id], API.applications.detail(id));

  const updateStatusMutation = useApiMutation<Application, { status: ApplicationStatus; note?: string }>(
    'PUT', API.applications.status(id), {
      onSuccess: () => {
        toast.success('Status updated');
        setStatusOpen(false);
        setStatusNote('');
      },
      onError: () => toast.error('Failed to update status'),
    }
  );

  const addNoteMutation = useApiMutation<ApplicationNote, { content: string; category: string }>(
    'POST', API.applications.notes(id), {
      onSuccess: () => {
        toast.success('Note added');
        setNoteContent('');
        setNoteCategory('general');
      },
      onError: () => toast.error('Failed to add note'),
    }
  );

  const deleteNoteMutation = useApiMutation<void, void>('DELETE', '', {
    onSuccess: () => toast.success('Note deleted'),
    onError: () => toast.error('Failed to delete note'),
  });

  const automateMutation = useApiMutation<any, void>('POST', API.applications.automate(id), {
    onSuccess: () => toast.success('Automation initiated'),
    onError: () => toast.error('Failed to start automation'),
  });

  const application = data?.data;

  if (isLoading) {
    return (
      <div className="p-6">
        <Skeleton className="mb-4 h-8 w-16" />
        <Skeleton className="mb-6 h-8 w-96" />
        <div className="flex gap-6">
          <div className="flex-1 space-y-4">
            <Skeleton className="h-32 rounded-xl" />
            <Skeleton className="h-48 rounded-xl" />
            <Skeleton className="h-32 rounded-xl" />
          </div>
          <Skeleton className="h-64 w-80 rounded-xl" />
        </div>
      </div>
    );
  }

  if (isError || !application) {
    return (
      <div className="flex flex-col items-center justify-center py-20">
        <p className="text-destructive">Failed to load application</p>
        <Button variant="outline" className="mt-4" onClick={() => router.push('/applications')}>
          Back to Applications
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
        Back to applications
      </button>

      <div className="mb-6 flex items-start justify-between">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold">Application Details</h1>
            <StatusBadge status={application.status} className="text-sm" />
          </div>
          <p className="mt-1 text-sm text-muted-foreground">
            Applied {new Date(application.appliedAt).toLocaleDateString()}
            {' · '}Updated {new Date(application.updatedAt).toLocaleDateString()}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={() => { setNewStatus(application.status); setStatusOpen(true); }}>
            <Clock className="mr-1.5 h-4 w-4" />
            Update Status
          </Button>
          <Button variant="outline" size="sm" onClick={() => automateMutation.mutate()} disabled={automateMutation.isPending}>
            <Sparkles className="mr-1.5 h-4 w-4" />
            {automateMutation.isPending ? 'Running...' : 'Automate'}
          </Button>
        </div>
      </div>

      <div className="flex gap-6">
        <div className="flex-1 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Status History</CardTitle>
            </CardHeader>
            <CardContent>
              <StatusTimeline history={application.statusHistory} />
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle className="text-base">Notes</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-3">
                {(application.notes?.length ?? 0) > 0 ? (
                  application.notes.map((note) => (
                    <ApplicationNoteCard
                      key={note.id}
                      note={note}
                      onDelete={(nid) => deleteNoteMutation.mutate()}
                    />
                  ))
                ) : (
                  <p className="text-sm text-muted-foreground">No notes added yet</p>
                )}
              </div>

              <div className="border-t pt-4">
                <h4 className="mb-3 text-sm font-medium">Add a Note</h4>
                <div className="space-y-3">
                  <div>
                    <Label htmlFor="note-category" className="text-xs">Category</Label>
                    <Select value={noteCategory} onValueChange={setNoteCategory}>
                      <SelectTrigger className="mt-1 h-8 text-xs">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="general">General</SelectItem>
                        <SelectItem value="interview">Interview</SelectItem>
                        <SelectItem value="follow-up">Follow-up</SelectItem>
                        <SelectItem value="research">Research</SelectItem>
                        <SelectItem value="offer">Offer</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <Textarea
                    placeholder="Write your note..."
                    value={noteContent}
                    onChange={(e) => setNoteContent(e.target.value)}
                    className="min-h-[80px]"
                  />
                  <Button
                    size="sm"
                    onClick={() => addNoteMutation.mutate({ content: noteContent, category: noteCategory })}
                    disabled={!noteContent.trim() || addNoteMutation.isPending}
                  >
                    {addNoteMutation.isPending ? 'Adding...' : 'Add Note'}
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="w-72 shrink-0 space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">Follow-up Reminders</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-muted-foreground">No reminders set</p>
              <Button variant="outline" size="sm" className="mt-3 w-full">
                <Clock className="mr-1.5 h-4 w-4" />
                Set Reminder
              </Button>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-sm">Quick Actions</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              <Button variant="outline" size="sm" className="w-full justify-start" onClick={() => setStatusOpen(true)}>
                <Clock className="mr-2 h-4 w-4" />
                Update Status
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="w-full justify-start"
                onClick={() => automateMutation.mutate()}
                disabled={automateMutation.isPending}
              >
                <Sparkles className="mr-2 h-4 w-4" />
                Automate Application
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>

      <Dialog open={statusOpen} onOpenChange={setStatusOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Update Status</DialogTitle>
            <DialogDescription>Change the current status of this application</DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label>New Status</Label>
              <Select value={newStatus} onValueChange={(v) => setNewStatus(v as ApplicationStatus)}>
                <SelectTrigger className="mt-1">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {APP_STATUSES.map((s) => (
                    <SelectItem key={s} value={s}>{STATUS_LABELS[s]}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label>Note (optional)</Label>
              <Textarea
                value={statusNote}
                onChange={(e) => setStatusNote(e.target.value)}
                placeholder="Add a note about this status change..."
                className="mt-1 min-h-[80px]"
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setStatusOpen(false)}>Cancel</Button>
            <Button
              onClick={() => updateStatusMutation.mutate({ status: newStatus, note: statusNote || undefined })}
              disabled={updateStatusMutation.isPending}
            >
              {updateStatusMutation.isPending ? 'Updating...' : 'Update Status'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
