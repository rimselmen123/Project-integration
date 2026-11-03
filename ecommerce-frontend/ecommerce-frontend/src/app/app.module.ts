import { NgModule, LOCALE_ID } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { FormsModule } from "@angular/forms";
import { HttpClientModule, HTTP_INTERCEPTORS } from "@angular/common/http";
import { registerLocaleData } from "@angular/common";
import localeFr from "@angular/common/locales/fr";
import { AppComponent } from "./app.component";
import { AppRoutingModule } from "./app-routing.module";
import { AuthInterceptor } from "./services/auth.interceptor";
import { NavbarComponent } from "./components/navbar/navbar.component";
import { FooterComponent } from "./components/footer/footer.component";
import { HeroComponent } from "./components/hero/hero.component";
import { ProductCardComponent } from "./components/product-card/product-card.component";
import { CategoryCardComponent } from "./components/category-card/category-card.component";
import { HomeComponent } from "./pages/home/home.component";
import { ProductsComponent } from "./pages/products/products.component";
import { ProductDetailsComponent } from "./pages/product-details/product-details.component";
import { CartComponent } from "./pages/cart/cart.component";
import { LoginComponent } from "./pages/login/login.component";
import { RegisterComponent } from "./pages/register/register.component";
import { OrdersComponent } from "./pages/orders/orders.component";
import { ProfileComponent } from "./pages/profile/profile.component";
import { AdminNavComponent } from "./components/admin-nav/admin-nav.component";
import { DashboardComponent } from "./admin/dashboard/dashboard.component";
import { AdminProductsComponent } from "./admin/products/admin-products.component";
import { AdminCategoriesComponent } from "./admin/categories/admin-categories.component";
import { AdminOrdersComponent } from "./admin/orders/admin-orders.component";
registerLocaleData(localeFr);
@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    FooterComponent,
    HeroComponent,
    ProductCardComponent,
    CategoryCardComponent,
    HomeComponent,
    ProductsComponent,
    ProductDetailsComponent,
    CartComponent,
    LoginComponent,
    RegisterComponent,
    OrdersComponent,
    ProfileComponent,
    AdminNavComponent,
    DashboardComponent,
    AdminProductsComponent,
    AdminCategoriesComponent,
    AdminOrdersComponent,
  ],
  imports: [BrowserModule, FormsModule, HttpClientModule, AppRoutingModule],
  providers: [
    { provide: LOCALE_ID, useValue: "fr" },
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
