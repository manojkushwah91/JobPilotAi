'use client';

import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Calendar, Clock, Target, Mic, MessageSquare, ArrowRight } from 'lucide-react';
import type { InterviewSession } from '@/types';
import { cn } from '@/lib/utils/cn';

interface InterviewCardProps {
  session: InterviewSession;
  onSelect: (id: string) => void;
}

const statusConfig: Record<string, { label: string; variant: 'default' | 'secondary' | 'success' | 'warning' | 'destructive' }> = {
  IN_PROGRESS: { label: 'In Progress', variant: 'warning' },
  COMPLETED: { label: 'Completed', variant: 'success' },
  CANCELLED: { label: 'Cancelled', variant: 'destructive' },
};

export function InterviewCard({ session, onSelect }: InterviewCardProps) {
  const status = statusConfig[session.status] ?? { label: session.status, variant: 'secondary' as const };

  return (
    <Card className="group cursor-pointer transition-all hover:shadow-md" onClick={() => onSelect(session.id)}>
      <CardHeader className="flex flex-row items-start justify-between space-y-0 pb-2">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <Target className="h-4 w-4 text-muted-foreground" />
            <h3 className="font-semibold leading-none">{session.targetRole}</h3>
          </div>
          {session.targetCompany && (
            <p className="text-sm text-muted-foreground">{session.targetCompany}</p>
          )}
        </div>
        <Badge variant={status.variant}>{status.label}</Badge>
      </CardHeader>
      <CardContent>
        <div className="flex items-center gap-4 text-sm text-muted-foreground">
          <span className="flex items-center gap-1">
            {session.mode === 'AUDIO' ? <Mic className="h-3.5 w-3.5" /> : <MessageSquare className="h-3.5 w-3.5" />}
            {session.mode === 'AUDIO' ? 'Audio' : 'Text'}
          </span>
          <span className="flex items-center gap-1">
            <Clock className="h-3.5 w-3.5" />
            {session.answeredQuestions}/{session.totalQuestions}
          </span>
          <span className="flex items-center gap-1">
            <Calendar className="h-3.5 w-3.5" />
            {new Date(session.createdAt).toLocaleDateString()}
          </span>
        </div>
        {session.score !== undefined && (
          <div className="mt-3 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-sm font-medium">Score:</span>
              <span className={cn(
                'text-lg font-bold',
                session.score >= 80 ? 'text-success' : session.score >= 60 ? 'text-warning' : 'text-destructive'
              )}>
                {session.score}%
              </span>
            </div>
            <Button variant="ghost" size="sm" className="gap-1 opacity-0 group-hover:opacity-100">
              View <ArrowRight className="h-3 w-3" />
            </Button>
          </div>
        )}
        {session.status === 'IN_PROGRESS' && (
          <div className="mt-3">
            <Button variant="outline" size="sm" className="w-full gap-1">
              Continue <ArrowRight className="h-3 w-3" />
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
