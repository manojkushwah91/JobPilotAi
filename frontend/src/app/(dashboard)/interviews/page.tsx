'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Skeleton } from '@/components/ui/skeleton';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { InterviewCard } from '@/components/features/interviews/InterviewCard';
import { Plus, BookOpen, RefreshCw } from 'lucide-react';
import { toast } from 'sonner';
import type { InterviewSession, QuestionBankItem, InterviewSessionCreate } from '@/types';

export default function InterviewsPage() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('sessions');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [newSession, setNewSession] = useState<InterviewSessionCreate>({
    targetRole: '',
    targetCompany: '',
    mode: 'TEXT',
  });

  const { data: sessionsRes, isLoading: sessionsLoading, isError: sessionsError, refetch: refetchSessions } = useApiQuery<InterviewSession[]>(
    ['interviews', 'sessions'],
    API.interviews.sessions
  );

  const { data: questionsRes, isLoading: questionsLoading } = useApiQuery<QuestionBankItem[]>(
    ['interviews', 'questions'],
    API.interviews.questionBank
  );

  const createMutation = useApiMutation<InterviewSession, InterviewSessionCreate>('POST', API.interviews.sessions, {
    onSuccess: (res) => {
      toast.success('Interview session created');
      setDialogOpen(false);
      router.push(`/interviews/${res.data.id}`);
    },
    onError: () => {
      toast.error('Failed to create interview session');
    },
  });

  const sessions = sessionsRes?.data ?? [];
  const questions = questionsRes?.data ?? [];

  const handleCreate = () => {
    if (!newSession.targetRole.trim()) {
      toast.error('Please enter a target role');
      return;
    }
    createMutation.mutate(newSession);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Interview Hub</h1>
          <p className="text-muted-foreground">Practice interviews and build your question bank</p>
        </div>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button className="gap-2">
              <Plus className="h-4 w-4" />
              Start New Interview
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Start New Interview</DialogTitle>
              <DialogDescription>Configure your practice interview session</DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="role">Target Role *</Label>
                <Input
                  id="role"
                  placeholder="e.g. Senior Frontend Engineer"
                  value={newSession.targetRole}
                  onChange={(e) => setNewSession({ ...newSession, targetRole: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="company">Target Company (optional)</Label>
                <Input
                  id="company"
                  placeholder="e.g. Google"
                  value={newSession.targetCompany}
                  onChange={(e) => setNewSession({ ...newSession, targetCompany: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <Label>Mode</Label>
                <Select
                  value={newSession.mode}
                  onValueChange={(v: 'TEXT' | 'AUDIO') => setNewSession({ ...newSession, mode: v })}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="TEXT">Text Response</SelectItem>
                    <SelectItem value="AUDIO">Audio Response</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setDialogOpen(false)}>Cancel</Button>
              <Button onClick={handleCreate} disabled={createMutation.isPending}>
                {createMutation.isPending ? 'Creating...' : 'Start Interview'}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value="sessions" className="gap-2">
            <BookOpen className="h-4 w-4" />
            Practice Sessions
          </TabsTrigger>
          <TabsTrigger value="questions" className="gap-2">
            <BookOpen className="h-4 w-4" />
            Question Bank
          </TabsTrigger>
        </TabsList>

        <TabsContent value="sessions" className="space-y-4">
          {sessionsLoading ? (
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {Array.from({ length: 6 }).map((_, i) => (
                <Card key={i}>
                  <CardHeader>
                    <Skeleton className="h-5 w-3/4" />
                    <Skeleton className="h-4 w-1/2" />
                  </CardHeader>
                  <CardContent className="space-y-2">
                    <Skeleton className="h-4 w-full" />
                    <Skeleton className="h-4 w-2/3" />
                    <Skeleton className="h-4 w-1/3" />
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : sessionsError ? (
            <Card>
              <CardContent className="flex flex-col items-center gap-4 py-12">
                <p className="text-destructive">Failed to load interview sessions</p>
                <Button variant="outline" onClick={() => refetchSessions()} className="gap-2">
                  <RefreshCw className="h-4 w-4" />
                  Retry
                </Button>
              </CardContent>
            </Card>
          ) : sessions.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center gap-4 py-12">
                <BookOpen className="h-12 w-12 text-muted-foreground" />
                <div className="text-center">
                  <p className="text-lg font-medium">No interview sessions yet</p>
                  <p className="text-sm text-muted-foreground">Start practicing to prepare for your next interview!</p>
                </div>
                <Button onClick={() => setDialogOpen(true)} className="gap-2">
                  <Plus className="h-4 w-4" />
                  Start First Interview
                </Button>
              </CardContent>
            </Card>
          ) : (
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {sessions.map((session) => (
                <InterviewCard
                  key={session.id}
                  session={session}
                  onSelect={(id) => router.push(`/interviews/${id}`)}
                />
              ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="questions" className="space-y-4">
          {questionsLoading ? (
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {Array.from({ length: 6 }).map((_, i) => (
                <Card key={i}>
                  <CardHeader>
                    <Skeleton className="h-5 w-3/4" />
                    <Skeleton className="h-4 w-1/2" />
                  </CardHeader>
                  <CardContent>
                    <Skeleton className="h-4 w-full" />
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : questions.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center gap-4 py-12">
                <BookOpen className="h-12 w-12 text-muted-foreground" />
                <div className="text-center">
                  <p className="text-lg font-medium">Question bank is empty</p>
                  <p className="text-sm text-muted-foreground">Questions will appear here after your interview sessions</p>
                </div>
                <Button onClick={() => setDialogOpen(true)} className="gap-2">
                  <Plus className="h-4 w-4" />
                  Start an Interview
                </Button>
              </CardContent>
            </Card>
          ) : (
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {questions.map((q) => (
                <Card key={q.id}>
                  <CardHeader>
                    <CardTitle className="text-base">{q.question}</CardTitle>
                    <CardDescription className="flex gap-2">
                      <span>{q.category}</span>
                      <span>·</span>
                      <span>{q.difficulty}</span>
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="flex flex-wrap gap-1">
                      {q.tags.map((tag) => (
                        <span key={tag} className="rounded bg-secondary px-2 py-0.5 text-xs text-secondary-foreground">
                          {tag}
                        </span>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
