import { Component, Input, Output, EventEmitter, OnInit } from "@angular/core";
import { Cart } from "../../models/cart";
import { CartService } from "../../services/cart.service";
import { OrderService } from "../../services/order.service";
import { Router } from "@angular/router";
import { finalize } from "rxjs";
import { errorMessage } from "../../services/error-message";
@Component({
  selector: "app-cart",
  standalone: false,
  templateUrl: "./cart.component.html",
})
export class CartComponent {
  cart: Cart | null = null;
  loading = true;
  busy = false;
  error = "";
  address = "";
  constructor(
    private service: CartService,
    private orders: OrderService,
    private router: Router,
  ) {}
  ngOnInit() {
    this.load();
  }
  load() {
    this.service
      .get()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (c) => (this.cart = c),
        error: (e) => (this.error = errorMessage(e)),
      });
  }
  update(id: number, quantity: number) {
    if (this.busy) return;
    this.busy = true;
    this.error = "";
    this.service
      .update(id, quantity)
      .pipe(finalize(() => (this.busy = false)))
      .subscribe({
        next: (c) => (this.cart = c),
        error: (e) => {
          this.error = errorMessage(e);
          this.load();
        },
      });
  }
  remove(id: number) {
    if (this.busy) return;
    this.busy = true;
    this.service
      .remove(id)
      .pipe(finalize(() => (this.busy = false)))
      .subscribe({
        next: () => this.load(),
        error: (e) => (this.error = errorMessage(e)),
      });
  }
  clear() {
    if (this.busy) return;
    this.busy = true;
    this.service
      .clear()
      .pipe(finalize(() => (this.busy = false)))
      .subscribe({
        next: () => this.load(),
        error: (e) => (this.error = errorMessage(e)),
      });
  }
  checkout() {
    if (this.busy) return;
    this.busy = true;
    this.error = "";
    this.orders
      .create(this.address)
      .pipe(finalize(() => (this.busy = false)))
      .subscribe({
        next: (o) =>
          this.router.navigate(["/orders"], { queryParams: { created: o.id } }),
        error: (e) => (this.error = errorMessage(e)),
      });
  }
  fallback(event: Event) {
    const image = event.target as HTMLImageElement;
    image.onerror = null;
    image.src = "/assets/placeholder.svg";
  }
}
