# ecommerce-backend

API Spring Boot 3.5.6, Java 17, Maven, PostgreSQL, JPA, Validation et Spring Security/JWT. Projet étudiant simple : produits, catégories, panier, commandes et rôles CLIENT/ADMIN. Aucun paiement réel.

## Prérequis

- JDK 17 ou supérieur ; `JAVA_HOME` doit pointer vers ce JDK, pas Java 8.
- PostgreSQL 16 conseillé, installé localement et accessible sur le port 5432.
- Maven 3.9+ **ou** le wrapper inclus (`mvnw` / `mvnw.cmd`). Le premier lancement du wrapper télécharge Maven et les dépendances.

## PostgreSQL

Avec PostgreSQL déjà installé, créer la base (par exemple dans psql ou pgAdmin) :

```sql
CREATE DATABASE ecommerce;
```

L’utilisateur local est `postgres` par défaut. Adapter son mot de passe via `DB_PASSWORD`. La base `ecommerce` peut être créée depuis pgAdmin 4 ou psql.

## Lancement local — PowerShell

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17' # adapter au JDK installé
$env:DB_PASSWORD = 'postgres'
$env:JWT_SECRET = 'local-demo-secret-change-me-32-bytes-minimum-12345'
$env:SEED_ENABLED = 'true'
$env:DEMO_PASSWORD = 'Demo12345!'
.\mvnw.cmd spring-boot:run
```

Linux/macOS :

```sh
chmod +x mvnw
export DB_PASSWORD=postgres
export JWT_SECRET='local-demo-secret-change-me-32-bytes-minimum-12345'
export SEED_ENABLED=true
export DEMO_PASSWORD='Demo12345!'
./mvnw spring-boot:run
```

La valeur de secret ci-dessus sert seulement au lancement local. Fournir une valeur aléatoire d’au moins 32 octets pour une installation réelle. L’API écoute `http://localhost:8083/api`. Le catalogue public permet un premier contrôle : `GET /api/products`.

## Configuration

| Variable | Défaut | Utilisation |
|---|---|---|
| DB_URL | jdbc:postgresql://localhost:5432/ecommerce | Connexion JDBC |
| DB_USERNAME | postgres | Utilisateur PostgreSQL |
| DB_PASSWORD | 58244524 | Mot de passe PostgreSQL local |
| SERVER_PORT | 8083 | Port API |
| JWT_SECRET | local-demo-secret-change-me-32-bytes-minimum-12345 | Clé de signature, au moins 32 octets ; à remplacer en production |
| JWT_EXPIRATION_MS | 86400000 | Durée du jeton : 24 heures |
| CORS_ORIGINS | http://localhost:4200 | Origines autorisées, séparées par une virgule |
| SEED_ENABLED | false | Active les comptes et le catalogue démo |
| DEMO_PASSWORD | Demo12345! | Mot de passe à la création des comptes démo |
| DDL_AUTO | update | Création/évolution simple des tables par Hibernate |

Le seed crée **admin@demo.com / ADMIN** et **client@demo.com / CLIENT** seulement s’ils n’existent pas. Le mot de passe est `DEMO_PASSWORD` au moment de leur création ; relancer le seed ne le remplace pas. Il ajoute six produits si les tables catalogue sont vides. L’inscription publique crée toujours CLIENT, même si un champ role est envoyé. En dehors d’une démo, désactiver le seed et utiliser des identifiants propres.

## Architecture

```text
src/main/java/com/example/ecommerce/
  entity/       User, Product, Category, Cart, CartItem, Order, OrderItem, enums
  repository/   accès JPA et requêtes avec verrouillage
  service/      authentification, catalogue, panier, commandes, mapping DTO
  controller/   routes REST
  dto/          requêtes validées et réponses sans données sensibles
  security/     signature/vérification JWT et filtre
  config/       CORS, permissions, seed
  exception/    erreurs métier et gestion globale
src/test/       tests HTTP et base H2 réservée aux tests
```

