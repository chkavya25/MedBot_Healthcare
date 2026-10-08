package com.medbot.model;

public record Doctor(long id, String name, String specialization, String qualification,
                     int experience, String hospital, double fee, double rating,
                     String languages, String about, String timings) {}
