'use client';

import { useState } from 'react';
import { TableCell, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { apiPut } from '@/lib/api/client';
import { API } from '@/lib/api/endpoints';
import type { AdminUser, UserRole, UserTier } from '@/types';

interface UserRowProps {
  user: AdminUser;
  onUpdate: () => void;
}

const roleVariants: Record<string, 'default' | 'secondary' | 'destructive' | 'success'> = {
  FREE: 'secondary',
  PREMIUM: 'default',
  PRO: 'success',
  ADMIN: 'destructive',
};

export function UserRow({ user, onUpdate }: UserRowProps) {
  const [updating, setUpdating] = useState(false);

  const handleRoleChange = async (role: string) => {
    setUpdating(true);
    try {
      await apiPut(API.admin.userDetail(user.id), { role });
      toast.success(`User role updated to ${role}`);
      onUpdate();
    } catch {
      toast.error('Failed to update role');
    } finally {
      setUpdating(false);
    }
  };

  const handleTierChange = async (tier: string) => {
    setUpdating(true);
    try {
      await apiPut(API.admin.userDetail(user.id), { tier });
      toast.success(`User tier updated to ${tier}`);
      onUpdate();
    } catch {
      toast.error('Failed to update tier');
    } finally {
      setUpdating(false);
    }
  };

  const handleToggleStatus = async () => {
    setUpdating(true);
    try {
      if (user.status === 'SUSPENDED') {
        await apiPut(API.admin.unsuspend(user.id));
        toast.success('User reactivated');
      } else {
        await apiPut(API.admin.suspend(user.id));
        toast.success('User suspended');
      }
      onUpdate();
    } catch {
      toast.error('Failed to update user status');
    } finally {
      setUpdating(false);
    }
  };

  return (
    <TableRow>
      <TableCell className="font-medium">{user.name}</TableCell>
      <TableCell>{user.email}</TableCell>
      <TableCell>
        <Select defaultValue={user.role} onValueChange={handleRoleChange} disabled={updating}>
          <SelectTrigger className="h-8 w-28">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="FREE">Free</SelectItem>
            <SelectItem value="PREMIUM">Premium</SelectItem>
            <SelectItem value="PRO">Pro</SelectItem>
            <SelectItem value="ADMIN">Admin</SelectItem>
          </SelectContent>
        </Select>
      </TableCell>
      <TableCell>
        <Select defaultValue={user.tier} onValueChange={handleTierChange} disabled={updating}>
          <SelectTrigger className="h-8 w-28">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="FREE">Free</SelectItem>
            <SelectItem value="PREMIUM">Premium</SelectItem>
            <SelectItem value="PRO">Pro</SelectItem>
          </SelectContent>
        </Select>
      </TableCell>
      <TableCell>
        <Badge variant={user.status === 'ACTIVE' ? 'success' : 'destructive'}>
          {user.status}
        </Badge>
      </TableCell>
      <TableCell className="text-sm text-muted-foreground">
        {new Date(user.createdAt).toLocaleDateString()}
      </TableCell>
      <TableCell>
        <Button
          variant={user.status === 'SUSPENDED' ? 'outline' : 'destructive'}
          size="sm"
          onClick={handleToggleStatus}
          disabled={updating}
        >
          {updating ? (
            <Loader2 className="h-3 w-3 animate-spin" />
          ) : user.status === 'SUSPENDED' ? (
            'Activate'
          ) : (
            'Suspend'
          )}
        </Button>
      </TableCell>
    </TableRow>
  );
}
