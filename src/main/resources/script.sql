-- ========================================
-- SCRIPT SQL COMPLET ET UNIFIÉ - COLOR RUN
-- Version finale avec toutes les améliorations
-- ========================================

-- Suppression des tables si elles existent (pour réinitialisation complète)
DROP TABLE IF EXISTS Paiement CASCADE;
DROP TABLE IF EXISTS Message CASCADE;
DROP TABLE IF EXISTS Discussion CASCADE;
DROP TABLE IF EXISTS CourseMember CASCADE;
DROP TABLE IF EXISTS AssociationMember CASCADE;
DROP TABLE IF EXISTS OrganizerRequest CASCADE;
DROP TABLE IF EXISTS Course CASCADE;
DROP TABLE IF EXISTS Association CASCADE;
DROP TABLE IF EXISTS Member CASCADE;

-- ========================================
-- CRÉATION DES TABLES
-- ========================================

-- Table des membres (utilisateurs)
CREATE TABLE IF NOT EXISTS Member (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      role VARCHAR(20) DEFAULT 'RUNNER',
                                      name VARCHAR(255),
                                      firstname VARCHAR(255),
                                      email VARCHAR(255) UNIQUE,
                                      password VARCHAR(255),
                                      phoneNumber VARCHAR(15),
                                      address VARCHAR(255),
                                      city VARCHAR(255),
                                      zipCode INTEGER,
                                      positionLatitude DOUBLE,
                                      positionLongitude DOUBLE
);

-- Table des associations
CREATE TABLE IF NOT EXISTS Association (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           name VARCHAR(255),
                                           description VARCHAR(255),
                                           websiteLink VARCHAR(255),
                                           logoPath VARCHAR(255),
                                           email VARCHAR(255) UNIQUE,
                                           phoneNumber VARCHAR(15),
                                           address VARCHAR(255),
                                           city VARCHAR(255),
                                           zipCode INTEGER
);

-- Table des courses
CREATE TABLE IF NOT EXISTS Course (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      name VARCHAR(255),
                                      description VARCHAR(255),
                                      associationId INTEGER,
                                      memberCreatorId INTEGER,
                                      startDate TIMESTAMP,
                                      endDate TIMESTAMP,
                                      startPositionLatitude DOUBLE,
                                      startPositionLongitude DOUBLE,
                                      endPositionLatitude DOUBLE,
                                      endPositionLongitude DOUBLE,
                                      distance DOUBLE,
                                      address VARCHAR(255),
                                      city VARCHAR(255),
                                      zipCode INTEGER,
                                      maxOfRunners INTEGER,
                                      currentNumberOfRunners INTEGER,
                                      price DOUBLE,
                                      FOREIGN KEY (associationId) REFERENCES Association(id),
                                      FOREIGN KEY (memberCreatorId) REFERENCES Member(id)
);

-- Table des inscriptions aux courses
CREATE TABLE IF NOT EXISTS CourseMember (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            courseId INTEGER,
                                            memberId INTEGER,
                                            registrationDate VARCHAR(255),
                                            registrationStatus VARCHAR(255),
                                            stripeSessionId VARCHAR(255) DEFAULT NULL,
                                            bibNumber INTEGER DEFAULT NULL,
                                            FOREIGN KEY (courseId) REFERENCES Course(id),
                                            FOREIGN KEY (memberId) REFERENCES Member(id)
);

-- Table des membres d'associations
CREATE TABLE IF NOT EXISTS AssociationMember (
                                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                 memberId INTEGER,
                                                 associationId INTEGER,
                                                 joinDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                 FOREIGN KEY (associationId) REFERENCES Association(id),
                                                 FOREIGN KEY (memberId) REFERENCES Member(id),
                                                 UNIQUE(memberId, associationId)
);

-- Table des demandes d'organisateur
CREATE TABLE IF NOT EXISTS OrganizerRequest (
                                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                memberId BIGINT NOT NULL,
                                                motivation TEXT NOT NULL,
                                                existingAssociationId BIGINT,
                                                newAssociationData TEXT,
                                                requestDate TIMESTAMP NOT NULL,
                                                status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                                adminComment TEXT,
                                                processedByAdminId BIGINT,
                                                processedDate TIMESTAMP,
                                                requestType VARCHAR(50) DEFAULT 'BECOME_ORGANIZER',
                                                existingAssociationName VARCHAR(255),
                                                FOREIGN KEY (memberId) REFERENCES Member(id),
                                                FOREIGN KEY (existingAssociationId) REFERENCES Association(id),
                                                FOREIGN KEY (processedByAdminId) REFERENCES Member(id)
);

