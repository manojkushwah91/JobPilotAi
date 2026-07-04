'use client';

import { useState, useEffect } from 'react';
import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Switch } from '@/components/ui/switch';
import { Skeleton } from '@/components/ui/skeleton';
import { SettingsSidebar } from '@/components/features/settings/SettingsSidebar';
import { X, Plus, RefreshCw, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import type { JobPreferences } from '@/types';

const EMPLOYMENT_TYPES = ['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'FREELANCE'];

export default function PreferencesSettingsPage() {
  const { data: res, isLoading, isError, refetch } = useApiQuery<JobPreferences>(
    ['settings', 'jobPreferences'],
    API.settings.jobPreferences
  );

  const updateMutation = useApiMutation<JobPreferences, Partial<JobPreferences>>(
    'PUT',
    API.settings.jobPreferences,
    {
      onSuccess: () => {
        toast.success('Preferences saved');
        refetch();
      },
      onError: () => toast.error('Failed to save preferences'),
    }
  );

  const [form, setForm] = useState<JobPreferences>({
    desiredRoles: [],
    preferredLocations: [],
    remotePreference: false,
    employmentTypes: [],
  });
  const [roleInput, setRoleInput] = useState('');
  const [locationInput, setLocationInput] = useState('');
  const [initialized, setInitialized] = useState(false);

  if (isLoading) {
    return (
      <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
        <SettingsSidebar />
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-40" />
            <Skeleton className="h-4 w-56" />
          </CardHeader>
          <CardContent className="space-y-4">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-6 w-24" />
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
            <p className="text-destructive">Failed to load preferences</p>
            <Button variant="outline" onClick={() => refetch()} className="gap-2">
              <RefreshCw className="h-4 w-4" />
              Retry
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const prefs = res!.data;
  useEffect(() => {
    if (!initialized && prefs) {
      setForm({
        desiredRoles: prefs.desiredRoles ?? [],
        preferredLocations: prefs.preferredLocations ?? [],
        remotePreference: prefs.remotePreference ?? false,
        employmentTypes: prefs.employmentTypes ?? [],
      });
      setInitialized(true);
    }
  }, [prefs, initialized]);

  const addRole = () => {
    const trimmed = roleInput.trim();
    if (trimmed && !form.desiredRoles.includes(trimmed)) {
      setForm({ ...form, desiredRoles: [...form.desiredRoles, trimmed] });
    }
    setRoleInput('');
  };

  const removeRole = (role: string) => {
    setForm({ ...form, desiredRoles: form.desiredRoles.filter((r) => r !== role) });
  };

  const addLocation = () => {
    const trimmed = locationInput.trim();
    if (trimmed && !form.preferredLocations.includes(trimmed)) {
      setForm({ ...form, preferredLocations: [...form.preferredLocations, trimmed] });
    }
    setLocationInput('');
  };

  const removeLocation = (loc: string) => {
    setForm({ ...form, preferredLocations: form.preferredLocations.filter((l) => l !== loc) });
  };

  const toggleEmploymentType = (type: string) => {
    setForm({
      ...form,
      employmentTypes: form.employmentTypes.includes(type)
        ? form.employmentTypes.filter((t) => t !== type)
        : [...form.employmentTypes, type],
    });
  };

  const handleSave = () => {
    updateMutation.mutate(form);
  };

  return (
    <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
      <SettingsSidebar />
      <Card>
        <CardHeader>
          <CardTitle>Job Preferences</CardTitle>
          <CardDescription>Set your job search preferences to get better recommendations</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="space-y-2">
            <Label>Desired Roles</Label>
            <div className="flex gap-2">
              <Input
                placeholder="e.g. Senior Frontend Engineer"
                value={roleInput}
                onChange={(e) => setRoleInput(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') addRole(); }}
              />
              <Button variant="outline" size="icon" onClick={addRole}>
                <Plus className="h-4 w-4" />
              </Button>
            </div>
            <div className="flex flex-wrap gap-1">
              {form.desiredRoles.map((role) => (
                <Badge key={role} variant="secondary" className="gap-1">
                  {role}
                  <button onClick={() => removeRole(role)} className="ml-1 hover:text-destructive">
                    <X className="h-3 w-3" />
                  </button>
                </Badge>
              ))}
            </div>
          </div>

          <div className="space-y-2">
            <Label>Preferred Locations</Label>
            <div className="flex gap-2">
              <Input
                placeholder="e.g. San Francisco, CA"
                value={locationInput}
                onChange={(e) => setLocationInput(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') addLocation(); }}
              />
              <Button variant="outline" size="icon" onClick={addLocation}>
                <Plus className="h-4 w-4" />
              </Button>
            </div>
            <div className="flex flex-wrap gap-1">
              {form.preferredLocations.map((loc) => (
                <Badge key={loc} variant="secondary" className="gap-1">
                  {loc}
                  <button onClick={() => removeLocation(loc)} className="ml-1 hover:text-destructive">
                    <X className="h-3 w-3" />
                  </button>
                </Badge>
              ))}
            </div>
          </div>

          <div className="flex items-center justify-between rounded-lg border p-4">
            <div>
              <Label className="text-base">Remote Preference</Label>
              <p className="text-sm text-muted-foreground">Only show remote positions</p>
            </div>
            <Switch
              checked={form.remotePreference}
              onCheckedChange={(checked) => setForm({ ...form, remotePreference: checked })}
            />
          </div>

          <div className="space-y-2">
            <Label>Employment Types</Label>
            <div className="grid gap-2 sm:grid-cols-2">
              {EMPLOYMENT_TYPES.map((type) => (
                <label key={type} className="flex items-center gap-2 rounded-lg border p-3 cursor-pointer hover:bg-accent">
                  <input
                    type="checkbox"
                    checked={form.employmentTypes.includes(type)}
                    onChange={() => toggleEmploymentType(type)}
                    className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                  />
                  <span className="text-sm font-medium">{type.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase())}</span>
                </label>
              ))}
            </div>
          </div>

          <Button onClick={handleSave} disabled={updateMutation.isPending} className="gap-2">
            {updateMutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
            Save Preferences
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
