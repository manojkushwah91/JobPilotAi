'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useAuth } from '@/lib/auth/AuthProvider';
import { useApiQuery } from '@/lib/hooks/useQuery';
import { apiGet } from '@/lib/api/client';
import { API } from '@/lib/api/endpoints';
import { Skeleton } from '@/components/ui/skeleton';
import type { AgentBriefing, MissionResponse, TaskResponse } from '@/types';

function taskPhases(tasks: TaskResponse[]) {
  const phaseOrder = ['DISCOVER_JOBS', 'ANALYZE_JOB', 'RANK_JOB', 'TAILOR_RESUME', 'GENERATE_COVER_LETTER', 'SUBMIT_APPLICATION', 'VERIFY_APPLICATION', 'OBSERVE_RESULTS', 'UPDATE_MEMORY', 'PLAN_NEXT_ACTION'] as const;
  const active = new Set(tasks.filter(t => t.status === 'RUNNING' || t.status === 'PENDING').map(t => t.taskType));
  return phaseOrder.map(type => ({
    type,
    active: active.has(type),
    label: type.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase()),
  }));
}

export default function DashboardPage() {
  const { user } = useAuth();
  const userId = user?.id ?? '';

  const DATE = new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' });
  const TIME = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

  /* ─── Data ─── */

  const [briefing, setBriefing] = useState<AgentBriefing | null>(null);
  const [briefingLoading, setBriefingLoading] = useState(true);

  useEffect(() => {
    if (!userId) return;
    setBriefingLoading(true);
    apiGet<AgentBriefing>(API.agent.briefing(userId))
      .then(r => setBriefing(r.data ?? null))
      .catch(() => setBriefing(null))
      .finally(() => setBriefingLoading(false));
  }, [userId]);

  const { data: missionsData, isLoading: missionsLoading } = useApiQuery<MissionResponse[]>(
    ['missions'], API.agent.userMissions, undefined, { retry: false },
  );

  const mission = missionsData?.data?.find(m => m.status === 'ACTIVE' || m.status === 'PAUSED') ?? null;

  const [missionStatus, setMissionStatus] = useState<{ tasks: TaskResponse[]; isRunning: boolean } | null>(null);
  useEffect(() => {
    if (!mission?.id) { setMissionStatus(null); return; }
    apiGet<{ tasks: TaskResponse[]; isRunning: boolean }>(API.agent.missionStatus(mission.id))
      .then(r => setMissionStatus(r.data ?? null))
      .catch(() => setMissionStatus(null));
  }, [mission?.id]);

  const tasks = missionStatus?.tasks ?? [];
  const phases = taskPhases(tasks);

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
        {briefingLoading ? (
          <BriefingSkeleton />
        ) : hasBriefing ? (
          <div className="leading-relaxed text-foreground/85">
            {briefing.briefing.split('\n').map((line, i) =>
              line.trim() ? <p key={i} className="mb-1.5 last:mb-0">{line}</p> : null
            )}
          </div>
        ) : (
          <EmptyState
            message="No active mission yet."
            detail="Once you create your first mission I'll begin searching and preparing applications."
            action={{ label: 'Find Jobs', href: '/jobs' }}
          />
        )}
      </Section>

      <Divider />

      {/* ─── Current Mission ─── */}
      <Section title="Current Mission">
        {missionsLoading ? (
          <Skeleton className="h-16 w-full" />
        ) : mission ? (
          <div>
            <div className="mb-4 rounded-lg border border-border/20 bg-muted/20 p-3">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-sm font-medium">{mission.targetRole}</p>
                  <p className="text-xs text-muted-foreground/60">
                    {mission.targetLocation ?? 'Remote'} &middot; {mission.experienceLevel ?? 'Any'}
                  </p>
                </div>
                <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-medium ${
                  mission.status === 'ACTIVE' ? 'bg-success/10 text-success' : 'bg-muted/50 text-muted-foreground'
                }`}>
                  {mission.status === 'ACTIVE' ? 'Active' : 'Paused'}
                </span>
              </div>
            </div>

            <div className="mb-4 grid grid-cols-2 gap-3 text-sm sm:grid-cols-4">
              <Stat label="Jobs found" value={mission.totalJobsFound} />
              <Stat label="Applications" value={mission.totalApplicationsSubmitted} />
              <Stat label="Rejected" value={mission.totalRejected} />
              <Stat label="Pending" value={mission.totalPending} />
            </div>

            {tasks.length > 0 && phases.some(p => p.active) && (
              <div className="space-y-1.5">
                <p className="text-[11px] font-medium uppercase tracking-wider text-muted-foreground/50">Activity phases</p>
                {phases.filter(p => p.active).map(p => (
                  <div key={p.type} className="flex items-center gap-2 text-sm text-foreground/75">
                    <span className="h-1.5 w-1.5 rounded-full bg-success" />
                    {p.label}
                  </div>
                ))}
              </div>
            )}
          </div>
        ) : (
          <EmptyState
            message="No active mission yet."
            detail="Create your first mission and I'll begin searching for opportunities immediately."
            action={{ label: 'Find Jobs', href: '/jobs' }}
          />
        )}
      </Section>

      {/* ─── Stats row (only when data exists) ─── */}
      {(briefing && !briefingLoading && mission) && (
        <>
          <Divider />
          <Section title="Stats">
            <div className="grid grid-cols-3 gap-4 text-sm">
              <div>
                <p className="text-2xl font-semibold tabular-nums">{briefing.totalApplications}</p>
                <p className="text-[11px] text-muted-foreground/60">Total applications</p>
              </div>
              <div>
                <p className="text-2xl font-semibold tabular-nums">{briefing.totalInterviews}</p>
                <p className="text-[11px] text-muted-foreground/60">Interviews scheduled</p>
              </div>
              <div>
                <p className="text-2xl font-semibold tabular-nums">{briefing.consecutiveFailures}</p>
                <p className="text-[11px] text-muted-foreground/60">Consecutive failures</p>
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
            <div className="leading-relaxed text-foreground/80">
              {briefing.currentPlan.split('\n').map((line, i) =>
                line.trim() ? (
                  <p key={i} className="mb-1 text-sm last:mb-0 font-mono text-muted-foreground/70">{line}</p>
                ) : null
              )}
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
              {tasks
                .filter(t => t.status === 'RUNNING' || t.status === 'COMPLETED' || t.status === 'FAILED')
                .sort((a, b) => {
                  const da = a.startedAt ?? a.createdAt;
                  const db = b.startedAt ?? b.createdAt;
                  return da < db ? 1 : -1;
                })
                .slice(0, 15)
                .map(t => (
                  <div key={t.id} className="flex items-start gap-3 text-sm">
                    <span className={`mt-1 h-2 w-2 shrink-0 rounded-full ${
                      t.status === 'COMPLETED' ? 'bg-success' :
                      t.status === 'FAILED' ? 'bg-destructive' :
                      'bg-amber-500'
                    }`} />
                    <div className="flex-1">
                      <p className="text-foreground/80">{t.description}</p>
                      <p className="text-[11px] text-muted-foreground/40">
                        {t.taskType.replace(/_/g, ' ')} &middot; {t.startedAt ? new Date(t.startedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : null}
                      </p>
                    </div>
                    <span className={`shrink-0 text-[10px] ${
                      t.status === 'COMPLETED' ? 'text-success/70' :
                      t.status === 'FAILED' ? 'text-destructive/70' :
                      'text-amber-500/70'
                    }`}>
                      {t.status === 'COMPLETED' ? 'Done' : t.status === 'FAILED' ? 'Failed' : 'In progress'}
                    </span>
                  </div>
                ))}
            </div>
          ) : (
            <EmptyState
              message="No activity recorded yet."
              detail="The timeline will become your AI's work diary once your first mission begins."
            />
          )}
        </Section>
      </>

      {/* ─── Career Health ─── */}
      {(briefing && !briefingLoading && briefing.totalApplications + briefing.totalInterviews + briefing.consecutiveFailures > 0) && (
        <>
          <Divider />
          <Section title="Career Health">
            <div className="text-sm text-foreground/70">
              <p>Insufficient information for a complete health score.</p>
              <p className="mt-1 text-xs text-muted-foreground/50">More data is needed across applications, interviews, and outcomes.</p>
            </div>
          </Section>
        </>
      )}

      {/* ─── Bottom spacing ─── */}
      <div className="h-12" />
    </div>
  );
}

/* ─── Helpers ─── */

function Divider() {
  return <div className="my-6 border-t border-border/20" />;
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div>
      <h2 className="mb-3 text-xs font-semibold uppercase tracking-widest text-muted-foreground/50">{title}</h2>
      {children}
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

function BriefingSkeleton() {
  return (
    <div className="space-y-2">
      <Skeleton className="h-4 w-3/5" />
      <Skeleton className="h-4 w-full" />
      <Skeleton className="h-4 w-5/6" />
      <Skeleton className="h-4 w-4/6" />
    </div>
  );
}
