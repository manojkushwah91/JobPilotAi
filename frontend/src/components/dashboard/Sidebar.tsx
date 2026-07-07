'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils/cn';
import { useAuth } from '@/lib/auth/AuthProvider';
import {
  LayoutDashboard,
  FileText,
  Briefcase,
  ClipboardList,
  Settings,
  Bot,
  MessageSquare,
  Bell,
  X,
  Sparkles,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

const navItems = [
  { href: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/automation', label: 'Automation', icon: Bot },
  { href: '/agent-chat', label: 'Agent Chat', icon: MessageSquare },
  { href: '/jobs', label: 'Jobs', icon: Briefcase },
  { href: '/applications', label: 'Applications', icon: ClipboardList },
  { href: '/notifications', label: 'Notifications', icon: Bell },
  { href: '/resumes', label: 'Resumes', icon: FileText },
  { href: '/settings', label: 'Settings', icon: Settings },
];

export default function Sidebar({ open, onClose }: SidebarProps) {
  const pathname = usePathname();

  return (
    <>
      {open && (
        <div
          className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm lg:hidden"
          onClick={onClose}
        />
      )}
      <aside
        className={cn(
          'fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r border-border/50 bg-background/80 backdrop-blur-xl transition-all duration-300 lg:static lg:translate-x-0',
          open ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        {/* Logo */}
        <div className="flex h-16 items-center justify-between border-b border-border/50 px-5">
          <Link href="/dashboard" className="flex items-center gap-2.5 group">
            <div className="relative flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-primary shadow-glow transition-shadow group-hover:shadow-glow-lg">
              <svg className="h-5 w-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
              </svg>
            </div>
            <div>
              <span className="text-lg font-bold tracking-tight">JobPilot</span>
              <span className="text-lg font-bold text-primary"> AI</span>
            </div>
          </Link>
          <Button
            variant="ghost"
            size="icon"
            className="lg:hidden h-8 w-8"
            onClick={onClose}
          >
            <X className="h-4 w-4" />
          </Button>
        </div>

        {/* Agent Status */}
        <div className="mx-3 mt-4 rounded-xl border border-border/50 bg-gradient-to-r from-primary/5 to-transparent p-3">
          <div className="flex items-center gap-2">
            <div className="relative">
              <Sparkles className="h-4 w-4 text-primary animate-pulse" />
            </div>
            <div>
              <p className="text-xs font-medium text-foreground">Agent Active</p>
              <p className="text-[10px] text-muted-foreground">3 jobs scoring</p>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <ScrollArea className="flex-1 py-4">
          <nav className="space-y-1 px-3">
            {navItems.map((item, i) => {
              const Icon = item.icon;
              const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  onClick={onClose}
                  className={cn(
                    'group flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-all duration-200',
                    isActive
                      ? 'bg-primary/10 text-primary shadow-sm'
                      : 'text-muted-foreground hover:bg-accent/50 hover:text-foreground'
                  )}
                  style={{ animationDelay: `${i * 30}ms` }}
                >
                  <div
                    className={cn(
                      'flex h-8 w-8 items-center justify-center rounded-lg transition-colors',
                      isActive
                        ? 'bg-primary/15 text-primary'
                        : 'bg-muted/50 text-muted-foreground group-hover:bg-accent group-hover:text-foreground'
                    )}
                  >
                    <Icon className="h-4 w-4" />
                  </div>
                  {item.label}
                  {item.label === 'Notifications' && (
                    <span className="ml-auto flex h-5 w-5 items-center justify-center rounded-full bg-destructive text-[10px] font-bold text-destructive-foreground">
                      3
                    </span>
                  )}
                </Link>
              );
            })}
          </nav>
        </ScrollArea>

        {/* Footer */}
        <div className="border-t border-border/50 p-4">
          <div className="flex items-center gap-3">
            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary/10 text-xs font-bold text-primary">
              MK
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-xs font-medium truncate">Manoj Kushwah</p>
              <p className="text-[10px] text-muted-foreground truncate">manoj@jobpilot.ai</p>
            </div>
          </div>
        </div>
      </aside>
    </>
  );
}
