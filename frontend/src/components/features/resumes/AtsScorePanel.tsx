'use client';

import { PieChart, Pie, Cell, ResponsiveContainer } from 'recharts';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { Skeleton } from '@/components/ui/skeleton';
import { cn } from '@/lib/utils/cn';
import type { AtsScore } from '@/types';
import { Sparkles, AlertTriangle, CheckCircle, XCircle } from 'lucide-react';

interface AtsScorePanelProps {
  score: AtsScore | null | undefined;
  isScoring: boolean;
  onScore: () => void;
}

function getScoreColor(score: number) {
  if (score >= 80) return { color: '#22c55e', label: 'Excellent' };
  if (score >= 60) return { color: '#eab308', label: 'Good' };
  if (score >= 40) return { color: '#f97316', label: 'Fair' };
  return { color: '#ef4444', label: 'Poor' };
}

const SEVERITY_COLORS: Record<string, string> = {
  CRITICAL: 'destructive',
  MAJOR: 'warning',
  MINOR: 'default',
};

export function AtsScorePanel({ score, isScoring, onScore }: AtsScorePanelProps) {
  if (!score) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="text-sm">ATS Score</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col items-center gap-3 py-6">
            <Sparkles className="h-8 w-8 text-muted-foreground" />
            <p className="text-center text-sm text-muted-foreground">
              Score your resume against job descriptions
            </p>
            <Button size="sm" onClick={onScore} disabled={isScoring}>
              {isScoring ? 'Scoring...' : 'Score with AI'}
            </Button>
          </div>
        </CardContent>
      </Card>
    );
  }

  const { color, label } = getScoreColor(score.overallScore);
  const keywordEntries = Object.entries(score.keywordMatches ?? {});
  const missingKeywords = score.missingKeywords ?? [];
  const suggestions = score.suggestions ?? [];

  const gaugeData = [
    { name: 'Score', value: score.overallScore },
    { name: 'Remaining', value: 100 - score.overallScore },
  ];

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-sm">ATS Score</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col items-center">
            <div className="relative h-32 w-32">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={gaugeData}
                    cx="50%"
                    cy="50%"
                    innerRadius={35}
                    outerRadius={55}
                    startAngle={180}
                    endAngle={0}
                    dataKey="value"
                    strokeWidth={0}
                  >
                    <Cell fill={color} />
                    <Cell fill="hsl(var(--muted))" />
                  </Pie>
                </PieChart>
              </ResponsiveContainer>
              <div className="absolute inset-0 flex flex-col items-center justify-center">
                <span className="text-2xl font-bold" style={{ color }}>{score.overallScore}</span>
                <span className="text-xs text-muted-foreground">{label}</span>
              </div>
            </div>
            <Button size="sm" variant="outline" onClick={onScore} disabled={isScoring} className="mt-2">
              <Sparkles className="mr-1 h-3.5 w-3.5" />
              {isScoring ? 'Scoring...' : 'Re-score'}
            </Button>
          </div>
        </CardContent>
      </Card>

      {keywordEntries.length > 0 && (
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm">Keyword Matches</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            {keywordEntries.map(([keyword, count]) => (
              <div key={keyword} className="flex items-center gap-2 text-sm">
                <CheckCircle className="h-3.5 w-3.5 shrink-0 text-success" />
                <span className="flex-1">{keyword}</span>
                <span className="text-muted-foreground">x{count}</span>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      {missingKeywords.length > 0 && (
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm">Missing Keywords</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-wrap gap-1.5">
            {missingKeywords.map((kw) => (
              <Badge key={kw} variant="destructive" className="text-xs">{kw}</Badge>
            ))}
          </CardContent>
        </Card>
      )}

      {suggestions.length > 0 && (
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm">Suggestions</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            {suggestions.map((s, i) => (
              <div key={i} className="rounded-lg border p-3 text-sm">
                <div className="mb-1 flex items-center gap-2">
                  <Badge variant={SEVERITY_COLORS[s.severity] as 'destructive' | 'warning' | 'default'} className="text-[10px] uppercase">
                    {s.severity}
                  </Badge>
                  <span className="font-medium">{s.category}</span>
                </div>
                <p className="text-muted-foreground">{s.message}</p>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      {!score && isScoring && (
        <div className="space-y-3">
          <Skeleton className="h-32 w-full rounded-lg" />
          <Skeleton className="h-20 w-full rounded-lg" />
          <Skeleton className="h-20 w-full rounded-lg" />
        </div>
      )}
    </div>
  );
}
