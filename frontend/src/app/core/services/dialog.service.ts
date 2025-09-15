import { inject, Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { CreateCameraResponse } from './http/camera.http.service';
import { CameraCreationPopin } from '../../feat/camera/settings/add-camera/camera-creation-popin/camera-creation-popin';
import { CameraTokenResult } from '../../feat/camera/settings/add-camera/camera-token-result/camera-token-result';
import { ComponentType } from '@angular/cdk/portal';

@Injectable({ providedIn: 'root' })
export class DialogService {
  private matDialog = inject(MatDialog);

  openCameraCreation(): MatDialogRef<CameraCreationPopin> {
    return this.openDialog<CameraCreationPopin>(CameraCreationPopin);
  }

  openCameraResponse(createCameraResponse: CreateCameraResponse): MatDialogRef<CameraTokenResult> {
    return this.openDialog<CameraTokenResult>(
      CameraTokenResult,
      (instance) => instance.cameraInfo.set(createCameraResponse),
    );
  }

  private openDialog<T>(
    component: ComponentType<T>,
    init?: (instance: T) => void,
  ): MatDialogRef<T> {
    const matDialogRef = this.matDialog.open<T>(component, {
      disableClose: true
    });
    if (init) {
      init(matDialogRef.componentInstance);
    }
    return matDialogRef;
  }
}
