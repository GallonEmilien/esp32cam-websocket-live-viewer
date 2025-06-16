


# Websocket ESP Camera

## TODO : 

- Changer de framework front
- Image docker + reverse proxy
- Sécuriser les routes
- Authentification JWT
- Streaming gRPC
- Optimisation du flux pour + d'images

## Introduction

Projet permettant sur un réseau local de faire communiquer un ESP-Cam avec un serveur afin de visualiser le flux vidéo de celui-ci.
Le serveur permet de visualiser sur une interface simple et n'importe où ce flux.

## Fonctionnement

Les ESP ont un port attribué pour communiquer avec le serveur. 
Le serveur traite les flux vidéos et les envoie aux clients qui sont connectés sur l'application.

Chaque requête avant son envoie est hashé via l'algorithme MD5 ce qui n'est pas optimal. Il faut donc faire en sorte de mettre des **VRAIS** mots de passes qui n'ont jamais été trouvé sur internet pour des raisons de sécurité.

## Installation

### ESP-Cam

+ Configurer le SSID & Mot de passe internet & Username & Password dans un fichier credentials.h que vous créerez dans le dossier esp.

Attention ! Le Username & Password doivent être les mêmes que ceux choisis pour le serveur

```
#define SSID ""
#define WIFI_PASSWORD ""
#define USERNAME ""
#define PASSWORD ""
```

+ Configurer l'IP du serveur local. (Pensez à modifier pour le mettre en IP fixe via la paramètres DHCP)

```
websocket_server_host = ...
```

+ Configurer le port de la caméra puis l'incrémenter de 1 pour chaque nouvelle caméra à ajouter.

```
const uint16_t websocket_server_port = 8880;
```
puis
```
const uint16_t websocket_server_port = 8881;
```

etc...

### Serveur 


+ lancer la commande `npm install`

+ Définir dans server-config.json le nombre de caméras et le port auquel le décompte commencera.
Par exemple : 
```
  "camera-number": 4,
  "camera-start-port": 8880
```
seront les caméras de 8880 à 8883.

+ Créer un .env de la sorte : 

```
API_KEY=... (doit être une clé forte qui sera utilisée ultérieurement)
USERNAME=... (doit correspondre à celui de l'ESP)
PASSWORD=... (doit correspondre à celui de l'ESP)
```

+ Lancer le serveur avec `node --env-file=.env server.js`

## Pistes d'améliorations

+ Empêcher les attaques MITD sur l'authentification (améliorer le système médiocre pour le moment)
+ SSL
+ Limiter le nombre de port à ouvrir ?
+ Permettre l'enregistrement (configurer pour X Go puis au fur et à mesure supprimer quand l'espace est dépassé, sur un event déclencher X minutes)
+ Intégrer les modules de détection de mouvement (déjà tenté en exploitant le dual-core de l'ESP-Cam mais beaucoup de soucis, à voir pour faire ça avec un module en parallèle (ESP8266?))
+ Factorisation du code
+ Améliorer le visuel

