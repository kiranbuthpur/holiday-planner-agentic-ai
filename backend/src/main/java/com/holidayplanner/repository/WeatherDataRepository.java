package com.holidayplanner.repository;

import com.holidayplanner.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {
    
    Optional<WeatherData> findByCityAndCountryAndDateAndForecastHour(String city, String country, LocalDate date, Integer forecastHour);
    
    List<WeatherData> findByCityAndCountryAndDateBetweenOrderByDateAscForecastHourAsc(String city, String country, LocalDate startDate, LocalDate endDate);
    
    List<WeatherData> findByCityAndCountryAndDateOrderByForecastHourAsc(String city, String country, LocalDate date);
    
    @Query("SELECT wd FROM WeatherData wd WHERE wd.city = :city AND wd.country = :country AND wd.date = :date AND wd.forecastType = :forecastType")
    List<WeatherData> findByCityAndCountryAndDateAndForecastType(@Param("city") String city,
                                                                @Param("country") String country,
                                                                @Param("date") LocalDate date,
                                                                @Param("forecastType") WeatherData.ForecastType forecastType);
    
    @Query("SELECT wd FROM WeatherData wd WHERE wd.city = :city AND wd.country = :country AND wd.date >= :startDate AND wd.date <= :endDate AND wd.forecastType = 'DAILY'")
    List<WeatherData> findDailyWeatherForPeriod(@Param("city") String city,
                                               @Param("country") String country,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
    
    @Query("SELECT wd FROM WeatherData wd WHERE wd.city = :city AND wd.country = :country AND wd.date = :date AND wd.forecastType = 'HOURLY' AND wd.forecastHour BETWEEN :startHour AND :endHour")
    List<WeatherData> findHourlyWeatherForTimeRange(@Param("city") String city,
                                                   @Param("country") String country,
                                                   @Param("date") LocalDate date,
                                                   @Param("startHour") Integer startHour,
                                                   @Param("endHour") Integer endHour);
    
    @Query("SELECT wd FROM WeatherData wd WHERE wd.city = :city AND wd.country = :country AND wd.date = :date AND wd.forecastType = 'HOURLY' AND wd.temperatureCelsius <= :maxTemp AND wd.humidityPercent <= :maxHumidity")
    List<WeatherData> findOptimalWeatherForOutdoorActivities(@Param("city") String city,
                                                            @Param("country") String country,
                                                            @Param("date") LocalDate date,
                                                            @Param("maxTemp") Double maxTemp,
                                                            @Param("maxHumidity") Integer maxHumidity);
    
    @Query("SELECT wd FROM WeatherData wd WHERE wd.city = :city AND wd.country = :country AND wd.date = :date AND wd.forecastType = 'HOURLY' AND (wd.temperatureCelsius >= :minTemp OR wd.rain1hMm > 0)")
    List<WeatherData> findOptimalWeatherForIndoorActivities(@Param("city") String city,
                                                           @Param("country") String country,
                                                           @Param("date") LocalDate date,
                                                           @Param("minTemp") Double minTemp);
    
    @Query("SELECT wd FROM WeatherData wd WHERE wd.city = :city AND wd.country = :country AND wd.date = :date AND wd.forecastType = 'HOURLY' AND wd.rain1hMm > 0")
    List<WeatherData> findRainyHours(@Param("city") String city,
                                   @Param("country") String country,
                                   @Param("date") LocalDate date);
    
    @Query("SELECT AVG(wd.temperatureCelsius) FROM WeatherData wd WHERE wd.city = :city AND wd.country = :country AND wd.date = :date AND wd.forecastType = 'HOURLY'")
    Double getAverageTemperatureForDay(@Param("city") String city,
                                      @Param("country") String country,
                                      @Param("date") LocalDate date);
    
    @Query("SELECT wd FROM WeatherData wd WHERE wd.city = :city AND wd.country = :country AND wd.date = :date AND wd.forecastType = 'HOURLY' AND wd.outdoorActivityScore >= :minScore ORDER BY wd.outdoorActivityScore DESC")
    List<WeatherData> findBestHoursForOutdoorActivities(@Param("city") String city,
                                                       @Param("country") String country,
                                                       @Param("date") LocalDate date,
                                                       @Param("minScore") Double minScore);
    
    @Query("SELECT wd FROM WeatherData wd WHERE wd.city = :city AND wd.country = :country AND wd.date = :date AND wd.forecastType = 'HOURLY' AND wd.indoorActivityScore >= :minScore ORDER BY wd.indoorActivityScore DESC")
    List<WeatherData> findBestHoursForIndoorActivities(@Param("city") String city,
                                                      @Param("country") String country,
                                                      @Param("date") LocalDate date,
                                                      @Param("minScore") Double minScore);
    
    @Query("DELETE FROM WeatherData wd WHERE wd.createdDate < :cutoffDate")
    void deleteOldWeatherData(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT DISTINCT wd.city FROM WeatherData wd WHERE wd.country = :country")
    List<String> findDistinctCitiesByCountry(@Param("country") String country);
}