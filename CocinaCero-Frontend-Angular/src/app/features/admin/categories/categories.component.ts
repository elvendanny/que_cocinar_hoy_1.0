import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { Category } from '../../../core/models/api.models';
import { CocinaService } from '../../../core/services/cocina.service';
import { apiErrorMessage } from '../../../core/utils/api-error';

@Component({
  selector: 'app-admin-categories',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './categories.component.html',
  styleUrl: './categories.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CategoriesComponent implements OnInit {
  readonly categories = signal<Category[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  editingId: number | null = null;
  nombre = '';
  descripcion = '';

  constructor(private readonly cocina: CocinaService) {}

  ngOnInit(): void {
    this.load();
  }

  edit(item: Category): void {
    this.editingId = item.id;
    this.nombre = item.nombre;
    this.descripcion = item.descripcion ?? '';
    this.error.set('');
    this.success.set('');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cancel(): void {
    this.editingId = null;
    this.nombre = '';
    this.descripcion = '';
  }

  save(): void {
    this.error.set('');
    this.success.set('');
    const nombre = this.nombre.trim();
    const descripcion = this.descripcion.trim();
    if (nombre.length < 2 || nombre.length > 80) {
      this.error.set('El nombre debe tener entre 2 y 80 caracteres.');
      return;
    }
    if (descripcion.length > 255) {
      this.error.set('La descripción no puede superar 255 caracteres.');
      return;
    }
    this.saving.set(true);
    const request = this.editingId
      ? this.cocina.updateCategory(this.editingId, { nombre, descripcion })
      : this.cocina.createCategory({ nombre, descripcion });
    request.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.success.set(this.editingId ? 'Categoría actualizada.' : 'Categoría creada.');
        this.cancel();
        this.load(false);
      },
      error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo guardar la categoría.'))
    });
  }

  remove(item: Category): void {
    if (!window.confirm(`¿Eliminar la categoría “${item.nombre}”? Solo se puede borrar si ninguna receta la utiliza.`)) return;
    this.error.set('');
    this.success.set('');
    this.cocina.deleteCategory(item.id).subscribe({
      next: () => {
        this.success.set('Categoría eliminada.');
        this.load(false);
      },
      error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo eliminar la categoría.'))
    });
  }

  private load(showSpinner = true): void {
    if (showSpinner) this.loading.set(true);
    this.cocina.categories().pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (items) => this.categories.set(items),
      error: (err) => this.error.set(apiErrorMessage(err))
    });
  }
}
