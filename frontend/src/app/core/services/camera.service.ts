import {inject, Injectable, signal, WritableSignal} from '@angular/core';
import {WebSocketService} from './websocket.service';

@Injectable({ providedIn: 'root' })
export class CameraService {
  private wsService = inject(WebSocketService);

  frames = new Map<string, WritableSignal<ImageBitmap | undefined>>();

  connect(jwt: string, espId: string) {
    if (!this.frames.has(espId)) {
      this.frames.set(espId, signal<ImageBitmap | undefined>(undefined));
    }

    const url = `ws://localhost:8080/api/ws/camera?jwt=${jwt}&mode=CLIENT&espId=${espId}`;
    this.wsService.connect(url, espId, async (data) => {
      const blob = new Blob([data], { type: 'image/jpeg' });
      try {
        const img = await createImageBitmap(blob);
        this.frames.get(espId)?.set(img);
      } catch (err) {
        console.error(err);
      }
    });
  }

  disconnect(espId: string) { this.wsService.disconnect(espId); }
}
