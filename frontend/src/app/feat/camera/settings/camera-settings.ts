import { Component, inject, signal } from '@angular/core';
import {
  MatCard,
  MatCardContent,
  MatCardHeader,
  MatCardTitle,
} from '@angular/material/card';
import { TranslocoDirective } from '@ngneat/transloco';
import { PickCamera } from './add-camera/pick-camera/pick-camera';
import { SimpleButton } from '../../../shared/components/simple-button/simple-button';
import { CameraTokenDialogService } from '../../../core/services/camera-token-dialog.service';
import { CameraService } from '../../../core/services/camera.service';
import { Camera } from '../../../core/model/Camera';
import { Router } from '@angular/router';
import { Spacer } from '../../../shared/components/spacer/spacer';

@Component({
  selector: 'app-camera-settings',
  templateUrl: './camera-settings.html',
  styleUrl: './camera-settings.scss',
  imports: [
    MatCard,
    MatCardTitle,
    MatCardHeader,
    TranslocoDirective,
    MatCardContent,
    PickCamera,
    SimpleButton,
    Spacer,
  ],
  standalone: true,
})
export class CameraSettings {
  selectedCamera = signal<Camera | undefined>(undefined);
  private cameraService = inject(CameraService);
  private dialogsService = inject(CameraTokenDialogService);
  private routerService = inject(Router);

  add() {
    this.cameraService.createCamera().subscribe((res) => {
      this.dialogsService.open(res);
    });
  }

  navigate() {
    const camera = this.selectedCamera();
    if (camera !== undefined) {
      this.routerService.navigate(['/camera', camera.id]);
    }
  }

  delete() {
    const camera = this.selectedCamera();
    if (camera !== undefined) {
      this.cameraService.deleteCamera(camera.id);
    }
  }
}
