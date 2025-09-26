#include "esp_camera.h"
#include <WiFi.h>
#include <ArduinoWebsockets.h>
#include <Base64.h>
#include <WiFiUdp.h>
#include "time.h"

#define CAMERA_MODEL_AI_THINKER
#include "camera_pins.h"
#include "credentials.h"

using namespace websockets;
const char* wsHost = "192.168.1.19";
const uint16_t wsPort = 8080;
WebsocketsClient client;
bool isConnected = false;
unsigned long lastReconnectAttempt = 0;
const unsigned long RECONNECT_INTERVAL = 5000;
unsigned long lastPingTime = 0;
const unsigned long PING_INTERVAL = 30000;
const char* ntpServer = "pool.ntp.org";
const long gmtOffset_sec = 0;
const int daylightOffset_sec = 0;

void setup() {
  Serial.begin(115200);
  WiFi.begin(SSID, WIFI_PASSWORD);
  WiFi.setSleep(false);
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println("\nWiFi connected");
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
  Serial.println("Time synchronized");
  setupWebSocket();
  // Init camera
  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = Y2_GPIO_NUM;
  config.pin_d1 = Y3_GPIO_NUM;
  config.pin_d2 = Y4_GPIO_NUM;
  config.pin_d3 = Y5_GPIO_NUM;
  config.pin_d4 = Y6_GPIO_NUM;
  config.pin_d5 = Y7_GPIO_NUM;
  config.pin_d6 = Y8_GPIO_NUM;
  config.pin_d7 = Y9_GPIO_NUM;
  config.pin_xclk = XCLK_GPIO_NUM;
  config.pin_pclk = PCLK_GPIO_NUM;
  config.pin_vsync = VSYNC_GPIO_NUM;
  config.pin_href = HREF_GPIO_NUM;
  config.pin_sccb_sda = SIOD_GPIO_NUM;
  config.pin_sccb_scl = SIOC_GPIO_NUM;
  config.pin_pwdn = PWDN_GPIO_NUM;
  config.pin_reset = RESET_GPIO_NUM;
  config.xclk_freq_hz = 20000000;
  config.pixel_format = PIXFORMAT_JPEG;
  config.frame_size = FRAMESIZE_VGA;
  config.jpeg_quality = 12;
  config.fb_count = 2;
  config.fb_location = CAMERA_FB_IN_PSRAM;
  config.grab_mode = CAMERA_GRAB_LATEST;
  if (esp_camera_init(&config) != ESP_OK) {
    Serial.println("Camera initialization error");
    ESP.restart();
  }
  delay(100);
}
void setupWebSocket() {
  String wsUrl = "ws://" + String(wsHost) + ":" + String(wsPort) + "/api/ws/camera?jwt=" + ESP_SECRET + "&espId=" + ESP_ID + "&mode=ESP";
  
  Serial.println("Connecting WebSocket to: " + wsUrl);
  client.onEvent([](WebsocketsEvent event, String data) {
    switch (event) {
      case WebsocketsEvent::ConnectionOpened:
        Serial.println("WebSocket connection opened");
        isConnected = true;
        break;
        
      case WebsocketsEvent::ConnectionClosed:
        Serial.println("WebSocket connection closed");
        isConnected = false;
        break;
        
      case WebsocketsEvent::GotPing:
        Serial.println("WebSocket ping received");
        client.pong();
        break;
        
      case WebsocketsEvent::GotPong:
        Serial.println("WebSocket pong received");
        break;
    }
  });
  bool connected = client.connect(wsUrl);
  if (connected) {
    Serial.println("WebSocket connection successful");
    isConnected = true;
  } else {
    Serial.println("WebSocket connection failed");
    isConnected = false;
  }
}
void loop() {
  if (!isConnected && (millis() - lastReconnectAttempt > RECONNECT_INTERVAL)) {
    Serial.println("Attempting to reconnect...");
    setupWebSocket();
    lastReconnectAttempt = millis();
    return;
  }
  if (isConnected) {
    client.poll(); 
    camera_fb_t *fb = esp_camera_fb_get();
    if (!fb) {
      Serial.println("Camera capture error");
      return;
    }
    bool success = client.sendBinary((const char*)fb->buf, fb->len);
    esp_camera_fb_return(fb);
    if (!success) {
      Serial.println("Image send error, the connection is probably closed");
      isConnected = false;
      return;
    }
    delay(50);
  }
}
