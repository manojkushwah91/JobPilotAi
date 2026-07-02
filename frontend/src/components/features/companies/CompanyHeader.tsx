'use client';

import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { Building2, Globe, MapPin, Calendar, Users, TrendingUp } from 'lucide-react';
import type { Company } from '@/types';

interface CompanyHeaderProps {
  company: Company;
}

export function CompanyHeader({ company }: CompanyHeaderProps) {
  return (
    <div className="space-y-6">
      <div className="flex items-start gap-6">
        <Avatar className="h-20 w-20 rounded-xl">
          <AvatarImage src={company.logoUrl} alt={company.name} />
          <AvatarFallback className="rounded-xl">
            <Building2 className="h-10 w-10 text-muted-foreground" />
          </AvatarFallback>
        </Avatar>
        <div className="flex-1 space-y-2">
          <div>
            <h1 className="text-3xl font-bold">{company.name}</h1>
            {company.industry && (
              <p className="text-muted-foreground">{company.industry}</p>
            )}
          </div>
          <div className="flex flex-wrap gap-4 text-sm text-muted-foreground">
            {company.location && (
              <span className="flex items-center gap-1">
                <MapPin className="h-4 w-4" />
                {company.location}
              </span>
            )}
            {company.website && (
              <a
                href={company.website}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-1 text-primary hover:underline"
              >
                <Globe className="h-4 w-4" />
                Website
              </a>
            )}
          </div>
          <div className="flex flex-wrap gap-2">
            {company.cultureKeywords.map((keyword) => (
              <Badge key={keyword} variant="secondary">{keyword}</Badge>
            ))}
          </div>
        </div>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {company.foundedYear && (
          <Card>
            <CardContent className="flex items-center gap-3 p-4">
              <Calendar className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">Founded</p>
                <p className="font-semibold">{company.foundedYear}</p>
              </div>
            </CardContent>
          </Card>
        )}
        {company.companySize && (
          <Card>
            <CardContent className="flex items-center gap-3 p-4">
              <Users className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">Company Size</p>
                <p className="font-semibold">{company.companySize}</p>
              </div>
            </CardContent>
          </Card>
        )}
        {company.stockSymbol && (
          <Card>
            <CardContent className="flex items-center gap-3 p-4">
              <TrendingUp className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">Stock Symbol</p>
                <p className="font-semibold">{company.stockSymbol}</p>
              </div>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}
