import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Recipe } from '../../../core/models/api.models';
import { ProtectedImageComponent } from '../protected-image/protected-image.component';

@Component({
  selector: 'app-recipe-card',
  standalone: true,
  imports: [RouterLink, ProtectedImageComponent],
  templateUrl: './recipe-card.component.html',
  styleUrl: './recipe-card.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RecipeCardComponent {
  @Input({ required: true }) recipe!: Recipe;
  @Input() editable = false;
}
