# Parking — API REST de parkings à proximité

API REST qui renvoie, pour une position donnée, les parkings les plus proches avec leur capacité et leurs places disponibles en temps réel.

## Contexte

L’objectif est de développer une application serveur exposant une API REST permettant à une application mobile ou à un site web d’afficher la liste des parkings à proximité de l’utilisateur, avec le nombre de places disponibles en temps réel.

La première source de données est l’open data de Grand Poitiers, qui fournit la liste des parkings et leurs disponibilités.

- **Multi-villes** : l’application doit pouvoir fonctionner dans d’autres villes. L’URL et le format des données peuvent différer d’une ville à l’autre, tandis que l’API REST exposée au client reste inchangée.
- **Périmètre** : seule la partie serveur est développée ; l’application mobile ou web cliente n’en fait pas partie.

Chaque ville est traitée comme un *tenant* déclaré en configuration, avec sa propre source de données, derrière un contrat REST unique.

## Technologies

- Java 21
- Spring Boot 4.1
- Maven
- Caffeine
- OpenAPI / Swagger
- JUnit 5

## Lancer le projet

### Prérequis

- Java 21
- Le wrapper Maven est fourni

### Commandes

```bash
./mvnw verify
./mvnw spring-boot:run
curl "http://localhost:8080/api/v1/parkings?latitude=46.5802&longitude=0.3404&radius=1000"
```

L’application est disponible sur `http://localhost:8080`.

La documentation OpenAPI est disponible sur `http://localhost:8080/swagger-ui.html`.

## Contrat REST

Le contrat est piloté par la position de l’utilisateur : le client n’a pas besoin de connaître la ville concernée.

```text
GET /api/v1/parkings?latitude=<double>&longitude=<double>&radius=<mètres, défaut 1000>&limit=<n, défaut 20>
```


| Paramètre  | Obligatoire | Contraintes                    |
| ----------- | ----------- | ------------------------------ |
| `latitude`  | Oui         | −90 à 90                     |
| `longitude` | Oui         | −180 à 180                   |
| `radius`    | Non         | 1 à 50 000 m, défaut : 1 000 |
| `limit`     | Non         | 1 à 100, défaut : 20         |

### Réponse 200

La réponse contient la ville identifiée et les parkings triés du plus proche au plus éloigné.

```json
{
  "city": { "id": "poitiers", "name": "Grand Poitiers" },
  "parkings": [
    {
      "id": "3",
      "name": "THEATRE",
      "capacity": 320,
      "availablePlaces": 37,
      "distance": 146,
      "latitude": 46.58383455409422,
      "longitude": 0.33779491061805567,
      "updatedAt": "2026-09-25T12:26:48Z"
    }
  ]
}
```

`distance` est exprimée en mètres, à vol d’oiseau. `updatedAt` correspond à l’horodatage fourni par la source.

### Réponses d’erreur


| Code  | Cas                                                                 |
| ----- | ------------------------------------------------------------------- |
| `400` | Paramètre manquant, invalide, hors limites ou mal typé            |
| `404` | Aucune ville configurée ne couvre la position                      |
| `503` | La source de données de la ville est indisponible ou inexploitable |

## Analyse et choix techniques

### Besoin métier

Pour la position de l’utilisateur, l’API doit retourner les parkings proches, triés par distance, avec leur nom, leur capacité, leur nombre de places disponibles, leurs coordonnées et leur date de mise à jour.

La contrainte principale est de séparer le contrat REST stable des sources de données propres à chaque ville. Les particularités d’une ville — URL, structure JSON ou noms de champs — ne doivent pas atteindre le cœur métier ni la réponse renvoyée au client.

### Source Grand Poitiers

La source Open Data de Grand Poitiers fournit, dans une même réponse, les informations de catalogue et les disponibilités en temps réel.

Constats sur une réponse réelle :

- Neuf parkings sont fournis.
- La position est une chaîne au format `"latitude, longitude"` qui doit être analysée.
- Deux parkings, GARE EFFIA et CORDELIERS, ne disposent pas de coordonnées.

### Architecture

Le projet utilise une architecture hexagonale (*ports & adapters*). Le cœur métier définit ses besoins à travers des interfaces, tandis que l’infrastructure les implémente. Les dépendances pointent vers le domaine, jamais vers l’extérieur.

Le domaine ne pose que deux questions à l’infrastructure : quelle ville couvre une position donnée, puis quels parkings sont disponibles dans cette ville. Il ne connaît ni les URL, ni les formats de données externes.

