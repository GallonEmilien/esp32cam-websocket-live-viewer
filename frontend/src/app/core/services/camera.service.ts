import {inject, Injectable, signal} from '@angular/core';
import {WebSocketService} from './websocket.service';

@Injectable({ providedIn: 'root' })
export class CameraService {
  latestFrame = signal<ImageBitmap | undefined>(undefined);

  private wsService = inject(WebSocketService);

  connect(jwt: string) {
    const url = `ws://localhost:8080/api/ws/camera?jwt=${jwt}&apiKey=CLIENT`;
    this.wsService.connect(url, async (data) => {
      const blob = new Blob([data], { type: 'image/jpeg' });
      try { this.latestFrame.set(await createImageBitmap(blob)); }
      catch (err) { console.error(err); }
    });
  }

  disconnect() { this.wsService.disconnect(); }
}
