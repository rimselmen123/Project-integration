import { Component, Input, Output, EventEmitter, OnInit } from "@angular/core";
import { Category } from "../../models/category";
@Component({
  selector: "app-category-card",
  standalone: false,
  templateUrl: "./category-card.component.html",
})
export class CategoryCardComponent {
  @Input({ required: true }) category!: Category;
}
