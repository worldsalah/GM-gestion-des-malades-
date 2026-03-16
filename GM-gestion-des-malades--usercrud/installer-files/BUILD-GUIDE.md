# App Médecin - Windows Installer Build Guide

## 📦 Overview

This guide provides complete instructions to create a professional Windows installer for your Java application with automatic dependency management and French language support.

## 🎯 Features Included

✅ **Automatic Java Installation** - Detects and installs Java 17 if missing  
✅ **Automatic MySQL Installation** - Installs MySQL Server 8.0 if not present  
✅ **Database Auto-Import** - Automatically imports `appdb` database  
✅ **French Interface** - Complete French language support throughout installer  
✅ **Desktop Shortcuts** - Creates desktop and Start Menu shortcuts  
✅ **Professional Installer** - Uses Inno Setup for Windows compliance  
✅ **Uninstaller** - Complete removal with database cleanup option  
✅ **UAC Support** - Proper Windows elevation handling  
✅ **Registry Management** - Clean Windows registry integration  

---

## 📁 File Structure Created

```
installer-files/
├── app-mecin-setup.iss          # Main Inno Setup script
├── install.bat                    # Automatic installation script
├── uninstall.bat                  # Uninstallation script  
├── build-installer.bat            # Build preparation script
├── README-INSTALLATION.md         # Complete installation guide
├── compiler/
│   └── Languages/
│       └── French.isl              # French language pack
├── DOWNLOAD-LINKS.txt            # Required download links
├── VERSION.txt                   # Version information
├── package-info.json             # Package metadata
└── dist/                        # Distribution folder (created by build script)
    ├── AppMedecin.jar
    ├── app-icon.ico
    ├── database.sql
    ├── install.bat
    └── uninstall.bat
```

---

## 🚀 Build Instructions

### Method 1: Quick Build (Automatic Script)

1. **Navigate to installer-files directory**:
   ```bash
   cd installer-files
   ```

2. **Run the build script**:
   ```bash
   build-installer.bat
   ```

3. **Result**: Creates `dist/` folder with all necessary files

### Method 2: Professional Build (Inno Setup)

1. **Install Inno Setup**:
   - Download: https://jrsoftware.org/inno-setup.php
   - Install with French language support

2. **Compile the installer**:
   ```bash
   iscc app-mecin-setup.iss
   ```

3. **Output**: `AppMedecin-Setup.exe` in installer-files directory

---

## 📋 Prerequisites for End Users

### Required Downloads (Before Running Installer)

1. **Java Runtime Environment 17**:
   - URL: https://download.oracle.com/java/17/latest/jre-17-windows-x64.exe
   - File: `jre-installer.exe`

2. **MySQL Server 8.0**:
   - URL: https://dev.mysql.com/get/Downloads/MySQL-Installer/mysql-8.0.33-winx64.msi
   - File: `mysql-installer.msi`

3. **Application Files**:
   - `AppMedecin.jar` (your compiled application)
   - `app-icon.ico` (application icon)
   - `database.sql` (database schema)

---

## 🔧 Installer Features

### Database Configuration
- **Database Name**: `appdb`
- **Default User**: `root`
- **Default Password**: `SALAH123`
- **Auto-Import**: Yes (from `database.sql`)

