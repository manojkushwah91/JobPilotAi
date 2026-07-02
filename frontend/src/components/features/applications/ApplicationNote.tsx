'use client';

import type { ApplicationNote } from '@/types';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { cn } from '@/lib/utils/cn';
import { Edit3, Trash2, FileText } from 'lucide-react';

interface ApplicationNoteProps {
  note: ApplicationNote;
  onEdit?: (note: ApplicationNote) => void;
  onDelete?: (id: string) => void;
}

function getCategoryColor(category: string) {
  switch (category?.toLowerCase()) {
    case 'interview': return 'border-l-blue-500 bg-blue-50/50';
    case 'follow-up': return 'border-l-amber-500 bg-amber-50/50';
    case 'research': return 'border-l-purple-500 bg-purple-50/50';
    case 'offer': return 'border-l-green-500 bg-green-50/50';
    default: return 'border-l-gray-300';
  }
}

export function ApplicationNoteCard({ note, onEdit, onDelete }: ApplicationNoteProps) {
  return (
    <Card className={cn('border-l-4', getCategoryColor(note.category))}>
      <CardContent className="flex items-start justify-between p-3">
        <div className="flex-1">
          <div className="mb-1 flex items-center gap-2">
            <FileText className="h-3.5 w-3.5 text-muted-foreground" />
            {note.category && (
              <span className="text-[10px] font-medium uppercase text-muted-foreground">
                {note.category}
              </span>
            )}
            <span className="text-[10px] text-muted-foreground">
              {new Date(note.createdAt).toLocaleDateString()}
            </span>
          </div>
          <p className="text-sm">{note.content}</p>
        </div>
        <div className="ml-2 flex shrink-0 gap-1">
          {onEdit && (
            <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => onEdit(note)}>
              <Edit3 className="h-3.5 w-3.5" />
            </Button>
          )}
          {onDelete && (
            <Button variant="ghost" size="icon" className="h-7 w-7 text-destructive" onClick={() => onDelete(note.id)}>
              <Trash2 className="h-3.5 w-3.5" />
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
