import apiClient from './apiClient';
import {
  HolidayPlan,
  Activity,
  CreateHolidayPlanForm,
  ExcelUploadForm,
  PlanStatus,
  FilterOptions,
  PaginationOptions,
  PagedResponse,
  OptimizationResult,
  HolidayPlanStatistics,
  ExportResult,
  ShareResult,
} from '../types';

class HolidayPlanService {
  private basePath = '/api/holidays';

  // Basic CRUD operations
  async getAllPlans(
    filters: FilterOptions = {},
    pagination: PaginationOptions = { page: 0, size: 20 }
  ): Promise<PagedResponse<HolidayPlan>> {
    const params = {
      ...filters,
      page: pagination.page,
      size: pagination.size,
      sort: pagination.sort ? `${pagination.sort.field},${pagination.sort.direction}` : undefined,
    };

    return apiClient.get(this.basePath, params);
  }

  async getPlanById(id: number): Promise<HolidayPlan> {
    return apiClient.get(`${this.basePath}/${id}`);
  }

  async createPlan(planData: CreateHolidayPlanForm): Promise<HolidayPlan> {
    return apiClient.post(this.basePath, planData);
  }

  async updatePlan(id: number, planData: Partial<CreateHolidayPlanForm>): Promise<HolidayPlan> {
    return apiClient.put(`${this.basePath}/${id}`, planData);
  }

  async deletePlan(id: number): Promise<void> {
    return apiClient.delete(`${this.basePath}/${id}`);
  }

  // Excel integration
  async uploadExcelFile(uploadData: ExcelUploadForm): Promise<HolidayPlan> {
    return apiClient.uploadFile(
      `${this.basePath}/upload`,
      uploadData.file,
      {
        userEmail: uploadData.userEmail,
        destination: uploadData.destination,
      }
    );
  }

  async exportPlan(id: number, format: 'excel' | 'pdf' = 'excel'): Promise<ExportResult> {
    return apiClient.post(`${this.basePath}/${id}/export`, { format });
  }

  // Activity management
  async getPlanActivities(planId: number): Promise<Activity[]> {
    return apiClient.get(`${this.basePath}/${planId}/activities`);
  }

  async getActivitiesForDate(planId: number, date: string): Promise<Activity[]> {
    return apiClient.get(`${this.basePath}/${planId}/activities/date/${date}`);
  }

  async addActivity(planId: number, activityData: any): Promise<Activity> {
    return apiClient.post(`${this.basePath}/${planId}/activities`, activityData);
  }

  async updateActivity(planId: number, activityId: number, activityData: any): Promise<Activity> {
    return apiClient.put(`${this.basePath}/${planId}/activities/${activityId}`, activityData);
  }

  async deleteActivity(planId: number, activityId: number): Promise<void> {
    return apiClient.delete(`${this.basePath}/${planId}/activities/${activityId}`);
  }

  // Weather optimization
  async optimizePlan(planId: number): Promise<OptimizationResult> {
    return apiClient.post(`${this.basePath}/${planId}/optimize`);
  }

  // Statistics and analytics
  async getPlanStatistics(planId: number): Promise<HolidayPlanStatistics> {
    return apiClient.get(`${this.basePath}/${planId}/statistics`);
  }

  // Plan management
  async clonePlan(planId: number, userEmail: string, newTitle?: string): Promise<HolidayPlan> {
    return apiClient.post(`${this.basePath}/${planId}/clone`, {
      userEmail,
      newTitle,
    });
  }

  async sharePlan(planId: number, recipientEmail: string, message?: string): Promise<ShareResult> {
    return apiClient.post(`${this.basePath}/${planId}/share`, {
      recipientEmail,
      message,
    });
  }

  async sendReminder(planId: number): Promise<{ message: string; daysUntilTrip: number }> {
    return apiClient.post(`${this.basePath}/${planId}/send-reminder`);
  }

  // User-specific queries
  async getUpcomingHolidays(userEmail: string, days: number = 30): Promise<HolidayPlan[]> {
    return apiClient.get(`${this.basePath}/user/${userEmail}/upcoming`, { days });
  }

