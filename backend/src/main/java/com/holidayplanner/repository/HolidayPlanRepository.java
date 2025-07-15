package com.holidayplanner.repository;

import com.holidayplanner.model.HolidayPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayPlanRepository extends JpaRepository<HolidayPlan, Long> {
    
    List<HolidayPlan> findByUserEmailOrderByCreatedDateDesc(String userEmail);
    
    List<HolidayPlan> findByUserEmailAndStatusOrderByStartDateAsc(String userEmail, HolidayPlan.PlanStatus status);
    
    List<HolidayPlan> findByDestinationAndStartDateBetween(String destination, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT hp FROM HolidayPlan hp WHERE hp.userEmail = :userEmail AND hp.startDate >= :startDate AND hp.endDate <= :endDate")
    List<HolidayPlan> findUpcomingPlans(@Param("userEmail") String userEmail, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT hp FROM HolidayPlan hp WHERE hp.weatherOptimizationEnabled = true AND hp.startDate >= :currentDate")
    List<HolidayPlan> findPlansNeedingWeatherOptimization(@Param("currentDate") LocalDate currentDate);
    
    Optional<HolidayPlan> findByGoogleCalendarEventId(String googleCalendarEventId);
    
    @Query("SELECT COUNT(hp) FROM HolidayPlan hp WHERE hp.userEmail = :userEmail AND hp.status = :status")
    long countByUserEmailAndStatus(@Param("userEmail") String userEmail, @Param("status") HolidayPlan.PlanStatus status);
    
    @Query("SELECT DISTINCT hp.destination FROM HolidayPlan hp WHERE hp.userEmail = :userEmail")
    List<String> findDistinctDestinationsByUserEmail(@Param("userEmail") String userEmail);
    
    @Query("SELECT hp FROM HolidayPlan hp WHERE hp.userEmail = :userEmail AND " +
           "(:destination IS NULL OR hp.destination LIKE %:destination%) AND " +
           "(:status IS NULL OR hp.status = :status) AND " +
           "(:startDate IS NULL OR hp.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR hp.endDate <= :endDate)")
    List<HolidayPlan> findWithFilters(@Param("userEmail") String userEmail,
                                     @Param("destination") String destination,
                                     @Param("status") HolidayPlan.PlanStatus status,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);
}