```text
com.instantsystem.parking
├── domain/                       Java pur, sans dépendance Spring
│   ├── models/                   Parking, Geopoint, City, Zone, NearbyParking
│   ├── port/in/                  Cas d’utilisation et modèles d’entrée/sortie
│   ├── port/out/                 CityCatalog, ParkingProvider
│   ├── services/                 Recherche, filtrage, tri et limitation
│   └── exception/                Exceptions métier
├── adapters/
│   ├── in/rest/                  Contrôleur REST et gestionnaire d’erreurs
│   └── out/                      Sources externes, cache et routage
└── config/                       Configuration Spring et propriétés validées
```

### Recherche des parkings proches

L’algorithme est indépendant de toute ville :

1. Identifier la première emprise géographique configurée qui contient la position demandée. En l’absence de ville correspondante, l’API retourne `404`.
2. Lire les parkings de cette ville au travers d’un cache court.
3. Calculer la distance avec la formule de Haversine.
4. Filtrer les résultats selon le rayon demandé.
5. Trier par distance croissante, puis appliquer la limite demandée.

Le filtrage est réalisé en mémoire après lecture des parkings de la ville. La source de Poitiers ne permet pas de filtrer par position ; cette approche évite de dupliquer la logique métier dans les adaptateurs et reste adaptée à un faible volume de données.

### Décisions principales

- Le contrat REST expose uniquement les données nécessaires à l’écran : identifiant, nom, capacité, places disponibles, distance, coordonnées, horodatage et ville.
- Les parkings sans coordonnées sont ignorés et journalisés, sans faire échouer la réponse entière.
- Les places disponibles sont plafonnées à la capacité lorsqu’une donnée source est incohérente.
- La fraîcheur des données est laissée à l’appréciation du client grâce à l’exposition de `updatedAt`.
- Un cache Caffeine de 30 secondes est appliqué par ville afin de protéger l’open data d’un trafic excessif.
- Les paramètres d’entrée sont validés : coordonnées, rayon et limite.

## Multi-villes

Chaque ville est déclarée dans `application.yaml` avec son identifiant, son format de source, son URL et son emprise géographique.

```yaml
parking:
  tenants:
    - id: poitiers
      name: Grand Poitiers
      format: ods-parking
      url: https://data.grandpoitiers.fr/data-fair/api/v1/datasets/mobilites-stationnement-des-parkings-en-temps-reel/lines
      zone: { min-latitude: 46.3051556, max-latitude: 46.7852034, min-longitude: -0.0471733, max-longitude: 0.7745855 }
```

Deux cas sont prévus lors de l’ajout d’une ville :

- **Même format de données, autre URL** : une entrée de configuration suffit.
- **Nouveau format** : un nouvel adaptateur est créé dans `adapters/out/<format>/`, avec son DTO, son mapper, sa source et sa fabrique `ParkingProviderFactory`. Le domaine, le contrôleur et le contrat REST ne changent pas.

Une configuration invalide (format inconnu, identifiant dupliqué ou champ manquant) fait échouer le démarrage de l’application afin d’éviter qu’une ville soit ignorée silencieusement.

## Tests

- `domain/` : tests unitaires purs de la formule de Haversine, des emprises, de l’identification de ville, du filtrage, du tri et de la limitation.
- `adapters/out/portail/` : tests du mapping à partir d’une réponse Open Data enregistrée et tests HTTP sans réseau avec `MockRestServiceServer`.
- `adapters/out/registry/` : tests du routage, de la priorité des emprises, de l’isolation du cache et de la validation de configuration.
- `adapters/in/rest/` : tests du contrat HTTP avec `@WebMvcTest` pour les réponses `200`, `400`, `404` et `503`.

## Limites et évolutions possibles

- Compléter les coordonnées manquantes à l’aide d’un référentiel statique par ville.
- Permettre l’ajout ou la modification de villes à chaud, via une base de données ou une API d’administration.
- Remplacer les emprises rectangulaires par des polygones pour les villes voisines ou chevauchantes.
- Ajouter des adaptateurs pour des sources CSV, XML, GTFS ou pour des sources séparant catalogue et temps réel.
- Signaler explicitement les données périmées.
- Gérer la pagination des sources importantes.
- Ajouter authentification, limitation de débit, CORS, traces, Dockerfile et intégration continue.
- Ajouter un test ArchUnit afin de vérifier automatiquement que le domaine ne dépend pas de Spring.
- Introduire une recherche par ville explicite; utile pour un client qui connaît déjà sa ville, pour les tests et pour les emprises qui se chevauchent.
- Ajouter `spring-boot-starter-actuator` pour exposer `/actuator/health` et des métriques (latence de la source, erreurs, taux de hit du cache par ville).
