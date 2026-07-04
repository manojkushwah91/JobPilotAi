$jar = "C:\JobPilotAi\backend\jobpilot-bootstrap\target\jobpilot-bootstrap-1.0.0-SNAPSHOT.jar"
$logFile = "C:\JobPilotAi\backend\backend-startup.log"
$errFile = "C:\JobPilotAi\backend\backend-startup.err"
$p = Start-Process -FilePath "java" -ArgumentList "-jar $jar --server.port=8080" -RedirectStandardOutput $logFile -RedirectStandardError $errFile -PassThru
$p.Id | Out-File "C:\JobPilotAi\backend\backend.pid"

$maxWait = 90
$waited = 0
while ($waited -lt $maxWait) {
    Start-Sleep -Seconds 5
    $waited += 5
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 5
        if ($r.StatusCode -eq 200) {
            Write-Output "READY after ${waited}s"
            exit 0
        }
    } catch {
        if ($_.Exception.Response.StatusCode -ne 503) {
            Write-Output "UNEXPECTED: $_"
        }
    }
}
Write-Output "TIMEOUT after ${waited}s"
Get-Content $logFile -Tail 30
exit 1
