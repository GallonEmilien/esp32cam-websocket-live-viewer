import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private sockets = new Map<string, WebSocket>();

  connect(
    url: string,
    espId: string,
    onMessage: (data: ArrayBuffer) => void,
    onClose?: (wasClean: boolean) => void
  ) {
    this.disconnect(espId);

    const ws = new WebSocket(url);
    ws.binaryType = 'arraybuffer';

    ws.onmessage = (e) => {
      if (e.data instanceof ArrayBuffer) {
        onMessage(e.data);
      }
    };

    ws.onclose = (e) => {
      this.sockets.delete(espId);
      if (onClose) {
        onClose(e.wasClean);
      }
    };

    ws.onerror = (err) => {
      console.warn(`WebSocket error on camera ${espId}:`, err);
      ws.close();
    };

    this.sockets.set(espId, ws);
  }

  disconnect(espId: string) {
    const ws = this.sockets.get(espId);
    if (ws) {
      this.sockets.delete(espId);
      // Remove handlers before close to avoid duplicate callbacks
      ws.onclose = null;
      ws.onerror = null;
      ws.onmessage = null;
      ws.close(1000, 'Client disconnect');
    }
  }

  isConnected(espId: string): boolean {
    const ws = this.sockets.get(espId);
    return !!ws && ws.readyState === WebSocket.OPEN;
  }
}
