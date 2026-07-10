'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useAuth } from '@/lib/auth/AuthProvider';
import { useApiQuery } from '@/lib/hooks/useQuery';
import { apiGet, apiPost } from '@/lib/api/client';
import { API } from '@/lib/api/endpoints';
import { Skeleton } from '@/components/ui/skeleton';
import type { AgentBriefing, MissionResponse, TaskResponse } from '@/types';

/* ─── Helpers ─── */

function Divider() { return <div className="my-6 border-t border-border/20" />; }
function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div>
      <h2 className="mb-3 text-xs font-semibold uppercase tracking-widest text-muted-foreground/50">{title}</h2>
      {children}
    </div>
  );
}
function EmptyState({ message, detail, action }: { message: string; detail: string; action?: { label: string; href: string } }) {
  return (
    <div className="rounded-lg border border-border/10 bg-muted/10 p-4">
      <p className="text-sm font-medium text-foreground/70">{message}</p>
      <p className="mt-1 text-xs text-muted-foreground/50">{detail}</p>
      {action && (
        <Link href={action.href} className="mt-3 inline-flex items-center gap-1 text-xs font-medium text-primary underline underline-offset-2 hover:no-underline">
          {action.label}
        </Link>
      )}
    </div>
  );
}
function BriefingSkeleton() { return (
  <div className="space-y-2"><Skeleton className="h-4 w-3/5" /><Skeleton className="h-4 w-full" /><Skeleton className="h-4 w-5/6" /><Skeleton className="h-4 w-4/6" /></div>
); }

