# Fonctionnalites du projet SmartDrive

## 1) Authentification et controle d'acces
- Connexion utilisateur via commande `LOGIN;<username>;<password>`.
- Blocage de compte possible cote admin.
- Separation des droits utilisateur/admin pour proteger les actions sensibles.

## 2) Stockage de fichiers (coeur metier)
- Upload binaire (`UPLOAD;<filename>;<size>`) avec verification du format et de la taille.
- Download binaire (`DOWNLOAD;<filename>`) avec reponse `FILE;<size>`.
- Liste des fichiers personnels (`LIST`).
- Quota par utilisateur (`QUOTA`) pour limiter l'espace disque consomme.

## 3) Corbeille et recuperation
- Suppression logique (`DELETE`) vers une corbeille utilisateur, pas une suppression immediate.
- Consultation de la corbeille (`TRASH_LIST`).
- Restauration d'un fichier (`TRASH_RESTORE;<id>`).
- Purge selective ou totale (`TRASH_PURGE;<id|ALL>`).

## 4) Versioning de fichiers
- Si un fichier est re-uploade avec le meme nom, l'ancienne version est archivee automatiquement.
- Consultation des versions (`VERSIONS;<filename>`).
- Restauration d'une version precedente (`RESTORE_VERSION;<filename>;<versionId>`).

## 5) Partage controle entre utilisateurs
- Consultation des fichiers d'un autre user (`LIST_SHARED;<owner>`).
- Demande d'acces en lecture (`REQUEST_READ;<owner>;<file>`).
- Liste des demandes recues (`LIST_REQUESTS`).
- Approbation/refus (`RESPOND_REQUEST;<requester>;<file>;<approve|deny>`).
- Telechargement autorise d'un fichier partage (`DOWNLOAD_AS;<owner>;<file>`).

## 6) Notifications
- Notifications persistantes par utilisateur (demandes de partage, mises a jour de statut, alerte quota).
- Consultation (`NOTIFS`) et nettoyage (`NOTIFS_CLEAR`).

## 7) Audit et tracabilite
- Les actions importantes sont journalisees (upload, download, delete, partage, actions admin).
- Exploitable dans le panneau admin pour suivi et preuve de fonctionnement.

## 8) Administration
- Liste des utilisateurs, statut bloque, role admin, quotas (`ADMIN_USERS`).
- Blocage/deblocage (`ADMIN_BLOCK`).
- Suppression d'utilisateur (compte + quota + donnees) (`ADMIN_DELETE`).
- Changement de quota (`ADMIN_SET_QUOTA`).
- Vue stockage globale (`ADMIN_STORAGE`), logs (`ADMIN_LOGS`), monitor systeme (`ADMIN_MONITOR`).
- Acces admin aux fichiers d'un user (`ADMIN_LIST_FILES`, `ADMIN_DOWNLOAD_AS`).

## 9) Repartition de charge (Load Balancer TCP)
- Le client se connecte d'abord au load balancer.
- Le load balancer affecte un serveur primaire selon la config (round-robin, limites, sticky IP possible).
- Permet de distribuer la charge et de rendre la plateforme plus robuste en demo multi-serveurs.

## 10) Ou est le slave ?
- Code du slave: `backend/server/src/SlaveServer.java`
- Activation Docker Compose (PC en mode slave):
  - laisser le bloc `slave` actif dans `docker-compose.yml`, puis `docker-compose up -d --build`
- Desactivation (PC non slave):
  - commenter le service `slave` dans `docker-compose.yml`, puis `docker-compose up -d --build`
- Config du slave: `backend/server/resources/server_config.json`
- Port actuel du slave: `2322` (champ `slave_port`)
- Dossier de stockage du slave: `backend/server/slave_storage/` (monte dans `/app/slave_storage`)

Important:
- Le composant slave existe et ecoute les messages `REPLICA;...`.
- A chaque upload valide sur un primaire, une replication vers le slave est declenchee automatiquement.
- Si le slave n'est pas actif/joignable, l'upload utilisateur reste valide sur le primaire (mode best-effort).

## 11) Pourquoi ce projet est bien de la programmation systeme
- Programmation reseau bas niveau avec `ServerSocket`/`Socket` (pas framework web).
- Protocole applicatif maison (commandes texte + flux binaire dans la meme connexion).
- Gestion explicite des flux, buffers, tailles et controle d'integrite du transfert.
- Concurrence par threads pour gerer plusieurs clients et services en parallele.
- Travail direct avec le systeme de fichiers (stockage, quotas, versions, corbeille).
- Supervision systeme (CPU/RAM/disque) et orchestration multi-processus/multi-conteneurs.

## 12) Pourquoi TCP et pas FTP
- Besoin d'un protocole metier unique et personnalise (quota, partage, notifications, admin) dans une seule logique.
- FTP est un protocole generaliste de transfert de fichiers, pas adapte nativement a vos commandes metier.
- FTP impose une logique plus lourde (canaux controle/donnees, modes actif/passif, ports supplementaires, firewall plus complexe).
- Avec TCP brut, vous gardez le controle total sur le protocole, utile pedagogiquement en programmation systeme.
- Le client JavaFX et le serveur restent simples a faire evoluer ensemble sans dependre d'un serveur FTP externe.
