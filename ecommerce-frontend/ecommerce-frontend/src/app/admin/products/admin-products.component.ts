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
  selector: "app-admin-products",
  standalone: false,
  templateUrl: "./admin-products.component.html",
})
export class AdminProductsComponent {
  products: Product[] = [];
  categories: Category[] = [];
  form: Partial<Product> = {};
  loading = true;
  busy = false;
  error = "";
  message = "";
  constructor(
    private ps: ProductService,
    private cs: CategoryService,
  ) {}
  ngOnInit() {
    this.reset();
    this.load();
  }
  load() {
    forkJoin({ products: this.ps.getAll(true), categories: this.cs.getAll() })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (r) => {
          this.products = r.products;
          this.categories = r.categories;
        },
        error: (e) => (this.error = errorMessage(e)),
      });
  }
  reset() {
    this.form = {
      name: "",
      description: "",
      price: 1,
      stock: 0,
      imageUrl: "/assets/placeholder.svg",
      active: true,
    };
  }
  edit(p: Product) {
    this.form = { ...p };
    this.message = "";
  }
  save() {
    if (this.busy) return;
    this.busy = true;
    this.error = "";
    this.ps
      .save(this.form)
      .pipe(finalize(() => (this.busy = false)))
      .subscribe({
        next: () => {
          this.message = "Produit enregistré.";
          this.reset();
          this.load();
        },
        error: (e) => (this.error = errorMessage(e)),
      });
  }
  remove(p: Product) {
    if (this.busy || !confirm("Désactiver " + p.name + " ?")) return;
    this.busy = true;
    this.error = "";
    this.ps
      .delete(p.id)
      .pipe(finalize(() => (this.busy = false)))
      .subscribe({
        next: () => {
          this.message =
            "Produit désactivé. Les commandes existantes sont conservées.";
          this.load();
        },
        error: (e) => (this.error = errorMessage(e)),
      });
  }
}
