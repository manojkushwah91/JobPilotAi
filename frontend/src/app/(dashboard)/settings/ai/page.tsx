'use client';

import { useState } from 'react';
import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Skeleton } from '@/components/ui/skeleton';
import { SettingsSidebar } from '@/components/features/settings/SettingsSidebar';
import { Cpu, RefreshCw, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import type { AiSettings } from '@/types';

const PROVIDER_MODELS: Record<string, string[]> = {
  OPENAI: ['gpt-4o', 'gpt-4o-mini', 'gpt-4-turbo', 'gpt-3.5-turbo'],
  ANTHROPIC: ['claude-3-opus', 'claude-3-sonnet', 'claude-3-haiku'],
  OLLAMA: ['llama3', 'mistral', 'codellama', 'mixtral'],
};

export default function AiSettingsPage() {
  const { data: res, isLoading, isError, refetch } = useApiQuery<AiSettings>(
    ['settings', 'ai'],
    API.settings.ai
  );

  const updateMutation = useApiMutation<AiSettings, AiSettings>('PUT', API.settings.ai, {
    onSuccess: () => {
      toast.success('AI settings updated');
      refetch();
    },
    onError: () => toast.error('Failed to update AI settings'),
  });

  const [form, setForm] = useState<AiSettings>({
    preferredProvider: 'OPENAI',
    model: 'gpt-4o',
  });
  const [initialized, setInitialized] = useState(false);

  if (isLoading) {
    return (
      <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
        <SettingsSidebar />
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-24" />
            <Skeleton className="h-4 w-48" />
          </CardHeader>
          <CardContent className="space-y-4">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </CardContent>
        </Card>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
        <SettingsSidebar />
        <Card>
          <CardContent className="flex flex-col items-center gap-4 py-12">
            <p className="text-destructive">Failed to load AI settings</p>
            <Button variant="outline" onClick={() => refetch()} className="gap-2">
              <RefreshCw className="h-4 w-4" />
              Retry
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const settings = res!.data;
  if (!initialized) {
    setForm({
      preferredProvider: settings.preferredProvider ?? 'OPENAI',
      model: settings.model ?? 'gpt-4o',
    });
    setInitialized(true);
  }

  const availableModels = PROVIDER_MODELS[form.preferredProvider] ?? [];

  const handleSave = () => {
    updateMutation.mutate(form);
  };

  return (
    <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
      <SettingsSidebar />
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Cpu className="h-5 w-5" />
            AI Settings
          </CardTitle>
          <CardDescription>Configure your AI provider and model preferences</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="provider">AI Provider</Label>
            <Select
              value={form.preferredProvider}
              onValueChange={(v: AiSettings['preferredProvider']) =>
                setForm({ ...form, preferredProvider: v, model: PROVIDER_MODELS[v]?.[0] ?? '' })
              }
            >
              <SelectTrigger id="provider">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="OPENAI">OpenAI</SelectItem>
                <SelectItem value="ANTHROPIC">Anthropic</SelectItem>
                <SelectItem value="OLLAMA">Ollama (Local)</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="model">Model</Label>
            <Select
              value={form.model}
              onValueChange={(v) => setForm({ ...form, model: v })}
            >
              <SelectTrigger id="model">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {availableModels.map((model) => (
                  <SelectItem key={model} value={model}>{model}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <Button onClick={handleSave} disabled={updateMutation.isPending} className="gap-2">
            {updateMutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
            Save Settings
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
