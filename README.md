= CineTrack - Application Android de gestion de films
:icons: font
:toc: macro
:toclevels: 2

image:https://img.shields.io/badge/Platform-Android-green.svg[]
image:https://img.shields.io/badge/Language-Java-blue.svg[]
image:https://img.shields.io/badge/API-OMDb-orange.svg[]

toc::[]

== 👥 Équipe

|===
| Nom | Rôle | GitHub  

| Issiakhem Malik | Développeur | https://github.com/tonpseudo[tonpseudo]  
| Kalaoun Luna | Développeur | https://github.com/xxx[xxx]  
| Diarra Hassane | Développeur | https://github.com/xxx[xxx]  
|===

---

== 📱 Présentation du projet

CineTrack est une application Android permettant de gérer une liste de films à voir ou déjà vus.

L’utilisateur peut :
- rechercher des films via l’API OMDb
- ajouter des films à sa liste personnelle
- consulter les détails d’un film (affiche, synopsis, etc.)
- modifier ou supprimer un film
- consulter des recommandations
- utiliser l’application même sans connexion (mode hors ligne)

---

== 🚀 Fonctionnalités principales

=== 🎬 Gestion des films
- Ajout de film via recherche OMDb
- Modification des informations locales (statut, note)
- Suppression avec confirmation
- Liste des films enregistrés

=== 🔍 Recherche API OMDb
- Recherche dynamique par titre
- Sélection d’un film dans les résultats
- Récupération automatique des informations (genre, année…)

=== 📄 Détail d’un film
- Affiche du film (Glide)
- Synopsis (Plot OMDb)
- Informations locales (note, statut)
- Bouton “Modifier ce film”

=== ⭐ Recommandations
- Affichage de plusieurs films recommandés
- Clic sur une recommandation → écran détail
- Possibilité d’ajouter à sa liste

=== 🌙 Mode clair / sombre
- Adaptation automatique selon le système Android
- Implémentation via thème DayNight

=== 📡 Mode hors ligne
- Sauvegarde de la dernière recommandation (SharedPreferences)
- Affichage en mode hors ligne avec badge “Hors-ligne”
- Bouton de rechargement manuel

---

== 🏗️ Architecture du projet

L’application suit une architecture en couches :

- UI (Activities + Adapters)
- Data (Repository + modèles + base locale)
- API (OMDb via Retrofit)

Toutes les données passent par le `MovieRepository`, ce qui respecte les bonnes pratiques (pas d’appel réseau dans les Activities).

---

== 📂 Organisation des classes

=== 🎨 UI Layer (Présentation)

|===
| Classe | Rôle

| MainActivity | Affiche les recommandations + gestion du mode hors ligne
| MovieListActivity | Affiche la liste des films enregistrés
| AddEditMovieActivity | Permet d’ajouter/modifier un film via l’API OMDb
| MovieDetailActivity | Affiche les détails complets d’un film
| ProfileActivity | Écran profil utilisateur
| MovieAdapter | Adapter pour afficher les films en liste
| OmdbSearchAdapter | Adapter pour afficher les résultats de recherche OMDb
|===

---

=== 🧠 Data Layer (Métier / Local)

|===
| Classe | Rôle

| MovieRepository | Classe centrale, gère API + cache + données locales
| Movie | Modèle représentant un film
| DatabaseHelper | Gestion de la base SQLite (films locaux)
| MovieUtils | Fonctions utilitaires liées aux films
| Constants | Contient les constantes (clé API OMDb, etc.)
|===

---

=== 🌐 API Layer (OMDb)

|===
| Classe | Rôle

| RetrofitClient | Configuration de Retrofit
| OmdbApi | Interface des endpoints OMDb
| OmdbResponse | Réponse détaillée d’un film
| OmdbSearchItem | Élément de recherche OMDb
| OmdbSearchResponse | Liste des résultats de recherche
|===

---

== 🔄 Fonctionnement global

1. L’utilisateur interagit avec une Activity (UI)
2. L’Activity appelle le `MovieRepository`
3. Le repository :
   - appelle l’API OMDb via Retrofit
   - ou utilise les données locales (SQLite / SharedPreferences)
4. Les données sont renvoyées à l’UI pour affichage

---

== 💾 Stockage des données

- SQLite → stockage des films ajoutés
- SharedPreferences → cache de la recommandation (mode hors ligne)

---

== 🧩 Layouts principaux

|===
| Layout | Description

| activity_main.xml | Écran d’accueil (recommandations)
| activity_movie_list.xml | Liste des films
| activity_add_edit_movie.xml | Formulaire d’ajout/modification
| activity_movie_detail.xml | Détail d’un film
| activity_profile.xml | Profil utilisateur
| item_film.xml | Carte d’un film
| item_omdb_search_result.xml | Résultat de recherche OMDb
|===

---

== 🛠️ Technologies utilisées

- Java (Android)
- Retrofit (API REST)
- Glide (chargement d’images)
- SQLite (stockage local)
- SharedPreferences (cache offline)
- OMDb API

---

== 📌 Points importants (soutenance)

- Respect du pattern Repository
- Aucun appel API direct dans les Activities
- Gestion du mode hors ligne
- Utilisation d’une API REST
- Interface moderne et responsive

---

== 🔥 Améliorations possibles

- Passage à Room au lieu de SQLite
- Ajout de favoris
- Tri et filtres avancés
- Authentification utilisateur
- Recommandations personnalisées

---

== 👨‍💻 Auteur

Projet réalisé dans le cadre d’un projet Android (ESGI).

---
