import { User, AuthResponse } from "../models/user";
import { Product } from "../models/product";
import { Category } from "../models/category";
import { Cart } from "../models/cart";
import { Order, OrderStatus } from "../models/order";
interface Account {
  user: User;
  password: string;
}
interface State {
  accounts: Account[];
  products: Product[];
  categories: Category[];
  carts: Record<string, Cart>;
  orders: Order[];
  nextId: number;
}
export interface StorageLike {
  getItem(key: string): string | null;
  setItem(key: string, value: string): void;
}
const clone = <T>(v: T): T => JSON.parse(JSON.stringify(v));
const money = (v: number) => Math.round(v * 100) / 100;
export class DemoStore {
  private state: State;
  constructor(private storage?: StorageLike) {
    const saved = storage?.getItem("maison-demo-v1");
    try {
      this.state = saved ? JSON.parse(saved) : this.seed();
    } catch {
      this.state = this.seed();
    }
  }
  private seed(): State {
    const categories: Category[] = [
      {
        id: 1,
        name: "Tech & accessoires",
        description: "Les essentiels de votre quotidien.",
      },
      { id: 2, name: "Maison", description: "Un espace qui vous ressemble." },
      {
        id: 3,
        name: "Style & lifestyle",
        description: "Simple, utile et intemporel.",
      },
    ];
    const data: [string, string, number, number, string, number][] = [
      [
        "Casque sans fil",
        "Un son clair et une autonomie de 30 heures pour étudier et se détendre.",
        39.9,
        30,
        "headphones",
        1,
      ],
      [
        "Lampe de bureau",
        "Une lumière douce et orientable pour votre espace de travail.",
        24.9,
        25,
        "lamp",
        2,
      ],
      [
        "Sac urbain",
        "Un sac léger avec compartiment ordinateur et poches pratiques.",
        49,
        20,
        "bag",
        3,
      ],
      [
        "Gourde isotherme",
        "Votre boisson reste fraîche ou chaude toute la journée.",
        19.9,
        40,
        "bottle",
        3,
      ],
      [
        "Clavier compact",
        "Un clavier confortable au format compact pour votre bureau.",
        59,
        15,
        "keyboard",
        1,
      ],
      [
        "Carnet premium",
        "Des pages crème et une couverture solide pour vos idées.",
        12.5,
        50,
        "notebook",
        2,
      ],
    ];
    return {
      accounts: [
        {
          user: {
            id: 1,
            name: "Administrateur",
            email: "admin@demo.com",
            role: "ADMIN",
          },
          password: "Demo12345!",
        },
        {
          user: {
            id: 2,
            name: "Alex Martin",
            email: "client@demo.com",
            role: "CLIENT",
          },
          password: "Demo12345!",
        },
      ],
      categories,
      products: data.map((p, i) => ({
        id: i + 1,
        name: p[0],
        description: p[1],
        price: p[2],
        stock: p[3],
        imageUrl: "/assets/" + p[4] + ".svg",
        categoryId: p[5],
        categoryName: categories.find((c) => c.id === p[5])!.name,
        active: true,
      })),
      carts: {},
      orders: [],
      nextId: 100,
    };
  }
  private persist() {
    this.storage?.setItem("maison-demo-v1", JSON.stringify(this.state));
  }
  private fail(message: string): never {
    throw new Error(message);
  }
  private account(id: number) {
    return (
      this.state.accounts.find((a) => a.user.id === id) ??
      this.fail("Connectez-vous pour continuer.")
    );
  }
  login(email: string, password: string): AuthResponse {
    const a = this.state.accounts.find(
      (a) =>
        a.user.email === email.trim().toLowerCase() && a.password === password,
    );
    if (!a) this.fail("Identifiants incorrects.");
    return { token: "demo-" + a.user.id, user: clone(a.user) };
  }
  register(name: string, email: string, password: string): AuthResponse {
    email = email.trim().toLowerCase();
    if (
      !name.trim() ||
      !/^\S+@\S+\.\S+$/.test(email) ||
      password.length < 8 ||
      password.length > 72
    )
      this.fail("Nom, email et mot de passe de 8 à 72 caractères requis.");
    if (this.state.accounts.some((a) => a.user.email === email))
      this.fail("Email déjà inscrit.");
    const user: User = {
      id: this.state.nextId++,
      name: name.trim(),
      email,
      role: "CLIENT",
    };
    this.state.accounts.push({ user, password });
    this.persist();
    return { token: "demo-" + user.id, user: clone(user) };
  }
  me(id: number) {
    return clone(this.account(id).user);
  }
  categories() {
    return clone(this.state.categories);
  }
  products(admin = false) {
    return clone(this.state.products.filter((p) => admin || p.active));
  }
  product(id: number) {
    return clone(
      this.state.products.find((p) => p.id === id) ??
        this.fail("Produit introuvable."),
    );
  }
  saveProduct(input: Partial<Product>): Product {
    const category = this.state.categories.find(
      (c) => c.id === Number(input.categoryId),
    );
    if (
      !category ||
      !input.name?.trim() ||
      !Number.isFinite(input.price) ||
      input.price! <= 0 ||
      !Number.isInteger(input.stock) ||
      input.stock! < 0
    )
      this.fail("Produit invalide : vérifiez les champs.");
    const product: Product = {
      id: input.id ?? this.state.nextId++,
      name: input.name!.trim(),
      description: input.description ?? "",
      price: money(input.price!),
      stock: input.stock!,
      imageUrl: input.imageUrl ?? "",
      categoryId: category.id,
      categoryName: category.name,
      active: input.active ?? true,
    };
    const index = this.state.products.findIndex((p) => p.id === product.id);
    if (index < 0) this.state.products.push(product);
    else this.state.products[index] = product;
    this.persist();
    return clone(product);
  }
  deleteProduct(id: number) {
    const p =
      this.state.products.find((p) => p.id === id) ??
      this.fail("Produit introuvable.");
    p.active = false;
    this.persist();
  }
  saveCategory(input: Partial<Category>): Category {
    if (!input.name?.trim()) this.fail("Le nom est requis.");
    if (
      this.state.categories.some(
        (c) =>
          c.id !== input.id &&
          c.name.toLowerCase() === input.name!.trim().toLowerCase(),
      )
    )
      this.fail("Cette catégorie existe déjà.");
    const c: Category = {
      id: input.id ?? this.state.nextId++,
      name: input.name!.trim(),
      description: input.description ?? "",
    };
    const index = this.state.categories.findIndex((x) => x.id === c.id);
    if (index < 0) this.state.categories.push(c);
    else this.state.categories[index] = c;
    this.state.products
      .filter((p) => p.categoryId === c.id)
      .forEach((p) => (p.categoryName = c.name));
    this.persist();
    return clone(c);
  }
  deleteCategory(id: number) {
    if (this.state.products.some((p) => p.categoryId === id))
      this.fail("Cette catégorie contient des produits.");
    this.state.categories = this.state.categories.filter((c) => c.id !== id);
    this.persist();
  }
  private mutableCart(userId: number): Cart {
    this.account(userId);
    return (
      this.state.carts[userId] ??
      (this.state.carts[userId] = { id: userId, items: [], total: 0 })
    );
  }
  cart(userId: number): Cart {
    const c = this.mutableCart(userId);
    c.items.forEach((i) => {
      const p = this.product(i.productId);
      i.price = p.price;
      i.productName = p.name;
      i.imageUrl = p.imageUrl;
      i.subtotal = money(p.price * i.quantity);
    });
    c.total = money(c.items.reduce((v, i) => v + i.subtotal, 0));
    return clone(c);
  }
  add(userId: number, productId: number, quantity: number): Cart {
    const c = this.mutableCart(userId);
    const p = this.product(productId);
    const i = c.items.find((i) => i.productId === productId);
    const next = quantity + (i?.quantity ?? 0);
    if (
      !Number.isInteger(quantity) ||
      quantity < 1 ||
      next > 1000 ||
      !p.active ||
      next > p.stock
    )
      this.fail("Quantité invalide ou stock insuffisant.");
    if (i) i.quantity = next;
    else
      c.items.push({
        id: this.state.nextId++,
        productId,
        productName: p.name,
        imageUrl: p.imageUrl,
        price: p.price,
        quantity,
        subtotal: 0,
      });
    const result = this.cart(userId);
    this.persist();
    return result;
  }
  update(userId: number, id: number, quantity: number): Cart {
    const i =
      this.mutableCart(userId).items.find((i) => i.id === id) ??
      this.fail("Article introuvable.");
    const p = this.product(i.productId);
    if (
      !Number.isInteger(quantity) ||
      quantity < 1 ||
      quantity > 1000 ||
      quantity > p.stock ||
      !p.active
    )
      this.fail("Quantité invalide ou stock insuffisant.");
    i.quantity = quantity;
    const result = this.cart(userId);
    this.persist();
    return result;
  }
  remove(userId: number, id: number) {
    const c = this.mutableCart(userId);
    c.items = c.items.filter((i) => i.id !== id);
    this.cart(userId);
    this.persist();
  }
  clear(userId: number) {
    const c = this.mutableCart(userId);
    c.items = [];
    c.total = 0;
    this.persist();
  }
  checkout(userId: number, address: string): Order {
    if (address.trim().length < 10 || address.length > 500)
      this.fail("Adresse complète requise (10 à 500 caractères).");
    const c = this.cart(userId);
    if (!c.items.length) this.fail("Le panier est vide.");
    c.items.forEach((i) => {
      const p = this.product(i.productId);
      if (!p.active || i.quantity > p.stock)
        this.fail("Stock insuffisant pour " + p.name);
    });
    const order: Order = {
      id: this.state.nextId++,
      userId,
      customerName: this.account(userId).user.name,
      createdAt: new Date().toISOString(),
      status: "EN_ATTENTE",
      shippingAddress: address.trim(),
      items: c.items.map((i) => ({
        id: this.state.nextId++,
        productId: i.productId,
        productName: i.productName,
        price: i.price,
        quantity: i.quantity,
        subtotal: i.subtotal,
      })),
      total: c.total,
    };
    c.items.forEach(
      (i) =>
        (this.state.products.find((p) => p.id === i.productId)!.stock -=
          i.quantity),
    );
    this.state.orders.unshift(order);
    this.clear(userId);
    return clone(order);
  }
  orders(userId: number, admin = false) {
    const u = this.account(userId).user;
    if (admin && u.role !== "ADMIN") this.fail("Accès refusé.");
    return clone(this.state.orders.filter((o) => admin || o.userId === userId));
  }
  order(userId: number, id: number) {
    const u = this.account(userId).user;
    return clone(
      this.state.orders.find(
        (o) => o.id === id && (o.userId === userId || u.role === "ADMIN"),
      ) ?? this.fail("Commande introuvable."),
    );
  }
  status(userId: number, id: number, status: OrderStatus) {
    if (this.account(userId).user.role !== "ADMIN") this.fail("Accès refusé.");
    const o =
      this.state.orders.find((o) => o.id === id) ??
      this.fail("Commande introuvable.");
    o.status = status;
    this.persist();
    return clone(o);
  }
}
