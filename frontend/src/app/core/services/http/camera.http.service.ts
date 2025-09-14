import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Camera } from '../../model/Camera';

export interface CreateCameraResponse {
  cameraId: string;
  cameraToken: string;
}

@Injectable({ providedIn: 'root' })
export class CameraHttpService {
  private http = inject(HttpClient);
  private apiUrl = '/api/camera';

  createCamera(): Observable<CreateCameraResponse> {
    return this.http.post<CreateCameraResponse>(this.apiUrl, null);
  }

  getCameras(): Observable<Camera[]> {
    return this.http.get<Camera[]>(this.apiUrl);
  }

  getCameraById(id: string): Observable<Camera> {
    return this.http.get<Camera>(`${this.apiUrl}/${id}`);
  }

  deleteCamera(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
