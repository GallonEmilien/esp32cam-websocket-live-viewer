


# Websocket ESP Camera

## TODO : 

- Streaming gRPC
- Multiple Caméra
- Plusieurs compte utilisateurs qui peuvent posséder ou non des accès à des caméras
- Déploiement docker
- Détection de mouvement & alertes utilisateur

## Introduction

Projet permettant sur un réseau local de faire communiquer un ESP-Cam avec un serveur afin de visualiser le flux vidéo de celui-ci.
Le serveur permet de visualiser sur une interface simple et n'importe où ce flux.

## Fonctionnement

Les ESP ont un port attribué pour communiquer avec le serveur. 
Le serveur traite les flux vidéos et les envoie aux clients qui sont connectés sur l'application.

Chaque requête avant son envoie est hashé via l'algorithme MD5 ce qui n'est pas optimal. Il faut donc faire en sorte de mettre des **VRAIS** mots de passes qui n'ont jamais été trouvé sur internet pour des raisons de sécurité.

## Installation

### ESP-Cam

+ Configurer le SSID & Mot de passe internet & le token utilisé par le serveur dans le esp/credentials.h
```
#define SSID ""
#define WIFI_PASSWORD ""
#define ESP_SECRET ""
```

+ Configurer l'IP du serveur local. (Pensez à modifier pour le mettre en IP fixe via la paramètres DHCP)

```
wsHost = ...
```

+ Configurer le port du serveur

```
const uint16_t wsPort = 8080;
```

etc...

### Serveur 

Définir les variables d'environnements pour le serveur : 

WS_CAM_ESP_SECRET=..;
WS_CAM_JWT_SECRET=..;
WS_CAM_PASSWORD=..;
WS_CAM_USER=..;


+ lancer la run config bootRun 


