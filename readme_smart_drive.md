# SmartDrive

Plateforme de partage de fichiers distribuée en **Java (sockets TCP)** avec interface **JavaFX**, load balancer, gestion des quotas, versioning, corbeille, partage contrôlé, notifications, administration avancée et supervision système.

---

## 🚀 Proposition de valeur

**SmartDrive** est une solution complète de stockage collaboratif, conçue **sans stack web**, idéale pour une démonstration académique de :

- la programmation système  
- la programmation réseau bas niveau  
- la conception d’un protocole applicatif  
- la gestion de stockage multi-utilisateurs  

### 🔹 Stack technique

- Backend TCP multi-serveurs (Java)
- Client desktop JavaFX
- Load balancer configurable
- Collaboration avancée (partage, demandes d’accès, notifications)
- Supervision et administration centralisée
- Déploiement multi-PC via Docker Compose

---

## 🛠 Fonctionnalités principales

### 1️⃣ Authentification & contrôle d’accès

- Connexion sécurisée par login / password  
- Blocage / déblocage de comptes par l’admin  
- Séparation des rôles : utilisateur / administrateur  

---

### 2️⃣ Stockage de fichiers

- Upload / download binaire via TCP  
- Quotas personnalisés par utilisateur  
- Listing des fichiers personnels  

---

### 3️⃣ Corbeille & récupération

- Suppression logique (corbeille)  
- Restauration de fichiers supprimés  
- Purge sélective ou totale  

---

### 4️⃣ Versioning automatique

- Archivage automatique des anciennes versions lors du ré-upload  
- Consultation des versions précédentes  
- Restauration d’une version spécifique  

---

### 5️⃣ Partage contrôlé entre utilisateurs

- Demande d’accès en lecture sur un fichier  
- Approbation / refus par le propriétaire  
- Téléchargement autorisé de fichiers partagés  

---

### 6️⃣ Notifications persistantes

- Alertes quota  
- Demandes de partage  
- Changements de statut  
- Consultation et nettoyage des notifications  

---

### 7️⃣ Audit & traçabilité

- Journalisation des actions importantes :
  - upload  
  - download  
  - suppression  
  - partage  
  - actions administrateur  

- Exploitation des logs dans le panneau admin  

---

### 8️⃣ Administration avancée

- Gestion des utilisateurs  
- Gestion des quotas  
- Blocage / suppression de comptes  
- Vue globale du stockage  
- Monitoring système  
- Accès admin aux fichiers utilisateurs  

---

### 9️⃣ Load Balancer TCP

- Distribution de la charge sur plusieurs serveurs primaires  
- Modes :
  - round-robin  
  - limites configurables  
  - stickiness IP  

- Robustesse accrue en environnement multi-serveurs  

---

### 🔟 Réplication automatique (Slave)

- Upload sur serveur primaire → réplication vers serveur slave  
- Mode **best-effort** : l’upload reste valide même si le slave est indisponible  

---

### 1️⃣1️⃣ Supervision système

- Monitoring CPU / RAM / disque  
- Orchestration multi-processus  
- Déploiement multi-conteneurs via Docker  

---

## 🏗 Architecture

```
client         → Client JavaFX (SmartDriveFxApp)
loadbalancer   → Load Balancer TCP
server         → Serveurs de stockage (Primary / Slave)
shared_storage → Données utilisateurs
```

### 🔁 Flux principal

1. Le client JavaFX se connecte au load balancer  
2. Le load balancer affecte un serveur primaire disponible  
3. Le serveur traite les commandes : fichiers, partage, quotas, administration  

---

## ⚙️ Démarrage rapide (Docker Compose)

### ▶ Lancer les services

```bash
docker-compose up --build
```

### ⏹ Arrêter les services

```bash
docker-compose down
```

### 🔁 Mode SLAVE

Activer ou désactiver le service `slave` dans `docker-compose.yml` selon le rôle du PC.

---

## 👤 Comptes de démonstration

| Utilisateur | Mot de passe | Rôle |
|------------|-------------|------|
| alice      | 1234        | user |
| bob        | 1234        | user |
| micka      | 1234        | user |
| Tsoa       | 1234        | admin |

---

## 🔒 Protocole TCP (exemples de commandes)

### 🔑 Authentification
```
LOGIN;<username>;<password>
```

### 📁 Fichiers
```
LIST
UPLOAD;<filename>;<size>
DOWNLOAD;<filename>
```

### 🗑 Corbeille
```
DELETE;<filename>
TRASH_LIST
TRASH_RESTORE;<id>
TRASH_PURGE;<id|ALL>
```

### 🕘 Versioning
```
VERSIONS;<filename>
RESTORE_VERSION;<filename>;<versionId>
```

### 🤝 Partage
```
LIST_SHARED;<owner>
REQUEST_READ;<owner>;<file>
RESPOND_REQUEST;<requester>;<file>;<approve|deny>
DOWNLOAD_AS;<owner>;<file>
```

### 🔔 Notifications
```
NOTIFS
NOTIFS_CLEAR
```

### 📊 Quota
```
QUOTA
```

### 👨‍💼 Admin
```
ADMIN_USERS
ADMIN_BLOCK
ADMIN_DELETE
ADMIN_SET_QUOTA
ADMIN_STORAGE
ADMIN_LOGS
ADMIN_MONITOR
ADMIN_LIST_FILES
ADMIN_DOWNLOAD_AS
```

---

## 🧑‍💻 Scénario de démo conseillé

1. Connexion avec **alice** et **bob**  
2. Upload d’un fichier chez alice  
3. bob demande l’accès au fichier  
4. alice approuve la demande  
5. bob télécharge le fichier partagé  
6. Démonstration corbeille + restauration  
7. Démonstration versioning (ré-upload + restauration d’une version)  
8. Connexion admin (**Tsoa**) : quotas, logs, monitoring  

---

## 📦 Déploiement multi-PC

Voir le guide dédié :

```
DEPLOIEMENT_MULTI_PC_DOCKER.md
```

---

## 🏅 Pourquoi c’est un vrai projet de programmation système

- Utilisation directe de `ServerSocket` / `Socket`  
- Conception d’un protocole applicatif maison  
- Gestion explicite des flux, buffers, tailles et intégrité des données  
- Gestion de la concurrence via threads  
- Manipulation directe du système de fichiers  
- Supervision système  
- Orchestration multi-conteneurs  

---

## 🔥 Limites (transparence pédagogique)

- Mots de passe stockés en clair (contexte académique)  
- `ADMIN_MONITOR` partiellement simulé  
- Réplication slave en mode best-effort  

---

## 📝 Résumé

**SmartDrive** est une mini-plateforme distribuée complète intégrant protocole réseau, gestion d’accès, collaboration, administration et supervision.

Déployable en local ou via Docker, elle constitue un excellent projet académique en programmation système et réseau.

