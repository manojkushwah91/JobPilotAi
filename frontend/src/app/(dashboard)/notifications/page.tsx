'use client';

import { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Bell, CheckCheck, Mail, MailOpen, Sparkles, Inbox } from 'lucide-react';
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

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="relative overflow-hidden rounded-2xl bg-gradient-mesh p-6">
          <div className="relative z-10">
            <div className="flex items-center gap-2 mb-1">
              <Sparkles className="h-4 w-4 text-primary animate-pulse" />
              <span className="text-xs font-medium text-primary">Notifications</span>
            </div>
            <h1 className="text-3xl font-bold tracking-tight mb-1">Notifications</h1>
            <p className="text-muted-foreground">Loading your notifications...</p>
          </div>
        </div>
        <div className="space-y-3">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="skeleton-premium h-24 rounded-xl" style={{ animationDelay: `${i * 50}ms` }} />
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-6">
        <div className="relative overflow-hidden rounded-2xl bg-gradient-mesh p-6">
          <div className="relative z-10">
            <div className="flex items-center gap-2 mb-1">
              <Sparkles className="h-4 w-4 text-primary animate-pulse" />
              <span className="text-xs font-medium text-primary">Notifications</span>
            </div>
            <h1 className="text-3xl font-bold tracking-tight mb-1">Notifications</h1>
            <p className="text-muted-foreground">Stay updated on your applications and activity</p>
          </div>
        </div>
        <Card className="glass">
          <CardContent className="flex flex-col items-center justify-center h-64 gap-4">
            <p className="text-muted-foreground">Failed to load notifications</p>
            <Button onClick={() => refetch()} className="bg-gradient-primary hover:opacity-90">
              Retry
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="relative overflow-hidden rounded-2xl bg-gradient-mesh p-6">
        <div className="relative z-10">
          <div className="flex items-center gap-2 mb-1">
            <Sparkles className="h-4 w-4 text-primary animate-pulse" />
            <span className="text-xs font-medium text-primary">Notifications</span>
          </div>
          <h1 className="text-3xl font-bold tracking-tight mb-1">Notifications</h1>
          <p className="text-muted-foreground">
            Stay updated on your applications and activity • <span className="text-foreground font-medium">{notifications.length} total</span>
            {unreadCount > 0 && (
              <> • <span className="text-primary font-medium">{unreadCount} unread</span></>
            )}
          </p>
        </div>
        <div className="absolute -right-16 -top-16 h-48 w-48 rounded-full bg-primary/5 blur-3xl" />
      </div>

      <div className="glass rounded-xl p-4 flex items-center justify-between">
        <Tabs value={tab} onValueChange={setTab} className="flex-1">
          <TabsList className="bg-muted/50">
            <TabsTrigger value="all" className="data-[state=active]:bg-background data-[state=active]:shadow-sm">
              All ({notifications.length})
            </TabsTrigger>
            <TabsTrigger value="unread" className="data-[state=active]:bg-background data-[state=active]:shadow-sm">
              Unread ({unreadCount})
            </TabsTrigger>
          </TabsList>
        </Tabs>
        {unreadCount > 0 && (
          <Button
            variant="outline"
            size="sm"
            className="ml-4 bg-gradient-primary hover:opacity-90 text-white border-0"
            onClick={async () => {
              try {
                await apiPost(API.notifications.markAllRead);
                queryClient.invalidateQueries({ queryKey: ['notifications'] });
                toast.success('All notifications marked as read');
              } catch {
                toast.error('Failed to mark all as read');
              }
            }}
          >
            <CheckCheck className="mr-2 h-4 w-4" />
            Mark all read
          </Button>
        )}
      </div>

      <Tabs value={tab} onValueChange={setTab}>
        <TabsContent value={tab}>
          {filtered.length === 0 ? (
            <Card className="glass">
              <CardContent className="flex flex-col items-center justify-center py-20">
                <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-muted/50">
                  <Inbox className="h-8 w-8 text-muted-foreground" />
                </div>
                <h3 className="mb-2 text-lg font-medium">No notifications</h3>
                <p className="text-sm text-muted-foreground text-center max-w-sm">
                  {tab === 'unread' ? 'All caught up! You have no unread notifications.' : 'You have no notifications yet. They\'ll appear here when your agent takes action.'}
                </p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-2 scrollbar-premium max-h-[60vh] overflow-y-auto pr-1">
              {filtered.map((n, i) => (
                <div
                  key={n.id}
                  className="animate-fade-in"
                  style={{ animationDelay: `${i * 50}ms` }}
                >
                  <Card className={`glass transition-all hover:shadow-md ${!n.read ? 'border-primary/20 bg-primary/5' : ''}`}>
                    <CardContent className="flex items-start gap-4 p-4">
                      <div className="mt-1">
                        {n.read ? (
                          <Mail className="h-5 w-5 text-muted-foreground" />
                        ) : (
                          <MailOpen className="h-5 w-5 text-primary" />
                        )}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className={`text-sm ${!n.read ? 'font-semibold text-foreground' : 'text-muted-foreground'}`}>
                          {n.title}
                        </p>
                        <p className="text-sm text-muted-foreground truncate">{n.message}</p>
                        <p className="text-xs text-muted-foreground mt-1">{dayjs(n.createdAt).fromNow()}</p>
                      </div>
                    </CardContent>
                  </Card>
                </div>
              ))}
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
