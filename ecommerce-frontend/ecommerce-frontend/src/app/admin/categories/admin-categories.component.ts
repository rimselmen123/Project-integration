import { Component, Input, Output, EventEmitter, OnInit } from "@angular/core";
import { Product } from "../../models/product";
import { Category } from "../../models/category";
import { ProductService } from "../../services/product.service";
import { CategoryService } from "../../services/category.service";
import { CartService } from "../../services/cart.service";
import { AuthService } from "../../services/auth.service";
import { Router, ActivatedRoute } from "@angular/router";
import { forkJoin, finalize } from "rxjs";
import { errorMessage } from "../../services/error-message";
@Component({
  selector: "app-admin-categories",
  standalone: false,
  templateUrl: "./admin-categories.component.html",
})
export class AdminCategoriesComponent {
  categories: Category[] = [];
  form: Partial<Category> = {};
  loading = true;
  busy = false;
  error = "";
  message = "";
  constructor(private cs: CategoryService) {}
  ngOnInit() {
    this.reset();
    this.load();
  }
  reset() {
    this.form = { name: "", description: "" };
  }
  load() {
    this.cs
      .getAll()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (c) => (this.categories = c),
        error: (e) => (this.error = errorMessage(e)),
      });
  }
  save() {
    if (this.busy) return;
    this.busy = true;
    this.error = "";
    this.cs
      .save(this.form)
      .pipe(finalize(() => (this.busy = false)))
      .subscribe({
        next: () => {
          this.message = "Catégorie enregistrée.";
          this.reset();
          this.load();
        },
        error: (e) => (this.error = errorMessage(e)),
      });
  }
  remove(c: Category) {
    if (this.busy || !confirm("Supprimer " + c.name + " ?")) return;
    this.busy = true;
    this.error = "";
    this.cs
      .delete(c.id)
      .pipe(finalize(() => (this.busy = false)))
      .subscribe({
        next: () => {
          this.message = "Catégorie supprimée.";
          this.load();
        },
        error: (e) => (this.error = errorMessage(e)),
      });
  }
}
