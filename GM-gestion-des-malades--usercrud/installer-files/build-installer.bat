# App Médecin - Build Script pour Installateur Windows

echo ========================================
echo   Préparation de l'installateur App Médecin
echo ========================================

:: Check if required files exist
if not exist "AppMedecin.jar" (
    echo [ERREUR] AppMedecin.jar introuvable!
    exit /b 1
)

if not exist "app-icon.ico" (
    echo [ERREUR] app-icon.ico introuvable!
    exit /b 1
)

if not exist "database.sql" (
    echo [ERREUR] database.sql introuvable!
    exit /b 1
)

echo [INFO] Création du dossier de distribution...
if not exist "dist" mkdir dist

:: Copy main files
echo [INFO] Copie des fichiers principaux...
copy "AppMedecin.jar" "dist\" /Y
copy "app-icon.ico" "dist\" /Y
copy "database.sql" "dist\" /Y
copy "install.bat" "dist\" /Y
copy "uninstall.bat" "dist\" /Y

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

:: Create package info
echo [INFO] Création du package d'information...
echo { >> "dist\package-info.json"
echo   "name": "App Médecin", >> "dist\package-info.json"
echo   "version": "1.0.0", >> "dist\package-info.json"
echo   "description": "Application médicale française pour la gestion des patients et rendez-vous", >> "dist\package-info.json"
echo   "author": "GM Medical", >> "dist\package-info.json"
echo   "requires_java": "17", >> "dist\package-info.json"
echo   "requires_mysql": "8.0", >> "dist\package-info.json"
echo   "database_name": "appdb", >> "dist\package-info.json"
echo   "default_user": "hanen", >> "dist\package-info.json"
echo   "default_password": "SALAH" >> "dist\package-info.json"
echo } >> "dist\package-info.json"

echo.
echo ========================================
echo   Préparation Terminée!
echo ========================================
echo.
echo Fichiers prêts dans le dossier 'dist':
echo   - AppMedecin.jar (application)
echo   - app-icon.ico (icône)
echo   - database.sql (base de données)
echo   - install.bat (installation automatique)
echo   - uninstall.bat (désinstallation)
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
