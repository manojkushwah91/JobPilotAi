'use client';

import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { FileText, Plus, Loader2, Download, Trash2, Sparkles } from 'lucide-react';
import { useApiQuery } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import { toast } from 'sonner';
import dayjs from 'dayjs';

export default function CoverLettersPage() {
  const [open, setOpen] = useState(false);
  const [generateOpen, setGenerateOpen] = useState(false);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [tone, setTone] = useState('professional');
  const [jobId, setJobId] = useState('');
  const [resumeId, setResumeId] = useState('');

  const { data: raw, isLoading, error, refetch } = useApiQuery(['cover-letters'], API.coverLetters.list);
  const letters: { id: string; title: string; content: string; tone?: string; createdAt: string }[] = (raw as any)?.data || [];

  if (isLoading) return <div className="flex items-center justify-center h-64"><Loader2 className="h-8 w-8 animate-spin" /></div>;
  if (error) return <div className="flex flex-col items-center justify-center h-64 gap-4"><p className="text-muted-foreground">Failed to load cover letters</p><Button onClick={() => refetch()}>Retry</Button></div>;

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1'}${API.coverLetters.list}`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title, content, tone }),
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Failed');
      toast.success('Cover letter created');
      setOpen(false); setTitle(''); setContent('');
      refetch();
    } catch { toast.error('Failed to create cover letter'); }
  }

  async function handleGenerate(e: React.FormEvent) {
    e.preventDefault();
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1'}${API.coverLetters.generate}`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ jobId, resumeId, tone }),
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Failed');
      toast.success('Cover letter generated!');
      setGenerateOpen(false);
      refetch();
    } catch { toast.error('Failed to generate cover letter'); }
  }

  async function handleDelete(id: string) {
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1'}${API.coverLetters.detail(id)}`, {
        method: 'DELETE', credentials: 'include',
      });
      if (!res.ok) throw new Error('Failed');
      toast.success('Cover letter deleted');
      refetch();
    } catch { toast.error('Failed to delete'); }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Cover Letters</h1>
          <p className="text-muted-foreground">Create, generate, and manage your cover letters</p>
        </div>
        <div className="flex gap-2">
          <Dialog open={generateOpen} onOpenChange={setGenerateOpen}>
            <DialogTrigger asChild><Button><Sparkles className="mr-2 h-4 w-4" /> Generate with AI</Button></DialogTrigger>
            <DialogContent>
              <form onSubmit={handleGenerate}>
                <DialogHeader><DialogTitle>Generate Cover Letter</DialogTitle><DialogDescription>AI will create a tailored cover letter for you</DialogDescription></DialogHeader>
                <div className="grid gap-4 py-4">
                  <div><Label>Job ID</Label><Input value={jobId} onChange={e => setJobId(e.target.value)} placeholder="Enter job ID" /></div>
                  <div><Label>Resume ID</Label><Input value={resumeId} onChange={e => setResumeId(e.target.value)} placeholder="Enter resume ID" /></div>
                  <div><Label>Tone</Label>
                    <Select value={tone} onValueChange={setTone}>
                      <SelectTrigger><SelectValue /></SelectTrigger>
                      <SelectContent><SelectItem value="professional">Professional</SelectItem><SelectItem value="friendly">Friendly</SelectItem><SelectItem value="enthusiastic">Enthusiastic</SelectItem><SelectItem value="formal">Formal</SelectItem></SelectContent>
                    </Select>
                  </div>
                </div>
                <DialogFooter><Button type="submit">Generate</Button></DialogFooter>
              </form>
            </DialogContent>
          </Dialog>
          <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild><Button variant="outline"><Plus className="mr-2 h-4 w-4" /> New Cover Letter</Button></DialogTrigger>
            <DialogContent>
              <form onSubmit={handleCreate}>
                <DialogHeader><DialogTitle>Create Cover Letter</DialogTitle><DialogDescription>Write a custom cover letter</DialogDescription></DialogHeader>
                <div className="grid gap-4 py-4">
                  <div><Label>Title</Label><Input value={title} onChange={e => setTitle(e.target.value)} placeholder="e.g. Software Engineer at Google" required /></div>
                  <div><Label>Content</Label><Textarea value={content} onChange={e => setContent(e.target.value)} rows={10} placeholder="Write your cover letter..." required /></div>
                  <div><Label>Tone</Label>
                    <Select value={tone} onValueChange={setTone}>
                      <SelectTrigger><SelectValue /></SelectTrigger>
                      <SelectContent><SelectItem value="professional">Professional</SelectItem><SelectItem value="friendly">Friendly</SelectItem><SelectItem value="enthusiastic">Enthusiastic</SelectItem><SelectItem value="formal">Formal</SelectItem></SelectContent>
                    </Select>
                  </div>
                </div>
                <DialogFooter><Button type="submit">Create</Button></DialogFooter>
              </form>
            </DialogContent>
          </Dialog>
        </div>
      </div>

      {letters.length === 0 ? (
        <div className="flex flex-col items-center justify-center h-64 gap-4 text-muted-foreground">
          <FileText className="h-12 w-12" />
          <p className="text-lg font-medium">No cover letters yet</p>
          <p>Create one manually or let AI generate it for you</p>
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {letters.map((letter) => (
            <Card key={letter.id} className="flex flex-col">
              <CardHeader>
                <CardTitle className="text-lg">{letter.title}</CardTitle>
                <CardDescription>{letter.tone ? `${letter.tone} tone` : 'No tone specified'} &middot; {dayjs(letter.createdAt).format('MMM D, YYYY')}</CardDescription>
              </CardHeader>
              <CardContent className="flex-1">
                <p className="text-sm text-muted-foreground line-clamp-4">{letter.content}</p>
              </CardContent>
              <CardFooter className="gap-2">
                <Button variant="outline" size="sm"><Download className="mr-2 h-4 w-4" /> Export PDF</Button>
                <Button variant="ghost" size="icon" className="ml-auto text-destructive" onClick={() => handleDelete(letter.id)}><Trash2 className="h-4 w-4" /></Button>
              </CardFooter>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
