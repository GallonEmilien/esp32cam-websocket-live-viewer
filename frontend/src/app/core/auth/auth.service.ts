import {inject, Injectable, signal} from '@angular/core';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import {HttpClient} from '@angular/common/http';

interface AuthResponse {
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = '/api/auth/login';

  private token = signal<string | null>(localStorage.getItem('jwt'));

  private http = inject(HttpClient);

  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.apiUrl, { username, password }).pipe(
      tap(res => this.token.set(res.token))
    );
  }

  logout() {
    this.token.set(null);
    localStorage.removeItem('jwt');
  }

  getToken() { return this.token(); }
  isLoggedIn() { return this.token() != null; }
}
