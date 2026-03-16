# French Language File for Inno Setup

; Custom French messages for App Médecin installer
; Save this as: compiler:Languages\French.isl

[LangOptions]
LanguageName=French
LanguageID=$040C
LanguageCode=fr
DialogFontName=MS Shell Dlg
DialogFontSize=8
WelcomeFontName=Arial
WelcomeFontSize=12
TitleFontName=Arial
TitleFontSize=12
CopyrightFontName=Arial
CopyrightFontSize=8

[Messages]
; Common
WelcomeLabel1=Bienvenue dans l'assistant d'installation de [name/ver]
WelcomeLabel2=Cet assistant va installer [name/ver] sur votre ordinateur.%n%n%nIl est recommandé de fermer toutes les autres applications avant de continuer.
WinVersionTooLowError=Cette version de [name] nécessite Windows [winvermin] ou supérieure.
Win64Required=Cette version de [name] nécessite une version 64-bit de Windows.
AdminPrivilegesRequired=Vous devez avoir des privilèges d'administrateur pour installer [name].
PowerShellNotInstalled=Cette version de [name] nécessite Windows PowerShell 5.1 ou supérieure.
ExistingApplication=Une version de [name] est déjà installée.%n%nVoulez-vous désinstaller l'ancienne version et installer [name/ver]?
ExistingApplicationNewer=Une version plus récente de [name] est déjà installée.%n%nSi vous souhaitez quand même installer cette version, il est recommandé de désinstaller la version actuelle d'abord.
ExistingApplicationSameVersion=Cette version de [name] est déjà installée.

; Setup wizard pages
SelectDirLabel3=Cliquez sur Suivant pour installer dans le dossier par défaut ou cliquez sur Parcourir pour choisir un autre dossier.
SelectDirBrowseLabel=Parcourir...
DiskSpaceMBLabel=Mo d'espace disque requis
DiskSpaceMBLabel2=Mo d'espace disque disponible
CannotInstallToNetworkDriveRoot=[name] ne peut pas être installé directement à la racine d'un lecteur réseau.
CannotInstallToUNCPath=[name] ne peut pas être installé dans un chemin UNC.
InvalidPath=Le chemin d'installation spécifié n'est pas valide.%n%n%nVeuillez spécifier un chemin valide.
SetupNeedsAdminRights1=Cette installation nécessite des droits d'administrateur.
SetupNeedsAdminRights2=Cliquez sur OK pour quitter l'installation, puis connectez-vous en tant qu'administrateur et relancez l'installation.
SelectComponentsLabel2=Sélectionnez les composants à installer:
SelectComponentsLabel3=Décochez les composants que vous ne souhaitez pas installer.
ComponentsDiskSpaceMBLabel=Mo d'espace disque requis
SelectStartMenuFolderLabel3=Sélectionnez le dossier du menu Démarrer:
SelectStartMenuFolderBrowseLabel=Parcourir...
MustEnterGroupName=Vous devez entrer un nom de groupe.
GroupNameTooLong=Le nom du groupe ne peut pas dépasser [max] caractères.
InvalidGroupName=Le nom du groupe n'est pas valide.
SelectTasksLabel2=Sélectionnez les tâches supplémentaires:
SelectTasksLabel3=Décochez les tâches que vous ne souhaitez pas effectuer.
ReadyLabel1=[name] est prêt à être installé.
ReadyLabel2a=Cliquez sur Installer pour continuer avec l'installation.
ReadyLabel2b=Cliquez sur Retour pour vérifier ou modifier les paramètres.
ReadyLabel2c=Cliquez sur Annuler pour quitter l'assistant.
WizardInfo=%nDossier d'installation:%n%1%n%n%nFichiers d'application:%n%2
WizardInfoText=Informations
PreparingDesc=Préparation de l'installation...
InstallingDesc=Installation des fichiers...
FinishedHeadingLabel=Installation terminée
FinishedLabel1=[name/ver] a été installé avec succès sur votre ordinateur.
FinishedLabel2=Cliquez sur Terminer pour quitter l'assistant.
FinishedRestartLabel=Pour terminer l'installation, vous devez redémarrer votre ordinateur.%n%nVoulez-vous redémarrer maintenant?
FinishedRestartLabel2=&Redémarrer maintenant
FinishedNoRestartLabel2=&Quitter sans redémarrer