export default function DashboardPage() {
  const { user } = useAuth();
  const userId = user?.id ?? '';
  const DATE = new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' });
  const TIME = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

  /* ─── Briefing ─── */
  const [briefing, setBriefing] = useState<AgentBriefing | null>(null);
  const [briefingLoading, setBriefingLoading] = useState(true);
  useEffect(() => {
    if (!userId) return;
    setBriefingLoading(true);
    apiGet<AgentBriefing>(API.agent.briefing(userId))
      .then(r => setBriefing(r.data ?? null)).catch(() => setBriefing(null))
      .finally(() => setBriefingLoading(false));
  }, [userId]);

  /* ─── Missions ─── */
  const { data: missionsData, isLoading: missionsLoading } = useApiQuery<MissionResponse[]>(
    ['missions'], API.agent.userMissions, undefined, { retry: false },
  );
  const mission = missionsData?.data?.find(m => m.status === 'ACTIVE' || m.status === 'PAUSED') ?? null;

  /* ─── Tasks ─── */
  const [missionStatus, setMissionStatus] = useState<{ tasks: TaskResponse[] } | null>(null);
  useEffect(() => {
    if (!mission?.id) { setMissionStatus(null); return; }
    apiGet<{ tasks: TaskResponse[] }>(API.agent.missionStatus(mission.id))
      .then(r => setMissionStatus(r.data ?? null)).catch(() => setMissionStatus(null));
  }, [mission?.id]);
  const tasks = missionStatus?.tasks ?? [];

  /* ─── Career Health ─── */
  const [health, setHealth] = useState<{ status: string; score?: number; breakdown?: Record<string, number> } | null>(null);
  useEffect(() => {
    if (!userId) return;
    apiGet<{ status: string; score?: number; breakdown?: Record<string, number> }>(API.agent.health(userId))
      .then(r => setHealth(r.data ?? null)).catch(() => setHealth(null));
  }, [userId]);

  /* ─── Recommendations ─── */
  const [recommendations, setRecommendations] = useState<{ title: string; description: string; reason: string; confidence: number; expectedImpact: string }[]>([]);
  useEffect(() => {
    if (!userId) return;
    apiGet<{ title: string; description: string; reason: string; confidence: number; expectedImpact: string }[]>(API.agent.recommendations(userId))
      .then(r => setRecommendations(r.data ?? [])).catch(() => setRecommendations([]));
  }, [userId]);

  /* ─── Requires Attention ─── */
  const [attention, setAttention] = useState<{ type: string; message: string; detail: string; timestamp: string }[]>([]);
  useEffect(() => {
    if (!userId) return;
    apiGet<{ type: string; message: string; detail: string; timestamp: string }[]>(API.agent.attention(userId))
      .then(r => setAttention(r.data ?? [])).catch(() => setAttention([]));
  }, [userId]);

  /* ─── Approvals ─── */
  const [approvals, setApprovals] = useState<{ taskId: string; description: string; taskType: string; createdAt: string }[]>([]);
  const fetchApprovals = () => {
    apiGet<{ taskId: string; description: string; taskType: string; createdAt: string }[]>(API.agent.approvals)
      .then(r => setApprovals(r.data ?? [])).catch(() => setApprovals([]));
  };
  useEffect(() => { fetchApprovals(); }, []);

  const handleApprove = async (taskId: string) => {
    await apiPost(API.agent.approveTask(taskId)); fetchApprovals();
  };
  const handleReject = async (taskId: string) => {
    await apiPost(API.agent.rejectTask(taskId), { reason: 'Rejected by user' }); fetchApprovals();
  };

  /* ─── Feedback ─── */
  const [feedbackSent, setFeedbackSent] = useState<Set<string>>(new Set());
  const handleFeedback = async (taskId: string, positive: boolean) => {
    await apiPost(API.agent.feedback, { taskId, positive }); setFeedbackSent(prev => new Set(prev).add(taskId));
  };

  /* ─── Mission creation modal ─── */
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({ title: '', targetRole: '', targetLocation: '', salaryMin: '', salaryMax: '', skills: '' });
  const [creating, setCreating] = useState(false);
  const [created, setCreated] = useState(false);

  const handleCreate = async () => {
    setCreating(true);
    try {
      await apiPost(API.agent.missions, {
        userId,
        title: form.title || form.targetRole,
        targetRole: form.targetRole,
        targetLocation: form.targetLocation || null,
        salaryMin: form.salaryMin ? parseInt(form.salaryMin) : null,
        salaryMax: form.salaryMax ? parseInt(form.salaryMax) : null,
        preferredSkills: form.skills ? form.skills.split(',').map(s => s.trim()) : [],
      });
      setCreated(true);
      setShowCreate(false);
    } catch { /* ignore */ } finally { setCreating(false); }
  };

  const hasBriefing = briefing?.briefing && briefing.briefing.length > 0 && briefing.briefing !== 'No agent state available.';
  const hasPlan = briefing?.currentPlan && briefing.currentPlan !== 'No plan' && briefing.currentPlan.length > 0;
  const hasReview = briefing?.latestWeeklyReview && briefing.latestWeeklyReview.length > 0;

  return (
    <div className="mx-auto max-w-3xl space-y-0">

      {/* ─── Header ─── */}
      <div className="flex items-center gap-3 pb-6">
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
          <svg className="h-5 w-5 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M9.813 15.904 9 18.75l-.813-2.846a4.5 4.5 0 0 0-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 0 0 3.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 0 0 3.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 0 0-3.09 3.09ZM18.259 8.715 18 9.75l-.259-1.035a3.375 3.375 0 0 0-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 0 0 2.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 0 0 2.455 2.456L21.75 6l-1.036.259a3.375 3.375 0 0 0-2.455 2.456ZM16.894 20.567 16.5 21.75l-.394-1.183a2.25 2.25 0 0 0-1.423-1.423L13.5 18.75l1.183-.394a2.25 2.25 0 0 0 1.423-1.423l.394-1.183.394 1.183a2.25 2.25 0 0 0 1.423 1.423l1.183.394-1.183.394a2.25 2.25 0 0 0-1.423 1.423Z" />
          </svg>
        </div>
        <div>
          <div className="flex items-center gap-2">
            <span className="text-sm font-semibold">JobPilot AI</span>
            <span className="text-[11px] text-muted-foreground/50">BOT</span>
          </div>
          <div className="flex items-center gap-2 text-xs text-muted-foreground/60">
            <span>#daily-report</span>
            <span className="text-muted-foreground/30">&middot;</span>
            <span>{DATE}</span>
          </div>
        </div>
        <span className="ml-auto text-[11px] text-muted-foreground/40">{TIME}</span>
      </div>

      {/* ─── Daily Briefing ─── */}
      <Section title="Today's Briefing">
        {briefingLoading ? <BriefingSkeleton /> : hasBriefing ? (
          <div className="leading-relaxed text-foreground/85">
            {briefing.briefing.split('\n').map((line, i) =>
              line.trim() ? <p key={i} className="mb-1.5 last:mb-0">{line}</p> : null
            )}
          </div>
        ) : (
          <EmptyState message="No active mission yet." detail="Once you create your first mission I'll begin searching and preparing applications." />
        )}
      </Section>

      <Divider />

      {/* ─── Current Mission + Create ─── */}
      <Section title="Current Mission">
        {missionsLoading ? <Skeleton className="h-16 w-full" /> : mission ? (
          <div>
            <div className="mb-4 rounded-lg border border-border/20 bg-muted/20 p-3">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-sm font-medium">{mission.targetRole}</p>
                  <p className="text-xs text-muted-foreground/60">{mission.targetLocation ?? 'Remote'} &middot; {mission.experienceLevel ?? 'Any'}</p>
                </div>
                <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-medium ${mission.status === 'ACTIVE' ? 'bg-success/10 text-success' : 'bg-muted/50 text-muted-foreground'}`}>
                  {mission.status === 'ACTIVE' ? 'Active' : 'Paused'}
                </span>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3 text-sm sm:grid-cols-4">
              <Stat label="Jobs found" value={mission.totalJobsFound} />
              <Stat label="Applications" value={mission.totalApplicationsSubmitted} />
              <Stat label="Rejected" value={mission.totalRejected} />
              <Stat label="Pending" value={mission.totalPending} />
            </div>
          </div>
        ) : missionsData?.data && missionsData.data.length > 0 ? (
          <div className="space-y-2">
            <p className="text-sm text-muted-foreground/70">No active mission. You have {missionsData.data.length} completed or cancelled missions.</p>
            <button onClick={() => setShowCreate(true)} className="text-xs font-medium text-primary underline underline-offset-2 hover:no-underline">Create a new mission</button>
          </div>
        ) : (
          <EmptyState message="No missions yet." detail="Create your first mission and I'll begin searching for opportunities immediately." action={{ label: 'Create Mission', href: '#' }} />
        )}
        {!mission && <button onClick={() => setShowCreate(true)} className="mt-3 inline-flex items-center gap-1 text-xs font-medium text-primary underline underline-offset-2 hover:no-underline">Create New Mission</button>}

        {tasks.filter(t => t.status === 'RUNNING').length > 0 && (
          <div className="mt-4 space-y-1.5">
            <p className="text-[11px] font-medium uppercase tracking-wider text-muted-foreground/50">Running</p>
            {tasks.filter(t => t.status === 'RUNNING').map(t => (
              <div key={t.id} className="flex items-center gap-2 text-sm text-foreground/75">
                <span className="h-1.5 w-1.5 rounded-full bg-success animate-pulse" />
                {t.description}
              </div>
            ))}
          </div>
        )}
      </Section>

      {/* ─── Requires Attention ─── */}
      {attention.length > 0 && (
        <>
          <Divider />
          <Section title="Requires Attention">
            <div className="space-y-2">
              {attention.map((a, i) => (
                <div key={i} className="flex items-start gap-3 rounded-lg border border-amber-500/20 bg-amber-500/5 p-3 text-sm">
                  <span className={`mt-0.5 h-2 w-2 shrink-0 rounded-full ${a.type === 'captcha' ? 'bg-amber-500' : a.type === 'interview' ? 'bg-success' : 'bg-destructive'}`} />
                  <div className="flex-1">
                    <p className="font-medium text-foreground/85">{a.message}</p>
                    <p className="text-xs text-muted-foreground/60 mt-0.5">{a.detail}</p>
                  </div>
                </div>
              ))}
            </div>
          </Section>
        </>
      )}

      {/* ─── Pending Approval ─── */}
      {approvals.length > 0 && (
        <>
          <Divider />
          <Section title="Pending Approval (Mode 2)">
            <div className="space-y-2">
              {approvals.map(a => (
                <div key={a.taskId} className="flex items-center justify-between rounded-lg border border-border/15 bg-muted/15 p-3 text-sm">
                  <div className="flex-1">
                    <p className="text-foreground/85">{a.description}</p>
                    <p className="text-[11px] text-muted-foreground/40">{a.taskType.replace(/_/g, ' ')}</p>
                  </div>
                  <div className="flex items-center gap-2 shrink-0 ml-3">
                    <button onClick={() => handleApprove(a.taskId)} className="rounded-lg bg-success/10 px-3 py-1.5 text-xs font-medium text-success hover:bg-success/20">Approve</button>
                    <button onClick={() => handleReject(a.taskId)} className="rounded-lg bg-destructive/10 px-3 py-1.5 text-xs font-medium text-destructive hover:bg-destructive/20">Reject</button>
                  </div>
                </div>
              ))}
            </div>
          </Section>
        </>
      )}

      {/* ─── Stats ─── */}
      {(briefing && !briefingLoading && (briefing.totalApplications + briefing.totalInterviews + briefing.consecutiveFailures > 0)) && (
        <>
          <Divider />
          <Section title="Stats">
            <div className="grid grid-cols-3 gap-4 text-sm">
              <div><p className="text-2xl font-semibold tabular-nums">{briefing.totalApplications}</p><p className="text-[11px] text-muted-foreground/60">Total applications</p></div>
              <div><p className="text-2xl font-semibold tabular-nums">{briefing.totalInterviews}</p><p className="text-[11px] text-muted-foreground/60">Interviews scheduled</p></div>
              <div><p className="text-2xl font-semibold tabular-nums">{briefing.consecutiveFailures}</p><p className="text-[11px] text-muted-foreground/60">Consecutive failures</p></div>
            </div>
          </Section>
        </>
      )}

      {/* ─── Career Health ─── */}
      {health && health.status !== 'insufficient_data' && health.score !== undefined && (
        <>
          <Divider />
          <Section title="Career Health">
            <div className="flex items-center gap-6">
              <div className="flex h-20 w-20 items-center justify-center rounded-full border-4 border-success/30">
                <span className="text-2xl font-bold tabular-nums">{health.score}</span>
              </div>
              <div className="text-sm text-foreground/70 space-y-1">
                {health.breakdown && (
                  <>
                    <p>Application volume: {health.breakdown.applicationVolume}</p>
                    <p>Application score: +{health.breakdown.applicationScore}</p>
                    <p>Interview score: +{health.breakdown.interviewScore}</p>
                    {health.breakdown.failurePenalty > 0 && <p>Failure penalty: -{health.breakdown.failurePenalty}</p>}
                  </>
                )}
              </div>
            </div>
          </Section>
        </>
      )}

      {/* ─── Current Plan ─── */}
      {hasPlan && (
        <>
          <Divider />
          <Section title="Current Plan">
            <div className="font-mono text-sm leading-relaxed text-muted-foreground/70">
              {briefing.currentPlan.split('\n').map((line, i) =>
                line.trim() ? <p key={i} className="mb-1 last:mb-0">{line}</p> : null
              )}
            </div>
          </Section>
        </>
      )}

      {/* ─── Recommendations ─── */}
      {recommendations.length > 0 && (
        <>
          <Divider />
          <Section title="Recommendations">
            <div className="space-y-3">
              {recommendations.map((r, i) => (
                <div key={i} className="rounded-lg border border-border/15 bg-muted/15 p-3">
                  <div className="flex items-start justify-between mb-2">
                    <p className="text-sm font-medium text-foreground/85">{r.title}</p>
                    <div className="flex items-center gap-2 shrink-0">
                      <span className="rounded-full bg-primary/10 px-2 py-0.5 text-[10px] font-medium text-primary">{r.confidence}%</span>
                      <span className={`rounded-full px-2 py-0.5 text-[10px] font-medium ${r.expectedImpact === 'High' ? 'bg-success/10 text-success' : 'bg-warning/10 text-warning'}`}>{r.expectedImpact}</span>
                    </div>
                  </div>
                  <p className="text-xs text-muted-foreground/60 mb-2">{r.description}</p>
                  <p className="text-[11px] text-muted-foreground/40 italic">Why: {r.reason}</p>
                </div>
              ))}
            </div>
          </Section>
        </>
      )}

      {/* ─── Weekly Review ─── */}
      {hasReview && (
        <>
          <Divider />
          <Section title="Weekly Review">
            <div className="leading-relaxed text-foreground/80">
              {briefing.latestWeeklyReview.split('\n').map((line, i) =>
                line.trim() ? <p key={i} className="mb-1.5 text-sm last:mb-0">{line}</p> : null
              )}
            </div>
          </Section>
        </>
      )}

      {/* ─── Activity ─── */}
      <>
        <Divider />
        <Section title="Activity">
          {tasks.length > 0 ? (
            <div className="space-y-2">
              {tasks.filter(t => t.status === 'RUNNING' || t.status === 'COMPLETED' || t.status === 'FAILED')
                .sort((a, b) => {
                  const da = a.startedAt ?? a.createdAt;
                  const db = b.startedAt ?? b.createdAt;
                  return da < db ? 1 : -1;
                }).slice(0, 15).map(t => (
                  <div key={t.id} className="flex items-start gap-3 text-sm">
                    <span className={`mt-1 h-2 w-2 shrink-0 rounded-full ${t.status === 'COMPLETED' ? 'bg-success' : t.status === 'FAILED' ? 'bg-destructive' : 'bg-amber-500'}`} />
                    <div className="flex-1">
                      <p className="text-foreground/80">{t.description}</p>
                      <p className="text-[11px] text-muted-foreground/40">{t.taskType.replace(/_/g, ' ')} &middot; {t.startedAt ? new Date(t.startedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}</p>
                    </div>
                    <div className="flex items-center gap-1.5 shrink-0">
                      <span className={`text-[10px] ${t.status === 'COMPLETED' ? 'text-success/70' : t.status === 'FAILED' ? 'text-destructive/70' : 'text-amber-500/70'}`}>
                        {t.status === 'COMPLETED' ? 'Done' : t.status === 'FAILED' ? 'Failed' : 'In progress'}
                      </span>
                      {t.status === 'COMPLETED' && !feedbackSent.has(t.id) && (
                        <div className="flex items-center gap-1 ml-2">
                          <button onClick={() => handleFeedback(t.id, true)} className="text-muted-foreground/40 hover:text-success transition-colors" title="Good job">&#x1F44D;</button>
                          <button onClick={() => handleFeedback(t.id, false)} className="text-muted-foreground/40 hover:text-destructive transition-colors" title="Not helpful">&#x1F44E;</button>
                        </div>
                      )}
                      {feedbackSent.has(t.id) && <span className="text-[9px] text-muted-foreground/30 ml-2">&#x2713;</span>}
                    </div>
                  </div>
                ))}
            </div>
          ) : (
            <EmptyState message="No activity recorded yet." detail="The timeline will become your AI's work diary once your first mission begins." />
          )}
        </Section>
      </>

      <div className="h-12" />

      {/* ─── Create Mission Modal ─── */}
      {showCreate && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50" onClick={() => setShowCreate(false)}>
          <div className="mx-4 w-full max-w-lg rounded-xl bg-background p-6 shadow-2xl" onClick={e => e.stopPropagation()}>
            <h2 className="text-lg font-semibold mb-4">Create Mission</h2>
            <div className="space-y-3">
              <div>
                <label className="text-xs font-medium text-muted-foreground/70">Target Role</label>
                <input className="mt-1 w-full rounded-lg border border-border/30 bg-muted/20 px-3 py-2 text-sm focus:border-primary focus:outline-none" placeholder="e.g. Senior Java Backend Engineer" value={form.targetRole} onChange={e => setForm({ ...form, targetRole: e.target.value })} />
              </div>
              <div>
                <label className="text-xs font-medium text-muted-foreground/70">Target Location</label>
                <input className="mt-1 w-full rounded-lg border border-border/30 bg-muted/20 px-3 py-2 text-sm focus:border-primary focus:outline-none" placeholder="e.g. San Francisco or Remote" value={form.targetLocation} onChange={e => setForm({ ...form, targetLocation: e.target.value })} />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs font-medium text-muted-foreground/70">Salary Min</label>
                  <input className="mt-1 w-full rounded-lg border border-border/30 bg-muted/20 px-3 py-2 text-sm focus:border-primary focus:outline-none" placeholder="e.g. 120000" value={form.salaryMin} onChange={e => setForm({ ...form, salaryMin: e.target.value })} />
                </div>
                <div>
                  <label className="text-xs font-medium text-muted-foreground/70">Salary Max</label>
                  <input className="mt-1 w-full rounded-lg border border-border/30 bg-muted/20 px-3 py-2 text-sm focus:border-primary focus:outline-none" placeholder="e.g. 180000" value={form.salaryMax} onChange={e => setForm({ ...form, salaryMax: e.target.value })} />
                </div>
              </div>
              <div>
                <label className="text-xs font-medium text-muted-foreground/70">Preferred Skills (comma-separated)</label>
                <input className="mt-1 w-full rounded-lg border border-border/30 bg-muted/20 px-3 py-2 text-sm focus:border-primary focus:outline-none" placeholder="e.g. Java, Spring Boot, Kafka" value={form.skills} onChange={e => setForm({ ...form, skills: e.target.value })} />
              </div>
            </div>
            <div className="mt-6 flex justify-end gap-3">
              <button onClick={() => setShowCreate(false)} className="rounded-lg px-4 py-2 text-sm text-muted-foreground hover:text-foreground">Cancel</button>
              <button disabled={creating || !form.targetRole} onClick={handleCreate} className="rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground disabled:opacity-50">
                {creating ? 'Creating...' : 'Create Mission'}
              </button>
            </div>
          </div>
        </div>
      )}

      {created && (
        <div className="fixed bottom-6 right-6 z-50 rounded-lg bg-success/10 border border-success/20 p-4 text-sm text-success shadow-lg">
          Mission created. Start it from the mission details page.
          <button onClick={() => setCreated(false)} className="ml-3 text-xs underline">Dismiss</button>
        </div>
      )}
    </div>
  );
}

function Stat({ label, value }: { label: string; value: number }) {
  return (
    <div className="rounded-lg border border-border/15 bg-muted/15 p-2.5">
      <p className="text-lg font-semibold tabular-nums">{value}</p>
      <p className="text-[10px] text-muted-foreground/60">{label}</p>
    </div>
  );
}
