# Color Run - Application de Gestion de Courses

## Description

Color Run est une application web Java permettant la gestion complète de courses de running (color runs). L'application offre un système complet de gestion des participants, des organisateurs, des associations sportives et des paiements en ligne.

## Architecture Technique

### Technologies utilisées

- **Langage** : Java 15/23
- **Framework Web** : Jakarta EE 6.1.0 (Servlets)
- **Template Engine** : Thymeleaf 3.1.2
- **Base de données** : H2 Database 2.2.220 (embarquée)
- **Gestionnaire de dépendances** : Maven 3
- **Pool de connexions** : HikariCP 5.0.1
- **Frontend** : HTML5, CSS3, JavaScript, Tailwind CSS
- **Paiements** : Stripe Java SDK 24.16.0
- **Génération PDF** : iText 7.2.5
- **Email** : Jakarta Mail 2.0.1
- **Sécurité** : BCrypt (jBCrypt 0.4)
- **Tests** : JUnit 5, Mockito, AssertJ

### Architecture du projet

```
src/
├── main/
│   ├── java/fr/esgi/color_run/
│   │   ├── business/           # Entités métier
│   │   ├── configuration/      # Configuration Thymeleaf
│   │   ├── repository/         # Couche d'accès aux données
│   │   │   └── impl/
│   │   ├── service/           # Couche de services
│   │   │   └── impl/
│   │   ├── servlet/           # Contrôleurs Web (Servlets)
│   │   └── util/              # Utilitaires
│   ├── resources/
│   │   ├── config.properties  # Configuration application
│   │   ├── script.sql         # Script d'initialisation BDD
│   │   └── assets/            # Ressources statiques
│   └── webapp/
│       ├── WEB-INF/
│       │   └── web.xml        # Configuration Web
│       ├── css/               # Feuilles de style
│       ├── js/                # Scripts JavaScript
│       └── images/            # Images
├── test/                      # Tests unitaires
└── db_file/                   # Fichiers base de données H2
```

## Fonctionnalités

### Gestion des utilisateurs
- **Trois types de rôles** :
  - `ADMIN` : Administration complète
  - `ORGANIZER` : Création et gestion de courses
  - `RUNNER` : Participation aux courses

### Gestion des courses
- Création de courses avec géolocalisation
- Gestion des inscriptions et paiements
- Limitation du nombre de participants
- Génération automatique de dossards PDF
- Calcul de distances géographiques

### Associations sportives
- Gestion d'associations d'organisateurs
- Demandes d'adhésion avec validation administrative
- Historique des associations

### Communication
- Système de chat intégré pour chaque course
- Modération des messages
- Épinglage et masquage de messages

### Paiements
- Intégration complète Stripe
- Gestion des sessions de paiement
- Callbacks de succès/échec
- Historique des paiements

### Administration
- Tableau de bord administrateur
- Gestion des utilisateurs
- Validation des demandes d'organisateurs
- Modération du contenu

## Configuration

### Configuration de la base de données

Le chemin de la base de données se configure dans le fichier `src/main/resources/config.properties` :

```properties
# Configuration du chemin de la base de données H2
db.path=C:/Users/votre-utilisateur/Desktop/projet-annuelle/Color_run/db_file/colorun

# Autres exemples de configuration :
# db.path=/Users/utilisateur/projets/Color_run/db_file/colorun  # macOS/Linux
# db.path=D:\\Projets\\Color_run\\db_file\\colorun             # Windows
```

**Important** : 
- Utiliser des slashes `/` ou doubler les backslashes `\\` sur Windows
- Le chemin doit être absolu ou relatif au répertoire du projet
- Le répertoire `db_file/` doit exister avant le premier lancement

### Configuration Maven

Dans le fichier `pom.xml`, la propriété `db.path` est également définie :

```xml
<properties>
    <db.path>${project.basedir}/db_file/colorun</db.path>
</properties>
```

### Configuration Stripe

Configuration des clés API Stripe dans `config.properties` :

```properties
stripe.public.key=pk_test_votre_cle_publique
stripe.secret.key=sk_test_votre_cle_secrete
stripe.success.url=http://localhost:8080/color_run_war/payment-success
stripe.cancel.url=http://localhost:8080/color_run_war/payment-cancel
```

