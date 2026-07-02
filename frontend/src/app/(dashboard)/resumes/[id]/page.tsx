'use client';

import { useState, useEffect, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import type { Resume, ResumeSection, SectionType, AtsScore } from '@/types';
import { SectionEditor } from '@/components/features/resumes/SectionEditor';
import { AtsScorePanel } from '@/components/features/resumes/AtsScorePanel';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Skeleton } from '@/components/ui/skeleton';
import { toast } from 'sonner';
import { ArrowLeft, Save, Download, Sparkles } from 'lucide-react';

export default function ResumeDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id as string;

  const [title, setTitle] = useState('');
  const [sections, setSections] = useState<ResumeSection[]>([]);
  const [selectedSectionId, setSelectedSectionId] = useState<string | null>(null);
  const [savedAt, setSavedAt] = useState<string | null>(null);
  const [dirty, setDirty] = useState(false);

  const { data, isLoading, isError } = useApiQuery<Resume>(['resume', id], API.resumes.detail(id));

  useEffect(() => {
    if (data?.data) {
      setTitle(data.data.title);
      setSections(data.data.sections ?? []);
      setSelectedSectionId((data.data.sections?.[0]?.id) ?? null);
      setSavedAt(data.data.updatedAt);
    }
  }, [data]);

  const saveMutation = useApiMutation<Resume, { title: string; sections: ResumeSection[] }>('PUT', API.resumes.detail(id), {
    onSuccess: (res) => {
      toast.success('Resume saved');
      setDirty(false);
      setSavedAt(res.data.updatedAt);
    },
    onError: () => toast.error('Failed to save resume'),
  });

  const scoreMutation = useApiMutation<AtsScore, void>('POST', API.resumes.score(id), {
    onSuccess: () => {
      toast.success('Resume scored successfully');
    },
    onError: () => toast.error('Failed to score resume'),
  });

  const handleSave = useCallback(() => {
    saveMutation.mutate({ title, sections });
  }, [title, sections, saveMutation]);

  const handleScore = useCallback(() => {
    scoreMutation.mutate();
  }, [scoreMutation]);

  const handleAddSection = useCallback((type: SectionType) => {
    const newSection: ResumeSection = {
      id: `new-${Date.now()}`,
      type,
      content: '',
      order: sections.length,
    };
    setSections((prev) => [...prev, newSection]);
    setSelectedSectionId(newSection.id);
    setDirty(true);
  }, [sections]);

  const handleRemoveSection = useCallback((sectionId: string) => {
    setSections((prev) => {
      const updated = prev.filter((s) => s.id !== sectionId).map((s, i) => ({ ...s, order: i }));
      if (selectedSectionId === sectionId) {
        setSelectedSectionId(updated[0]?.id ?? null);
      }
      return updated;
    });
    setDirty(true);
  }, [selectedSectionId]);

  const handleMoveUp = useCallback((sectionId: string) => {
    setSections((prev) => {
      const idx = prev.findIndex((s) => s.id === sectionId);
      if (idx <= 0) return prev;
      const next = [...prev];
      [next[idx - 1].order, next[idx].order] = [next[idx].order, next[idx - 1].order];
      next.sort((a, b) => a.order - b.order);
      return next;
    });
    setDirty(true);
  }, []);

  const handleMoveDown = useCallback((sectionId: string) => {
    setSections((prev) => {
      const idx = prev.findIndex((s) => s.id === sectionId);
      if (idx < 0 || idx >= prev.length - 1) return prev;
      const next = [...prev];
      [next[idx].order, next[idx + 1].order] = [next[idx + 1].order, next[idx].order];
      next.sort((a, b) => a.order - b.order);
      return next;
    });
    setDirty(true);
  }, []);

  const handleContentChange = useCallback((sectionId: string, content: string) => {
    setSections((prev) => prev.map((s) => (s.id === sectionId ? { ...s, content } : s)));
    setDirty(true);
  }, []);

  const handleTitleChange = useCallback((sectionId: string, sectionTitle: string) => {
  }, []);

  const atsScore = data?.data?.atsScore;

  if (isLoading) {
    return (
      <div className="p-6">
        <div className="mb-6 flex items-center gap-4">
          <Skeleton className="h-9 w-9 rounded" />
          <Skeleton className="h-8 w-64" />
        </div>
        <div className="flex gap-6">
          <Skeleton className="h-[500px] w-64 rounded-xl" />
          <Skeleton className="h-[500px] flex-1 rounded-xl" />
          <Skeleton className="h-[500px] w-80 rounded-xl" />
        </div>
      </div>
    );
  }

  if (isError || !data?.data) {
    return (
      <div className="flex flex-col items-center justify-center py-20">
        <p className="text-destructive">Failed to load resume</p>
        <Button variant="outline" className="mt-4" onClick={() => router.push('/resumes')}>
          Back to Resumes
        </Button>
      </div>
    );
  }

  return (
    <div className="p-6">
      <div className="mb-6 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="icon" onClick={() => router.push('/resumes')}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div className="flex items-center gap-3">
            <Input
              value={title}
              onChange={(e) => { setTitle(e.target.value); setDirty(true); }}
              className="h-8 w-64 border-0 bg-transparent text-lg font-bold focus-visible:bg-background"
            />
            {savedAt && (
              <span className="text-xs text-muted-foreground">
                Saved {new Date(savedAt).toLocaleString()}
              </span>
            )}
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={handleScore} disabled={scoreMutation.isPending}>
            <Sparkles className="mr-1.5 h-4 w-4" />
            {scoreMutation.isPending ? 'Scoring...' : 'Score with AI'}
          </Button>
          <Button variant="outline" size="sm" asChild>
            <a href={API.resumes.export(id, 'pdf')} target="_blank" rel="noopener noreferrer">
              <Download className="mr-1.5 h-4 w-4" />
              Export PDF
            </a>
          </Button>
          <Button size="sm" onClick={handleSave} disabled={!dirty || saveMutation.isPending}>
            <Save className="mr-1.5 h-4 w-4" />
            {saveMutation.isPending ? 'Saving...' : 'Save'}
          </Button>
        </div>
      </div>

      <div className="flex gap-6">
        <div className="flex-1">
          <SectionEditor
            sections={sections}
            selectedId={selectedSectionId}
            onSelect={setSelectedSectionId}
            onAdd={handleAddSection}
            onRemove={handleRemoveSection}
            onMoveUp={handleMoveUp}
            onMoveDown={handleMoveDown}
            onContentChange={handleContentChange}
            onTitleChange={handleTitleChange}
          />
        </div>
        <div className="w-80 shrink-0">
          <AtsScorePanel
            score={atsScore}
            isScoring={scoreMutation.isPending}
            onScore={handleScore}
          />
        </div>
      </div>
    </div>
  );
}
