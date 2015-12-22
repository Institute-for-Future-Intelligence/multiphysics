#expr Exec("C:\Program Files (x86)\Windows Kits\8.0\bin\x64\signtool.exe", "sign /n Concord /tr http://tsa.starfieldtech.com " + AddBackslash(SourcePath) + "Energy2D\Energy2D.exe")

;This file will be executed next to the application bundle image
;I.e. current directory will contain folder Energy3D with application files
[Setup]
AppId={{org.concord.energy2d}}
AppName=Energy2D
AppVersion=2.4
AppVerName=Energy2D
AppPublisher=Concord Consortium Inc.
AppComments=Energy2D
AppCopyright=2010-2016 Concord Consortium Inc.
AppPublisherURL=http://energy.concord.org/energy2d/
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
DefaultDirName={localappdata}\Energy2D
DisableStartupPrompt=Yes
DisableDirPage=Yes
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=Yes
DefaultGroupName=Energy2D
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,6.0 
OutputBaseFilename=energy2d
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=Energy2D\Energy2D.ico
UninstallDisplayIcon={app}\Energy2D.ico
UninstallDisplayName=Energy2D
WizardImageStretch=No
WizardSmallImageFile=Energy2D-setup-icon.bmp   
ChangesAssociations=yes
SignTool=mysign

[Registry]
Root: HKCU; Subkey: "Software\Classes\.e2d"; ValueType: string; ValueName: ""; ValueData: "Energy2DFile"; Flags: uninsdeletevalue
Root: HKCU; Subkey: "Software\Classes\Mime\Database\Content Type\application/energy2d"; ValueType: string; ValueName: "Extension"; ValueData: ".e2d"; Flags: uninsdeletevalue
Root: HKCU; Subkey: "Software\Classes\Energy2DFile"; ValueType: string; ValueName: ""; ValueData: "Energy2D File"; Flags: uninsdeletekey
Root: HKCU; Subkey: "Software\Classes\Energy2DFile\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\Energy2D"" ""%1"""


[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "Energy2D\Energy2D.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "Energy2D\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\Energy2D"; Filename: "{app}\Energy2D.exe"; IconFilename: "{app}\Energy2D.ico"; Check: returnTrue()
Name: "{commondesktop}\Energy2D"; Filename: "{app}\Energy2D.exe";  IconFilename: "{app}\Energy2D.ico"; Check: returnTrue()


[Run]
Filename: "{app}\Energy2D.exe"; Description: "{cm:LaunchProgram,Energy2D}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\Energy2D.exe"; Parameters: "-install -svcName ""Energy2D"" -svcDesc ""Energy2D"" -mainExe ""Energy2D.exe""  "; Check: returnFalse()

[UninstallRun]
Filename: "{app}\Energy2D.exe "; Parameters: "-uninstall -svcName Energy2D -stopOnUninstall"; Check: returnFalse()

[UninstallDelete]
Type: files; Name: "{app}\app\gettingdown.lock"
Type: files; Name: "{app}\app\launcher.log"
Type: files; Name: "{app}\app\proxy.txt"
Type: files; Name: "{app}\app\*.jarv"
Type: files; Name: "{app}\app\lib\*.jarv"

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  
