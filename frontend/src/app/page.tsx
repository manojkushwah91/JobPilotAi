import Link from 'next/link';
import { Bot, Search, FileText, BarChart3, Sparkles, ArrowRight, Github } from 'lucide-react';

const features = [
  { icon: FileText, title: 'AI Resume Studio', desc: 'Upload, analyze, and optimize your resume with AI. Get ATS scoring and smart suggestions.' },
  { icon: Search, title: 'Job Discovery', desc: 'Search millions of jobs with smart filters. Get AI-powered matching and recommendations.' },
  { icon: Sparkles, title: 'Cover Letters', desc: 'Generate tailored cover letters instantly with AI. Choose your tone and style.' },
  { icon: Bot, title: 'Browser Automation', desc: 'Automate job applications with Playwright. One-click Easy Apply on LinkedIn.' },
  { icon: BarChart3, title: 'Career Analytics', desc: 'Track applications, interviews, and salary trends. Identify skill gaps with AI.' },
  { icon: Sparkles, title: 'Interview Coach', desc: 'Practice with AI-powered mock interviews. Get real-time feedback and scoring.' },
];

export default function LandingPage() {
  return (
    <div className="flex min-h-screen flex-col">
      <header className="border-b">
        <div className="container mx-auto flex h-16 items-center justify-between px-4">
          <div className="flex items-center gap-2 font-bold text-xl">
            <Bot className="h-6 w-6 text-primary" />
            JobPilot AI
          </div>
          <nav className="flex items-center gap-4">
            <Link href="https://github.com/manojkushwah91/JobPilotAi" className="text-sm text-muted-foreground hover:text-foreground">
              <Github className="h-5 w-5" />
            </Link>
            <Link href="/login" className="text-sm font-medium hover:text-primary">Sign in</Link>
            <Link href="/register" className="inline-flex h-9 items-center justify-center rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground hover:bg-primary/90">
              Get Started
            </Link>
          </nav>
        </div>
      </header>

      <section className="container mx-auto px-4 py-20 text-center">
        <h1 className="text-5xl font-bold tracking-tight sm:text-6xl">
          Your AI-Powered<br />
          <span className="text-primary">Career Operating System</span>
        </h1>
        <p className="mt-6 text-lg text-muted-foreground max-w-2xl mx-auto">
          Search jobs, analyze resumes, optimize ATS scores, generate tailored applications,
          practice interviews, and automate your job search — all running 100% locally with Ollama.
        </p>
        <div className="mt-10 flex items-center justify-center gap-4">
          <Link href="/register" className="inline-flex h-12 items-center justify-center rounded-md bg-primary px-8 text-sm font-medium text-primary-foreground hover:bg-primary/90">
            Get Started Free <ArrowRight className="ml-2 h-4 w-4" />
          </Link>
          <Link href="/login" className="inline-flex h-12 items-center justify-center rounded-md border px-8 text-sm font-medium hover:bg-accent">
            Sign In
          </Link>
        </div>
      </section>

      <section className="border-t py-20">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">Everything you need to land your next role</h2>
          <div className="grid gap-8 md:grid-cols-2 lg:grid-cols-3">
            {features.map((f) => (
              <div key={f.title} className="rounded-lg border p-6 hover:border-primary/50 transition-colors">
                <f.icon className="h-8 w-8 text-primary mb-4" />
                <h3 className="font-semibold text-lg mb-2">{f.title}</h3>
                <p className="text-sm text-muted-foreground">{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="border-t py-20 bg-muted/50">
        <div className="container mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold mb-4">100% Offline AI</h2>
          <p className="text-lg text-muted-foreground max-w-xl mx-auto mb-8">
            No API keys. No subscriptions. Your data stays on your machine.
            Powered by Ollama and open-source LLMs.
          </p>
          <div className="flex justify-center gap-8 text-sm">
            <div className="rounded-lg border bg-background px-6 py-3"><code>git clone</code></div>
            <div className="rounded-lg border bg-background px-6 py-3"><code>docker compose up</code></div>
            <div className="rounded-lg border bg-background px-6 py-3"><code>make dev</code></div>
          </div>
        </div>
      </section>

      <footer className="border-t py-8">
        <div className="container mx-auto px-4 text-center text-sm text-muted-foreground">
          JobPilot AI &middot; Built with Java 21, Spring Boot, Next.js, and Ollama &middot; MIT License
        </div>
      </footer>
    </div>
  );
}
