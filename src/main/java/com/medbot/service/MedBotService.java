package com.medbot.service;

import com.medbot.model.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class MedBotService {
    private final List<Doctor> doctors = new ArrayList<>();
    private final List<Treatment> treatments = new ArrayList<>();
    private final Map<Long, List<Slot>> slots = new HashMap<>();
    private final List<Appointment> appointments = new ArrayList<>();
    private final AtomicLong appointmentSeq = new AtomicLong(1001);

    public MedBotService() { seed(); }

    private void seed() {
        // One hospital with five doctor categories for the prototype.
        doctors.add(new Doctor(1,"Dr. Swetha Rao","General Physician","MBBS, MD General Medicine",12,"MedBot Hospital",300,4.9,"English, Telugu, Hindi","First point of care for fever, cold, cough, common infections, routine checkups and general health concerns.","Mon-Sat · 9:00 AM-12:00 PM, 5:00 PM-8:00 PM"));
        doctors.add(new Doctor(2,"Dr. Arjun Varma","Cardiologist","MBBS, MD Cardiology",14,"MedBot Hospital",600,4.8,"English, Telugu, Hindi","Handles heart and chest-related concerns, cardiac consultation and preventive heart care.","Mon-Sat · 10:00 AM-1:00 PM, 4:00 PM-7:00 PM"));
        doctors.add(new Doctor(3,"Dr. Priya Sharma","Dermatologist","MBBS, MD Dermatology",9,"MedBot Hospital",500,4.7,"English, Telugu, Hindi","Handles skin, hair and nail concerns including acne, rashes and common skin conditions.","Mon-Fri · 9:30 AM-1:00 PM, 3:30 PM-6:30 PM"));
        doctors.add(new Doctor(4,"Dr. Anitha Reddy","Pediatrician","MBBS, MD Pediatrics",8,"MedBot Hospital",400,4.8,"English, Telugu, Hindi","Provides healthcare for infants and children, including common childhood illnesses and growth checkups.","Mon-Sat · 10:00 AM-1:00 PM, 4:00 PM-6:00 PM"));
        doctors.add(new Doctor(5,"Dr. Kiran Varma","Orthopedic Specialist","MBBS, MS Orthopedics",14,"MedBot Hospital",550,4.6,"English, Telugu","Handles bone, joint, muscle and back-pain concerns, including common sports injuries.","Mon-Sat · 11:00 AM-2:00 PM, 5:00 PM-7:30 PM"));

        treatments.add(new Treatment(1,"General Health Checkup","General Physician","Routine consultation for common health concerns and preventive care.",300,"20-30 min"));
        treatments.add(new Treatment(2,"Fever & Cold Care","General Physician","Assessment and guidance for common fever, cold, cough and minor infections.",300,"20 min"));
        treatments.add(new Treatment(3,"Heart Consultation","Cardiologist","Cardiac consultation and preventive heart-health assessment.",600,"30 min"));
        treatments.add(new Treatment(4,"Acne & Skin Consultation","Dermatologist","Consultation for acne, rashes and other common skin concerns.",500,"25 min"));
        treatments.add(new Treatment(5,"Child Health Checkup","Pediatrician","Routine child health, growth and common illness consultation.",400,"25 min"));
        treatments.add(new Treatment(6,"Joint & Back Pain Consultation","Orthopedic Specialist","Assessment of common joint, bone, muscle and back-pain concerns.",550,"30 min"));
        treatments.add(new Treatment(7,"Sports Injury Consultation","Orthopedic Specialist","Initial consultation for common sports-related bone and joint injuries.",550,"30 min"));
        treatments.add(new Treatment(8,"Preventive Health Review","General Physician","General review of health concerns and preventive-care guidance.",300,"20-30 min"));

        LocalDate base=LocalDate.now();
        for (Doctor d: doctors) {
            List<Slot> list=new ArrayList<>(); long sid=d.id()*10000;
            String[] times={"09:00 AM","09:30 AM","10:00 AM","10:30 AM","11:00 AM","11:30 AM","12:00 PM","12:30 PM","04:00 PM","04:30 PM","05:00 PM","05:30 PM","06:00 PM","06:30 PM"};
            for(int day=0; day<14; day++) {
                LocalDate date=base.plusDays(day);
                for(int i=0;i<times.length;i++) {
                    if((d.id()+day+i)%5==0) continue;
                    list.add(new Slot(sid++,d.id(),date.toString(),times[i],"AVAILABLE"));
                }
            }
            slots.put(d.id(),list);
        }
        // A couple of sample appointments are kept for the demo patient so the cancellation/reschedule screens can be demonstrated immediately.
        bookInternal("Reshma",1,LocalDate.now().plusDays(1).toString(),"10:30 AM");
        bookInternal("Reshma",2,LocalDate.now().plusDays(3).toString(),"11:00 AM");
    }

    private Appointment bookInternal(String patient,long doctorId,String date,String time) {
        Doctor d=getDoctor(doctorId);
        Slot s=findSlot(doctorId,date,time);
        if(s==null || !"AVAILABLE".equals(s.status())) return null;
        replaceSlot(s,new Slot(s.id(),s.doctorId(),s.date(),s.time(),"BOOKED"));
        Appointment a=new Appointment(appointmentSeq.getAndIncrement(),patient,doctorId,d.name(),d.specialization(),date,time,d.fee(),"CONFIRMED");
        appointments.add(a); return a;
    }

    public List<Doctor> doctors(){return doctors;}
    public List<Treatment> treatments(){return treatments;}
    public Doctor getDoctor(long id){return doctors.stream().filter(d->d.id()==id).findFirst().orElseThrow();}
    public List<Doctor> searchDoctors(String q,String specialization){
        return doctors.stream().filter(d -> (q==null||q.isBlank()||d.name().toLowerCase().contains(q.toLowerCase())||d.specialization().toLowerCase().contains(q.toLowerCase())) && (specialization==null||specialization.isBlank()||d.specialization().equalsIgnoreCase(specialization))).toList();
    }
    public List<Slot> getSlots(long doctorId,String date){return slots.getOrDefault(doctorId,List.of()).stream().filter(s->date==null||date.isBlank()||s.date().equals(date)).toList();}
    public synchronized Appointment book(String patient,long doctorId,String date,String time){
        if(patient==null||patient.isBlank()) throw new IllegalArgumentException("Patient name is required");
        Slot s=findSlot(doctorId,date,time);
        if(s==null) throw new IllegalArgumentException("Slot not found for the selected date and time.");
        if(!"AVAILABLE".equals(s.status())) throw new IllegalStateException("This slot is already filled. Please choose another available slot.");
        return bookInternal(patient,doctorId,date,time);
    }
    public List<Appointment> appointments(String patient){return appointments.stream().filter(a->a.patientName.equalsIgnoreCase(patient==null?"":patient)).collect(Collectors.toList());}
    public synchronized Appointment cancel(long id){
        Appointment a=findAppointment(id); a.status="CANCELLED"; setSlotStatus(a,"AVAILABLE"); return a;
    }
    public synchronized Appointment reschedule(long id,String date,String time){
        Appointment a=findAppointment(id);
        if(!"CONFIRMED".equals(a.status)) throw new IllegalStateException("Only confirmed appointments can be rescheduled.");
        Slot target=findSlot(a.doctorId,date,time);
        if(target==null) throw new IllegalArgumentException("Selected slot does not exist.");
        if(!"AVAILABLE".equals(target.status())) throw new IllegalStateException("That slot is filled. Please select another slot.");
        setSlotStatus(a,"AVAILABLE"); replaceSlot(target,new Slot(target.id(),target.doctorId(),target.date(),target.time(),"BOOKED"));
        a.date=date; a.time=time; a.status="CONFIRMED"; return a;
    }
    private Appointment findAppointment(long id){return appointments.stream().filter(a->a.id==id).findFirst().orElseThrow(()->new IllegalArgumentException("Appointment not found."));}
    private Slot findSlot(long doctorId,String date,String time){return slots.getOrDefault(doctorId,List.of()).stream().filter(s->s.date().equals(date)&&s.time().equals(time)).findFirst().orElse(null);}
    private void replaceSlot(Slot old,Slot newer){List<Slot> l=slots.get(old.doctorId()); for(int i=0;i<l.size();i++) if(l.get(i).id()==old.id()){l.set(i,newer);return;}}
    private void setSlotStatus(Appointment a,String status){Slot s=findSlot(a.doctorId,a.date,a.time);if(s!=null)replaceSlot(s,new Slot(s.id(),s.doctorId(),s.date(),s.time(),status));}
    public String chat(String message){
        String m=(message==null?"":message).toLowerCase(Locale.ROOT).trim();
        if(m.isBlank()) return "Please type a question or choose one of the options below.";
        if(m.matches(".*\\b(hi|hello|hey|hii|hai)\\b.*")) return "Hi 👋 Welcome to MedBot! How can I help you today?\n\n1. Book an appointment\n2. Find a doctor\n3. View treatments\n4. Check appointment\n5. Help / How to book";
        if(m.contains("how to")&&m.contains("book") || m.contains("booking process")) return "Booking is easy: 1) Choose a specialization or doctor 2) Open the doctor profile 3) Select a date 4) Choose a green available slot 5) Confirm the appointment. The selected slot becomes booked immediately.";
        if(m.contains("book")||m.contains("appointment")) return "Sure 😊 I can help with appointments. You can use the Book Appointment section, select a doctor, date and an available green slot. You can also ask me for doctor types, prices, timings or available slots.";
        if(m.contains("skin")||m.contains("acne")||m.contains("hair")) return "For skin, hair or nail concerns, choose a Dermatologist. MedBot has Dermatologists for acne, skin concerns and hair/scalp consultations.";
        if(m.contains("fever")||m.contains("cold")||m.contains("cough")) return "For common fever, cold, cough or general symptoms, a General Physician is the usual first choice. You can browse General Physicians from Find Doctors.";
        if(m.contains("heart")||m.contains("cardio")) return "For heart-related concerns, choose a Cardiologist. Dr. Swetha Rao is available in this demo with a ₹600 consultation fee.";
        if(m.contains("child")||m.contains("kid")||m.contains("baby")) return "For children's health, choose a Pediatrician. Dr. Anitha Reddy is available in this single-hospital demo with a ₹400 consultation fee.";
        if(m.contains("bone")||m.contains("joint")||m.contains("back pain")||m.contains("muscle")) return "For bones, joints, muscles or back pain, choose an Orthopedic Specialist.";
        if(m.contains("price")||m.contains("fee")||m.contains("cost")) return "Demo consultation fees: General Physician ₹300, Pediatrician ₹400, Dermatologist ₹500, Orthopedic ₹550, Cardiologist ₹600.";
        if(m.contains("slot")||m.contains("available")) return "Open Find Doctors → choose a doctor → select a date. Green slots are available, red/filled slots are booked, and blocked times are not bookable.";
        if(m.contains("reschedule")) return "Open My Appointments, choose a confirmed appointment, select Reschedule, then choose a new available date and green slot.";
        if(m.contains("cancel")) return "Open My Appointments and select Cancel on a confirmed appointment. The slot becomes available again in this demo.";
        if(m.contains("treatment")) return "You can browse treatments such as General Health Checkup, Heart Consultation, Acne Treatment, Joint Pain Consultation, Child Health Checkup and more in the Treatments section.";
        if(m.contains("thank")) return "You're welcome 😊 I'm here to help.";
        if(m.contains("help")) return "I can help with: doctor selection, treatments, consultation fees, timings, booking steps, available slots, appointment status, rescheduling and cancellation.";
        return "I can help you with doctors, treatments, prices, timings, available slots and appointments. Try: 'Which doctor for fever?', 'How to book?', or 'How much is a cardiologist?'.";
    }
}
