$logFile = "C:\JobPilotAi\backend\backend-persist.log"
$jar = "C:\JobPilotAi\backend\jobpilot-bootstrap\target\jobpilot-bootstrap-1.0.0-SNAPSHOT.jar"
$p = Start-Process -FilePath "java" -ArgumentList "-jar $jar --server.port=8080" -PassThru
$p.Id | Out-File -FilePath "C:\JobPilotAi\backend\backend.pid" -Encoding utf8
Write-Output "Started PID: $($p.Id)"
