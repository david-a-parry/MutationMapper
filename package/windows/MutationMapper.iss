;This file will be executed next to the application bundle image
;I.e. current directory will contain folder MutationMapper with application files
[Setup]
AppId={{MutationMapper}}
AppName=MutationMapper
AppVersion=2.0
AppVerName=MutationMapper 2.0
AppPublisher=David A. Parry
AppComments=MutationMapper
AppCopyright=Copyright (C) 2015
;AppPublisherURL=http://java.com/
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
DefaultDirName={localappdata}\MutationMapper
DisableStartupPrompt=Yes
DisableDirPage=Yes
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=Yes
DefaultGroupName=MutationMapper
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=MutationMapper-2.0
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=MutationMapper\MutationMapper.ico
UninstallDisplayIcon={app}\MutationMapper.ico
UninstallDisplayName=MutationMapper
WizardImageStretch=No
WizardSmallImageFile=MutationMapper-setup-icon.bmp   
ArchitecturesInstallIn64BitMode=x64

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "MutationMapper\MutationMapper.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "MutationMapper\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\MutationMapper"; Filename: "{app}\MutationMapper.exe"; IconFilename: "{app}\MutationMapper.ico"; Check: returnTrue()
Name: "{commondesktop}\MutationMapper"; Filename: "{app}\MutationMapper.exe";  IconFilename: "{app}\MutationMapper.ico"; Check: returnFalse()

[Run]
Filename: "{app}\MutationMapper.exe"; Description: "{cm:LaunchProgram,MutationMapper}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\MutationMapper.exe"; Parameters: "-install -svcName ""MutationMapper"" -svcDesc ""MutationMapper"" -mainExe ""MutationMapper.exe""  "; Check: returnFalse()

[UninstallRun]
Filename: "{app}\MutationMapper.exe "; Parameters: "-uninstall -svcName MutationMapper -stopOnUninstall"; Check: returnFalse()

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
