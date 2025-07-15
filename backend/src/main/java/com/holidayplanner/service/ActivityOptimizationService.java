package com.holidayplanner.service;

import com.holidayplanner.model.Activity;
import com.holidayplanner.model.HolidayPlan;
import com.holidayplanner.model.WeatherData;
import com.holidayplanner.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityOptimizationService {
    
    private final ActivityRepository activityRepository;
    private final WeatherService weatherService;
    private final ChatClient chatClient;
    
    @Value("${ai.agent.activity-optimization.indoor-temp-threshold:28}")
    private double indoorTempThreshold;
    
    @Value("${ai.agent.activity-optimization.outdoor-temp-threshold:25}")
    private double outdoorTempThreshold;
    
    @Value("${ai.agent.activity-optimization.comfort-humidity-max:70}")
    private int comfortHumidityMax;
    
    @Transactional
    public List<Activity> optimizeActivitiesForWeather(HolidayPlan holidayPlan) {
        log.info("Starting activity optimization for holiday plan: {}", holidayPlan.getId());
        
        List<Activity> activities = activityRepository.findByHolidayPlanIdOrderByDateAscStartTimeAsc(holidayPlan.getId());
        List<Activity> optimizedActivities = new ArrayList<>();
        
        // Group activities by date
        Map<LocalDate, List<Activity>> activitiesByDate = activities.stream()
                .collect(Collectors.groupingBy(Activity::getDate));
        
        for (Map.Entry<LocalDate, List<Activity>> entry : activitiesByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<Activity> dailyActivities = entry.getValue();
            
            // Get weather data for the date
            List<WeatherData> weatherData = weatherService.getWeatherForDateRange(
                    holidayPlan.getDestination(), "IT", date, date);
            
            if (!weatherData.isEmpty()) {
                List<Activity> optimizedDailyActivities = optimizeDailyActivities(dailyActivities, weatherData);
                optimizedActivities.addAll(optimizedDailyActivities);
            } else {
                log.warn("No weather data available for date: {}", date);
                optimizedActivities.addAll(dailyActivities);
            }
        }
        
        // Save optimized activities
        optimizedActivities.forEach(activity -> {
            activity.setAiOptimized(true);
            activityRepository.save(activity);
        });
        
        log.info("Completed activity optimization for {} activities", optimizedActivities.size());
        return optimizedActivities;
    }
    
    private List<Activity> optimizeDailyActivities(List<Activity> activities, List<WeatherData> weatherData) {
        List<Activity> optimizedActivities = new ArrayList<>(activities);
        
        // Get hourly weather data
        Map<Integer, WeatherData> hourlyWeather = weatherData.stream()
                .filter(wd -> wd.getForecastHour() != null)
                .collect(Collectors.toMap(WeatherData::getForecastHour, wd -> wd));
        
        // Separate indoor and outdoor activities
        List<Activity> indoorActivities = activities.stream()
                .filter(this::isIndoorActivity)
                .collect(Collectors.toList());
        
        List<Activity> outdoorActivities = activities.stream()
                .filter(this::isOutdoorActivity)
                .collect(Collectors.toList());
        
        // Optimize timing based on weather
        optimizeOutdoorActivities(outdoorActivities, hourlyWeather);
        optimizeIndoorActivities(indoorActivities, hourlyWeather);
        
        // Use AI to suggest additional optimizations
        String aiOptimizationSuggestions = generateAIOptimizationSuggestions(activities, weatherData);
        log.info("AI optimization suggestions: {}", aiOptimizationSuggestions);
        
        // Apply AI suggestions
        applyAIOptimizationSuggestions(optimizedActivities, aiOptimizationSuggestions);
        
        return optimizedActivities;
    }
    
    private void optimizeOutdoorActivities(List<Activity> outdoorActivities, Map<Integer, WeatherData> hourlyWeather) {
        for (Activity activity : outdoorActivities) {
            OptimalTimeSlot optimalSlot = findOptimalTimeSlotForOutdoorActivity(activity, hourlyWeather);
            
            if (optimalSlot != null) {
                activity.setStartTime(optimalSlot.startTime);
                activity.setEndTime(optimalSlot.endTime);
                activity.setTimeSlot(optimalSlot.timeSlot);
                activity.setOptimizationReason(optimalSlot.reason);
                
                log.info("Optimized outdoor activity '{}' to time slot: {} - {}", 
                        activity.getName(), optimalSlot.startTime, optimalSlot.endTime);
            }
        }
    }
    
    private void optimizeIndoorActivities(List<Activity> indoorActivities, Map<Integer, WeatherData> hourlyWeather) {
        for (Activity activity : indoorActivities) {
            OptimalTimeSlot optimalSlot = findOptimalTimeSlotForIndoorActivity(activity, hourlyWeather);
            
            if (optimalSlot != null) {
                activity.setStartTime(optimalSlot.startTime);
                activity.setEndTime(optimalSlot.endTime);
                activity.setTimeSlot(optimalSlot.timeSlot);
                activity.setOptimizationReason(optimalSlot.reason);
                
                log.info("Optimized indoor activity '{}' to time slot: {} - {}", 
                        activity.getName(), optimalSlot.startTime, optimalSlot.endTime);
            }
        }
    }
    
    private OptimalTimeSlot findOptimalTimeSlotForOutdoorActivity(Activity activity, Map<Integer, WeatherData> hourlyWeather) {
        List<TimeSlotScore> scores = new ArrayList<>();
        
        // Morning slot (8-12)
        TimeSlotScore morningScore = calculateTimeSlotScore(8, 12, hourlyWeather, true);
        morningScore.timeSlot = Activity.TimeSlot.MORNING;
        scores.add(morningScore);
        
        // Afternoon slot (12-17)
        TimeSlotScore afternoonScore = calculateTimeSlotScore(12, 17, hourlyWeather, true);
        afternoonScore.timeSlot = Activity.TimeSlot.AFTERNOON;
        scores.add(afternoonScore);
        
        // Evening slot (17-20)
        TimeSlotScore eveningScore = calculateTimeSlotScore(17, 20, hourlyWeather, true);
        eveningScore.timeSlot = Activity.TimeSlot.EVENING;
        scores.add(eveningScore);
        
        // Find the best slot
        Optional<TimeSlotScore> bestSlot = scores.stream()
                .max(Comparator.comparing(s -> s.score));
        
        if (bestSlot.isPresent() && bestSlot.get().score > 50) {
            TimeSlotScore best = bestSlot.get();
            return new OptimalTimeSlot(
                    LocalTime.of(best.startHour, 0),
                    LocalTime.of(best.endHour, 0),
                    best.timeSlot,
                    String.format("AI optimized for outdoor conditions (score: %.1f)", best.score)
            );
        }
        
        return null;
    }
    
    private OptimalTimeSlot findOptimalTimeSlotForIndoorActivity(Activity activity, Map<Integer, WeatherData> hourlyWeather) {
        List<TimeSlotScore> scores = new ArrayList<>();
        
        // Morning slot (8-12)
        TimeSlotScore morningScore = calculateTimeSlotScore(8, 12, hourlyWeather, false);
        morningScore.timeSlot = Activity.TimeSlot.MORNING;
        scores.add(morningScore);
        
        // Afternoon slot (12-17)
        TimeSlotScore afternoonScore = calculateTimeSlotScore(12, 17, hourlyWeather, false);
        afternoonScore.timeSlot = Activity.TimeSlot.AFTERNOON;
        scores.add(afternoonScore);
        
        // Evening slot (17-20)
        TimeSlotScore eveningScore = calculateTimeSlotScore(17, 20, hourlyWeather, false);
        eveningScore.timeSlot = Activity.TimeSlot.EVENING;
        scores.add(eveningScore);
        
        // Find the best slot (for indoor activities, we prefer times when outdoor conditions are poor)
        Optional<TimeSlotScore> bestSlot = scores.stream()
                .max(Comparator.comparing(s -> s.score));
        
        if (bestSlot.isPresent() && bestSlot.get().score > 50) {
            TimeSlotScore best = bestSlot.get();
            return new OptimalTimeSlot(
                    LocalTime.of(best.startHour, 0),
                    LocalTime.of(best.endHour, 0),
                    best.timeSlot,
                    String.format("AI optimized for indoor conditions (score: %.1f)", best.score)
            );
        }
        
        return null;
    }
    
    private TimeSlotScore calculateTimeSlotScore(int startHour, int endHour, Map<Integer, WeatherData> hourlyWeather, boolean isOutdoor) {
        TimeSlotScore score = new TimeSlotScore();
        score.startHour = startHour;
        score.endHour = endHour;
        score.score = 0;
        
        double totalScore = 0;
        int hourCount = 0;
        
        for (int hour = startHour; hour < endHour; hour++) {
            WeatherData weather = hourlyWeather.get(hour);
            if (weather != null) {
                if (isOutdoor) {
                    totalScore += weather.getOutdoorActivityScore();
                } else {
                    totalScore += weather.getIndoorActivityScore();
                }
                hourCount++;
            }
        }
        
        if (hourCount > 0) {
            score.score = totalScore / hourCount;
        }
        
        return score;
    }
    
    private String generateAIOptimizationSuggestions(List<Activity> activities, List<WeatherData> weatherData) {
        PromptTemplate promptTemplate = new PromptTemplate("""
                You are an expert travel planner specializing in weather-based activity optimization for Italy.
                
                Given the following activities and weather forecast, provide specific recommendations for optimizing the schedule:
                
                Activities:
                {activities}
                
                Weather Forecast:
                {weather}
                
                Please provide:
                1. Specific time recommendations for each activity
                2. Any activities that should be rescheduled due to weather
                3. Alternative indoor activities if weather is poor
                4. Best times for outdoor activities based on temperature, humidity, and rain
                5. Any safety considerations
                
                Format your response as actionable recommendations with reasons.
                """);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("activities", formatActivitiesForAI(activities));
        variables.put("weather", formatWeatherForAI(weatherData));
        
        Prompt prompt = promptTemplate.create(variables);
        
        try {
            return chatClient.call(prompt).getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("Error generating AI optimization suggestions: {}", e.getMessage());
            return "AI optimization temporarily unavailable.";
        }
    }
    
    private void applyAIOptimizationSuggestions(List<Activity> activities, String aiSuggestions) {
        // Parse AI suggestions and apply them
        // This is a simplified implementation - in production, you'd want more sophisticated parsing
        
        for (Activity activity : activities) {
            if (aiSuggestions.toLowerCase().contains(activity.getName().toLowerCase())) {
                if (aiSuggestions.toLowerCase().contains("morning")) {
                    activity.setTimeSlot(Activity.TimeSlot.MORNING);
                    activity.setStartTime(LocalTime.of(9, 0));
                    activity.setEndTime(LocalTime.of(12, 0));
                } else if (aiSuggestions.toLowerCase().contains("afternoon")) {
                    activity.setTimeSlot(Activity.TimeSlot.AFTERNOON);
                    activity.setStartTime(LocalTime.of(14, 0));
                    activity.setEndTime(LocalTime.of(17, 0));
                } else if (aiSuggestions.toLowerCase().contains("evening")) {
                    activity.setTimeSlot(Activity.TimeSlot.EVENING);
                    activity.setStartTime(LocalTime.of(18, 0));
                    activity.setEndTime(LocalTime.of(21, 0));
                }
                
                if (activity.getOptimizationReason() == null) {
                    activity.setOptimizationReason("AI suggested optimization based on weather forecast");
                }
            }
        }
    }
    
    private String formatActivitiesForAI(List<Activity> activities) {
        return activities.stream()
                .map(activity -> String.format("- %s (%s) at %s - Type: %s, Time: %s",
                        activity.getName(),
                        activity.getDescription() != null ? activity.getDescription() : "No description",
                        activity.getLocation(),
                        activity.getType(),
                        activity.getTimeSlot()))
                .collect(Collectors.joining("\n"));
    }
    
    private String formatWeatherForAI(List<WeatherData> weatherData) {
        return weatherData.stream()
                .map(wd -> String.format("Hour %d: %s, %.1f°C, %d%% humidity, %s",
                        wd.getForecastHour(),
                        wd.getWeatherDescription(),
                        wd.getTemperatureCelsius(),
                        wd.getHumidityPercent(),
                        wd.getRain1hMm() != null && wd.getRain1hMm() > 0 ? "Rain" : "No rain"))
                .collect(Collectors.joining("\n"));
    }
    
    private boolean isIndoorActivity(Activity activity) {
        return activity.getType() == Activity.ActivityType.MUSEUM ||
               activity.getType() == Activity.ActivityType.SHOPPING ||
               activity.getType() == Activity.ActivityType.RESTAURANT ||
               activity.getType() == Activity.ActivityType.ENTERTAINMENT ||
               activity.getType() == Activity.ActivityType.CULTURAL ||
               activity.getType() == Activity.ActivityType.NIGHTLIFE ||
               (activity.getWeatherDependent() != null && !activity.getWeatherDependent());
    }
    
    private boolean isOutdoorActivity(Activity activity) {
        return activity.getType() == Activity.ActivityType.SIGHTSEEING ||
               activity.getType() == Activity.ActivityType.OUTDOOR_ACTIVITY ||
               activity.getType() == Activity.ActivityType.NATURE ||
               activity.getType() == Activity.ActivityType.SPORTS ||
               activity.getType() == Activity.ActivityType.ADVENTURE ||
               activity.getType() == Activity.ActivityType.WATER_ACTIVITY ||
               activity.getType() == Activity.ActivityType.MOUNTAIN_ACTIVITY ||
               activity.getType() == Activity.ActivityType.CITY_TOUR ||
               (activity.getWeatherDependent() != null && activity.getWeatherDependent());
    }
    
    public List<Activity> suggestAlternativeActivities(Activity originalActivity, List<WeatherData> weatherData) {
        List<Activity> alternatives = new ArrayList<>();
        
        // Use AI to suggest alternatives based on weather
        String aiSuggestions = generateAlternativeActivitySuggestions(originalActivity, weatherData);
        
        // Parse AI suggestions and create alternative activities
        // This is a simplified implementation
        String[] suggestions = aiSuggestions.split("\n");
        
        for (String suggestion : suggestions) {
            if (suggestion.trim().startsWith("-")) {
                Activity alternative = new Activity();
                alternative.setName(suggestion.trim().substring(1).trim());
                alternative.setDate(originalActivity.getDate());
                alternative.setLocation(originalActivity.getLocation());
                alternative.setHolidayPlan(originalActivity.getHolidayPlan());
                alternative.setType(Activity.ActivityType.OTHER);
                alternative.setOptimizationReason("AI suggested alternative due to weather conditions");
                alternatives.add(alternative);
            }
        }
        
        return alternatives;
    }
    
    private String generateAlternativeActivitySuggestions(Activity activity, List<WeatherData> weatherData) {
        PromptTemplate promptTemplate = new PromptTemplate("""
                Given the following activity and weather conditions, suggest 3-5 alternative activities suitable for the weather:
                
                Original Activity: {activity}
                Weather Conditions: {weather}
                
                Provide specific alternative activities that would be suitable for the weather conditions.
                Format each suggestion as a bullet point starting with "-".
                """);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("activity", String.format("%s - %s at %s", 
                activity.getName(), activity.getType(), activity.getLocation()));
        variables.put("weather", formatWeatherForAI(weatherData));
        
        Prompt prompt = promptTemplate.create(variables);
        
        try {
            return chatClient.call(prompt).getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("Error generating alternative activity suggestions: {}", e.getMessage());
            return "Alternative activity suggestions temporarily unavailable.";
        }
    }
    
    private static class TimeSlotScore {
        int startHour;
        int endHour;
        double score;
        Activity.TimeSlot timeSlot;
    }
    
    private static class OptimalTimeSlot {
        LocalTime startTime;
        LocalTime endTime;
        Activity.TimeSlot timeSlot;
        String reason;
        
        OptimalTimeSlot(LocalTime startTime, LocalTime endTime, Activity.TimeSlot timeSlot, String reason) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.timeSlot = timeSlot;
            this.reason = reason;
        }
    }
}