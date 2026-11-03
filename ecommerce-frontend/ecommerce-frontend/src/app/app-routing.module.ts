import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { AuthGuard } from "./guards/auth.guard";
import { AdminGuard } from "./guards/admin.guard";
import { HomeComponent } from "./pages/home/home.component";
import { ProductsComponent } from "./pages/products/products.component";
import { ProductDetailsComponent } from "./pages/product-details/product-details.component";
import { CartComponent } from "./pages/cart/cart.component";
import { LoginComponent } from "./pages/login/login.component";
import { RegisterComponent } from "./pages/register/register.component";
import { OrdersComponent } from "./pages/orders/orders.component";
import { ProfileComponent } from "./pages/profile/profile.component";
import { DashboardComponent } from "./admin/dashboard/dashboard.component";
import { AdminProductsComponent } from "./admin/products/admin-products.component";
import { AdminCategoriesComponent } from "./admin/categories/admin-categories.component";
import { AdminOrdersComponent } from "./admin/orders/admin-orders.component";
const routes: Routes = [
  { path: "", component: HomeComponent },
  { path: "products", component: ProductsComponent },
  { path: "products/:id", component: ProductDetailsComponent },
  { path: "cart", component: CartComponent, canActivate: [AuthGuard] },
  { path: "login", component: LoginComponent },
  { path: "register", component: RegisterComponent },
  { path: "orders", component: OrdersComponent, canActivate: [AuthGuard] },
  { path: "profile", component: ProfileComponent, canActivate: [AuthGuard] },
  {
    path: "admin",
    component: DashboardComponent,
    canActivate: [AuthGuard, AdminGuard],
  },
  {
    path: "admin/products",
    component: AdminProductsComponent,
    canActivate: [AuthGuard, AdminGuard],
  },
  {
    path: "admin/categories",
    component: AdminCategoriesComponent,
    canActivate: [AuthGuard, AdminGuard],
  },
  {
    path: "admin/orders",
    component: AdminOrdersComponent,
    canActivate: [AuthGuard, AdminGuard],
  },
  { path: "**", redirectTo: "" },
];
@NgModule({
  imports: [
    RouterModule.forRoot(routes, { scrollPositionRestoration: "enabled" }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
