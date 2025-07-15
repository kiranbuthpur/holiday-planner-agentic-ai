package com.holidayplanner.controller;

import com.holidayplanner.model.Activity;
import com.holidayplanner.model.HolidayPlan;
import com.holidayplanner.service.ActivityOptimizationService;
import com.holidayplanner.service.EmailService;
import com.holidayplanner.service.HolidayPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${cors.allowed-origins}")
public class HolidayPlanController {
    
    private final HolidayPlanService holidayPlanService;
    private final ActivityOptimizationService activityOptimizationService;
    private final EmailService emailService;
    
    @GetMapping
    public ResponseEntity<Page<HolidayPlan>> getAllHolidayPlans(
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) HolidayPlan.PlanStatus status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable) {
        
        log.info("Fetching holiday plans with filters - user: {}, destination: {}, status: {}", 
                userEmail, destination, status);
        
        Page<HolidayPlan> plans = holidayPlanService.findWithFilters(
                userEmail, destination, status, startDate, endDate, pageable);
        
        return ResponseEntity.ok(plans);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<HolidayPlan> getHolidayPlan(@PathVariable Long id) {
        log.info("Fetching holiday plan with id: {}", id);
        
        HolidayPlan plan = holidayPlanService.findById(id);
        return ResponseEntity.ok(plan);
    }
    
    @PostMapping
    public ResponseEntity<HolidayPlan> createHolidayPlan(@Valid @RequestBody HolidayPlan holidayPlan) {
        log.info("Creating new holiday plan: {}", holidayPlan.getTitle());
        
        HolidayPlan createdPlan = holidayPlanService.createHolidayPlan(holidayPlan);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlan);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<HolidayPlan> updateHolidayPlan(
            @PathVariable Long id,
            @Valid @RequestBody HolidayPlan holidayPlan) {
        
        log.info("Updating holiday plan with id: {}", id);
        
        HolidayPlan updatedPlan = holidayPlanService.updateHolidayPlan(id, holidayPlan);
        return ResponseEntity.ok(updatedPlan);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHolidayPlan(@PathVariable Long id) {
        log.info("Deleting holiday plan with id: {}", id);
        
        holidayPlanService.deleteHolidayPlan(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HolidayPlan> uploadExcelFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userEmail") String userEmail,
            @RequestParam("destination") String destination) {
        
        log.info("Uploading Excel file for user: {}, destination: {}", userEmail, destination);
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            HolidayPlan plan = holidayPlanService.createHolidayPlanFromExcel(file, userEmail, destination);
            return ResponseEntity.status(HttpStatus.CREATED).body(plan);
        } catch (Exception e) {
            log.error("Error processing Excel file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/{id}/optimize")
    public ResponseEntity<Map<String, Object>> optimizeHolidayPlan(@PathVariable Long id) {
        log.info("Optimizing holiday plan with id: {}", id);
        
        try {
            HolidayPlan plan = holidayPlanService.findById(id);
            List<Activity> optimizedActivities = activityOptimizationService.optimizeActivitiesForWeather(plan);
            
            // Send optimization email
            emailService.sendHolidayPlanOptimizationEmail(plan.getUserEmail(), plan, optimizedActivities);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Holiday plan optimized successfully",
                    "optimizedActivities", optimizedActivities.size(),
                    "planId", id
            ));
        } catch (Exception e) {
            log.error("Error optimizing holiday plan: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to optimize holiday plan"));
        }
    }
    
    @GetMapping("/{id}/activities")
    public ResponseEntity<List<Activity>> getHolidayPlanActivities(@PathVariable Long id) {
        log.info("Fetching activities for holiday plan: {}", id);
        
        List<Activity> activities = holidayPlanService.getActivitiesForPlan(id);
        return ResponseEntity.ok(activities);
    }
    
    @GetMapping("/{id}/activities/date/{date}")
    public ResponseEntity<List<Activity>> getActivitiesForDate(
            @PathVariable Long id,
            @PathVariable LocalDate date) {
        
        log.info("Fetching activities for holiday plan: {} on date: {}", id, date);
        
        List<Activity> activities = holidayPlanService.getActivitiesForDate(id, date);
        return ResponseEntity.ok(activities);
    }
    
    @PostMapping("/{id}/activities")
    public ResponseEntity<Activity> addActivity(
            @PathVariable Long id,
            @Valid @RequestBody Activity activity) {
        
        log.info("Adding activity to holiday plan: {}", id);
        
        Activity createdActivity = holidayPlanService.addActivity(id, activity);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdActivity);
    }
    
    @PutMapping("/{id}/activities/{activityId}")
    public ResponseEntity<Activity> updateActivity(
            @PathVariable Long id,
            @PathVariable Long activityId,
            @Valid @RequestBody Activity activity) {
        
        log.info("Updating activity {} in holiday plan: {}", activityId, id);
        
        Activity updatedActivity = holidayPlanService.updateActivity(id, activityId, activity);
        return ResponseEntity.ok(updatedActivity);
    }
    
    @DeleteMapping("/{id}/activities/{activityId}")
    public ResponseEntity<Void> deleteActivity(
            @PathVariable Long id,
            @PathVariable Long activityId) {
        
        log.info("Deleting activity {} from holiday plan: {}", activityId, id);
        
        holidayPlanService.deleteActivity(id, activityId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/statistics")
    public ResponseEntity<Map<String, Object>> getHolidayPlanStatistics(@PathVariable Long id) {
        log.info("Fetching statistics for holiday plan: {}", id);
        
        Map<String, Object> statistics = holidayPlanService.getHolidayPlanStatistics(id);
        return ResponseEntity.ok(statistics);
    }
    
    @PostMapping("/{id}/clone")
    public ResponseEntity<HolidayPlan> cloneHolidayPlan(
            @PathVariable Long id,
            @RequestParam String userEmail,
            @RequestParam(required = false) String newTitle) {
        
        log.info("Cloning holiday plan: {} for user: {}", id, userEmail);
        
        HolidayPlan clonedPlan = holidayPlanService.cloneHolidayPlan(id, userEmail, newTitle);
        return ResponseEntity.status(HttpStatus.CREATED).body(clonedPlan);
    }
    
    @PostMapping("/{id}/export")
    public ResponseEntity<Map<String, Object>> exportHolidayPlan(
            @PathVariable Long id,
            @RequestParam String format) {
        
        log.info("Exporting holiday plan: {} in format: {}", id, format);
        
        Map<String, Object> exportResult = holidayPlanService.exportHolidayPlan(id, format);
        return ResponseEntity.ok(exportResult);
    }
    
    @PostMapping("/{id}/share")
    public ResponseEntity<Map<String, Object>> shareHolidayPlan(
            @PathVariable Long id,
            @RequestParam String recipientEmail,
            @RequestParam(required = false) String message) {
        
        log.info("Sharing holiday plan: {} with: {}", id, recipientEmail);
        
        Map<String, Object> shareResult = holidayPlanService.shareHolidayPlan(id, recipientEmail, message);
        return ResponseEntity.ok(shareResult);
    }
    
    @PostMapping("/{id}/send-reminder")
    public ResponseEntity<Map<String, Object>> sendHolidayReminder(@PathVariable Long id) {
        log.info("Sending reminder for holiday plan: {}", id);
        
        try {
            HolidayPlan plan = holidayPlanService.findById(id);
            int daysUntilTrip = (int) (plan.getStartDate().toEpochDay() - LocalDate.now().toEpochDay());
            
            if (daysUntilTrip > 0) {
                emailService.sendHolidayReminderEmail(plan.getUserEmail(), plan, daysUntilTrip);
                return ResponseEntity.ok(Map.of(
                        "message", "Reminder sent successfully",
                        "daysUntilTrip", daysUntilTrip
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Holiday has already started or passed"
                ));
            }
        } catch (Exception e) {
            log.error("Error sending reminder: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send reminder"));
        }
    }
    
    @GetMapping("/user/{userEmail}/upcoming")
    public ResponseEntity<List<HolidayPlan>> getUpcomingHolidays(
            @PathVariable String userEmail,
            @RequestParam(defaultValue = "30") int days) {
        
        log.info("Fetching upcoming holidays for user: {} within {} days", userEmail, days);
        
        List<HolidayPlan> upcomingPlans = holidayPlanService.getUpcomingHolidaysForUser(userEmail, days);
        return ResponseEntity.ok(upcomingPlans);
    }
    
    @GetMapping("/destinations")
    public ResponseEntity<List<String>> getPopularDestinations() {
        log.info("Fetching popular destinations");
        
        List<String> destinations = holidayPlanService.getPopularDestinations();
        return ResponseEntity.ok(destinations);
    }
    
    @GetMapping("/user/{userEmail}/destinations")
    public ResponseEntity<List<String>> getUserDestinations(@PathVariable String userEmail) {
        log.info("Fetching destinations for user: {}", userEmail);
        
        List<String> destinations = holidayPlanService.getUserDestinations(userEmail);
        return ResponseEntity.ok(destinations);
    }
}