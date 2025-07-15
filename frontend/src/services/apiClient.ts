import axios, { AxiosInstance, AxiosResponse, AxiosError } from 'axios';
import { ApiError, ApiResponse } from '../types';
import toast from 'react-hot-toast';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor
    this.client.interceptors.request.use(
      (config) => {
        // Add authentication token if available
        const token = localStorage.getItem('authToken');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }

        // Add user email to headers if available
        const userEmail = localStorage.getItem('userEmail');
        if (userEmail) {
          config.headers['X-User-Email'] = userEmail;
        }

        // Add request timestamp
        config.headers['X-Request-Time'] = new Date().toISOString();

        // Log requests in development
        if (process.env.NODE_ENV === 'development') {
          console.log(`🚀 API Request: ${config.method?.toUpperCase()} ${config.url}`, config.data);
        }

        return config;
      },
      (error) => {
        console.error('Request interceptor error:', error);
        return Promise.reject(error);
      }
    );

    // Response interceptor
    this.client.interceptors.response.use(
      (response: AxiosResponse) => {
        // Log responses in development
        if (process.env.NODE_ENV === 'development') {
          console.log(`✅ API Response: ${response.config.method?.toUpperCase()} ${response.config.url}`, response.data);
        }

        return response;
      },
      (error: AxiosError) => {
        const apiError = this.handleApiError(error);
        
        // Show error toast for client errors (except 401)
        if (apiError.status !== 401) {
          toast.error(apiError.message);
        }

        // Handle 401 errors (unauthorized)
        if (apiError.status === 401) {
          this.handleUnauthorized();
        }

        // Handle 403 errors (forbidden)
        if (apiError.status === 403) {
          toast.error('Access denied. You don\'t have permission to perform this action.');
        }

        // Handle 500 errors
        if (apiError.status >= 500) {
          toast.error('Server error. Please try again later.');
        }

        console.error('API Error:', apiError);
        return Promise.reject(apiError);
      }
    );
  }

  private handleApiError(error: AxiosError): ApiError {
    const timestamp = new Date().toISOString();

    if (error.response) {
      // Server responded with error status
      const { status, data } = error.response;
      return {
        status,
        message: (data as any)?.message || error.message || 'An error occurred',
        code: (data as any)?.code,
        details: (data as any)?.details,
        timestamp,
      };
    } else if (error.request) {
      // Request was made but no response received
      return {
        status: 0,
        message: 'Network error. Please check your connection.',
        timestamp,
      };
    } else {
      // Something else happened
      return {
        status: 0,
        message: error.message || 'An unexpected error occurred',
        timestamp,
      };
    }
  }

  private handleUnauthorized() {
    // Clear authentication data
    localStorage.removeItem('authToken');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userProfile');

    // Show login message
    toast.error('Your session has expired. Please log in again.');

    // Redirect to login page
    window.location.href = '/login';
  }

  // Generic HTTP methods
  async get<T>(url: string, params?: any): Promise<T> {
    const response = await this.client.get<T>(url, { params });
    return response.data;
  }

  async post<T>(url: string, data?: any): Promise<T> {
    const response = await this.client.post<T>(url, data);
    return response.data;
  }

  async put<T>(url: string, data?: any): Promise<T> {
    const response = await this.client.put<T>(url, data);
    return response.data;
  }

  async patch<T>(url: string, data?: any): Promise<T> {
    const response = await this.client.patch<T>(url, data);
    return response.data;
  }

  async delete<T>(url: string): Promise<T> {
    const response = await this.client.delete<T>(url);
    return response.data;
  }

  // File upload method
  async uploadFile<T>(url: string, file: File, additionalData?: any): Promise<T> {
    const formData = new FormData();
    formData.append('file', file);

    // Add additional data to form
    if (additionalData) {
      Object.keys(additionalData).forEach(key => {
        formData.append(key, additionalData[key]);
      });
    }

    const response = await this.client.post<T>(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / (progressEvent.total || 1));
        console.log(`Upload progress: ${percentCompleted}%`);
      },
    });

    return response.data;
  }

  // Download file method
  async downloadFile(url: string, filename?: string): Promise<void> {
    const response = await this.client.get(url, {
      responseType: 'blob',
    });

    // Create blob URL and trigger download
    const blob = new Blob([response.data], { 
      type: response.headers['content-type'] || 'application/octet-stream' 
    });
    const blobUrl = window.URL.createObjectURL(blob);
    
    const link = document.createElement('a');
    link.href = blobUrl;
    link.download = filename || 'download';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    // Clean up
    window.URL.revokeObjectURL(blobUrl);
  }

  // Server-Sent Events method
  createEventSource(url: string): EventSource {
    const token = localStorage.getItem('authToken');
    const eventSourceUrl = `${API_BASE_URL}${url}${token ? `?token=${token}` : ''}`;
    return new EventSource(eventSourceUrl);
  }

  // Health check
  async healthCheck(): Promise<{ status: string; timestamp: string }> {
    return this.get('/health');
  }

  // Authentication methods
  async login(email: string, password: string): Promise<{ token: string; user: any }> {
    const response = await this.post('/auth/login', { email, password });
    
    // Store authentication data
    if (response.token) {
      localStorage.setItem('authToken', response.token);
      localStorage.setItem('userEmail', email);
      localStorage.setItem('userProfile', JSON.stringify(response.user));
    }
    
    return response;
  }

  async logout(): Promise<void> {
    try {
      await this.post('/auth/logout');
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      // Clear local storage regardless of API response
      localStorage.removeItem('authToken');
      localStorage.removeItem('userEmail');
      localStorage.removeItem('userProfile');
    }
  }

  async refreshToken(): Promise<{ token: string }> {
    const response = await this.post('/auth/refresh');
    
    if (response.token) {
      localStorage.setItem('authToken', response.token);
    }
    
    return response;
  }

  // User methods
  getCurrentUser(): any {
    const userProfile = localStorage.getItem('userProfile');
    return userProfile ? JSON.parse(userProfile) : null;
  }

  getUserEmail(): string | null {
    return localStorage.getItem('userEmail');
  }

  isAuthenticated(): boolean {
    const token = localStorage.getItem('authToken');
    return !!token;
  }

  // Utility methods
  setAuthToken(token: string): void {
    localStorage.setItem('authToken', token);
  }

  clearAuthToken(): void {
    localStorage.removeItem('authToken');
  }

  // Request cancellation
  createCancelToken() {
    return axios.CancelToken.source();
  }

  // Batch requests
  async batchRequests<T>(requests: Array<() => Promise<T>>): Promise<T[]> {
    return Promise.all(requests.map(request => request()));
  }

  // Retry mechanism
  async retryRequest<T>(
    requestFn: () => Promise<T>,
    maxRetries: number = 3,
    delay: number = 1000
  ): Promise<T> {
    for (let i = 0; i < maxRetries; i++) {
      try {
        return await requestFn();
      } catch (error) {
        if (i === maxRetries - 1) throw error;
        
        console.log(`Request failed, retrying in ${delay}ms... (${i + 1}/${maxRetries})`);
        await new Promise(resolve => setTimeout(resolve, delay));
        delay *= 2; // Exponential backoff
      }
    }
    
    throw new Error('Max retries reached');
  }

  // Cache management
  private cache = new Map<string, { data: any; timestamp: number; ttl: number }>();

  async getCached<T>(
    key: string,
    requestFn: () => Promise<T>,
    ttl: number = 300000 // 5 minutes
  ): Promise<T> {
    const cached = this.cache.get(key);
    const now = Date.now();

    if (cached && (now - cached.timestamp) < cached.ttl) {
      console.log(`Cache hit for key: ${key}`);
      return cached.data;
    }

    console.log(`Cache miss for key: ${key}`);
    const data = await requestFn();
    this.cache.set(key, { data, timestamp: now, ttl });
    return data;
  }

  clearCache(key?: string): void {
    if (key) {
      this.cache.delete(key);
    } else {
      this.cache.clear();
    }
  }

  // Request queuing for rate limiting
  private requestQueue: Array<{ fn: () => Promise<any>; resolve: (value: any) => void; reject: (error: any) => void }> = [];
  private isProcessingQueue = false;

  async queueRequest<T>(requestFn: () => Promise<T>): Promise<T> {
    return new Promise((resolve, reject) => {
      this.requestQueue.push({ fn: requestFn, resolve, reject });
      this.processQueue();
    });
  }

  private async processQueue() {
    if (this.isProcessingQueue || this.requestQueue.length === 0) return;
    
    this.isProcessingQueue = true;
    
    while (this.requestQueue.length > 0) {
      const { fn, resolve, reject } = this.requestQueue.shift()!;
      
      try {
        const result = await fn();
        resolve(result);
      } catch (error) {
        reject(error);
      }
      
      // Add delay between requests to respect rate limits
      await new Promise(resolve => setTimeout(resolve, 100));
    }
    
    this.isProcessingQueue = false;
  }

  // Performance monitoring
  private performanceMetrics = {
    requests: 0,
    totalResponseTime: 0,
    errors: 0,
  };

  getPerformanceMetrics() {
    return {
      ...this.performanceMetrics,
      averageResponseTime: this.performanceMetrics.totalResponseTime / this.performanceMetrics.requests || 0,
      errorRate: this.performanceMetrics.errors / this.performanceMetrics.requests || 0,
    };
  }

  resetPerformanceMetrics() {
    this.performanceMetrics = {
      requests: 0,
      totalResponseTime: 0,
      errors: 0,
    };
  }
}

// Create singleton instance
const apiClient = new ApiClient();

export default apiClient;