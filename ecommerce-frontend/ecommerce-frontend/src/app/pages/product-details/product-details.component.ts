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
  selector: "app-product-details",
  standalone: false,
  templateUrl: "./product-details.component.html",
})
export class ProductDetailsComponent {
  product: Product | null = null;
  quantity = 1;
  loading = true;
  busy = false;
  error = "";
  message = "";
  constructor(
    private service: ProductService,
    private cart: CartService,
    private auth: AuthService,
    private route: ActivatedRoute,
    private router: Router,
  ) {}
  ngOnInit() {
    this.route.paramMap.subscribe((params) => {
      this.loading = true;
      this.error = "";
      this.product = null;
      this.service
        .getById(Number(params.get("id")))
        .pipe(finalize(() => (this.loading = false)))
        .subscribe({
          next: (p) => (this.product = p),
          error: (e) => (this.error = errorMessage(e)),
        });
    });
  }
  add() {
    if (!this.product || this.busy) return;
    if (!this.auth.user) {
      this.router.navigate(["/login"], {
        queryParams: { returnUrl: this.router.url },
      });
      return;
    }
    this.busy = true;
    this.error = "";
    this.cart
      .add(this.product.id, this.quantity)
      .pipe(finalize(() => (this.busy = false)))
      .subscribe({
        next: () => (this.message = "Votre essentiel a été ajouté."),
        error: (e) => (this.error = errorMessage(e)),
      });
  }
  fallback(event: Event) {
    const image = event.target as HTMLImageElement;
    image.onerror = null;
    image.src = "/assets/placeholder.svg";
  }
}