-- Table des discussions
CREATE TABLE IF NOT EXISTS Discussion (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          courseId INTEGER,
                                          isActive BOOLEAN DEFAULT TRUE,
                                          FOREIGN KEY (courseId) REFERENCES Course(id),
                                          UNIQUE(courseId)
);

-- Table des messages
CREATE TABLE IF NOT EXISTS Message (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       discussionId INTEGER,
                                       memberId INTEGER,
                                       content VARCHAR(1000),
                                       originalContent VARCHAR(1000),
                                       date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       lastModifiedDate TIMESTAMP,
                                       isPin BOOLEAN DEFAULT FALSE,
                                       isHidden BOOLEAN DEFAULT FALSE,
                                       isModified BOOLEAN DEFAULT FALSE,
                                       isDeleted BOOLEAN DEFAULT FALSE,
                                       hiddenByMemberId INTEGER,
                                       FOREIGN KEY (discussionId) REFERENCES Discussion(id),
                                       FOREIGN KEY (memberId) REFERENCES Member(id),
                                       FOREIGN KEY (hiddenByMemberId) REFERENCES Member(id)
);

-- Table des paiements
CREATE TABLE IF NOT EXISTS Paiement (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        courseMemberId INTEGER,
                                        date VARCHAR(32),
                                        amount DOUBLE,
                                        FOREIGN KEY (courseMemberId) REFERENCES CourseMember(id)
);

-- ========================================
-- INSERTION DES DONNÉES DE TEST
-- ========================================

-- ADMINISTRATEURS
INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ADMIN', 'Dupont', 'Jean', 'jean.dupont@email.com', 'password123', '0601020304', '12 rue de Paris', 'Paris', 75001, 48.8566, 2.3522
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'jean.dupont@email.com');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ADMIN', 'Rousseau', 'Marie', 'marie.rousseau@admin.com', 'admin2024', '0612345600', '45 avenue de la République', 'Lyon', 69002, 45.764, 4.8357
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'marie.rousseau@admin.com');

-- ORGANISATEURS CONFIRMÉS
INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Durand', 'Alice', 'alice.durand@email.com', 'password123', '0612345678', '45 rue des Lilas', 'Nantes', 44000, 47.218371, -1.553621
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'alice.durand@email.com');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Monet', 'Léa', 'lea.monet@outlook.com', 'password123', '0612425690', '16 Rue Perrière', 'Annecy', 74000, 45.89812149200697, 6.127460404462661
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'lea.monet@outlook.com');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Dubois', 'Thomas', 'thomas.dubois@email.com', 'secure456', '0689012345', '12 avenue de la République', 'Bordeaux', 33000, 44.8378, -0.5792
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'thomas.dubois@email.com');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Lefebvre', 'Antoine', 'antoine.lefebvre@email.com', 'antoine123', '0701234567', '23 boulevard Jean Jaurès', 'Grenoble', 38000, 45.1885, 5.7245
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'antoine.lefebvre@email.com');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Varin', 'Elise', 'elisevarin@email.com', 'elise123', '0781235537', '19 Pl. des Célestins','Lyon', 69002, 45.75989913978603, 4.831608799811371
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'elisevarin@email.com');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Lambert', 'Nicolas', 'nicolas.lambert@sport.fr', 'nico2024', '0756789012', '78 rue des Sports', 'Toulouse', 31000, 43.604652, 1.444209
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'nicolas.lambert@sport.fr');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Garcia', 'Carmen', 'carmen.garcia@run.es', 'carmen123', '0634567891', '12 place de l''Espagne', 'Perpignan', 66000, 42.6886, 2.8946
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'carmen.garcia@run.es');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Fischer', 'Klaus', 'klaus.fischer@alsace.fr', 'klaus2024', '0387654321', '45 rue de Strasbourg', 'Mulhouse', 68100, 47.7508, 7.3359
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'klaus.fischer@alsace.fr');

