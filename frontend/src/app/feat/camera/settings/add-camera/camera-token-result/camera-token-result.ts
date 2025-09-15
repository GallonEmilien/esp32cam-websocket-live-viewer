import { Component, inject, signal } from '@angular/core';
import { CreateCameraResponse } from '../../../../../core/services/http/camera.http.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogRef } from '@angular/material/dialog';
import { TranslocoDirective } from '@ngneat/transloco';
import {CameraDialogWrapper} from '../../../../../shared/components/dialog-wrapper/dialog-wrapper';

@Component({
  selector: 'app-camera-token-result',
  standalone: true,
  templateUrl: './camera-token-result.html',
  styleUrl: './camera-token-result.scss',
  imports: [
    MatFormFieldModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    TranslocoDirective,
    CameraDialogWrapper,
  ],
})
export class CameraTokenResult {
  matDialogRef = inject(MatDialogRef<CameraTokenResult>);

  cameraInfo = signal<CreateCameraResponse | undefined>(undefined);

  showToken = signal(false);

  toggleTokenVisibility() {
    this.showToken.set(!this.showToken());
  }

  copyToClipboard(value: string | undefined) {
    if (!value) return;
    navigator.clipboard
      .writeText(value)
      .catch((err) => console.error('Failed to copy:', err));
  }

  closeModal() {
    this.matDialogRef.close();
  }
}
