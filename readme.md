# SmartHome WebApp

> **Projet de Collaboration**  
> Ce projet a été développé en collaboration avec [Ahmed Mbarek](https://github.com/Burden19)

## 📋 Table des Matières
- [Aperçu](#aperçu)
- [Fonctionnalités](#fonctionnalités)
- [Stack Technologique](#stack-technologique)
- [Structure du Projet](#structure-du-projet)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Lancement de l'Application](#lancement-de-lapplication)
- [Base de Données](#base-de-données)
- [Intégration du Machine Learning](#intégration-du-machine-learning)
- [Comment ça Marche](#comment-ça-marche)
- [Contribution](#contribution)
- [Licence](#licence)

## 🏠 Aperçu

SmartHome WebApp est une application web complète conçue pour gérer et surveiller les appareils domotiques et la consommation énergétique. L'application combine une technologie backend Java avec des capacités de machine learning pour fournir des solutions intelligentes d'automatisation domestique et de gestion énergétique.

## ✨ Fonctionnalités

- **Gestion des Appareils Intelligents** : Surveillez et contrôlez divers appareils domotiques
- **Monitoring Énergétique** : Suivez la consommation et la production d'énergie en temps réel
- **Intégration du Machine Learning** : Analyses prédictives pour l'optimisation énergétique
- **Interface Conviviale** : Interface web intuitive pour une interaction facile
- **Intégration Base de Données** : Stockage persistant des données avec base de données H2
- **Automatisation par Scripts Batch** : Scripts simplifiés de configuration et d'exécution pour Windows

## 🛠 Stack Technologique

### Backend
- **Java** (61.3%) - Logique applicative principale
- **Maven** - Automatisation du build et gestion des dépendances
- **Base de Données H2** - Base de données embarquée pour la persistance des données

### Frontend
- **HTML** (28.3%) - Balisage de l'interface utilisateur

### Scripts
- **Fichiers Batch** (5.4%) - Scripts d'automatisation Windows

### Machine Learning
- **Python** (5.0%) - Implémentation des modèles ML et traitement des données

## 📁 Structure du Projet

```
SmartHome_WebApp/
├── .idea/                  # Fichiers de projet IntelliJ IDEA
├── ml/                     # Modèles et scripts de Machine Learning
├── src/
│   └── main/
│       ├── java/          # Fichiers source Java
│       ├── resources/     # Ressources de l'application
│       └── webapp/        # Fichiers de l'application web (HTML, CSS, JS)
├── orb.db                 # Fichier de base de données (gestion des appareils/orbite)
├── smart_energy_db.mv.db  # Fichier de base de données H2 pour les données énergétiques
├── pom.xml                # Configuration du projet Maven
├── check_setup.bat        # Script de vérification de la configuration
├── run.bat                # Script d'exécution de l'application
└── .gitignore            # Règles d'ignore Git
```

## 📋 Prérequis

Avant d'exécuter l'application, assurez-vous d'avoir installé les éléments suivants :

- **Java Development Kit (JDK)** - Version 8 ou supérieure
- **Maven** - Pour construire le projet
- **Python** - Version 3.x (pour les fonctionnalités de machine learning)
- **Navigateur Web** - Navigateur moderne (Chrome, Firefox, Edge)

## 🚀 Installation

1. **Cloner le Dépôt**
   ```bash
   git clone https://github.com/dvli999/SmartHome_WebApp.git
   cd SmartHome_WebApp
   ```

2. **Vérifier la Configuration (Windows)**
   ```bash
   check_setup.bat
   ```
   Ce script vérifiera que toutes les dépendances requises sont correctement installées.

3. **Installer les Dépendances**
   ```bash
   mvn clean install
   ```

4. **Installer les Dépendances Python** (pour les fonctionnalités ML)
   ```bash
   cd ml
   pip install -r requirements.txt
   cd ..
   ```

## ▶️ Lancement de l'Application

### Windows (Recommandé)
Double-cliquez simplement ou exécutez :
```bash
run.bat
```

### Démarrage Manuel (Toutes Plateformes)
```bash
mvn spring-boot:run
```
Ou après la construction :
```bash
java -jar target/SmartHome_WebApp-1.0.jar
```

### Accéder à l'Application
Une fois démarrée, ouvrez votre navigateur web et naviguez vers :
```
http://localhost:8080
```

## 💾 Base de Données

L'application utilise deux instances de base de données :

1. **orb.db** - Gère les configurations des appareils et les données de planification/orbite
2. **smart_energy_db.mv.db** - Base de données H2 stockant :
   - Enregistrements de consommation énergétique
   - Statistiques d'utilisation des appareils
   - Préférences utilisateur
   - Données historiques pour les analyses

### Configuration de la Base de Données
Les paramètres de la base de données peuvent être configurés dans `application.properties` :
```properties
spring.datasource.url=jdbc:h2:file:./smart_energy_db
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=update
```

## 🤖 Intégration du Machine Learning

Le répertoire `ml/` contient des scripts Python pour :

- **Prédiction Énergétique** : Prévision des modèles de consommation énergétique
- **Optimisation des Appareils** : Suggestion d'horaires d'utilisation optimaux des appareils
- **Détection d'Anomalies** : Identification de modèles de consommation énergétique inhabituels
- **Analyse de Données** : Traitement des données historiques pour obtenir des insights

### Fonctionnalités ML
- Prédiction en temps réel de la consommation énergétique
- Recommandations de planification intelligente
- Algorithmes d'optimisation des coûts
- Reconnaissance des modèles d'utilisation

## 🔧 Comment ça Marche

### Vue d'Ensemble de l'Architecture

1. **Couche Frontend**
   - Interface HTML/CSS/JavaScript
   - Design responsive pour divers appareils
   - Visualisation de données en temps réel

2. **Couche Backend**
   - Application Java Spring Boot
   - Points de terminaison API RESTful
   - Implémentation de la logique métier
   - Interactions avec la base de données

3. **Couche Machine Learning**
   - Modèles ML basés sur Python
   - Intégration avec le backend Java via API REST ou exécution de processus
   - Prétraitement des données et ingénierie des fonctionnalités
   - Entraînement du modèle et prédiction

### Flux de l'Application

1. **Interaction Utilisateur** : Les utilisateurs accèdent à l'interface web via leur navigateur
2. **Gestion des Appareils** : Ajout, configuration et surveillance des appareils domotiques
3. **Collecte de Données** : Le système collecte les données de consommation énergétique des appareils
4. **Traitement ML** : Les scripts Python analysent les données et génèrent des prédictions
5. **Visualisation** : Les résultats sont affichés via des graphiques et tableaux de bord
6. **Actions de Contrôle** : Les utilisateurs peuvent contrôler les appareils en fonction des recommandations

### Composants Clés

#### Contrôleur d'Appareils
Gère les connexions et états des appareils domotiques :
- Enregistrement des appareils
- Surveillance des états
- Exécution des commandes

#### Moniteur Énergétique
Suit la consommation énergétique :
- Surveillance en temps réel
- Stockage des données historiques
- Analyse statistique

#### Prédicteur ML
Fournit des insights intelligents :
- Prévision de charge
- Planification optimale
- Prédictions de coûts

#### Gestionnaire de Base de Données
Gère la persistance des données :
- Opérations CRUD
- Optimisation des requêtes
- Intégrité des données

## 🤝 Contribution

Les contributions sont les bienvenues ! N'hésitez pas à soumettre des pull requests ou ouvrir des issues.

### Configuration pour le Développement
1. Forkez le dépôt
2. Créez une branche de fonctionnalité (`git checkout -b feature/NouvelleFonctionnalite`)
3. Commitez vos changements (`git commit -m 'Ajout d'une nouvelle fonctionnalité'`)
4. Poussez vers la branche (`git push origin feature/NouvelleFonctionnalite`)
5. Ouvrez une Pull Request

## 📝 Licence

Ce projet est open source. Veuillez consulter le dépôt pour les détails de la licence.

## 👥 Auteurs

- **Mohamed Ali Thabet** - [Profil GitHub](https://github.com/dvli999)
- **Ahmed Mbarek** - [Profil GitHub](https://github.com/Burden19)

## 📧 Contact

Pour toute question ou support, veuillez ouvrir une issue sur le dépôt GitHub.

---

**Note** : Ce projet est conçu à des fins éducatives et personnelles. Pour un déploiement en production, des mesures de sécurité et configurations supplémentaires doivent être mises en œuvre.