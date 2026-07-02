'use client';

import { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Textarea } from '@/components/ui/textarea';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { QuestionDisplay } from '@/components/features/interviews/QuestionDisplay';
import { ScoreSummary } from '@/components/features/interviews/ScoreSummary';
import { ArrowLeft, Send, SkipForward, StopCircle, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import type { InterviewSession } from '@/types';

export default function InterviewSessionPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id as string;
  const [answer, setAnswer] = useState('');
  const [endDialogOpen, setEndDialogOpen] = useState(false);
  const [submittingAnswer, setSubmittingAnswer] = useState(false);

  const { data: sessionRes, isLoading, isError, refetch } = useApiQuery<InterviewSession>(
    ['interviews', 'session', id],
    API.interviews.sessionDetail(id)
  );

  const answerMutation = useApiMutation<InterviewSession, { answer: string }>(
    'POST',
    API.interviews.answer(id, sessionRes?.data.questions.find(q => !q.answer)?.id ?? ''),
    {
      onSuccess: () => {
        setAnswer('');
        setSubmittingAnswer(false);
        refetch();
      },
      onError: () => {
        setSubmittingAnswer(false);
        toast.error('Failed to submit answer');
      },
    }
  );

  const nextQuestionMutation = useApiMutation<InterviewSession, void>('POST', API.interviews.nextQuestion(id), {
    onSuccess: () => {
      refetch();
    },
    onError: () => toast.error('Failed to load next question'),
  });

  const completeMutation = useApiMutation<InterviewSession, void>('POST', API.interviews.complete(id), {
    onSuccess: () => {
      toast.success('Session completed');
      setEndDialogOpen(false);
      refetch();
    },
    onError: () => toast.error('Failed to complete session'),
  });

  if (isLoading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-8 w-48" />
        <div className="space-y-4">
          <Skeleton className="h-6 w-32" />
          <Card>
            <CardContent className="p-6">
              <Skeleton className="h-24 w-full" />
            </CardContent>
          </Card>
          <Skeleton className="h-32 w-full" />
        </div>
      </div>
    );
  }

  if (isError) {
    return (
      <Card>
        <CardContent className="flex flex-col items-center gap-4 py-12">
          <p className="text-destructive">Failed to load interview session</p>
          <Button variant="outline" onClick={() => refetch()}>Retry</Button>
        </CardContent>
      </Card>
    );
  }

  const session = sessionRes!.data;
  const currentQuestion = session.questions.find((q) => !q.answer);
  const isCompleted = session.status === 'COMPLETED';

  const currentIndex = session.questions.findIndex((q) => q.id === currentQuestion?.id);
  const questionNumber = currentIndex >= 0 ? currentIndex + 1 : session.questions.length;

  const handleSubmitAnswer = async () => {
    if (!answer.trim() || !currentQuestion) return;
    setSubmittingAnswer(true);
    answerMutation.mutate({ answer });
  };

  const handleEndSession = () => {
    completeMutation.mutate();
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => router.push('/interviews')}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h1 className="text-2xl font-bold">{session.targetRole}</h1>
            {session.targetCompany && (
              <p className="text-sm text-muted-foreground">{session.targetCompany}</p>
            )}
          </div>
        </div>
        {!isCompleted && (
          <Dialog open={endDialogOpen} onOpenChange={setEndDialogOpen}>
            <DialogTrigger asChild>
              <Button variant="destructive" className="gap-2">
                <StopCircle className="h-4 w-4" />
                End Session
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>End Interview Session</DialogTitle>
                <DialogDescription>
                  Are you sure you want to end this session? Your progress will be saved and you&apos;ll receive feedback.
                </DialogDescription>
              </DialogHeader>
              <DialogFooter>
                <Button variant="outline" onClick={() => setEndDialogOpen(false)}>Cancel</Button>
                <Button variant="destructive" onClick={handleEndSession} disabled={completeMutation.isPending}>
                  {completeMutation.isPending ? 'Ending...' : 'End Session'}
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        )}
      </div>

      {isCompleted && session.feedback ? (
        <ScoreSummary feedback={session.feedback} />
      ) : currentQuestion ? (
        <div className="space-y-6">
          <QuestionDisplay
            question={currentQuestion}
            questionNumber={questionNumber}
            totalQuestions={session.totalQuestions}
          />

          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Your Answer</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <Textarea
                placeholder="Type your answer here..."
                value={answer}
                onChange={(e) => setAnswer(e.target.value)}
                rows={6}
              />
              <div className="flex gap-2">
                <Button
                  onClick={handleSubmitAnswer}
                  disabled={!answer.trim() || submittingAnswer}
                  className="gap-2"
                >
                  {submittingAnswer ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <Send className="h-4 w-4" />
                  )}
                  Submit Answer
                </Button>
                <Button
                  variant="outline"
                  onClick={() => nextQuestionMutation.mutate()}
                  disabled={nextQuestionMutation.isPending}
                  className="gap-2"
                >
                  <SkipForward className="h-4 w-4" />
                  Next Question
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      ) : (
        <Card>
          <CardContent className="flex flex-col items-center gap-4 py-12">
            <p className="text-lg font-medium">All questions answered!</p>
            <p className="text-sm text-muted-foreground">Click end session to get your feedback and score.</p>
            <Button onClick={handleEndSession} disabled={completeMutation.isPending} className="gap-2">
              {completeMutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
              Complete & View Results
            </Button>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
