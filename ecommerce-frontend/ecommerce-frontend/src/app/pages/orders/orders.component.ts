import { Component, Input, Output, EventEmitter, OnInit } from "@angular/core";
import { Order, OrderStatus } from "../../models/order";
import { OrderService } from "../../services/order.service";
import { ActivatedRoute } from "@angular/router";
import { finalize } from "rxjs";
import { errorMessage } from "../../services/error-message";
import { statusLabel } from "../../services/status-label";
@Component({
  selector: "app-orders",
  standalone: false,
  templateUrl: "./orders.component.html",
})
export class OrdersComponent {
  orders: Order[] = [];
  loading = true;
  error = "";
  expanded = 0;
  created = "";
  constructor(
    private service: OrderService,
    private route: ActivatedRoute,
  ) {}
  ngOnInit() {
    this.created = this.route.snapshot.queryParamMap.get("created") || "";
    this.service
      .getAll()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (o) => (this.orders = o),
        error: (e) => (this.error = errorMessage(e)),
      });
  }
  label(status: OrderStatus) {
    return statusLabel(status);
  }
}