### Configuration Email

Pour l'envoi d'emails (notifications, réinitialisation mot de passe) :

```properties
email.smtp.host=smtp.gmail.com
email.smtp.port=587
email.smtp.auth=true
email.smtp.starttls.enable=true
```

## Installation et déploiement

### Prérequis

- **Java 15 ou supérieur**
- **Apache Tomcat 10.x** (compatible Jakarta EE 6.x)
- **Maven 3.6+**

### Version de Tomcat recommandée

Le projet utilise Jakarta EE 6.1.0, il est donc recommandé d'utiliser :
- **Apache Tomcat 10.1.x** ou supérieur
- **Apache Tomcat 11.x** (dernière version)

### Compilation et déploiement

1. **Cloner le projet**
```bash
git clone <url-du-repo>
cd Color_run
```

2. **Configurer la base de données**
```bash
# Créer le répertoire si nécessaire
mkdir db_file
```

3. **Modifier la configuration**
Éditer `src/main/resources/config.properties` avec les bons chemins et clés API.

4. **Compiler le projet**
```bash
mvn clean compile
```

5. **Initialiser la base de données**
```bash
mvn sql:execute
```

6. **Générer le WAR**
```bash
mvn clean package
```

7. **Déployer sur Tomcat**
- Copier `target/color_run-1.0-SNAPSHOT.war` dans le répertoire `webapps/` de Tomcat
- Renommer en `color_run_war.war` si nécessaire
- Démarrer Tomcat

### URL d'accès

L'application sera accessible à l'adresse :
```
http://localhost:8080/color_run_war/
```

**Note** : Le port 8080 est configuré dans l'application. Assurez-vous que Tomcat utilise ce port ou modifiez les URLs dans `config.properties`.


### Base de données de test

Une base de données séparée est utilisée pour les tests :
- Fichier : `db_file/colorun_test.mv.db`
- Script d'initialisation : `src/test/resources/script.sql`

## Structure de la base de données

### Tables principales

- **member** : Utilisateurs (administrateurs, organisateurs, coureurs)
- **association** : Associations sportives
- **course** : Courses avec géolocalisation
- **course_member** : Inscriptions aux courses
- **association_member** : Adhésions aux associations
- **organizer_request** : Demandes pour devenir organisateur
- **discussion** : Discussions de cours
- **message** : Messages de chat
- **paiement** : Historique des paiements

### Comptes par défaut

Après initialisation, plusieurs comptes sont créés :

**Administrateurs** :
- Email : `jean.dupont@email.com`
- Email : `marie.rousseau@admin.com`

**Organisateurs** :
- Email : `alice.durand@email.com`
- Et plusieurs autres...

**Coureurs** :
- Email : `sophie.martin@email.com`
- Et plusieurs autres...

**Mot de passe par défaut** : Tous les comptes utilisent le même mot de passe hashé.

## Développement

### Lancement en mode développement

1. **Avec Maven et Tomcat intégré** (si configuré)
2. **Avec IDE** (IntelliJ IDEA, Eclipse)
   - Importer le projet Maven
   - Configurer un serveur Tomcat
   - Déployer le WAR

### Logs et debugging

- Les logs de l'application sont visibles dans la console Tomcat
- Les fichiers de trace H2 sont disponibles dans `db_file/`
- Mode debug activable via les paramètres JVM

## Sécurité

- **Authentification** : Session-based avec BCrypt
- **Autorisation** : Contrôle d'accès basé sur les rôles
- **HTTPS** : Recommandé en production
- **Base de données** : H2 en mode serveur pour l'accès concurrent

## Maintenance

### Sauvegarde de la base de données

```bash
# Sauvegarder les fichiers H2
cp db_file/colorun.mv.db db_file/colorun_backup_$(date +%Y%m%d).mv.db
```

### Changement de configuration

Pour changer le chemin de la base de données :
1. Modifier `src/main/resources/config.properties`
2. Optionnellement modifier `pom.xml` (propriété `db.path`)
3. Recompiler et redéployer
4. Migrer les données si nécessaire

### Monitoring

- Surveiller l'espace disque (fichiers H2 peuvent grandir)
- Vérifier les logs Tomcat pour les erreurs
- Monitorer l'utilisation mémoire (pool de connexions)

