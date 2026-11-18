import { inject, Injectable, signal, WritableSignal } from '@angular/core';
import { WebSocketService } from './websocket.service';
import { CameraHttpService } from './http/camera.http.service';
import { environment } from '../../../environments/environment';
import { firstValueFrom } from 'rxjs';

export interface FrameUpdate {
  bitmap: ImageBitmap;
  timestamp: number;
}

@Injectable({ providedIn: 'root' })
export class CameraViewerService {
  frames = new Map<string, WritableSignal<ImageBitmap | undefined>>();
  private frameUpdates = new Map<string, WritableSignal<number>>();
  private previousBitmaps = new Map<string, ImageBitmap>();
  private reconnectTimers = new Map<string, any>();
  private activeConnections = new Set<string>();

  private wsService = inject(WebSocketService);
  private cameraHttpService = inject(CameraHttpService);

  async connect(espId: string, fallbackJwt?: string) {
    this.activeConnections.add(espId);
    this.clearReconnectTimer(espId);

    if (!this.frames.has(espId)) {
      this.frames.set(espId, signal<ImageBitmap | undefined>(undefined));
    }
    if (!this.frameUpdates.has(espId)) {
      this.frameUpdates.set(espId, signal<number>(0));
    }

    try {
      // 1. Obtain short-lived One-Time WebSocket Ticket
      let authParam = '';
      try {
        const ticketRes = await firstValueFrom(this.cameraHttpService.getWsTicket(espId));
        authParam = `ticket=${encodeURIComponent(ticketRes.ticket)}`;
      } catch (err) {
        console.warn(`Failed to acquire WS ticket, falling back to bearer token:`, err);
        if (fallbackJwt) {
          authParam = `jwt=${encodeURIComponent(fallbackJwt)}`;
        } else {
          throw new Error('No authentication credential available for WebSocket');
        }
      }

      // 2. Build secure WebSocket URL (wss:// on https, ws:// on http)
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      const urlHost = environment.production ? window.location.host : 'localhost:8080';
      const url = `${protocol}//${urlHost}/api/ws/camera?${authParam}&mode=CLIENT&espId=${encodeURIComponent(espId)}`;

      // 3. Connect and handle stream
      this.wsService.connect(
        url,
        espId,
        async (data: ArrayBuffer) => {
          try {
            const blob = new Blob([data], { type: 'image/jpeg' });
            const newBitmap = await createImageBitmap(blob);

            // Close old GPU bitmap texture immediately to eliminate browser VRAM memory leak
            const oldBitmap = this.previousBitmaps.get(espId);
            if (oldBitmap) {
              oldBitmap.close();
            }
            this.previousBitmaps.set(espId, newBitmap);

            // Update signals
            this.frames.get(espId)?.set(newBitmap);
            this.frameUpdates.get(espId)?.set(Date.now());
          } catch (err) {
            console.error('Error decoding video frame:', err);
          }
        },
        (wasClean: boolean) => {
          // Automatic reconnection with fresh ticket if not closed intentionally
          if (!wasClean && this.activeConnections.has(espId)) {
            console.info(`Connection lost for camera ${espId}, scheduling reconnect...`);
            this.scheduleReconnect(espId, fallbackJwt);
          }
        }
      );
    } catch (error) {
      console.error(`Failed to initialize WebSocket connection for camera ${espId}:`, error);
      if (this.activeConnections.has(espId)) {
        this.scheduleReconnect(espId, fallbackJwt);
      }
    }
  }

  disconnect(espId: string) {
    this.activeConnections.delete(espId);
    this.clearReconnectTimer(espId);
    this.wsService.disconnect(espId);

    // Free VRAM resources
    const oldBitmap = this.previousBitmaps.get(espId);
    if (oldBitmap) {
      oldBitmap.close();
      this.previousBitmaps.delete(espId);
    }
    this.frames.get(espId)?.set(undefined);
  }

  getFrameUpdateSignal(espId: string): WritableSignal<number> | undefined {
    return this.frameUpdates.get(espId);
  }

  private scheduleReconnect(espId: string, fallbackJwt?: string) {
    this.clearReconnectTimer(espId);
    const timer = setTimeout(() => {
      if (this.activeConnections.has(espId)) {
        this.connect(espId, fallbackJwt);
      }
    }, 2000);
    this.reconnectTimers.set(espId, timer);
  }

  private clearReconnectTimer(espId: string) {
    const timer = this.reconnectTimers.get(espId);
    if (timer) {
      clearTimeout(timer);
      this.reconnectTimers.delete(espId);
    }
  }
}
