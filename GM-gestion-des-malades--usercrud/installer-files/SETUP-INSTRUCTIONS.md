# 📦 App Médecin - Installation Setup Instructions

## 🎯 What You Need to Place in `installer-files/` Directory

### **📋 Required Files:**
```
installer-files/
├── AppMedecin.jar        # ✅ Your compiled Java application
├── database.sql          # ✅ MySQL database dump
├── app-icon.ico          # 🎨 Your custom icon (optional)
├── app-mecin-setup.iss   # ✅ Inno Setup script (already created)
├── install.bat           # ✅ Auto-install script (already created)
├── uninstall.bat         # ✅ Uninstall script (already created)
├── build-installer.bat   # ✅ Build script (already created)
├── README-INSTALLATION.md # ✅ User documentation (already created)
├── BUILD-GUIDE.md       # ✅ Developer guide (already created)
└── compiler/
    └── Languages/
        └── French.isl      # ✅ French language pack (already created)
```

## 🚀 Quick Start Steps

### **Step 1: Prepare Your Files**
1. **Copy `AppMedecin.jar`** - Your compiled Java application
2. **Copy `database.sql`** - Your MySQL database file  
3. **Add `app-icon.ico`** - Your custom icon (optional but recommended)

### **Step 2: Run Build Script**
```bash
cd installer-files
build-installer.bat
```

### **Step 3: Choose Installation Method**

#### **Method A: Automatic Build (Recommended)**
- The script will detect if Inno Setup is installed
- If found, it will automatically compile `AppMedecin-Setup.exe`
- Result: Professional Windows installer ready for distribution

#### **Method B: Manual Build**
1. Install Inno Setup: https://jrsoftware.org/inno-setup.php
2. Run: `iscc app-mecin-setup.iss`
3. Result: `AppMedecin-Setup.exe` in installer-files directory

## 🎯 What the Build Script Does

### **✅ File Verification**
- Checks for `AppMedecin.jar` (required)
- Checks for `database.sql` (required)  
- Checks for `app-icon.ico` (optional)
- Reports missing files with clear error messages

### **✅ Smart Icon Handling**
- If `app-icon.ico` exists: Uses your custom icon
- If missing: Uses default Inno Setup icon
- Updates version info accordingly

### **✅ Complete Package Creation**
- Creates `dist/` folder with all necessary files
- Copies language files (French.isl)
- Generates version information
- Creates download links file
- Builds package metadata

### **✅ Auto-Compilation (Optional)**
- Detects Inno Setup installation
- Offers to compile installer automatically
- Creates `AppMedecin-Setup.exe` if successful

## 🎨 Adding Your Custom Icon

### **Icon Requirements:**
- **Format**: `.ico` file
- **Size**: Multiple sizes embedded (16x16, 32x32, 48x48, 256x256)
- **Recommended**: Use online converter like https://convertico.com/

### **Icon Placement:**
1. Save your icon as `app-icon.ico`
2. Place in `installer-files/` directory
3. Run `build-installer.bat`
4. Icon will be automatically included

## 📦 Final Output

### **After Running Build Script:**
```
dist/
├── AppMedecin.jar          # Your application
├── app-icon.ico            # Your icon (if provided)
├── database.sql             # Your database
├── app-mecin-setup.iss      # Inno Setup script
├── install.bat             # Auto-install script
├── uninstall.bat           # Uninstall script
├── compiler/
│   └── Languages/
│       └── French.isl      # French language pack
├── DOWNLOAD-LINKS.txt      # Required downloads
├── VERSION.txt             # Version information
└── package-info.json       # Package metadata
```

### **If Inno Setup Available:**
- **`AppMedecin-Setup.exe`** - Professional Windows installer
- **Size**: ~50MB (with bundled dependencies)
- **Ready for distribution**

## 🔧 Troubleshooting

### **"AppMedecin.jar not found"**
- Compile your Java application first
- Ensure the `.jar` file is in `installer-files/` directory

### **"Icon not found"**
- Icon is optional - installer will use default
- To add custom icon: save as `app-icon.ico` in `installer-files/`

### **"Inno Setup not found"**
- Download and install: https://jrsoftware.org/inno-setup.php
- Or use the `install.bat` method instead

### **Compilation fails**
- Check all required files are present
- Verify icon format is valid `.ico`
- Ensure Inno Setup is properly installed

## 🎉 Success!

When you see:
```
[SUCCÈS] AppMedecin-Setup.exe créé avec succès!
```

Your professional Windows installer is ready for distribution!

### **What Users Get:**
- 🇫🇷 Complete French installation experience
- 🔧 Automatic Java and MySQL setup
- 🗄️ Database auto-import
- 🖥️ Desktop shortcuts
- 🎯 Your custom icon
- 📦 Professional installer package

Your App Médecin is now ready for Windows distribution! 🚀
