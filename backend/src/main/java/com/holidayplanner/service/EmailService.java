package com.holidayplanner.service;

import com.holidayplanner.model.Activity;
import com.holidayplanner.model.HolidayPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${email.template.base-url}")
    private String baseUrl;
    
    @Value("${email.template.logo-url}")
    private String logoUrl;
    
    public void sendHolidayPlanOptimizationEmail(String recipientEmail, HolidayPlan holidayPlan, List<Activity> optimizedActivities) {
        try {
            Context context = createHolidayOptimizationContext(holidayPlan, optimizedActivities);
            String htmlContent = templateEngine.process("holiday-optimization-email", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("🌟 Your Holiday Plan Has Been Optimized! - " + holidayPlan.getTitle());
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Holiday optimization email sent to: {}", recipientEmail);
            
        } catch (MessagingException e) {
            log.error("Error sending holiday optimization email to {}: {}", recipientEmail, e.getMessage());
            throw new RuntimeException("Failed to send optimization email", e);
        }
    }
    
    public void sendWeatherAlertEmail(String recipientEmail, HolidayPlan holidayPlan, List<Activity> affectedActivities, String weatherAlert) {
        try {
            Context context = createWeatherAlertContext(holidayPlan, affectedActivities, weatherAlert);
            String htmlContent = templateEngine.process("weather-alert-email", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("⚠️ Weather Alert for Your Holiday - " + holidayPlan.getTitle());
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Weather alert email sent to: {}", recipientEmail);
            
        } catch (MessagingException e) {
            log.error("Error sending weather alert email to {}: {}", recipientEmail, e.getMessage());
            throw new RuntimeException("Failed to send weather alert email", e);
        }
    }
    
    public void sendHolidayReminderEmail(String recipientEmail, HolidayPlan holidayPlan, int daysUntilTrip) {
        try {
            Context context = createHolidayReminderContext(holidayPlan, daysUntilTrip);
            String htmlContent = templateEngine.process("holiday-reminder-email", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("🎒 Your Holiday to " + holidayPlan.getDestination() + " is Coming Up!");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Holiday reminder email sent to: {}", recipientEmail);
            
        } catch (MessagingException e) {
            log.error("Error sending holiday reminder email to {}: {}", recipientEmail, e.getMessage());
            throw new RuntimeException("Failed to send reminder email", e);
        }
    }
    
    public void sendDailyItineraryEmail(String recipientEmail, HolidayPlan holidayPlan, LocalDate date, List<Activity> dailyActivities) {
        try {
            Context context = createDailyItineraryContext(holidayPlan, date, dailyActivities);
            String htmlContent = templateEngine.process("daily-itinerary-email", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("📅 Today's Itinerary - " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Daily itinerary email sent to: {}", recipientEmail);
            
        } catch (MessagingException e) {
            log.error("Error sending daily itinerary email to {}: {}", recipientEmail, e.getMessage());
            throw new RuntimeException("Failed to send daily itinerary email", e);
        }
    }
    
    private Context createHolidayOptimizationContext(HolidayPlan holidayPlan, List<Activity> optimizedActivities) {
        Context context = new Context(Locale.ENGLISH);
        
        context.setVariable("planTitle", holidayPlan.getTitle());
        context.setVariable("destination", holidayPlan.getDestination());
        context.setVariable("startDate", holidayPlan.getStartDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        context.setVariable("endDate", holidayPlan.getEndDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("logoUrl", logoUrl);
        context.setVariable("planId", holidayPlan.getId());
        
        // Group activities by date
        var activitiesByDate = optimizedActivities.stream()
                .collect(Collectors.groupingBy(Activity::getDate));
        
        context.setVariable("activitiesByDate", activitiesByDate);
        
        // Count optimized activities
        long optimizedCount = optimizedActivities.stream()
                .filter(activity -> activity.getAiOptimized() != null && activity.getAiOptimized())
                .count();
        context.setVariable("optimizedCount", optimizedCount);
        
        // CTA button URL
        context.setVariable("ctaUrl", baseUrl + "/holiday-plans/" + holidayPlan.getId() + "/review");
        
        return context;
    }
    
    private Context createWeatherAlertContext(HolidayPlan holidayPlan, List<Activity> affectedActivities, String weatherAlert) {
        Context context = new Context(Locale.ENGLISH);
        
        context.setVariable("planTitle", holidayPlan.getTitle());
        context.setVariable("destination", holidayPlan.getDestination());
        context.setVariable("weatherAlert", weatherAlert);
        context.setVariable("affectedActivities", affectedActivities);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("logoUrl", logoUrl);
        context.setVariable("planId", holidayPlan.getId());
        
        // CTA button URL
        context.setVariable("ctaUrl", baseUrl + "/holiday-plans/" + holidayPlan.getId() + "/weather-update");
        
        return context;
    }
    
    private Context createHolidayReminderContext(HolidayPlan holidayPlan, int daysUntilTrip) {
        Context context = new Context(Locale.ENGLISH);
        
        context.setVariable("planTitle", holidayPlan.getTitle());
        context.setVariable("destination", holidayPlan.getDestination());
        context.setVariable("startDate", holidayPlan.getStartDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        context.setVariable("endDate", holidayPlan.getEndDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        context.setVariable("daysUntilTrip", daysUntilTrip);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("logoUrl", logoUrl);
        context.setVariable("planId", holidayPlan.getId());
        
        // Generate reminder checklist
        context.setVariable("reminderChecklist", generateReminderChecklist(daysUntilTrip));
        
        // CTA button URL
        context.setVariable("ctaUrl", baseUrl + "/holiday-plans/" + holidayPlan.getId());
        
        return context;
    }
    
    private Context createDailyItineraryContext(HolidayPlan holidayPlan, LocalDate date, List<Activity> dailyActivities) {
        Context context = new Context(Locale.ENGLISH);
        
        context.setVariable("planTitle", holidayPlan.getTitle());
        context.setVariable("destination", holidayPlan.getDestination());
        context.setVariable("date", date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        context.setVariable("dailyActivities", dailyActivities);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("logoUrl", logoUrl);
        context.setVariable("planId", holidayPlan.getId());
        
        // Group activities by time slot
        var activitiesByTimeSlot = dailyActivities.stream()
                .collect(Collectors.groupingBy(Activity::getTimeSlot));
        context.setVariable("activitiesByTimeSlot", activitiesByTimeSlot);
        
        // CTA button URL
        context.setVariable("ctaUrl", baseUrl + "/holiday-plans/" + holidayPlan.getId() + "/day/" + date);
        
        return context;
    }
    
    private List<String> generateReminderChecklist(int daysUntilTrip) {
        if (daysUntilTrip <= 7) {
            return List.of(
                "Check weather forecast",
                "Pack appropriate clothing",
                "Confirm accommodation bookings",
                "Check travel documents",
                "Download offline maps",
                "Review daily itinerary"
            );
        } else if (daysUntilTrip <= 14) {
            return List.of(
                "Book restaurant reservations",
                "Purchase travel insurance",
                "Notify bank of travel plans",
                "Research local customs",
                "Book attraction tickets",
                "Check passport validity"
            );
        } else {
            return List.of(
                "Research your destination",
                "Book flights and accommodation",
                "Apply for visa if needed",
                "Check vaccination requirements",
                "Plan your budget",
                "Create packing list"
            );
        }
    }
    
    public void sendTestEmail(String recipientEmail) {
        try {
            Context context = new Context(Locale.ENGLISH);
            context.setVariable("recipientEmail", recipientEmail);
            context.setVariable("baseUrl", baseUrl);
            context.setVariable("logoUrl", logoUrl);
            
            String htmlContent = templateEngine.process("test-email", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("✅ Holiday Planner Email Test");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Test email sent to: {}", recipientEmail);
            
        } catch (MessagingException e) {
            log.error("Error sending test email to {}: {}", recipientEmail, e.getMessage());
            throw new RuntimeException("Failed to send test email", e);
        }
    }
}