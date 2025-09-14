# Websocket ESP Camera

## TODO:

- Multiple user accounts that may or may not have access to cameras (need to add user add perm to other person
- Better informations on camera management (camera name, status, owner...)
- Frontend update (camera selection, auth fix, multiple user, admin view, user permissions...)
- Motion detection & user alerts
- Configure ESP with with when reset button pressed. If nothing is done in the 30s seconds, then it loads on the default config

## Introduction

Project that allows ESP-Cam devices to communicate with a server on a local network in order to visualize their video
stream.

## Installation

### ESP-Cam

+ /!\ Make sure to downgrade ESP32 to 3.2.0 on arduino IDE
+ Configure the SSID & Internet password.
+ Then, in the app you're gonna create a new camera and define ESP_SECRET & ESP_ID depending on the dialog

```
#define SSID ""
#define WIFI_PASSWORD ""
#define ESP_SECRET ""
#define ESP_ID ""
```

+ Configure the local server IP. (Remember to modify it to set a static IP via DHCP settings)

```
wsHost = ...
```

+ Configure the server port

```
const uint16_t wsPort = 8080;
```

### Server

Define the environment variables for the server:

```
WS_CAM_JWT_SECRET=..;
WS_CAM_PASSWORD=..; // Default generated admin user
WS_CAM_USER=..;
```

+ Launch the bootRun configuration
