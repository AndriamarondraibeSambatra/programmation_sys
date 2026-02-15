# Commandes Multi-PC (PC1, PC2, PC3)

Ce guide utilise `docker compose` (Compose v2).

## 0) IPs a adapter

- `IP_PC1` = IP LAN du PC1 (ex: `192.168.88.40`)
- `IP_PC2` = IP LAN du PC2 (ex: `192.168.88.54`)
- `IP_PC3` = IP LAN du PC3 (ex: `192.168.88.60`)

## 1) PC2 (server2 only)

Sur PC2:

```bash
cd ~/PROG-SYS/backend/server

cat > docker-compose.yml <<'YAML'
services:
  server2:
    build: .
    command: ["2122"]
    ports:
      - "2122:2122"
      - "2222:2222"
    volumes:
      - ./resources:/app/resources
      - ./shared_storage:/app/shared_storage
    restart: unless-stopped
YAML

docker compose down --remove-orphans
docker compose up -d --build
docker compose ps
docker compose logs -f server2
```

## 2) PC3 (server3 only)

Sur PC3:

```bash
cd ~/PROG-SYS/backend/server

cat > docker-compose.yml <<'YAML'
services:
  server3:
    build: .
    command: ["2123"]
    ports:
      - "2123:2123"
      - "2223:2223"
    volumes:
      - ./resources:/app/resources
      - ./shared_storage:/app/shared_storage
    restart: unless-stopped
YAML

docker compose down --remove-orphans
docker compose up -d --build
docker compose ps
docker compose logs -f server3
```

## 3) PC1 (load balancer + server1 + client JavaFX)

### 3.1 Config LB (fichier unique)

Sur PC1:

```bash
cd ~/PROG-SYS

cat > backend/loadbalancer/resources/lb_config.json <<'JSON'
{
  "lb_port": 2100,
  "servers": [
    {"ip": "IP_PC1", "port": 2121},
    {"ip": "IP_PC2", "port": 2122},
    {"ip": "IP_PC3", "port": 2123}
  ],
  "admin_exempt_users": ["Tsoa"],
  "max_clients_per_server": 2,
  "round_robin": true
}
JSON
```

### 3.2 Compose PC1 dedie

Sur PC1:

```bash
cd ~/PROG-SYS

cat > docker-compose.pc1.yml <<'YAML'
services:
  server1:
    build:
      context: ./backend/server
    command: ["2121"]
    ports:
      - "2121:2121"
      - "2221:2221"
    volumes:
      - ./backend/server/resources:/app/resources
      - ./backend/server/shared_storage:/app/shared_storage
    restart: unless-stopped

  loadbalancer:
    build:
      context: ./backend/loadbalancer
    ports:
      - "2100:2100"
    volumes:
      - ./backend/loadbalancer/resources:/app/resources:ro
    environment:
      SMARTDRIVE_LB_CONFIG: /app/resources/lb_config.json
    depends_on:
      - server1
    restart: unless-stopped

  javafx:
    build:
      context: ./backend/client
      dockerfile: Dockerfile.javafx
    depends_on:
      - loadbalancer
    environment:
      SMARTDRIVE_BACKEND_HOST: loadbalancer
      SMARTDRIVE_BACKEND_PORT: "2100"
      DISPLAY: "${DISPLAY:-:0}"
      GDK_BACKEND: x11
      LANG: C.UTF-8
      LC_ALL: C.UTF-8
      LANGUAGE: C.UTF-8
    volumes:
      - /tmp/.X11-unix:/tmp/.X11-unix
      - ${HOME:-/tmp}:/host-home
    stdin_open: true
    tty: true
    restart: unless-stopped
YAML
```

### 3.3 Lancement PC1

```bash
cd ~/PROG-SYS
xhost +local:docker
docker compose -f docker-compose.pc1.yml down --remove-orphans
docker compose -f docker-compose.pc1.yml up -d --build
docker compose -f docker-compose.pc1.yml ps
```

### 3.4 Logs LB (commande demandee)

Sur PC1, pour verifier le load balancer:

```bash
docker logs -f prog-sys_loadbalancer_1
```

Commande alternative (independante du nom de conteneur):

```bash
docker compose -f docker-compose.pc1.yml logs -f loadbalancer
```

## 4) Tests rapides depuis PC1

```bash
nc -vz IP_PC2 2122
nc -vz IP_PC3 2123
```

Puis lancer 2-3 clients et verifier dans les logs LB que les connexions alternent entre `2121`, `2122`, `2123`.

## 5) Arret

- Sur PC1:

```bash
cd ~/PROG-SYS
docker compose -f docker-compose.pc1.yml down --remove-orphans
```

- Sur PC2:

```bash
cd ~/PROG-SYS/backend/server
docker compose down --remove-orphans
```

- Sur PC3:

```bash
cd ~/PROG-SYS/backend/server
docker compose down --remove-orphans
```
