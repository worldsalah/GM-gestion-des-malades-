@echo off
title App Médecin - Installation Automatique
color 0A

echo ========================================
echo   Installation de App Médecin
echo ========================================
echo.

:: Check if running as administrator
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo [ERREUR] Ce script doit être exécuté en tant qu'administrateur!
    echo Clic droit sur le fichier et "Exécuter en tant qu'administrateur"
    pause
    exit /b 1
)

echo [INFO] Vérification des prérequis...

:: Check Java
java -version >nul 2>&1
if %errorLevel% neq 0 (
    echo [INFO] Java n'est pas installé. Installation de Java Runtime...
    if not exist "jre-installer.exe" (
        echo [ERREUR] Fichier d'installation Java introuvable!
        pause
        exit /b 1
    )
    start /wait jre-installer.exe /s
    echo [INFO] Java Runtime installé avec succès.
) else (
    echo [INFO] Java Runtime est déjà installé.
)

:: Check MySQL
mysql --version >nul 2>&1
if %errorLevel% neq 0 (
    echo [INFO] MySQL n'est pas installé. Installation de MySQL Server...
    if not exist "mysql-installer.msi" (
        echo [ERREUR] Fichier d'installation MySQL introuvable!
        pause
        exit /b 1
    )
    start /wait msiexec /i mysql-installer.msi /quiet ADDLOCAL=ALL
    echo [INFO] MySQL Server installé avec succès.
    
    :: Wait for MySQL service to start
    timeout /t 30 /nobreak
    
    :: Set root password (you should modify this for production)
    echo [INFO] Configuration du mot de passe MySQL root...
    mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'SALAH123';"
) else (
    echo [INFO] MySQL Server est déjà installé.
)

:: Create application directory
if not exist "%PROGRAMFILES%\App Médecin" (
    mkdir "%PROGRAMFILES%\App Médecin"
)

:: Copy application files
echo [INFO] Copie des fichiers de l'application...
copy "AppMedecin.jar" "%PROGRAMFILES%\App Médecin\" /Y
copy "app-icon.ico" "%PROGRAMFILES%\App Médecin\" /Y
copy "database.sql" "%PROGRAMFILES%\App Médecin\" /Y

:: Import database
echo [INFO] Importation de la base de données...
mysql -u root -pSALAH123 -e "CREATE DATABASE IF NOT EXISTS appdb; USE appdb; SOURCE \"%PROGRAMFILES%\App Médecin\database.sql\";" 2>nul
if %errorLevel% equ 0 (
    echo [SUCCÈS] Base de données importée avec succès!
) else (
    echo [AVERTISSEMENT] Erreur lors de l'importation de la base de données. Vérifiez manuellement.
)

:: Create desktop shortcut
echo [INFO] Création du raccourci bureau...
powershell "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%PUBLIC%\Desktop\App Médecin.lnk'); $Shortcut.TargetPath = '%PROGRAMFILES%\App Médecin\AppMedecin.jar'; $Shortcut.IconLocation = '%PROGRAMFILES%\App Médecin\app-icon.ico'; $Shortcut.Save()"

:: Create Start Menu shortcut
echo [INFO] Création du raccourci menu Démarrer...
if not exist "%APPDATA%\Microsoft\Windows\Start Menu\Programs\App Médecin" (
    mkdir "%APPDATA%\Microsoft\Windows\Start Menu\Programs\App Médecin"
)
powershell "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%APPDATA%\Microsoft\Windows\Start Menu\Programs\App Médecin\App Médecin.lnk'); $Shortcut.TargetPath = '%PROGRAMFILES%\App Médecin\AppMedecin.jar'; $Shortcut.IconLocation = '%PROGRAMFILES%\App Médecin\app-icon.ico'; $Shortcut.Save()"

:: Add to PATH (optional)
setx PATH "%PATH%;%PROGRAMFILES%\App Médecin" /M

echo.
echo ========================================
echo   Installation Terminée!
echo ========================================
echo.
echo L'application App Médecin a été installée avec succès.
echo.
echo Raccourcis créés:
echo   - Bureau: App Médecin
echo   - Menu Démarrer: App Médecin
echo.
echo Base de données: appdb
echo Mot de passe MySQL root: SALAH123
echo.
pause
