'use client';

import { useState, useEffect } from 'react';
import { useAuth } from '@/lib/auth/AuthProvider';
import { apiGet, apiPut } from '@/lib/api/client';
import { API } from '@/lib/api/endpoints';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Separator } from '@/components/ui/separator';
import { Skeleton } from '@/components/ui/skeleton';
import { toast } from 'sonner';
import { Check, Loader2, Plus, X, User, Wrench, Briefcase, GraduationCap, Settings, Sparkles } from 'lucide-react';

interface ExperienceItem {
  title: string;
  company: string;
  startDate: string;
  endDate: string;
  description: string;
}

interface EducationItem {
  degree: string;
  institution: string;
  field: string;
  graduationYear: string;
}

interface ProfileData {
  fullName: string;
  headline: string;
  summary: string;
  location: string;
  skills: string[];
  experience: string[];
  education: string[];
  desiredRole: string;
  desiredLocation: string;
  salaryExpectationMin: number;
  salaryExpectationMax: number;
  employmentType: string;
  workPreference: string;
  linkedinUrl: string;
  portfolioUrl: string;
  certifications: string[];
}

interface AgentSettingsData {
  preferredCompanies: string;
  avoidCompanies: string;
  careerGoal: string;
  certifications: string;
  projects: string;
  preferredLocation: string;
  salaryMin: number;
  salaryMax: number;
  employmentType: string;
  workAuthorization: string;
}

const defaultProfile: ProfileData = {
  fullName: '',
  headline: '',
  summary: '',
  location: '',
  skills: [],
  experience: [],
  education: [],
  desiredRole: '',
  desiredLocation: '',
  salaryExpectationMin: 0,
  salaryExpectationMax: 0,
  employmentType: '',
  workPreference: '',
  linkedinUrl: '',
  portfolioUrl: '',
  certifications: [],
};

const defaultAgent: AgentSettingsData = {
  preferredCompanies: '',
  avoidCompanies: '',
  careerGoal: '',
  certifications: '',
  projects: '',
  preferredLocation: '',
  salaryMin: 0,
  salaryMax: 0,
  employmentType: '',
  workAuthorization: '',
};

const EMPLOYMENT_TYPES = ['FULL_TIME', 'PART_TIME', 'CONTRACT'] as const;
const WORK_PREFERENCES = ['REMOTE', 'HYBRID', 'ONSITE'] as const;

function parseExperience(pipe: string): ExperienceItem {
  const parts = pipe.split('|');
  return {
    title: parts[0] || '',
    company: parts[1] || '',
    startDate: parts[2] || '',
    endDate: parts[3] || '',
    description: parts[4] || '',
  };
}

function serializeExperience(item: ExperienceItem): string {
  return [item.title, item.company, item.startDate, item.endDate, item.description].join('|');
}

function parseEducation(pipe: string): EducationItem {
  const parts = pipe.split('|');
  return {
    degree: parts[0] || '',
    institution: parts[1] || '',
    field: parts[2] || '',
    graduationYear: parts[3] || '',
  };
}

function serializeEducation(item: EducationItem): string {
  return [item.degree, item.institution, item.field, item.graduationYear].join('|');
}

