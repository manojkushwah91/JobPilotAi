'use client';

import { useEffect, useRef, useState } from 'react';
import Link from 'next/link';
import {
  Bot,
  Search,
  FileText,
  BarChart3,
  Sparkles,
  ArrowRight,
  ChevronRight,
  Star,
  Shield,
  Zap,
  Globe,
  Cpu,
  Github,
  Check,
  Quote,
  Menu,
  X,
  Layers,
  Target,
  TrendingUp,
  Briefcase,
  Mic,
} from 'lucide-react';

const stats = [
  { value: '10K+', label: 'Jobs Analyzed' },
  { value: '5K+', label: 'Resumes Scored' },
  { value: '2K+', label: 'Active Users' },
  { value: '98%', label: 'ATS Pass Rate' },
];

const features = [
  {
    icon: FileText,
    title: 'AI Resume Studio',
    desc: 'Upload, analyze, and optimize your resume with AI. Get ATS scoring and smart suggestions in real-time.',
    gradient: 'from-violet-500/20 to-purple-500/10',
    border: 'border-violet-500/20',
    glow: 'group-hover:shadow-violet-500/10',
  },
  {
    icon: Search,
    title: 'Smart Job Discovery',
    desc: 'Search millions of jobs across multiple boards. AI-powered matching scores every opportunity.',
    gradient: 'from-blue-500/20 to-cyan-500/10',
    border: 'border-blue-500/20',
    glow: 'group-hover:shadow-blue-500/10',
  },
  {
    icon: Sparkles,
    title: 'Auto Cover Letters',
    desc: 'Generate tailored cover letters instantly. AI adapts tone and style to match each company.',
    gradient: 'from-amber-500/20 to-orange-500/10',
    border: 'border-amber-500/20',
    glow: 'group-hover:shadow-amber-500/10',
  },
  {
    icon: Bot,
    title: 'Browser Automation',
    desc: 'Automate job applications with Playwright. One-click Easy Apply on LinkedIn, Indeed, and more.',
    gradient: 'from-emerald-500/20 to-green-500/10',
    border: 'border-emerald-500/20',
    glow: 'group-hover:shadow-emerald-500/10',
  },
  {
    icon: BarChart3,
    title: 'Career Analytics',
    desc: 'Track applications, interviews, and salary trends. Identify skill gaps with AI-powered insights.',
    gradient: 'from-pink-500/20 to-rose-500/10',
    border: 'border-pink-500/20',
    glow: 'group-hover:shadow-pink-500/10',
  },
  {
    icon: Mic,
    title: 'Interview Coach',
    desc: 'Practice with AI-powered mock interviews. Get real-time feedback and scoring on your responses.',
    gradient: 'from-indigo-500/20 to-violet-500/10',
    border: 'border-indigo-500/20',
    glow: 'group-hover:shadow-indigo-500/10',
  },
];

const steps = [
  { icon: Upload, title: 'Upload Resume', desc: 'Drop your PDF and let AI analyze your experience, skills, and achievements.' },
  { icon: Target, title: 'Find Your Match', desc: 'AI scores jobs against your profile. Only see opportunities worth your time.' },
  { icon: Zap, title: 'Apply Smarter', desc: 'Generate tailored resumes and cover letters. Auto-apply or review each one.' },
  { icon: TrendingUp, title: 'Level Up', desc: 'Track your progress. Practice interviews. Close skill gaps. Land the offer.' },
];

const testimonials = [
  {
    quote: 'JobPilot AI transformed my job search. The ATS analysis alone helped me double my interview rate.',
    author: 'Sarah Chen',
    role: 'Senior Software Engineer',
    rating: 5,
  },
  {
    quote: 'The AI interview coach is incredible. I practiced for two weeks and nailed my Google interview.',
    author: 'Marcus Williams',
    role: 'Product Manager at Google',
    rating: 5,
  },
  {
    quote: 'Finally, a tool that respects my privacy. Everything runs locally. No data leaves my machine.',
    author: 'Priya Patel',
    role: 'DevOps Engineer',
    rating: 5,
  },
];

