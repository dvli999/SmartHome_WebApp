
# SmartHome WebApp - Système Domotique Distribué

> **Projet de Collaboration**  
> Ce projet a été développé en collaboration avec [Ahmed Mbarek](https://github.com/Burden19)

## Table des Matières
- [Aperçu](#aperçu)
- [Architecture Distribuée](#architecture-distribuée)
- [Fonctionnalités](#fonctionnalités)
- [Stack Technologique](#stack-technologique)
- [Structure du Projet](#structure-du-projet)
- [Prérequis](#prérequis)
- [Installation et Configuration](#installation-et-configuration)
- [Services SOAP et API](#services-soap-et-api)
- [Intégration du Machine Learning](#intégration-du-machine-learning)
- [Contribution](#contribution)

---

## Aperçu

SmartHome WebApp est une solution de domotique avancée reposant sur une **architecture distribuée**. Elle permet la gestion intelligente des appareils et l'optimisation énergétique en combinant plusieurs protocoles de communication (RMI, CORBA, SOAP, JMS) et une base de données NoSQL (MongoDB). Le système intègre également un module de Machine Learning pour la prédiction de consommation.

## Architecture Distribuée

Ce projet démontre l'interopérabilité de plusieurs technologies middleware :

1.  **SOAP (JAX-WS)** : Services Web pour la gestion de l'énergie et l'administration globale (`EnergyManagementService`, `DeviceManagementService`).
2.  **CORBA** : Gestion centralisée du temps système et synchronisation (`Temps.idl`).
3.  **RMI (Remote Method Invocation)** : Pilotage à distance des objets "Appareils" (`AppareilInterface`).
4.  **JMS (Java Message Service)** : Système de notifications asynchrones et alertes (`NotificationJMS`).
5.  **MongoDB** : Persistance des données (logs énergétiques, état des appareils).

## Fonctionnalités

- **Pilotage Multi-Protocole** : Contrôle des appareils via RMI et SOAP.
- **Web Services SOAP** : Exposition de services standardisés (WSDL) pour la consommation d'énergie et la gestion des seuils.
- **Base de Données NoSQL** : Stockage flexible et performant des historiques avec MongoDB.
- **Monitoring Intelligent** : Prédictions de consommation via modèles Random Forest (Python).
- **Synchronisation Temporelle** : Serveur de temps distribué via CORBA.
- **Tableau de Bord Web** : Interface `dashboard.html` servie par un serveur HTTP léger (`WebServer.java`).

## Stack Technologique

### Backend & Middleware
- **Java (JDK 8+)** : Cœur du système.
- **JAX-WS (SOAP)** : Publication des Web Services.
- **JacORB / IDL** : Implémentation CORBA.
- **Java RMI** : Invocation distante native.
- **ActiveMQ (ou équivalent JMS)** : Gestion des messages.

### Base de Données
- **MongoDB** : Base de données principale (Remplacement de H2).

### Machine Learning
- **Python 3.x** : Scikit-learn, Pandas.
- **Modèles** : Random Forest (`rf_modele_energie.pkl`).

### Frontend
- **HTML5 / JavaScript** : Dashboard interactif.

## Structure du Projet

```text
SmartHome_WebApp/
├── .idea/                  # Configuration IDE
├── ml/                     # Module Machine Learning
│   ├── features_energie.pkl
│   ├── rf_modele_energie.pkl
│   ├── scaler_energie.pkl
│   └── predict_ml.py
├── src/
│   └── main/
│       ├── controller/
│       │   └── ControllerMain.java       # Point d'entrée principal
│       ├── corba/                        # Module CORBA (Gestion du Temps)
│       │   ├── Temps.idl
│       │   ├── TempsImpl.java
│       │   └── ... (Stubs & Skeletons)
│       ├── jms/                          # Module JMS
│       │   └── NotificationJMS.java
│       ├── rmi/                          # Module RMI (Appareils)
│       │   ├── AppareilInterface.java
│       │   └── AppareilImpl.java
│       ├── soap/                         # Services SOAP
│       │   ├── EnergyManagementService.java
│       │   ├── DeviceManagementService.java
│       │   └── SoapServicePublisher.java
│       ├── web/                          # Serveur Web & DB
│       │   ├── MongoDBManager.java       # Connexion MongoDB
│       │   └── WebServer.java            # Serveur HTTP Simple
│       └── dashboard.html                # Interface Utilisateur
├── target/                 # Fichiers compilés et générés (WSDL, XSD)
├── pom.xml                 # Dépendances Maven
├── check_setup.bat         # Script de vérification
├── run.bat                 # Script de lancement
└── readme.md
```

## Prérequis

- **Java JDK 8** ou supérieur.
- **Maven** pour la construction.
- **MongoDB Server** (installé et en cours d'exécution sur le port par défaut 27017).
- **Python 3.x** avec les librairies : `pandas`, `scikit-learn`, `joblib`.

## Installation et Configuration

1.  **Cloner le Dépôt**
    ```bash
    git clone https://github.com/dvli999/SmartHome_WebApp.git
    cd SmartHome_WebApp
    ```

2.  **Configurer MongoDB**
    Assurez-vous que MongoDB est lancé localement. Le fichier `MongoDBManager.java` est configuré par défaut pour se connecter à :
    `mongodb://localhost:27017`

3.  **Installer les Dépendances Java**
    ```bash
    mvn clean install
    ```

4.  **Préparer l'environnement Python**
    ```bash
    cd ml
    pip install pandas scikit-learn joblib
    cd ..
    ```

## Lancement de l'Application

### Via le script automatique (Windows)
Double-cliquez simplement ou exécutez :
```bash
run.bat
```

### Lancement Manuel
L'application démarre plusieurs services (Serveur RMI, ORB CORBA, Publisher SOAP, Serveur Web). Exécutez la classe principale :

```bash
mvn exec:java -Dexec.mainClass="controller.ControllerMain"
```

Accédez ensuite au tableau de bord via :
**http://localhost:8080/dashboard.html** (ou le port défini dans `WebServer.java`).

## Services SOAP et API

Une fois l'application lancée, les descriptions WSDL des services SOAP sont généralement accessibles via :

*   **Energy Service** : `http://localhost:9999/ws/energy?wsdl` (Exemple d'URL, vérifier `SoapServicePublisher.java`)
*   **Device Service** : `http://localhost:9999/ws/device?wsdl`

Ces services exposent des méthodes telles que :
*   `getEnergyHistory()`
*   `predictEnergyConsumption()`
*   `shutdownAllDevices()`

## Intégration du Machine Learning

Le système utilise un script Python (`predict_ml.py`) appelé par le backend Java.
*   **Flux :** Java reçoit une requête SOAP/RMI -> Extrait les données -> Appelle le script Python -> Python charge `rf_modele_energie.pkl` -> Retourne la prédiction au Java.

## Contribution

Les contributions sont les bienvenues. Veuillez suivre le flux standard Fork -> Feature Branch -> PR.

## Auteurs 👥

- **Mohamed Ali Thabet** - [Profil GitHub](https://github.com/dvli999)
- **Ahmed Mbarek** - [Profil GitHub](https://github.com/Burden19)

