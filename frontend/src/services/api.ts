import { ApiResponse, Job, DashboardMetrics, ValidationRule } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000/api';

class ApiClient {
  private baseURL: string;

  constructor(baseURL: string = API_BASE_URL) {
    this.baseURL = baseURL;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<ApiResponse<T>> {
    const url = `${this.baseURL}${endpoint}`;
    
    const config: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    };

    try {
      const response = await fetch(url, config);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const data = await response.json();
      return data;
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

  // Dashboard APIs
  async getDashboardMetrics(): Promise<ApiResponse<DashboardMetrics>> {
    return this.request<DashboardMetrics>('/dashboard/metrics');
  }

  // Job APIs
  async getJobs(params?: {
    status?: string;
    type?: string;
    limit?: number;
    offset?: number;
  }): Promise<ApiResponse<Job[]>> {
    const queryParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const endpoint = `/jobs${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return this.request<Job[]>(endpoint);
  }

  async getJob(id: string): Promise<ApiResponse<Job>> {
    return this.request<Job>(`/jobs/${id}`);
  }

  async createJob(jobData: Partial<Job>): Promise<ApiResponse<Job>> {
    return this.request<Job>('/jobs', {
      method: 'POST',
      body: JSON.stringify(jobData),
    });
  }

  async updateJob(id: string, jobData: Partial<Job>): Promise<ApiResponse<Job>> {
    return this.request<Job>(`/jobs/${id}`, {
      method: 'PUT',
      body: JSON.stringify(jobData),
    });
  }

  async deleteJob(id: string): Promise<ApiResponse<void>> {
    return this.request<void>(`/jobs/${id}`, {
      method: 'DELETE',
    });
  }

  async startJob(id: string): Promise<ApiResponse<Job>> {
    return this.request<Job>(`/jobs/${id}/start`, {
      method: 'POST',
    });
  }

  async stopJob(id: string): Promise<ApiResponse<Job>> {
    return this.request<Job>(`/jobs/${id}/stop`, {
      method: 'POST',
    });
  }

  async getJobLogs(id: string, params?: {
    level?: string;
    limit?: number;
    offset?: number;
  }): Promise<ApiResponse<any[]>> {
    const queryParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined) {
          queryParams.append(key, value.toString());
        }
      });
    }
    
    const endpoint = `/jobs/${id}/logs${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    return this.request<any[]>(endpoint);
  }

  // Validation Rules APIs
  async getValidationRules(): Promise<ApiResponse<ValidationRule[]>> {
    return this.request<ValidationRule[]>('/validation-rules');
  }

  async createValidationRule(rule: Partial<ValidationRule>): Promise<ApiResponse<ValidationRule>> {
    return this.request<ValidationRule>('/validation-rules', {
      method: 'POST',
      body: JSON.stringify(rule),
    });
  }

  async updateValidationRule(id: string, rule: Partial<ValidationRule>): Promise<ApiResponse<ValidationRule>> {
    return this.request<ValidationRule>(`/validation-rules/${id}`, {
      method: 'PUT',
      body: JSON.stringify(rule),
    });
  }

  async deleteValidationRule(id: string): Promise<ApiResponse<void>> {
    return this.request<void>(`/validation-rules/${id}`, {
      method: 'DELETE',
    });
  }

  // System APIs
  async getSystemHealth(): Promise<ApiResponse<any>> {
    return this.request<any>('/system/health');
  }

  async getSystemMetrics(): Promise<ApiResponse<any>> {
    return this.request<any>('/system/metrics');
  }
}

export const apiClient = new ApiClient();
export default apiClient;