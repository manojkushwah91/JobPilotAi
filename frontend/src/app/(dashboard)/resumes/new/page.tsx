'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import type { Resume } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent } from '@/components/ui/card';
import { toast } from 'sonner';
import { cn } from '@/lib/utils/cn';
import { ArrowLeft, Check, FileText } from 'lucide-react';

const TEMPLATES = [
  { id: 'professional', name: 'Professional', description: 'Clean and classic' },
  { id: 'modern', name: 'Modern', description: 'Bold and contemporary' },
  { id: 'minimal', name: 'Minimal', description: 'Simple and elegant' },
  { id: 'creative', name: 'Creative', description: 'Unique and expressive' },
];

const STEPS = ['Template', 'Details'];

export default function NewResumePage() {
  const router = useRouter();
  const [step, setStep] = useState(0);
  const [title, setTitle] = useState('');
  const [selectedTemplate, setSelectedTemplate] = useState('');

  const createMutation = useApiMutation<Resume, { title: string; template: string }>('POST', API.resumes.list, {
    onSuccess: (res) => {
      toast.success('Resume created');
      router.push(`/resumes/${res.data.id}`);
    },
    onError: () => toast.error('Failed to create resume'),
  });

  const canProceed = step === 0 ? selectedTemplate : title.trim();

  const handleCreate = () => {
    if (!canProceed) return;
    if (step === 0) {
      setStep(1);
    } else {
      createMutation.mutate({ title: title.trim(), template: selectedTemplate });
    }
  };

  return (
    <div className="mx-auto max-w-3xl p-6">
      <button
        onClick={() => router.back()}
        className="mb-6 flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" />
        Back
      </button>

      <div className="mb-8">
        <h1 className="text-2xl font-bold">Create New Resume</h1>
        <p className="text-sm text-muted-foreground">Choose a template and name your resume</p>
      </div>

      <div className="mb-8 flex items-center gap-2">
        {STEPS.map((s, i) => (
          <div key={s} className="flex items-center gap-2">
            <div
              className={cn(
                'flex h-8 w-8 items-center justify-center rounded-full text-sm font-medium transition-colors',
                i < step ? 'bg-primary text-primary-foreground' : i === step ? 'bg-primary/20 text-primary' : 'bg-muted text-muted-foreground'
              )}
            >
              {i < step ? <Check className="h-4 w-4" /> : i + 1}
            </div>
            <span className={cn('text-sm', i === step ? 'font-medium text-foreground' : 'text-muted-foreground')}>
              {s}
            </span>
            {i < STEPS.length - 1 && <div className="h-px w-8 bg-border" />}
          </div>
        ))}
      </div>

      {step === 0 ? (
        <div className="grid gap-4 sm:grid-cols-2">
          {TEMPLATES.map((t) => (
            <Card
              key={t.id}
              className={cn(
                'cursor-pointer transition-all hover:shadow-md',
                selectedTemplate === t.id && 'ring-2 ring-primary'
              )}
              onClick={() => setSelectedTemplate(t.id)}
            >
              <CardContent className="p-4">
                <div className="mb-3 flex aspect-[3/4] items-center justify-center rounded-lg bg-muted">
                  <FileText className="h-12 w-12 text-muted-foreground" />
                </div>
                <h3 className="font-medium">{t.name}</h3>
                <p className="text-xs text-muted-foreground">{t.description}</p>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : (
        <div className="space-y-4">
          <div>
            <Label htmlFor="title">Resume Title</Label>
            <Input
              id="title"
              placeholder="e.g. Software Engineer Resume 2024"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="mt-1"
            />
            <p className="mt-1 text-xs text-muted-foreground">
              Give your resume a name to easily identify it later
            </p>
          </div>
        </div>
      )}

      <div className="mt-8 flex justify-end gap-2">
        {step > 0 && (
          <Button variant="outline" onClick={() => setStep(step - 1)}>
            Back
          </Button>
        )}
        <Button onClick={handleCreate} disabled={!canProceed || createMutation.isPending}>
          {createMutation.isPending ? 'Creating...' : step === 0 ? 'Next' : 'Create Resume'}
        </Button>
      </div>
    </div>
  );
}
