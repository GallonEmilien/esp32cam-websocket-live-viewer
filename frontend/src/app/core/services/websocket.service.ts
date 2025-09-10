import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private sockets = new Map<string, WebSocket>();

  connect(url: string, espId: string, onMessage: (data: ArrayBuffer) => void) {
    const existing = this.sockets.get(espId);

    if (existing && existing.readyState === WebSocket.OPEN) {
      return;
    }

    const ws = new WebSocket(url);
    ws.binaryType = 'arraybuffer';
    ws.onmessage = (e) => onMessage(e.data);
    ws.onclose = (e) => {
      this.sockets.delete(espId);
      if (!e.wasClean) {
        setTimeout(() => this.connect(url, espId, onMessage), 1000);
      }
    };
    ws.onerror = () => {
      ws.close();
    };

    this.sockets.set(espId, ws);
  }

  disconnect(espId: string) {
    const ws = this.sockets.get(espId);
    if (ws) {
      this.sockets.delete(espId);
      ws.close(1000, 'Client disconnect');
    }
  }
}
