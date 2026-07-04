$logFile = "C:\JobPilotAi\backend.log"
& "C:\Program Files\Java\jdk-17\bin\java.exe" -jar "C:\JobPilotAi\backend\jobpilot-bootstrap\target\jobpilot-bootstrap-1.0.0-SNAPSHOT.jar" --spring.profiles.active=dev > $logFile 2>&1
