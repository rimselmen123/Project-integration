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
  selector: "app-home",
  standalone: false,
  templateUrl: "./home.component.html",
})
export class HomeComponent {
  products: Product[] = [];
  categories: Category[] = [];
  loading = true;
  error = "";
  message = "";
  adding = 0;
  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private cart: CartService,
    private auth: AuthService,
    private router: Router,
  ) {}
  ngOnInit() {
    forkJoin({
      products: this.productService.getAll(),
      categories: this.categoryService.getAll(),
    })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (r) => {
          this.products = r.products.slice(0, 4);
          this.categories = r.categories;
        },
        error: (e) => (this.error = errorMessage(e)),
      });
  }
  add(p: Product) {
    if (!this.auth.user) {
      this.router.navigate(["/login"], {
        queryParams: { returnUrl: this.router.url },
      });
      return;
    }
    this.adding = p.id;
    this.error = "";
    this.cart
      .add(p.id)
      .pipe(finalize(() => (this.adding = 0)))
      .subscribe({
        next: () => (this.message = p.name + " ajouté au panier."),
        error: (e) => (this.error = errorMessage(e)),
      });
  }
}
