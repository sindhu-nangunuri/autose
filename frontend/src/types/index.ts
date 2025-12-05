// API Response Types
export interface ApiResponse<T> {
  data: T;
  message: string;
  success: boolean;
}

// Data Quality Types
export interface Dataset {
  id: string;
  name: string;
  columns: string[];
  data: Record<string, any>[];
  metadata?: Record<string, any>;
  createdAt: string;
  rowCount: number;
  columnCount: number;
}

export interface DataQualityMetric {
  name: string;
  displayName: string;
}

export interface DataQualityResult {
  metric: DataQualityMetric;
  score: number;
  threshold: number;
  passed: boolean;
  issues: string[];
  recommendations: string[];
  details: Record<string, any>;
}

export interface DataQualityScore {
  overallScore: number;
  grade: string;
  metricScores: Record<string, number>;
  weights: Record<string, number>;
  calculatedAt: string;
}

export interface DataQualityReport {
  id: string;
  datasetName: string;
  preProcessingScore: DataQualityScore;
  postProcessingScore: DataQualityScore;
  results: DataQualityResult[];
  rectificationActions: string[];
  summary: string;
  timestamp: string;
  processingTimeMs: number;
  metadata?: Record<string, any>;
}

// SharePoint Types
export interface SharePointFile {
  name: string;
  size: number;
  lastModified: string;
  type: string;
}

export interface SharePointConfig {
  supportedFormats: string[];
  maxFileSizeMB: number;
  documentLibrary: string;
  status: string;
}

// Job Types
export interface Job {
  id: string;
  name: string;
  description: string;
  status: JobStatus;
  type: JobType;
  createdAt: string;
  updatedAt: string;
  startTime?: string;
  endTime?: string;
  duration?: number;
  progress: number;
  logs: JobLog[];
  configuration: JobConfiguration;
}

export type JobStatus = 'pending' | 'running' | 'completed' | 'failed' | 'cancelled';
export type JobType = 'data_quality' | 'orchestration' | 'monitoring' | 'validation';

export interface JobLog {
  id: string;
  timestamp: string;
  level: 'info' | 'warning' | 'error' | 'debug';
  message: string;
  source: string;
}

export interface JobConfiguration {
  [key: string]: any;
  dataSource?: string;
  rules?: ValidationRule[];
  schedule?: string;
  notifications?: NotificationConfig;
}

// Validation Rules
export interface ValidationRule {
  id: string;
  name: string;
  type: 'completeness' | 'accuracy' | 'consistency' | 'validity' | 'uniqueness';
  description: string;
  condition: string;
  severity: 'low' | 'medium' | 'high' | 'critical';
  enabled: boolean;
}

// Notifications
export interface NotificationConfig {
  email?: string[];
  slack?: string;
  webhook?: string;
  onSuccess: boolean;
  onFailure: boolean;
  onWarning: boolean;
}

// Dashboard Metrics
export interface DashboardMetrics {
  totalJobs: number;
  runningJobs: number;
  completedJobs: number;
  failedJobs: number;
  successRate: number;
  avgExecutionTime: number;
  dataQualityScore: number;
  recentActivity: ActivityItem[];
}

export interface ActivityItem {
  id: string;
  type: 'job_started' | 'job_completed' | 'job_failed' | 'alert_triggered';
  message: string;
  timestamp: string;
  severity: 'info' | 'warning' | 'error';
}

// Chart Data
export interface ChartDataPoint {
  name: string;
  value: number;
  timestamp?: string;
}

export interface TimeSeriesData {
  timestamp: string;
  value: number;
  label?: string;
}

// Form Types
export interface JobFormData {
  name: string;
  description: string;
  type: JobType;
  dataSource: string;
  schedule: string;
  validationRules: string[];
  notifications: NotificationConfig;
}

// User and Auth (for future implementation)
export interface User {
  id: string;
  username: string;
  email: string;
  role: 'admin' | 'operator' | 'viewer';
  permissions: string[];
}

// System Configuration
export interface SystemConfig {
  apiBaseUrl: string;
  refreshInterval: number;
  maxRetries: number;
  timeout: number;
  features: {
    realTimeUpdates: boolean;
    notifications: boolean;
    advancedCharts: boolean;
  };
}