const pricing = [
  {
    name: 'Free',
    price: '$0',
    desc: 'For individual job seekers getting started',
    features: [
      'Resume ATS analysis',
      '5 job matches per day',
      '10 interview questions',
      'Basic career insights',
      'Local AI (Ollama)',
    ],
    cta: 'Get Started Free',
    href: '/register',
    featured: false,
  },
  {
    name: 'Pro',
    price: '$19',
    period: '/month',
    desc: 'For serious candidates who want every advantage',
    features: [
      'Unlimited everything',
      'Auto-apply engine',
      'AI interview simulator',
      'Career analytics dashboard',
      'AI memory & coaching',
      'Priority support',
    ],
    cta: 'Start Free Trial',
    href: '/register?plan=pro',
    featured: true,
  },
];

function AnimatedCounter({ value, suffix = '' }: { value: string; suffix?: string }) {
  const [display, setDisplay] = useState('0');
  const ref = useRef<HTMLSpanElement>(null);
  const [hasAnimated, setHasAnimated] = useState(false);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting && !hasAnimated) {
          setHasAnimated(true);
          const num = parseInt(value.replace(/\D/g, ''));
          if (isNaN(num)) { setDisplay(value); return; }
          let start = 0;
          const duration = 1500;
          const step = Math.ceil(num / 60);
          const timer = setInterval(() => {
            start += step;
            if (start >= num) {
              setDisplay(value);
              clearInterval(timer);
            } else {
              setDisplay(start + suffix);
            }
          }, 25);
          return () => clearInterval(timer);
        }
      },
      { threshold: 0.3 }
    );
    if (ref.current) observer.observe(ref.current);
    return () => observer.disconnect();
  }, [value, suffix, hasAnimated]);

  return <span ref={ref}>{display}</span>;
}

function StarRating({ rating }: { rating: number }) {
  return (
    <div className="flex gap-0.5">
      {Array.from({ length: 5 }).map((_, i) => (
        <Star key={i} className={`h-3.5 w-3.5 ${i < rating ? 'fill-amber-400 text-amber-400' : 'text-muted-foreground/20'}`} />
      ))}
    </div>
  );
}

function Upload({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
      <polyline points="17 8 12 3 7 8" />
      <line x1="12" y1="3" x2="12" y2="15" />
    </svg>
  );
}

