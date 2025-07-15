package com.holidayplanner.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class WeatherData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String city;
    
    @Column(nullable = false)
    private String country;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(name = "forecast_hour")
    private Integer forecastHour; // 0-23 for hourly forecasts
    
    @Column(name = "temperature_celsius")
    private Double temperatureCelsius;
    
    @Column(name = "feels_like_celsius")
    private Double feelsLikeCelsius;
    
    @Column(name = "temperature_min")
    private Double temperatureMin;
    
    @Column(name = "temperature_max")
    private Double temperatureMax;
    
    @Column(name = "humidity_percent")
    private Integer humidityPercent;
    
    @Column(name = "pressure_hpa")
    private Double pressureHpa;
    
    @Column(name = "wind_speed_mps")
    private Double windSpeedMps;
    
    @Column(name = "wind_direction_degrees")
    private Integer windDirectionDegrees;
    
    @Column(name = "cloudiness_percent")
    private Integer cloudinessPercent;
    
    @Column(name = "visibility_meters")
    private Integer visibilityMeters;
    
    @Column(name = "uv_index")
    private Double uvIndex;
    
    @Column(name = "weather_main")
    private String weatherMain;
    
    @Column(name = "weather_description")
    private String weatherDescription;
    
    @Column(name = "weather_icon")
    private String weatherIcon;
    
    @Column(name = "rain_1h_mm")
    private Double rain1hMm;
    
    @Column(name = "rain_3h_mm")
    private Double rain3hMm;
    
    @Column(name = "snow_1h_mm")
    private Double snow1hMm;
    
    @Column(name = "snow_3h_mm")
    private Double snow3hMm;
    
    @Column(name = "sunrise_time")
    private LocalDateTime sunriseTime;
    
    @Column(name = "sunset_time")
    private LocalDateTime sunsetTime;
    
    @Column(name = "air_quality_index")
    private Integer airQualityIndex;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "forecast_type")
    private ForecastType forecastType;
    
    @Column(name = "data_source")
    private String dataSource = "OpenWeatherMap";
    
    @Column(name = "ai_comfort_score")
    private Double aiComfortScore; // AI-calculated comfort score 0-100
    
    @Column(name = "outdoor_activity_score")
    private Double outdoorActivityScore; // AI-calculated suitability for outdoor activities 0-100
    
    @Column(name = "indoor_activity_score")
    private Double indoorActivityScore; // AI-calculated suitability for indoor activities 0-100
    
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    public enum ForecastType {
        CURRENT,
        HOURLY,
        DAILY,
        HISTORICAL
    }
}