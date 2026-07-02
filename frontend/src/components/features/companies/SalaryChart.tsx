'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import type { SalaryDataItem } from '@/types';

interface SalaryChartProps {
  salaryData: SalaryDataItem[];
}

export function SalaryChart({ salaryData }: SalaryChartProps) {
  if (salaryData.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Salary Data</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground">No salary data available for this company.</p>
        </CardContent>
      </Card>
    );
  }

  const chartData = salaryData.map((item) => ({
    role: item.role,
    Min: item.min,
    Median: item.median,
    Max: item.max,
  }));

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">Salary by Role</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="h-80">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 20 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
              <XAxis dataKey="role" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip
                contentStyle={{
                  background: 'hsl(var(--popover))',
                  border: '1px solid hsl(var(--border))',
                  borderRadius: 'var(--radius)',
                }}
                formatter={(value: number) => [`$${value.toLocaleString()}`, undefined]}
              />
              <Legend />
              <Bar dataKey="Min" fill="hsl(var(--muted-foreground))" radius={[4, 4, 0, 0]} />
              <Bar dataKey="Median" fill="hsl(var(--primary))" radius={[4, 4, 0, 0]} />
              <Bar dataKey="Max" fill="hsl(var(--success))" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
}
