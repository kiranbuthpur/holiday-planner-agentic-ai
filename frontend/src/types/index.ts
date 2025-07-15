// Core Domain Types
export interface HolidayPlan {
  id: number;
  title: string;
  destination: string;
  startDate: string;
  endDate: string;
  userEmail: string;
  status: PlanStatus;
  activities: Activity[];
  notes?: string;
  weatherOptimizationEnabled: boolean;
  lastWeatherUpdate?: string;
  googleCalendarEventId?: string;
  createdDate: string;
  lastModifiedDate: string;
}

export enum PlanStatus {
  DRAFT = 'DRAFT',
  CONFIRMED = 'CONFIRMED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export interface Activity {
  id: number;
  name: string;
  description?: string;
  date: string;
  startTime?: string;
  endTime?: string;
  type: ActivityType;
  timeSlot: TimeSlot;
  location: string;
  weatherDependent: boolean;
  preferredWeather?: string;
  minTemperature?: number;
  maxTemperature?: number;
  maxHumidity?: number;
  avoidRain: boolean;
  priorityLevel: number;
  estimatedDurationMinutes?: number;
  costEstimate?: number;
  bookingRequired: boolean;
  bookingUrl?: string;
  contactInfo?: string;
  notes?: string;
  aiOptimized: boolean;
  optimizationReason?: string;
  holidayPlan?: HolidayPlan;
  createdDate: string;
  lastModifiedDate: string;
}

export enum ActivityType {
  SIGHTSEEING = 'SIGHTSEEING',
  MUSEUM = 'MUSEUM',
  RESTAURANT = 'RESTAURANT',
  SHOPPING = 'SHOPPING',
  OUTDOOR_ACTIVITY = 'OUTDOOR_ACTIVITY',
  ENTERTAINMENT = 'ENTERTAINMENT',
  TRANSPORTATION = 'TRANSPORTATION',
  ACCOMMODATION = 'ACCOMMODATION',
  CULTURAL = 'CULTURAL',
  SPORTS = 'SPORTS',
  RELAXATION = 'RELAXATION',
  ADVENTURE = 'ADVENTURE',
  NIGHTLIFE = 'NIGHTLIFE',
  HISTORICAL = 'HISTORICAL',
  RELIGIOUS = 'RELIGIOUS',
  NATURE = 'NATURE',
  FOOD_EXPERIENCE = 'FOOD_EXPERIENCE',
  WATER_ACTIVITY = 'WATER_ACTIVITY',
  MOUNTAIN_ACTIVITY = 'MOUNTAIN_ACTIVITY',
  CITY_TOUR = 'CITY_TOUR',
  OTHER = 'OTHER'
}

export enum TimeSlot {
  MORNING = 'MORNING',
  AFTERNOON = 'AFTERNOON',
  EVENING = 'EVENING',
  NIGHT = 'NIGHT',
  FULL_DAY = 'FULL_DAY'
}

export interface WeatherData {
  id: number;
  city: string;
  country: string;
  date: string;
  forecastHour?: number;
  temperatureCelsius: number;
  feelsLikeCelsius: number;
  temperatureMin: number;
  temperatureMax: number;
  humidityPercent: number;
  pressureHpa: number;
  windSpeedMps: number;
  windDirectionDegrees?: number;
  cloudinessPercent: number;
  visibilityMeters?: number;
  uvIndex?: number;
  weatherMain: string;
  weatherDescription: string;
  weatherIcon: string;
  rain1hMm?: number;
  rain3hMm?: number;
  snow1hMm?: number;
  snow3hMm?: number;
  sunriseTime?: string;
  sunsetTime?: string;
  airQualityIndex?: number;
  forecastType: ForecastType;
  dataSource: string;
  aiComfortScore: number;
  outdoorActivityScore: number;
  indoorActivityScore: number;
  createdDate: string;
}

export enum ForecastType {
  CURRENT = 'CURRENT',
  HOURLY = 'HOURLY',
  DAILY = 'DAILY',
  HISTORICAL = 'HISTORICAL'
}

// API Response Types
export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
  timestamp: string;
}

export interface PagedResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalPages: number;
  totalElements: number;
  last: boolean;
  first: boolean;
  numberOfElements: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  empty: boolean;
}

export interface OptimizationResult {
  message: string;
  optimizedActivities: number;
  planId: number;
}

