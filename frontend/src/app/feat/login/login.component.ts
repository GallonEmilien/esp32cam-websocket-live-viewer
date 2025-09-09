import { Component, signal, inject } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { Router } from "@angular/router";
import { AuthService } from "../../core/auth/auth.service";
import { CommonModule } from "@angular/common";
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import {TranslocoDirective} from '@ngneat/transloco';

@Component({
  selector: "app-login",
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    FormsModule,
    MatSlideToggleModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    TranslocoDirective
  ],
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.scss"],
})
export class LoginComponent {
  private router = inject(Router);
  private authService = inject(AuthService);

  username = signal<string | undefined>(undefined);
  password = signal<string | undefined>(undefined);
  loginError = signal<boolean>(false);

  onLogin() {
    const username = this.username();
    const password = this.password();

    if (!username || !password) {
      this.loginError.set(true);
      return;
    }

    this.authService.login(username, password).subscribe({
      next: () => {
        this.loginError.set(false);
        this.router.navigate(["/camera"]);
      },
      error: () => {
        this.loginError.set(true);
        this.username.set(undefined);
        this.password.set(undefined);
      },
    });
  }
}
