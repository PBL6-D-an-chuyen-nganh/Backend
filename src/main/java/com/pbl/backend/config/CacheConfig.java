package com.pbl.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct; // Import thêm cái này
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @PostConstruct
    public void logConfigStatus() {
        System.err.println("==================================================");
        System.err.println("!!! [DEBUG] CACHE CONFIG IS LOADED SUCCESSFULY !!!");
        System.err.println("==================================================");
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(500)
                .recordStats());

        cacheManager.setCacheNames(Arrays.asList(
                "appointments_by_creator", "appointments_by_doctor", "appointment_details",
                "doctor_slots", "articles", "doctors", "doctor_details", "diagnoses", "patient_lists", "doctor_summaries",
                "doctors_by_specialty"
        ));
        return cacheManager;
    }
}