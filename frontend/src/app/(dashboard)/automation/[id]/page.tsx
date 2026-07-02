'use client';

import { useParams, useRouter } from 'next/navigation';
import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import { AutomationProgress } from '@/components/features/automation/AutomationProgress';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Badge } from '@/components/ui/badge';
import { ArrowLeft, RotateCcw, XCircle } from 'lucide-react';
import { toast } from 'sonner';
import Image from 'next/image';

const STATUS_LABELS: Record<string, string> = {
  QUEUED: 'Queued',
  RUNNING: 'Running',
  AWAITING_CONFIRMATION: 'Awaiting Confirmation',
  COMPLETED: 'Completed',
  FAILED: 'Failed',
  CANCELLED: 'Cancelled',
};

const STATUS_VARIANTS: Record<string, string> = {
  QUEUED: 'secondary',
  RUNNING: 'default',
  AWAITING_CONFIRMATION: 'warning',
  COMPLETED: 'success',
  FAILED: 'destructive',
  CANCELLED: 'outline',
};

interface AutomationSession {
  id: string;
  userId: string;
  applicationId: string;
  status: string;
  currentStep?: string;
  progress: number;
  screenshots: string[];
  errorMessage?: string;
  startedAt: string;
  completedAt?: string;
  createdAt: string;
}

export default function AutomationSessionPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id as string;

  const { data, isLoading, isError, refetch } = useApiQuery<AutomationSession>(
    ['automation', id],
    `/api/v1/automation/sessions/${id}`,
  );

  const confirmMutation = useApiMutation<AutomationSession, void>(
    'POST',
    `/api/v1/automation/sessions/${id}/confirm`,
    {
      onSuccess: () => {
        toast.success('Confirmed! Completing submission...');
        refetch();
      },
      onError: () => toast.error('Failed to confirm'),
    },
  );

  const cancelMutation = useApiMutation<AutomationSession, void>(
    'POST',
    `/api/v1/automation/sessions/${id}/cancel`,
    {
      onSuccess: () => {
        toast.success('Session cancelled');
        refetch();
      },
      onError: () => toast.error('Failed to cancel'),
    },
  );

  const session = data?.data;

  if (isLoading) {
    return (
      <div className="p-6 space-y-4">
        <Skeleton className="h-8 w-48" />
        <Skeleton className="h-4 w-96" />
        <Skeleton className="h-64 rounded-xl" />
      </div>
    );
  }

  if (isError || !session) {
    return (
      <Card>
        <CardContent className="flex flex-col items-center gap-4 py-12">
          <p className="text-destructive">Failed to load automation session</p>
          <Button variant="outline" onClick={() => refetch()}>Retry</Button>
        </CardContent>
      </Card>
    );
  }

  const isAwaiting = session.status === 'AWAITING_CONFIRMATION';
  const isFailed = session.status === 'FAILED';
  const isRunning = session.status === 'RUNNING' || session.status === 'QUEUED';

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => router.back()}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h1 className="text-2xl font-bold">Automation Session</h1>
            <p className="text-sm text-muted-foreground">ID: {session.id}</p>
          </div>
        </div>
        <Badge variant={STATUS_VARIANTS[session.status] as any}>
          {STATUS_LABELS[session.status] || session.status}
        </Badge>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Progress</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="mb-4">
                <div className="flex items-center justify-between text-sm mb-1">
                  <span>{session.progress}% complete</span>
                  {session.currentStep && (
                    <span className="text-muted-foreground capitalize">
                      {session.currentStep.replace(/_/g, ' ')}
                    </span>
                  )}
                </div>
                <div className="h-2 w-full rounded-full bg-muted">
                  <div
                    className={`h-full rounded-full transition-all duration-500 ${
                      isFailed ? 'bg-destructive' : 'bg-primary'
                    }`}
                    style={{ width: `${session.progress}%` }}
                  />
                </div>
              </div>
              <AutomationProgress
                currentStep={session.currentStep}
                progress={session.progress}
                status={session.status}
              />
            </CardContent>
          </Card>

          {session.screenshots.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Screenshots</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid gap-4 sm:grid-cols-2">
                  {session.screenshots.map((url, i) => (
                    <div key={i} className="relative aspect-video rounded-lg border bg-muted overflow-hidden">
                      <Image
                        src={url}
                        alt={`Screenshot ${i + 1}`}
                        fill
                        className="object-cover"
                        unoptimized
                      />
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}

          {isFailed && session.errorMessage && (
            <Card className="border-destructive">
              <CardHeader>
                <CardTitle className="text-base text-destructive flex items-center gap-2">
                  <XCircle className="h-4 w-4" />
                  Error
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-destructive">{session.errorMessage}</p>
                <Button
                  variant="outline"
                  size="sm"
                  className="mt-4 gap-2"
                  onClick={() => {
                    window.location.reload();
                  }}
                >
                  <RotateCcw className="h-4 w-4" />
                  Retry
                </Button>
              </CardContent>
            </Card>
          )}
        </div>

        <div className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">Actions</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {isAwaiting && (
                <Button
                  className="w-full"
                  onClick={() => confirmMutation.mutate()}
                  disabled={confirmMutation.isPending}
                >
                  {confirmMutation.isPending ? 'Confirming...' : 'Confirm & Submit'}
                </Button>
              )}
              {isRunning && (
                <Button
                  variant="outline"
                  className="w-full gap-2"
                  onClick={() => cancelMutation.mutate()}
                  disabled={cancelMutation.isPending}
                >
                  <XCircle className="h-4 w-4" />
                  {cancelMutation.isPending ? 'Cancelling...' : 'Cancel'}
                </Button>
              )}
              {isFailed && (
                <Button
                  variant="outline"
                  className="w-full gap-2"
                  onClick={() => router.push('/applications')}
                >
                  Back to Applications
                </Button>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-sm">Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Created</span>
                <span>{new Date(session.createdAt).toLocaleString()}</span>
              </div>
              {session.startedAt && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Started</span>
                  <span>{new Date(session.startedAt).toLocaleString()}</span>
                </div>
              )}
              {session.completedAt && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Completed</span>
                  <span>{new Date(session.completedAt).toLocaleString()}</span>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
