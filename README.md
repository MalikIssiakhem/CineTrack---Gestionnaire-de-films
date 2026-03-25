# CineTrack - Application Android de gestion de films

![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![Language](https://img.shields.io/badge/Language-Java-blue.svg)
![API](https://img.shields.io/badge/API-OMDb-orange.svg)

## 👥 Équipe

| Nom | Rôle | GitHub |
|---|---|---|
| Issiakhem Malik | Développeur | [MalikIssiakhem](https://github.com/MalikIssiakhem) |
| Kalaoun Luna | Développeur | [GitHub](https://github.com/xxx) |
| Diarra Hassane | Développeur | [GitHub](https://github.com/xxx) |

## 📱 Présentation du projet

CineTrack est une application Android permettant de gérer une liste de films à voir ou déjà vus.

L’utilisateur peut :
- rechercher des films via l’API OMDb
- ajouter des films à sa liste personnelle
- consulter les détails d’un film (affiche, synopsis, etc.)
- modifier ou supprimer un film
- consulter des recommandations
- utiliser l’application même sans connexion (mode hors ligne)

## 🚀 Fonctionnalités principales

### 🎬 Gestion des films
- Ajout de film via recherche OMDb
- Modification des informations locales (statut, note)
- Suppression avec confirmation
- Liste des films enregistrés

### 🔍 Recherche API OMDb
- Recherche dynamique par titre
- Sélection d’un film dans les résultats
- Récupération automatique des informations (genre, année)

### 📄 Détail d’un film
- Affiche du film avec Glide
- Synopsis via OMDb
- Informations locales (note, statut)
- Bouton **Modifier ce film**

### ⭐ Recommandations
- Affichage de plusieurs films recommandés
- Clic sur une recommandation pour ouvrir l’écran détail
- Possibilité d’ajouter une recommandation à sa liste

### 🌙 Mode clair / sombre
- Adaptation automatique selon le thème système Android
- Implémentation via thème DayNight

### 📡 Mode hors ligne
- Sauvegarde de la dernière recommandation dans SharedPreferences
- Affichage en mode hors ligne avec badge **Hors-ligne**
- Bouton de rechargement manuel

## 🏗️ Architecture du projet

L’application suit une architecture simple en couches :

- **UI** : Activities et Adapters
- **Data** : Repository, modèles, base locale
- **API** : OMDb via Retrofit

Toutes les données passent par `MovieRepository`, ce qui permet de respecter une bonne séparation des responsabilités et d’éviter les appels réseau directs dans les Activities.

## 📂 Organisation des classes

<img width="2200" height="1300" alt="CineTrack_clean_pro" src="https://github.com/user-attachments/assets/9f4dcdc1-cb45-4a89-af19-3be74fac6d77" />

### 🎨 UI Layer

| Classe | Rôle |
|---|---|
| `MainActivity` | Affiche les recommandations et gère le mode hors ligne |
| `MovieListActivity` | Affiche la liste des films enregistrés |
| `AddEditMovieActivity` | Permet d’ajouter ou modifier un film via l’API OMDb |
| `MovieDetailActivity` | Affiche les détails complets d’un film |
| `ProfileActivity` | Écran profil utilisateur |
| `MovieAdapter` | Adapter pour afficher les films dans la liste |
| `OmdbSearchAdapter` | Adapter pour afficher les résultats de recherche OMDb |

### 🧠 Data Layer

| Classe | Rôle |
|---|---|
| `MovieRepository` | Classe centrale, gère API, cache et données locales |
| `Movie` | Modèle représentant un film |
| `DatabaseHelper` | Gestion de la base SQLite |
| `MovieUtils` | Fonctions utilitaires liées aux films |
| `Constants` | Contient les constantes du projet (clé API, etc.) |

### 🌐 API Layer

| Classe | Rôle |
|---|---|
| `RetrofitClient` | Configuration de Retrofit |
| `OmdbApi` | Interface des endpoints OMDb |
| `OmdbResponse` | Réponse détaillée d’un film |
| `OmdbSearchItem` | Élément de résultat de recherche OMDb |
| `OmdbSearchResponse` | Liste des résultats de recherche |

## 🔄 Fonctionnement global

1. L’utilisateur interagit avec une Activity
2. L’Activity appelle `MovieRepository`
3. `MovieRepository` :
   - appelle l’API OMDb via Retrofit
   - ou récupère les données locales via SQLite / SharedPreferences
4. Les données sont renvoyées à l’interface pour affichage

## 💾 Stockage des données

- **SQLite** : stockage des films ajoutés par l’utilisateur
- **SharedPreferences** : cache de la recommandation principale pour le mode hors ligne

## 🧩 Layouts principaux

| Layout | Description |
|---|---|
| `activity_main.xml` | Écran d’accueil avec recommandations |
| `activity_movie_list.xml` | Liste des films |
| `activity_add_edit_movie.xml` | Formulaire d’ajout / modification |
| `activity_movie_detail.xml` | Détail d’un film |
| `activity_profile.xml` | Profil utilisateur |
| `item_film.xml` | Carte d’un film dans la liste |
| `item_omdb_search_result.xml` | Élément d’un résultat de recherche OMDb |

## 🛠️ Technologies utilisées

- Java
- Android SDK
- Retrofit
- Glide
- SQLite
- SharedPreferences
- OMDb API

## 📌 Points importants pour la soutenance

- Respect du pattern Repository
- Aucun appel API direct dans les Activities
- Gestion d’une API REST avec Retrofit
- Gestion du mode hors ligne
- Interface moderne et lisible
- Affichage des affiches via Glide

## 🔥 Améliorations possibles

- Passage de SQLite vers Room
- Ajout d’un système de favoris
- Tri et filtres avancés
- Recommandations personnalisées
- Authentification utilisateur

## 👨‍💻 Auteur

Projet réalisé dans le cadre d’un projet Android à l’ESGI.
