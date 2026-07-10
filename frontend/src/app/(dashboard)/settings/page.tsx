'use client';

import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '@/lib/auth/AuthProvider';
import { apiGet, apiPut } from '@/lib/api/client';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Separator } from '@/components/ui/separator';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Sparkles,
  Brain,
  Building2,
  Ban,
  Target,
  Gavel,
  Sliders,
  Loader2,
  CheckCircle2,
} from 'lucide-react';
import { toast } from 'sonner';

type AutonomyMode = 'recommendations' | 'approval' | 'autonomous';

const MODES: { value: AutonomyMode; label: string; desc: string }[] = [
  { value: 'recommendations', label: 'Recommendations Only', desc: 'AI suggests actions, you decide' },
  { value: 'approval', label: 'Approval Required', desc: 'AI asks before every application' },
  { value: 'autonomous', label: 'Fully Autonomous', desc: 'AI applies according to your rules' },
];

const WORK_AUTHORIZATIONS = [
  'US Citizen',
  'Permanent Resident',
  'H1-B',
  'F1-OPT',
  'TN',
  'E-3',
  'Other',
] as const;

const EMPLOYMENT_TYPES = ['Full-time', 'Contract', 'Part-time', 'Internship', 'Freelance'] as const;

interface AgentSettingsData {
  preferredCompanies: string;
  avoidCompanies: string;
  careerGoal: string;
  preferredLocation: string;
  salaryMin: number;
  salaryMax: number;
  employmentType: string;
  workAuthorization: string;
}

interface UserSettings {
  aiPreferences?: Record<string, unknown>;
  jobPreferences?: Record<string, unknown>;
}

const defaultAgent: AgentSettingsData = {
  preferredCompanies: '',
  avoidCompanies: '',
  careerGoal: '',
  preferredLocation: '',
  salaryMin: 0,
  salaryMax: 0,
  employmentType: '',
  workAuthorization: '',
};

