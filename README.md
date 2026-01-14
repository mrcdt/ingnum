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

# TP2 - Conteneurisation

## 1. Architecture du Projet

C'est la structure de ton dossier `ingnum` à la racine de ton projet :

```text
ingnum/
├── docker-compose.yml
├── RentalService/
│   ├── src/main/java/.../RentalController.java
│   ├── src/main/resources/application.properties
│   ├── build/libs/RentalService-0.0.1-SNAPSHOT.jar
│   ├── Dockerfile
│   └── gradlew
└── PhpService/
    ├── index.php
    └── Dockerfile

```

---

## 2. Les Fichiers Créés et Modifiés

### A. Côté PHP (`PhpService/`)

* **`index.php`** : Renvoie mon nom.
```php
header('Content-Type: text/plain');

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    echo "Marine Cadet";
} else {
    http_response_code(405);
    echo "Method Not Allowed";
}

```


* **`Dockerfile`** :
```dockerfile
FROM php:8.2-apache
COPY index.php /var/www/html/index.php
EXPOSE 80

```

* **Construire l'image**

Dans `ingnum/PhpService` :
```
docker build -t marinec-php-service .
```
Vérifier la création de l'image :
```
docker images
```



### B. Côté Java (`RentalService/`)

* **`application.properties`** : Définit l'URL du service PHP pour Docker.
```properties
customer.service.url=http://php-service
```


* **`RentalController.java`** : 
```java
@Value("${customer.service.url}")
private String customerServiceUrl;

@GetMapping("/customer/{name}")
public String getCustomer(@PathVariable String name) {
    RestTemplate restTemplate = new RestTemplate();
    String response = restTemplate.getForObject(customerServiceUrl, String.class);
    return "Client : " + name + " | Réponse du service PHP : " + response;
}

```


* **`Dockerfile`** : Emballe l'application Java 21.
```dockerfile
FROM eclipse-temurin:21-jre-jammy
ADD ./build/libs/RentalService-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

```



### C. `docker-compose.yml`

```yaml
version: '3.8'
services:
  rental-service:
    build: ./RentalService
    ports:
      - "8080:8080"
    networks:
      - rental-network
  php-service:
    build: ./PhpService
    ports:
      - "8082:80"
    networks:
      - rental-network
networks:
  rental-network:
    driver: bridge

```

---

## 3. Liste des Commandes (une à une)

Voici l'ordre exact que nous avons suivi pour tout faire fonctionner :

| Étape | Commande | Utilité |
| --- | --- | --- |
| **1. Build Java** | `.\gradlew build` (dans RentalService) | Crée le fichier .jar indispensable |
| **2. Lancement** | `docker-compose up --build` | Construit les images et lance les services |
| **3. Git Add** | `git add .` | Prépare les fichiers pour la sauvegarde |
| **4. Git Commit** | `git commit -m "TP3 Terminé"` | Crée un point de sauvegarde local |
| **5. Git Push** | `git push origin main` | Envoie tout sur ton GitHub |

---

## 4. Comment Tester le TP

1. **Démarrer les deux services**

Dans le dossier `ingnum`
```
docker-compose up --build
```
2. **Service PHP seul :** [http://localhost:8082](https://www.google.com/search?q=http://localhost:8082) 
3. **Service Java (Communication) :** [http://localhost:8080/customer/Marine](https://www.google.com/search?q=http://localhost:8080/customer/Marine).

## 5. Publier l'image sur Docker Hub

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
https://hub.docker.com/r/marinecdt/marinec-php-service
```

## 6. Mettre à jour GitHub

```
git add .
git commit -m "Ajout du microservice PHP"
git push origin main
```

