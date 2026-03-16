; Inno Setup Script for App Médecin
; Automatically installs Java, MySQL, imports database, and creates desktop shortcut

#define MyAppName "App Médecin"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "GM Medical"
#define MyAppURL "https://github.com/worldsalah/GM-gestion-des-malades-"
#define MyAppExeName "AppMedecin.exe"
#define MyAppAssocName "App Médecin"

[Setup]
AppId={{A5B6C3D-9F8E-4D2A-9B4E-8F5E2C1E2E3}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={#MyAppName}
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
OutputDir=installer-output
OutputBaseFilename=AppMedecin-Setup
SetupIconFile=app-icon.ico
Compression=lzma2
SolidCompression=yes
PrivilegesRequired=admin
WizardStyle=modern
ArchitecturesAllowed=x64
MinVersion=6.1sp1sp1

[Languages]
Name: "french"; MessagesFile: "compiler:Languages\French.isl"
Name: "english"; MessagesFile: "compiler:Languages\English.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:Icons}"; Flags: uncheckedonce
Name: "quicklaunchicon"; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:Icons}"; Flags: uncheckedonce; OnlyBelowVersion: 6.1

[Files]
; Application files
Source: "AppMedecin.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "app-icon.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "database.sql"; DestDir: "{app}"; Flags: ignoreversion

; Java Runtime (bundled)
Source: "jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{groupname}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon
Name: "{groupname}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: quicklaunchicon

[Run]
Filename: "{app}\jre\bin\javaw.exe"; Parameters: "-jar ""{app}\AppMedecin.jar"""; WorkingDir: "{app}"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
Type: filesandordirs; Name: "{app}"

[Registry]
Root: HKLM32; Subkey: "SOFTWARE\{#MyAppName}"; ValueType: string; ValueName: "InstallPath"; ValueData: "{app}"
Root: HKLM32; Subkey: "SOFTWARE\{#MyAppName}"; ValueType: string; ValueName: "Version"; ValueData: "{#MyAppVersion}"

[Code]
; Check if Java is installed
function InitializeSetup: Boolean;
begin
  Result := True;
end;

function NextButtonClick(CurPageID: Integer): Boolean;
begin
  if CurPageID = wpReady then
    // Install MySQL if not present
    if not RegKeyExists(HKLM32, 'SOFTWARE\MySQL AB\MySQL Server 8.0') then
      if MsgBox('MySQL n''est pas installé. Voulez-vous installer MySQL Server 8.0?', mbConfirmation, MB_YESNO) = IDYES then
        // Extract and run MySQL installer
        ExtractTemporaryFile('mysql-8.0.33-winx64.msi', '{tmp}\mysql-installer.msi');
        Exec('{tmp}\mysql-installer.msi', '/quiet ADDLOCAL=ALL', '', SW_SHOW, ewWaitUntilTerminated);
      end;
    end;
    
    // Import database
    if MsgBox('Importer la base de données MySQL?', mbConfirmation, MB_YESNO) = IDYES then
      if RegKeyExists(HKLM32, 'SOFTWARE\MySQL AB\MySQL Server 8.0') then
        // Get MySQL installation path
        RegQueryStringValue(HKLM32, 'SOFTWARE\MySQL AB\MySQL Server 8.0', 'Location', MySQLPath);
        
        // Import database using MySQL command line
        Exec(AddBackslash(MySQLPath) + 'bin\mysql.exe', 
             '-u root -p -e "CREATE DATABASE IF NOT EXISTS appdb; USE appdb; SOURCE {app}\database.sql;"', 
             '', SW_HIDE, ewWaitUntilTerminated);
        
        MsgBox('Base de données importée avec succès!', mbInformation, MB_OK);
      end;
    end;
  end;
  
  Result := True;
end;

function AddBackslash(const S: string): string;
begin
  if Copy(S, Length(S), 1) <> '\' then
    Result := S + '\'
  else
    Result := S;
end;
