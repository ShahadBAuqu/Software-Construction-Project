# Smart Task Manager

## Project Idea
This project is a simple task manager, but with an extra idea which is considering the weather.  
The system helps the user decide if a task is suitable or not based on weather conditions.

---

## What does the system do?
- Add tasks  
- View tasks  
- Check if a task is suitable depending on the weather  
- Has a GUI using Swing  

---

## How does it work?
- The user adds a task  
- When clicking "Update Weather"  
- The system gets weather data (currently simulated)  
- Then it shows:
  - SAFE  if everything is okay  
  - RISKY  if the weather may affect the task  

---

## How do we decide SAFE or RISKY?
If the task is weatherSensitive = true  
and the precipitation probability is high (> 0.6)  

→ The task is marked as RISKY  

Otherwise → SAFE  

---

## Reactive Programming (Mono)
We used Mono from Reactor to handle operations asynchronously,  
like fetching weather data without blocking the program.

---

## How to run the application
1. Open the project in VS Code  
2. Run MainApp.java  

Or using terminal:

1- mvn clean install 
2- mvn exec:java "-Dexec.mainClass=taskmanager.api.MainApp" 

---

## API Key
Currently, we are not using a real weather API.  
The weather values are simulated in the code.

---

## Example usage
TaskManager tm = TaskManager.builder()         .withWeatherApiKey("YOUR_API_KEY_HERE")         
.build();  

tm.addTask(new Task("1", "Study", LocalDateTime.now(), false)); 

---

## Notes
- We separated API and implementation  
- We used custom exceptions  
- We used Reactor and threading with Swing  

---

## Team Members
- Mirad Alqarhi  
- Shahad Aloufi
- ⁠Ruba Baz