package com.holidayplanner.service;

import com.holidayplanner.model.Activity;
import com.holidayplanner.model.HolidayPlan;
import com.holidayplanner.repository.ActivityRepository;
import com.holidayplanner.repository.HolidayPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HolidayPlanService {
    
    private final HolidayPlanRepository holidayPlanRepository;
    private final ActivityRepository activityRepository;
    private final EmailService emailService;
    
    public Page<HolidayPlan> findWithFilters(String userEmail, String destination, HolidayPlan.PlanStatus status, 
                                           LocalDate startDate, LocalDate endDate, Pageable pageable) {
        // For now, return all plans - in production, implement filtering
        return holidayPlanRepository.findAll(pageable);
    }
    
    public HolidayPlan findById(Long id) {
        return holidayPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday plan not found with id: " + id));
    }
    
    @Transactional
    public HolidayPlan createHolidayPlan(HolidayPlan holidayPlan) {
        validateHolidayPlan(holidayPlan);
        
        holidayPlan.setStatus(HolidayPlan.PlanStatus.DRAFT);
        holidayPlan = holidayPlanRepository.save(holidayPlan);
        
        log.info("Created holiday plan: {} for user: {}", holidayPlan.getId(), holidayPlan.getUserEmail());
        return holidayPlan;
    }
    
    @Transactional
    public HolidayPlan updateHolidayPlan(Long id, HolidayPlan updatedPlan) {
        HolidayPlan existingPlan = findById(id);
        
        existingPlan.setTitle(updatedPlan.getTitle());
        existingPlan.setDestination(updatedPlan.getDestination());
        existingPlan.setStartDate(updatedPlan.getStartDate());
        existingPlan.setEndDate(updatedPlan.getEndDate());
        existingPlan.setNotes(updatedPlan.getNotes());
        existingPlan.setWeatherOptimizationEnabled(updatedPlan.getWeatherOptimizationEnabled());
        existingPlan.setStatus(updatedPlan.getStatus());
        
        return holidayPlanRepository.save(existingPlan);
    }
    
    @Transactional
    public void deleteHolidayPlan(Long id) {
        HolidayPlan plan = findById(id);
        holidayPlanRepository.delete(plan);
        log.info("Deleted holiday plan: {}", id);
    }
    
    @Transactional
    public HolidayPlan createHolidayPlanFromExcel(MultipartFile file, String userEmail, String destination) {
        try {
            log.info("Processing Excel file for user: {}", userEmail);
            
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            
            // Parse basic information
            String title = getCellValue(sheet.getRow(0), 1, destination + " Holiday Plan");
            LocalDate startDate = LocalDate.parse(getCellValue(sheet.getRow(1), 1, LocalDate.now().toString()));
            LocalDate endDate = LocalDate.parse(getCellValue(sheet.getRow(2), 1, LocalDate.now().plusDays(7).toString()));
            
            // Create holiday plan
            HolidayPlan holidayPlan = new HolidayPlan();
            holidayPlan.setTitle(title);
            holidayPlan.setDestination(destination);
            holidayPlan.setStartDate(startDate);
            holidayPlan.setEndDate(endDate);
            holidayPlan.setUserEmail(userEmail);
            holidayPlan.setStatus(HolidayPlan.PlanStatus.DRAFT);
            holidayPlan.setWeatherOptimizationEnabled(true);
            
            holidayPlan = holidayPlanRepository.save(holidayPlan);
            
            // Parse activities (assuming they start from row 5)
            List<Activity> activities = parseActivitiesFromExcel(sheet, holidayPlan);
            
            // Save activities
            for (Activity activity : activities) {
                activity.setHolidayPlan(holidayPlan);
                activityRepository.save(activity);
            }
            
            workbook.close();
            
            log.info("Successfully created holiday plan from Excel: {}", holidayPlan.getId());
            return holidayPlan;
            
        } catch (IOException e) {
            log.error("Error reading Excel file: {}", e.getMessage());
            throw new RuntimeException("Failed to process Excel file", e);
        } catch (Exception e) {
            log.error("Error creating holiday plan from Excel: {}", e.getMessage());
            throw new RuntimeException("Failed to create holiday plan from Excel", e);
        }
    }
    
    private List<Activity> parseActivitiesFromExcel(Sheet sheet, HolidayPlan holidayPlan) {
        List<Activity> activities = new ArrayList<>();
        
        // Skip header rows and start from row 5 (0-indexed: row 4)
        for (int i = 4; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            try {
                Activity activity = new Activity();
                
                // Parse activity data from Excel columns
                String name = getCellValue(row, 0, "");
                if (name.trim().isEmpty()) continue;
                
                activity.setName(name);
                activity.setDescription(getCellValue(row, 1, ""));
                activity.setDate(LocalDate.parse(getCellValue(row, 2, LocalDate.now().toString())));
                activity.setLocation(getCellValue(row, 3, holidayPlan.getDestination()));
                
                // Parse activity type
                String typeStr = getCellValue(row, 4, "OTHER");
                try {
                    activity.setType(Activity.ActivityType.valueOf(typeStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    activity.setType(Activity.ActivityType.OTHER);
                }
                
                // Parse time slot
                String timeSlotStr = getCellValue(row, 5, "MORNING");
                try {
                    activity.setTimeSlot(Activity.TimeSlot.valueOf(timeSlotStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    activity.setTimeSlot(Activity.TimeSlot.MORNING);
                }
                
                // Parse times if provided
                String startTimeStr = getCellValue(row, 6, "");
                String endTimeStr = getCellValue(row, 7, "");
                
                if (!startTimeStr.isEmpty()) {
                    try {
                        activity.setStartTime(LocalTime.parse(startTimeStr));
                    } catch (Exception e) {
                        log.warn("Invalid start time format: {}", startTimeStr);
                    }
                }
                
                if (!endTimeStr.isEmpty()) {
                    try {
                        activity.setEndTime(LocalTime.parse(endTimeStr));
                    } catch (Exception e) {
                        log.warn("Invalid end time format: {}", endTimeStr);
                    }
                }
                
                // Parse weather dependency
                String weatherDependentStr = getCellValue(row, 8, "false");
                activity.setWeatherDependent(Boolean.parseBoolean(weatherDependentStr));
                
                // Parse priority
                String priorityStr = getCellValue(row, 9, "5");
                try {
                    activity.setPriorityLevel(Integer.parseInt(priorityStr));
                } catch (NumberFormatException e) {
                    activity.setPriorityLevel(5);
                }
                
                // Parse cost estimate
                String costStr = getCellValue(row, 10, "0");
                try {
                    activity.setCostEstimate(Double.parseDouble(costStr));
                } catch (NumberFormatException e) {
                    activity.setCostEstimate(0.0);
                }
                
                // Parse duration
                String durationStr = getCellValue(row, 11, "60");
                try {
                    activity.setEstimatedDurationMinutes(Integer.parseInt(durationStr));
                } catch (NumberFormatException e) {
                    activity.setEstimatedDurationMinutes(60);
                }
                
                activity.setNotes(getCellValue(row, 12, ""));
                activity.setBookingRequired(Boolean.parseBoolean(getCellValue(row, 13, "false")));
                activity.setBookingUrl(getCellValue(row, 14, ""));
                activity.setContactInfo(getCellValue(row, 15, ""));
                
                activities.add(activity);
                
            } catch (Exception e) {
                log.warn("Error parsing activity from row {}: {}", i, e.getMessage());
            }
        }
        
        return activities;
    }
    
    private String getCellValue(Row row, int columnIndex, String defaultValue) {
        if (row == null) return defaultValue;
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return defaultValue;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return defaultValue;
        }
    }
    
    public List<Activity> getActivitiesForPlan(Long planId) {
        return activityRepository.findByHolidayPlanIdOrderByDateAscStartTimeAsc(planId);
    }
    
    public List<Activity> getActivitiesForDate(Long planId, LocalDate date) {
        return activityRepository.findByHolidayPlanIdAndDateOrderByStartTimeAsc(planId, date);
    }
    
    @Transactional
    public Activity addActivity(Long planId, Activity activity) {
        HolidayPlan plan = findById(planId);
        activity.setHolidayPlan(plan);
        
        // Validate activity dates are within plan dates
        if (activity.getDate().isBefore(plan.getStartDate()) || 
            activity.getDate().isAfter(plan.getEndDate())) {
            throw new RuntimeException("Activity date must be within holiday plan dates");
        }
        
        return activityRepository.save(activity);
    }
    
    @Transactional
    public Activity updateActivity(Long planId, Long activityId, Activity updatedActivity) {
        Activity existingActivity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found: " + activityId));
        
        if (!existingActivity.getHolidayPlan().getId().equals(planId)) {
            throw new RuntimeException("Activity does not belong to this holiday plan");
        }
        
        // Update activity fields
        existingActivity.setName(updatedActivity.getName());
        existingActivity.setDescription(updatedActivity.getDescription());
        existingActivity.setDate(updatedActivity.getDate());
        existingActivity.setStartTime(updatedActivity.getStartTime());
        existingActivity.setEndTime(updatedActivity.getEndTime());
        existingActivity.setType(updatedActivity.getType());
        existingActivity.setTimeSlot(updatedActivity.getTimeSlot());
        existingActivity.setLocation(updatedActivity.getLocation());
        existingActivity.setWeatherDependent(updatedActivity.getWeatherDependent());
        existingActivity.setPriorityLevel(updatedActivity.getPriorityLevel());
        existingActivity.setCostEstimate(updatedActivity.getCostEstimate());
        existingActivity.setEstimatedDurationMinutes(updatedActivity.getEstimatedDurationMinutes());
        existingActivity.setNotes(updatedActivity.getNotes());
        existingActivity.setBookingRequired(updatedActivity.getBookingRequired());
        existingActivity.setBookingUrl(updatedActivity.getBookingUrl());
        existingActivity.setContactInfo(updatedActivity.getContactInfo());
        
        return activityRepository.save(existingActivity);
    }
    
    @Transactional
    public void deleteActivity(Long planId, Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found: " + activityId));
        
        if (!activity.getHolidayPlan().getId().equals(planId)) {
            throw new RuntimeException("Activity does not belong to this holiday plan");
        }
        
        activityRepository.delete(activity);
    }
    
    public Map<String, Object> getHolidayPlanStatistics(Long planId) {
        HolidayPlan plan = findById(planId);
        List<Activity> activities = getActivitiesForPlan(planId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalActivities", activities.size());
        stats.put("totalDays", plan.getEndDate().toEpochDay() - plan.getStartDate().toEpochDay() + 1);
        
        // Calculate total estimated cost
        double totalCost = activities.stream()
                .mapToDouble(activity -> activity.getCostEstimate() != null ? activity.getCostEstimate() : 0)
                .sum();
        stats.put("totalEstimatedCost", totalCost);
        
        // Calculate total estimated duration
        int totalDuration = activities.stream()
                .mapToInt(activity -> activity.getEstimatedDurationMinutes() != null ? activity.getEstimatedDurationMinutes() : 0)
                .sum();
        stats.put("totalEstimatedDurationMinutes", totalDuration);
        
        // Activities by type
        Map<Activity.ActivityType, Long> activitiesByType = activities.stream()
                .collect(Collectors.groupingBy(Activity::getType, Collectors.counting()));
        stats.put("activitiesByType", activitiesByType);
        
        // Activities by time slot
        Map<Activity.TimeSlot, Long> activitiesByTimeSlot = activities.stream()
                .collect(Collectors.groupingBy(Activity::getTimeSlot, Collectors.counting()));
        stats.put("activitiesByTimeSlot", activitiesByTimeSlot);
        
        // Weather-dependent activities
        long weatherDependentCount = activities.stream()
                .filter(activity -> activity.getWeatherDependent() != null && activity.getWeatherDependent())
                .count();
        stats.put("weatherDependentActivities", weatherDependentCount);
        
        // AI-optimized activities
        long aiOptimizedCount = activities.stream()
                .filter(activity -> activity.getAiOptimized() != null && activity.getAiOptimized())
                .count();
        stats.put("aiOptimizedActivities", aiOptimizedCount);
        
        return stats;
    }
    
    @Transactional
    public HolidayPlan cloneHolidayPlan(Long originalPlanId, String userEmail, String newTitle) {
        HolidayPlan originalPlan = findById(originalPlanId);
        
        // Create new plan
        HolidayPlan clonedPlan = new HolidayPlan();
        clonedPlan.setTitle(newTitle != null ? newTitle : originalPlan.getTitle() + " (Copy)");
        clonedPlan.setDestination(originalPlan.getDestination());
        clonedPlan.setStartDate(originalPlan.getStartDate());
        clonedPlan.setEndDate(originalPlan.getEndDate());
        clonedPlan.setUserEmail(userEmail);
        clonedPlan.setStatus(HolidayPlan.PlanStatus.DRAFT);
        clonedPlan.setWeatherOptimizationEnabled(originalPlan.getWeatherOptimizationEnabled());
        clonedPlan.setNotes(originalPlan.getNotes());
        
        clonedPlan = holidayPlanRepository.save(clonedPlan);
        
        // Clone activities
        List<Activity> originalActivities = getActivitiesForPlan(originalPlanId);
        for (Activity originalActivity : originalActivities) {
            Activity clonedActivity = new Activity();
            clonedActivity.setName(originalActivity.getName());
            clonedActivity.setDescription(originalActivity.getDescription());
            clonedActivity.setDate(originalActivity.getDate());
            clonedActivity.setStartTime(originalActivity.getStartTime());
            clonedActivity.setEndTime(originalActivity.getEndTime());
            clonedActivity.setType(originalActivity.getType());
            clonedActivity.setTimeSlot(originalActivity.getTimeSlot());
            clonedActivity.setLocation(originalActivity.getLocation());
            clonedActivity.setWeatherDependent(originalActivity.getWeatherDependent());
            clonedActivity.setPriorityLevel(originalActivity.getPriorityLevel());
            clonedActivity.setCostEstimate(originalActivity.getCostEstimate());
            clonedActivity.setEstimatedDurationMinutes(originalActivity.getEstimatedDurationMinutes());
            clonedActivity.setNotes(originalActivity.getNotes());
            clonedActivity.setBookingRequired(originalActivity.getBookingRequired());
            clonedActivity.setBookingUrl(originalActivity.getBookingUrl());
            clonedActivity.setContactInfo(originalActivity.getContactInfo());
            clonedActivity.setHolidayPlan(clonedPlan);
            
            activityRepository.save(clonedActivity);
        }
        
        return clonedPlan;
    }
    
    public Map<String, Object> exportHolidayPlan(Long planId, String format) {
        HolidayPlan plan = findById(planId);
        List<Activity> activities = getActivitiesForPlan(planId);
        
        Map<String, Object> result = new HashMap<>();
        
        if ("excel".equalsIgnoreCase(format)) {
            try {
                byte[] excelData = exportToExcel(plan, activities);
                result.put("data", Base64.getEncoder().encodeToString(excelData));
                result.put("filename", plan.getTitle() + "_" + LocalDate.now() + ".xlsx");
                result.put("contentType", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } catch (Exception e) {
                log.error("Error exporting to Excel: {}", e.getMessage());
                throw new RuntimeException("Failed to export to Excel", e);
            }
        } else if ("pdf".equalsIgnoreCase(format)) {
            // TODO: Implement PDF export
            result.put("error", "PDF export not yet implemented");
        } else {
            result.put("error", "Unsupported format: " + format);
        }
        
        return result;
    }
    
    private byte[] exportToExcel(HolidayPlan plan, List<Activity> activities) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Holiday Plan");
        
        // Create header information
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Title:");
        titleRow.createCell(1).setCellValue(plan.getTitle());
        
        Row destinationRow = sheet.createRow(1);
        destinationRow.createCell(0).setCellValue("Destination:");
        destinationRow.createCell(1).setCellValue(plan.getDestination());
        
        Row startDateRow = sheet.createRow(2);
        startDateRow.createCell(0).setCellValue("Start Date:");
        startDateRow.createCell(1).setCellValue(plan.getStartDate().toString());
        
        Row endDateRow = sheet.createRow(3);
        endDateRow.createCell(0).setCellValue("End Date:");
        endDateRow.createCell(1).setCellValue(plan.getEndDate().toString());
        
        // Create activities header
        Row headerRow = sheet.createRow(5);
        String[] headers = {"Activity", "Description", "Date", "Location", "Type", "Time Slot", 
                           "Start Time", "End Time", "Weather Dependent", "Priority", "Cost", "Duration", "Notes"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        
        // Add activities
        int rowNum = 6;
        for (Activity activity : activities) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(activity.getName());
            row.createCell(1).setCellValue(activity.getDescription() != null ? activity.getDescription() : "");
            row.createCell(2).setCellValue(activity.getDate().toString());
            row.createCell(3).setCellValue(activity.getLocation());
            row.createCell(4).setCellValue(activity.getType().toString());
            row.createCell(5).setCellValue(activity.getTimeSlot().toString());
            row.createCell(6).setCellValue(activity.getStartTime() != null ? activity.getStartTime().toString() : "");
            row.createCell(7).setCellValue(activity.getEndTime() != null ? activity.getEndTime().toString() : "");
            row.createCell(8).setCellValue(activity.getWeatherDependent() != null ? activity.getWeatherDependent() : false);
            row.createCell(9).setCellValue(activity.getPriorityLevel() != null ? activity.getPriorityLevel() : 5);
            row.createCell(10).setCellValue(activity.getCostEstimate() != null ? activity.getCostEstimate() : 0);
            row.createCell(11).setCellValue(activity.getEstimatedDurationMinutes() != null ? activity.getEstimatedDurationMinutes() : 60);
            row.createCell(12).setCellValue(activity.getNotes() != null ? activity.getNotes() : "");
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    public Map<String, Object> shareHolidayPlan(Long planId, String recipientEmail, String message) {
        HolidayPlan plan = findById(planId);
        
        try {
            // TODO: Implement share functionality
            // For now, just return success
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Holiday plan shared successfully");
            result.put("recipient", recipientEmail);
            result.put("planTitle", plan.getTitle());
            
            return result;
        } catch (Exception e) {
            log.error("Error sharing holiday plan: {}", e.getMessage());
            throw new RuntimeException("Failed to share holiday plan", e);
        }
    }
    
    public List<HolidayPlan> getUpcomingHolidaysForUser(String userEmail, int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        
        return holidayPlanRepository.findUpcomingPlans(userEmail, startDate, endDate);
    }
    
    public List<String> getPopularDestinations() {
        // TODO: Implement based on actual data
        return List.of("Rome", "Milan", "Venice", "Florence", "Naples", "Turin", "Genoa", "Bologna");
    }
    
    public List<String> getUserDestinations(String userEmail) {
        return holidayPlanRepository.findDistinctDestinationsByUserEmail(userEmail);
    }
    
    private void validateHolidayPlan(HolidayPlan holidayPlan) {
        if (holidayPlan.getStartDate().isAfter(holidayPlan.getEndDate())) {
            throw new RuntimeException("Start date must be before end date");
        }
        
        if (holidayPlan.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Start date cannot be in the past");
        }
        
        if (holidayPlan.getTitle() == null || holidayPlan.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Holiday plan title is required");
        }
        
        if (holidayPlan.getDestination() == null || holidayPlan.getDestination().trim().isEmpty()) {
            throw new RuntimeException("Holiday plan destination is required");
        }
        
        if (holidayPlan.getUserEmail() == null || holidayPlan.getUserEmail().trim().isEmpty()) {
            throw new RuntimeException("User email is required");
        }
    }
}