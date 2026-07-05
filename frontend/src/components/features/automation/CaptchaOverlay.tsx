'use client';

import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { AlertTriangle, Loader2 } from 'lucide-react';

interface CaptchaOverlayProps {
  sessionId: string;
  jobTitle: string;
  onSolved: () => void;
}

export function CaptchaOverlay({ sessionId, jobTitle, onSolved }: CaptchaOverlayProps) {
  const [solution, setSolution] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async () => {
    if (!solution.trim()) return;
    setSubmitting(true);
    setError('');

    try {
      const response = await fetch('/api/v1/agent/automate/captcha/solve', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sessionId, solution: solution.trim() }),
      });

      if (response.ok) {
        setSolution('');
        onSolved();
      } else {
        setError('Failed to submit solution');
      }
    } catch (err) {
      setError('Network error');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
      <Card className="w-full max-w-md mx-4 border-yellow-500">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-yellow-600">
            <AlertTriangle className="h-5 w-5" />
            CAPTCHA Detected
          </CardTitle>
          <CardDescription>
            Manual intervention required for: {jobTitle}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-sm text-muted-foreground">
            A CAPTCHA was detected during automated application. Please solve it manually
            in the browser window, then enter the solution below (or click Skip to skip this job).
          </p>

          <div className="space-y-2">
            <Input
              type="text"
              placeholder="Enter CAPTCHA solution (if applicable)"
              value={solution}
              onChange={(e) => setSolution(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
            />
            {error && <p className="text-xs text-red-500">{error}</p>}
          </div>

          <div className="flex gap-2">
            <Button
              onClick={handleSubmit}
              disabled={submitting || !solution.trim()}
              className="flex-1"
            >
              {submitting ? (
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
              ) : null}
              Submit Solution
            </Button>
            <Button
              variant="outline"
              onClick={onSolved}
              disabled={submitting}
            >
              Skip
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
