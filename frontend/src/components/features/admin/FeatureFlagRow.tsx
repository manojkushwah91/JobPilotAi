'use client';

import { useState } from 'react';
import { TableCell, TableRow } from '@/components/ui/table';
import { Switch } from '@/components/ui/switch';
import { Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { apiPut } from '@/lib/api/client';
import { API } from '@/lib/api/endpoints';
import type { FeatureFlag } from '@/types';

interface FeatureFlagRowProps {
  flag: FeatureFlag;
  onUpdate: () => void;
}

export function FeatureFlagRow({ flag, onUpdate }: FeatureFlagRowProps) {
  const [updating, setUpdating] = useState(false);

  const handleToggle = async (enabled: boolean) => {
    setUpdating(true);
    try {
      await apiPut(API.admin.featureFlagToggle(flag.key), { enabled });
      toast.success(`Feature "${flag.key}" ${enabled ? 'enabled' : 'disabled'}`);
      onUpdate();
    } catch {
      toast.error('Failed to update feature flag');
    } finally {
      setUpdating(false);
    }
  };

  return (
    <TableRow>
      <TableCell className="font-mono text-sm font-medium">{flag.key}</TableCell>
      <TableCell className="text-sm text-muted-foreground">{flag.description}</TableCell>
      <TableCell>
        <div className="flex items-center gap-2">
          <Switch
            checked={flag.enabled}
            onCheckedChange={handleToggle}
            disabled={updating}
          />
          {updating && <Loader2 className="h-3 w-3 animate-spin" />}
        </div>
      </TableCell>
      <TableCell className="text-sm text-muted-foreground">
        {new Date(flag.updatedAt).toLocaleDateString()}
      </TableCell>
    </TableRow>
  );
}