-- PARTICIPANTS RÉGULIERS
INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Martin', 'Sophie', 'sophie.martin@email.com', 'pass456', '0612345678', '25 avenue des Champs', 'Lyon', 69000, 45.764, 4.8357
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'sophie.martin@email.com');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Pollet', 'Theo', 'theop@mail.com', 'password123', '0765467809', '356 rue Victor Hugo', 'Dijon', 21000, 47.32136825551213, 5.041485596025622
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email =  'theop@mail.com');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Bernard', 'Paul', 'paul.bernard@email.com', 'password123', '0623456789', '12 avenue des Fleurs', 'Nice', 06000, 43.710173, 7.262003
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'paul.bernard@email.com');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Lemoine', 'Claire', 'claire.lemoine@email.com', 'password123', '0634567890', '78 boulevard des Roses', 'Lille', 59000, 50.62925, 3.057256
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'claire.lemoine@email.com');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Petit', 'Marc', 'marc.petit@email.com', 'password123', '0645678901', '23 rue des Champs', 'Strasbourg', 67000, 48.573405, 7.752111
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'marc.petit@email.com');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Roux', 'Sophie', 'sophie.roux@email.com', 'password123', '0656789012', '56 place des Arbres', 'Montpellier', 34000, 43.610769, 3.876716
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'sophie.roux@email.com');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Leroy', 'Camille', 'camille.leroy@email.com', 'password123', '0678901234', '45 rue des Oliviers', 'Marseille', 13008, 43.2715, 5.4018
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'camille.leroy@email.com');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Moreau', 'Julie', 'julie.moreau@email.com', 'julie2023', '0690123456', '8 place du Marché', 'Rennes', 35000, 48.1173, -1.6778
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'julie.moreau@email.com');

-- NOUVEAUX PARTICIPANTS
INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Blanc', 'Pierre', 'pierre.blanc@gmail.com', 'pierre123', '0612345601', '23 rue de la Paix', 'Paris', 75001, 48.8566, 2.3522
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'pierre.blanc@gmail.com');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Noir', 'Emma', 'emma.noir@hotmail.fr', 'emma2024', '0623456702', '67 avenue Victor Hugo', 'Lyon', 69003, 45.7640, 4.8400
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'emma.noir@hotmail.fr');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Vert', 'Lucas', 'lucas.vert@yahoo.fr', 'lucas456', '0634567803', '89 boulevard Gambetta', 'Marseille', 13001, 43.2965, 5.3698
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'lucas.vert@yahoo.fr');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Rouge', 'Manon', 'manon.rouge@orange.fr', 'manon789', '0645678904', '12 place Stanislas', 'Nancy', 54000, 48.6937, 6.1834
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'manon.rouge@orange.fr');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Bleu', 'Hugo', 'hugo.bleu@free.fr', 'hugo012', '0656789005', '34 rue Nationale', 'Tours', 37000, 47.3941, 0.6848
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'hugo.bleu@free.fr');

-- CANDIDATS ORGANISATEURS
INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Duval', 'Maxime', 'maxime.duval@student.fr', 'maxime123', '0712345611', '15 rue des Étudiants', 'Montpellier', 34000, 43.6108, 3.8767
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'maxime.duval@student.fr');

INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Chevalier', 'Sarah', 'sarah.chevalier@pro.fr', 'sarah456', '0723456712', '89 boulevard des Belges', 'Rouen', 76000, 49.4431, 1.0993
WHERE NOT EXISTS (SELECT 1 FROM Member WHERE email = 'sarah.chevalier@pro.fr');

-- ========================================
-- ASSOCIATIONS
-- ========================================

INSERT INTO Association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Association Sportive Paris', 'Promouvoir la course à pied en Île-de-France', 'https://www.assoparis.com', '/images/logo1.png', 'contact@assoparis.com', '0145789652', '10 place de la République', 'Paris', 75011
WHERE NOT EXISTS (SELECT 1 FROM Association WHERE email = 'contact@assoparis.com');

INSERT INTO Association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Courir Ensemble', 'Organisation de courses caritatives', 'https://www.courirensemble.org', '/images/logo2.png', 'contact@courirensemble.org', '0187654321', '15 rue Lafayette', 'Marseille', 13001
WHERE NOT EXISTS (SELECT 1 FROM Association WHERE email = 'contact@courirensemble.org');

INSERT INTO Association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Sport & Solidarité', 'Sport solidaire et courses caritatives', 'https://www.sportsolidarite.org', '/images/logo4.png', 'hello@sportsolidarite.org', '0156789012', '35 rue de la Paix', 'Toulouse', 31001
WHERE NOT EXISTS (SELECT 1 FROM Association WHERE email = 'hello@sportsolidarite.org');

INSERT INTO Association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Run For Fun Association', 'Courses colorées et événements ludiques', 'https://www.runforfun.fr', '/images/logo5.png', 'contact@runforfun.fr', '0134567890', '18 place des Victoires', 'Bordeaux', 33001
WHERE NOT EXISTS (SELECT 1 FROM Association WHERE email = 'contact@runforfun.fr');

INSERT INTO Association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Courir pour la Planète', 'Courses écologiques et sensibilisation environnementale', 'https://www.courirplanete.org', '/images/logo6.png', 'contact@courirplanete.org', '0712345678', '45 rue Verte', 'Nantes', 44000
WHERE NOT EXISTS (SELECT 1 FROM Association WHERE email = 'contact@courirplanete.org');

