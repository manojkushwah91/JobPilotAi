'use client';

import { useState, useEffect } from 'react';
import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Skeleton } from '@/components/ui/skeleton';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { SettingsSidebar } from '@/components/features/settings/SettingsSidebar';
import { User, Camera, RefreshCw, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import type { UserProfile } from '@/types';

export default function ProfileSettingsPage() {
  const { data: res, isLoading, isError, refetch } = useApiQuery<UserProfile>(
    ['settings', 'profile'],
    API.settings.profile
  );

  const updateMutation = useApiMutation<UserProfile, Partial<UserProfile>>(
    'PUT',
    API.settings.profile,
    {
      onSuccess: () => {
        toast.success('Profile updated successfully');
        refetch();
      },
      onError: () => toast.error('Failed to update profile'),
    }
  );

  const [form, setForm] = useState<{ name: string; email: string }>({ name: '', email: '' });
  const [initialized, setInitialized] = useState(false);

  const profile = res?.data;
  useEffect(() => {
    if (!initialized && profile) {
      setForm({ name: profile.name ?? '', email: profile.email ?? '' });
      setInitialized(true);
    }
  }, [profile, initialized]);

  if (isLoading) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">Settings</h1>
        <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
          <SettingsSidebar />
          <Card>
            <CardHeader>
              <Skeleton className="h-6 w-32" />
              <Skeleton className="h-4 w-48" />
            </CardHeader>
            <CardContent className="space-y-4">
              <Skeleton className="h-20 w-20 rounded-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
            </CardContent>
          </Card>
        </div>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">Settings</h1>
        <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
          <SettingsSidebar />
          <Card>
            <CardContent className="flex flex-col items-center gap-4 py-12">
              <p className="text-destructive">Failed to load profile</p>
              <Button variant="outline" onClick={() => refetch()} className="gap-2">
                <RefreshCw className="h-4 w-4" />
                Retry
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>
    );
  }

  const handleSave = () => {
    if (!form.name.trim()) {
      toast.error('Name is required');
      return;
    }
    updateMutation.mutate({ name: form.name, email: form.email });
  };

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Settings</h1>
      <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
        <SettingsSidebar />
        <Card>
          <CardHeader>
            <CardTitle>Profile</CardTitle>
            <CardDescription>Manage your personal information</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="flex items-center gap-4">
              <Avatar className="h-20 w-20">
                <AvatarImage src={profile?.avatarUrl} alt={form.name} />
                <AvatarFallback>
                  <User className="h-8 w-8 text-muted-foreground" />
                </AvatarFallback>
              </Avatar>
              <Button variant="outline" size="sm" className="gap-2">
                <Camera className="h-4 w-4" />
                Change Avatar
              </Button>
            </div>
            <div className="space-y-2">
              <Label htmlFor="name">Name</Label>
              <Input
                id="name"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
              />
            </div>
            <Button onClick={handleSave} disabled={updateMutation.isPending} className="gap-2">
              {updateMutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
              Save Changes
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
