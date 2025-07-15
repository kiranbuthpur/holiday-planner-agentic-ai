package com.holidayplanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.holidayplanner.model.WeatherData;
import com.holidayplanner.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {
    
    private final WeatherDataRepository weatherDataRepository;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${weather.api.key}")
    private String apiKey;
    
    @Value("${weather.api.base-url}")
    private String baseUrl;
    
    @Value("${weather.api.forecast-url}")
    private String forecastUrl;
    
    public WeatherData getCurrentWeather(String city, String country) {
        try {
            WebClient webClient = webClientBuilder.build();
            
            String url = String.format("%s/weather?q=%s,%s&appid=%s&units=metric",
                    baseUrl, city, country, apiKey);
            
            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            
            if (response != null) {
                WeatherData weatherData = parseCurrentWeatherResponse(response, city, country);
                return saveWeatherData(weatherData);
            }
            
        } catch (WebClientResponseException e) {
            log.error("Error fetching current weather for {}, {}: {}", city, country, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error fetching current weather: {}", e.getMessage());
        }
        
        return null;
    }
    
    public List<WeatherData> getWeatherForecast(String city, String country, int days) {
        try {
            WebClient webClient = webClientBuilder.build();
            
            String url = String.format("%s?q=%s,%s&appid=%s&units=metric&cnt=%d",
                    forecastUrl, city, country, apiKey, days * 8); // 8 forecasts per day (3-hour intervals)
            
            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            
            if (response != null) {
                List<WeatherData> forecasts = parseForecastResponse(response, city, country);
                return forecasts.stream()
                        .map(this::saveWeatherData)
                        .toList();
            }
            
        } catch (WebClientResponseException e) {
            log.error("Error fetching weather forecast for {}, {}: {}", city, country, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error fetching weather forecast: {}", e.getMessage());
        }
        
        return List.of();
    }
    
    public List<WeatherData> getWeatherForDateRange(String city, String country, LocalDate startDate, LocalDate endDate) {
        // First check if we have cached data
        List<WeatherData> cachedData = weatherDataRepository
                .findByCityAndCountryAndDateBetweenOrderByDateAscForecastHourAsc(city, country, startDate, endDate);
        
        if (!cachedData.isEmpty()) {
            log.info("Returning cached weather data for {}, {} from {} to {}", city, country, startDate, endDate);
            return cachedData;
        }
        
        // If no cached data, fetch from API
        int days = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
        return getWeatherForecast(city, country, Math.min(days, 5)); // OpenWeather free tier supports 5 days
    }
    
    public List<WeatherData> getOptimalWeatherForOutdoorActivities(String city, String country, LocalDate date, Double maxTemp, Integer maxHumidity) {
        return weatherDataRepository.findOptimalWeatherForOutdoorActivities(city, country, date, maxTemp, maxHumidity);
    }
    
    public List<WeatherData> getOptimalWeatherForIndoorActivities(String city, String country, LocalDate date, Double minTemp) {
        return weatherDataRepository.findOptimalWeatherForIndoorActivities(city, country, date, minTemp);
    }
    
    public List<WeatherData> getBestHoursForOutdoorActivities(String city, String country, LocalDate date, Double minScore) {
        return weatherDataRepository.findBestHoursForOutdoorActivities(city, country, date, minScore);
    }
    
    public List<WeatherData> getBestHoursForIndoorActivities(String city, String country, LocalDate date, Double minScore) {
        return weatherDataRepository.findBestHoursForIndoorActivities(city, country, date, minScore);
    }
    
    public Double getAverageTemperatureForDay(String city, String country, LocalDate date) {
        return weatherDataRepository.getAverageTemperatureForDay(city, country, date);
    }
    
    public boolean isRainyDay(String city, String country, LocalDate date) {
        List<WeatherData> rainyHours = weatherDataRepository.findRainyHours(city, country, date);
        return !rainyHours.isEmpty();
    }
    
    private WeatherData parseCurrentWeatherResponse(JsonNode response, String city, String country) {
        WeatherData weatherData = new WeatherData();
        weatherData.setCity(city);
        weatherData.setCountry(country);
        weatherData.setDate(LocalDate.now());
        weatherData.setForecastType(WeatherData.ForecastType.CURRENT);
        
        JsonNode main = response.get("main");
        if (main != null) {
            weatherData.setTemperatureCelsius(main.get("temp").asDouble());
            weatherData.setFeelsLikeCelsius(main.get("feels_like").asDouble());
            weatherData.setHumidityPercent(main.get("humidity").asInt());
            weatherData.setPressureHpa(main.get("pressure").asDouble());
            weatherData.setTemperatureMin(main.get("temp_min").asDouble());
            weatherData.setTemperatureMax(main.get("temp_max").asDouble());
        }
        
        JsonNode weather = response.get("weather");
        if (weather != null && weather.isArray() && weather.size() > 0) {
            JsonNode weatherInfo = weather.get(0);
            weatherData.setWeatherMain(weatherInfo.get("main").asText());
            weatherData.setWeatherDescription(weatherInfo.get("description").asText());
            weatherData.setWeatherIcon(weatherInfo.get("icon").asText());
        }
        
        JsonNode wind = response.get("wind");
        if (wind != null) {
            weatherData.setWindSpeedMps(wind.get("speed").asDouble());
            if (wind.has("deg")) {
                weatherData.setWindDirectionDegrees(wind.get("deg").asInt());
            }
        }
        
        JsonNode clouds = response.get("clouds");
        if (clouds != null) {
            weatherData.setCloudinessPercent(clouds.get("all").asInt());
        }
        
        JsonNode visibility = response.get("visibility");
        if (visibility != null) {
            weatherData.setVisibilityMeters(visibility.asInt());
        }
        
        JsonNode rain = response.get("rain");
        if (rain != null) {
            if (rain.has("1h")) {
                weatherData.setRain1hMm(rain.get("1h").asDouble());
            }
            if (rain.has("3h")) {
                weatherData.setRain3hMm(rain.get("3h").asDouble());
            }
        }
        
        JsonNode snow = response.get("snow");
        if (snow != null) {
            if (snow.has("1h")) {
                weatherData.setSnow1hMm(snow.get("1h").asDouble());
            }
            if (snow.has("3h")) {
                weatherData.setSnow3hMm(snow.get("3h").asDouble());
            }
        }
        
        JsonNode sys = response.get("sys");
        if (sys != null) {
            if (sys.has("sunrise")) {
                weatherData.setSunriseTime(LocalDateTime.ofEpochSecond(sys.get("sunrise").asLong(), 0, ZoneOffset.UTC));
            }
            if (sys.has("sunset")) {
                weatherData.setSunsetTime(LocalDateTime.ofEpochSecond(sys.get("sunset").asLong(), 0, ZoneOffset.UTC));
            }
        }
        
        // Calculate AI comfort scores
        calculateComfortScores(weatherData);
        
        return weatherData;
    }
    
    private List<WeatherData> parseForecastResponse(JsonNode response, String city, String country) {
        return response.get("list").findValues("dt").stream()
                .map(forecast -> {
                    WeatherData weatherData = new WeatherData();
                    weatherData.setCity(city);
                    weatherData.setCountry(country);
                    weatherData.setForecastType(WeatherData.ForecastType.HOURLY);
                    
                    long timestamp = forecast.asLong();
                    LocalDateTime dateTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC);
                    weatherData.setDate(dateTime.toLocalDate());
                    weatherData.setForecastHour(dateTime.getHour());
                    
                    JsonNode parent = forecast.findParent();
                    
                    JsonNode main = parent.get("main");
                    if (main != null) {
                        weatherData.setTemperatureCelsius(main.get("temp").asDouble());
                        weatherData.setFeelsLikeCelsius(main.get("feels_like").asDouble());
                        weatherData.setHumidityPercent(main.get("humidity").asInt());
                        weatherData.setPressureHpa(main.get("pressure").asDouble());
                        weatherData.setTemperatureMin(main.get("temp_min").asDouble());
                        weatherData.setTemperatureMax(main.get("temp_max").asDouble());
                    }
                    
                    JsonNode weather = parent.get("weather");
                    if (weather != null && weather.isArray() && weather.size() > 0) {
                        JsonNode weatherInfo = weather.get(0);
                        weatherData.setWeatherMain(weatherInfo.get("main").asText());
                        weatherData.setWeatherDescription(weatherInfo.get("description").asText());
                        weatherData.setWeatherIcon(weatherInfo.get("icon").asText());
                    }
                    
                    JsonNode wind = parent.get("wind");
                    if (wind != null) {
                        weatherData.setWindSpeedMps(wind.get("speed").asDouble());
                        if (wind.has("deg")) {
                            weatherData.setWindDirectionDegrees(wind.get("deg").asInt());
                        }
                    }
                    
                    JsonNode clouds = parent.get("clouds");
                    if (clouds != null) {
                        weatherData.setCloudinessPercent(clouds.get("all").asInt());
                    }
                    
                    JsonNode visibility = parent.get("visibility");
                    if (visibility != null) {
                        weatherData.setVisibilityMeters(visibility.asInt());
                    }
                    
                    JsonNode rain = parent.get("rain");
                    if (rain != null && rain.has("3h")) {
                        weatherData.setRain3hMm(rain.get("3h").asDouble());
                    }
                    
                    JsonNode snow = parent.get("snow");
                    if (snow != null && snow.has("3h")) {
                        weatherData.setSnow3hMm(snow.get("3h").asDouble());
                    }
                    
                    // Calculate AI comfort scores
                    calculateComfortScores(weatherData);
                    
                    return weatherData;
                })
                .toList();
    }
    
    private void calculateComfortScores(WeatherData weatherData) {
        double temp = weatherData.getTemperatureCelsius();
        int humidity = weatherData.getHumidityPercent();
        double windSpeed = weatherData.getWindSpeedMps();
        boolean isRaining = weatherData.getRain1hMm() != null && weatherData.getRain1hMm() > 0;
        
        // Calculate outdoor activity score (0-100)
        double outdoorScore = 100.0;
        
        // Temperature factor
        if (temp < 10 || temp > 30) {
            outdoorScore -= 30;
        } else if (temp < 15 || temp > 25) {
            outdoorScore -= 15;
        }
        
        // Humidity factor
        if (humidity > 80) {
            outdoorScore -= 20;
        } else if (humidity > 70) {
            outdoorScore -= 10;
        }
        
        // Wind factor
        if (windSpeed > 10) {
            outdoorScore -= 15;
        } else if (windSpeed > 7) {
            outdoorScore -= 5;
        }
        
        // Rain factor
        if (isRaining) {
            outdoorScore -= 40;
        }
        
        weatherData.setOutdoorActivityScore(Math.max(0, outdoorScore));
        
        // Calculate indoor activity score (inverse of outdoor factors)
        double indoorScore = 100.0;
        
        // Indoor activities are better when it's too hot/cold/rainy outside
        if (temp > 30 || temp < 15 || isRaining) {
            indoorScore += 20;
        }
        
        // Indoor activities are good during high humidity
        if (humidity > 70) {
            indoorScore += 10;
        }
        
        weatherData.setIndoorActivityScore(Math.min(100, indoorScore));
        
        // Calculate general comfort score
        double comfortScore = (outdoorScore + indoorScore) / 2;
        weatherData.setAiComfortScore(comfortScore);
    }
    
    private WeatherData saveWeatherData(WeatherData weatherData) {
        try {
            // Check if data already exists
            Optional<WeatherData> existing = weatherDataRepository
                    .findByCityAndCountryAndDateAndForecastHour(
                            weatherData.getCity(),
                            weatherData.getCountry(),
                            weatherData.getDate(),
                            weatherData.getForecastHour()
                    );
            
            if (existing.isPresent()) {
                // Update existing data
                WeatherData existingData = existing.get();
                updateWeatherData(existingData, weatherData);
                return weatherDataRepository.save(existingData);
            } else {
                // Save new data
                return weatherDataRepository.save(weatherData);
            }
        } catch (Exception e) {
            log.error("Error saving weather data: {}", e.getMessage());
            return weatherData;
        }
    }
    
    private void updateWeatherData(WeatherData existing, WeatherData newData) {
        existing.setTemperatureCelsius(newData.getTemperatureCelsius());
        existing.setFeelsLikeCelsius(newData.getFeelsLikeCelsius());
        existing.setHumidityPercent(newData.getHumidityPercent());
        existing.setPressureHpa(newData.getPressureHpa());
        existing.setWindSpeedMps(newData.getWindSpeedMps());
        existing.setWindDirectionDegrees(newData.getWindDirectionDegrees());
        existing.setCloudinessPercent(newData.getCloudinessPercent());
        existing.setVisibilityMeters(newData.getVisibilityMeters());
        existing.setWeatherMain(newData.getWeatherMain());
        existing.setWeatherDescription(newData.getWeatherDescription());
        existing.setWeatherIcon(newData.getWeatherIcon());
        existing.setRain1hMm(newData.getRain1hMm());
        existing.setRain3hMm(newData.getRain3hMm());
        existing.setSnow1hMm(newData.getSnow1hMm());
        existing.setSnow3hMm(newData.getSnow3hMm());
        existing.setAiComfortScore(newData.getAiComfortScore());
        existing.setOutdoorActivityScore(newData.getOutdoorActivityScore());
        existing.setIndoorActivityScore(newData.getIndoorActivityScore());
    }
    
    public void cleanupOldWeatherData() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        weatherDataRepository.deleteOldWeatherData(cutoffDate);
        log.info("Cleaned up weather data older than {}", cutoffDate);
    }
}