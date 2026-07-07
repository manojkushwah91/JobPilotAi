'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { CommandDialog, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from '@/components/ui/command';
import {
  Search,
  LayoutDashboard,
  Bot,
  MessageSquare,
  Briefcase,
  ClipboardList,
  FileText,
  Bell,
  Settings,
  Sparkles,
  TrendingUp,
  Mic,
} from 'lucide-react';

const navigationItems = [
  { label: 'Dashboard', icon: LayoutDashboard, href: '/dashboard' },
  { label: 'Agent Chat', icon: MessageSquare, href: '/agent-chat' },
  { label: 'Automation', icon: Bot, href: '/automation' },
  { label: 'Jobs', icon: Briefcase, href: '/jobs' },
  { label: 'Applications', icon: ClipboardList, href: '/applications' },
  { label: 'Resumes', icon: FileText, href: '/resumes' },
  { label: 'Notifications', icon: Bell, href: '/notifications' },
  { label: 'Settings', icon: Settings, href: '/settings' },
];

const agentCommands = [
  { label: 'Find remote Java jobs', icon: Search, command: 'Find remote Java jobs' },
  { label: 'Score my resume', icon: TrendingUp, command: 'Score my resume against the job' },
  { label: 'Generate cover letter', icon: FileText, command: 'Generate a cover letter for the last job' },
  { label: 'Practice interview', icon: Mic, command: 'Help me practice interview questions' },
  { label: 'Pause agent', icon: Bot, command: 'Pause the agent until tomorrow' },
];

export function CommandPalette() {
  const [open, setOpen] = useState(false);
  const router = useRouter();

  useEffect(() => {
    const down = (e: KeyboardEvent) => {
      if (e.key === 'k' && (e.metaKey || e.ctrlKey)) {
        e.preventDefault();
        setOpen((open) => !open);
      }
    };
    document.addEventListener('keydown', down);
    return () => document.removeEventListener('keydown', down);
  }, []);

  const handleSelect = (value: string) => {
    setOpen(false);

    const navItem = navigationItems.find((item) => item.label.toLowerCase() === value);
    if (navItem) {
      router.push(navItem.href);
      return;
    }

    const agentCmd = agentCommands.find((cmd) => cmd.command === value);
    if (agentCmd) {
      router.push(`/agent-chat?q=${encodeURIComponent(agentCmd.command)}`);
      return;
    }
  };

  return (
    <CommandDialog open={open} onOpenChange={setOpen}>
      <CommandInput placeholder="Type a command or search..." />
      <CommandList>
        <CommandEmpty>No results found.</CommandEmpty>

        <CommandGroup heading="Navigation">
          {navigationItems.map((item) => {
            const Icon = item.icon;
            return (
              <CommandItem
                key={item.href}
                value={item.label.toLowerCase()}
                onSelect={handleSelect}
                className="gap-3"
              >
                <Icon className="h-4 w-4 text-muted-foreground" />
                <span>{item.label}</span>
              </CommandItem>
            );
          })}
        </CommandGroup>

        <CommandGroup heading="Agent Commands">
          {agentCommands.map((cmd) => {
            const Icon = cmd.icon;
            return (
              <CommandItem
                key={cmd.command}
                value={cmd.command}
                onSelect={handleSelect}
                className="gap-3"
              >
                <Icon className="h-4 w-4 text-primary" />
                <span>{cmd.label}</span>
                <span className="ml-auto text-xs text-muted-foreground">Agent</span>
              </CommandItem>
            );
          })}
        </CommandGroup>
      </CommandList>
    </CommandDialog>
  );
}
