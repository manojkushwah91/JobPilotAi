import Link from 'next/link';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils/cn';
import type { Resume } from '@/types';
import { FileText, Clock } from 'lucide-react';

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function getScoreColor(score: number) {
  if (score >= 80) return 'text-success border-success/30 bg-success/10';
  if (score >= 60) return 'text-warning border-warning/30 bg-warning/10';
  return 'text-destructive border-destructive/30 bg-destructive/10';
}

interface ResumeCardProps {
  resume: Resume;
}

export function ResumeCard({ resume }: ResumeCardProps) {
  const score = resume.atsScore?.overallScore ?? 0;
  const sectionCount = resume.sections?.length ?? 0;

  return (
    <Link href={`/resumes/${resume.id}`}>
      <Card className="h-full cursor-pointer transition-shadow hover:shadow-md">
        <CardHeader className="pb-3">
          <div className="flex items-start justify-between">
            <CardTitle className="text-base">{resume.title}</CardTitle>
            {score > 0 && (
              <Badge variant="outline" className={cn('ml-2 shrink-0', getScoreColor(score))}>
                {score}/100
              </Badge>
            )}
          </div>
        </CardHeader>
        <CardContent className="pb-2">
          <div className="flex items-center gap-4 text-sm text-muted-foreground">
            <span className="flex items-center gap-1">
              <FileText className="h-3.5 w-3.5" />
              {sectionCount} section{sectionCount !== 1 ? 's' : ''}
            </span>
            <span className="flex items-center gap-1">
              <Clock className="h-3.5 w-3.5" />
              {formatDate(resume.updatedAt)}
            </span>
          </div>
        </CardContent>
        <CardFooter className="pt-1">
          <p className="text-xs text-muted-foreground">
            Created {formatDate(resume.createdAt)}
          </p>
        </CardFooter>
      </Card>
    </Link>
  );
}
