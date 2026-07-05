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
  Mic,
  Building2,
  BarChart3,
  Settings,
  Shield,
  X,
  Rocket,
  MessageSquare,
  Bot,
  Mail,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

const navItems = [
  { href: '/mission-control', label: 'Mission Control', icon: Rocket },
  { href: '/automation', label: 'Automation', icon: Bot },
  { href: '/agent-chat', label: 'Agent Chat', icon: MessageSquare },
  { href: '/email-events', label: 'Email Events', icon: Mail },
  { href: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/resumes', label: 'Resumes', icon: FileText },
  { href: '/jobs', label: 'Jobs', icon: Briefcase },
  { href: '/applications', label: 'Applications', icon: ClipboardList },
  { href: '/interviews', label: 'Interviews', icon: Mic },
  { href: '/companies', label: 'Companies', icon: Building2 },
  { href: '/analytics', label: 'Analytics', icon: BarChart3 },
  { href: '/settings', label: 'Settings', icon: Settings },
];

const adminItem = { href: '/admin', label: 'Admin', icon: Shield };

export default function Sidebar({ open, onClose }: SidebarProps) {
  const pathname = usePathname();
  const { user } = useAuth();

  const items = user?.role === 'ADMIN' ? [...navItems, adminItem] : navItems;

  return (
    <>
      {open && (
        <div className="fixed inset-0 z-40 bg-black/50 lg:hidden" onClick={onClose} />
      )}
      <aside
        className={cn(
          'fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r bg-background transition-transform duration-300 lg:static lg:translate-x-0',
          open ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        <div className="flex h-14 items-center justify-between border-b px-4">
          <Link href="/dashboard" className="flex items-center gap-2 font-bold text-lg">
            <svg className="h-6 w-6 text-primary" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
            </svg>
            JobPilot AI
          </Link>
          <Button variant="ghost" size="icon" className="lg:hidden" onClick={onClose}>
            <X className="h-5 w-5" />
          </Button>
        </div>
        <ScrollArea className="flex-1 py-2">
          <nav className="space-y-1 px-2">
            {items.map((item) => {
              const Icon = item.icon;
              const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  onClick={onClose}
                  className={cn(
                    'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                    isActive
                      ? 'bg-primary/10 text-primary'
                      : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                  )}
                >
                  <Icon className="h-4 w-4" />
                  {item.label}
                </Link>
              );
            })}
          </nav>
        </ScrollArea>
        <div className="border-t p-4">
          <p className="text-xs text-muted-foreground">&copy; {new Date().getFullYear()} JobPilot AI</p>
        </div>
      </aside>
    </>
  );
}
