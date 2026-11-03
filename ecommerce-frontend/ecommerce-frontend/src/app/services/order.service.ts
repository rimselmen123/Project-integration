import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { defer, of, Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { DemoService } from "./demo.service";
import { Order, OrderStatus } from "../models/order";
import { AuthService } from "./auth.service";
@Injectable({ providedIn: "root" })
export class OrderService {
  private url = environment.apiUrl + "/orders";
  constructor(
    private http: HttpClient,
    private demo: DemoService,
    private auth: AuthService,
  ) {}
  getAll(admin = false): Observable<Order[]> {
    return environment.useMock
      ? defer(() => of(this.demo.orders(this.auth.userId, admin)))
      : this.http.get<Order[]>(this.url + (admin ? "/all" : ""));
  }
  getById(id: number): Observable<Order> {
    return environment.useMock
      ? defer(() => of(this.demo.order(this.auth.userId, id)))
      : this.http.get<Order>(this.url + "/" + id);
  }
  create(shippingAddress: string): Observable<Order> {
    return environment.useMock
      ? defer(() => of(this.demo.checkout(this.auth.userId, shippingAddress)))
      : this.http.post<Order>(this.url, { shippingAddress });
  }
  updateStatus(id: number, status: OrderStatus): Observable<Order> {
    return environment.useMock
      ? defer(() => of(this.demo.status(this.auth.userId, id, status)))
      : this.http.patch<Order>(this.url + "/" + id + "/status", { status });
  }
}