INSERT INTO Association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Montagne & Running', 'Courses en montagne et trails', 'https://www.montagnerunning.com', '/images/logo7.png', 'info@montagnerunning.com', '0723456789', '18 chemin des Cimes', 'Grenoble', 38000
WHERE NOT EXISTS (SELECT 1 FROM Association WHERE email = 'info@montagnerunning.com');

INSERT INTO Association (name, description, websiteLink, email, phoneNumber, address, city, zipCode)
SELECT 'Run In Lyon', 'Association sportive de Lyon pour promouvoir le sport et la culture lyonnaise.', 'https://www.runinlyon.com/fr', 'info@runinlyon.fr', '0450709654', '9 Pl. des Célestins', 'Lyon', 69002
WHERE NOT EXISTS(SELECT 1 FROM Association WHERE email = 'info@runinlyon.fr');

INSERT INTO Association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Runners du Sud', 'Association de coureurs du sud de la France', 'https://www.runnersdusud.fr', '/images/logo8.png', 'contact@runnersdusud.fr', '0467890123', '78 avenue de la Méditerranée', 'Montpellier', 34000
WHERE NOT EXISTS (SELECT 1 FROM Association WHERE email = 'contact@runnersdusud.fr');

INSERT INTO Association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Les Foulées Alsaciennes', 'Promotion de la course à pied en Alsace', 'https://www.fouleesalsaciennes.fr', '/images/logo9.png', 'contact@fouleesalsaciennes.fr', '0388765432', '56 rue de la Krutenau', 'Strasbourg', 67000
WHERE NOT EXISTS (SELECT 1 FROM Association WHERE email = 'contact@fouleesalsaciennes.fr');

INSERT INTO Association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Trail Aventure', 'Spécialiste des courses nature et trail', 'https://www.trailaventure.org', '/images/logo10.png', 'info@trailaventure.org', '0556789012', '23 chemin des Randonneurs', 'Bordeaux', 33000
WHERE NOT EXISTS (SELECT 1 FROM Association WHERE email = 'info@trailaventure.org');

-- ========================================
-- ASSOCIATION MEMBERS
-- ========================================

INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM Member m, Association a
WHERE m.email = 'alice.durand@email.com' AND a.name = 'Run For Fun Association'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM Member m, Association a
WHERE m.email = 'thomas.dubois@email.com' AND a.name = 'Courir pour la Planète'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM Member m, Association a
WHERE m.email = 'antoine.lefebvre@email.com' AND a.name = 'Montagne & Running'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM Member m, Association a
WHERE m.email = 'elisevarin@email.com' AND a.name = 'Run In Lyon'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM Member m, Association a
WHERE m.email = 'nicolas.lambert@sport.fr' AND a.name = 'Sport & Solidarité'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM Member m, Association a
WHERE m.email = 'carmen.garcia@run.es' AND a.name = 'Runners du Sud'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM Member m, Association a
WHERE m.email = 'klaus.fischer@alsace.fr' AND a.name = 'Les Foulées Alsaciennes'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM Member m, Association a
WHERE m.email = 'lea.monet@outlook.com' AND a.name = 'Association Sportive Paris'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

-- ========================================
-- COURSES
-- ========================================

