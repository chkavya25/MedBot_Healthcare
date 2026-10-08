package com.medbot.model;

public class Appointment {
    public long id;
    public String patientName;
    public long doctorId;
    public String doctorName;
    public String specialization;
    public String date;
    public String time;
    public double fee;
    public String status;

    public Appointment(long id, String patientName, long doctorId, String doctorName,
                       String specialization, String date, String time, double fee, String status) {
        this.id=id; this.patientName=patientName; this.doctorId=doctorId; this.doctorName=doctorName;
        this.specialization=specialization; this.date=date; this.time=time; this.fee=fee; this.status=status;
    }
}
