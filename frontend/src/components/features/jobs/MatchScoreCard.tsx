'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { cn } from '@/lib/utils/cn';
import { CheckCircle, XCircle } from 'lucide-react';

interface MatchScoreCardProps {
  matchScore: number;
  matchedSkills: string[];
  missingSkills: string[];
  isLoading?: boolean;
}

function getScoreColor(score: number) {
  if (score >= 80) return { color: '#22c55e', text: 'text-success' };
  if (score >= 60) return { color: '#eab308', text: 'text-warning' };
  return { color: '#ef4444', text: 'text-destructive' };
}

export function MatchScoreCard({ matchScore, matchedSkills, missingSkills, isLoading }: MatchScoreCardProps) {
  const { color, text } = getScoreColor(matchScore);

  return (
    <Card>
      <CardHeader className="pb-3">
        <CardTitle className="text-sm">Match Score</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex flex-col items-center">
          <div className="relative mb-2 flex h-24 w-24 items-center justify-center">
            <svg className="absolute inset-0 h-full w-full -rotate-90" viewBox="0 0 100 100">
              <circle cx="50" cy="50" r="42" fill="none" stroke="hsl(var(--muted))" strokeWidth="8" />
              <circle
                cx="50" cy="50" r="42" fill="none" stroke={color} strokeWidth="8"
                strokeDasharray={`${(matchScore / 100) * 264} 264`}
                strokeLinecap="round"
              />
            </svg>
            <span className={cn('text-2xl font-bold', text)}>{matchScore}%</span>
          </div>
          <Progress value={matchScore} className="h-2 w-full" />
        </div>

        {matchedSkills.length > 0 && (
          <div>
            <h4 className="mb-2 flex items-center gap-1 text-xs font-medium text-success">
              <CheckCircle className="h-3 w-3" /> Matched Skills
            </h4>
            <div className="flex flex-wrap gap-1">
              {matchedSkills.map((s) => (
                <Badge key={s} variant="success" className="text-[10px]">{s}</Badge>
              ))}
            </div>
          </div>
        )}

        {missingSkills.length > 0 && (
          <div>
            <h4 className="mb-2 flex items-center gap-1 text-xs font-medium text-destructive">
              <XCircle className="h-3 w-3" /> Missing Skills
            </h4>
            <div className="flex flex-wrap gap-1">
              {missingSkills.map((s) => (
                <Badge key={s} variant="destructive" className="text-[10px]">{s}</Badge>
              ))}
            </div>
          </div>
        )}

        {matchedSkills.length === 0 && missingSkills.length === 0 && (
          <p className="text-center text-xs text-muted-foreground">
            Run AI match analysis to see your skill matches
          </p>
        )}
      </CardContent>
    </Card>
  );
}
