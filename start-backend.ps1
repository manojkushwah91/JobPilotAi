$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:SPRING_PROFILES_ACTIVE = "dev"
Set-Location "C:\JobPilotAi\backend"
& mvn.cmd spring-boot:run -pl jobpilot-bootstrap 2>&1 | Tee-Object -FilePath "C:\JobPilotAi\backend.log"
