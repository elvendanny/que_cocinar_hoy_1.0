import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  LoginResponse,
  MessageResponse,
  RegistrationResponse,
  Revision,
  User,
  UserPage,
  UserRole,
  UserStatus
} from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly base = environment.authUrl;

  constructor(private readonly http: HttpClient) {}

  health(): Observable<unknown> {
    return this.http.get(`${this.base}/health`);
  }

  register(payload: { nombre: string; email: string; password: string }): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>(`${this.base}/api/auth/registro`, payload);
  }

  verify(payload: { email: string; codigo: string }): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>(`${this.base}/api/auth/verificar`, payload);
  }

  resend(email: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.base}/api/auth/reenviar`, { email });
  }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.base}/api/auth/login`, { email, password });
  }

  me(): Observable<User> {
    return this.http.get<User>(`${this.base}/api/auth/me`);
  }

  updateProfile(nombre: string): Observable<User> {
    return this.http.patch<User>(`${this.base}/api/auth/me`, { nombre });
  }

  logout(): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.base}/api/auth/logout`, {});
  }

  listUsers(filters: {
    page?: number;
    limit?: number;
    texto?: string;
    estado?: UserStatus | '';
    emailVerificado?: boolean | null;
  }): Observable<UserPage> {
    let params = new HttpParams()
      .set('page', String(filters.page ?? 1))
      .set('limit', String(filters.limit ?? 20));
    if (filters.texto?.trim()) params = params.set('texto', filters.texto.trim());
    if (filters.estado) params = params.set('estado', filters.estado);
    if (filters.emailVerificado !== undefined && filters.emailVerificado !== null) {
      params = params.set('emailVerificado', String(filters.emailVerificado));
    }
    return this.http.get<UserPage>(`${this.base}/api/usuarios`, { params });
  }

  getUser(id: number): Observable<User> {
    return this.http.get<User>(`${this.base}/api/usuarios/${id}`);
  }

  updateStatus(id: number, estado: UserStatus, motivo: string): Observable<User> {
    return this.http.patch<User>(`${this.base}/api/usuarios/${id}/estado`, { estado, motivo });
  }

  updateRole(id: number, rol: UserRole): Observable<User> {
    return this.http.patch<User>(`${this.base}/api/usuarios/${id}/rol`, { rol });
  }

  revisions(id: number): Observable<Revision[]> {
    return this.http.get<Revision[]>(`${this.base}/api/usuarios/${id}/revisiones`);
  }

  roles(): Observable<UserRole[]> {
    return this.http.get<UserRole[]>(`${this.base}/api/roles`);
  }
}
