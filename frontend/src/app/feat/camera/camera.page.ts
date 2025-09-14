import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CameraViewerComponent } from './viewer/camera-viewer.component';

@Component({
  imports: [CameraViewerComponent],
  templateUrl: './camera.page.html',
  standalone: true,
  styleUrls: ['./camera.page.scss'],
})
export class CameraPage {
  cameraId = signal<string | undefined>(undefined);
  private route = inject(ActivatedRoute);

  constructor() {
    const cameraId = this.route.snapshot.paramMap.get('id');
    if (cameraId) this.cameraId.set(cameraId);
  }
}
