'use client';

import { useState } from 'react';
import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Switch } from '@/components/ui/switch';
import { Label } from '@/components/ui/label';
import { Skeleton } from '@/components/ui/skeleton';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { SettingsSidebar } from '@/components/features/settings/SettingsSidebar';
import { Shield, Download, AlertTriangle, RefreshCw, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { apiGet, apiDelete } from '@/lib/api/client';
import type { PrivacySettings } from '@/types';

export default function PrivacySettingsPage() {
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  const { data: res, isLoading, isError, refetch } = useApiQuery<PrivacySettings>(
    ['settings', 'privacy'],
    API.settings.privacy
  );

  const updateMutation = useApiMutation<PrivacySettings, { profileVisibility: 'PUBLIC' | 'PRIVATE' }>(
    'PUT',
    API.settings.privacy,
    {
      onSuccess: () => {
        toast.success('Privacy settings updated');
        refetch();
      },
      onError: () => toast.error('Failed to update privacy settings'),
    }
  );

  const [exportingData, setExportingData] = useState(false);

  const handleExportData = async () => {
    setExportingData(true);
    try {
      const res = await apiGet('/users/me/export');
      toast.success('Data export started. You will receive an email when ready.');
    } catch {
      toast.error('Failed to export data');
    } finally {
      setExportingData(false);
    }
  };

  const deleteMutation = useApiMutation<void, void>('DELETE', API.users.deleteAccount, {
    onSuccess: () => {
      toast.success('Account deleted');
      setDeleteDialogOpen(false);
      window.location.href = '/login';
    },
    onError: () => toast.error('Failed to delete account'),
  });

  if (isLoading) {
    return (
      <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
        <SettingsSidebar />
        <div className="space-y-6">
          <Skeleton className="h-48 w-full" />
          <Skeleton className="h-32 w-full" />
        </div>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
        <SettingsSidebar />
        <Card>
          <CardContent className="flex flex-col items-center gap-4 py-12">
            <p className="text-destructive">Failed to load privacy settings</p>
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

  return (
    <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
      <SettingsSidebar />
      <div className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Shield className="h-5 w-5" />
              Privacy Settings
            </CardTitle>
            <CardDescription>Control your profile visibility and data</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="flex items-center justify-between rounded-lg border p-4">
              <div>
                <Label className="text-base">Profile Visibility</Label>
                <p className="text-sm text-muted-foreground">
                  {settings.profileVisibility === 'PUBLIC' ? 'Your profile is visible to employers' : 'Your profile is private'}
                </p>
              </div>
              <Switch
                checked={settings.profileVisibility === 'PUBLIC'}
                onCheckedChange={(checked) => {
                  updateMutation.mutate({ profileVisibility: checked ? 'PUBLIC' : 'PRIVATE' });
                }}
              />
            </div>

            <div className="flex items-center justify-between rounded-lg border p-4">
              <div>
                <Label className="text-base">Export My Data</Label>
                <p className="text-sm text-muted-foreground">Download all your data including resumes, applications, and settings</p>
              </div>
              <Button variant="outline" onClick={handleExportData} disabled={exportingData} className="gap-2">
                {exportingData ? <Loader2 className="h-4 w-4 animate-spin" /> : <Download className="h-4 w-4" />}
                Export Data
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card className="border-destructive">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-destructive">
              <AlertTriangle className="h-5 w-5" />
              Danger Zone
            </CardTitle>
            <CardDescription>Irreversible actions for your account</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex items-center justify-between rounded-lg border border-destructive/50 p-4">
              <div>
                <Label className="text-base text-destructive">Delete Account</Label>
                <p className="text-sm text-muted-foreground">
                  Permanently delete your account and all associated data
                </p>
              </div>
              <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
                <DialogTrigger asChild>
                  <Button variant="destructive">Delete Account</Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Delete Account</DialogTitle>
                    <DialogDescription>
                      Are you absolutely sure? This action cannot be undone. All your data will be permanently deleted.
                    </DialogDescription>
                  </DialogHeader>
                  <DialogFooter>
                    <Button variant="outline" onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
                    <Button variant="destructive" onClick={() => deleteMutation.mutate()} disabled={deleteMutation.isPending}>
                      {deleteMutation.isPending ? 'Deleting...' : 'Delete My Account'}
                    </Button>
                  </DialogFooter>
                </DialogContent>
              </Dialog>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