export default function LandingPage() {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <div className="flex min-h-screen flex-col bg-background">
      {/* ═══════════════════════════════════════════
           NAVBAR
           ═══════════════════════════════════════════ */}
      <header className="fixed top-0 z-50 w-full border-b border-border/50 bg-background/80 backdrop-blur-xl">
        <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
          <Link href="/" className="flex items-center gap-2.5 group">
            <div className="relative flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-primary shadow-glow transition-shadow group-hover:shadow-glow-lg">
              <svg className="h-5 w-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
              </svg>
            </div>
            <span className="text-lg font-bold tracking-tight">JobPilot <span className="text-primary">AI</span></span>
          </Link>
          <nav className="hidden items-center gap-8 md:flex">
            <Link href="#features" className="text-sm text-muted-foreground transition-colors hover:text-foreground">Features</Link>
            <Link href="#how-it-works" className="text-sm text-muted-foreground transition-colors hover:text-foreground">How It Works</Link>
            <Link href="#pricing" className="text-sm text-muted-foreground transition-colors hover:text-foreground">Pricing</Link>
            <Link href="https://github.com/manojkushwah91/JobPilotAi" className="text-muted-foreground transition-colors hover:text-foreground">
              <Github className="h-5 w-5" />
            </Link>
          </nav>
          <div className="hidden items-center gap-3 md:flex">
            <Link href="/login" className="text-sm font-medium text-muted-foreground transition-colors hover:text-foreground">
              Sign in
            </Link>
            <Link
              href="/register"
              className="inline-flex h-9 items-center gap-2 rounded-lg bg-gradient-primary px-4 text-sm font-medium text-white shadow-glow transition-all hover:shadow-glow-lg hover:scale-105"
            >
              Get Started
              <ArrowRight className="h-3.5 w-3.5" />
            </Link>
          </div>
          <button className="md:hidden" onClick={() => setMenuOpen(!menuOpen)}>
            {menuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </button>
        </div>
        {menuOpen && (
          <div className="border-t border-border/50 bg-background px-4 py-4 md:hidden">
            <nav className="flex flex-col gap-3">
              <Link href="#features" className="text-sm text-muted-foreground" onClick={() => setMenuOpen(false)}>Features</Link>
              <Link href="#how-it-works" className="text-sm text-muted-foreground" onClick={() => setMenuOpen(false)}>How It Works</Link>
              <Link href="#pricing" className="text-sm text-muted-foreground" onClick={() => setMenuOpen(false)}>Pricing</Link>
              <div className="flex gap-3 pt-2">
                <Link href="/login" className="flex-1 rounded-lg border px-4 py-2 text-center text-sm font-medium">Sign in</Link>
                <Link href="/register" className="flex-1 rounded-lg bg-gradient-primary px-4 py-2 text-center text-sm font-medium text-white">Get Started</Link>
              </div>
            </nav>
          </div>
        )}
      </header>

      {/* ═══════════════════════════════════════════
           HERO
           ═══════════════════════════════════════════ */}
      <section className="relative min-h-[90vh] overflow-hidden pt-16">
        <div className="absolute inset-0 bg-gradient-hero" />
        <div className="absolute inset-0 bg-gradient-mesh opacity-60" />
        <div className="absolute -left-40 -top-40 h-80 w-80 rounded-full bg-primary/10 blur-3xl" />
        <div className="absolute -right-40 top-1/3 h-96 w-96 rounded-full bg-violet-500/10 blur-3xl" />
        <div className="absolute -bottom-40 left-1/3 h-80 w-80 rounded-full bg-amber-500/5 blur-3xl" />

        <div className="relative z-10 mx-auto flex min-h-[calc(90vh-4rem)] max-w-7xl flex-col items-center justify-center px-4 text-center sm:px-6 lg:px-8">
          <div className="animate-fade-in mb-6 inline-flex items-center gap-2 rounded-full border border-primary/20 bg-primary/5 px-4 py-1.5 text-xs font-medium text-primary">
            <Sparkles className="h-3.5 w-3.5" />
            AI-Powered Career Operating System
          </div>
          <h1 className="animate-fade-in max-w-4xl text-4xl font-bold tracking-tight sm:text-5xl md:text-6xl lg:text-7xl">
            Your{' '}
            <span className="text-gradient">AI Career</span>
            <br />
            <span className="text-gradient">Command Center</span>
          </h1>
          <p className="animate-fade-in mt-6 max-w-2xl text-base text-muted-foreground sm:text-lg stagger-1">
            Search jobs, analyze resumes, optimize ATS scores, generate tailored applications,
            practice interviews, and automate your job search — all running 100% locally with Ollama.
          </p>
          <div className="animate-fade-in mt-10 flex flex-col items-center gap-4 sm:flex-row stagger-2">
            <Link
              href="/register"
              className="group inline-flex h-12 items-center gap-2 rounded-xl bg-gradient-primary px-8 text-sm font-semibold text-white shadow-glow transition-all duration-300 hover:shadow-glow-lg hover:scale-105"
            >
              Get Started Free
              <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />
            </Link>
            <Link
              href="#features"
              className="glass inline-flex h-12 items-center gap-2 rounded-xl border-border/50 px-8 text-sm font-medium text-foreground transition-all duration-300 hover:border-primary/30 hover:bg-primary/5"
            >
              See Features
              <ChevronRight className="h-4 w-4" />
            </Link>
          </div>
          <div className="animate-fade-in mt-12 flex items-center gap-2 text-xs text-muted-foreground stagger-3">
            <div className="flex -space-x-2">
              {['bg-violet-500', 'bg-blue-500', 'bg-emerald-500', 'bg-amber-500'].map((c, i) => (
                <div key={i} className={`h-7 w-7 rounded-full ${c} ring-2 ring-background flex items-center justify-center text-[10px] font-bold text-white`}>
                  {['SC', 'MW', 'PP', 'AK'][i]}
                </div>
              ))}
            </div>
            <span className="ml-2">Trusted by 2,000+ job seekers</span>
          </div>
        </div>
      </section>

      {/* ═══════════════════════════════════════════
           STATS BAR
           ═══════════════════════════════════════════ */}
      <section className="relative border-y border-border/50 bg-muted/30">
        <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
          <div className="grid grid-cols-2 gap-8 md:grid-cols-4">
            {stats.map((stat, i) => (
              <div key={stat.label} className={`animate-fade-in text-center stagger-${i + 1}`}>
                <p className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
                  <AnimatedCounter value={stat.value} />
                </p>
                <p className="mt-1 text-sm text-muted-foreground">{stat.label}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ═══════════════════════════════════════════
           FEATURES
           ═══════════════════════════════════════════ */}
      <section id="features" className="relative py-24">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="mx-auto max-w-2xl text-center">
            <div className="mb-4 inline-flex items-center gap-2 rounded-full border border-primary/20 bg-primary/5 px-4 py-1.5 text-xs font-medium text-primary">
              <Layers className="h-3.5 w-3.5" />
              Everything You Need
            </div>
            <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">
              Your complete{' '}
              <span className="text-gradient">career toolkit</span>
            </h2>
            <p className="mt-4 text-muted-foreground">
              Seven interconnected modules work together to maximize your probability of getting hired.
            </p>
          </div>
          <div className="mt-16 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {features.map((f, i) => {
              const Icon = f.icon;
              return (
                <div
                  key={f.title}
                  className={`group relative overflow-hidden rounded-2xl border ${f.border} bg-gradient-card p-6 transition-all duration-500 hover-lift animate-fade-in stagger-${i + 1}`}
                >
                  <div className={`absolute inset-0 bg-gradient-to-br ${f.gradient} opacity-0 transition-opacity duration-500 group-hover:opacity-100`} />
                  <div className="relative z-10">
                    <div className={`mb-4 flex h-12 w-12 items-center justify-center rounded-xl border ${f.border} bg-background/50`}>
                      <Icon className="h-6 w-6 text-primary" />
                    </div>
                    <h3 className="text-lg font-semibold">{f.title}</h3>
                    <p className="mt-2 text-sm leading-relaxed text-muted-foreground">{f.desc}</p>
                  </div>
                  <div className={`absolute -inset-px rounded-2xl transition-shadow duration-500 ${f.glow}`} />
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* ═══════════════════════════════════════════
           HOW IT WORKS
           ═══════════════════════════════════════════ */}
      <section id="how-it-works" className="relative border-y border-border/50 bg-muted/30 py-24">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="mx-auto max-w-2xl text-center">
            <div className="mb-4 inline-flex items-center gap-2 rounded-full border border-primary/20 bg-primary/5 px-4 py-1.5 text-xs font-medium text-primary">
              <Zap className="h-3.5 w-3.5" />
              From Zero to Offer
            </div>
            <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">
              How it <span className="text-gradient">works</span>
            </h2>
            <p className="mt-4 text-muted-foreground">
              Four simple steps to transform your job search from chaotic to strategic.
            </p>
          </div>
          <div className="mt-16 grid gap-8 md:grid-cols-4">
            {steps.map((step, i) => {
              const Icon = step.icon;
              return (
                <div key={step.title} className={`relative animate-fade-in text-center stagger-${i + 1}`}>
                  {i < steps.length - 1 && (
                    <div className="absolute left-[60%] top-12 hidden h-px w-[80%] bg-gradient-to-r from-primary/50 to-transparent md:block" />
                  )}
                  <div className="mx-auto mb-6 flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-primary shadow-glow">
                    <span className="absolute -top-2 -right-2 flex h-7 w-7 items-center justify-center rounded-full bg-background text-xs font-bold text-primary ring-2 ring-primary/20">
                      {i + 1}
                    </span>
                    <Icon className="h-7 w-7 text-white" />
                  </div>
                  <h3 className="text-lg font-semibold">{step.title}</h3>
                  <p className="mt-2 text-sm text-muted-foreground">{step.desc}</p>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* ═══════════════════════════════════════════
           TESTIMONIALS
           ═══════════════════════════════════════════ */}
      <section className="relative py-24">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="mx-auto max-w-2xl text-center">
            <div className="mb-4 inline-flex items-center gap-2 rounded-full border border-primary/20 bg-primary/5 px-4 py-1.5 text-xs font-medium text-primary">
              <Quote className="h-3.5 w-3.5" />
              Trusted by professionals
            </div>
            <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">
              What users <span className="text-gradient">say</span>
            </h2>
          </div>
          <div className="mt-16 grid gap-6 md:grid-cols-3">
            {testimonials.map((t, i) => (
              <div
                key={t.author}
                className={`glass-strong rounded-2xl p-6 transition-all duration-500 hover-lift animate-fade-in stagger-${i + 1}`}
              >
                <StarRating rating={t.rating} />
                <p className="mt-4 text-sm leading-relaxed text-muted-foreground">&ldquo;{t.quote}&rdquo;</p>
                <div className="mt-6 flex items-center gap-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gradient-primary text-xs font-bold text-white">
                    {t.author.split(' ').map(n => n[0]).join('')}
                  </div>
                  <div>
                    <p className="text-sm font-medium">{t.author}</p>
                    <p className="text-xs text-muted-foreground">{t.role}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ═══════════════════════════════════════════
           PRICING
           ═══════════════════════════════════════════ */}
      <section id="pricing" className="relative border-y border-border/50 bg-muted/30 py-24">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="mx-auto max-w-2xl text-center">
            <div className="mb-4 inline-flex items-center gap-2 rounded-full border border-primary/20 bg-primary/5 px-4 py-1.5 text-xs font-medium text-primary">
              <Shield className="h-3.5 w-3.5" />
              Simple, Transparent Pricing
            </div>
            <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">
              Start free, <span className="text-gradient">upgrade</span> when ready
            </h2>
            <p className="mt-4 text-muted-foreground">
              No hidden fees. No API key required. Your data stays on your machine.
            </p>
          </div>
          <div className="mt-16 grid gap-8 md:grid-cols-2 md:mx-auto md:max-w-3xl">
            {pricing.map((plan) => (
              <div
                key={plan.name}
                className={`relative rounded-2xl p-8 transition-all duration-500 animate-fade-in ${
                  plan.featured
                    ? 'border-2 border-primary/30 bg-gradient-card shadow-glow'
                    : 'glass-strong border border-border/50'
                }`}
              >
                {plan.featured && (
                  <div className="absolute -top-3 left-1/2 -translate-x-1/2 rounded-full bg-gradient-primary px-4 py-1 text-xs font-semibold text-white">
                    Most Popular
                  </div>
                )}
                <h3 className="text-lg font-semibold">{plan.name}</h3>
                <div className="mt-4 flex items-baseline gap-1">
                  <span className="text-4xl font-bold">{plan.price}</span>
                  {plan.period && <span className="text-sm text-muted-foreground">{plan.period}</span>}
                </div>
                <p className="mt-2 text-sm text-muted-foreground">{plan.desc}</p>
                <ul className="mt-6 space-y-3">
                  {plan.features.map((f) => (
                    <li key={f} className="flex items-start gap-3 text-sm">
                      <Check className="mt-0.5 h-4 w-4 shrink-0 text-success" />
                      <span>{f}</span>
                    </li>
                  ))}
                </ul>
                <Link
                  href={plan.href}
                  className={`mt-8 flex h-11 w-full items-center justify-center rounded-xl text-sm font-semibold transition-all ${
                    plan.featured
                      ? 'bg-gradient-primary text-white shadow-glow hover:shadow-glow-lg'
                      : 'border border-border/50 text-foreground hover:bg-accent'
                  }`}
                >
                  {plan.cta}
                </Link>
              </div>
            ))}
          </div>
          <div className="mt-12 text-center">
            <div className="inline-flex items-center gap-4 rounded-xl border border-border/30 bg-background/50 px-6 py-3 text-sm">
              <Cpu className="h-5 w-5 text-primary" />
              <span className="text-muted-foreground">All AI runs locally with Ollama —</span>
              <span className="font-medium text-foreground">no cloud dependency</span>
            </div>
          </div>
        </div>
      </section>

      {/* ═══════════════════════════════════════════
           FINAL CTA
           ═══════════════════════════════════════════ */}
      <section className="relative py-24">
        <div className="absolute inset-0 bg-gradient-hero" />
        <div className="absolute inset-0 bg-gradient-mesh opacity-40" />
        <div className="relative z-10 mx-auto max-w-3xl px-4 text-center sm:px-6 lg:px-8">
          <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">
            Ready to take control of{' '}
            <span className="text-gradient">your career?</span>
          </h2>
          <p className="mt-6 text-lg text-muted-foreground">
            Join thousands of professionals who use JobPilot AI to land better jobs faster.
            Start free — no credit card required.
          </p>
          <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
            <Link
              href="/register"
              className="group inline-flex h-12 items-center gap-2 rounded-xl bg-gradient-primary px-8 text-sm font-semibold text-white shadow-glow transition-all duration-300 hover:shadow-glow-lg hover:scale-105"
            >
              Start Your Free Account
              <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />
            </Link>
            <Link
              href="https://github.com/manojkushwah91/JobPilotAi"
              className="glass inline-flex h-12 items-center gap-2 rounded-xl border-border/50 px-8 text-sm font-medium transition-all duration-300 hover:border-primary/30 hover:bg-primary/5"
            >
              <Github className="h-4 w-4" />
              View on GitHub
            </Link>
          </div>
          <div className="mt-8 flex items-center justify-center gap-4 text-xs text-muted-foreground">
            <span className="flex items-center gap-1"><Globe className="h-3.5 w-3.5" /> 100% offline</span>
            <span className="flex items-center gap-1"><Shield className="h-3.5 w-3.5" /> No data collection</span>
            <span className="flex items-center gap-1"><Zap className="h-3.5 w-3.5" /> Open source</span>
          </div>
        </div>
      </section>

      {/* ═══════════════════════════════════════════
           FOOTER
           ═══════════════════════════════════════════ */}
      <footer className="border-t border-border/50 py-12">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col items-center justify-between gap-6 md:flex-row">
            <div className="flex items-center gap-2.5">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-primary">
                <svg className="h-4 w-4 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
                </svg>
              </div>
              <span className="text-sm font-bold">JobPilot <span className="text-primary">AI</span></span>
            </div>
            <div className="flex items-center gap-6 text-xs text-muted-foreground">
              <span>Built with Java 21, Spring Boot, Next.js, and Ollama</span>
              <span>&middot;</span>
              <span>MIT License</span>
              <span>&middot;</span>
              <Link href="https://github.com/manojkushwah91/JobPilotAi" className="hover:text-foreground transition-colors">
                <Github className="h-4 w-4" />
              </Link>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}
