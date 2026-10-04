import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Ingredient, Recipe } from '../../core/models/api.models';
import { CocinaService } from '../../core/services/cocina.service';
import { SessionService } from '../../core/services/session.service';
import { apiErrorMessage } from '../../core/utils/api-error';
import { RecipeCardComponent } from '../../shared/components/recipe-card/recipe-card.component';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [FormsModule, RecipeCardComponent],
  templateUrl: './recommendations.component.html',
  styleUrl: './recommendations.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RecommendationsComponent implements OnInit {
  readonly ingredients = signal<Ingredient[]>([]);
  readonly selected = signal<Set<number>>(new Set());
  readonly recipes = signal<Recipe[]>([]);
  readonly loading = signal(true);
  readonly searching = signal(false);
  readonly error = signal('');
  query = '';

  constructor(
    private readonly cocina: CocinaService,
    public readonly session: SessionService
  ) {}

  ngOnInit(): void {
    this.cocina.ingredients().pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (items) => this.ingredients.set(items),
      error: (err) => this.error.set(apiErrorMessage(err))
    });
  }

  filteredIngredients(): Ingredient[] {
    const q = this.query.trim().toLowerCase();
    return !q ? this.ingredients() : this.ingredients().filter((item) => item.nombre.toLowerCase().includes(q));
  }

  toggle(id: number): void {
    const next = new Set(this.selected());
    next.has(id) ? next.delete(id) : next.add(id);
    this.selected.set(next);
  }

  isSelected(id: number): boolean {
    return this.selected().has(id);
  }

  clear(): void {
    this.selected.set(new Set());
    this.recipes.set([]);
  }

  recommend(): void {
    this.error.set('');
    const ids = [...this.selected()];
    if (!ids.length) {
      this.error.set('Selecciona al menos un ingrediente que tengas disponible.');
      return;
    }
    this.searching.set(true);
    this.cocina.recommendations(ids).pipe(finalize(() => this.searching.set(false))).subscribe({
      next: (recipes) => this.recipes.set(recipes),
      error: (err) => this.error.set(apiErrorMessage(err, 'No se pudieron calcular las recomendaciones.'))
    });
  }

  canEdit(userId: number): boolean {
    return this.session.isAdmin() || this.session.user()?.id === userId;
  }
}
