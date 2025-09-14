import { inject, Injectable, signal, WritableSignal } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import {AuthHttpService} from './http/auth.http.service';

interface AuthResponse {
  token: string;
  expiresAt?: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = '/api/auth/login';
  private readonly TOKEN_KEY = 'jwt_token';
  private readonly EXPIRES_KEY = 'jwt_expires';

  private authHttpService = inject(AuthHttpService);

  token: WritableSignal<string | null> = signal(this.getTokenFromStorage());

  login(username: string, password: string): Observable<AuthResponse> {
    return this.authHttpService.login(username, password).pipe(
      tap(({ token, expiresAt }) => {
        this.storeToken(token, expiresAt ?? Date.now() + 3600000);
      }),
      catchError(err => {
        console.error('Login failed:', err);
        return throwError(() => err);
      })
    );
  }

  logout(): void {
    this.token.set(null);
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.EXPIRES_KEY);
  }

  getToken(): string | null {
    if (this.isTokenExpired()) {
      this.logout();
      return null;
    }
    return this.token();
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  private getTokenFromStorage(): string | null {
    const t = localStorage.getItem(this.TOKEN_KEY);
    return t && !this.isTokenExpired() ? t : null;
  }

  private storeToken(token: string, expiresAt: number): void {
    this.token.set(token);
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.EXPIRES_KEY, expiresAt.toString());
  }

  private isTokenExpired(): boolean {
    const expiresAt = localStorage.getItem(this.EXPIRES_KEY);
    return !expiresAt || Date.now() >= +expiresAt;
  }
}
