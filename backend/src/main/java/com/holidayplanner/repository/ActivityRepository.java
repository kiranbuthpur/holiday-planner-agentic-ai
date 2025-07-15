package com.holidayplanner.repository;

import com.holidayplanner.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    
    List<Activity> findByHolidayPlanIdOrderByDateAscStartTimeAsc(Long holidayPlanId);
    
    List<Activity> findByHolidayPlanIdAndDateOrderByStartTimeAsc(Long holidayPlanId, LocalDate date);
    
    List<Activity> findByHolidayPlanIdAndTimeSlotOrderByDateAsc(Long holidayPlanId, Activity.TimeSlot timeSlot);
    
    @Query("SELECT a FROM Activity a WHERE a.holidayPlan.id = :holidayPlanId AND a.weatherDependent = true")
    List<Activity> findWeatherDependentActivities(@Param("holidayPlanId") Long holidayPlanId);
    
    @Query("SELECT a FROM Activity a WHERE a.holidayPlan.id = :holidayPlanId AND a.type = :type ORDER BY a.date ASC")
    List<Activity> findByHolidayPlanIdAndType(@Param("holidayPlanId") Long holidayPlanId, @Param("type") Activity.ActivityType type);
    
    @Query("SELECT a FROM Activity a WHERE a.holidayPlan.id = :holidayPlanId AND a.date = :date AND " +
           "((a.startTime <= :endTime AND a.endTime >= :startTime) OR " +
           "(a.startTime IS NULL OR a.endTime IS NULL))")
    List<Activity> findConflictingActivities(@Param("holidayPlanId") Long holidayPlanId,
                                           @Param("date") LocalDate date,
                                           @Param("startTime") LocalTime startTime,
                                           @Param("endTime") LocalTime endTime);
    
    @Query("SELECT a FROM Activity a WHERE a.holidayPlan.id = :holidayPlanId AND a.location = :location ORDER BY a.date ASC")
    List<Activity> findByHolidayPlanIdAndLocation(@Param("holidayPlanId") Long holidayPlanId, @Param("location") String location);
    
    @Query("SELECT a FROM Activity a WHERE a.holidayPlan.id = :holidayPlanId AND a.aiOptimized = true ORDER BY a.date ASC")
    List<Activity> findAiOptimizedActivities(@Param("holidayPlanId") Long holidayPlanId);
    
    @Query("SELECT a FROM Activity a WHERE a.holidayPlan.id = :holidayPlanId AND a.bookingRequired = true AND a.bookingUrl IS NOT NULL")
    List<Activity> findActivitiesRequiringBooking(@Param("holidayPlanId") Long holidayPlanId);
    
    @Query("SELECT a FROM Activity a WHERE a.holidayPlan.id = :holidayPlanId AND a.priorityLevel >= :minPriority ORDER BY a.priorityLevel DESC, a.date ASC")
    List<Activity> findHighPriorityActivities(@Param("holidayPlanId") Long holidayPlanId, @Param("minPriority") Integer minPriority);
    
    @Query("SELECT COUNT(a) FROM Activity a WHERE a.holidayPlan.id = :holidayPlanId AND a.date = :date")
    long countActivitiesForDate(@Param("holidayPlanId") Long holidayPlanId, @Param("date") LocalDate date);
    
    @Query("SELECT SUM(a.estimatedDurationMinutes) FROM Activity a WHERE a.holidayPlan.id = :holidayPlanId AND a.date = :date")
    Long getTotalDurationForDate(@Param("holidayPlanId") Long holidayPlanId, @Param("date") LocalDate date);
    
    @Query("SELECT SUM(a.costEstimate) FROM Activity a WHERE a.holidayPlan.id = :holidayPlanId")
    Double getTotalCostEstimate(@Param("holidayPlanId") Long holidayPlanId);
}