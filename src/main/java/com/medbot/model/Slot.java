package com.medbot.model;

public record Slot(long id, long doctorId, String date, String time, String status) {}
