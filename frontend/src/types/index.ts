// API Response
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  pagination?: Pagination;
  error?: ErrorDetail;
}

export interface Pagination {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ErrorDetail {
  code: string;
  message: string;
  details?: Record<string, string[]>;
}

// User
export interface User {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  tier: UserTier;
  avatarUrl?: string;
  createdAt: string;
}

export type UserRole = 'FREE' | 'PREMIUM' | 'PRO' | 'ADMIN';
export type UserTier = 'FREE' | 'PREMIUM' | 'PRO';

// Auth
export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  confirmPassword: string;
  name: string;
}

// Resume
export interface Resume {
  id: string;
  title: string;
  sections: ResumeSection[];
  atsScore?: AtsScore;
  createdAt: string;
  updatedAt: string;
}

export interface ResumeSection {
  id: string;
  type: SectionType;
  content: string;
  order: number;
}

export type SectionType = 'SUMMARY' | 'EXPERIENCE' | 'EDUCATION' | 'SKILLS' | 'CERTIFICATIONS' | 'PROJECTS';

export interface AtsScore {
  overallScore: number;
  keywordMatches: Record<string, number>;
  missingKeywords: string[];
  suggestions: AtsSuggestion[];
}

export interface AtsSuggestion {
  category: string;
  severity: 'CRITICAL' | 'MAJOR' | 'MINOR';
  message: string;
}

// Job
export interface JobListing {
  id: string;
  source?: string;
  sourceId?: string;
  title: string;
  companyName: string;
  companyLogoUrl?: string;
  companyId?: string;
  location?: Record<string, unknown>;
  salary?: Record<string, unknown>;
  description?: string;
  requirements?: string[];
  responsibilities?: string[];
  benefits?: string[];
  employmentType?: string;
  experienceLevel?: string;
  industry?: string;
  skills: string[];
  applicationUrl?: string;
  postedAt: string;
  matchScore?: number;
}

export interface SalaryRange {
  min: number;
  max: number;
  currency: string;
}

// Application
export interface Application {
  id: string;
  jobListingId: string;
  status: ApplicationStatus;
  statusHistory: StatusChange[];
  notes: ApplicationNote[];
  appliedAt: string;
  updatedAt: string;
}

export type ApplicationStatus =
  | 'SAVED'
  | 'APPLIED'
  | 'PHONE_SCREEN'
  | 'TECHNICAL_INTERVIEW'
  | 'ONSITE_INTERVIEW'
  | 'OFFER'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'WITHDRAWN';

export interface StatusChange {
  from: string;
  to: string;
  changedBy: string;
  timestamp: string;
}

export interface ApplicationNote {
  id: string;
  content: string;
  category: string;
  createdAt: string;
}

// Analytics
export interface ApplicationFunnel {
  byStatus: Record<string, number>;
  conversionRate: number;
  avgDaysPerStage: Record<string, number>;
}

export interface SkillGap {
  matchedSkills: string[];
  gapSkills: string[];
  recommendations: SkillRecommendation[];
}

export interface SkillRecommendation {
  skill: string;
  demandLevel: 'HIGH' | 'MEDIUM' | 'LOW';
  resources: string[];
}

// Interview
export type InterviewMode = 'TEXT' | 'AUDIO';
export type InterviewStatus = 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface InterviewSession {
  id: string;
  targetRole: string;
  targetCompany?: string;
  mode: InterviewMode;
  status: InterviewStatus;
  score?: number;
  totalQuestions: number;
  answeredQuestions: number;
  questions: InterviewQuestion[];
  feedback?: InterviewFeedback;
  createdAt: string;
  updatedAt: string;
}

export interface InterviewQuestion {
  id: string;
  question: string;
  category: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  answer?: string;
  feedback?: string;
  score?: number;
  order: number;
}

export interface InterviewFeedback {
  overallScore: number;
  strengths: string[];
  weaknesses: string[];
  summary: string;
  categoryScores: Record<string, number>;
}

export interface InterviewSessionCreate {
  targetRole: string;
  targetCompany?: string;
  mode: InterviewMode;
}

export interface QuestionBankItem {
  id: string;
  question: string;
  category: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  tags: string[];
}

// Company
export interface Company {
  id: string;
  name: string;
  description?: string;
  logoUrl?: string;
  industry?: string;
  location?: string;
  foundedYear?: number;
  companySize?: string;
  stockSymbol?: string;
  website?: string;
  techStack: string[];
  cultureKeywords: string[];
  rating?: number;
  salaryData: SalaryDataItem[];
  fundingRounds: FundingRound[];
}

export interface SalaryDataItem {
  role: string;
  min: number;
  max: number;
  median: number;
  currency: string;
}

export interface FundingRound {
  date: string;
  stage: string;
  amount: number;
  investors: string[];
}

export interface CompanySearchParams {
  query?: string;
  industry?: string;
  location?: string;
  page?: number;
  size?: number;
}

// Analytics
export interface AnalyticsOverview {
  totalApplications: number;
  totalInterviews: number;
  jobOffers: number;
  aiCalls: number;
  activeUsers: number;
  jobsTracked: number;
  applicationsByStatus: Record<string, number>;
  applicationsOverTime: TimeSeriesDataPoint[];
  jobsBySource: Record<string, number>;
  aiUsageByUseCase: Record<string, number>;
  monthlyTrends: MonthlyTrend[];
  skillGaps: SkillGapAnalysis[];
}

export interface TimeSeriesDataPoint {
  date: string;
  value: number;
}

export interface MonthlyTrend {
  month: string;
  applications: number;
  interviews: number;
  aiCalls: number;
}

export interface SkillGapAnalysis {
  skill: string;
  demandLevel: string;
  proficiency: number;
  relevanceScore: number;
}

// Settings
export interface UserProfile {
  name: string;
  email: string;
  avatarUrl?: string;
}

export interface JobPreferences {
  desiredRoles: string[];
  preferredLocations: string[];
  remotePreference: boolean;
  employmentTypes: string[];
}

export interface PrivacySettings {
  profileVisibility: 'PUBLIC' | 'PRIVATE';
}

export interface AiSettings {
  preferredProvider: 'OPENAI' | 'ANTHROPIC' | 'OLLAMA';
  model: string;
}

export interface BillingPlan {
  tier: 'FREE' | 'PREMIUM' | 'PRO';
  status: 'ACTIVE' | 'CANCELLED' | 'EXPIRED';
  currentPeriodStart: string;
  currentPeriodEnd: string;
}

export interface CoverLetter {
  id: string;
  title: string;
  content: string;
  tone?: string;
  createdAt: string;
}

export interface Notification {
  id: string;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
  link?: string;
}

export interface Invoice {
  id: string;
  amount: number;
  currency: string;
  status: string;
  date: string;
  pdfUrl?: string;
}

export interface PlanFeature {
  feature: string;
  free: boolean | string;
  premium: boolean | string;
  pro: boolean | string;
}

// Admin
export interface AdminUser {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  tier: UserTier;
  status: 'ACTIVE' | 'SUSPENDED';
  createdAt: string;
}

export interface FeatureFlag {
  key: string;
  description: string;
  enabled: boolean;
  updatedAt: string;
}

export interface AuditLog {
  id: string;
  timestamp: string;
  actor: string;
  action: string;
  resourceType: string;
  resourceId: string;
  details: string;
}

export interface AdminMetrics {
  totalUsers: number;
  activeSubscriptions: number;
  monthlyApiUsage: number;
  newUsersThisMonth: number;
}
