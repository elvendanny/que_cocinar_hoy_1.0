import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { concatMap, finalize, from, switchMap, toArray } from 'rxjs';
import { Attachment, Recipe } from '../../../core/models/api.models';
import { CocinaService } from '../../../core/services/cocina.service';
import { SessionService } from '../../../core/services/session.service';
import { apiErrorMessage } from '../../../core/utils/api-error';
import { ProtectedImageComponent } from '../../../shared/components/protected-image/protected-image.component';

@Component({
  selector: 'app-recipe-detail',
  standalone: true,
  imports: [RouterLink, ProtectedImageComponent, DatePipe, DecimalPipe],
  templateUrl: './recipe-detail.component.html',
  styleUrl: './recipe-detail.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RecipeDetailComponent implements OnInit {
  readonly recipe = signal<Recipe | null>(null);
  readonly loading = signal(true);
  readonly deleting = signal(false);
  readonly error = signal('');
  readonly downloading = signal<number | null>(null);
  private readonly id = Number(this.route.snapshot.paramMap.get('id'));

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly cocina: CocinaService,
    public readonly session: SessionService
  ) {}

  ngOnInit(): void {
    if (!Number.isInteger(this.id) || this.id <= 0) {
      void this.router.navigate(['/recetas']);
      return;
    }
    this.load();
  }

  canEdit(): boolean {
    const recipe = this.recipe();
    return !!recipe && (this.session.isAdmin() || this.session.user()?.id === recipe.usuarioId);
  }

  download(file: Attachment): void {
    this.error.set('');
    this.downloading.set(file.id);
    this.cocina.fileBlob(file.id).pipe(finalize(() => this.downloading.set(null))).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = file.nombreOriginal;
        anchor.click();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
      },
      error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo descargar el archivo.'))
    });
  }

  deleteRecipe(): void {
    const recipe = this.recipe();
    if (!recipe || !this.canEdit()) return;
    const message = recipe.archivos.length
      ? `Esta receta tiene ${recipe.archivos.length} archivo(s). La web eliminará primero esos archivos y después la receta. ¿Continuar?`
      : '¿Eliminar esta receta definitivamente?';
    if (!window.confirm(message)) return;

    this.deleting.set(true);
    this.error.set('');
    from(recipe.archivos).pipe(
      concatMap((file) => this.cocina.deleteFile(file.id)),
      toArray(),
      switchMap(() => this.cocina.deleteRecipe(recipe.id)),
      finalize(() => this.deleting.set(false))
    ).subscribe({
      next: () => void this.router.navigate(['/mis-recetas']),
      error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo eliminar la receta.'))
    });
  }

  imageFiles(): Attachment[] {
    return this.recipe()?.archivos.filter((file) => file.tipo === 'image/png' || file.tipo === 'image/jpeg') ?? [];
  }

  private load(): void {
    this.loading.set(true);
    this.cocina.recipe(this.id).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (recipe) => this.recipe.set(recipe),
      error: (err) => this.error.set(apiErrorMessage(err))
    });
  }
}
