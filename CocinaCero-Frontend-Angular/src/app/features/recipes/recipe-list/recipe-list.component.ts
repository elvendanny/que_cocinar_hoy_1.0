import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin, finalize } from 'rxjs';
import { Category, Difficulty, RecipePage } from '../../../core/models/api.models';
import { CocinaService } from '../../../core/services/cocina.service';
import { SessionService } from '../../../core/services/session.service';
import { apiErrorMessage } from '../../../core/utils/api-error';
import { RecipeCardComponent } from '../../../shared/components/recipe-card/recipe-card.component';

@Component({
  selector: 'app-recipe-list',
  standalone: true,
  imports: [FormsModule, RouterLink, RecipeCardComponent],
  templateUrl: './recipe-list.component.html',
  styleUrl: './recipe-list.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RecipeListComponent implements OnInit {
  readonly loading = signal(true);
  readonly error = signal('');
  readonly categories = signal<Category[]>([]);
  readonly result = signal<RecipePage>({ datos: [], total: 0, page: 1, limit: 9, paginas: 0 });
  readonly mine: boolean;

  texto = '';
  categoriaId: number | null = null;
  dificultad: Difficulty | '' = '';
  page = 1;

  constructor(
    private readonly cocina: CocinaService,
    public readonly session: SessionService,
    route: ActivatedRoute
  ) {
    this.mine = route.snapshot.data['mine'] === true;
  }

  ngOnInit(): void {
    this.loading.set(true);
    forkJoin({ categories: this.cocina.categories(), recipes: this.fetchRecipes() })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ({ categories, recipes }) => {
          this.categories.set(categories);
          this.result.set(recipes);
        },
        error: (err) => this.error.set(apiErrorMessage(err))
      });
  }

  search(): void {
    this.page = 1;
    this.loadRecipes();
  }

  clear(): void {
    this.texto = '';
    this.categoriaId = null;
    this.dificultad = '';
    this.page = 1;
    this.loadRecipes();
  }

  goPage(page: number): void {
    if (page < 1 || page > Math.max(1, this.result().paginas) || page === this.page) return;
    this.page = page;
    this.loadRecipes();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  canEdit(recipeUserId: number): boolean {
    return this.session.isAdmin() || this.session.user()?.id === recipeUserId;
  }

  private fetchRecipes() {
    return this.cocina.recipesPage({
      texto: this.texto,
      categoriaId: this.categoriaId,
      dificultad: this.dificultad,
      mias: this.mine,
      page: this.page,
      limit: 9
    });
  }

  private loadRecipes(): void {
    this.loading.set(true);
    this.error.set('');
    this.fetchRecipes().pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (recipes) => this.result.set(recipes),
      error: (err) => this.error.set(apiErrorMessage(err))
    });
  }
}
