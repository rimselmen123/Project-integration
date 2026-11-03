# Vérifications effectuées — 1 octobre 2026

Les vérifications ont été réellement exécutées sur les sources livrées, sous Windows avec Node.js 22.23.2 et JDK 17.0.12.

- Frontend : npm install, npm test (4 tests réussis), npm run build (réussi), audit npm sans vulnérabilité signalée au moment de la livraison.
- Backend : wrapper Maven 3.9.11 inclus, mvnw.cmd clean package (BUILD SUCCESS), 9 tests réussis, aucune erreur ni test ignoré.
- Régression du montant élevé : test échouant avant correction, puis réussi après augmentation de la précision du total.
- Régression CORS : test préflight échouant avant correction, puis réussi avec l’origine Angular autorisée et les autres origines refusées.
- Tests serveur : authentification, rôle CLIENT imposé, permissions admin, isolation des commandes, quantité et stock, panier, CRUD catégories, snapshots des commandes et deux commandes concurrentes pour un seul article en stock.
- Navigateur : parcours démo complet (connexion, panier, quantité, commande, détail et statut admin), affichage mobile 390 px ; en mode API, catalogue, connexion JWT et panier vérifiés avec l’API lancée sur une base H2 de test.
- Revue indépendante des sources et de la documentation ; les écarts relevés ont été corrigés.

La configuration normale utilise PostgreSQL. Les tests et le lancement de vérification API utilisent H2 : Docker n’étant pas démarré dans cet environnement, le lancement sur un serveur PostgreSQL réel n’a pas été exécuté. Suivre le README pour la base PostgreSQL locale. Les réglages temporaires de vérification (port API 8085, mode API) ont été retirés du frontend : les sources livrées activent le mode démo et ciblent l’API localhost:8080.

Les archives contiennent les sources et configurations, sans node_modules, target, caches ou secrets de production. Recompiler avec les commandes du README pour produire le build frontend et le JAR backend.
