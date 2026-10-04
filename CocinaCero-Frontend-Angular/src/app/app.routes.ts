import { Routes } from '@angular/router';
import { adminGuard } from './core/guards/admin.guard';
import { authGuard } from './core/guards/auth.guard';
import { CategoriesComponent } from './features/admin/categories/categories.component';
import { IngredientsComponent } from './features/admin/ingredients/ingredients.component';
import { UsersComponent } from './features/admin/users/users.component';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { VerifyComponent } from './features/auth/verify/verify.component';
import { HomeComponent } from './features/home/home.component';
import { NotFoundComponent } from './features/not-found/not-found.component';
import { ProfileComponent } from './features/profile/profile.component';
import { RecipeDetailComponent } from './features/recipes/recipe-detail/recipe-detail.component';
import { RecipeFormComponent } from './features/recipes/recipe-form/recipe-form.component';
import { RecipeListComponent } from './features/recipes/recipe-list/recipe-list.component';
import { RecommendationsComponent } from './features/recommendations/recommendations.component';

export const routes: Routes = [
  { path: '', component: HomeComponent, title: 'Cocina Cero' },
  { path: 'login', component: LoginComponent, title: 'Ingresar | Cocina Cero' },
  { path: 'registro', component: RegisterComponent, title: 'Registro | Cocina Cero' },
  { path: 'verificar', component: VerifyComponent, title: 'Verificar correo | Cocina Cero' },

  { path: 'recetas', component: RecipeListComponent, canActivate: [authGuard], data: { mine: false }, title: 'Recetas | Cocina Cero' },
  { path: 'mis-recetas', component: RecipeListComponent, canActivate: [authGuard], data: { mine: true }, title: 'Mis recetas | Cocina Cero' },
  { path: 'recetas/nueva', component: RecipeFormComponent, canActivate: [authGuard], title: 'Nueva receta | Cocina Cero' },
  { path: 'recetas/:id/editar', component: RecipeFormComponent, canActivate: [authGuard], title: 'Editar receta | Cocina Cero' },
  { path: 'recetas/:id', component: RecipeDetailComponent, canActivate: [authGuard], title: 'Detalle de receta | Cocina Cero' },
  { path: 'que-cocinar', component: RecommendationsComponent, canActivate: [authGuard], title: 'Qué cocinar | Cocina Cero' },
  { path: 'perfil', component: ProfileComponent, canActivate: [authGuard], title: 'Mi perfil | Cocina Cero' },

  { path: 'admin/usuarios', component: UsersComponent, canActivate: [adminGuard], title: 'Usuarios | Cocina Cero' },
  { path: 'admin/categorias', component: CategoriesComponent, canActivate: [adminGuard], title: 'Categorías | Cocina Cero' },
  { path: 'admin/ingredientes', component: IngredientsComponent, canActivate: [adminGuard], title: 'Ingredientes | Cocina Cero' },

  { path: '**', component: NotFoundComponent, title: 'Página no encontrada | Cocina Cero' }
];
