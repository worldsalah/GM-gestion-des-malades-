@echo off
title App Médecin - Désinstallation
color 0C

echo ========================================
echo   Désinstallation de App Médecin
echo ========================================
echo.

:: Check if running as administrator
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo [ERREUR] Ce script doit être exécuté en tant qu'administrateur!
    pause
    exit /b 1
)

echo [INFO] Arrêt de l'application...

:: Force close application
taskkill /f /im javaw.exe >nul 2>&1

:: Remove desktop shortcuts
echo [INFO] Suppression des raccourcis bureau...
if exist "%PUBLIC%\Desktop\App Médecin.lnk" (
    del "%PUBLIC%\Desktop\App Médecin.lnk"
    echo [SUCCÈS] Raccourci bureau supprimé.
)

:: Remove start menu shortcuts
echo [INFO] Suppression des raccourcis menu Démarrer...
if exist "%APPDATA%\Microsoft\Windows\Start Menu\Programs\App Médecin" (
    rmdir /s /q "%APPDATA%\Microsoft\Windows\Start Menu\Programs\App Médecin"
    echo [SUCCÈS] Raccourcis menu Démarrer supprimés.
)

:: Ask about database
echo.
set /p delete_db="Supprimer la base de données MySQL? (O/N): "
if /i "%delete_db%"=="O" (
    echo [INFO] Suppression de la base de données appdb...
    mysql -u root -pSALAH123 -e "DROP DATABASE IF EXISTS appdb;" 2>nul
    if %errorLevel% equ 0 (
        echo [SUCCÈS] Base de données supprimée.
    ) else (
        echo [AVERTISSEMENT] Erreur lors de la suppression de la base de données.
    )
)

:: Remove application files
echo [INFO] Suppression des fichiers de l'application...
if exist "%PROGRAMFILES%\App Médecin" (
    rmdir /s /q "%PROGRAMFILES%\App Médecin"
    echo [SUCCÈS] Fichiers de l'application supprimés.
)

:: Remove from PATH
echo [INFO] Mise à jour du PATH système...
setx PATH "%PATH:C:\Program Files\App Médecin=%" /M

:: Clean registry
echo [INFO] Nettoyage du registre...
reg delete "HKLM\SOFTWARE\App Médecin" /f >nul 2>&1

echo.
echo ========================================
echo   Désinstallation Terminée!
echo ========================================
echo.
echo App Médecin a été complètement désinstallé.
echo.
pause
