'use client';

import { useState, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import apiClient from '@/lib/api/client';
import type { Resume } from '@/types';
import { ResumeCard } from '@/components/features/resumes/ResumeCard';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { toast } from 'sonner';
import { Plus, FileText, Search, Upload, Loader2, Sparkles } from 'lucide-react';

export default function ResumesPage() {
  const router = useRouter();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [search, setSearch] = useState('');
  const [createOpen, setCreateOpen] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [uploading, setUploading] = useState(false);

  const { data, isLoading, isError, refetch } = useApiQuery<Resume[]>(['resumes'], API.resumes.list);

  const createMutation = useApiMutation<Resume, { title: string }>('POST', API.resumes.list, {
    onSuccess: (res) => {
      toast.success('Resume created');
      setCreateOpen(false);
      setNewTitle('');
      router.push(`/resumes/${res.data.id}`);
    },
    onError: () => toast.error('Failed to create resume'),
  });

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await apiClient.post(API.resumes.uploadAndParse, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      const parsed = res.data?.data?.parsed;
      toast.success(`Resume parsed! Found ${parsed?.skills?.length || 0} skills, profile auto-populated.`);
      refetch();
      router.push('/settings/profile');
    } catch {
      toast.error('Failed to upload and parse resume');
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const resumes = data?.data ?? [];
  const filtered = resumes.filter((r) =>
    r.title.toLowerCase().includes(search.toLowerCase())
  );

  if (isError) {
    return (
      <div className="flex flex-col items-center justify-center py-20">
        <p className="text-destructive">Failed to load resumes</p>
        <Button variant="outline" className="mt-4" onClick={() => window.location.reload()}>
          Try Again
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-mesh p-6">
        <div className="relative z-10">
          <div className="flex items-center gap-2 mb-1">
            <Sparkles className="h-4 w-4 text-primary" />
            <span className="text-xs font-medium text-primary">Resume Builder</span>
          </div>
          <h1 className="text-3xl font-bold tracking-tight mb-1">Resumes</h1>
          <p className="text-muted-foreground">
            Manage your resumes and optimize them for ATS • {resumes.length} total
          </p>
        </div>
        <div className="absolute -right-16 -top-16 h-48 w-48 rounded-full bg-primary/5 blur-3xl" />
      </div>

      {/* Search & Actions Bar */}
      <div className="glass rounded-xl p-4">
        <div className="flex flex-col gap-3 sm:flex-row">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Search resumes..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="h-11 pl-10 bg-background/50 border-border/50 focus:border-primary/50"
            />
          </div>
          <div className="flex items-center gap-2">
            <input
              ref={fileInputRef}
              type="file"
              accept=".pdf,.docx,.doc,.txt"
              className="hidden"
              onChange={handleUpload}
            />
            <Button
              variant="outline"
              onClick={() => fileInputRef.current?.click()}
              disabled={uploading}
              className="h-11"
            >
              {uploading ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <Upload className="mr-2 h-4 w-4" />
              )}
              {uploading ? 'Parsing...' : 'Upload'}
            </Button>
            <Dialog open={createOpen} onOpenChange={setCreateOpen}>
              <DialogTrigger asChild>
                <Button className="h-11 bg-gradient-primary hover:opacity-90">
                  <Plus className="mr-2 h-4 w-4" />
                  Create
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Create New Resume</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                  <div>
                    <Label htmlFor="title">Resume Title</Label>
                    <Input
                      id="title"
                      placeholder="e.g. Software Engineer Resume"
                      value={newTitle}
                      onChange={(e) => setNewTitle(e.target.value)}
                      className="mt-1"
                    />
                  </div>
                  <div className="flex justify-end gap-2">
                    <Button variant="outline" onClick={() => setCreateOpen(false)}>Cancel</Button>
                    <Button
                      disabled={!newTitle.trim() || createMutation.isPending}
                      onClick={() => createMutation.mutate({ title: newTitle.trim() })}
                      className="bg-gradient-primary hover:opacity-90"
                    >
                      {createMutation.isPending ? 'Creating...' : 'Create'}
                    </Button>
                  </div>
                </div>
              </DialogContent>
            </Dialog>
          </div>
        </div>
      </div>

      {/* Resume Grid */}
      {isLoading ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="skeleton-premium h-40 rounded-xl" />
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 glass rounded-xl">
          <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-muted/50">
            <FileText className="h-8 w-8 text-muted-foreground" />
          </div>
          <h3 className="mb-2 text-lg font-medium">
            {search ? 'No resumes match your search' : 'No resumes yet'}
          </h3>
          <p className="mb-6 text-sm text-muted-foreground text-center max-w-sm">
            {search ? 'Try a different search term' : 'Create your first resume to get started'}
          </p>
          {!search && (
            <Button onClick={() => setCreateOpen(true)} className="bg-gradient-primary hover:opacity-90">
              <Plus className="mr-2 h-4 w-4" />
              Create your first resume
            </Button>
          )}
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 scrollbar-premium">
          {filtered.map((resume, i) => (
            <div key={resume.id} className="animate-fade-in" style={{ animationDelay: `${i * 50}ms` }}>
              <ResumeCard resume={resume} />
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
