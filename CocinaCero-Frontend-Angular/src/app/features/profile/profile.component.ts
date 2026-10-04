import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { SessionService } from '../../core/services/session.service';
import { apiErrorMessage } from '../../core/utils/api-error';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProfileComponent implements OnInit {
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly loggingOut = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  readonly form = this.fb.group({ nombre: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80)]] });

  constructor(
    private readonly fb: NonNullableFormBuilder,
    private readonly auth: AuthService,
    public readonly session: SessionService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.auth.me().pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (user) => {
        this.session.updateUser(user);
        this.form.patchValue({ nombre: user.nombre });
      },
      error: (err) => this.error.set(apiErrorMessage(err))
    });
  }

  save(): void {
    this.error.set('');
    this.success.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.auth.updateProfile(this.form.controls.nombre.value.trim()).pipe(finalize(() => this.saving.set(false))).subscribe({
      next: (user) => {
        this.session.updateUser(user);
        this.form.patchValue({ nombre: user.nombre });
        this.success.set('Nombre actualizado correctamente.');
      },
      error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo actualizar el perfil.'))
    });
  }

  logout(): void {
    this.loggingOut.set(true);
    this.error.set('');
    this.auth.logout().pipe(finalize(() => this.loggingOut.set(false))).subscribe({
      next: () => {
        this.session.clear();
        void this.router.navigate(['/login']);
      },
      error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo cerrar la sesión en el servidor.'))
    });
  }
}
