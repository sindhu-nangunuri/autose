import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../services/api';
import { Job, ValidationRule } from '../types';

// Query Keys
export const queryKeys = {
  dashboard: ['dashboard'] as const,
  jobs: ['jobs'] as const,
  job: (id: string) => ['jobs', id] as const,
  jobLogs: (id: string) => ['jobs', id, 'logs'] as const,
  validationRules: ['validation-rules'] as const,
  systemHealth: ['system', 'health'] as const,
  systemMetrics: ['system', 'metrics'] as const,
};

// Dashboard Hooks
export const useDashboardMetrics = () => {
  return useQuery({
    queryKey: queryKeys.dashboard,
    queryFn: () => apiClient.getDashboardMetrics(),
    refetchInterval: 30000, // Refetch every 30 seconds
    select: (data) => data.data,
  });
};

// Job Hooks
export const useJobs = (params?: {
  status?: string;
  type?: string;
  limit?: number;
  offset?: number;
}) => {
  return useQuery({
    queryKey: [...queryKeys.jobs, params],
    queryFn: () => apiClient.getJobs(params),
    refetchInterval: 10000, // Refetch every 10 seconds
    select: (data) => data.data,
  });
};

export const useJob = (id: string) => {
  return useQuery({
    queryKey: queryKeys.job(id),
    queryFn: () => apiClient.getJob(id),
    enabled: !!id,
    refetchInterval: 5000, // Refetch every 5 seconds for real-time updates
    select: (data) => data.data,
  });
};

export const useJobLogs = (id: string, params?: {
  level?: string;
  limit?: number;
  offset?: number;
}) => {
  return useQuery({
    queryKey: [...queryKeys.jobLogs(id), params],
    queryFn: () => apiClient.getJobLogs(id, params),
    enabled: !!id,
    refetchInterval: 2000, // Refetch every 2 seconds for logs
    select: (data) => data.data,
  });
};

// Job Mutations
export const useCreateJob = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (jobData: Partial<Job>) => apiClient.createJob(jobData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.jobs });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
    },
  });
};

export const useUpdateJob = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<Job> }) => 
      apiClient.updateJob(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.job(id) });
      queryClient.invalidateQueries({ queryKey: queryKeys.jobs });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
    },
  });
};

export const useDeleteJob = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (id: string) => apiClient.deleteJob(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.jobs });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
    },
  });
};

export const useStartJob = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (id: string) => apiClient.startJob(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.job(id) });
      queryClient.invalidateQueries({ queryKey: queryKeys.jobs });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
    },
  });
};

export const useStopJob = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (id: string) => apiClient.stopJob(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.job(id) });
      queryClient.invalidateQueries({ queryKey: queryKeys.jobs });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
    },
  });
};

// Validation Rules Hooks
export const useValidationRules = () => {
  return useQuery({
    queryKey: queryKeys.validationRules,
    queryFn: () => apiClient.getValidationRules(),
    select: (data) => data.data,
  });
};

export const useCreateValidationRule = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (rule: Partial<ValidationRule>) => apiClient.createValidationRule(rule),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.validationRules });
    },
  });
};

export const useUpdateValidationRule = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<ValidationRule> }) => 
      apiClient.updateValidationRule(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.validationRules });
    },
  });
};

export const useDeleteValidationRule = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (id: string) => apiClient.deleteValidationRule(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.validationRules });
    },
  });
};

// System Hooks
export const useSystemHealth = () => {
  return useQuery({
    queryKey: queryKeys.systemHealth,
    queryFn: () => apiClient.getSystemHealth(),
    refetchInterval: 60000, // Refetch every minute
    select: (data) => data.data,
  });
};

export const useSystemMetrics = () => {
  return useQuery({
    queryKey: queryKeys.systemMetrics,
    queryFn: () => apiClient.getSystemMetrics(),
    refetchInterval: 30000, // Refetch every 30 seconds
    select: (data) => data.data,
  });
};