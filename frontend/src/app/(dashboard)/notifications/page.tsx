'use client';

import { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Bell, CheckCheck, Loader2, Mail, MailOpen } from 'lucide-react';
import { useApiQuery } from '@/lib/hooks/useQuery';
import { apiPost } from '@/lib/api/client';
import { API } from '@/lib/api/endpoints';
import type { Notification } from '@/types';
import { toast } from 'sonner';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(relativeTime);

export default function NotificationsPage() {
  const queryClient = useQueryClient();
  const [tab, setTab] = useState('all');
  const { data, isLoading, error, refetch } = useApiQuery<Notification[]>(['notifications'], API.notifications.list);
  const notifications: Notification[] = data?.data ?? [];
  const filtered = tab === 'unread' ? notifications.filter(n => !n.read) : notifications;
  const unreadCount = notifications.filter(n => !n.read).length;

  if (isLoading) return <div className="flex items-center justify-center h-64"><Loader2 className="h-8 w-8 animate-spin text-muted-foreground" /></div>;
  if (error) return <div className="flex flex-col items-center justify-center h-64 gap-4"><p className="text-muted-foreground">Failed to load notifications</p><Button onClick={() => refetch()}>Retry</Button></div>;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Notifications</h1>
          <p className="text-muted-foreground">Stay updated on your applications and activity</p>
        </div>
        {unreadCount > 0 && <Button variant="outline" size="sm" onClick={async () => { try { await apiPost(API.notifications.markAllRead); queryClient.invalidateQueries({ queryKey: ['notifications'] }); toast.success('All notifications marked as read'); } catch { toast.error('Failed to mark all as read'); } }}><CheckCheck className="mr-2 h-4 w-4" /> Mark all read</Button>}
      </div>
      <Tabs value={tab} onValueChange={setTab}>
        <TabsList>
          <TabsTrigger value="all">All ({notifications.length})</TabsTrigger>
          <TabsTrigger value="unread">Unread ({unreadCount})</TabsTrigger>
        </TabsList>
        <TabsContent value={tab} className="mt-4">
          {filtered.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-64 gap-4 text-muted-foreground">
              <Bell className="h-12 w-12" />
              <p className="text-lg font-medium">No notifications</p>
              <p>{tab === 'unread' ? 'All caught up!' : 'You have no notifications yet'}</p>
            </div>
          ) : (
            <div className="space-y-2">
              {filtered.map((n) => (
                <Card key={n.id} className={`transition-colors ${!n.read ? 'border-primary/20 bg-primary/5' : ''}`}>
                  <CardContent className="flex items-start gap-4 p-4">
                    <div className="mt-1">{n.read ? <Mail className="h-5 w-5 text-muted-foreground" /> : <MailOpen className="h-5 w-5 text-primary" />}</div>
                    <div className="flex-1 min-w-0">
                      <p className={`text-sm ${!n.read ? 'font-semibold' : ''}`}>{n.title}</p>
                      <p className="text-sm text-muted-foreground truncate">{n.message}</p>
                      <p className="text-xs text-muted-foreground mt-1">{dayjs(n.createdAt).fromNow()}</p>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