INSERT INTO Course (name, description, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Marathon de Paris', 'Un marathon mythique au cœur de Paris', m.id, '2025-10-10 17:00', '2025-10-10 18:00', 48.8566, 2.3522, 48.8606, 2.3376, 42.195, 'Champs Élysées', 'Paris', 75008, 5000, 1200, 50.0
FROM Member m
WHERE m.email = 'jean.dupont@email.com'
  AND NOT EXISTS (SELECT 1 FROM Course WHERE name = 'Marathon de Paris');

INSERT INTO Course (name, description, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Color Beer Run', 'Une course festive avec de la bière et de la couleur', m.id, '2025-08-20 16:00', '2025-08-20 19:00', 48.8566, 2.3522, 48.8606, 2.3376, 5.0, 'Parc des Buttes-Chaumont', 'Paris', 75019, 2000, 500, 20.0
FROM Member m
WHERE m.email = 'alice.durand@email.com'
  AND NOT EXISTS (SELECT 1 FROM Course WHERE name = 'Color Beer Run');

INSERT INTO Course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Course des Héros', 'Course caritative pour la bonne cause', a.id, m.id, '2025-08-15 13:00', '2025-08-15 15:30', 45.764, 4.8357, 45.7700, 4.8300, 5, 'Parc Blandant', 'Lyon', 69006, 3000, 800, 30.0
FROM Association a, Member m
WHERE a.name = 'Courir Ensemble' AND m.email = 'marie.rousseau@admin.com'
  AND NOT EXISTS (SELECT 1 FROM Course WHERE name = 'Course des Héros');

INSERT INTO Course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Trail des Alpes', 'Une course en montagne avec des paysages magnifiques', a.id, m.id, '2025-07-20 08:00', '2025-07-20 14:00', 45.9237, 6.8694, 45.9170, 6.8720, 25.0, 'Mont Blanc', 'Chamonix', 74400, 1500, 500, 40.0
FROM Association a, Member m
WHERE a.name = 'Courir Ensemble' AND m.email = 'marie.rousseau@admin.com'
  AND NOT EXISTS (SELECT 1 FROM Course WHERE name = 'Trail des Alpes');

INSERT INTO Course (name, description, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Course des Fleurs', 'Une course colorée et festive', m.id, '2025-06-05 10:00', '2025-06-05 12:00', 43.610769, 3.876716, 43.6150, 3.8800, 10.0, 'Jardin des Plantes', 'Montpellier', 34000, 200, 64, 25.0
FROM Member m
WHERE m.email = 'lea.monet@outlook.com'
  AND NOT EXISTS (SELECT 1 FROM Course WHERE name = 'Course des Fleurs');

INSERT INTO Course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Eco-Trail Nantais', 'Course écologique à travers les espaces verts de Nantes', a.id, m.id, '2025-09-15 09:00', '2025-09-15 12:00', 47.2184, -1.5536, 47.2250, -1.5421, 15.0, 'Parc de Procé', 'Nantes', 44000, 1000, 250, 35.0
FROM Association a, Member m
WHERE a.name = 'Courir pour la Planète' AND m.email = 'thomas.dubois@email.com'
  AND NOT EXISTS (SELECT 1 FROM Course WHERE name = 'Eco-Trail Nantais');

INSERT INTO Course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Trail des Sommets', 'Parcours technique en moyenne montagne', a.id, m.id, '2025-07-12 08:30', '2025-07-12 14:00', 45.1885, 5.7245, 45.2101, 5.7689, 22.5, 'Col de Porte', 'Grenoble', 38000, 500, 120, 45.0
FROM Association a, Member m
WHERE a.name = 'Montagne & Running' AND m.email = 'antoine.lefebvre@email.com'
  AND NOT EXISTS (SELECT 1 FROM Course WHERE name = 'Trail des Sommets');

INSERT INTO Course(name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Bocuse Color Run', 'Une course colorée et gourmande à Lyon', a.id, m.id, '2025-09-10 10:00', '2025-09-10 12:00', 45.764, 4.8357, 45.7700, 4.8300, 8.0, 'Parc de la Tête d''Or', 'Lyon', 69006, 1500, 300, 30.0
FROM Association a, Member m
WHERE a.name = 'Run In Lyon' AND m.email = 'elisevarin@email.com'
  AND NOT EXISTS (SELECT 1 FROM Course WHERE name = 'Bocuse Color Run');

INSERT INTO Course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Fun Run Bordeaux', 'Course ludique et colorée dans les vignobles', a.id, m.id, '2025-07-05 14:00', '2025-07-05 17:00', 44.8378, -0.5792, 44.8500, -0.5600, 12.0, 'Esplanade des Quinconces', 'Bordeaux', 33000, 800, 150, 28.0
FROM Association a, Member m
WHERE a.name = 'Run For Fun Association' AND m.email = 'alice.durand@email.com'
  AND NOT EXISTS (SELECT 1 FROM Course WHERE name = 'Fun Run Bordeaux');

INSERT INTO Course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Solidarité Trail Toulouse', 'Trail solidaire pour les associations locales', a.id, m.id, '2025-09-28 09:00', '2025-09-28 13:00', 43.604652, 1.444209, 43.6200, 1.4600, 18.0, 'Canal du Midi', 'Toulouse', 31000, 600, 180, 38.0
FROM Association a, Member m
WHERE a.name = 'Sport & Solidarité' AND m.email = 'nicolas.lambert@sport.fr'
  AND NOT EXISTS (SELECT 1 FROM Course WHERE name = 'Solidarité Trail Toulouse');

INSERT INTO Course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Color Run Méditerranée', 'Course colorée face à la mer', a.id, m.id, '2025-08-12 10:30', '2025-08-12 12:30', 43.6108, 3.8767, 43.6200, 3.8900, 6.5, 'Plage de Palavas', 'Montpellier', 34000, 1200, 340, 22.0
FROM Association a, Member m
WHERE a.name = 'Runners du Sud' AND m.email = 'carmen.garcia@run.es'
  AND NOT EXISTS (SELECT 1 FROM Course WHERE name = 'Color Run Méditerranée');

INSERT INTO Course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Foulées de Strasbourg', 'Course traditionnelle alsacienne', a.id, m.id, '2025-10-15 08:00', '2025-10-15 11:00', 48.5734, 7.7521, 48.5800, 7.7600, 21.0, 'Place Kléber', 'Strasbourg', 67000, 900, 250, 42.0
FROM Association a, Member m
WHERE a.name = 'Les Foulées Alsaciennes' AND m.email = 'klaus.fischer@alsace.fr'
  AND NOT EXISTS (SELECT 1 FROM Course WHERE name = 'Foulées de Strasbourg');

INSERT INTO Course (name, description, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Night Run Paris', 'Course nocturne illuminée dans Paris', m.id, '2025-11-02 19:00', '2025-11-02 21:30', 48.8566, 2.3522, 48.8606, 2.3376, 8.5, 'Tour Eiffel', 'Paris', 75007, 1500, 420, 32.0
FROM Member m
WHERE m.email = 'lea.monet@outlook.com'
  AND NOT EXISTS (SELECT 1 FROM Course WHERE name = 'Night Run Paris');

-- Courses passées pour avoir de l'historique
INSERT INTO Course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Spring Color Run 2025', 'Course colorée du printemps (passée)', a.id, m.id, '2025-04-15 10:00', '2025-04-15 12:00', 45.764, 4.8357, 45.7700, 4.8300, 7.5, 'Parc de Gerland', 'Lyon', 69007, 1000, 800, 25.0
FROM Association a, Member m
WHERE a.name = 'Run In Lyon' AND m.email = 'elisevarin@email.com'
  AND NOT EXISTS (SELECT 1 FROM Course WHERE name = 'Spring Color Run 2025');

INSERT INTO Course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Marathon Vert 2025', 'Marathon écologique (passé)', a.id, m.id, '2025-05-20 08:00', '2025-05-20 12:30', 47.2184, -1.5536, 47.2400, -1.5300, 42.195, 'Parc de la Beaujoire', 'Nantes', 44300, 2000, 1650, 48.0
FROM Association a, Member m
WHERE a.name = 'Courir pour la Planète' AND m.email = 'thomas.dubois@email.com'
  AND NOT EXISTS (SELECT 1 FROM Course WHERE name = 'Marathon Vert 2025');

-- ========================================
-- DEMANDES D'ORGANISATEUR
-- ========================================

INSERT INTO OrganizerRequest (memberId, requestType, motivation, status, requestDate, existingAssociationId)
SELECT m.id, 'BECOME_ORGANIZER', 'Je souhaite devenir organisateur pour promouvoir le sport dans ma région. J''ai de l''expérience en gestion d''événements et je pense pouvoir apporter une contribution positive à la communauté des coureurs.', 'PENDING', '2025-06-15 14:30:00', a.id
FROM Member m, Association a
WHERE m.email = 'sophie.martin@email.com' AND a.name = 'Run For Fun Association'
  AND NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = m.id);

INSERT INTO OrganizerRequest (memberId, motivation, existingAssociationId, newAssociationData, requestDate, status, requestType, existingAssociationName)
SELECT m.id, 'Je souhaite devenir organisateur de course, pour récolter des financements au profit de la préservation de la faune maritime.', NULL, '{"name":"Sea Shepherd","email":"seashepherd@contact.fr","description":"Protection de la faune maritime","websiteLink":"https://seashepherd.fr/","phone":"0145678901","address":"22 Rue Boulard","zipCode":"75014","city":"Paris"}', '2025-06-24 18:09:48.568678', 'PENDING', 'CREATE_ASSOCIATION', NULL
FROM Member m
WHERE m.email = 'theop@mail.com'
  AND NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = m.id);

INSERT INTO OrganizerRequest (memberId, requestType, motivation, status, requestDate, existingAssociationId)
SELECT m.id, 'BECOME_ORGANIZER', 'Passionné de running depuis 5 ans, je souhaite organiser des événements sportifs pour promouvoir l''activité physique chez les jeunes de ma ville. J''ai déjà participé à l''organisation de plusieurs événements associatifs.', 'PENDING', '2025-06-20 16:45:00', a.id
FROM Member m, Association a
WHERE m.email = 'maxime.duval@student.fr' AND a.name = 'Runners du Sud'
  AND NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = m.id);

INSERT INTO OrganizerRequest (memberId, requestType, motivation, status, requestDate, existingAssociationId)
SELECT m.id, 'BECOME_ORGANIZER', 'En tant que professionnelle du sport, je souhaite contribuer à l''organisation d''événements sportifs de qualité. Mon expertise en communication et marketing pourrait bénéficier à votre association.', 'PENDING', '2025-06-22 11:20:00', a.id
FROM Member m, Association a
WHERE m.email = 'sarah.chevalier@pro.fr' AND a.name = 'Association Sportive Paris'
  AND NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = m.id);

-- Demandes ACCEPTÉES
INSERT INTO OrganizerRequest (memberId, requestType, motivation, status, requestDate, processedDate, processedByAdminId, adminComment, existingAssociationId)
SELECT m.id, 'BECOME_ORGANIZER', 'Organisateur expérimenté souhaitant rejoindre votre association pour développer des événements trail en montagne.', 'ACCEPTED', '2025-05-10 10:00:00', '2025-05-15 16:30:00', admin.id, 'Profil parfait pour nos événements montagne. Accepté à l''unanimité.', a.id
FROM Member m, Member admin, Association a
WHERE m.email = 'antoine.lefebvre@email.com' AND admin.email = 'jean.dupont@email.com' AND a.name = 'Montagne & Running'
  AND NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = m.id);

