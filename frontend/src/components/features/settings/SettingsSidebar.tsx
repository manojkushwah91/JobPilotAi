'use client';

import { usePathname, useRouter } from 'next/navigation';
import { cn } from '@/lib/utils/cn';
import { Button } from '@/components/ui/button';
import { User, Briefcase } from 'lucide-react';

const settingsTabs = [
  { id: 'profile', label: 'Profile', icon: User, href: '/settings/profile' },
  { id: 'preferences', label: 'Job Preferences', icon: Briefcase, href: '/settings/preferences' },
];

export function SettingsSidebar() {
  const pathname = usePathname();
  const router = useRouter();

  return (
    <nav className="flex space-x-2 lg:flex-col lg:space-x-0 lg:space-y-1">
      {settingsTabs.map((tab) => (
        <Button
          key={tab.id}
          variant={pathname === tab.href ? 'secondary' : 'ghost'}
          className={cn(
            'w-full justify-start gap-2',
            pathname === tab.href && 'bg-secondary font-medium'
          )}
          onClick={() => router.push(tab.href)}
        >
          <tab.icon className="h-4 w-4" />
          {tab.label}
        </Button>
      ))}
    </nav>
  );
}
