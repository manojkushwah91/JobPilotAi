'use client';

import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import type { InterviewQuestion } from '@/types';
import { cn } from '@/lib/utils/cn';

interface QuestionDisplayProps {
  question: InterviewQuestion;
  questionNumber: number;
  totalQuestions: number;
}

const difficultyConfig = {
  EASY: { label: 'Easy', variant: 'success' as const },
  MEDIUM: { label: 'Medium', variant: 'warning' as const },
  HARD: { label: 'Hard', variant: 'destructive' as const },
};

export function QuestionDisplay({ question, questionNumber, totalQuestions }: QuestionDisplayProps) {
  const difficulty = difficultyConfig[question.difficulty];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <span className="text-sm text-muted-foreground">
          Question {questionNumber} of {totalQuestions}
        </span>
        <div className="flex gap-2">
          <Badge variant="secondary">{question.category}</Badge>
          <Badge variant={difficulty.variant}>{difficulty.label}</Badge>
        </div>
      </div>
      <Card className={cn('border-l-4', question.difficulty === 'HARD' ? 'border-l-destructive' : question.difficulty === 'MEDIUM' ? 'border-l-warning' : 'border-l-success')}>
        <CardContent className="p-6">
          <p className="text-lg font-medium leading-relaxed">{question.question}</p>
        </CardContent>
      </Card>
      {question.feedback && (
        <Card className="bg-muted/50">
          <CardContent className="p-4">
            <p className="text-sm text-muted-foreground">
              <span className="font-medium text-foreground">Feedback: </span>
              {question.feedback}
            </p>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
