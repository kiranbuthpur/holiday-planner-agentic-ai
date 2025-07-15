export interface WeatherData {
  location: string;
  temperature: number;
  description: string;
  humidity: number;
  windSpeed: number;
  uvIndex: number;
  feels_like: number;
  visibility: number;
  icon: string;
}

export interface Activity {
  id: string;
  name: string;
  description: string;
  category: 'indoor' | 'outdoor';
  duration: number; // in hours
  cost: number;
  location: string;
  rating: number;
  tags: string[];
  timeOfDay: 'morning' | 'afternoon' | 'evening' | 'any';
  weatherSuitability: {
    minTemp: number;
    maxTemp: number;
    allowRain: boolean;
    maxWindSpeed: number;
  };
}

export interface ItineraryItem {
  id: string;
  activity: Activity;
  startTime: string;
  endTime: string;
  date: string;
  notes?: string;
}

export interface DayPlan {
  date: string;
  morning: ItineraryItem[];
  afternoon: ItineraryItem[];
  evening: ItineraryItem[];
  weather: WeatherData;
}

export interface HolidayPlan {
  id: string;
  title: string;
  startDate: string;
  endDate: string;
  days: DayPlan[];
  totalBudget: number;
  preferences: UserPreferences;
}

export interface UserPreferences {
  budget: number;
  interests: string[];
  activityLevel: 'low' | 'moderate' | 'high';
  accommodations: string;
  transportation: string[];
  dietaryRestrictions: string[];
  accessibility: boolean;
}