'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import type { InterviewFeedback } from '@/types';
import { cn } from '@/lib/utils/cn';

interface ScoreSummaryProps {
  feedback: InterviewFeedback;
}

export function ScoreSummary({ feedback }: ScoreSummaryProps) {
  const categoryData = Object.entries(feedback.categoryScores).map(([name, score]) => ({
    name,
    score,
    fill: score >= 80 ? 'hsl(var(--success))' : score >= 60 ? 'hsl(var(--warning))' : 'hsl(var(--destructive))',
  }));

  return (
    <div className="space-y-6">
      <Card className="text-center">
        <CardContent className="p-8">
          <div className="mb-2 text-sm font-medium text-muted-foreground">Overall Score</div>
          <div className={cn(
            'text-5xl font-bold',
            feedback.overallScore >= 80 ? 'text-success' : feedback.overallScore >= 60 ? 'text-warning' : 'text-destructive'
          )}>
            {feedback.overallScore}%
          </div>
          <p className="mt-2 text-muted-foreground">{feedback.summary}</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Category Scores</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={categoryData} margin={{ top: 10, right: 10, left: 0, bottom: 20 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                <YAxis domain={[0, 100]} tick={{ fontSize: 12 }} />
                <Tooltip
                  contentStyle={{
                    background: 'hsl(var(--popover))',
                    border: '1px solid hsl(var(--border))',
                    borderRadius: 'var(--radius)',
                  }}
                  formatter={(value: number) => [`${value}%`, 'Score']}
                />
                <Bar dataKey="score" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Strengths</CardTitle>
          </CardHeader>
          <CardContent>
            {feedback.strengths.length > 0 ? (
              <ul className="space-y-2">
                {feedback.strengths.map((s, i) => (
                  <li key={i} className="flex items-start gap-2">
                    <Badge variant="success" className="mt-0.5 shrink-0">+</Badge>
                    <span className="text-sm">{s}</span>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-sm text-muted-foreground">No specific strengths identified.</p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Areas to Improve</CardTitle>
          </CardHeader>
          <CardContent>
            {feedback.weaknesses.length > 0 ? (
              <ul className="space-y-2">
                {feedback.weaknesses.map((w, i) => (
                  <li key={i} className="flex items-start gap-2">
                    <Badge variant="destructive" className="mt-0.5 shrink-0">-</Badge>
                    <span className="text-sm">{w}</span>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-sm text-muted-foreground">No areas for improvement identified.</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
