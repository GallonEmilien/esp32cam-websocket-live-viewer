import { inject, Injectable, signal } from '@angular/core';
import {
  CameraHttpService,
  CreateCameraResponse,
} from './http/camera.http.service';
import { Observable } from 'rxjs';
import { Camera } from '../model/Camera';
import { tap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class CameraService {
  camerasSignal = signal<Camera[]>([]);
  private cameraHttpService = inject(CameraHttpService);

  constructor() {
    this.loadCameras();
  }

  createCamera(name: string): Observable<CreateCameraResponse> {
    return this.cameraHttpService.createCamera(name).pipe(
      tap(() => {
        this.loadCameras();
      }),
    );
  }

  getCameras(): Observable<Camera[]> {
    return this.cameraHttpService.getCameras();
  }

  getCameraById(id: string): Observable<Camera> {
    return this.cameraHttpService.getCameraById(id);
  }

  deleteCamera(id: string) {
    this.cameraHttpService
      .deleteCamera(id)
      .pipe(
        tap(() => {
          this.loadCameras();
        }),
      )
      .subscribe();
  }

  private loadCameras() {
    this.getCameras().subscribe((cameras) => {
      this.camerasSignal.set(cameras);
    });
  }
}
