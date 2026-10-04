import { computed, Injectable, signal } from '@angular/core';
import { LoginResponse, User } from '../models/api.models';

const TOKEN_KEY = 'cocina-cero-token';
const USER_KEY = 'cocina-cero-user';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly tokenState = signal<string | null>(this.readToken());
  private readonly userState = signal<User | null>(this.readUser());

  readonly token = this.tokenState.asReadonly();
  readonly user = this.userState.asReadonly();
  readonly isLoggedIn = computed(() => !!this.tokenState() && !!this.userState());
  readonly isAdmin = computed(() => this.userState()?.rol === 'ADMIN');

  setLogin(response: LoginResponse): void {
    this.tokenState.set(response.token);
    this.userState.set(response.usuario);
    if (typeof sessionStorage !== 'undefined') {
      sessionStorage.setItem(TOKEN_KEY, response.token);
      sessionStorage.setItem(USER_KEY, JSON.stringify(response.usuario));
    }
  }

  updateUser(user: User): void {
    this.userState.set(user);
    if (typeof sessionStorage !== 'undefined') {
      sessionStorage.setItem(USER_KEY, JSON.stringify(user));
    }
  }

  clear(): void {
    this.tokenState.set(null);
    this.userState.set(null);
    if (typeof sessionStorage !== 'undefined') {
      sessionStorage.removeItem(TOKEN_KEY);
      sessionStorage.removeItem(USER_KEY);
    }
  }

  private readToken(): string | null {
    return typeof sessionStorage !== 'undefined' ? sessionStorage.getItem(TOKEN_KEY) : null;
  }

  private readUser(): User | null {
    if (typeof sessionStorage === 'undefined') return null;
    const raw = sessionStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as User;
    } catch {
      sessionStorage.removeItem(USER_KEY);
      return null;
    }
  }
}
