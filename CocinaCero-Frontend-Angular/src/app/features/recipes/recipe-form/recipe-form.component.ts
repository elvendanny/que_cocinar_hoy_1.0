import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, UntypedFormArray, UntypedFormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { concatMap, finalize, forkJoin, from, Observable, of, switchMap, toArray } from 'rxjs';
import {
  AppOptions,
  Attachment,
  Category,
  Ingredient,
  Recipe,
  RecipePayload
} from '../../../core/models/api.models';
import { CocinaService } from '../../../core/services/cocina.service';
import { SessionService } from '../../../core/services/session.service';
import { apiErrorMessage } from '../../../core/utils/api-error';

@Component({
  selector: 'app-recipe-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './recipe-form.component.html',
  styleUrl: './recipe-form.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RecipeFormComponent implements OnInit {
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly fileBusy = signal<number | null>(null);
  readonly error = signal('');
  readonly categories = signal<Category[]>([]);
  readonly ingredients = signal<Ingredient[]>([]);
  readonly options = signal<AppOptions | null>(null);
  readonly currentRecipe = signal<Recipe | null>(null);
  readonly existingFiles = signal<Attachment[]>([]);
  readonly selectedFiles = signal<File[]>([]);

  readonly id = Number(this.route.snapshot.paramMap.get('id')) || null;
  readonly editing = this.id !== null;

  readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    descripcion: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(500)]],
    instrucciones: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(20000)]],
    tiempoPreparacion: [20, [Validators.required, Validators.min(1), Validators.max(1440)]],
    porciones: [2, [Validators.required, Validators.min(1), Validators.max(100)]],
    dificultad: ['FACIL', Validators.required],
    categoriaId: [null, Validators.required],
    ingredientes: this.fb.array([])
  });

  constructor(
    private readonly fb: UntypedFormBuilder,
    private readonly cocina: CocinaService,
    private readonly session: SessionService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  get ingredientsArray(): UntypedFormArray {
    return this.form.get('ingredientes') as UntypedFormArray;
  }

  ngOnInit(): void {
    if (this.editing && (!Number.isInteger(this.id) || (this.id ?? 0) <= 0)) {
      void this.router.navigate(['/recetas']);
      return;
    }

    const recipe$: Observable<Recipe | null> = this.editing && this.id
      ? this.cocina.recipe(this.id)
      : of(null);

    forkJoin({
      categories: this.cocina.categories(),
      ingredients: this.cocina.ingredients(),
      options: this.cocina.options(),
      recipe: recipe$
    }).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: ({ categories, ingredients, options, recipe }) => {
        this.categories.set(categories);
        this.ingredients.set(ingredients);
        this.options.set(options);
        if (recipe) {
          if (!this.session.isAdmin() && this.session.user()?.id !== recipe.usuarioId) {
            this.error.set('Solo el propietario o ADMIN puede editar esta receta.');
            void this.router.navigate(['/recetas', recipe.id]);
            return;
          }
          this.fill(recipe);
        } else {
          this.addIngredient();
        }
      },
      error: (err) => this.error.set(apiErrorMessage(err))
    });
  }

  addIngredient(value?: { ingredienteId: number; cantidad: number; unidadMedida: string; opcional: boolean }): void {
    if (this.ingredientsArray.length >= 50) return;
    this.ingredientsArray.push(this.fb.group({
      ingredienteId: [value?.ingredienteId ?? null, Validators.required],
      cantidad: [value?.cantidad ?? 1, [Validators.required, Validators.min(0.01), Validators.max(99999999.99)]],
      unidadMedida: [value?.unidadMedida ?? 'UNIDADES', Validators.required],
      opcional: [value?.opcional ?? false]
    }));
  }

  removeIngredient(index: number): void {
    if (this.ingredientsArray.length <= 1) return;
    this.ingredientsArray.removeAt(index);
  }

  chooseFiles(event: Event): void {
    this.error.set('');
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files ?? []);
    input.value = '';
    if (!files.length) return;

    const rules = this.options()?.archivos;
    const maxBytes = rules?.maxBytes ?? 5 * 1024 * 1024;
    const allowed = new Set(rules?.tipos ?? ['image/png', 'image/jpeg', 'application/pdf']);
    const maxCount = rules?.maxPorReceta ?? 10;
    const currentCount = this.existingFiles().length + this.selectedFiles().length;

    for (const file of files) {
      if (!allowed.has(file.type)) {
        this.error.set(`“${file.name}” no es PNG, JPEG ni PDF.`);
        return;
      }
      if (file.size > maxBytes) {
        this.error.set(`“${file.name}” supera el máximo de 5 MB.`);
        return;
      }
    }
    if (currentCount + files.length > maxCount) {
      this.error.set(`Solo se permiten ${maxCount} archivos por receta.`);
      return;
    }
    this.selectedFiles.set([...this.selectedFiles(), ...files]);
  }

  removeSelectedFile(index: number): void {
    this.selectedFiles.set(this.selectedFiles().filter((_, i) => i !== index));
  }

  deleteExistingFile(file: Attachment): void {
    if (!window.confirm(`¿Eliminar el archivo “${file.nombreOriginal}”?`)) return;
    this.fileBusy.set(file.id);
    this.error.set('');
    this.cocina.deleteFile(file.id).pipe(
      switchMap(() => this.id ? this.cocina.recipe(this.id) : of(null)),
      finalize(() => this.fileBusy.set(null))
    ).subscribe({
      next: (recipe) => {
        if (recipe) {
          this.currentRecipe.set(recipe);
          this.existingFiles.set(recipe.archivos);
        }
      },
      error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo eliminar el archivo.'))
    });
  }

  setCover(fileId: number | null): void {
    if (!this.id) return;
    this.fileBusy.set(fileId ?? -1);
    this.error.set('');
    this.cocina.setCover(this.id, fileId).pipe(finalize(() => this.fileBusy.set(null))).subscribe({
      next: (recipe) => this.currentRecipe.set(recipe),
      error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo cambiar la portada.'))
    });
  }

  submit(): void {
    this.error.set('');
    if (this.form.invalid || this.ingredientsArray.length === 0) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const selectedIds = raw.ingredientes.map((item: { ingredienteId: number }) => Number(item.ingredienteId));
    if (new Set(selectedIds).size !== selectedIds.length) {
      this.error.set('No repitas ingredientes dentro de la misma receta.');
      return;
    }

    const payload: RecipePayload = {
      nombre: String(raw.nombre).trim(),
      descripcion: String(raw.descripcion).trim(),
      instrucciones: String(raw.instrucciones).trim(),
      tiempoPreparacion: Number(raw.tiempoPreparacion),
      porciones: Number(raw.porciones),
      dificultad: raw.dificultad,
      categoriaId: Number(raw.categoriaId),
      ingredientes: raw.ingredientes.map((item: { ingredienteId: number; cantidad: number; unidadMedida: string; opcional: boolean }) => ({
        ingredienteId: Number(item.ingredienteId),
        cantidad: Number(item.cantidad),
        unidadMedida: item.unidadMedida,
        opcional: Boolean(item.opcional)
      }))
    } as RecipePayload;

    this.saving.set(true);
    const save$ = this.editing && this.id
      ? this.cocina.updateRecipe(this.id, payload)
      : this.cocina.createRecipe(payload);

    save$.pipe(
      switchMap((recipe) => {
        const pending = this.selectedFiles();
        if (!pending.length) return of(recipe);
        return from(pending).pipe(
          concatMap((file) => this.cocina.uploadFile(recipe.id, file)),
          toArray(),
          switchMap(() => this.cocina.recipe(recipe.id))
        );
      }),
      finalize(() => this.saving.set(false))
    ).subscribe({
      next: (recipe) => {
        this.selectedFiles.set([]);
        void this.router.navigate(['/recetas', recipe.id]);
      },
      error: (err) => this.error.set(apiErrorMessage(err, 'No se pudo guardar completamente la receta. Revisa los datos y archivos.'))
    });
  }

  isCurrentCover(fileId: number): boolean {
    return this.currentRecipe()?.imagenPrincipal.archivoId === fileId;
  }

  isImage(file: Attachment): boolean {
    return file.tipo === 'image/png' || file.tipo === 'image/jpeg';
  }

  private fill(recipe: Recipe): void {
    this.currentRecipe.set(recipe);
    this.existingFiles.set(recipe.archivos);
    this.form.patchValue({
      nombre: recipe.nombre,
      descripcion: recipe.descripcion,
      instrucciones: recipe.instrucciones,
      tiempoPreparacion: recipe.tiempoPreparacion,
      porciones: recipe.porciones,
      dificultad: recipe.dificultad,
      categoriaId: recipe.categoriaId
    });
    this.ingredientsArray.clear();
    recipe.ingredientes.forEach((item) => this.addIngredient({
      ingredienteId: item.ingredienteId,
      cantidad: item.cantidad,
      unidadMedida: item.unidadMedida,
      opcional: item.opcional
    }));
  }
}
