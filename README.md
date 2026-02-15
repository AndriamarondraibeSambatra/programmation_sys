# SmartDrive

Plateforme de partage de fichiers distribuée en Java (sockets TCP) avec interface JavaFX, load balancer, gestion des quotas et administration.

## Proposition de valeur
SmartDrive montre une architecture complète de stockage collaboratif, sans stack web: 
- backend TCP multi-serveurs,
- client desktop JavaFX,
- équilibrage de charge,
- fonctionnalités de collaboration (partage, demandes d'accès, notifications),
- supervision et administration.

Le projet est adapté à une démo académique solide de systeme distribue, protocoles reseau et gestion de stockage.

## Fonctionnalites principales
- Authentification utilisateur avec gestion des comptes bloques.
- Upload/download binaire de fichiers via TCP.
- Corbeille par utilisateur (suppression logique, restauration, purge).
- Versioning automatique: chaque ecrasement archive l'ancienne version.
- Partage controle: demande d'acces, approbation/refus, telechargement autorise (`DOWNLOAD_AS`).
- Notifications utilisateur persistees (demandes de partage, quota presque plein, etc.).
- Quotas dynamiques par utilisateur, modifiables a chaud.
- Journal d'audit (upload, download, delete, actions admin, partage).
- Panneau admin JavaFX: utilisateurs, quotas, stockage, logs, monitoring.
- Load balancer TCP configurable (round-robin, limites de connexions, stickiness IP optionnelle).
- Replication automatique primaire -> slave apres upload (si le slave est actif).

## Architecture
- `backend/client/`: client JavaFX (`fx.SmartDriveFxApp`)
- `backend/loadbalancer/`: load balancer TCP (`LoadBalancer`)
- `backend/server/`: serveurs de stockage (`MainServer`)
- `backend/server/shared_storage/`: donnees utilisateurs

Flux principal:
1. Le client JavaFX se connecte au load balancer.
2. Le load balancer affecte un serveur primaire disponible.
3. Le serveur traite les commandes (fichiers, partage, quota, admin).

## Fonctionnalites admin (compte `Tsoa`)
- Liste des utilisateurs + statut admin/bloque + quota.
- Blocage/deblocage d'un compte.
- Suppression d'utilisateur (compte + quota + donnees).
- Modification de quota.
- Vue stockage global (taille totale, nombre de fichiers, etat slave).
- Consultation des logs d'audit.
- Monitoring CPU/RAM/disque + trafic (trafic simule dans l'etat actuel).
- Telechargement admin de fichiers utilisateur (`ADMIN_DOWNLOAD_AS`).

## Demarrage rapide (Docker Compose)
Depuis la racine du projet:
```bash
docker-compose up -d --build
```
Arret:
```bash
docker-compose down --remove-orphans
```

Mode SLAVE:
- Si ce PC doit etre slave: laisser le service `slave` actif dans `docker-compose.yml`, puis lancer la commande standard.
- Si ce PC ne doit pas etre slave: commenter le bloc `slave` dans `docker-compose.yml`, puis lancer la meme commande standard.

Sous Linux/X11 (GUI JavaFX dans conteneur):
```bash
xhost +local:docker
```

## Comptes de demonstration
Fichier: `backend/server/resources/user.json`

- `alice / 1234`
- `bob / 1234`
- `micka / 1234`
- `Tsoa / 1234` (admin)

## Configuration utile
- Utilisateurs: `backend/server/resources/user.json`
- Quotas: `backend/server/resources/quotas.json`
- Permissions: `backend/server/resources/permissions.json`
- Config serveur: `backend/server/resources/server_config.json`
- Config LB: `backend/loadbalancer/resources/lb_config.json`
- Config client JavaFX: `backend/client/resources/config.json`

Variables d'environnement supportees (exemples):
- `SMARTDRIVE_BACKEND_HOST`, `SMARTDRIVE_BACKEND_PORT`
- `SMARTDRIVE_LB_CONFIG`
- `SMARTDRIVE_USERS_PATH`
- `SMARTDRIVE_QUOTAS_PATH`
- `SMARTDRIVE_PERMISSIONS_PATH`

Options LB (dans `lb_config.json`):
- `max_clients_per_server`: limite par serveur pour les clients standards.
- `admin_exempt_users`: usernames admin non comptés dans la limite (ex: `["Tsoa"]`).

## Protocole TCP (resume)
Auth obligatoire:
- `LOGIN;<username>;<password>`

Commandes principales:
- `LIST`, `USERS`, `UPLOAD;<filename>;<size>`, `DOWNLOAD;<filename>`
- `DELETE;<filename>`, `TRASH_LIST`, `TRASH_RESTORE;<id>`, `TRASH_PURGE;<id|ALL>`
- `VERSIONS;<filename>`, `RESTORE_VERSION;<filename>;<versionId>`
- `LIST_SHARED;<owner>`, `REQUEST_READ;<owner>;<file>`, `LIST_REQUESTS`, `RESPOND_REQUEST;<requester>;<file>;<approve|deny>`, `DOWNLOAD_AS;<owner>;<file>`
- `NOTIFS`, `NOTIFS_CLEAR`, `QUOTA`

Admin:
- `ADMIN_USERS`, `ADMIN_BLOCK`, `ADMIN_DELETE`, `ADMIN_SET_QUOTA`
- `ADMIN_STORAGE`, `ADMIN_LOGS`, `ADMIN_MONITOR`
- `ADMIN_LIST_FILES`, `ADMIN_DOWNLOAD_AS`

## Scenario de demo conseille (prof/jury)
1. Connexion avec `alice` et `bob`.
2. Upload d'un fichier chez `alice`.
3. `bob` envoie une demande d'acces sur ce fichier.
4. `alice` approuve la demande.
5. `bob` telecharge avec `DOWNLOAD_AS`.
6. Demonstration corbeille + restauration.
7. Demonstration versioning (re-upload meme nom + restauration version).
8. Connexion admin `Tsoa`: quotas, logs, monitoring.

## Deploiement multi-PC
Guide dedie:
- `DEPLOIEMENT_MULTI_PC_DOCKER.md`

## Limites actuelles (transparentes)
- Les mots de passe sont stockes en clair dans `user.json` (contexte pedagogique).
- Le trafic dans `ADMIN_MONITOR` est actuellement simule.
- La replication vers le slave est en mode best-effort (si le slave est indisponible, l'upload primaire continue).

## Resume
SmartDrive n'est pas une simple maquette de transfert de fichiers: c'est une mini-plateforme distribuee, avec protocole reseau, gouvernance d'acces, administration et observabilite, exploitable en local ou via Docker pour une demonstration complete.
