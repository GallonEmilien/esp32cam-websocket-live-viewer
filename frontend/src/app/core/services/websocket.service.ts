import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private ws?: WebSocket;

  connect(url: string, onMessage: (data: ArrayBuffer) => void) {
    if (this.ws) this.ws.close();

    this.ws = new WebSocket(url);
    this.ws.binaryType = 'arraybuffer';

    this.ws.onmessage = (e) => onMessage(e.data);
    this.ws.onclose = () => setTimeout(() => this.connect(url, onMessage), 1000);
    this.ws.onerror = () => this.ws?.close();
  }

  disconnect() { this.ws?.close(); }
}
