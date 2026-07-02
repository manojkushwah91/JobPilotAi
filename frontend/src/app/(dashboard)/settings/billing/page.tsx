'use client';

import { useState } from 'react';
import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { SettingsSidebar } from '@/components/features/settings/SettingsSidebar';
import { CreditCard, Check, X as XIcon, RefreshCw, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import type { BillingPlan, Invoice, PlanFeature } from '@/types';

const PLANS: PlanFeature[] = [
  { feature: 'Resume Analysis', free: true, premium: true, pro: true },
  { feature: 'AI Cover Letters', free: '5/month', premium: '50/month', pro: 'Unlimited' },
  { feature: 'Job Matching', free: true, premium: true, pro: true },
  { feature: 'Interview Practice', free: '3/month', premium: '30/month', pro: 'Unlimited' },
  { feature: 'Company Research', free: true, premium: true, pro: true },
  { feature: 'Analytics Dashboard', free: 'Basic', premium: 'Advanced', pro: 'Full Access' },
  { feature: 'Priority Support', free: false, premium: false, pro: true },
  { feature: 'API Access', free: false, premium: true, pro: true },
  { feature: 'Team Collaboration', free: false, premium: false, pro: true },
];

const TIER_LABELS: Record<string, string> = { FREE: 'Free', PREMIUM: 'Premium', PRO: 'Pro' };
const TIER_VARIANTS: Record<string, 'default' | 'secondary' | 'success'> = { FREE: 'secondary', PREMIUM: 'default', PRO: 'success' };

export default function BillingSettingsPage() {
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [changePlanDialogOpen, setChangePlanDialogOpen] = useState(false);

  const { data: planRes, isLoading: planLoading, isError: planError, refetch: refetchPlan } = useApiQuery<BillingPlan>(
    ['settings', 'billing'],
    API.settings.billing
  );

  const { data: invoicesRes, isLoading: invoicesLoading } = useApiQuery<Invoice[]>(
    ['settings', 'invoices'],
    API.settings.invoices
  );

  const cancelMutation = useApiMutation<void, void>('DELETE', API.settings.cancelSubscription, {
    onSuccess: () => {
      toast.success('Subscription cancelled');
      setCancelDialogOpen(false);
      refetchPlan();
    },
    onError: () => toast.error('Failed to cancel subscription'),
  });

  const changePlanMutation = useApiMutation<BillingPlan, { tier: string }>('PUT', API.settings.billing, {
    onSuccess: () => {
      toast.success('Plan changed successfully');
      setChangePlanDialogOpen(false);
      refetchPlan();
    },
    onError: () => toast.error('Failed to change plan'),
  });

  if (planLoading) {
    return (
      <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
        <SettingsSidebar />
        <div className="space-y-6">
          <Skeleton className="h-48 w-full" />
          <Skeleton className="h-64 w-full" />
        </div>
      </div>
    );
  }

  if (planError) {
    return (
      <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
        <SettingsSidebar />
        <Card>
          <CardContent className="flex flex-col items-center gap-4 py-12">
            <p className="text-destructive">Failed to load billing info</p>
            <Button variant="outline" onClick={() => refetchPlan()} className="gap-2">
              <RefreshCw className="h-4 w-4" />
              Retry
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const plan = planRes!.data;
  const invoices = invoicesRes?.data ?? [];
  const isFree = plan.tier === 'FREE';

  return (
    <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
      <SettingsSidebar />
      <div className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center justify-between">
              <span>Current Plan</span>
              <Badge variant={TIER_VARIANTS[plan.tier]}>{TIER_LABELS[plan.tier]}</Badge>
            </CardTitle>
            <CardDescription>
              {plan.status === 'ACTIVE' ? 'Your subscription is active' : `Status: ${plan.status}`}
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex gap-4">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <CreditCard className="h-4 w-4" />
                Current period: {new Date(plan.currentPeriodStart).toLocaleDateString()} - {new Date(plan.currentPeriodEnd).toLocaleDateString()}
              </div>
            </div>
            <div className="flex gap-2">
              <Dialog open={changePlanDialogOpen} onOpenChange={setChangePlanDialogOpen}>
                <DialogTrigger asChild>
                  <Button variant="outline">Change Plan</Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Change Plan</DialogTitle>
                    <DialogDescription>Choose a new subscription plan</DialogDescription>
                  </DialogHeader>
                  <div className="grid gap-3">
                    {['FREE', 'PREMIUM', 'PRO'].map((tier) => (
                      <Button
                        key={tier}
                        variant={plan.tier === tier ? 'default' : 'outline'}
                        className="justify-between"
                        onClick={() => {
                          if (tier !== plan.tier) {
                            changePlanMutation.mutate({ tier });
                          }
                        }}
                        disabled={plan.tier === tier || changePlanMutation.isPending}
                      >
                        <span>{TIER_LABELS[tier]}</span>
                        {plan.tier === tier && <Badge variant="secondary">Current</Badge>}
                      </Button>
                    ))}
                  </div>
                  <DialogFooter>
                    <Button variant="outline" onClick={() => setChangePlanDialogOpen(false)}>Cancel</Button>
                  </DialogFooter>
                </DialogContent>
              </Dialog>

              {!isFree && (
                <Dialog open={cancelDialogOpen} onOpenChange={setCancelDialogOpen}>
                  <DialogTrigger asChild>
                    <Button variant="destructive">Cancel Subscription</Button>
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>Cancel Subscription</DialogTitle>
                      <DialogDescription>
                        Are you sure you want to cancel? You&apos;ll lose access to premium features at the end of your billing period.
                      </DialogDescription>
                    </DialogHeader>
                    <DialogFooter>
                      <Button variant="outline" onClick={() => setCancelDialogOpen(false)}>Keep Subscription</Button>
                      <Button variant="destructive" onClick={() => cancelMutation.mutate()} disabled={cancelMutation.isPending}>
                        {cancelMutation.isPending ? 'Cancelling...' : 'Confirm Cancel'}
                      </Button>
                    </DialogFooter>
                  </DialogContent>
                </Dialog>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Compare Plans</CardTitle>
            <CardDescription>See what each plan includes</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-1/3">Feature</TableHead>
                  <TableHead>Free</TableHead>
                  <TableHead>Premium</TableHead>
                  <TableHead>Pro</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {PLANS.map((feature) => (
                  <TableRow key={feature.feature}>
                    <TableCell className="font-medium">{feature.feature}</TableCell>
                    <TableCell>
                      {typeof feature.free === 'boolean' ? (
                        feature.free ? <Check className="h-4 w-4 text-success" /> : <XIcon className="h-4 w-4 text-destructive" />
                      ) : (
                        feature.free
                      )}
                    </TableCell>
                    <TableCell>
                      {typeof feature.premium === 'boolean' ? (
                        feature.premium ? <Check className="h-4 w-4 text-success" /> : <XIcon className="h-4 w-4 text-destructive" />
                      ) : (
                        feature.premium
                      )}
                    </TableCell>
                    <TableCell>
                      {typeof feature.pro === 'boolean' ? (
                        feature.pro ? <Check className="h-4 w-4 text-success" /> : <XIcon className="h-4 w-4 text-destructive" />
                      ) : (
                        feature.pro
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Invoice History</CardTitle>
            <CardDescription>View your past invoices</CardDescription>
          </CardHeader>
          <CardContent>
            {invoicesLoading ? (
              <div className="space-y-2">
                <Skeleton className="h-10 w-full" />
                <Skeleton className="h-10 w-full" />
                <Skeleton className="h-10 w-full" />
              </div>
            ) : invoices.length === 0 ? (
              <p className="text-sm text-muted-foreground">No invoices found.</p>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Date</TableHead>
                    <TableHead>Amount</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Invoice</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {invoices.map((inv) => (
                    <TableRow key={inv.id}>
                      <TableCell>{new Date(inv.date).toLocaleDateString()}</TableCell>
                      <TableCell>${(inv.amount / 100).toFixed(2)} {inv.currency}</TableCell>
                      <TableCell>
                        <Badge variant={inv.status === 'PAID' ? 'success' : 'secondary'}>{inv.status}</Badge>
                      </TableCell>
                      <TableCell>
                        {inv.pdfUrl && (
                          <Button variant="link" size="sm" asChild>
                            <a href={inv.pdfUrl} target="_blank" rel="noopener noreferrer">Download</a>
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
