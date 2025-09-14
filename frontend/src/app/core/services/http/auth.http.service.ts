import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AuthResponse {
  token: string;
  expiresAt?: number;
}

@Injectable({ providedIn: 'root' })
export class AuthHttpService {
  private http = inject(HttpClient);
  private apiUrl = '/api/auth/login';

  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.apiUrl, { username, password });
  }
}
