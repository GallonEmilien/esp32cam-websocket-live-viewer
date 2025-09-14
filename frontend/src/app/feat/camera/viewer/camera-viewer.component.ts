import {
  AfterViewInit,
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  input,
  OnDestroy,
  signal,
  ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { CameraViewerService } from '../../../core/services/camera-viewer.service';
import { AuthService } from '../../../core/services/auth.service';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslocoDirective } from '@ngneat/transloco';
import {
  MatCard,
  MatCardContent,
  MatCardHeader,
  MatCardTitle,
} from '@angular/material/card';

@Component({
  selector: 'app-camera-viewer',
  standalone: true,
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    TranslocoDirective,
    MatCardContent,
    MatCardTitle,
    MatCardHeader,
    MatCard,
  ],
  templateUrl: './camera-viewer.component.html',
  styleUrls: ['./camera-viewer.component.scss'],
})
export class CameraViewerComponent implements AfterViewInit, OnDestroy {
  cameraId = input<string | undefined>(undefined);
  isActive = signal(false);
  @ViewChild('canvas') canvasRef!: ElementRef<HTMLCanvasElement>;
  lastPing = computed(() => {
    if (this.latestFrame()) {
      return Date.now();
    }
    return 0;
  });
  private cameraService = inject(CameraViewerService);
  latestFrame = computed(() => {
    const cameraId = this.cameraId();
    if (!cameraId) return undefined;
    return this.cameraService.frames.get(cameraId)?.();
  });
  private authService = inject(AuthService);
  private ctx!: CanvasRenderingContext2D;

  private animationId!: number;

  constructor() {
    effect(() => {
      const token = this.authService.getToken();
      const cameraId = this.cameraId();
      if (token && cameraId) {
        this.cameraService.connect(token, cameraId);
      }
    });
  }

  ngAfterViewInit() {
    this.ctx = this.canvasRef.nativeElement.getContext('2d')!;
    this.drawLoop();
  }

  ngOnDestroy() {
    cancelAnimationFrame(this.animationId);
    this.cameraService.disconnect('0');
  }

  private drawLoop() {
    const frame = this.latestFrame();
    this.isActive.set(Date.now() - this.lastPing() < 2000);
    if (frame && this.ctx) {
      const canvas = this.canvasRef.nativeElement;
      if (canvas.width !== frame.width) canvas.width = frame.width;
      if (canvas.height !== frame.height) canvas.height = frame.height;
      this.ctx.drawImage(frame, 0, 0, frame.width, frame.height);
    }
    this.animationId = requestAnimationFrame(() => this.drawLoop());
  }
}
