import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { AppOptions, Ingredient, IngredientType } from '../../../core/models/api.models';
import { CocinaService } from '../../../core/services/cocina.service';
import { apiErrorMessage } from '../../../core/utils/api-error';

@Component({
  selector: 'app-admin-ingredients',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './ingredients.component.html',
  styleUrl: './ingredients.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class IngredientsComponent implements OnInit {
  readonly ingredients = signal<Ingredient[]>([]);
  readonly options = signal<AppOptions | null>(null);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  editingId: number | null = null;
  nombre = '';
  descripcion = '';
  tipo: IngredientType = 'OTRO';
  texto = '';
  tipoFiltro: IngredientType | '' = '';

  constructor(private readonly cocina: CocinaService) {}

  ngOnInit(): void {
    forkJoin({ items: this.cocina.ingredients(), options: this.cocina.options() })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ({ items, options }) => {
          this.ingredients.set(items);
          this.options.set(options);
        },
        error: (err) => this.error.set(apiErrorMessage(err))
      });
  }

  search(): void {
    this.load();
  }

  clearFilters(): void {
    this.texto = '';
    this.tipoFiltro = '';
    this.load();
  }

  edit(item: Ingredient): void {
    this.editingId = item.id;
    this.nombre = item.nombre;
    this.descripcion = item.descripcion ?? '';
    this.tipo = item.tipo;
    this.error.set('');
    this.success.set('');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cancel(): void {
    this.editingId = null;
    this.nombre = '';
    this.descripcion = '';
    this.tipo = 'OTRO';
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
    const payload = { nombre, descripcion, tipo: this.tipo };
    const request = this.editingId
      ? this.cocina.updateIngredient(this.editingId, payload)
      : this.cocina.createIngredient(payload);
    request.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.success.set(this.editingId ? 'Ingrediente actualizado.' : 'Ingrediente creado.');
        this.cancel();
        this.load();
      },
      error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo guardar el ingrediente.'))
    });
  }

  remove(item: Ingredient): void {
    if (!window.confirm(`¿Eliminar “${item.nombre}”? El backend lo impedirá si está usado por alguna receta.`)) return;
    this.error.set('');
    this.success.set('');
    this.cocina.deleteIngredient(item.id).subscribe({
      next: () => {
        this.success.set('Ingrediente eliminado.');
        this.load();
      },
      error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo eliminar el ingrediente.'))
    });
  }

  private load(): void {
    this.loading.set(true);
    this.cocina.ingredients({ texto: this.texto, tipo: this.tipoFiltro })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (items) => this.ingredients.set(items),
        error: (err) => this.error.set(apiErrorMessage(err))
      });
  }
}
