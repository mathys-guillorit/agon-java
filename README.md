# Agon - Jeu de Plateau en Java

Implémentation complète en **Java 21** du jeu de stratégie combinatoire abstrait **Agon** (également appelé la *Garde de la Reine*). Ce projet met l'accent sur les principes de conception orientée objet, l'application de patrons de conception (*Design Patterns*), la séparation des responsabilités et les tests automatisés.

---

## Présentation du Jeu

Agon se joue sur un plateau hexagonal concentrique. Chaque joueur contrôle :
- **1 Reine**
- **6 Gardes**

**Objectif :** Être le premier joueur à amener sa Reine sur la case centrale (le trône) entourée de ses 6 gardes.

---

## Stack Technique & Architecture

- **Langage :** Java 21
- **Interface Utilisateur :** JavaFX (IHM)
- **Gestionnaire de build & dépendances :** Maven
- **Tests unitaires :** JUnit 5
- **Documentation :** Javadoc

### Principes de Conception & Bonnes Pratiques
- **Modèle-Vue-Contrôleur (MVC) :** Séparation stricte entre les règles métiers (moteur de jeu déterministe) et l'interface graphique (JavaFX).
- **Design Patterns :** Application de patrons de conception classiques (GoF) pour assurer la modularité, la lisibilité et l'extensibilité du code.
- **Robustesse & Qualité :** Validation systématique des coups, gestion des exceptions et non-régression validée par une suite de tests unitaires.

---

## Installation & Exécution

### Prérequis
- **JDK 21** ou version supérieure
- **Maven 3.8+**
- 
Pour compiler l'ensemble du projet :
**mvn clean compile**

Pour lancer les tests automatisés :
**mvn test**

Pour exécuter l'application JavaFX :
**mvn javafx:run**

### Cloner le dépôt

```bash
git clone [https://github.com/mathys-guillorit/agon-java.git](https://github.com/mathys-guillorit/agon-java.git)
cd agon-java