-- ========================================
-- INSCRIPTIONS AUX COURSES
-- ========================================

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-03-01', 'ACCEPTED', 'sess_1234567890'
FROM Course c, Member m
WHERE c.name = 'Marathon de Paris' AND m.email = 'sophie.martin@email.com'
  AND NOT EXISTS (SELECT 1 FROM CourseMember cm WHERE cm.courseId = c.id AND cm.memberId = m.id);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-03-02', 'ACCEPTED', 'sess_2345678901'
FROM Course c, Member m
WHERE c.name = 'Marathon de Paris' AND m.email = 'paul.bernard@email.com'
  AND NOT EXISTS (SELECT 1 FROM CourseMember cm WHERE cm.courseId = c.id AND cm.memberId = m.id);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-03-03', 'ACCEPTED', 'sess_3456789012'
FROM Course c, Member m
WHERE c.name = 'Marathon de Paris' AND m.email = 'claire.lemoine@email.com'
  AND NOT EXISTS (SELECT 1 FROM CourseMember cm WHERE cm.courseId = c.id AND cm.memberId = m.id);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-04-01', 'ACCEPTED', 'sess_4567890123'
FROM Course c, Member m
WHERE c.name = 'Course des Héros' AND m.email = 'marc.petit@email.com'
  AND NOT EXISTS (SELECT 1 FROM CourseMember cm WHERE cm.courseId = c.id AND cm.memberId = m.id);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-04-02', 'ACCEPTED', 'sess_5678901234'
