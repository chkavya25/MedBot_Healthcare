package com.medbot.controller;

import com.medbot.model.*;
import com.medbot.service.MedBotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class MedBotController {
    private final MedBotService service;
    public MedBotController(MedBotService service){this.service=service;}

    @GetMapping("/doctors") public List<Doctor> doctors(@RequestParam(required=false) String q,@RequestParam(required=false) String specialization){return service.searchDoctors(q,specialization);}
    @GetMapping("/doctors/{id}") public Doctor doctor(@PathVariable long id){return service.getDoctor(id);}
    @GetMapping("/doctors/{id}/slots") public List<Slot> slots(@PathVariable long id,@RequestParam String date){return service.getSlots(id,date);}
    @GetMapping("/treatments") public List<Treatment> treatments(){return service.treatments();}

    @PostMapping("/login") public ResponseEntity<?> login(@RequestBody Map<String,String> body){
        String email=body.getOrDefault("email",""); String password=body.getOrDefault("password",""); String role=body.getOrDefault("role","PATIENT");
        boolean ok=("patient@medbot.com".equalsIgnoreCase(email)&&"1234".equals(password)&&"PATIENT".equals(role)) || ("doctor@medbot.com".equalsIgnoreCase(email)&&"1234".equals(password)&&"DOCTOR".equals(role));
        if(!ok) return ResponseEntity.status(401).body(Map.of("success",false,"message","Invalid demo credentials. Use patient@medbot.com / 1234 or doctor@medbot.com / 1234."));
        String name="PATIENT".equals(role)?"Reshma":"Dr. Swetha Rao";
        return ResponseEntity.ok(Map.of("success",true,"name",name,"role",role,"email",email));
    }
    @PostMapping("/appointments") public ResponseEntity<?> book(@RequestBody Map<String,String> b){
        try { return ResponseEntity.ok(service.book(b.get("patientName"),Long.parseLong(b.get("doctorId")),b.get("date"),b.get("time"))); }
        catch(IllegalStateException|IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));}
    }
    @GetMapping("/appointments") public List<Appointment> appointments(@RequestParam String patient){return service.appointments(patient);}
    @PutMapping("/appointments/{id}/cancel") public ResponseEntity<?> cancel(@PathVariable long id){try{return ResponseEntity.ok(service.cancel(id));}catch(Exception e){return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));}}
    @PutMapping("/appointments/{id}/reschedule") public ResponseEntity<?> reschedule(@PathVariable long id,@RequestBody Map<String,String> b){try{return ResponseEntity.ok(service.reschedule(id,b.get("date"),b.get("time")));}catch(Exception e){return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));}}
    @PostMapping("/chat") public Map<String,String> chat(@RequestBody Map<String,String> b){return Map.of("reply",service.chat(b.get("message")));}
}
