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
  private lastFrameTimestamp = 0;
  private watchdogTimer?: any;

  @ViewChild('canvas') canvasRef!: ElementRef<HTMLCanvasElement>;
  private ctx: CanvasRenderingContext2D | null = null;

  private cameraService = inject(CameraViewerService);
  private authService = inject(AuthService);

  latestFrame = computed(() => {
    const id = this.cameraId();
    if (!id) return undefined;
    return this.cameraService.frames.get(id)?.();
  });

  constructor() {
    // 1. Establish connection when camera ID is provided
    effect(() => {
      const token = this.authService.getToken();
      const id = this.cameraId();
      if (id) {
        this.cameraService.connect(id, token ?? undefined);
      }
    });

    // 2. Reactively render each new frame as soon as it arrives (event-driven rendering)
    effect(() => {
      const frame = this.latestFrame();
      if (frame && this.ctx && this.canvasRef) {
        this.lastFrameTimestamp = Date.now();
        this.isActive.set(true);
        this.renderFrame(frame);
      }
    });
  }

  ngAfterViewInit() {
    this.ctx = this.canvasRef.nativeElement.getContext('2d');

    // Watchdog to toggle live indicator off if no frame received for 2.5s
    this.watchdogTimer = setInterval(() => {
      if (this.lastFrameTimestamp > 0 && Date.now() - this.lastFrameTimestamp > 2500) {
        this.isActive.set(false);
      }
    }, 1000);
  }

  ngOnDestroy() {
    if (this.watchdogTimer) {
      clearInterval(this.watchdogTimer);
    }

    // Fix critical bug: properly disconnect using active camera ID instead of hardcoded '0'
    const id = this.cameraId();
    if (id) {
      this.cameraService.disconnect(id);
    }
  }

  private renderFrame(frame: ImageBitmap) {
    if (!this.ctx || !this.canvasRef) return;
    const canvas = this.canvasRef.nativeElement;

    if (canvas.width !== frame.width) {
      canvas.width = frame.width;
    }
    if (canvas.height !== frame.height) {
      canvas.height = frame.height;
    }

    this.ctx.drawImage(frame, 0, 0, frame.width, frame.height);
  }
}
