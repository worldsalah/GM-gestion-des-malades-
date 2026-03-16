@echo off
setlocal enabledelayedexpansion
title App Médecin - Build Script pour Installateur Windows
color 0A

echo ========================================
echo   Préparation de l'installateur App Médecin
echo ========================================

:: Check if required files exist
echo [INFO] Vérification des fichiers requis...

if not exist "AppMedecin.jar" (
    echo [ERREUR] AppMedecin.jar introuvable!
    echo Veuillez compiler votre application Java et placer le fichier ici.
    pause
    exit /b 1
)

if not exist "database.sql" (
    echo [ERREUR] database.sql introuvable!
    echo Veuillez copier votre script SQL ici.
    pause
    exit /b 1
)

if not exist "app-icon.ico" (
    echo [AVERTISSEMENT] app-icon.ico introuvable!
    echo L'installateur utilisera l'icône par défaut.
    set ICON_AVAILABLE=NO
) else (
    set ICON_AVAILABLE=YES
    echo [INFO] Icône app-icon.ico trouvée.
)

echo [INFO] Création du dossier de distribution...
if not exist "dist" mkdir dist

:: Copy main files
echo [INFO] Copie des fichiers principaux...
copy "AppMedecin.jar" "dist\" /Y
copy "database.sql" "dist\" /Y
copy "app-mecin-setup.iss" "dist\" /Y
copy "install.bat" "dist\" /Y
copy "uninstall.bat" "dist\" /Y

:: Copy icon if available
if "%ICON_AVAILABLE%"=="YES" (
    copy "app-icon.ico" "dist\" /Y
    echo [INFO] Icône copiée vers le dossier de distribution.
)

:: Copy language files
echo [INFO] Copie des fichiers de langue...
if not exist "dist\compiler" mkdir "dist\compiler"
if not exist "dist\compiler\Languages" mkdir "dist\compiler\Languages"
copy "compiler\Languages\French.isl" "dist\compiler\Languages\" /Y

:: Create JRE bundle directory
echo [INFO] Préparation du JRE...
if not exist "dist\jre" mkdir "dist\jre"

:: Download links information
echo [INFO] Création du fichier d'informations de téléchargement...
echo # Liens de téléchargement requis pour App Médecin > "dist\DOWNLOAD-LINKS.txt"
echo. >> "dist\DOWNLOAD-LINKS.txt"
echo # Java Runtime Environment (JRE 17) >> "dist\DOWNLOAD-LINKS.txt"
echo https://download.oracle.com/java/17/latest/jre-17-windows-x64.exe >> "dist\DOWNLOAD-LINKS.txt"
echo. >> "dist\DOWNLOAD-LINKS.txt"
echo # MySQL Server 8.0.33 >> "dist\DOWNLOAD-LINKS.txt"
echo https://dev.mysql.com/get/Downloads/MySQL-Installer/mysql-8.0.33-winx64.msi >> "dist\DOWNLOAD-LINKS.txt"
echo. >> "dist\DOWNLOAD-LINKS.txt"
echo # Inno Setup (pour compiler l'installateur professionnel) >> "dist\DOWNLOAD-LINKS.txt"
echo https://jrsoftware.org/inno-setup.php >> "dist\DOWNLOAD-LINKS.txt"

:: Create version info
echo [INFO] Création des informations de version...
echo App Médecin > "dist\VERSION.txt"
echo Version: 1.0.0 >> "dist\VERSION.txt"
echo Build Date: %date% %time% >> "dist\VERSION.txt"
echo Language: French >> "dist\VERSION.txt"
if "%ICON_AVAILABLE%"=="YES" (
    echo Icon: app-icon.ico >> "dist\VERSION.txt"
) else (
    echo Icon: Default >> "dist\VERSION.txt"
)

:: Create package info
echo [INFO] Création du package d'information...
echo { > "dist\package-info.json"
echo   "name": "App Médecin", >> "dist\package-info.json"
echo   "version": "1.0.0", >> "dist\package-info.json"
echo   "description": "Application médicale française pour la gestion des patients et rendez-vous", >> "dist\package-info.json"
echo   "author": "GM Medical", >> "dist\package-info.json"
echo   "requires_java": "17", >> "dist\package-info.json"
echo   "requires_mysql": "8.0", >> "dist\package-info.json"
echo   "database_name": "appdb", >> "dist\package-info.json"
echo   "default_user": "hanen", >> "dist\package-info.json"
echo   "default_password": "SALAH", >> "dist\package-info.json"
echo   "icon_available": "%ICON_AVAILABLE%" >> "dist\package-info.json"
echo } >> "dist\package-info.json"

:: Check if Inno Setup is available
echo [INFO] Vérification de Inno Setup...
iscc >nul 2>&1
if %errorLevel% equ 0 (
    echo [SUCCÈS] Inno Setup trouvé!
    echo.
    set /p compile_now="Compiler l'installateur maintenant? (O/N): "
    if /i "!compile_now!"=="O" (
        echo [INFO] Compilation de l'installateur...
        cd dist
        iscc app-mecin-setup.iss
        if %errorLevel% equ 0 (
            echo [SUCCÈS] AppMedecin-Setup.exe créé avec succès!
        ) else (
            echo [ERREUR] Erreur lors de la compilation!
        )
        cd ..
    )
) else (
    echo [AVERTISSEMENT] Inno Setup non trouvé!
    echo Veuillez installer Inno Setup depuis: https://jrsoftware.org/inno-setup.php
)

echo.
echo ========================================
echo   Préparation Terminée!
echo ========================================
echo.
echo Fichiers prêts dans le dossier 'dist':
if "%ICON_AVAILABLE%"=="YES" (
    echo   - AppMedecin.jar (application)
    echo   - app-icon.ico (icône)
) else (
    echo   - AppMedecin.jar (application)
    echo   - (icône par défaut)
)
echo   - database.sql (base de données)
echo   - install.bat (installation automatique)
echo   - uninstall.bat (désinstallation)
echo   - app-mecin-setup.iss (script Inno Setup)
echo   - French.isl (pack de langue française)
echo   - DOWNLOAD-LINKS.txt (liens de téléchargement)
echo   - VERSION.txt (informations de version)
echo   - package-info.json (métadonnées du package)
echo.
echo Prochaines étapes:
echo   1. Télécharger jre-installer.exe et mysql-installer.msi
echo   2. Placer dans le dossier 'dist'
echo   3. Exécuter install.bat pour l'installation automatique
echo   4. OU compiler app-mecin-setup.iss avec Inno Setup
echo.
pause
