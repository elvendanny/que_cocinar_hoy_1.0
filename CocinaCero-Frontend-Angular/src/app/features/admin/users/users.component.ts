import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { Revision, User, UserRole, UserStatus } from '../../../core/models/api.models';
import { AuthService } from '../../../core/services/auth.service';
import { SessionService } from '../../../core/services/session.service';
import { apiErrorMessage } from '../../../core/utils/api-error';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './users.component.html',
  styleUrl: './users.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UsersComponent implements OnInit {
  readonly users = signal<User[]>([]);
  readonly total = signal(0);
  readonly loading = signal(true);
  readonly actionBusy = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  readonly selectedUser = signal<User | null>(null);
  readonly revisions = signal<Revision[]>([]);
  readonly revisionsLoading = signal(false);

  page = 1;
  readonly limit = 15;
  texto = '';
  estado: UserStatus | '' = '';
  emailFilter: '' | 'true' | 'false' = '';
  decision: UserStatus = 'APROBADA';
  motivo = '';
  role: UserRole = 'USER';

  constructor(
    private readonly auth: AuthService,
    public readonly session: SessionService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  pages(): number {
    return Math.max(1, Math.ceil(this.total() / this.limit));
  }

  search(): void {
    this.page = 1;
    this.load();
  }

  clear(): void {
    this.texto = '';
    this.estado = '';
    this.emailFilter = '';
    this.page = 1;
    this.load();
  }

  goPage(value: number): void {
    if (value < 1 || value > this.pages()) return;
    this.page = value;
    this.load();
  }

  manage(user: User): void {
    this.selectedUser.set(user);
    this.decision = user.estado;
    this.role = user.rol;
    this.motivo = user.motivoRevision ?? '';
    this.loadRevisions(user.id);
  }

  closeManage(): void {
    this.selectedUser.set(null);
    this.revisions.set([]);
    this.motivo = '';
  }

  updateStatus(): void {
    const user = this.selectedUser();
    if (!user || !this.canManage(user)) return;
    if (!this.motivo.trim()) {
      this.error.set('Escribe un motivo para registrar el cambio de estado.');
      return;
    }
    this.actionBusy.set(true);
    this.error.set('');
    this.success.set('');
    this.auth.updateStatus(user.id, this.decision, this.motivo.trim())
      .pipe(finalize(() => this.actionBusy.set(false)))
      .subscribe({
        next: (updated) => {
          this.selectedUser.set(updated);
          this.success.set(`Estado de ${updated.nombre} actualizado a ${updated.estado}.`);
          this.load(false);
          this.loadRevisions(updated.id);
        },
        error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo cambiar el estado.'))
      });
  }

  updateRole(): void {
    const user = this.selectedUser();
    if (!user || !this.canManage(user)) return;
    if (!window.confirm(`¿Cambiar el rol de ${user.nombre} a ${this.role}? Sus sesiones actuales serán revocadas.`)) return;
    this.actionBusy.set(true);
    this.error.set('');
    this.success.set('');
    this.auth.updateRole(user.id, this.role).pipe(finalize(() => this.actionBusy.set(false))).subscribe({
      next: (updated) => {
        this.selectedUser.set(updated);
        this.success.set(`Rol actualizado a ${updated.rol}.`);
        this.load(false);
      },
      error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo cambiar el rol.'))
    });
  }

  canManage(user: User): boolean {
    return user.id !== this.session.user()?.id;
  }

  statusClass(status: UserStatus): string {
    if (status === 'APROBADA') return 'badge badge-green';
    if (status === 'PENDIENTE') return 'badge badge-gold';
    return 'badge badge-danger';
  }

  private load(showSpinner = true): void {
    if (showSpinner) this.loading.set(true);
    this.error.set('');
    const emailVerificado = this.emailFilter === '' ? null : this.emailFilter === 'true';
    this.auth.listUsers({ page: this.page, limit: this.limit, texto: this.texto, estado: this.estado, emailVerificado })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (result) => {
          this.users.set(result.datos);
          this.total.set(result.total);
        },
        error: (err) => this.error.set(apiErrorMessage(err))
      });
  }

  private loadRevisions(id: number): void {
    this.revisionsLoading.set(true);
    this.auth.revisions(id).pipe(finalize(() => this.revisionsLoading.set(false))).subscribe({
      next: (items) => this.revisions.set(items),
      error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo cargar el historial.'))
    });
  }
}