; Custom messages
WelcomeLabel2=Cet assistant va installer App Médecin sur votre ordinateur.%n%n%nL'application sera entièrement en français et configurée automatiquement avec la base de données.
SelectDirLabel3=App Médecin sera installé dans le dossier suivant:
SelectStartMenuFolderLabel3=L'application sera ajoutée au dossier suivant du menu Démarrer:
SelectComponentsLabel2=Composants à installer:
SelectTasksLabel2=Tâches additionnelles:
NameAndVersion=%1 version %2
CreateDesktopIcon=Créer une icône sur le bureau
CreateQuickLaunchIcon=Créer une icône de lancement rapide
AdditionalIcons=Icônes additionnelles
AssociateFiles=Associer des fichiers
FileDescription=Gestion médicale des patients et rendez-vous

; Status messages
StatusCreateDir=Création du dossier: %1
StatusExtractFiles=Extraction des fichiers...
StatusCreateIcons=Création des icônes...
StatusCreateIniFiles=Création des fichiers de configuration...
StatusCreateRegEntries=Création des entrées de registre...
StatusSaveIni=Enregistrement de la configuration...
StatusRunProgram=Exécution du programme...
StatusRollback=Annulation des changements...

; Error messages
ErrorInternalError1=Erreur interne %1
ErrorInternalError2=Erreur interne %1.%n%n%nMessage:%n%2
ErrorFunctionFailedNoCode=Échec de la fonction:%n%1%n%nVeuillez contacter le support technique.
ErrorFunctionFailedWithCode=Échec de la fonction:%n%1%n%nCode:%n%2
ErrorExecutingProgram=Erreur lors de l'exécution du programme:%n%1
ErrorRegisterServer=Erreur lors de l'enregistrement du serveur DLL/OCX:%n%1
ErrorRegSvr=Échec de l'enregistrement du serveur DLL/OCX:%n%1
ErrorCreateDir=Erreur lors de la création du dossier:%n%1
ErrorCreateShortcut=Erreur lors de la création du raccourci:%n%1
ErrorCorruptINI=Fichier de configuration endommagé:%n%1
ErrorInvalidParameter=Paramètre de ligne de commande invalide:%n%1
ErrorInvalidOption=Option invalide sur la ligne de commande:%n%1
ErrorMissingParameter=Paramètre de ligne de commande requis:%n%1
ErrorInvalidCommandLine=Syntaxe de ligne de commande invalide.

; Buttons
ButtonBack=< &Retour
ButtonNext=S&uivant >
ButtonCancel=&Annuler
ButtonInstall=&Installer
ButtonFinish=&Terminer
ButtonBrowse=&Parcourir...
ButtonNo=&Non
ButtonYes=&Oui
ButtonOK=OK
ButtonClose=&Fermer

; Other
BrowseForFolder=Parcourir les dossiers
BrowseForFile=Parcourir les fichiers
Change=Modifier...
SelectProgramGroupDesc=Sélectionner le dossier dans le menu Démarrer où les raccourcis seront créés:
SelectProgramGroup=Programmes

; Uninstall
UninstallNotFound=Fichier de désinstallation introuvable.%n%n%nVeuillez désinstaller manuellement.
ConfirmUninstall=Êtes-vous sûr de vouloir désinstaller complètement [name] et tous ses composants?
UninstallStatusLabel=Statut de la désinstallation:
StatusUninstalling=Désinstallation en cours...
StatusRollback=Annulation des changements...
UnfinishedText=La désinstallation a été interrompue.%n%nVotre système n'a pas été modifié.
FinishedUninstallText=[name] a été désinstallé avec succès de votre ordinateur.
UninstalledAll=Désinstallation terminée