FROM Course c, Member m
WHERE c.name = 'Course des Héros' AND m.email = 'sophie.roux@email.com'
  AND NOT EXISTS (SELECT 1 FROM CourseMember cm WHERE cm.courseId = c.id AND cm.memberId = m.id);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-05-10', 'ACCEPTED', 'sess_8901234567'
FROM Course c, Member m
WHERE c.name = 'Bocuse Color Run' AND m.email = 'pierre.blanc@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM CourseMember cm WHERE cm.courseId = c.id AND cm.memberId = m.id);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-05-11', 'ACCEPTED', 'sess_9012345678'
FROM Course c, Member m
WHERE c.name = 'Bocuse Color Run' AND m.email = 'emma.noir@hotmail.fr'
  AND NOT EXISTS (SELECT 1 FROM CourseMember cm WHERE cm.courseId = c.id AND cm.memberId = m.id);

-- ========================================
-- DISCUSSIONS ET MESSAGES
-- ========================================

INSERT INTO Discussion (courseId, isActive)
SELECT c.id, TRUE
FROM Course c
WHERE NOT EXISTS (SELECT 1 FROM Discussion WHERE courseId = c.id);

INSERT INTO Message (discussionId, memberId, content, originalContent, date, isPin, isHidden)
SELECT d.id, m.id, 'Bienvenue sur la discussion du Marathon de Paris ! Qui est prêt pour cette aventure incroyable ?', 'Bienvenue sur la discussion du Marathon de Paris ! Qui est prêt pour cette aventure incroyable ?', '2025-03-01 10:00:00', TRUE, FALSE
FROM Discussion d
         JOIN Course c ON d.courseId = c.id
         JOIN Member m ON m.email = 'jean.dupont@email.com'
WHERE c.name = 'Marathon de Paris'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'Bienvenue sur la discussion du Marathon%');

INSERT INTO Message (discussionId, memberId, content, originalContent, date, isPin, isHidden)
SELECT d.id, m.id, 'Quelqu''un connaît le parcours exacte ? J''aimerais m''entraîner sur le trajet !', 'Quelqu''un connaît le parcours exacte ? J''aimerais m''entraîner sur le trajet !', '2025-03-02 14:30:00', FALSE, FALSE
FROM Discussion d
         JOIN Course c ON d.courseId = c.id
         JOIN Member m ON m.email = 'sophie.martin@email.com'
