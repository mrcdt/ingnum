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



