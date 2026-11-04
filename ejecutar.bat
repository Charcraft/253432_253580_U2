@echo off
chcp 65001 > nul
setlocal

cd /d "%~dp0"

echo Compilando el proyecto...
if not exist build mkdir build
javac -encoding UTF-8 -d build src\*.java
if errorlevel 1 (
    echo.
    echo Ocurrio un error al compilar. Revise que el JDK este instalado.
    pause
    exit /b 1
)

echo Ejecutando Gestion de Asientos en el Aula...
java -cp build GestionAsientosAula
