# TP 1. RentalService - Guide d’installation et lancement

## Partie 1️ : Lancer le projet sans Docker

### 1. Installer Java 21

- Télécharge JDK 21 (ex. Eclipse Adoptium Temurin 21) : [https://adoptium.net/](https://adoptium.net/)
- Installe-le dans un dossier, par exemple : C:\Program Files\Java\jdk-21


### 2. Ajouter Java 21 dans le PATH

1. Ouvre **Paramètres → Système → Informations système → Paramètres système avancés → Variables d’environnement**
2. Dans **Variables système → Path → Modifier** :
   - Ajoute en **première position** : C:\Program Files\Java\jdk-21\bin
3. Ferme et rouvre PowerShell pour que les changements soient pris en compte.

### 3. Vérifier la version de Java

```powershell
java -version
```
### 4. Tester

```
cd C:\Users\marin\Desktop\cours\docker\ingnum\RentalService
.\gradlew build
dir build\libs
& "C:\Program Files\Java\jdk-21\bin\java.exe" -jar build\libs\RentalService-0.0.1-SNAPSHOT.jar
```
- tester dans le navigateur : http://localhost:8080/bonjour

## Partie 2️ : Lancer le projet avec Docker

### 1. Créer le Dockerfile

- Dans le dossier `RentalService`, crée un fichier nommé `Dockerfile` :

```dockerfile
FROM eclipse-temurin:21-jre-jammy

VOLUME /tmp

EXPOSE 8080

ADD ./build/libs/RentalService-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```
### 2. Tester le programme avec Docker
```
docker build -t rentalservice .
```
- Veuiller arrêter le serveur précédent et lancer l'application avec Docker :
  ```
  docker run -p 8080:8080 rentalservice
  ```

  Regerder dans le navigateur : http://localhost:8080/bonjour

### 3. Construire l'image avec Docker

```
docker login
docker images
docker tag rentalservice marinecdt/rentalservice:v1
 docker push marinecdt/rentalservice:v1
```

# TP 2. PhpService - Guide d’installation et lancement

## 1. Mise en place du micro-servcie

### 1. Création du micro-service
- Dans le dossier `ingnum`, crée un dossier nommé `PhpService` qui sera notre seconde micro-service

### 2. Création du programme php
- Dans le dossier `PhpService`, crée un fichier nommé `index.php` :
```
<?php
header('Content-Type: text/plain');

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    echo "Marine Cadet";
} else {
    http_response_code(405);
    echo "Method Not Allowed";
}
```
### 3. Création du Dockerfil
- Dans le dossier `PhpService`, crée un fichier nommé `Dockerfile` :
```
FROM php:8.2-apache

VOLUME /tmp

EXPOSE 80

ADD ./index.php /var/www/html/index.php

ENTRYPOINT ["apache2-foreground"]
```

## 2. Construire l'image

Dans `ingnum/PhpService` :
```
docker build -t marinec-php-service .
```
Vérifier la création de l'image :
```
docker images
```

## 3. Tester
```
docker run -p 8082:80 marinec-php-service
```

Dans le navigateur : 
```
http://localhost:8082
```

Résultat :
```
Marine Cadet
```

## 4. Publier l'image sur Docker Hub

Se connecter à Docker Hub :
```
docker login
```

Créer un tag sur l'image :
```
docker tag marinec-php-service marinecdt/marinec-php-service:latest
```

Publier l'image :
```
docker push marinecdt/marinec-php-service:latest
```

Vérifier la présence de l'image sur Docker Hub : 
```
https://hub.docker.com/r/tonpseudo/prenom-php-service
```

## 5. Mettre à jour GitHub

```
git add .
git commit -m "Ajout du microservice PHP"
git push origin main
```

# TP 3. Orchestration et Communication Microservices

Ce volet finalise le projet en mettant en place une architecture orchestrée où le microservice PHP communique avec le microservice Java au sein d'un réseau Docker.

## 1. Architecture Réseau
L'application utilise un réseau Docker de type `bridge` nommé `microservices-network`. 
- **PHP (`phpservice`)** : Client qui initie la requête.
- **Java (`rental-service`)** : Serveur API qui traite la requête.

## 2. Modifier Dockerfile `RentalService`

```
# --- Étape 1 : Build (Compilation) ---
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# On copie les fichiers de configuration Gradle en premier pour mettre en cache les dépendances
COPY build.gradle settings.gradle ./
# On copie le code source
COPY src ./src

# On lance la compilation (le -x test permet d'aller plus vite en ignorant les tests unitaires)
RUN gradle build --no-daemon -x test

# --- Étape 2 : Run (Exécution) ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# On récupère UNIQUEMENT le JAR généré à l'étape précédente
# Le dossier de build de Gradle dans le conteneur est /app/build/libs/
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
```

## 2. Configuration Orchestrée (docker-compose.yml)
À la racine du projet, le fichier `docker-compose.yml` définit les deux services :

```yaml
version: '3.8'

services:
  phpservice:
    build:
      context: ./PhpService
      dockerfile: Dockerfile
    container_name: phpservice
    ports:
      - "8081:80"
    networks:
      - microservices-network
    restart: unless-stopped

  rental-service:
    build:
      context: ./RentalService
      dockerfile: Dockerfile
    container_name: rental-service
    ports:
      - "8080:8080"
    networks:
      - microservices-network
    depends_on:
      - phpservice
    restart: unless-stopped

networks:
  microservices-network:
    driver: bridge
```

## 3. Code Source des Microservices

### A. PHP Service (`index.php`)
Le code PHP identifie la méthode HTTP reçue par le navigateur et la transmet au service Java en utilisant le nom du service Docker (`rental-service`) comme hôte.



```php
<?php
$prenom = "Marine";
$javaService = "http://rental-service:8080/api/rentals";
$method = $_SERVER['REQUEST_METHOD'];

// Configuration du contexte pour l'appel au service Java
$options = [
    'http' => [
        'method' => $method,
        'header' => "Content-Type: application/json\r\n",
        'content' => json_encode(["client" => "PHP"])
    ]
];
$context = stream_context_create($options);

// Logique de routage vers le service Java
if ($method === 'GET') {
    $response = file_get_contents($javaService);
} elseif (in_array($method, ['POST', 'PUT', 'PATCH', 'DELETE'])) {
    $url = ($method === 'POST') ? $javaService : "$javaService/1";
    $response = file_get_contents($url, false, $context);
}

// Affichage final
echo "Prénom : " . $prenom . "<br>";
echo "Réponse du service Java : " . $response;
?>
```

### B. Java Service (`RentalController.java`)

Le microservice backend est développé avec **Spring Boot**. L'API Java expose les différents points d'entrée (endpoints) sur le chemin `/api/rentals`. L'annotation `@RestController` permet de retourner directement des chaînes de caractères ou du JSON au service PHP.



```java
package com.ingnum.rentalservice.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    /**
     * Récupère la liste des locations (Appelé par le GET de PHP)
     */
    @GetMapping
    public String getRentals() {
        return "GET → Java : liste des locations";
    }

    /**
     * Crée une nouvelle location (Appelé par le POST de PHP)
     */
    @PostMapping
    public String createRental(@RequestBody String body) {
        return "POST → Java : création location " + body;
    }

    /**
     * Met à jour une location (Appelé par le PUT de PHP)
     */
    @PutMapping("/{id}")
    public String updateRental(@PathVariable int id, @RequestBody String body) {
        return "PUT → Java : remplacement location " + id;
    }

    /**
     * Modification partielle (Appelé par le PATCH de PHP)
     */
    @PatchMapping("/{id}")
    public String patchRental(@PathVariable int id, @RequestBody String body) {
        return "PATCH → Java : modification partielle location " + id;
    }

    /**
     * Supprime une location (Appelé par le DELETE de PHP)
     */
    @DeleteMapping("/{id}")
    public String deleteRental(@PathVariable int id) {
        return "DELETE → Java : suppression location " + id;
    }
}
```

## 4. Procédure de Déploiement

Cette étape permet de transformer le code source en conteneurs opérationnels. Elle se décompose en deux phases : la préparation du livrable Java et le lancement de l'orchestration globale.

### 1. Compilation du projet Java
Il est indispensable de générer manuellement le fichier `.jar` avant de construire l'image Docker. Cela garantit que les dernières modifications apportées au code source (notamment le `RentalController`) sont bien incluses dans l'image finale.



```powershell
# Accéder au dossier du service Java
cd RentalService

# Nettoyer les anciens builds et générer le nouveau JAR avec Gradle
.\gradlew clean build

# Revenir à la racine du projet (où se trouve le docker-compose.yml)
cd ..
```

### 2. Lancement de l'orchestration
Le fichier `docker-compose.yml` est le chef d'orchestre de votre infrastructure. Il permet de monter simultanément les deux services (`phpservice` et `rental-service`) ainsi que le réseau privé qui les lie, le tout en une seule ligne de commande.

```powershell
# Lancer l'ensemble de l'architecture et forcer la reconstruction des images
docker-compose up --build
```

## 5. TEst et validation
Cette étape permet de confirmer que les deux microservices sont non seulement actifs, mais qu'ils parviennent à échanger des données à travers le réseau Docker.

* **Test PHP (Port 8081)** : Accédez à [http://localhost:8081].
   * Résultat : Le navigateur affiche "Prénom : Marine" suivi de la réponse envoyée par le service Java : "GET → Java : liste des locations". Cela       valide la réussite de la communication inter-conteneurs.
* **Test Java (Port 8080)** : Accédez à [http://localhost:8080/api/rentals] pour vérifier l'état du backend de manière indépendante.

## 6. Publication sur Docker Hub

Une fois les tests validés, les images locales ont été taguées avec mon identifiant Docker Hub `(marinecdt)` puis poussées sur le registre public pour permettre un déploiement distant.

```
# --- Service PHP ---
# Tag de l'image locale vers le format Docker Hub
docker tag ingnum-phpservice:latest marinecdt/marinec-php-service:latest

# Publication de l'image
docker push marinecdt/marinec-php-service:latest

# --- Service Java ---
# Tag de l'image locale vers le format Docker Hub
docker tag ingnum-rental-service:latest marinecdt/rentalservice:latest

# Publication de l'image
docker push marinecdt/rentalservice:latest
```
