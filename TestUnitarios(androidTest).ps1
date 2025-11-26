Write-Host "Ejecutando pruebas de UI (androidTest)..." -ForegroundColor Cyan
 # Ejecuta las pruebas de UI instrumentadas
 .\gradlew connectedAndroidTest
 # Si todo se ejecuta correctamente, abre el reporte HTML
 if ($LASTEXITCODE -eq 0) {
    $reportPath = "app\build\reports\androidTests\connected\index.html"
    if (Test-Path $reportPath) {
        Write-Host "Pruebas de UI completadas. Abriendo reporte en el navegador..." -ForegroundColor Green
        Start-Process $reportPath
    }
    else {
        Write-Host "No se encontró el archivo de reporte en: $reportPath" -ForegroundColor Yellow
    }
 }
 else {
    Write-Host "Ocurrió un error al ejecutar las pruebas de UI." -ForegroundColor Red
 }