  async getUserDestinations(userEmail: string): Promise<string[]> {
    return apiClient.get(`${this.basePath}/user/${userEmail}/destinations`);
  }

  // General queries
  async getPopularDestinations(): Promise<string[]> {
    return apiClient.get(`${this.basePath}/destinations`);
  }

  // Plan status management
  async updatePlanStatus(planId: number, status: PlanStatus): Promise<HolidayPlan> {
    return this.updatePlan(planId, { status } as any);
  }

  async confirmPlan(planId: number): Promise<HolidayPlan> {
    return this.updatePlanStatus(planId, PlanStatus.CONFIRMED);
  }

  async startPlan(planId: number): Promise<HolidayPlan> {
    return this.updatePlanStatus(planId, PlanStatus.IN_PROGRESS);
  }

  async completePlan(planId: number): Promise<HolidayPlan> {
    return this.updatePlanStatus(planId, PlanStatus.COMPLETED);
  }

  async cancelPlan(planId: number): Promise<HolidayPlan> {
    return this.updatePlanStatus(planId, PlanStatus.CANCELLED);
  }

  // Bulk operations
  async bulkUpdateActivities(planId: number, activities: Partial<Activity>[]): Promise<Activity[]> {
    return apiClient.put(`${this.basePath}/${planId}/activities/bulk`, { activities });
  }

  async bulkDeleteActivities(planId: number, activityIds: number[]): Promise<void> {
    return apiClient.delete(`${this.basePath}/${planId}/activities/bulk`, { activityIds });
  }

  // Search and filtering
  async searchPlans(
    query: string,
    filters: FilterOptions = {},
    pagination: PaginationOptions = { page: 0, size: 20 }
  ): Promise<PagedResponse<HolidayPlan>> {
    const params = {
      q: query,
      ...filters,
      page: pagination.page,
      size: pagination.size,
      sort: pagination.sort ? `${pagination.sort.field},${pagination.sort.direction}` : undefined,
    };

    return apiClient.get(`${this.basePath}/search`, params);
  }

  async getPlansForDateRange(
    startDate: string,
    endDate: string,
    userEmail?: string
  ): Promise<HolidayPlan[]> {
    return apiClient.get(`${this.basePath}/date-range`, {
      startDate,
      endDate,
      userEmail,
    });
  }

  // Template management
  async saveAsTemplate(planId: number, templateName: string): Promise<{ id: string; name: string }> {
    return apiClient.post(`${this.basePath}/${planId}/save-template`, { templateName });
  }

  async getTemplates(userEmail?: string): Promise<{ id: string; name: string; destination: string }[]> {
    return apiClient.get(`${this.basePath}/templates`, { userEmail });
  }

  async createFromTemplate(templateId: string, planData: Partial<CreateHolidayPlanForm>): Promise<HolidayPlan> {
    return apiClient.post(`${this.basePath}/from-template/${templateId}`, planData);
  }

  // Weather integration
  async getWeatherForecast(planId: number): Promise<any> {
    return apiClient.get(`${this.basePath}/${planId}/weather-forecast`);
  }

  async getWeatherAlerts(planId: number): Promise<any[]> {
    return apiClient.get(`${this.basePath}/${planId}/weather-alerts`);
  }

  // Calendar integration
  async syncWithGoogleCalendar(planId: number): Promise<{ success: boolean; eventId?: string }> {
    return apiClient.post(`${this.basePath}/${planId}/sync-calendar`);
  }

  async removeFromCalendar(planId: number): Promise<{ success: boolean }> {
    return apiClient.delete(`${this.basePath}/${planId}/calendar`);
  }

  // Collaboration
  async addCollaborator(planId: number, collaboratorEmail: string, role: 'viewer' | 'editor' = 'viewer'): Promise<void> {
    return apiClient.post(`${this.basePath}/${planId}/collaborators`, {
      email: collaboratorEmail,
      role,
    });
  }

  async removeCollaborator(planId: number, collaboratorEmail: string): Promise<void> {
    return apiClient.delete(`${this.basePath}/${planId}/collaborators/${collaboratorEmail}`);
  }

  async getCollaborators(planId: number): Promise<Array<{ email: string; role: string; addedDate: string }>> {
    return apiClient.get(`${this.basePath}/${planId}/collaborators`);
  }

