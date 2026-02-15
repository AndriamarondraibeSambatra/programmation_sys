# SmartDrive – Guide d’exécution selon le rôle du PC

Ce document explique comment lancer SmartDrive selon le rôle de la machine (serveur seul, environnement complet ou supervision des logs).

---

## 🖥 1. PC serveur seul

Lancer uniquement le serveur principal :

```bash
docker-compose up -d --build server
```

- Démarre uniquement le service `server`.
- Les services `client`, `loadbalancer` et `slave` restent arrêtés.

---

## 🖥 2. PC avec load balancer, serveur et client

Tout lancer sur le même PC :

```bash
docker-compose up -d --build loadbalancer server client
```

- Démarre le load balancer
- Démarre le serveur principal
- Démarre le client JavaFX localement

---

## 🖥 3. PC qui observe les flux de login via le terminal

Afficher les logs du serveur en temps réel (pour voir les connexions/login) :

```bash
docker-compose logs -f server
```

- Affiche toutes les actions du serveur
- Permet de voir les tentatives de connexion

Pour ne voir que les lignes contenant "LOGIN" :

```bash
docker-compose logs -f server | grep LOGIN
```

- Filtre les logs pour ne montrer que les flux liés à l’authentification

> 💡 Astuce : adaptez les noms de services si votre `docker-compose.yml` utilise d’autres noms.

---

## 📡 Surveillance directe d’un conteneur spécifique

Afficher les logs en temps réel d’un conteneur précis :

```bash
docker logs -f prog-sys_loadbalancer_1
```

Explications :

- `-f` signifie *follow* (affichage en temps réel)
- `prog-sys_loadbalancer_1` est le nom automatique donné par Docker au conteneur du service `loadbalancer` dans le projet `prog-sys`
- Cette commande affiche tout ce que le load balancer écrit dans sa sortie standard : logs, erreurs, connexions, etc.

Utile pour :

- surveiller la répartition des connexions
- détecter des erreurs réseau
- observer l’activité en direct

Pour voir les logs d’un autre service, adaptez le nom du conteneur (exemple : `prog-sys_server_1`).

---

Ce README peut être utilisé comme guide rapide pour les démonstrations multi-PC ou la supervision en direct lors d’une soutenance.