### Installation Paths
- **Application**: `C:\Program Files\App Médecin`
- **Shortcuts**: Desktop + Start Menu
- **JRE Bundle**: Bundled with installer
- **Logs**: `%PROGRAMFILES%\App Médecin\logs\`

### French Language Support
- **Installer Interface**: 100% French
- **Error Messages**: French
- **Dialog Boxes**: French
- **Button Labels**: French
- **Progress Messages**: French

---

## 🧪 Testing the Installer

### Pre-Installation Testing
1. **Clean VM**: Start with fresh Windows VM
2. **No Java/MySQL**: Ensure neither is installed
3. **Run Installer**: Execute `AppMedecin-Setup.exe`
4. **Verify All Features**:
   - ✅ Java auto-installation works
   - ✅ MySQL auto-installation works
   - ✅ Database import succeeds
   - ✅ Shortcuts created
   - ✅ Application launches correctly

### Post-Installation Testing
1. **Launch Application**: Use desktop shortcut
2. **Test Login**: Use `hanen`/`SALAH` credentials
3. **Verify French Interface**: All UI elements in French
4. **Test Database**: Patient creation, appointments
5. **Test Uninstall**: Clean removal option

---

## 📦 Distribution Package

### Final Package Contents
```
App-Medecin-Setup.exe    # Main installer (recommended for users)
install.bat              # Alternative automatic install
app-mecin-setup.iss     # Source script for customization
README-INSTALLATION.md   # User documentation
```

### Distribution Methods

#### Method 1: Single Executable (Recommended)
- **File**: `AppMedecin-Setup.exe`
- **Size**: ~50MB (with bundled JRE)
- **Features**: Complete self-contained installer
- **Usage**: Download and double-click

#### Method 2: Script-Based Installation
- **Files**: `install.bat` + dependencies
- **Size**: ~15MB (without JRE/MySQL)
- **Features**: Manual dependency management
- **Usage**: Run as administrator

---

## 🔒 Security Considerations

### Installation Security
- ✅ **UAC Elevation**: Proper admin rights handling
- ✅ **Code Signing**: Recommended for distribution
- ✅ **Path Validation**: Prevents directory traversal
- ✅ **Service Management**: Safe MySQL service handling

### Database Security
- 🔐 **Default Password**: Change `SALAH123` in production
- 🔐 **Root Access**: Installer uses root for setup only
- 🔐 **Local Access**: Database configured for localhost only

---

## 📚 Advanced Customization

### Modifying app-mecin-setup.iss
```pascal
// Change application info
#define MyAppName "Your App Name"
#define MyAppVersion "1.0.0"

// Modify installation paths
DefaultDirName="YourAppFolder"

// Customize database settings
mysql -u root -pYOUR_PASSWORD -e "CREATE DATABASE your_db;"
```

### Adding New Languages
1. **Copy French.isl** to new language file
2. **Translate all messages** to target language
3. **Update script** to include new language
4. **Recompile installer**

---

## 🚨 Troubleshooting

### Common Build Issues

#### Inno Setup Compilation Errors
```bash
# Check Inno Setup installation
iscc

# Verify script syntax
iscc /Q app-mecin-setup.iss

# Check for missing files
dir /b
```

#### Runtime Errors
- **Java Detection**: Verify Java installation check logic
- **MySQL Service**: Check MySQL service startup timing
- **Permission Issues**: Ensure admin rights

### Debug Mode
```bash
# Enable verbose logging
iscc /V app-mecin-setup.iss

# Test specific sections
iscc /DTEST_MODE app-mecin-setup.iss
```

---

## 📈 Deployment Strategy

### Release Channels
1. **GitHub Releases**: Primary distribution channel
2. **Direct Download**: For enterprise deployments
3. **CDN Distribution**: For large-scale deployment

### Version Management
- **Semantic Versioning**: `1.0.0` format
- **Build Numbers**: Auto-increment
- **Update Notifications**: Built-in update checker

---

## ✅ Success Criteria

Your installer is complete when:
- [x] Compiles without errors
- [x] Installs on clean Windows system
- [x] Automatically handles dependencies
- [x] Creates working shortcuts
- [x] Imports database correctly
- [x] Application launches successfully
- [x] Uninstaller works completely
- [x] All text is in French
- [x] No manual configuration required

---

## 🎉 Final Result

You now have a **professional Windows installer** for your Java application that:
- 🇫🇷 **Speaks French** throughout the entire process
- 🔧 **Handles Dependencies** automatically (Java/MySQL)
- 🗄️ **Manages Database** setup and import
- 🎯 **Creates Shortcuts** for easy access
- 🔒 **Follows Windows** security best practices
- 📦 **Produces Professional** installer package

Your users can now download a single file and have a fully configured French medical application!
