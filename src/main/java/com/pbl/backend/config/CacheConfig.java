package com.pbl.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(5000)
                .recordStats());

        cacheManager.setCacheNames(Arrays.asList(
                "appointments_by_creator", "appointments_by_doctor", "appointment_details",
                "doctor_slots", "doctors", "doctor_details", "diagnoses", "patient_lists",
                "doctors_by_specialty", "doctor_summaries"
        ));
        return cacheManager;
    }

    @Bean
    public CacheManager permanentCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .recordStats());

        cacheManager.setCacheNames(Arrays.asList("articles"));
        return cacheManager;
    }
}