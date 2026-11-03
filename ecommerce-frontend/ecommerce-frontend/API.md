# API Maison

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
