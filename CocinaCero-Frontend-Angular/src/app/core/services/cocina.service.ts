import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AppOptions,
  Attachment,
  Category,
  Difficulty,
  Ingredient,
  IngredientType,
  Recipe,
  RecipePage,
  RecipePayload
} from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class CocinaService {
  private readonly base = environment.cocinaUrl;

  constructor(private readonly http: HttpClient) {}

  health(): Observable<unknown> {
    return this.http.get(`${this.base}/health`);
  }

  options(): Observable<AppOptions> {
    return this.http.get<AppOptions>(`${this.base}/api/opciones`);
  }

  categories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.base}/api/categorias`);
  }

  createCategory(payload: { nombre: string; descripcion?: string }): Observable<Category> {
    return this.http.post<Category>(`${this.base}/api/categorias`, payload);
  }

  updateCategory(id: number, payload: { nombre: string; descripcion?: string }): Observable<Category> {
    return this.http.put<Category>(`${this.base}/api/categorias/${id}`, payload);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/api/categorias/${id}`);
  }

  ingredients(filters: { texto?: string; tipo?: IngredientType | '' } = {}): Observable<Ingredient[]> {
    let params = new HttpParams();
    if (filters.texto?.trim()) params = params.set('texto', filters.texto.trim());
    if (filters.tipo) params = params.set('tipo', filters.tipo);
    return this.http.get<Ingredient[]>(`${this.base}/api/ingredientes`, { params });
  }

  createIngredient(payload: { nombre: string; descripcion?: string; tipo: IngredientType }): Observable<Ingredient> {
    return this.http.post<Ingredient>(`${this.base}/api/ingredientes`, payload);
  }

  updateIngredient(id: number, payload: { nombre: string; descripcion?: string; tipo: IngredientType }): Observable<Ingredient> {
    return this.http.put<Ingredient>(`${this.base}/api/ingredientes/${id}`, payload);
  }

  deleteIngredient(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/api/ingredientes/${id}`);
  }

  recipesPage(filters: {
    texto?: string;
    categoriaId?: number | null;
    dificultad?: Difficulty | '';
    mias?: boolean;
    page?: number;
    limit?: number;
  }): Observable<RecipePage> {
    let params = new HttpParams()
      .set('page', String(filters.page ?? 1))
      .set('limit', String(filters.limit ?? 9))
      .set('mias', String(filters.mias ?? false));
    if (filters.texto?.trim()) params = params.set('texto', filters.texto.trim());
    if (filters.categoriaId) params = params.set('categoriaId', String(filters.categoriaId));
    if (filters.dificultad) params = params.set('dificultad', filters.dificultad);
    return this.http.get<RecipePage>(`${this.base}/api/recetas/pagina`, { params });
  }

  recipe(id: number): Observable<Recipe> {
    return this.http.get<Recipe>(`${this.base}/api/recetas/${id}`);
  }

  createRecipe(payload: RecipePayload): Observable<Recipe> {
    return this.http.post<Recipe>(`${this.base}/api/recetas`, payload);
  }

  updateRecipe(id: number, payload: RecipePayload): Observable<Recipe> {
    return this.http.put<Recipe>(`${this.base}/api/recetas/${id}`, payload);
  }

  deleteRecipe(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/api/recetas/${id}`);
  }

  recommendations(ingredientIds: number[]): Observable<Recipe[]> {
    const params = new HttpParams().set('ingredientes', ingredientIds.join(','));
    return this.http.get<Recipe[]>(`${this.base}/api/recomendaciones`, { params });
  }

  uploadFile(recipeId: number, file: File): Observable<Attachment> {
    const data = new FormData();
    data.append('archivo', file);
    return this.http.post<Attachment>(`${this.base}/api/recetas/${recipeId}/archivos`, data);
  }

  setCover(recipeId: number, archivoId: number | null): Observable<Recipe> {
    return this.http.patch<Recipe>(`${this.base}/api/recetas/${recipeId}/portada`, { archivoId });
  }

  deleteFile(fileId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/api/archivos/${fileId}`);
  }

  fileBlob(fileId: number): Observable<Blob> {
    return this.http.get(`${this.base}/api/archivos/${fileId}`, { responseType: 'blob' });
  }

  imageBlob(path: string): Observable<Blob> {
    return this.http.get(`${this.base}${path}`, { responseType: 'blob' });
  }
}
