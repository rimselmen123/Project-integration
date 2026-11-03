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
import { Order, OrderStatus } from "../../models/order";
import { OrderService } from "../../services/order.service";
import { statusLabel } from "../../services/status-label";
@Component({
  selector: "app-dashboard",
  standalone: false,
  templateUrl: "./dashboard.component.html",
})
export class DashboardComponent {
  products: Product[] = [];
  categories: Category[] = [];
  orders: Order[] = [];
  loading = true;
  error = "";
  constructor(
    private ps: ProductService,
    private cs: CategoryService,
    private os: OrderService,
  ) {}
  ngOnInit() {
    forkJoin({
      products: this.ps.getAll(true),
      categories: this.cs.getAll(),
      orders: this.os.getAll(true),
    })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (r) => {
          this.products = r.products;
          this.categories = r.categories;
          this.orders = r.orders;
        },
        error: (e) => (this.error = errorMessage(e)),
      });
  }
  get total() {
    return this.orders.reduce((sum, o) => sum + o.total, 0);
  }
  label(s: OrderStatus) {
    return statusLabel(s);
  }
}
