# Script PowerShell para ejecutar el proyecto
# Ejecuta la aplicación Spring Boot

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Iniciando aplicación Spring Boot" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "La aplicación estará disponible en:" -ForegroundColor Yellow
Write-Host "  http://localhost:8080" -ForegroundColor White
Write-Host ""
Write-Host "Endpoints disponibles:" -ForegroundColor Yellow
Write-Host "  GET http://localhost:8080/api/ok" -ForegroundColor White
Write-Host "  GET http://localhost:8080/api/lento" -ForegroundColor White
Write-Host "  GET http://localhost:8080/api/lento?shouldFail=true" -ForegroundColor White
Write-Host ""
Write-Host "Presiona Ctrl+C para detener" -ForegroundColor Gray
Write-Host ""

.\mvnw.cmd spring-boot:run
