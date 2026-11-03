import { Component, Input, Output, EventEmitter, OnInit } from "@angular/core";
import { Order, OrderStatus } from "../../models/order";
import { OrderService } from "../../services/order.service";
import { statuses, statusLabel } from "../../services/status-label";
import { finalize } from "rxjs";
import { errorMessage } from "../../services/error-message";
@Component({
  selector: "app-admin-orders",
  standalone: false,
  templateUrl: "./admin-orders.component.html",
})
export class AdminOrdersComponent {
  orders: Order[] = [];
  loading = true;
  busy = false;
  error = "";
  message = "";
  filter = "";
  statuses = statuses;
  constructor(private service: OrderService) {}
  ngOnInit() {
    this.load();
  }
  load() {
    this.service
      .getAll(true)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (o) => (this.orders = o),
        error: (e) => (this.error = errorMessage(e)),
      });
  }
  get filtered() {
    return this.orders.filter((o) => !this.filter || o.status === this.filter);
  }
  label(s: OrderStatus) {
    return statusLabel(s);
  }
  update(o: Order, status: OrderStatus) {
    if (this.busy) return;
    this.busy = true;
    this.error = "";
    this.service
      .updateStatus(o.id, status)
      .pipe(finalize(() => (this.busy = false)))
      .subscribe({
        next: (r) => {
          this.orders = this.orders.map((x) => (x.id === r.id ? r : x));
          this.message = "Statut de la commande #" + r.id + " mis à jour.";
        },
        error: (e) => {
          this.error = errorMessage(e);
          this.load();
        },
      });
  }
}
