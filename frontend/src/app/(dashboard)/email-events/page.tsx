'use client';

import { useState, useEffect, useCallback } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { RefreshCw, Mail, MailCheck, MailX, Calendar, Gift, HelpCircle, Loader2 } from 'lucide-react';

interface EmailEvent {
  eventId: string;
  jobUrl: string;
  jobTitle: string;
  companyName: string;
  eventType: string;
  senderEmail: string;
  subject: string;
  bodySnippet: string;
  receivedAt: string;
  createdAt: string;
}

interface EventCounts {
  APPLICATION_CONFIRMATION: number;
  APPLICATION_REJECTION: number;
  INTERVIEW_INVITATION: number;
  OFFER_RECEIVED: number;
  UNKNOWN: number;
}

const EVENT_CONFIG: Record<string, { label: string; icon: typeof Mail; color: string; bg: string }> = {
  APPLICATION_CONFIRMATION: { label: 'Confirmed', icon: MailCheck, color: 'text-green-600', bg: 'bg-green-100 text-green-800' },
  APPLICATION_REJECTION: { label: 'Rejected', icon: MailX, color: 'text-red-600', bg: 'bg-red-100 text-red-800' },
  INTERVIEW_INVITATION: { label: 'Interview', icon: Calendar, color: 'text-blue-600', bg: 'bg-blue-100 text-blue-800' },
  OFFER_RECEIVED: { label: 'Offer', icon: Gift, color: 'text-purple-600', bg: 'bg-purple-100 text-purple-800' },
  UNKNOWN: { label: 'Other', icon: HelpCircle, color: 'text-gray-600', bg: 'bg-gray-100 text-gray-800' },
};

export default function EmailEventsPage() {
  const [events, setEvents] = useState<EmailEvent[]>([]);
  const [counts, setCounts] = useState<EventCounts | null>(null);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<string>('ALL');

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const [eventsRes, countsRes] = await Promise.all([
        fetch('/api/v1/agent/email/events'),
        fetch('/api/v1/agent/email/events/counts'),
      ]);
      if (eventsRes.ok) setEvents(await eventsRes.json());
      if (countsRes.ok) setCounts(await countsRes.json());
    } catch (error) {
      console.error('Failed to fetch email events:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const filteredEvents = filter === 'ALL' ? events : events.filter((e) => e.eventType === filter);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin" />
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Email Events</h1>
          <p className="text-muted-foreground">
            Parsed email notifications from job applications
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchData}>
          <RefreshCw className="h-4 w-4 mr-1" />
          Refresh
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-5">
        {Object.entries(EVENT_CONFIG).map(([type, config]) => {
          const Icon = config.icon;
          const count = counts?.[type as keyof EventCounts] || 0;
          return (
            <Card
              key={type}
              className={`cursor-pointer transition-all ${filter === type ? 'ring-2 ring-primary' : ''}`}
              onClick={() => setFilter(filter === type ? 'ALL' : type)}
            >
              <CardContent className="pt-6">
                <div className="flex items-center gap-2">
                  <Icon className={`h-5 w-5 ${config.color}`} />
                  <span className="text-2xl font-bold">{count}</span>
                </div>
                <p className="text-xs text-muted-foreground mt-1">{config.label}</p>
              </CardContent>
            </Card>
          );
        })}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Email Events</CardTitle>
          <CardDescription>
            {filter === 'ALL' ? 'All events' : EVENT_CONFIG[filter]?.label} ({filteredEvents.length})
          </CardDescription>
        </CardHeader>
        <CardContent>
          {filteredEvents.length === 0 ? (
            <p className="text-sm text-muted-foreground text-center py-8">
              No email events found. Run automation to receive application emails.
            </p>
          ) : (
            <div className="space-y-3">
              {filteredEvents.map((event) => {
                const config = EVENT_CONFIG[event.eventType] || EVENT_CONFIG.UNKNOWN;
                return (
                  <div key={event.eventId} className="border rounded-lg p-4">
                    <div className="flex items-start justify-between">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-1">
                          <Badge className={config.bg}>{config.label}</Badge>
                          {event.companyName && (
                            <span className="text-sm font-medium">{event.companyName}</span>
                          )}
                        </div>
                        <p className="text-sm font-medium truncate">{event.subject}</p>
                        {event.jobTitle && (
                          <p className="text-xs text-muted-foreground mt-1">{event.jobTitle}</p>
                        )}
                        {event.bodySnippet && (
                          <p className="text-xs text-muted-foreground mt-1 line-clamp-2">
                            {event.bodySnippet}
                          </p>
                        )}
                      </div>
                      <div className="text-right ml-4 shrink-0">
                        <p className="text-xs text-muted-foreground">
                          {new Date(event.receivedAt).toLocaleDateString()}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          {new Date(event.receivedAt).toLocaleTimeString()}
                        </p>
                        {event.senderEmail && (
                          <p className="text-xs text-muted-foreground mt-1 truncate max-w-[150px]">
                            {event.senderEmail}
                          </p>
                        )}
                      </div>
                    </div>
                    {event.jobUrl && (
                      <a
                        href={event.jobUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-xs text-primary hover:underline mt-2 inline-block"
                      >
                        View Job
                      </a>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
