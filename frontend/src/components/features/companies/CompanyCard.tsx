'use client';

import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Building2, MapPin, Star } from 'lucide-react';
import type { Company } from '@/types';

interface CompanyCardProps {
  company: Company;
  onSelect: (id: string) => void;
}

export function CompanyCard({ company, onSelect }: CompanyCardProps) {
  return (
    <Card className="cursor-pointer transition-all hover:shadow-md" onClick={() => onSelect(company.id)}>
      <CardHeader className="flex flex-row items-start gap-4 space-y-0 pb-2">
        <Avatar className="h-12 w-12 rounded-lg">
          <AvatarImage src={company.logoUrl} alt={company.name} />
          <AvatarFallback className="rounded-lg">
            <Building2 className="h-6 w-6 text-muted-foreground" />
          </AvatarFallback>
        </Avatar>
        <div className="flex-1 space-y-1">
          <CardTitle className="text-lg">{company.name}</CardTitle>
          <CardDescription className="flex items-center gap-1">
            <MapPin className="h-3 w-3" />
            {company.location ?? 'Unknown location'}
          </CardDescription>
        </div>
        {company.rating && (
          <div className="flex items-center gap-1 text-sm">
            <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
            <span className="font-medium">{company.rating.toFixed(1)}</span>
          </div>
        )}
      </CardHeader>
      <CardContent>
        {company.industry && (
          <p className="mb-2 text-sm text-muted-foreground">{company.industry}</p>
        )}
        <div className="flex flex-wrap gap-1">
          {company.techStack.slice(0, 5).map((tech) => (
            <Badge key={tech} variant="secondary" className="text-xs">
              {tech}
            </Badge>
          ))}
          {company.techStack.length > 5 && (
            <Badge variant="outline" className="text-xs">
              +{company.techStack.length - 5}
            </Badge>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
