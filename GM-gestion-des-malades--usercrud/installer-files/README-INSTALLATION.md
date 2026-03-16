# App Médecin - Guide d'Installation Complète

## 📦 Fichiers d'Installation Requis

### 1. Fichiers Principaux
- `AppMedecin.jar` - Application Java principale
- `app-icon.ico` - Icône de l'application
- `database.sql` - Script SQL pour la base de données

### 2. Fichiers d'Installation
- `install.bat` - Script d'installation automatique (Windows)
- `app-mecin-setup.iss` - Script Inno Setup (professionnel)
- `jre-installer.exe` - Installation Java Runtime (à télécharger)
- `mysql-installer.msi` - Installation MySQL Server (à télécharger)

---

## 🚀 Méthode 1: Installation Automatique (Recommandée)

### Étapes:
1. **Télécharger les prérequis**:
   - Java Runtime: https://download.oracle.com/java/17/latest/jre-17-windows-x64.exe
   - MySQL Server: https://dev.mysql.com/get/Downloads/MySQL-Installer/mysql-8.0.33-winx64.msi

2. **Placer tous les fichiers** dans un dossier `App-Medecin-Installer`

3. **Exécuter en tant qu'administrateur**:
   - Clic droit sur `install.bat`
   - "Exécuter en tant qu'administrateur"

### Ce que fait le script:
- ✅ Vérifie les privilèges administrateur
- ✅ Installe Java Runtime si absent
- ✅ Installe MySQL Server si absent
- ✅ Configure MySQL avec mot de passe root: `SALAH123`
- ✅ Importe automatiquement la base de données `appdb`
- ✅ Copie les fichiers dans `Program Files\App Médecin`
- ✅ Crée raccourcis bureau et menu Démarrer
- ✅ Configure le PATH système

---

## 🚀 Méthode 2: Installation Professionnelle (Inno Setup)

### Prérequis:
1. **Installer Inno Setup**: https://jrsoftware.org/inno-setup.php

2. **Compiler l'installateur**:
   ```bash
   iscc app-mecin-setup.iss
   ```

3. **Résultat**: Génère `AppMedecin-Setup.exe`

### Avantages de Inno Setup:
- 🎯 Installateur Windows professionnel
- 🔐 Vérification UAC automatique
- 📦 Gestion des dépendances
- 🗂️ Désinstallation propre
- 📊 Support multilingue (Français/Anglais)
- 🔄 Mises à jour automatiques

---

## 🗄️ Configuration Base de Données

### MySQL Configuration:
- **Nom de la base**: `appdb`
- **Utilisateur**: `root`
- **Mot de passe**: `SALAH123`
- **Port**: `3306`

### Importation Manuelle:
```sql
mysql -u root -pSALAH123 -e "CREATE DATABASE IF NOT EXISTS appdb; USE appdb; SOURCE database.sql;"
```

---

## 🎯 Lancement de l'Application

### Méthodes:
1. **Raccourci Bureau**: Double-cliquer sur "App Médecin"
2. **Menu Démarrer**: Chercher "App Médecin"
3. **Ligne de commande**:
   ```bash
   cd "C:\Program Files\App Médecin"
   javaw -jar AppMedecin.jar
   ```

---

## 🔧 Configuration Application

### Fichier de Configuration:
- **Emplacement**: `%PROGRAMFILES%\App Médecin\config.properties`
- **Paramètres**:
  ```properties
  database.url=jdbc:mysql://localhost:3306/appdb
  database.username=root
  database.password=SALAH123
  app.language=fr
  ```

---

## 🐛 Dépannage

### Problèmes Communs:

#### 1. "Java non trouvé"
```bash
# Vérifier installation
java -version

# Réinstaller si nécessaire
jre-installer.exe
```

#### 2. "Erreur de connexion MySQL"
```bash
# Vérifier service MySQL
sc query mysql

# Démarrer service
net start mysql

# Réinitialiser mot de passe
mysql -u root -p
```

#### 3. "Base de données vide"
```bash
# Importer manuellement
mysql -u root -pSALAH123 appdb < database.sql
```

---

## 📁 Structure des Fichiers

```
App-Medecin-Installer/
├── AppMedecin.jar          # Application principale
├── app-icon.ico            # Icône application
├── database.sql             # Script base de données
├── install.bat             # Installation automatique
├── app-mecin-setup.iss     # Script Inno Setup
├── jre-installer.exe       # Runtime Java
├── mysql-installer.msi      # MySQL Server
└── README.md               # Ce fichier
```

---

## 🔄 Mises à Jour

### Processus:
1. **Télécharger nouvelle version**
2. **Remplacer `AppMedecin.jar`**
3. **Exécuter script de migration** (si nécessaire)
4. **Redémarrer application**

### Configuration Automatique:
- L'application vérifie les mises à jour au démarrage
- Télécharge et installe automatiquement
- Préserve les données utilisateur

---

## 📞 Support Technique

### Informations Système Requises:
- **OS**: Windows 10/11 (64-bit)
- **RAM**: 4GB minimum
- **Stockage**: 500MB disponible
- **Java**: Runtime 17+
- **MySQL**: Server 8.0+

### Logs:
- **Application**: `%PROGRAMFILES%\App Médecin\logs\`
- **Installation**: `%TEMP%\app-mecin-install.log`

### Contact:
- **Documentation**: README.md
- **Issues**: https://github.com/worldsalah/GM-gestion-des-malades-/issues
- **Support**: [votre email de support]

---

## ✅ Vérification Post-Installation

### Checklist:
- [ ] Application se lance correctement
- [ ] Connexion base de données fonctionnelle
- [ ] Raccourcis bureau créés
- [ ] Services MySQL actifs
- [ ] Logs d'erreur vides
- [ ] Interface en français

### Test Final:
1. Lancer "App Médecin"
2. Se connecter avec: `hanen` / `SALAH`
3. Vérifier l'interface française
4. Tester la création de patient
5. Vérifier les rendez-vous

---

## 🎉 Félicitations!

Votre application médicale est maintenant installée et prête à l'emploi!
L'interface est entièrement en français pour une expérience utilisateur optimale.