export default function OnboardingPage() {
  const { user } = useAuth();

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState<string | null>(null);
  const [savedSections, setSavedSections] = useState<Set<string>>(new Set());

  const [profile, setProfile] = useState<ProfileData>(defaultProfile);
  const [agent, setAgent] = useState<AgentSettingsData>(defaultAgent);

  const [skillInput, setSkillInput] = useState('');

  const [experienceItems, setExperienceItems] = useState<ExperienceItem[]>([]);
  const [expTitle, setExpTitle] = useState('');
  const [expCompany, setExpCompany] = useState('');
  const [expStart, setExpStart] = useState('');
  const [expEnd, setExpEnd] = useState('');
  const [expDesc, setExpDesc] = useState('');

  const [educationItems, setEducationItems] = useState<EducationItem[]>([]);
  const [eduDegree, setEduDegree] = useState('');
  const [eduInstitution, setEduInstitution] = useState('');
  const [eduField, setEduField] = useState('');
  const [eduYear, setEduYear] = useState('');

  useEffect(() => {
    if (!user) return;
    let cancelled = false;

    (async () => {
      try {
        const { data } = await apiGet<ProfileData>(API.candidateProfile.get);
        if (cancelled || !data) return;

        const p = data as any;
        setProfile({
          fullName: p.fullName ?? '',
          headline: p.headline ?? '',
          summary: p.summary ?? '',
          location: p.location ?? '',
          skills: p.skills ?? [],
          experience: p.experience ?? [],
          education: p.education ?? [],
          desiredRole: p.desiredRole ?? '',
          desiredLocation: p.desiredLocation ?? '',
          salaryExpectationMin: p.salaryExpectationMin ?? 0,
          salaryExpectationMax: p.salaryExpectationMax ?? 0,
          employmentType: p.employmentType ?? '',
          workPreference: p.workPreference ?? '',
          linkedinUrl: p.linkedinUrl ?? '',
          portfolioUrl: p.portfolioUrl ?? '',
          certifications: p.certifications ?? [],
        });
        setExperienceItems((p.experience ?? []).map(parseExperience));
        setEducationItems((p.education ?? []).map(parseEducation));
      } catch {
        // profile may 404 — use defaults
      }
    })();

    return () => { cancelled = true; };
  }, [user]);

  useEffect(() => {
    if (!user?.id) return;
    let cancelled = false;

    (async () => {
      try {
        const { data } = await apiGet<AgentSettingsData>(API.agent.settings(user.id));
        if (cancelled || !data) return;
        setAgent({
          preferredCompanies: data.preferredCompanies ?? '',
          avoidCompanies: data.avoidCompanies ?? '',
          careerGoal: data.careerGoal ?? '',
          certifications: data.certifications ?? '',
          projects: data.projects ?? '',
          preferredLocation: data.preferredLocation ?? '',
          salaryMin: data.salaryMin ?? 0,
          salaryMax: data.salaryMax ?? 0,
          employmentType: data.employmentType ?? '',
          workAuthorization: data.workAuthorization ?? '',
        });
      } catch {
        // agent settings may 404 — use defaults
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => { cancelled = true; };
  }, [user?.id]);

  const showSaved = (section: string) => {
    setSavedSections((prev) => new Set(prev).add(section));
    setTimeout(() => {
      setSavedSections((prev) => {
        const next = new Set(prev);
        next.delete(section);
        return next;
      });
    }, 2000);
  };

  const handleSaveBasic = async () => {
    setSaving('basic');
    try {
      await apiPut(API.candidateProfile.update, {
        fullName: profile.fullName,
        phone: '',
        location: profile.location,
        headline: profile.headline,
        summary: profile.summary,
      });
      toast.success('Basic info saved');
      showSaved('basic');
    } catch {
      toast.error('Failed to save basic info');
    } finally {
      setSaving(null);
    }
  };

  const handleSaveSkills = async () => {
    setSaving('skills');
    try {
      await apiPut(API.candidateProfile.skills, { skills: profile.skills });
      toast.success('Skills saved');
      showSaved('skills');
    } catch {
      toast.error('Failed to save skills');
    } finally {
      setSaving(null);
    }
  };

  const handleSaveExperience = async () => {
    setSaving('experience');
    try {
      await apiPut(API.candidateProfile.experience, {
        experience: experienceItems.map(serializeExperience),
      });
      toast.success('Experience saved');
      showSaved('experience');
    } catch {
      toast.error('Failed to save experience');
    } finally {
      setSaving(null);
    }
  };

  const handleSaveEducation = async () => {
    setSaving('education');
    try {
      await apiPut(API.candidateProfile.education, {
        education: educationItems.map(serializeEducation),
      });
      toast.success('Education saved');
      showSaved('education');
    } catch {
      toast.error('Failed to save education');
    } finally {
      setSaving(null);
    }
  };

  const handleSavePreferences = async () => {
    setSaving('preferences');
    try {
      await apiPut(API.candidateProfile.preferences, {
        desiredRole: profile.desiredRole,
        desiredLocation: profile.desiredLocation,
        salaryExpectationMin: profile.salaryExpectationMin,
        salaryExpectationMax: profile.salaryExpectationMax,
        currency: 'USD',
        employmentType: profile.employmentType,
        workPreference: profile.workPreference,
      });
      toast.success('Preferences saved');
      showSaved('preferences');
    } catch {
      toast.error('Failed to save preferences');
    } finally {
      setSaving(null);
    }
  };

  const handleSaveAgent = async () => {
    if (!user?.id) return;
    setSaving('agent');
    try {
      await apiPut(API.agent.settings(user.id), {
        preferredCompanies: agent.preferredCompanies,
        avoidCompanies: agent.avoidCompanies,
        careerGoal: agent.careerGoal,
        certifications: agent.certifications,
        projects: agent.projects,
        preferredLocation: agent.preferredLocation,
        salaryMin: agent.salaryMin,
        salaryMax: agent.salaryMax,
        employmentType: agent.employmentType,
        workAuthorization: agent.workAuthorization,
      });
      toast.success('Agent settings saved');
      showSaved('agent');
    } catch {
      toast.error('Failed to save agent settings');
    } finally {
      setSaving(null);
    }
  };

  const addSkill = () => {
    const trimmed = skillInput.trim();
    if (trimmed && !profile.skills.includes(trimmed)) {
      setProfile({ ...profile, skills: [...profile.skills, trimmed] });
    }
    setSkillInput('');
  };

  const removeSkill = (index: number) => {
    setProfile({ ...profile, skills: profile.skills.filter((_, i) => i !== index) });
  };

  const addExperienceItem = () => {
    if (!expTitle.trim() || !expCompany.trim()) return;
    setExperienceItems([
      ...experienceItems,
      { title: expTitle.trim(), company: expCompany.trim(), startDate: expStart.trim(), endDate: expEnd.trim(), description: expDesc.trim() },
    ]);
    setExpTitle('');
    setExpCompany('');
    setExpStart('');
    setExpEnd('');
    setExpDesc('');
  };

  const removeExperienceItem = (index: number) => {
    setExperienceItems(experienceItems.filter((_, i) => i !== index));
  };

  const addEducationItem = () => {
    if (!eduDegree.trim() || !eduInstitution.trim()) return;
    setEducationItems([
      ...educationItems,
      { degree: eduDegree.trim(), institution: eduInstitution.trim(), field: eduField.trim(), graduationYear: eduYear.trim() },
    ]);
    setEduDegree('');
    setEduInstitution('');
    setEduField('');
    setEduYear('');
  };

  const removeEducationItem = (index: number) => {
    setEducationItems(educationItems.filter((_, i) => i !== index));
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="relative overflow-hidden rounded-2xl bg-gradient-mesh p-6">
          <div className="relative z-10">
            <h1 className="text-3xl font-bold tracking-tight mb-1">Set Up Your Profile</h1>
            <p className="text-muted-foreground">Help your AI employee understand your background</p>
          </div>
          <div className="absolute -right-16 -top-16 h-48 w-48 rounded-full bg-primary/5 blur-3xl" />
        </div>
        <div className="space-y-4">
          <Skeleton className="h-48 w-full rounded-xl" />
          <Skeleton className="h-32 w-full rounded-xl" />
          <Skeleton className="h-64 w-full rounded-xl" />
          <Skeleton className="h-48 w-full rounded-xl" />
          <Skeleton className="h-48 w-full rounded-xl" />
          <Skeleton className="h-64 w-full rounded-xl" />
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
            <span className="text-xs font-medium text-primary">Onboarding</span>
          </div>
          <h1 className="text-3xl font-bold tracking-tight mb-1">Set Up Your Profile</h1>
          <p className="text-muted-foreground">Help your AI employee understand your background</p>
        </div>
        <div className="absolute -right-16 -top-16 h-48 w-48 rounded-full bg-primary/5 blur-3xl" />
      </div>

      <div className="space-y-6">
        {/* Basic Info */}
        <Card>
          <CardContent className="p-6 space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <User className="h-5 w-5 text-primary" />
                <h2 className="text-lg font-semibold">Basic Info</h2>
              </div>
              {savedSections.has('basic') && (
                <span className="flex items-center gap-1 text-sm text-emerald-500">
                  <Check className="h-4 w-4" /> Saved
                </span>
              )}
            </div>
            <Separator />
            {!profile.fullName && !profile.headline && !profile.summary && !profile.location ? (
              <p className="text-sm text-muted-foreground italic">No data yet. Fill in the fields below.</p>
            ) : null}
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label>Full Name</Label>
                <Input value={profile.fullName} onChange={(e) => setProfile({ ...profile, fullName: e.target.value })} placeholder="John Doe" />
              </div>
              <div className="space-y-2">
                <Label>Location</Label>
                <Input value={profile.location} onChange={(e) => setProfile({ ...profile, location: e.target.value })} placeholder="San Francisco, CA" />
              </div>
            </div>
            <div className="space-y-2">
              <Label>Headline</Label>
              <Input value={profile.headline} onChange={(e) => setProfile({ ...profile, headline: e.target.value })} placeholder="Senior Software Engineer" />
            </div>
            <div className="space-y-2">
              <Label>Summary</Label>
              <Textarea value={profile.summary} onChange={(e) => setProfile({ ...profile, summary: e.target.value })} placeholder="Brief professional summary..." rows={3} />
            </div>
            <Button onClick={handleSaveBasic} disabled={saving === 'basic'} className="gap-2">
              {saving === 'basic' ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
              Save Basic Info
            </Button>
          </CardContent>
        </Card>

        {/* Skills */}
        <Card>
          <CardContent className="p-6 space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Wrench className="h-5 w-5 text-primary" />
                <h2 className="text-lg font-semibold">Skills</h2>
              </div>
              {savedSections.has('skills') && (
                <span className="flex items-center gap-1 text-sm text-emerald-500">
                  <Check className="h-4 w-4" /> Saved
                </span>
              )}
            </div>
            <Separator />
            {profile.skills.length === 0 ? (
              <p className="text-sm text-muted-foreground italic">No data yet. Fill in the fields below.</p>
            ) : null}
            <div className="flex flex-wrap gap-2">
              {profile.skills.map((skill, i) => (
                <span key={i} className="inline-flex items-center gap-1 rounded-full bg-primary/10 px-3 py-1 text-sm">
                  {skill}
                  <button onClick={() => removeSkill(i)} className="ml-1 hover:text-destructive"><X className="h-3 w-3" /></button>
                </span>
              ))}
            </div>
            <div className="flex gap-2">
              <Input value={skillInput} onChange={(e) => setSkillInput(e.target.value)} placeholder="Add a skill..." onKeyDown={(e) => { if (e.key === 'Enter') addSkill(); }} />
              <Button variant="outline" size="icon" onClick={addSkill}><Plus className="h-4 w-4" /></Button>
            </div>
            <Button onClick={handleSaveSkills} disabled={saving === 'skills'} className="gap-2">
              {saving === 'skills' ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
              Save Skills
            </Button>
          </CardContent>
        </Card>

        {/* Experience */}
        <Card>
          <CardContent className="p-6 space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Briefcase className="h-5 w-5 text-primary" />
                <h2 className="text-lg font-semibold">Experience</h2>
              </div>
              {savedSections.has('experience') && (
                <span className="flex items-center gap-1 text-sm text-emerald-500">
                  <Check className="h-4 w-4" /> Saved
                </span>
              )}
            </div>
            <Separator />
            {experienceItems.length === 0 ? (
              <p className="text-sm text-muted-foreground italic">No data yet. Fill in the fields below.</p>
            ) : null}
            <div className="space-y-3">
              {experienceItems.map((item, i) => (
                <div key={i} className="rounded-lg border p-3">
                  <div className="flex items-start justify-between">
                    <div>
                      <p className="font-medium">{item.title}</p>
                      <p className="text-sm text-muted-foreground">{item.company}</p>
                      <p className="text-xs text-muted-foreground">{item.startDate}{item.endDate ? ` - ${item.endDate}` : ''}</p>
                      {item.description && <p className="text-sm mt-1">{item.description}</p>}
                    </div>
                    <button onClick={() => removeExperienceItem(i)} className="shrink-0 hover:text-destructive"><X className="h-4 w-4" /></button>
                  </div>
                </div>
              ))}
            </div>
            <div className="grid gap-3 sm:grid-cols-2 rounded-lg border p-4">
              <div className="space-y-2">
                <Label>Title</Label>
                <Input value={expTitle} onChange={(e) => setExpTitle(e.target.value)} placeholder="Software Engineer" />
              </div>
              <div className="space-y-2">
                <Label>Company</Label>
                <Input value={expCompany} onChange={(e) => setExpCompany(e.target.value)} placeholder="Google" />
              </div>
              <div className="space-y-2">
                <Label>Start Date</Label>
                <Input value={expStart} onChange={(e) => setExpStart(e.target.value)} placeholder="2020-01" />
              </div>
              <div className="space-y-2">
                <Label>End Date</Label>
                <Input value={expEnd} onChange={(e) => setExpEnd(e.target.value)} placeholder="2023-12 (or Present)" />
              </div>
              <div className="space-y-2 sm:col-span-2">
                <Label>Description</Label>
                <Textarea value={expDesc} onChange={(e) => setExpDesc(e.target.value)} placeholder="Key responsibilities and achievements..." rows={2} />
              </div>
              <Button variant="outline" onClick={addExperienceItem} className="gap-2 sm:col-span-2">
                <Plus className="h-4 w-4" /> Add Experience
              </Button>
            </div>
            <Button onClick={handleSaveExperience} disabled={saving === 'experience'} className="gap-2">
              {saving === 'experience' ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
              Save Experience
            </Button>
          </CardContent>
        </Card>

        {/* Education */}
        <Card>
          <CardContent className="p-6 space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <GraduationCap className="h-5 w-5 text-primary" />
                <h2 className="text-lg font-semibold">Education</h2>
              </div>
              {savedSections.has('education') && (
                <span className="flex items-center gap-1 text-sm text-emerald-500">
                  <Check className="h-4 w-4" /> Saved
                </span>
              )}
            </div>
            <Separator />
            {educationItems.length === 0 ? (
              <p className="text-sm text-muted-foreground italic">No data yet. Fill in the fields below.</p>
            ) : null}
            <div className="space-y-3">
              {educationItems.map((item, i) => (
                <div key={i} className="rounded-lg border p-3">
                  <div className="flex items-start justify-between">
                    <div>
                      <p className="font-medium">{item.degree}</p>
                      <p className="text-sm text-muted-foreground">{item.institution}{item.field ? ` - ${item.field}` : ''}</p>
                      {item.graduationYear && <p className="text-xs text-muted-foreground">Class of {item.graduationYear}</p>}
                    </div>
                    <button onClick={() => removeEducationItem(i)} className="shrink-0 hover:text-destructive"><X className="h-4 w-4" /></button>
                  </div>
                </div>
              ))}
            </div>
            <div className="grid gap-3 sm:grid-cols-2 rounded-lg border p-4">
              <div className="space-y-2">
                <Label>Degree</Label>
                <Input value={eduDegree} onChange={(e) => setEduDegree(e.target.value)} placeholder="Bachelor of Science" />
              </div>
              <div className="space-y-2">
                <Label>Institution</Label>
                <Input value={eduInstitution} onChange={(e) => setEduInstitution(e.target.value)} placeholder="MIT" />
              </div>
              <div className="space-y-2">
                <Label>Field of Study</Label>
                <Input value={eduField} onChange={(e) => setEduField(e.target.value)} placeholder="Computer Science" />
              </div>
              <div className="space-y-2">
                <Label>Graduation Year</Label>
                <Input value={eduYear} onChange={(e) => setEduYear(e.target.value)} placeholder="2020" />
              </div>
              <Button variant="outline" onClick={addEducationItem} className="gap-2 sm:col-span-2">
                <Plus className="h-4 w-4" /> Add Education
              </Button>
            </div>
            <Button onClick={handleSaveEducation} disabled={saving === 'education'} className="gap-2">
              {saving === 'education' ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
              Save Education
            </Button>
          </CardContent>
        </Card>

        {/* Preferences */}
        <Card>
          <CardContent className="p-6 space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Settings className="h-5 w-5 text-primary" />
                <h2 className="text-lg font-semibold">Preferences</h2>
              </div>
              {savedSections.has('preferences') && (
                <span className="flex items-center gap-1 text-sm text-emerald-500">
                  <Check className="h-4 w-4" /> Saved
                </span>
              )}
            </div>
            <Separator />
            {!profile.desiredRole && !profile.desiredLocation && !profile.employmentType && !profile.workPreference ? (
              <p className="text-sm text-muted-foreground italic">No data yet. Fill in the fields below.</p>
            ) : null}
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label>Desired Role</Label>
                <Input value={profile.desiredRole} onChange={(e) => setProfile({ ...profile, desiredRole: e.target.value })} placeholder="Software Engineer" />
              </div>
              <div className="space-y-2">
                <Label>Desired Location</Label>
                <Input value={profile.desiredLocation} onChange={(e) => setProfile({ ...profile, desiredLocation: e.target.value })} placeholder="San Francisco, CA" />
              </div>
              <div className="space-y-2">
                <Label>Salary Min ($)</Label>
                <Input type="number" value={profile.salaryExpectationMin || ''} onChange={(e) => setProfile({ ...profile, salaryExpectationMin: parseInt(e.target.value) || 0 })} placeholder="80000" />
              </div>
              <div className="space-y-2">
                <Label>Salary Max ($)</Label>
                <Input type="number" value={profile.salaryExpectationMax || ''} onChange={(e) => setProfile({ ...profile, salaryExpectationMax: parseInt(e.target.value) || 0 })} placeholder="150000" />
              </div>
              <div className="space-y-2">
                <Label>Employment Type</Label>
                <Select value={profile.employmentType} onValueChange={(val) => setProfile({ ...profile, employmentType: val })}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select type" />
                  </SelectTrigger>
                  <SelectContent>
                    {EMPLOYMENT_TYPES.map((t) => (
                      <SelectItem key={t} value={t}>{t.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase())}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>Work Preference</Label>
                <Select value={profile.workPreference} onValueChange={(val) => setProfile({ ...profile, workPreference: val })}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select preference" />
                  </SelectTrigger>
                  <SelectContent>
                    {WORK_PREFERENCES.map((w) => (
                      <SelectItem key={w} value={w}>{w.replace(/\b\w/g, (c) => c.toUpperCase())}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <Button onClick={handleSavePreferences} disabled={saving === 'preferences'} className="gap-2">
              {saving === 'preferences' ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
              Save Preferences
            </Button>
          </CardContent>
        </Card>

        {/* Agent Settings */}
        <Card>
          <CardContent className="p-6 space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Sparkles className="h-5 w-5 text-primary" />
                <h2 className="text-lg font-semibold">Agent Settings</h2>
              </div>
              {savedSections.has('agent') && (
                <span className="flex items-center gap-1 text-sm text-emerald-500">
                  <Check className="h-4 w-4" /> Saved
                </span>
              )}
            </div>
            <Separator />
            {!agent.careerGoal && !agent.preferredCompanies && !agent.avoidCompanies && !agent.projects ? (
              <p className="text-sm text-muted-foreground italic">No data yet. Fill in the fields below.</p>
            ) : null}
            <div className="space-y-2">
              <Label>Career Goal</Label>
              <Textarea value={agent.careerGoal} onChange={(e) => setAgent({ ...agent, careerGoal: e.target.value })} placeholder="Become a Senior Staff Engineer at a top tech company..." rows={3} />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label>Preferred Companies</Label>
                <Input value={agent.preferredCompanies} onChange={(e) => setAgent({ ...agent, preferredCompanies: e.target.value })} placeholder="Google, Microsoft, Stripe" />
                <p className="text-xs text-muted-foreground">Comma-separated</p>
              </div>
              <div className="space-y-2">
                <Label>Avoid Companies</Label>
                <Input value={agent.avoidCompanies} onChange={(e) => setAgent({ ...agent, avoidCompanies: e.target.value })} placeholder="Amazon, Meta" />
                <p className="text-xs text-muted-foreground">Comma-separated</p>
              </div>
            </div>
            <div className="space-y-2">
              <Label>Projects</Label>
              <Input value={agent.projects} onChange={(e) => setAgent({ ...agent, projects: e.target.value })} placeholder="Project A, Project B, Project C" />
              <p className="text-xs text-muted-foreground">Comma-separated list of notable projects</p>
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label>Preferred Location</Label>
                <Input value={agent.preferredLocation} onChange={(e) => setAgent({ ...agent, preferredLocation: e.target.value })} placeholder="Remote, San Francisco" />
              </div>
              <div className="space-y-2">
                <Label>Work Authorization</Label>
                <Input value={agent.workAuthorization} onChange={(e) => setAgent({ ...agent, workAuthorization: e.target.value })} placeholder="US Citizen, H1-B, etc." />
              </div>
            </div>
            <Button onClick={handleSaveAgent} disabled={saving === 'agent'} className="gap-2">
              {saving === 'agent' ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
              Save Agent Settings
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