export default function SettingsPage() {
  const { user } = useAuth();

  // Loading / saving states
  const [loadingSettings, setLoadingSettings] = useState(true);
  const [loadingAgent, setLoadingAgent] = useState(true);
  const [saving, setSaving] = useState<string | null>(null);
  const [savedSections, setSavedSections] = useState<Set<string>>(new Set());

  // Autonomy mode (stored in aiPreferences)
  const [autonomyMode, setAutonomyMode] = useState<AutonomyMode>('recommendations');

  // Application limits
  const [dailyLimit, setDailyLimit] = useState(10);
  const [salaryMin, setSalaryMin] = useState(0);
  const [salaryMax, setSalaryMax] = useState(0);

  // Agent preferences
  const [agent, setAgent] = useState<AgentSettingsData>(defaultAgent);

  // Load settings (GET /api/v1/settings → aiPreferences)
  useEffect(() => {
    if (!user) return;
    let cancelled = false;

    (async () => {
      try {
        const { data } = await apiGet<UserSettings>('/settings');
        if (cancelled) return;

        const aiPrefs = (data?.aiPreferences ?? {}) as Record<string, unknown>;
        const jp = (data?.jobPreferences ?? {}) as Record<string, unknown>;

        setAutonomyMode((aiPrefs.autonomyMode as AutonomyMode) ?? 'recommendations');
        setDailyLimit(Number(aiPrefs.dailyApplicationLimit ?? 10));
        setSalaryMin(Number(aiPrefs.salaryMin ?? 0));
        setSalaryMax(Number(aiPrefs.salaryMax ?? 0));
      } catch {
        toast.error('Failed to load settings');
      } finally {
        if (!cancelled) setLoadingSettings(false);
      }
    })();

    return () => { cancelled = true; };
  }, [user]);

  // Load agent settings (GET /api/v1/agent/settings?userId=...)
  useEffect(() => {
    if (!user?.id) return;
    let cancelled = false;

    (async () => {
      try {
        const { data } = await apiGet<AgentSettingsData>(`/agent/settings?userId=${user.id}`);
        if (cancelled) return;
        if (data) {
          setAgent({
            preferredCompanies: data.preferredCompanies ?? '',
            avoidCompanies: data.avoidCompanies ?? '',
            careerGoal: data.careerGoal ?? '',
            preferredLocation: data.preferredLocation ?? '',
            salaryMin: data.salaryMin ?? 0,
            salaryMax: data.salaryMax ?? 0,
            employmentType: data.employmentType ?? '',
            workAuthorization: data.workAuthorization ?? '',
          });
        }
      } catch {
        // agent settings may 404 if not yet created — use defaults
      } finally {
        if (!cancelled) setLoadingAgent(false);
      }
    })();

    return () => { cancelled = true; };
  }, [user?.id]);

  const showSaved = useCallback((section: string) => {
    setSavedSections((prev) => new Set(prev).add(section));
    setTimeout(() => {
      setSavedSections((prev) => {
        const next = new Set(prev);
        next.delete(section);
        return next;
      });
    }, 2000);
  }, []);

  const handleSaveAutonomy = async () => {
    if (!user) return;
    setSaving('autonomy');
    try {
      await apiPut('/users/me/settings', {
        aiPreferences: {
          autonomyMode,
          dailyApplicationLimit: dailyLimit,
          salaryMin,
          salaryMax,
        },
        jobPreferences: {},
      });
      toast.success('Autonomy settings saved');
      showSaved('autonomy');
    } catch {
      toast.error('Failed to save autonomy settings');
    } finally {
      setSaving(null);
    }
  };

  const handleSaveAgent = async () => {
    if (!user?.id) return;
    setSaving('agent');
    try {
      await apiPut(`/agent/settings?userId=${user.id}`, {
        preferredCompanies: agent.preferredCompanies,
        avoidCompanies: agent.avoidCompanies,
        careerGoal: agent.careerGoal,
        preferredLocation: agent.preferredLocation,
        salaryMin: agent.salaryMin,
        salaryMax: agent.salaryMax,
        employmentType: agent.employmentType,
        workAuthorization: agent.workAuthorization,
      });
      toast.success('Agent preferences saved');
      showSaved('agent');
    } catch {
      toast.error('Failed to save agent preferences');
    } finally {
      setSaving(null);
    }
  };

  const isLoading = loadingSettings || loadingAgent;

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="relative overflow-hidden rounded-2xl bg-gradient-mesh p-6">
          <div className="relative z-10">
            <div className="flex items-center gap-2 mb-1">
              <Sparkles className="h-4 w-4 text-primary" />
              <span className="text-xs font-medium text-primary">Agent Settings</span>
            </div>
            <h1 className="text-3xl font-bold tracking-tight mb-1">Settings</h1>
            <p className="text-muted-foreground">Configure how your AI job agent operates</p>
          </div>
          <div className="absolute -right-16 -top-16 h-48 w-48 rounded-full bg-primary/5 blur-3xl" />
        </div>
        <div className="space-y-4">
          <Skeleton className="h-48 w-full rounded-xl" />
          <Skeleton className="h-64 w-full rounded-xl" />
          <Skeleton className="h-48 w-full rounded-xl" />
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-mesh p-6">
        <div className="relative z-10">
          <div className="flex items-center gap-2 mb-1">
            <Sparkles className="h-4 w-4 text-primary" />
            <span className="text-xs font-medium text-primary">Agent Settings</span>
          </div>
          <h1 className="text-3xl font-bold tracking-tight mb-1">Settings</h1>
          <p className="text-muted-foreground">Configure how your AI job agent operates</p>
        </div>
        <div className="absolute -right-16 -top-16 h-48 w-48 rounded-full bg-primary/5 blur-3xl" />
      </div>

      <div className="space-y-6">
        {/* Autonomy Mode */}
        <Card className="glass-strong animate-fade-in">
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="flex items-center gap-2">
                  <Brain className="h-5 w-5 text-primary" />
                  Autonomy Mode
                </CardTitle>
                <CardDescription>
                  Control how much independence your AI agent has
                </CardDescription>
              </div>
              {savedSections.has('autonomy') && (
                <span className="flex items-center gap-1 text-sm text-emerald-500">
                  <CheckCircle2 className="h-4 w-4" /> Saved
                </span>
              )}
            </div>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid gap-3 sm:grid-cols-3">
              {MODES.map((mode) => {
                const selected = autonomyMode === mode.value;
                return (
                  <button
                    key={mode.value}
                    type="button"
                    onClick={() => setAutonomyMode(mode.value)}
                    className={`relative flex flex-col items-start gap-2 rounded-xl border-2 p-4 text-left transition-all ${
                      selected
                        ? 'border-primary bg-primary/5 shadow-sm'
                        : 'border-border hover:border-muted-foreground/30'
                    }`}
                  >
                    <div
                      className={`flex h-5 w-5 items-center justify-center rounded-full border-2 ${
                        selected ? 'border-primary' : 'border-muted-foreground'
                      }`}
                    >
                      {selected && <div className="h-2.5 w-2.5 rounded-full bg-primary" />}
                    </div>
                    <div>
                      <p className="text-sm font-medium">{mode.label}</p>
                      <p className="text-xs text-muted-foreground mt-0.5">{mode.desc}</p>
                    </div>
                  </button>
                );
              })}
            </div>
            <Separator />
            <Button
              onClick={handleSaveAutonomy}
              disabled={saving === 'autonomy'}
              className="gap-2 bg-gradient-primary hover:opacity-90"
            >
              {saving === 'autonomy' ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <Brain className="h-4 w-4" />
              )}
              Save Autonomy Settings
            </Button>
          </CardContent>
        </Card>

        {/* Application Limits */}
        <Card className="glass-strong animate-fade-in" style={{ animationDelay: '50ms' }}>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="flex items-center gap-2">
                  <Sliders className="h-5 w-5 text-primary" />
                  Application Limits
                </CardTitle>
                <CardDescription>
                  Set boundaries for automated job applications
                </CardDescription>
              </div>
              {savedSections.has('autonomy') && (
                <span className="flex items-center gap-1 text-sm text-emerald-500">
                  <CheckCircle2 className="h-4 w-4" /> Saved
                </span>
              )}
            </div>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 sm:grid-cols-3">
              <div className="space-y-2">
                <Label>Daily Application Limit</Label>
                <Input
                  type="number"
                  min={0}
                  value={dailyLimit}
                  onChange={(e) => setDailyLimit(parseInt(e.target.value) || 0)}
                />
                <p className="text-xs text-muted-foreground">Max applications per day (default: 10)</p>
              </div>
              <div className="space-y-2">
                <Label>Salary Minimum ($)</Label>
                <Input
                  type="number"
                  min={0}
                  value={salaryMin || ''}
                  onChange={(e) => setSalaryMin(parseInt(e.target.value) || 0)}
                  placeholder="80000"
                />
              </div>
              <div className="space-y-2">
                <Label>Salary Maximum ($)</Label>
                <Input
                  type="number"
                  min={0}
                  value={salaryMax || ''}
                  onChange={(e) => setSalaryMax(parseInt(e.target.value) || 0)}
                  placeholder="150000"
                />
              </div>
            </div>
            <div className="mt-4">
              <Button
                onClick={handleSaveAutonomy}
                disabled={saving === 'autonomy'}
                className="gap-2 bg-gradient-primary hover:opacity-90"
              >
                {saving === 'autonomy' ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <Sliders className="h-4 w-4" />
                )}
                Save Limits
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Agent Preferences */}
        <Card className="glass-strong animate-fade-in" style={{ animationDelay: '100ms' }}>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="flex items-center gap-2">
                  <Gavel className="h-5 w-5 text-primary" />
                  Agent Preferences
                </CardTitle>
                <CardDescription>
                  Define the rules and preferences for your AI job agent
                </CardDescription>
              </div>
              {savedSections.has('agent') && (
                <span className="flex items-center gap-1 text-sm text-emerald-500">
                  <CheckCircle2 className="h-4 w-4" /> Saved
                </span>
              )}
            </div>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="space-y-2">
              <Label className="flex items-center gap-2">
                <Building2 className="h-4 w-4 text-muted-foreground" />
                Preferred Companies
              </Label>
              <Input
                value={agent.preferredCompanies}
                onChange={(e) => setAgent({ ...agent, preferredCompanies: e.target.value })}
                placeholder="Google, Microsoft, Stripe"
              />
              <p className="text-xs text-muted-foreground">Comma-separated list of companies you want to target</p>
            </div>

            <div className="space-y-2">
              <Label className="flex items-center gap-2">
                <Ban className="h-4 w-4 text-muted-foreground" />
                Companies to Avoid
              </Label>
              <Input
                value={agent.avoidCompanies}
                onChange={(e) => setAgent({ ...agent, avoidCompanies: e.target.value })}
                placeholder="Amazon, Meta"
              />
              <p className="text-xs text-muted-foreground">Comma-separated list of companies to skip</p>
            </div>

            <div className="space-y-2">
              <Label className="flex items-center gap-2">
                <Target className="h-4 w-4 text-muted-foreground" />
                Career Goal
              </Label>
              <Input
                value={agent.careerGoal}
                onChange={(e) => setAgent({ ...agent, careerGoal: e.target.value })}
                placeholder="Become a Senior Staff Engineer at a top tech company"
              />
              <p className="text-xs text-muted-foreground">Your long-term career aspiration</p>
            </div>

            <div className="space-y-2">
              <Label className="flex items-center gap-2">
                <Target className="h-4 w-4 text-muted-foreground" />
                Preferred Location
              </Label>
              <Input
                value={agent.preferredLocation}
                onChange={(e) => setAgent({ ...agent, preferredLocation: e.target.value })}
                placeholder="Remote, San Francisco, New York"
              />
              <p className="text-xs text-muted-foreground">Desired job location(s)</p>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label>Work Authorization</Label>
                <Select
                  value={agent.workAuthorization}
                  onValueChange={(val) => setAgent({ ...agent, workAuthorization: val })}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select authorization" />
                  </SelectTrigger>
                  <SelectContent>
                    {WORK_AUTHORIZATIONS.map((auth) => (
                      <SelectItem key={auth} value={auth}>
                        {auth}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label>Preferred Employment Type</Label>
                <Select
                  value={agent.employmentType}
                  onValueChange={(val) => setAgent({ ...agent, employmentType: val })}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select type" />
                  </SelectTrigger>
                  <SelectContent>
                    {EMPLOYMENT_TYPES.map((type) => (
                      <SelectItem key={type} value={type}>
                        {type}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <Button
              onClick={handleSaveAgent}
              disabled={saving === 'agent'}
              className="gap-2 bg-gradient-primary hover:opacity-90"
            >
              {saving === 'agent' ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <Gavel className="h-4 w-4" />
              )}
              Save Agent Preferences
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
