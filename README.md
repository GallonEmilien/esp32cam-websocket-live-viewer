# Websocket ESP Camera

## TODO:
- gRPC Streaming
- Multiple user accounts that may or may not have access to cameras
- Docker deployment
- Motion detection & user alerts

## Introduction
Project that allows ESP-Cam devices to communicate with a server on a local network in order to visualize their video stream.

## Installation

### ESP-Cam
+ Configure the SSID & Internet password & the token used by the server in esp/credentials.h

```
#define SSID ""
#define WIFI_PASSWORD ""
#define ESP_SECRET ""
```

+ Configure the local server IP. (Remember to modify it to set a static IP via DHCP settings)

```
wsHost = ...
```

+ Configure the server port

```
const uint16_t wsPort = 8080;
```

+ Configure the camera identifier

```
const char* espId = "1";
```

### Server
Define the environment variables for the server:
```
WS_CAM_ESP_SECRET=..; //should preferably be different from the JWT secret
WS_CAM_JWT_SECRET=..;
WS_CAM_PASSWORD=..;
WS_CAM_USER=..;
```

+ Launch the bootRun configuration
