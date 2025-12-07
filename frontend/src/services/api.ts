import { 
  ApiResponse, 
  Job, 
  DashboardMetrics, 
  ValidationRule,
  Dataset,
  DataQualityReport,
  DataQualityScore,
  DataQualityResult,
  SharePointConfig,
  DataQualityMetric
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api';

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

  // Data Quality APIs
  async analyzeDataset(dataset: Dataset): Promise<DataQualityReport> {
    const response = await this.request<DataQualityReport>('/data-quality/analyze', {
      method: 'POST',
      body: JSON.stringify(dataset),
    });
    return response.data || response as any;
  }

  async calculateScore(dataset: Dataset): Promise<DataQualityScore> {
    const response = await this.request<DataQualityScore>('/data-quality/score', {
      method: 'POST',
      body: JSON.stringify(dataset),
    });
    return response.data || response as any;
  }

  async analyzeOnly(dataset: Dataset): Promise<{ results: DataQualityResult[]; score: DataQualityScore }> {
    const response = await this.request<{ results: DataQualityResult[]; score: DataQualityScore }>('/data-quality/analyze-only', {
      method: 'POST',
      body: JSON.stringify(dataset),
    });
    return response.data || response as any;
  }

  async getRecommendations(results: DataQualityResult[]): Promise<{ recommendations: string[] }> {
    const response = await this.request<{ recommendations: string[] }>('/data-quality/recommendations', {
      method: 'POST',
      body: JSON.stringify(results),
    });
    return response.data || response as any;
  }

  async getAvailableMetrics(): Promise<{ metrics: DataQualityMetric[]; descriptions: Record<string, string> }> {
    const response = await this.request<{ metrics: DataQualityMetric[]; descriptions: Record<string, string> }>('/data-quality/metrics');
    return response.data || response as any;
  }

  async generateSampleDataset(): Promise<Dataset> {
    const response = await this.request<Dataset>('/data-quality/sample-dataset', {
      method: 'POST',
    });
    return response.data || response as any;
  }

  async getDataQualityHealth(): Promise<any> {
    const response = await this.request<any>('/data-quality/health');
    return response.data || response as any;
  }

  async processPrompt(prompt: string): Promise<any> {
    const response = await this.request<any>('/data-quality/prompt', {
      method: 'POST',
      body: JSON.stringify({ prompt }),
    });
    return response.data || response as any;
  }

  // SharePoint APIs
  async listSharePointFiles(): Promise<{ files: string[]; count: number; timestamp: number }> {
    const response = await this.request<{ files: string[]; count: number; timestamp: number }>('/sharepoint/files');
    return response.data || response as any;
  }

  async fetchSharePointFile(fileName: string): Promise<Dataset> {
    const response = await this.request<Dataset>(`/sharepoint/files/${encodeURIComponent(fileName)}`);
    return response.data || response as any;
  }

  async analyzeSharePointFile(fileName: string): Promise<DataQualityReport> {
    const response = await this.request<DataQualityReport>(`/sharepoint/analyze/${encodeURIComponent(fileName)}`, {
      method: 'POST',
    });
    return response.data || response as any;
  }

  async analyzeSharePointFileOnly(fileName: string): Promise<{
    results: DataQualityResult[];
    score: DataQualityScore;
    dataset: Dataset;
    sourceType: string;
    originalFileName: string;
  }> {
    const response = await this.request<{
      results: DataQualityResult[];
      score: DataQualityScore;
      dataset: Dataset;
      sourceType: string;
      originalFileName: string;
    }>(`/sharepoint/analyze-only/${encodeURIComponent(fileName)}`, {
      method: 'POST',
    });
    return response.data || response as any;
  }

  async getSharePointConfig(): Promise<SharePointConfig> {
    const response = await this.request<SharePointConfig>('/sharepoint/config');
    return response.data || response as any;
  }

  async getSharePointHealth(): Promise<any> {
    const response = await this.request<any>('/sharepoint/health');
    return response.data || response as any;
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