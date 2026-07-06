'use client';

import { useState, useEffect } from 'react';
import { useApiQuery, useApiMutation } from '@/lib/hooks/useQuery';
import { API } from '@/lib/api/endpoints';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Skeleton } from '@/components/ui/skeleton';
import { SettingsSidebar } from '@/components/features/settings/SettingsSidebar';
import { User, Loader2, Plus, X } from 'lucide-react';
import { toast } from 'sonner';

interface CandidateProfile {
  id?: string;
  fullName: string;
  email: string;
  phone: string;
  location: string;
  headline: string;
  summary: string;
  skills: string[];
  experience: string[];
  education: string[];
  resumeText: string;
  linkedinUrl: string;
  portfolioUrl: string;
  yearsExperience: number;
  desiredRole: string;
  desiredLocation: string;
  salaryExpectationMin: number;
  salaryExpectationMax: number;
  currency: string;
  employmentType: string;
  workPreference: string;
}

const emptyProfile: CandidateProfile = {
  fullName: '', email: '', phone: '', location: '', headline: '', summary: '',
  skills: [], experience: [], education: [], resumeText: '',
  linkedinUrl: '', portfolioUrl: '', yearsExperience: 0,
  desiredRole: '', desiredLocation: '', salaryExpectationMin: 0,
  salaryExpectationMax: 0, currency: 'USD', employmentType: '', workPreference: '',
};