WHERE c.name = 'Marathon de Paris'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'Quelqu''un connaît le parcours%');

INSERT INTO Message (discussionId, memberId, content, originalContent, date, isPin, isHidden)
SELECT d.id, m.id, 'Le parcours est disponible sur le site officiel. Très beau parcours dans Paris !', 'Le parcours est disponible sur le site officiel. Très beau parcours dans Paris !', '2025-03-02 15:45:00', FALSE, FALSE
FROM Discussion d
         JOIN Course c ON d.courseId = c.id
         JOIN Member m ON m.email = 'paul.bernard@email.com'
WHERE c.name = 'Marathon de Paris'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'Le parcours est disponible%');

INSERT INTO Message (discussionId, memberId, content, originalContent, date, isPin)
SELECT d.id, m.id, 'Bienvenue sur l''Eco-Trail Nantais! N''oubliez pas votre gourde réutilisable et vos chaussures de trail.', 'Bienvenue sur l''Eco-Trail Nantais! N''oubliez pas votre gourde réutilisable et vos chaussures de trail.', '2025-04-20 09:00:00', TRUE
FROM Discussion d
         JOIN Course c ON d.courseId = c.id
         JOIN Member m ON m.email = 'thomas.dubois@email.com'
WHERE c.name = 'Eco-Trail Nantais'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'Bienvenue sur l''Eco-Trail%');

INSERT INTO Message (discussionId, memberId, content, originalContent, date, isPin, isHidden)
SELECT d.id, m.id, 'Préparez-vous pour une explosion de couleurs ! 🌈 Course familiale et festive garantie !', 'Préparez-vous pour une explosion de couleurs ! 🌈 Course familiale et festive garantie !', '2025-05-10 10:00:00', TRUE, FALSE
FROM Discussion d
         JOIN Course c ON d.courseId = c.id
         JOIN Member m ON m.email = 'elisevarin@email.com'
WHERE c.name = 'Bocuse Color Run'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'Préparez-vous pour une explosion%');

-- ========================================
-- PAIEMENTS
-- ========================================

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-03-02', 50.0
FROM CourseMember cm
         JOIN Course c ON cm.courseId = c.id
         JOIN Member m ON cm.memberId = m.id
WHERE c.name = 'Marathon de Paris' AND m.email = 'sophie.martin@email.com'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-03-03', 50.0
FROM CourseMember cm
         JOIN Course c ON cm.courseId = c.id
         JOIN Member m ON cm.memberId = m.id
WHERE c.name = 'Marathon de Paris' AND m.email = 'paul.bernard@email.com'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-04-02', 30.0
FROM CourseMember cm
         JOIN Course c ON cm.courseId = c.id
         JOIN Member m ON cm.memberId = m.id
WHERE c.name = 'Course des Héros' AND m.email = 'marc.petit@email.com'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-05-11', 30.0
FROM CourseMember cm
         JOIN Course c ON cm.courseId = c.id
         JOIN Member m ON cm.memberId = m.id
WHERE c.name = 'Bocuse Color Run' AND m.email = 'pierre.blanc@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

-- ========================================
-- MISE À JOUR DES COMPTEURS
-- ========================================

UPDATE Course
SET currentNumberOfRunners = (
    SELECT COUNT(*)
    FROM CourseMember cm
    WHERE cm.courseId = Course.id
      AND cm.registrationStatus = 'ACCEPTED'
    )
WHERE EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = Course.id
);

-- ========================================
-- RÉSUMÉ FINAL
-- ========================================

SELECT 'MEMBERS' as table_name, COUNT(*) as count FROM Member
UNION ALL
SELECT 'ASSOCIATIONS', COUNT(*) FROM Association
UNION ALL
SELECT 'COURSES', COUNT(*) FROM Course
UNION ALL
SELECT 'COURSEMEMBERS', COUNT(*) FROM CourseMember
UNION ALL
SELECT 'ASSOCIATIONMEMBERS', COUNT(*) FROM AssociationMember
UNION ALL
SELECT 'ORGANIZER_REQUESTS', COUNT(*) FROM OrganizerRequest
UNION ALL
SELECT 'DISCUSSIONS', COUNT(*) FROM Discussion
UNION ALL
SELECT 'MESSAGES', COUNT(*) FROM Message
UNION ALL
SELECT 'PAIEMENTS', COUNT(*) FROM Paiement;

-- ========================================
-- FIN DU SCRIPT
-- Script SQL complet et unifié pour Color Run
-- Toutes les améliorations incluses
-- ========================================