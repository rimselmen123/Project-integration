# Maison — ecommerce-frontend

Application Angular 20 avec architecture **NgModule non-standalone**, un seul `AppModule` et un seul `AppRoutingModule`. Design français responsive SCSS, sans Angular Material ; illustrations SVG locales incluses. Aucun paiement réel.

## Prérequis et démarrage

- Node.js 22.12+ (vérifié avec 22.23.2) ou une version compatible Angular 20.
- npm 10+.

```sh
npm install
npm start
```

Ouvrir `http://localhost:4200`. Pour compiler : `npm run build`. Le résultat est dans `dist/ecommerce/browser`. Pour les tests du stockage démo : `npm test`. Pour vérifier les dépendances : `npm audit`. Le lockfile est inclus ; `npm ci` reproduit l’installation. Un override `piscina: 5.3.2` corrige la dépendance de compilation tout en gardant Angular 20.

## Démo sans backend

Le mode démo est activé par défaut dans `src/environments/environment.ts` :

```ts
export const environment = {
  production: false,
  apiUrl: "http://localhost:8080/api",
  useMock: true,
};
```

Catalogue, comptes, paniers, commandes et modifications admin sont enregistrés dans le navigateur via localStorage. Comptes : **client@demo.com** et **admin@demo.com**, mot de passe **Demo12345!**. On peut également créer un compte CLIENT. Les paniers et commandes sont séparés par compte.

Le bandeau indique toujours le mode démo. Ce stockage sert uniquement à la démonstration : les comptes et mots de passe sont locaux, sans sécurité de production, et les guards peuvent être contournés. Utilisez seulement des identifiants fictifs. Le backend impose les véritables contrôles de permission.

Pour réinitialiser les données démo, supprimer les clés `maison-demo-v1` et `maison-demo-auth` dans le stockage du navigateur. Les données API et démo sont indépendantes et ne sont pas migrées. Le mode démo doit être choisi avant le lancement : un échec API ne bascule jamais silencieusement en démo.

## Utiliser le backend PostgreSQL

1. Suivre le README du backend et lancer l’API sur le port 8080.
2. Mettre `useMock: false` et vérifier `apiUrl` dans le fichier ci-dessus.
3. Relancer Angular (ou recompiler si vous servez le build).
4. Se connecter avec les comptes seed du backend si `SEED_ENABLED=true`, ou créer un compte CLIENT. Les comptes locaux du mode démo n’existent pas automatiquement dans PostgreSQL.

Le backend accepte `http://localhost:4200` par défaut. Si vous ouvrez Angular via `127.0.0.1`, ajouter cette origine à `CORS_ORIGINS` côté backend. Aucune configuration PostgreSQL n’est placée dans Angular ; la base est accessible seulement par l’API. `AuthInterceptor` ajoute le jeton uniquement aux requêtes de cette API. Un 401 sur une route connectée ferme la session.

## Structure et routes

```text
src/app/
  models/       User, Product, Category, Cart/CartItem, Order/OrderItem
  services/     Auth, Product, Category, Cart, Order, interceptor, stockage démo
  guards/       AuthGuard, AdminGuard
  components/   navbar, footer, hero, product-card, category-card, admin-nav
  pages/        home, products, product-details, cart, login, register, orders, profile
  admin/        dashboard, products, categories, orders
  app.module.ts
  app-routing.module.ts
public/assets/  illustrations locales
tests/          tests de comportement du mode démo
```

Routes centralisées : `/`, `/products`, `/products/:id`, `/login`, `/register`, `/cart`, `/orders`, `/profile`, `/admin`, `/admin/products`, `/admin/categories`, `/admin/orders`. Panier, commandes et profil nécessitent une connexion. Les routes admin nécessitent ADMIN. Profil en lecture seule : aucune route de modification du compte n’est prévue dans ce périmètre.

Lors d’un hébergement du build, configurer le serveur pour renvoyer `index.html` sur les routes Angular (fallback SPA).

## Contrat API REST commun

Base : `http://localhost:8080/api`. Corps et réponses JSON, montants en EUR. Un objet erreur contient `message` et `errors` (dictionnaire des erreurs par champ). Envoyer `Authorization: Bearer <token>` sur les routes connectées. Une liste est un tableau JSON, sans pagination pour garder le projet simple.

