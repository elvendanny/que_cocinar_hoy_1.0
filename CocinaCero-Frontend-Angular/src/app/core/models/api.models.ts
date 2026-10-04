export interface ApiValidationDetail {
  campo: string;
  mensaje: string;
}

export interface ApiErrorBody {
  error?: string;
  detalles?: ApiValidationDetail[];
}

export type UserRole = 'USER' | 'ADMIN';
export type UserStatus = 'PENDIENTE' | 'APROBADA' | 'RECHAZADA' | 'BLOQUEADA';

export interface User {
  id: number;
  nombre: string;
  email: string;
  rol: UserRole;
  estado: UserStatus;
  emailVerificado: boolean;
  creadoEn?: string;
  revisadoEn?: string | null;
  revisadoPor?: number | null;
  motivoRevision?: string | null;
}

export interface LoginResponse {
  token: string;
  tipo: 'Bearer';
  expiraEn: number;
  usuario: User;
}

export interface RegistrationResponse {
  usuario: User;
  mensaje: string;
}

export interface MessageResponse {
  mensaje: string;
}

export interface UserPage {
  datos: User[];
  total: number;
  page: number;
  limit: number;
}

export interface Revision {
  id: number;
  usuarioId: number;
  administradorId: number;
  estadoAnterior: UserStatus;
  estadoNuevo: UserStatus;
  motivo: string;
  fecha: string;
}

export interface Category {
  id: number;
  nombre: string;
  descripcion?: string | null;
}

export type IngredientType =
  | 'PROTEINA'
  | 'VEGETAL'
  | 'FRUTA'
  | 'LACTEO'
  | 'CEREAL'
  | 'CONDIMENTO'
  | 'OTRO';

export interface Ingredient {
  id: number;
  nombre: string;
  descripcion?: string | null;
  tipo: IngredientType;
}

export type Difficulty = 'FACIL' | 'MEDIA' | 'DIFICIL';
export type UnitMeasure =
  | 'GRAMOS'
  | 'MILILITROS'
  | 'UNIDADES'
  | 'TAZAS'
  | 'CUCHARADAS'
  | 'CUCHARADITAS'
  | 'AL_GUSTO';

export interface RecipeIngredient {
  ingredienteId: number;
  nombre: string;
  cantidad: number;
  unidadMedida: UnitMeasure;
  opcional: boolean;
}

export interface Attachment {
  id: number;
  nombreOriginal: string;
  tipo: string;
  bytes: number;
  recetaId?: number;
}

export interface RecipeImage {
  origen: 'ARCHIVO' | 'DEMO';
  archivoId?: number;
  tipo: string;
  url: string;
}

export interface Recipe {
  id: number;
  nombre: string;
  descripcion: string;
  instrucciones: string;
  tiempoPreparacion: number;
  porciones: number;
  dificultad: Difficulty;
  categoriaId: number;
  categoria: string;
  usuarioId: number;
  creadoEn: string;
  imagenPrincipal: RecipeImage;
  ingredientes: RecipeIngredient[];
  archivos: Attachment[];
  coincidencias?: number;
  porcentaje?: number;
}

export interface RecipePage {
  datos: Recipe[];
  total: number;
  page: number;
  limit: number;
  paginas: number;
}

export interface RecipePayloadIngredient {
  ingredienteId: number;
  cantidad: number;
  unidadMedida: UnitMeasure;
  opcional: boolean;
}

export interface RecipePayload {
  nombre: string;
  descripcion: string;
  instrucciones: string;
  tiempoPreparacion: number;
  porciones: number;
  dificultad: Difficulty;
  categoriaId: number;
  ingredientes: RecipePayloadIngredient[];
}

export interface AppOptions {
  dificultades: Difficulty[];
  tiposIngrediente: IngredientType[];
  unidadesMedida: UnitMeasure[];
  archivos: {
    maxBytes: number;
    maxPorReceta: number;
    tipos: string[];
  };
}
