package com.holidayplanner.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Activity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(name = "start_time")
    private LocalTime startTime;
    
    @Column(name = "end_time")
    private LocalTime endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimeSlot timeSlot;
    
    @Column(nullable = false)
    private String location;
    
    @Column(name = "is_weather_dependent")
    private Boolean weatherDependent = false;
    
    @Column(name = "preferred_weather")
    private String preferredWeather;
    
    @Column(name = "min_temperature")
    private Double minTemperature;
    
    @Column(name = "max_temperature")
    private Double maxTemperature;
    
    @Column(name = "max_humidity")
    private Double maxHumidity;
    
    @Column(name = "avoid_rain")
    private Boolean avoidRain = false;
    
    @Column(name = "priority_level")
    private Integer priorityLevel = 5; // 1-10 scale
    
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;
    
    @Column(name = "cost_estimate")
    private Double costEstimate;
    
    @Column(name = "booking_required")
    private Boolean bookingRequired = false;
    
    @Column(name = "booking_url")
    private String bookingUrl;
    
    @Column(name = "contact_info")
    private String contactInfo;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "ai_optimized")
    private Boolean aiOptimized = false;
    
    @Column(name = "optimization_reason")
    private String optimizationReason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holiday_plan_id", nullable = false)
    private HolidayPlan holidayPlan;
    
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
    
    public enum ActivityType {
        SIGHTSEEING,
        MUSEUM,
        RESTAURANT,
        SHOPPING,
        OUTDOOR_ACTIVITY,
        ENTERTAINMENT,
        TRANSPORTATION,
        ACCOMMODATION,
        CULTURAL,
        SPORTS,
        RELAXATION,
        ADVENTURE,
        NIGHTLIFE,
        HISTORICAL,
        RELIGIOUS,
        NATURE,
        FOOD_EXPERIENCE,
        WATER_ACTIVITY,
        MOUNTAIN_ACTIVITY,
        CITY_TOUR,
        OTHER
    }
    
    public enum TimeSlot {
        MORNING,
        AFTERNOON,
        EVENING,
        NIGHT,
        FULL_DAY
    }
}