| Méthode | Route après /api    | Accès                | Corps / réponse                                                                |
| ------- | ------------------- | -------------------- | ------------------------------------------------------------------------------ |
| POST    | /auth/register      | Public               | `{name,email,password}` → 201 `{token,user}` ; rôle toujours CLIENT            |
| POST    | /auth/login         | Public               | `{email,password}` → `{token,user}`                                            |
| GET     | /auth/me            | Connecté             | User                                                                           |
| GET     | /products           | Public               | Product[] actifs                                                               |
| GET     | /products/admin     | ADMIN                | Product[] incluant les désactivés                                              |
| GET     | /products/{id}      | Public / ADMIN       | Product ; un produit désactivé est visible seulement par ADMIN                 |
| POST    | /products           | ADMIN                | ProductInput → 201 Product                                                     |
| PUT     | /products/{id}      | ADMIN                | ProductInput → Product                                                         |
| DELETE  | /products/{id}      | ADMIN                | Désactive le produit, 204                                                      |
| GET     | /categories         | Public               | Category[]                                                                     |
| POST    | /categories         | ADMIN                | `{name,description}` → 201 Category                                            |
| PUT     | /categories/{id}    | ADMIN                | `{name,description}` → Category                                                |
| DELETE  | /categories/{id}    | ADMIN                | 204 ; 409 si utilisée                                                          |
| GET     | /cart               | Connecté             | Cart du compte connecté                                                        |
| POST    | /cart/items         | Connecté             | `{productId,quantity}` → Cart ; ajoute à la quantité existante                 |
| PUT     | /cart/items/{id}    | Connecté             | `{quantity}` → Cart ; remplace la quantité                                     |
| DELETE  | /cart/items/{id}    | Connecté             | Retire la ligne de son propre panier, 204                                      |
| DELETE  | /cart               | Connecté             | Vide le panier, 204                                                            |
| POST    | /orders             | Connecté             | `{shippingAddress}` → 201 Order ; prix, stock et total calculés sur le serveur |
| GET     | /orders             | Connecté             | Ses propres Order[], les plus récentes d’abord                                 |
| GET     | /orders/all         | ADMIN                | Toutes les Order[]                                                             |
| GET     | /orders/{id}        | Propriétaire / ADMIN | Order                                                                          |
| PATCH   | /orders/{id}/status | ADMIN                | `{status}` → Order                                                             |

`ProductInput` : `{name,description,price,stock,imageUrl,categoryId,active}`. Prix de 0,01 à 99 999 999,99 (2 décimales), stock entier de 0 à 1 000 000. `active` doit être envoyé explicitement à true pour publier un nouveau produit. `imageUrl` est un chemin local (par exemple `/assets/headphones.svg`) ou une URL HTTP(S). Les images du seed sont servies par le frontend.

Quantité : entier de 1 à 1000, limitée au stock disponible. Une commande nécessite une adresse de 10 à 500 caractères et un panier non vide. Le panier ne réserve pas le stock ; une commande peut échouer si un autre client a acheté le dernier article. Le panier est alors conservé. Statuts : `EN_ATTENTE`, `CONFIRMEE`, `EXPEDIEE`, `LIVREE`. L’administrateur choisit librement un de ces quatre statuts (pas de workflow de transition imposé).

Formats de réponse :

```text
User = { id, name, email, role: CLIENT|ADMIN }
Category = { id, name, description }
Product = { id, name, description, price, stock, imageUrl, categoryId, categoryName, active }
Cart = { id, items: CartItem[], total }
CartItem = { id, productId, productName, imageUrl, price, quantity, subtotal }
Order = { id, userId, customerName, createdAt, status, shippingAddress, items: OrderItem[], total }
OrderItem = { id, productId, productName, price, quantity, subtotal }
```

`createdAt` est une date ISO UTC. Le mot de passe n’apparaît jamais dans une réponse. Les lignes de commande conservent le nom, le prix et la quantité du moment de l’achat. Désactiver ou modifier un produit ne modifie pas les commandes existantes.

Codes : 400 données invalides ; 401 connexion nécessaire ou identifiants incorrects ; 403 permission refusée ; 404 ressource introuvable ; 409 email déjà inscrit, stock insuffisant, panier vide ou catégorie utilisée. Une erreur ne produit jamais automatiquement une réponse de démonstration en mode API.