  // Comments and notes
  async addComment(planId: number, activityId: number, comment: string): Promise<any> {
    return apiClient.post(`${this.basePath}/${planId}/activities/${activityId}/comments`, { comment });
  }

  async getComments(planId: number, activityId: number): Promise<any[]> {
    return apiClient.get(`${this.basePath}/${planId}/activities/${activityId}/comments`);
  }

  async updatePlanNotes(planId: number, notes: string): Promise<HolidayPlan> {
    return this.updatePlan(planId, { notes } as any);
  }

  // Cost tracking
  async updateBudget(planId: number, budget: number): Promise<HolidayPlan> {
    return apiClient.put(`${this.basePath}/${planId}/budget`, { budget });
  }

  async getCostBreakdown(planId: number): Promise<{
    totalCost: number;
    byCategory: Record<string, number>;
    byDate: Record<string, number>;
  }> {
    return apiClient.get(`${this.basePath}/${planId}/cost-breakdown`);
  }

  // Recommendations
  async getRecommendations(planId: number): Promise<{
    activities: Activity[];
    restaurants: any[];
    accommodations: any[];
    transportation: any[];
  }> {
    return apiClient.get(`${this.basePath}/${planId}/recommendations`);
  }

  async getAlternativeActivities(planId: number, activityId: number): Promise<Activity[]> {
    return apiClient.get(`${this.basePath}/${planId}/activities/${activityId}/alternatives`);
  }

  // Reporting
  async generateReport(planId: number, type: 'summary' | 'detailed' | 'cost' = 'summary'): Promise<any> {
    return apiClient.get(`${this.basePath}/${planId}/report`, { type });
  }

  async getUsageStatistics(userEmail: string): Promise<{
    totalPlans: number;
    totalActivities: number;
    totalDestinations: number;
    averagePlanDuration: number;
    mostVisitedDestinations: string[];
  }> {
    return apiClient.get(`${this.basePath}/user/${userEmail}/statistics`);
  }

  // Utility methods
  async validatePlan(planData: CreateHolidayPlanForm): Promise<{ valid: boolean; errors: string[] }> {
    return apiClient.post(`${this.basePath}/validate`, planData);
  }

  async duplicateActivities(planId: number, activityIds: number[], targetDate: string): Promise<Activity[]> {
    return apiClient.post(`${this.basePath}/${planId}/duplicate-activities`, {
      activityIds,
      targetDate,
    });
  }

  async optimizeForBudget(planId: number, maxBudget: number): Promise<{
    optimizedActivities: Activity[];
    removedActivities: Activity[];
    totalCost: number;
  }> {
    return apiClient.post(`${this.basePath}/${planId}/optimize-budget`, { maxBudget });
  }

  // Caching helpers
  async getCachedPlan(planId: number): Promise<HolidayPlan> {
    return apiClient.getCached(
      `plan_${planId}`,
      () => this.getPlanById(planId),
      300000 // 5 minutes cache
    );
  }

  async getCachedActivities(planId: number): Promise<Activity[]> {
    return apiClient.getCached(
      `activities_${planId}`,
      () => this.getPlanActivities(planId),
      180000 // 3 minutes cache
    );
  }

  // Error handling helpers
  async safeGetPlan(planId: number): Promise<HolidayPlan | null> {
    try {
      return await this.getPlanById(planId);
    } catch (error) {
      console.error('Error fetching plan:', error);
      return null;
    }
  }

  async safeOptimizePlan(planId: number): Promise<OptimizationResult | null> {
    try {
      return await this.optimizePlan(planId);
    } catch (error) {
      console.error('Error optimizing plan:', error);
      return null;
    }
  }

  // Real-time updates
  subscribeToUpdates(planId: number, callback: (data: any) => void): EventSource {
    const eventSource = apiClient.createEventSource(`${this.basePath}/${planId}/updates`);
    
    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        callback(data);
      } catch (error) {
        console.error('Error parsing SSE data:', error);
      }
    };

    return eventSource;
  }
}

// Create singleton instance
const holidayPlanService = new HolidayPlanService();

export default holidayPlanService;