# Script completo: Compila y ejecuta el proyecto

Write-Host "========================================" -ForegroundColor Magenta
Write-Host "  Setup completo del proyecto" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

# Compilar
Write-Host "[1/2] Compilando proyecto..." -ForegroundColor Cyan
.\mvnw.cmd clean install -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "[2/2] Iniciando aplicación..." -ForegroundColor Cyan
    Write-Host ""
    .\mvnw.cmd spring-boot:run
} else {
    Write-Host ""
    Write-Host "ERROR: La compilación falló" -ForegroundColor Red
    Write-Host "Revisa los errores arriba" -ForegroundColor Red
    exit 1
}