export default function ProfileSettingsPage() {
  const [form, setForm] = useState<CandidateProfile>(emptyProfile);
  const [newSkill, setNewSkill] = useState('');
  const [newExperience, setNewExperience] = useState('');
  const [newEducation, setNewEducation] = useState('');
  const [initialized, setInitialized] = useState(false);

  const { data: res, isLoading, refetch } = useApiQuery<CandidateProfile>(
    ['candidateProfile'],
    API.candidateProfile.get
  );

  useEffect(() => {
    if (!initialized && res?.data) {
      const d = res.data as any;
      setForm({
        fullName: d.fullName || '',
        email: d.email || '',
        phone: d.phone || '',
        location: d.location || '',
        headline: d.headline || '',
        summary: d.summary || '',
        skills: d.skills || [],
        experience: d.experience || [],
        education: d.education || [],
        resumeText: d.resumeText || '',
        linkedinUrl: d.linkedinUrl || '',
        portfolioUrl: d.portfolioUrl || '',
        yearsExperience: d.yearsExperience || 0,
        desiredRole: d.desiredRole || '',
        desiredLocation: d.desiredLocation || '',
        salaryExpectationMin: d.salaryExpectationMin || 0,
        salaryExpectationMax: d.salaryExpectationMax || 0,
        currency: d.currency || 'USD',
        employmentType: d.employmentType || '',
        workPreference: d.workPreference || '',
      });
      setInitialized(true);
    }
  }, [res, initialized]);

  const saveBasic = useApiMutation<any, any>('PUT', API.candidateProfile.update, {
    onSuccess: () => { toast.success('Profile saved'); refetch(); },
    onError: () => toast.error('Failed to save profile'),
  });

  const saveSkills = useApiMutation<any, any>('PUT', API.candidateProfile.skills, {
    onSuccess: () => { toast.success('Skills saved'); refetch(); },
    onError: () => toast.error('Failed to save skills'),
  });

  const saveExperience = useApiMutation<any, any>('PUT', API.candidateProfile.experience, {
    onSuccess: () => { toast.success('Experience saved'); refetch(); },
    onError: () => toast.error('Failed to save experience'),
  });

  const saveEducation = useApiMutation<any, any>('PUT', API.candidateProfile.education, {
    onSuccess: () => { toast.success('Education saved'); refetch(); },
    onError: () => toast.error('Failed to save education'),
  });

  const savePreferences = useApiMutation<any, any>('PUT', API.candidateProfile.preferences, {
    onSuccess: () => { toast.success('Preferences saved'); refetch(); },
    onError: () => toast.error('Failed to save preferences'),
  });

  if (isLoading) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">Settings</h1>
        <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
          <SettingsSidebar />
          <Card><CardContent className="space-y-4 py-6">
            <Skeleton className="h-10 w-full" /><Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" /><Skeleton className="h-10 w-full" />
          </CardContent></Card>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Settings</h1>
      <div className="grid gap-6 lg:grid-cols-[240px_1fr]">
        <SettingsSidebar />
        <div className="space-y-6">

          {/* Basic Info */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2"><User className="h-5 w-5" /> Basic Information</CardTitle>
              <CardDescription>Your personal details for job applications</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label>Full Name *</Label>
                  <Input value={form.fullName} onChange={e => setForm({ ...form, fullName: e.target.value })} placeholder="John Doe" />
                </div>
                <div className="space-y-2">
                  <Label>Email *</Label>
                  <Input type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} placeholder="john@example.com" />
                </div>
                <div className="space-y-2">
                  <Label>Phone</Label>
                  <Input value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value })} placeholder="+1 234 567 890" />
                </div>
                <div className="space-y-2">
                  <Label>Location</Label>
                  <Input value={form.location} onChange={e => setForm({ ...form, location: e.target.value })} placeholder="San Francisco, CA" />
                </div>
              </div>
              <div className="space-y-2">
                <Label>Headline</Label>
                <Input value={form.headline} onChange={e => setForm({ ...form, headline: e.target.value })} placeholder="Senior Software Engineer" />
              </div>
              <div className="space-y-2">
                <Label>Summary</Label>
                <Textarea value={form.summary} onChange={e => setForm({ ...form, summary: e.target.value })} placeholder="Brief professional summary..." rows={3} />
              </div>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label>LinkedIn URL</Label>
                  <Input value={form.linkedinUrl} onChange={e => setForm({ ...form, linkedinUrl: e.target.value })} placeholder="https://linkedin.com/in/..." />
                </div>
                <div className="space-y-2">
                  <Label>Portfolio URL</Label>
                  <Input value={form.portfolioUrl} onChange={e => setForm({ ...form, portfolioUrl: e.target.value })} placeholder="https://..." />
                </div>
              </div>
              <Button onClick={() => saveBasic.mutate({ fullName: form.fullName, phone: form.phone, location: form.location, headline: form.headline, summary: form.summary })} disabled={saveBasic.isPending} className="gap-2">
                {saveBasic.isPending && <Loader2 className="h-4 w-4 animate-spin" />} Save Basic Info
              </Button>
            </CardContent>
          </Card>

          {/* Skills */}
          <Card>
            <CardHeader>
              <CardTitle>Skills</CardTitle>
              <CardDescription>Skills the agent will use for job matching</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex flex-wrap gap-2">
                {form.skills.map((skill, i) => (
                  <span key={i} className="inline-flex items-center gap-1 rounded-full bg-primary/10 px-3 py-1 text-sm">
                    {skill}
                    <button onClick={() => setForm({ ...form, skills: form.skills.filter((_, j) => j !== i) })} className="ml-1 hover:text-destructive"><X className="h-3 w-3" /></button>
                  </span>
                ))}
              </div>
              <div className="flex gap-2">
                <Input value={newSkill} onChange={e => setNewSkill(e.target.value)} placeholder="Add a skill..." onKeyDown={e => { if (e.key === 'Enter' && newSkill.trim()) { setForm({ ...form, skills: [...form.skills, newSkill.trim()] }); setNewSkill(''); } }} />
                <Button variant="outline" size="icon" onClick={() => { if (newSkill.trim()) { setForm({ ...form, skills: [...form.skills, newSkill.trim()] }); setNewSkill(''); } }}><Plus className="h-4 w-4" /></Button>
              </div>
              <Button onClick={() => saveSkills.mutate({ skills: form.skills })} disabled={saveSkills.isPending} className="gap-2">
                {saveSkills.isPending && <Loader2 className="h-4 w-4 animate-spin" />} Save Skills
              </Button>
            </CardContent>
          </Card>

          {/* Experience */}
          <Card>
            <CardHeader>
              <CardTitle>Experience</CardTitle>
              <CardDescription>Work experience for the agent to reference</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {form.experience.map((exp, i) => (
                <div key={i} className="flex items-start gap-2">
                  <p className="flex-1 text-sm">{exp}</p>
                  <button onClick={() => setForm({ ...form, experience: form.experience.filter((_, j) => j !== i) })} className="shrink-0 hover:text-destructive"><X className="h-4 w-4" /></button>
                </div>
              ))}
              <div className="flex gap-2">
                <Input value={newExperience} onChange={e => setNewExperience(e.target.value)} placeholder="e.g. Software Engineer at Google (2020-2023)" onKeyDown={e => { if (e.key === 'Enter' && newExperience.trim()) { setForm({ ...form, experience: [...form.experience, newExperience.trim()] }); setNewExperience(''); } }} />
                <Button variant="outline" size="icon" onClick={() => { if (newExperience.trim()) { setForm({ ...form, experience: [...form.experience, newExperience.trim()] }); setNewExperience(''); } }}><Plus className="h-4 w-4" /></Button>
              </div>
              <Button onClick={() => saveExperience.mutate({ experience: form.experience })} disabled={saveExperience.isPending} className="gap-2">
                {saveExperience.isPending && <Loader2 className="h-4 w-4 animate-spin" />} Save Experience
              </Button>
            </CardContent>
          </Card>

          {/* Education */}
          <Card>
            <CardHeader>
              <CardTitle>Education</CardTitle>
              <CardDescription>Educational background</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {form.education.map((edu, i) => (
                <div key={i} className="flex items-start gap-2">
                  <p className="flex-1 text-sm">{edu}</p>
                  <button onClick={() => setForm({ ...form, education: form.education.filter((_, j) => j !== i) })} className="shrink-0 hover:text-destructive"><X className="h-4 w-4" /></button>
                </div>
              ))}
              <div className="flex gap-2">
                <Input value={newEducation} onChange={e => setNewEducation(e.target.value)} placeholder="e.g. B.S. Computer Science, MIT (2016-2020)" onKeyDown={e => { if (e.key === 'Enter' && newEducation.trim()) { setForm({ ...form, education: [...form.education, newEducation.trim()] }); setNewEducation(''); } }} />
                <Button variant="outline" size="icon" onClick={() => { if (newEducation.trim()) { setForm({ ...form, education: [...form.education, newEducation.trim()] }); setNewEducation(''); } }}><Plus className="h-4 w-4" /></Button>
              </div>
              <Button onClick={() => saveEducation.mutate({ education: form.education })} disabled={saveEducation.isPending} className="gap-2">
                {saveEducation.isPending && <Loader2 className="h-4 w-4 animate-spin" />} Save Education
              </Button>
            </CardContent>
          </Card>

          {/* Resume Text */}
          <Card>
            <CardHeader>
              <CardTitle>Resume</CardTitle>
              <CardDescription>Paste your resume text for the agent to use when applying</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <Textarea value={form.resumeText} onChange={e => setForm({ ...form, resumeText: e.target.value })} placeholder="Paste your resume content here..." rows={8} />
              <Button onClick={() => saveBasic.mutate({ resumeText: form.resumeText })} disabled={saveBasic.isPending} className="gap-2">
                {saveBasic.isPending && <Loader2 className="h-4 w-4 animate-spin" />} Save Resume
              </Button>
            </CardContent>
          </Card>

          {/* Job Preferences */}
          <Card>
            <CardHeader>
              <CardTitle>Job Preferences</CardTitle>
              <CardDescription>What kind of jobs the agent should target</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label>Desired Role</Label>
                  <Input value={form.desiredRole} onChange={e => setForm({ ...form, desiredRole: e.target.value })} placeholder="Software Engineer" />
                </div>
                <div className="space-y-2">
                  <Label>Desired Location</Label>
                  <Input value={form.desiredLocation} onChange={e => setForm({ ...form, desiredLocation: e.target.value })} placeholder="Remote, San Francisco" />
                </div>
                <div className="space-y-2">
                  <Label>Salary Min</Label>
                  <Input type="number" value={form.salaryExpectationMin || ''} onChange={e => setForm({ ...form, salaryExpectationMin: parseInt(e.target.value) || 0 })} placeholder="80000" />
                </div>
                <div className="space-y-2">
                  <Label>Salary Max</Label>
                  <Input type="number" value={form.salaryExpectationMax || ''} onChange={e => setForm({ ...form, salaryExpectationMax: parseInt(e.target.value) || 0 })} placeholder="150000" />
                </div>
                <div className="space-y-2">
                  <Label>Employment Type</Label>
                  <Input value={form.employmentType} onChange={e => setForm({ ...form, employmentType: e.target.value })} placeholder="Full-time" />
                </div>
                <div className="space-y-2">
                  <Label>Work Preference</Label>
                  <Input value={form.workPreference} onChange={e => setForm({ ...form, workPreference: e.target.value })} placeholder="Remote, Hybrid, On-site" />
                </div>
                <div className="space-y-2">
                  <Label>Years of Experience</Label>
                  <Input type="number" value={form.yearsExperience || ''} onChange={e => setForm({ ...form, yearsExperience: parseInt(e.target.value) || 0 })} placeholder="5" />
                </div>
                <div className="space-y-2">
                  <Label>Currency</Label>
                  <Input value={form.currency} onChange={e => setForm({ ...form, currency: e.target.value })} placeholder="USD" />
                </div>
              </div>
              <Button onClick={() => savePreferences.mutate({ desiredRole: form.desiredRole, desiredLocation: form.desiredLocation, salaryExpectationMin: form.salaryExpectationMin, salaryExpectationMax: form.salaryExpectationMax, currency: form.currency, employmentType: form.employmentType, workPreference: form.workPreference })} disabled={savePreferences.isPending} className="gap-2">
                {savePreferences.isPending && <Loader2 className="h-4 w-4 animate-spin" />} Save Preferences
              </Button>
            </CardContent>
          </Card>

        </div>
      </div>
    </div>
  );
}
