'use client';

import { useState, useEffect, useCallback, useRef } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Progress } from '@/components/ui/progress';
import {
  Play,
  Square,
  RefreshCw,
  Wifi,
  WifiOff,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Clock,
  Loader2,
} from 'lucide-react';

interface AutomationStatus {
  running: boolean;
  currentSessionId: string;
  queueSize: number;
  availableBoards: string[];
}

interface ProgressEvent {
  sessionId: string;
  type: string;
  jobUrl: string;
  jobTitle: string;
  currentStep: string;
  detail: string;
  outcome: string;
  errorCount: number;
  waitingForCaptcha: boolean;
  timestamp: string;
}

interface ApplicationResult {
  id: string;
  sessionId: string;
  userId: string;
  missionId: string;
  jobUrl: string;
  jobTitle: string;
  companyName: string;
  outcome: string;
  errorMessage: string;
  createdAt: string;
}

export default function AutomationDashboard() {
  const [status, setStatus] = useState<AutomationStatus | null>(null);
  const [progressEvents, setProgressEvents] = useState<ProgressEvent[]>([]);
  const [results, setResults] = useState<ApplicationResult[]>([]);
  const [wsConnected, setWsConnected] = useState(false);
  const [loading, setLoading] = useState(true);
  const [selectedBoard, setSelectedBoard] = useState('');
  const [credentials, setCredentials] = useState({ username: '', password: '' });
  const [showLoginForm, setShowLoginForm] = useState(false);
  const wsRef = useRef<WebSocket | null>(null);
  const eventLogRef = useRef<HTMLDivElement>(null);

  const fetchStatus = useCallback(async () => {
    try {
      const response = await fetch('/api/v1/agent/automate/status');
      if (response.ok) {
        const data = await response.json();
        setStatus(data);
      }
    } catch (error) {
      console.error('Failed to fetch automation status:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchResults = useCallback(async () => {
    try {
      const response = await fetch('/api/v1/agent/automate/results');
      if (response.ok) {
        const data = await response.json();
        setResults(data);
      }
    } catch (error) {
      console.error('Failed to fetch results:', error);
    }
  }, []);

  const connectWebSocket = useCallback(() => {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/automation`;

    try {
      const ws = new WebSocket(wsUrl);
      wsRef.current = ws;

      ws.onopen = () => {
        ws.send('subscribe:automation_progress');
        setWsConnected(true);
      };

      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          if (data.type === 'automation_progress' && data.data) {
            setProgressEvents((prev) => [data.data, ...prev].slice(0, 50));
            if (eventLogRef.current) {
              eventLogRef.current.scrollTop = 0;
            }
          }
        } catch (e) {
          console.error('Failed to parse WebSocket message:', e);
        }
      };

      ws.onclose = () => {
        setWsConnected(false);
        setTimeout(connectWebSocket, 3000);
      };

      ws.onerror = () => {
        setWsConnected(false);
      };
    } catch (error) {
      console.error('WebSocket connection failed:', error);
    }
  }, []);

  useEffect(() => {
    fetchStatus();
    connectWebSocket();
    return () => {
      wsRef.current?.close();
    };
  }, [fetchStatus, connectWebSocket]);

  const startAutomation = async () => {
    if (!selectedBoard) return;

    try {
      const response = await fetch('/api/v1/agent/automate/start', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          boardName: selectedBoard,
          credentials: credentials.username ? credentials : {},
          userId: 'current',
        }),
      });
      if (response.ok) {
        fetchStatus();
        setShowLoginForm(false);
      }
    } catch (error) {
      console.error('Failed to start automation:', error);
    }
  };

  const stopAutomation = async () => {
    try {
      await fetch('/api/v1/agent/automate/stop', { method: 'POST' });
      fetchStatus();
    } catch (error) {
      console.error('Failed to stop automation:', error);
    }
  };

  const getOutcomeIcon = (outcome: string) => {
    switch (outcome) {
      case 'SUBMITTED':
      case 'submitted':
        return <CheckCircle2 className="h-4 w-4 text-green-500" />;
      case 'FAILED':
      case 'failed':
        return <XCircle className="h-4 w-4 text-red-500" />;
      case 'PENDING_CAPTCHA':
        return <AlertTriangle className="h-4 w-4 text-yellow-500" />;
      default:
        return <Clock className="h-4 w-4 text-gray-500" />;
    }
  };

  const getOutcomeColor = (outcome: string) => {
    switch (outcome) {
      case 'SUBMITTED':
      case 'submitted':
        return 'bg-green-100 text-green-800';
      case 'FAILED':
      case 'failed':
        return 'bg-red-100 text-red-800';
      case 'PENDING_CAPTCHA':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

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
          <h1 className="text-3xl font-bold">Automation Dashboard</h1>
          <p className="text-muted-foreground">
            Browser automation status and real-time progress
          </p>
        </div>
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2">
            {wsConnected ? (
              <Wifi className="h-4 w-4 text-green-500" />
            ) : (
              <WifiOff className="h-4 w-4 text-red-500" />
            )}
            <span className="text-sm text-muted-foreground">
              {wsConnected ? 'Live' : 'Disconnected'}
            </span>
          </div>
          <Button variant="outline" size="sm" onClick={() => { fetchStatus(); fetchResults(); }}>
            <RefreshCw className="h-4 w-4 mr-1" />
            Refresh
          </Button>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardContent className="pt-6">
            <div className="text-2xl font-bold">
              {status?.running ? (
                <span className="text-green-600">Running</span>
              ) : (
                <span className="text-gray-500">Idle</span>
              )}
            </div>
            <p className="text-xs text-muted-foreground">Automation Status</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="text-2xl font-bold">{status?.queueSize || 0}</div>
            <p className="text-xs text-muted-foreground">Queue Size</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="text-2xl font-bold">
              {results.filter((r) => r.outcome === 'SUBMITTED').length}
            </div>
            <p className="text-xs text-muted-foreground">Applications Sent</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="text-2xl font-bold">
              {results.filter((r) => r.outcome === 'FAILED').length}
            </div>
            <p className="text-xs text-muted-foreground">Failed</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Controls</CardTitle>
          <CardDescription>Start or stop browser automation</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex items-center gap-4">
            {status?.running ? (
              <Button variant="destructive" onClick={stopAutomation}>
                <Square className="h-4 w-4 mr-2" />
                Stop Automation
              </Button>
            ) : (
              <>
                <select
                  className="border rounded-md px-3 py-2 text-sm"
                  value={selectedBoard}
                  onChange={(e) => setSelectedBoard(e.target.value)}
                >
                  <option value="">Select Board</option>
                  {(status?.availableBoards || []).map((board) => (
                    <option key={board} value={board}>
                      {board.charAt(0).toUpperCase() + board.slice(1)}
                    </option>
                  ))}
                </select>
                <Button
                  onClick={() => setShowLoginForm(true)}
                  disabled={!selectedBoard}
                >
                  <Play className="h-4 w-4 mr-2" />
                  Start
                </Button>
              </>
            )}
          </div>

          {showLoginForm && (
            <div className="mt-4 p-4 border rounded-lg space-y-3">
              <p className="text-sm font-medium">Login Credentials (optional for some boards)</p>
              <Input
                type="text"
                placeholder="Username / Email"
                value={credentials.username}
                onChange={(e) => setCredentials({ ...credentials, username: e.target.value })}
              />
              <Input
                type="password"
                placeholder="Password"
                value={credentials.password}
                onChange={(e) => setCredentials({ ...credentials, password: e.target.value })}
              />
              <div className="flex gap-2">
                <Button size="sm" onClick={startAutomation}>Confirm & Start</Button>
                <Button size="sm" variant="outline" onClick={() => setShowLoginForm(false)}>Cancel</Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Live Progress</CardTitle>
            <CardDescription>Real-time automation events via WebSocket</CardDescription>
          </CardHeader>
          <CardContent>
            <div
              ref={eventLogRef}
              className="h-80 overflow-y-auto space-y-2"
            >
              {progressEvents.length === 0 ? (
                <p className="text-sm text-muted-foreground text-center py-8">
                  No events yet. Start automation to see live progress.
                </p>
              ) : (
                progressEvents.map((event, index) => (
                  <div
                    key={`${event.sessionId}-${event.timestamp}-${index}`}
                    className="border rounded-lg p-3 text-sm"
                  >
                    <div className="flex items-center justify-between mb-1">
                      <span className="font-medium truncate max-w-[200px]">
                        {event.jobTitle || event.jobUrl}
                      </span>
                      <Badge className={getOutcomeColor(event.type)}>
                        {event.type}
                      </Badge>
                    </div>
                    {event.detail && (
                      <p className="text-xs text-muted-foreground">{event.detail}</p>
                    )}
                    <p className="text-xs text-muted-foreground mt-1">
                      {new Date(event.timestamp).toLocaleTimeString()}
                    </p>
                  </div>
                ))
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Recent Applications</CardTitle>
            <CardDescription>Latest application results</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-80 overflow-y-auto space-y-2">
              {results.length === 0 ? (
                <p className="text-sm text-muted-foreground text-center py-8">
                  No applications yet.
                </p>
              ) : (
                results.slice(0, 20).map((result) => (
                  <div
                    key={result.id}
                    className="border rounded-lg p-3 text-sm"
                  >
                    <div className="flex items-center justify-between mb-1">
                      <div className="flex items-center gap-2">
                        {getOutcomeIcon(result.outcome)}
                        <span className="font-medium truncate max-w-[180px]">
                          {result.jobTitle || result.jobUrl}
                        </span>
                      </div>
                      <Badge className={getOutcomeColor(result.outcome)}>
                        {result.outcome}
                      </Badge>
                    </div>
                    {result.companyName && (
                      <p className="text-xs text-muted-foreground">{result.companyName}</p>
                    )}
                    {result.errorMessage && (
                      <p className="text-xs text-red-500 mt-1 truncate">{result.errorMessage}</p>
                    )}
                    <p className="text-xs text-muted-foreground mt-1">
                      {new Date(result.createdAt).toLocaleString()}
                    </p>
                  </div>
                ))
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
