import { inject, Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { CameraTokenResult } from '../../feat/camera/settings/add-camera/camera-token-result/camera-token-result';
import { CreateCameraResponse } from './http/camera.http.service';

@Injectable({ providedIn: 'root' })
export class CameraTokenDialogService {
  matDialog = inject(MatDialog);

  open(
    createCameraResponse: CreateCameraResponse,
  ): MatDialogRef<CameraTokenResult> {
    const matDialogRef = this.matDialog.open(CameraTokenResult, {
      disableClose: true,
    });
    matDialogRef.componentInstance.cameraInfo.set(createCameraResponse);
    return matDialogRef;
  }
}