export interface HolidayPlanStatistics {
  totalActivities: number;
  totalDays: number;
  totalEstimatedCost: number;
  totalEstimatedDurationMinutes: number;
  activitiesByType: Record<ActivityType, number>;
  activitiesByTimeSlot: Record<TimeSlot, number>;
  weatherDependentActivities: number;
  aiOptimizedActivities: number;
}

export interface ExportResult {
  data: string;
  filename: string;
  contentType: string;
}

export interface ShareResult {
  message: string;
  recipient: string;
  planTitle: string;
}

// Form Types
export interface CreateHolidayPlanForm {
  title: string;
  destination: string;
  startDate: string;
  endDate: string;
  userEmail: string;
  notes?: string;
  weatherOptimizationEnabled: boolean;
}

export interface CreateActivityForm {
  name: string;
  description?: string;
  date: string;
  startTime?: string;
  endTime?: string;
  type: ActivityType;
  timeSlot: TimeSlot;
  location: string;
  weatherDependent: boolean;
  preferredWeather?: string;
  minTemperature?: number;
  maxTemperature?: number;
  maxHumidity?: number;
  avoidRain: boolean;
  priorityLevel: number;
  estimatedDurationMinutes?: number;
  costEstimate?: number;
  bookingRequired: boolean;
  bookingUrl?: string;
  contactInfo?: string;
  notes?: string;
}

export interface ExcelUploadForm {
  file: File;
  userEmail: string;
  destination: string;
}

// UI Component Types
export interface FilterOptions {
  userEmail?: string;
  destination?: string;
  status?: PlanStatus;
  startDate?: string;
  endDate?: string;
}

export interface SortOptions {
  field: string;
  direction: 'asc' | 'desc';
}

export interface PaginationOptions {
  page: number;
  size: number;
  sort?: SortOptions;
}

export interface WeatherWidgetData {
  current: WeatherData;
  forecast: WeatherData[];
  alerts: WeatherAlert[];
}

export interface WeatherAlert {
  id: string;
  type: 'warning' | 'info' | 'error';
  title: string;
  message: string;
  severity: 'low' | 'medium' | 'high';
  timestamp: string;
  affectedActivities: number[];
}

// Calendar Types
export interface CalendarEvent {
  id: string;
  title: string;
  start: string;
  end: string;
  allDay: boolean;
  resource: Activity;
  className: string;
  textColor: string;
  backgroundColor: string;
}

export interface CalendarView {
  type: 'month' | 'week' | 'day' | 'agenda';
  date: string;
}

// Theme Types
export interface Theme {
  palette: {
    primary: {
      main: string;
      light: string;
      dark: string;
    };
    secondary: {
      main: string;
      light: string;
      dark: string;
    };
    background: {
      default: string;
      paper: string;
    };
    text: {
      primary: string;
      secondary: string;
    };
    error: {
      main: string;
    };
    warning: {
      main: string;
    };
    info: {
      main: string;
    };
    success: {
      main: string;
    };
  };
  typography: {
    fontFamily: string;
    fontSize: number;
    h1: {
      fontSize: string;
      fontWeight: number;
    };
    h2: {
      fontSize: string;
      fontWeight: number;
    };
    h3: {
      fontSize: string;
      fontWeight: number;
    };
    h4: {
      fontSize: string;
      fontWeight: number;
    };
    h5: {
      fontSize: string;
      fontWeight: number;
    };
    h6: {
      fontSize: string;
      fontWeight: number;
    };
    body1: {
      fontSize: string;
    };
    body2: {
      fontSize: string;
    };
  };
  spacing: (factor: number) => number;
}

// Notification Types
export interface Notification {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  duration?: number;
  timestamp: string;
  read: boolean;
}

// User Types
export interface User {
  id: string;
  email: string;
  name: string;
  avatar?: string;
  preferences: UserPreferences;
  subscription: SubscriptionLevel;
  createdDate: string;
  lastLoginDate: string;
}

export interface UserPreferences {
  theme: 'light' | 'dark' | 'auto';
  language: string;
  timezone: string;
  notifications: {
    email: boolean;
    push: boolean;
    weather: boolean;
    reminders: boolean;
  };
  weather: {
    units: 'metric' | 'imperial';
    defaultLocation: string;
  };
  calendar: {
    defaultView: 'month' | 'week' | 'day';
    startWeek: 'monday' | 'sunday';
  };
}

