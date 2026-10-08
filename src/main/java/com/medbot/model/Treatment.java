package com.medbot.model;

public record Treatment(long id, String name, String specialization, String description,
                        double price, String duration) {}
