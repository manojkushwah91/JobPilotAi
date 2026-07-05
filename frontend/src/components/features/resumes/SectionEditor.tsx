'use client';

import { useState } from 'react';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import type { ResumeSection, SectionType } from '@/types';
import { cn } from '@/lib/utils/cn';
import { Plus, GripVertical, Trash2 } from 'lucide-react';

const SECTION_TYPES: { value: SectionType; label: string }[] = [
  { value: 'SUMMARY', label: 'Summary' },
  { value: 'EXPERIENCE', label: 'Experience' },
  { value: 'EDUCATION', label: 'Education' },
  { value: 'SKILLS', label: 'Skills' },
  { value: 'CERTIFICATIONS', label: 'Certifications' },
  { value: 'PROJECTS', label: 'Projects' },
];

const SECTION_LABELS: Record<SectionType, string> = {
  SUMMARY: 'Summary',
  EXPERIENCE: 'Experience',
  EDUCATION: 'Education',
  SKILLS: 'Skills',
  CERTIFICATIONS: 'Certifications',
  PROJECTS: 'Projects',
};

interface SectionEditorProps {
  sections: ResumeSection[];
  selectedId: string | null;
  onSelect: (id: string) => void;
  onAdd: (type: SectionType) => void;
  onRemove: (id: string) => void;
  onMoveUp: (id: string) => void;
  onMoveDown: (id: string) => void;
  onContentChange: (id: string, content: string) => void;
  onTitleChange: (id: string, title: string) => void;
}

export function SectionEditor({
  sections,
  selectedId,
  onSelect,
  onAdd,
  onRemove,
  onMoveUp,
  onMoveDown,
  onContentChange,
  onTitleChange,
}: SectionEditorProps) {
  const [addingType, setAddingType] = useState<SectionType | null>(null);
  const selected = sections.find((s) => s.id === selectedId);

  const availableTypes = SECTION_TYPES.filter(
    (st) => !sections.some((s) => s.type === st.value)
  );

  return (
    <div className="flex h-full gap-4">
      <div className="w-64 shrink-0 space-y-2">
        <div className="mb-3 flex items-center justify-between">
          <h3 className="text-sm font-medium">Sections</h3>
          <div className="relative">
            <Button
              variant="outline"
              size="sm"
              disabled={availableTypes.length === 0}
              onClick={() => setAddingType(addingType ? null : availableTypes[0]?.value ?? null)}
            >
              <Plus className="mr-1 h-3.5 w-3.5" />
              Add
            </Button>
            {addingType && (
              <Card className="absolute right-0 top-full z-10 mt-1 w-44">
                <CardContent className="p-1">
                  {availableTypes.map((t) => (
                    <button
                      key={t.value}
                      className="w-full rounded-sm px-2 py-1.5 text-left text-sm hover:bg-accent"
                      onClick={() => {
                        onAdd(t.value);
                        setAddingType(null);
                      }}
                    >
                      {t.label}
                    </button>
                  ))}
                </CardContent>
              </Card>
            )}
          </div>
        </div>
        <div className="space-y-1">
          {sections
            .sort((a, b) => a.order - b.order)
            .map((section, idx) => (
              <div
                key={section.id}
                className={cn(
                  'group flex items-center gap-1 rounded-md border px-2 py-1.5 text-sm cursor-pointer transition-colors',
                  selectedId === section.id
                    ? 'border-primary bg-primary/5'
                    : 'border-transparent hover:bg-accent'
                )}
                onClick={() => onSelect(section.id)}
              >
                <GripVertical className="h-3.5 w-3.5 shrink-0 text-muted-foreground" />
                <span className="flex-1 truncate">
                  {SECTION_LABELS[section.type]}
                </span>
                <div className="flex shrink-0 gap-0.5 opacity-0 group-hover:opacity-100">
                  <button
                    disabled={idx === 0}
                    className="rounded p-0.5 text-muted-foreground hover:text-foreground disabled:opacity-30"
                    onClick={(e) => { e.stopPropagation(); onMoveUp(section.id); }}
                    title="Move up"
                  >
                    <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 15l7-7 7 7" /></svg>
                  </button>
                  <button
                    disabled={idx === sections.length - 1}
                    className="rounded p-0.5 text-muted-foreground hover:text-foreground disabled:opacity-30"
                    onClick={(e) => { e.stopPropagation(); onMoveDown(section.id); }}
                    title="Move down"
                  >
                    <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" /></svg>
                  </button>
                  <button
                    className="rounded p-0.5 text-destructive hover:text-destructive"
                    onClick={(e) => { e.stopPropagation(); onRemove(section.id); }}
                    title="Remove section"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                </div>
              </div>
            ))}
          {sections.length === 0 && (
            <p className="py-4 text-center text-sm text-muted-foreground">
              No sections yet. Click &quot;Add&quot; to begin.
            </p>
          )}
        </div>
      </div>

      <div className="flex-1">
        {selected ? (
          <div className="space-y-4">
            <div>
              <Label htmlFor="section-label">Section Type</Label>
              <Input id="section-label" value={SECTION_LABELS[selected.type]} disabled className="mt-1" />
            </div>
            <div>
              <Label htmlFor="section-content">Content</Label>
              <Textarea
                id="section-content"
                value={selected.content}
                onChange={(e) => onContentChange(selected.id, e.target.value)}
                className="mt-1 min-h-[300px]"
                placeholder={`Enter your ${SECTION_LABELS[selected.type].toLowerCase()} content here...`}
              />
            </div>
          </div>
        ) : (
          <div className="flex h-full items-center justify-center">
            <p className="text-sm text-muted-foreground">Select a section to edit its content</p>
          </div>
        )}
      </div>
    </div>
  );
}