export enum SubscriptionLevel {
  FREE = 'FREE',
  PREMIUM = 'PREMIUM',
  ENTERPRISE = 'ENTERPRISE'
}

// Error Types
export interface ApiError {
  status: number;
  message: string;
  code?: string;
  details?: Record<string, unknown>;
  timestamp: string;
}

// Loading States
export interface LoadingState {
  isLoading: boolean;
  error?: ApiError;
  lastUpdated?: string;
}

// Navigation Types
export interface NavigationItem {
  id: string;
  title: string;
  icon: string;
  path: string;
  badge?: number;
  children?: NavigationItem[];
  roles?: string[];
}

// Chart Types
export interface ChartData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
    backgroundColor: string | string[];
    borderColor: string | string[];
    borderWidth: number;
  }[];
}

export interface WeatherChartData {
  time: string;
  temperature: number;
  humidity: number;
  windSpeed: number;
  precipitation: number;
  outdoorScore: number;
  indoorScore: number;
}

// Map Types
export interface MapLocation {
  id: string;
  name: string;
  latitude: number;
  longitude: number;
  type: 'activity' | 'accommodation' | 'restaurant' | 'attraction';
  description?: string;
  activity?: Activity;
}

export interface MapBounds {
  northeast: {
    lat: number;
    lng: number;
  };
  southwest: {
    lat: number;
    lng: number;
  };
}

// Email Template Types
export interface EmailTemplate {
  id: string;
  name: string;
  subject: string;
  htmlContent: string;
  variables: Record<string, string>;
  type: 'optimization' | 'reminder' | 'weather_alert' | 'daily_itinerary';
}

// Google Calendar Integration Types
export interface GoogleCalendarConfig {
  clientId: string;
  apiKey: string;
  scopes: string[];
  discoveryDocs: string[];
}

export interface CalendarIntegration {
  id: string;
  provider: 'google' | 'outlook' | 'apple';
  connected: boolean;
  lastSync: string;
  settings: {
    autoSync: boolean;
    calendarId: string;
    reminderMinutes: number;
  };
}

// Drag and Drop Types
export interface DragItem {
  id: string;
  type: 'activity';
  activity: Activity;
  sourceDate: string;
  sourceTimeSlot: TimeSlot;
}

export interface DropResult {
  draggableId: string;
  type: string;
  source: {
    droppableId: string;
    index: number;
  };
  destination: {
    droppableId: string;
    index: number;
  } | null;
}

// Search Types
export interface SearchQuery {
  term: string;
  filters: {
    type?: ActivityType[];
    timeSlot?: TimeSlot[];
    weatherDependent?: boolean;
    location?: string;
    dateRange?: {
      start: string;
      end: string;
    };
  };
  sort: SortOptions;
}

export interface SearchResult<T> {
  items: T[];
  total: number;
  query: SearchQuery;
  facets: {
    [key: string]: {
      [value: string]: number;
    };
  };
}

// Analytics Types
export interface AnalyticsEvent {
  name: string;
  properties: Record<string, unknown>;
  timestamp: string;
  userId?: string;
  sessionId: string;
}

export interface AnalyticsMetrics {
  pageViews: number;
  uniqueVisitors: number;
  averageSessionDuration: number;
  bounceRate: number;
  conversions: number;
  topPages: { path: string; views: number }[];
  topCountries: { country: string; visitors: number }[];
}

// Utility Types
export type DeepPartial<T> = {
  [P in keyof T]?: T[P] extends object ? DeepPartial<T[P]> : T[P];
};

export type Optional<T, K extends keyof T> = Omit<T, K> & Partial<Pick<T, K>>;

export type RequiredFields<T, K extends keyof T> = T & Required<Pick<T, K>>;

export type ID = string | number;

export type Callback<T = void> = (data: T) => void;

export type AsyncCallback<T = void> = (data: T) => Promise<void>;

export type EventHandler<T = Event> = (event: T) => void;

export type KeyValuePair<T = unknown> = { [key: string]: T };

export type SelectOption = {
  value: string | number;
  label: string;
  disabled?: boolean;
};

export type TabItem = {
  id: string;
  label: string;
  icon?: string;
  badge?: number;
  disabled?: boolean;
  content: React.ReactNode;
};