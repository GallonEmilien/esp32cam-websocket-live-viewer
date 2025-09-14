import { Component, inject } from '@angular/core';
import { MatToolbar } from '@angular/material/toolbar';
import { MatButton } from '@angular/material/button';
import { TranslocoDirective } from '@ngneat/transloco';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-header',
  templateUrl: './header.html',
  standalone: true,
  imports: [MatToolbar, MatButton, TranslocoDirective],
  styleUrl: './header.scss',
})
export class Header {
  private authService = inject(AuthService);
  private router = inject(Router);

  home() {
    this.router.navigate(['/home']);
  }

  logout() {
    this.authService.logout();
  }
}
