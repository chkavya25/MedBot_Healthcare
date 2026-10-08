# MedBot Healthcare Assistant — No Database

A complete demo healthcare appointment website based on the MedBot project idea.

## Stack
- HTML5
- CSS3
- Vanilla JavaScript
- Java 17 + Spring Boot
- In-memory Java collections (NO MySQL / NO database)

## Features
- Patient and doctor demo login
- Doctor specializations and profiles
- Treatments list
- Consultation prices and timings
- Date-wise available/filled slots
- Appointment booking
- Appointment history
- Reschedule and cancel
- Rule-based MedBot chatbot
- Responsive UI

## Demo credentials
Patient: `patient@medbot.com` / `1234`
Doctor: `doctor@medbot.com` / `1234`

## Run
Requirements: JDK 17+ and Maven 3.9+

```bash
mvn spring-boot:run
```
Open http://localhost:8080

Or build:
```bash
mvn clean package
java -jar target/medbot-no-db-1.0.0.jar
```

All data is stored only in Java memory and resets when the application restarts. No database is required.


## How the MedBot chatbot works
The chatbot is a rule-based assistant; it does not use an external AI API or database.

1. The user types a message or selects a quick option in `index.html`.
2. `app.js` sends the message to the Java backend using `POST /api/chat`.
3. `MedBotController.java` receives the request and calls `MedBotService.chat()`.
4. `MedBotService.java` checks the message using Java keyword/rule matching.
5. A reply is returned as JSON and `app.js` displays it in the chat window.

Examples:
- "Hi" → welcome/options
- "How to book?" → booking steps
- "Which doctor for fever?" → General Physician
- "How much is a cardiologist?" → consultation fee information

## Chatbot minimize behavior
Clicking `−` now minimizes the chat window without removing it permanently. A floating `💬` button appears in the bottom-right corner; clicking it restores the chatbot.

The chatbot window is positioned slightly above the floating button to avoid overlap and is responsive on mobile screens.


## Updated demo behavior
- `run.bat` starts the Spring Boot server and automatically opens MedBot in the browser.
- MedBot uses port `8081` so it can run alongside another project using port `8080`.
- The prototype contains one hospital with five doctor categories: General Physician, Cardiologist, Dermatologist, Pediatrician and Orthopedic Specialist.
- The Doctors section now includes a simple guide explaining what each category is generally used for.
- After booking, a confirmation dialog displays appointment ID, patient, doctor, category, date, time, fee and status.
- Cancelling an appointment displays the cancelled appointment details and makes the slot available again.
- Rescheduling also displays the updated appointment details.
