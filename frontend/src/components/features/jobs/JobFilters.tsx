'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { X } from 'lucide-react';

const EMPLOYMENT_TYPES = ['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'REMOTE'] as const;
const EXPERIENCE_LEVELS = ['ENTRY', 'JUNIOR', 'MID', 'SENIOR', 'LEAD'] as const;

interface JobFiltersProps {
  filters: {
    keyword: string;
    location: string;
    employmentType: string[];
    experienceLevel: string[];
    salaryMin: string;
    salaryMax: string;
    postedWithin: string;
  };
  onChange: (filters: JobFiltersProps['filters']) => void;
  onReset: () => void;
}

export function JobFilters({ filters, onChange, onReset }: JobFiltersProps) {
  const toggleArray = (key: 'employmentType' | 'experienceLevel', value: string) => {
    const arr = filters[key];
    const next = arr.includes(value) ? arr.filter((v: string) => v !== value) : [...arr, value];
    onChange({ ...filters, [key]: next });
  };

  const hasFilters =
    filters.keyword ||
    filters.location ||
    filters.employmentType.length > 0 ||
    filters.experienceLevel.length > 0 ||
    filters.salaryMin ||
    filters.salaryMax ||
    filters.postedWithin;

  return (
    <Card>
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <CardTitle className="text-sm">Filters</CardTitle>
          {hasFilters && (
            <Button variant="ghost" size="sm" className="h-7 text-xs" onClick={onReset}>
              <X className="mr-1 h-3 w-3" />
              Reset
            </Button>
          )}
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        <div>
          <Label className="text-xs">Salary Range</Label>
          <div className="mt-1 flex items-center gap-2">
            <Input
              placeholder="Min"
              value={filters.salaryMin}
              onChange={(e) => onChange({ ...filters, salaryMin: e.target.value })}
              className="h-8 text-xs"
            />
            <span className="text-muted-foreground">-</span>
            <Input
              placeholder="Max"
              value={filters.salaryMax}
              onChange={(e) => onChange({ ...filters, salaryMax: e.target.value })}
              className="h-8 text-xs"
            />
          </div>
        </div>

        <div>
          <Label className="text-xs">Employment Type</Label>
          <div className="mt-1 space-y-1">
            {EMPLOYMENT_TYPES.map((t) => (
              <label key={t} className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={filters.employmentType.includes(t)}
                  onChange={() => toggleArray('employmentType', t)}
                  className="h-3.5 w-3.5 rounded border-gray-300"
                />
                {t.replace('_', ' ')}
              </label>
            ))}
          </div>
        </div>

        <Separator />

        <div>
          <Label className="text-xs">Experience Level</Label>
          <div className="mt-1 space-y-1">
            {EXPERIENCE_LEVELS.map((l) => (
              <label key={l} className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={filters.experienceLevel.includes(l)}
                  onChange={() => toggleArray('experienceLevel', l)}
                  className="h-3.5 w-3.5 rounded border-gray-300"
                />
                {l.charAt(0) + l.slice(1).toLowerCase()}
              </label>
            ))}
          </div>
        </div>

        <Separator />

        <div>
          <Label className="text-xs">Posted Within</Label>
          <select
            value={filters.postedWithin}
            onChange={(e) => onChange({ ...filters, postedWithin: e.target.value })}
            className="mt-1 h-8 w-full rounded-md border border-input bg-transparent px-2 text-xs"
          >
            <option value="">Any time</option>
            <option value="24h">24 hours</option>
            <option value="3d">3 days</option>
            <option value="7d">7 days</option>
            <option value="14d">14 days</option>
            <option value="30d">30 days</option>
          </select>
        </div>
      </CardContent>
    </Card>
  );
}
