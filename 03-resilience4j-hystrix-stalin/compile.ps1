# Script PowerShell para compilar el proyecto
# No necesitas instalar Maven manualmente

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Compilando proyecto Spring Boot" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar Java
Write-Host "Verificando Java..." -ForegroundColor Yellow
java -version
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Java no está instalado o no está en el PATH" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Limpiando compilación anterior..." -ForegroundColor Yellow
.\mvnw.cmd clean

Write-Host ""
Write-Host "Compilando proyecto..." -ForegroundColor Yellow
.\mvnw.cmd install -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  ✓ COMPILACIÓN EXITOSA" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Para ejecutar el proyecto, usa:" -ForegroundColor Cyan
    Write-Host "  .\run.ps1" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  ✗ ERROR EN LA COMPILACIÓN" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
}
