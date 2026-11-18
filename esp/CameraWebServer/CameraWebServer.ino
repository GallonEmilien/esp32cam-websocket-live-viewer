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

// Non-blocking reconnect timers
unsigned long lastReconnectAttempt = 0;
const unsigned long RECONNECT_INTERVAL = 3000;

// Non-blocking frame rate control (Target: ~25-30 FPS without blocking the CPU)
unsigned long lastFrameTime = 0;
const unsigned long MIN_FRAME_INTERVAL_MS = 25; // ~35 FPS maximum cap to avoid overflowing LwIP TCP buffers

// Watchdog & FPS monitoring
unsigned long lastFpsReport = 0;
unsigned int framesSent = 0;
unsigned int consecutiveFailures = 0;
const unsigned int MAX_FAILURES_BEFORE_RESET = 3;

// Keep-alive ping interval
unsigned long lastPingTime = 0;
const unsigned long PING_INTERVAL = 15000;

const char* ntpServer = "pool.ntp.org";
const long gmtOffset_sec = 0;
const int daylightOffset_sec = 0;

void setupCamera() {
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
  
  // FRAMESIZE_VGA (640x480) with balanced JPEG compression
  // Quality 13 provides an optimal balance between sharpness and Wi-Fi throughput
  config.frame_size = FRAMESIZE_VGA;
  config.jpeg_quality = 13;
  config.fb_count = 2;
  config.fb_location = CAMERA_FB_IN_PSRAM;
  config.grab_mode = CAMERA_GRAB_LATEST; // Always grabs the latest available frame

  if (esp_camera_init(&config) != ESP_OK) {
    Serial.println("[Camera] Initialization failed! Rebooting...");
    delay(2000);
    ESP.restart();
  }
  Serial.println("[Camera] Initialized successfully");
}

void setupWebSocket() {
  // Use apiKey param with fallback support on backend
  String wsUrl = "ws://" + String(wsHost) + ":" + String(wsPort) + 
                 "/api/ws/camera?apiKey=" + ESP_SECRET + 
                 "&espId=" + ESP_ID + 
                 "&mode=ESP";
  
  Serial.println("[WebSocket] Connecting to: " + wsUrl);

  client.onEvent([](WebsocketsEvent event, String data) {
    switch (event) {
      case WebsocketsEvent::ConnectionOpened:
        Serial.println("[WebSocket] Connection opened successfully");
        isConnected = true;
        consecutiveFailures = 0;
        break;
        
      case WebsocketsEvent::ConnectionClosed:
        Serial.println("[WebSocket] Connection closed");
        isConnected = false;
        break;
        
      case WebsocketsEvent::GotPing:
        client.pong();
        break;
        
      case WebsocketsEvent::GotPong:
        break;
    }
  });

  bool connected = client.connect(wsUrl);
  if (connected) {
    Serial.println("[WebSocket] Handshake OK");
    isConnected = true;
    consecutiveFailures = 0;
  } else {
    Serial.println("[WebSocket] Connection failed");
    isConnected = false;
  }
}

void setup() {
  Serial.begin(115200);
  delay(500);
  Serial.println("\n--- Starting ESP32 Camera Streamer ---");

  WiFi.begin(SSID, WIFI_PASSWORD);
  WiFi.setSleep(false); // Disable Wi-Fi sleep for lowest latency streaming
  
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(300);
  }
  Serial.printf("\n[WiFi] Connected, IP: %s\n", WiFi.localIP().toString().c_str());

  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
  setupCamera();
  setupWebSocket();
}

void loop() {
  unsigned long now = millis();

  // 1. Non-blocking reconnection mechanism
  if (!isConnected) {
    if (now - lastReconnectAttempt > RECONNECT_INTERVAL) {
      lastReconnectAttempt = now;
      Serial.println("[WebSocket] Reconnecting...");
      setupWebSocket();
    }
    return;
  }

  // 2. Poll incoming control frames (pings, close frames)
  client.poll();

  // 3. Periodic keep-alive ping
  if (now - lastPingTime > PING_INTERVAL) {
    lastPingTime = now;
    client.ping();
  }

  // 4. Non-blocking frame pacing: capture & send as soon as frame interval elapsed
  if (now - lastFrameTime >= MIN_FRAME_INTERVAL_MS) {
    lastFrameTime = now;

    camera_fb_t *fb = esp_camera_fb_get();
    if (!fb) {
      Serial.println("[Camera] Capture failed");
      return;
    }

    bool success = client.sendBinary((const char*)fb->buf, fb->len);
    esp_camera_fb_return(fb);

    if (success) {
      framesSent++;
      consecutiveFailures = 0;
    } else {
      consecutiveFailures++;
      Serial.printf("[WebSocket] Send failed (attempt %u/%u)\n", consecutiveFailures, MAX_FAILURES_BEFORE_RESET);
      
      if (consecutiveFailures >= MAX_FAILURES_BEFORE_RESET) {
        Serial.println("[WebSocket] Too many send failures, resetting socket...");
        client.close();
        isConnected = false;
        lastReconnectAttempt = millis();
        return;
      }
    }
  }

  // 5. FPS reporting every 5 seconds
  if (now - lastFpsReport >= 5000) {
    float fps = (float)framesSent / ((now - lastFpsReport) / 1000.0f);
    Serial.printf("[Stats] Current Stream Rate: %.1f FPS\n", fps);
    framesSent = 0;
    lastFpsReport = now;
  }
}
