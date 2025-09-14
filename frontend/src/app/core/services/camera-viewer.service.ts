import { inject, Injectable, signal, WritableSignal } from '@angular/core';
import { WebSocketService } from './websocket.service';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CameraViewerService {
  frames = new Map<string, WritableSignal<ImageBitmap | undefined>>();
  private wsService = inject(WebSocketService);

  connect(jwt: string, espId: string) {
    if (!this.frames.has(espId)) {
      this.frames.set(espId, signal<ImageBitmap | undefined>(undefined));
    }

    let urlHost: string;
    if (environment.production) urlHost = window.location.host;
    else urlHost = 'localhost:8080';

    const url = `ws://${urlHost}/api/ws/camera?jwt=${jwt}&mode=CLIENT&espId=${espId}`;

    console.log(url);

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

  disconnect(espId: string) {
    this.wsService.disconnect(espId);
  }
}
