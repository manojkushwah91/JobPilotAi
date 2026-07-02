'use client';

import { Check, Loader2 } from 'lucide-react';

const STEPS = [
  { key: 'starting_browser', label: 'Start Browser' },
  { key: 'navigating', label: 'Navigate' },
  { key: 'filling_form', label: 'Fill Form' },
  { key: 'uploading_resume', label: 'Upload Resume' },
  { key: 'reviewing', label: 'Review' },
  { key: 'submitting', label: 'Submit' },
];

interface AutomationProgressProps {
  currentStep?: string;
  progress: number;
  status: string;
}

export function AutomationProgress({ currentStep, progress, status }: AutomationProgressProps) {
  const currentIndex = STEPS.findIndex((s) => s.key === currentStep);

  return (
    <div className="space-y-2">
      {STEPS.map((step, index) => {
        const isCompleted = index < currentIndex;
        const isCurrent = index === currentIndex;
        const isPending = index > currentIndex;

        return (
          <div key={step.key} className="flex items-center gap-3">
            <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full border text-sm">
              {isCompleted ? (
                <Check className="h-4 w-4 text-green-600" />
              ) : isCurrent ? (
                <Loader2 className="h-4 w-4 animate-spin text-blue-600" />
              ) : (
                <span className="text-xs text-muted-foreground">{index + 1}</span>
              )}
            </div>
            <span
              className={`text-sm ${
                isCompleted
                  ? 'font-medium text-green-600'
                  : isCurrent
                    ? 'font-medium text-blue-600'
                    : isPending
                      ? 'text-muted-foreground'
                      : ''
              }`}
            >
              {step.label}
            </span>
          </div>
        );
      })}
    </div>
  );
}