Pour rester lisible, les entités utilisent des champs publics JPA et les relations vers une seule entité sont chargées immédiatement ; aucune entité n’est exposée directement dans l’API. Les commandes sont transactionnelles : verrou du client pour son panier, verrous produit dans l’ordre des identifiants, relecture du stock sous verrou, copie des prix, décrémentation du stock, puis vidage du panier. Les autres clients ne peuvent lire ou modifier votre panier ni vos commandes.

Supprimer un produit signifie le **désactiver** pour conserver les références de panier et l’historique. Une catégorie utilisée ne peut être supprimée. `ddl-auto=update` est pratique pour un projet étudiant ; un déploiement réel nécessiterait migrations, HTTPS, limitations de tentatives de connexion, gestion des secrets et exploitation adaptée. Ce projet n’inclut pas de paiement, d’envoi email, de récupération de mot de passe ni de modification du profil.

## Tests et compilation

```powershell
.\mvnw.cmd test package
java -jar target/ecommerce-1.0.0.jar
```

Linux/macOS : `./mvnw test package`. Les tests utilisent H2 en mode PostgreSQL et leur propre secret et seed ; ils ne nécessitent pas votre base PostgreSQL. Le lancement du JAR nécessite les variables normales et PostgreSQL. Les tests vérifient authentification, rôle CLIENT imposé, permissions, isolation, quantité/stock, CRUD catégories, panier, historique et achats concurrents du dernier stock. H2 ne remplace pas une validation du comportement propre au serveur PostgreSQL.


## Contrat API REST commun

Base : `http://localhost:8080/api`. Corps et réponses JSON, montants en EUR. Un objet erreur contient `message` et `errors` (dictionnaire des erreurs par champ). Envoyer `Authorization: Bearer <token>` sur les routes connectées. Une liste est un tableau JSON, sans pagination pour garder le projet simple.

| Méthode | Route après /api | Accès | Corps / réponse |
|---|---|---|---|
| POST | /auth/register | Public | `{name,email,password}` → 201 `{token,user}` ; rôle toujours CLIENT |
| POST | /auth/login | Public | `{email,password}` → `{token,user}` |
| GET | /auth/me | Connecté | User |
| GET | /products | Public | Product[] actifs |
| GET | /products/admin | ADMIN | Product[] incluant les désactivés |
| GET | /products/{id} | Public / ADMIN | Product ; un produit désactivé est visible seulement par ADMIN |
| POST | /products | ADMIN | ProductInput → 201 Product |
| PUT | /products/{id} | ADMIN | ProductInput → Product |
| DELETE | /products/{id} | ADMIN | Désactive le produit, 204 |
| GET | /categories | Public | Category[] |
| POST | /categories | ADMIN | `{name,description}` → 201 Category |
| PUT | /categories/{id} | ADMIN | `{name,description}` → Category |
| DELETE | /categories/{id} | ADMIN | 204 ; 409 si utilisée |
| GET | /cart | Connecté | Cart du compte connecté |
| POST | /cart/items | Connecté | `{productId,quantity}` → Cart ; ajoute à la quantité existante |
| PUT | /cart/items/{id} | Connecté | `{quantity}` → Cart ; remplace la quantité |
| DELETE | /cart/items/{id} | Connecté | Retire la ligne de son propre panier, 204 |
| DELETE | /cart | Connecté | Vide le panier, 204 |
| POST | /orders | Connecté | `{shippingAddress}` → 201 Order ; prix, stock et total calculés sur le serveur |
| GET | /orders | Connecté | Ses propres Order[], les plus récentes d’abord |
| GET | /orders/all | ADMIN | Toutes les Order[] |
| GET | /orders/{id} | Propriétaire / ADMIN | Order |
| PATCH | /orders/{id}/status | ADMIN | `{status}` → Order |

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
