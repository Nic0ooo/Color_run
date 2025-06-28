-- Création des tables si elles n'existent pas

CREATE TABLE IF NOT EXISTS member (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      role VARCHAR(20) DEFAULT 'RUNNER',
                                      name VARCHAR(255),
                                      firstname VARCHAR(255),
                                      email VARCHAR(255) UNIQUE ,
                                      password VARCHAR(255),
                                      phoneNumber VARCHAR(15),
                                      address VARCHAR(255),
                                      city VARCHAR(255),
                                      zipCode INTEGER,
                                      positionLatitude DOUBLE,
                                      positionLongitude DOUBLE
);

CREATE TABLE IF NOT EXISTS association (
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

CREATE TABLE IF NOT EXISTS course (
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

CREATE TABLE IF NOT EXISTS CourseMember (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            courseId INTEGER,
                                            memberId INTEGER,
                                            registrationDate VARCHAR(255),
                                            registrationStatus VARCHAR(255),
                                            stripeSessionId VARCHAR(255) DEFAULT NULL,
                                            FOREIGN KEY (courseId) REFERENCES Course(id),
                                            FOREIGN KEY (memberId) REFERENCES Member(id),
                                            UNIQUE(courseId, memberId)
);

CREATE TABLE IF NOT EXISTS AssociationMember (
                                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                 memberId INTEGER,
                                                 associationId INTEGER,
                                                 joinDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                 FOREIGN KEY (associationId) REFERENCES Association(id),
                                                 FOREIGN KEY (memberId) REFERENCES Member(id),
                                                 UNIQUE(memberId, associationId)
);

CREATE TABLE OrganizerRequest (
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
                                  existingAssociationName VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS Discussion (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          courseId INTEGER,
                                          isActive BOOLEAN DEFAULT TRUE,
                                          FOREIGN KEY (courseId) REFERENCES Course(id),
                                            UNIQUE(courseId)
);

CREATE TABLE IF NOT EXISTS Message (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       discussionId INTEGER,
                                       memberId INTEGER,
                                       content VARCHAR(255),
                                       date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       isPin BOOLEAN DEFAULT FALSE,
                                       isHidden BOOLEAN DEFAULT FALSE,
                                       FOREIGN KEY (discussionId) REFERENCES Discussion(id),
                                       FOREIGN KEY (memberId) REFERENCES Member(id)
);

CREATE TABLE IF NOT EXISTS Paiement (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        courseMemberId INTEGER,
                                        date VARCHAR(32),
                                        amount DOUBLE,
                                        FOREIGN KEY (courseMemberId) REFERENCES CourseMember(id)
);

-- Insertion conditionnelle des données (si la table est vide)
-- ADMINISTRATEURS
INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ADMIN', 'Dupont', 'Jean', 'jean.dupont@email.com', 'password123', '0601020304', '12 rue de Paris', 'Paris', 75001, 48.8566, 2.3522
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'jean.dupont@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ADMIN', 'Rousseau', 'Marie', 'marie.rousseau@admin.com', 'admin2024', '0612345600', '45 avenue de la République', 'Lyon', 69002, 45.764, 4.8357
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'marie.rousseau@admin.com');

-- ORGANISATEURS CONFIRMÉS
INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Durand', 'Alice', 'alice.durand@email.com', 'password123', '0612345678', '45 rue des Lilas', 'Nantes', 44000, 47.218371, -1.553621
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'alice.durand@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Monet', 'Léa', 'lea.monet@outlook.com', 'password123', '0612425690', '16 Rue Perrière', 'Annecy', 74000, 45.89812149200697, 6.127460404462661
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'lea.monet@outlook.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Dubois', 'Thomas', 'thomas.dubois@email.com', 'secure456', '0689012345', '12 avenue de la République', 'Bordeaux', 33000, 44.8378, -0.5792
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'thomas.dubois@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Lefebvre', 'Antoine', 'antoine.lefebvre@email.com', 'antoine123', '0701234567', '23 boulevard Jean Jaurès', 'Grenoble', 38000, 45.1885, 5.7245
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'antoine.lefebvre@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Varin', 'Elise', 'elisevarin@email.com', 'elise123', '0781235537', '19 Pl. des Célestins','Lyon', 69002, 45.75989913978603, 4.831608799811371
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'elisevarin@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Lambert', 'Nicolas', 'nicolas.lambert@sport.fr', 'nico2024', '0756789012', '78 rue des Sports', 'Toulouse', 31000, 43.604652, 1.444209
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'nicolas.lambert@sport.fr');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Garcia', 'Carmen', 'carmen.garcia@run.es', 'carmen123', '0634567891', '12 place de l''Espagne', 'Perpignan', 66000, 42.6886, 2.8946
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'carmen.garcia@run.es');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Fischer', 'Klaus', 'klaus.fischer@alsace.fr', 'klaus2024', '0387654321', '45 rue de Strasbourg', 'Mulhouse', 68100, 47.7508, 7.3359
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'klaus.fischer@alsace.fr');

-- PARTICIPANTS RÉGULIERS
INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Martin', 'Sophie', 'sophie.martin@email.com', 'pass456', '0612345678', '25 avenue des Champs', 'Lyon', 69000, 45.764, 4.8357
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'sophie.martin@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Pollet', 'Theo', 'theop@mail.com', 'password123', '0765467809', '356 rue Victor Hugo', 'Dijon', 21000, 47.32136825551213, 5.041485596025622
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email =  'theop@mail.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Bernard', 'Paul', 'paul.bernard@email.com', 'password123', '0623456789', '12 avenue des Fleurs', 'Nice', 06000, 43.710173, 7.262003
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'paul.bernard@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Lemoine', 'Claire', 'claire.lemoine@email.com', 'password123', '0634567890', '78 boulevard des Roses', 'Lille', 59000, 50.62925, 3.057256
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'claire.lemoine@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Petit', 'Marc', 'marc.petit@email.com', 'password123', '0645678901', '23 rue des Champs', 'Strasbourg', 67000, 48.573405, 7.752111
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'marc.petit@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Roux', 'Sophie', 'sophie.roux@email.com', 'password123', '0656789012', '56 place des Arbres', 'Montpellier', 34000, 43.610769, 3.876716
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'sophie.roux@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Leroy', 'Camille', 'camille.leroy@email.com', 'password123', '0678901234', '45 rue des Oliviers', 'Marseille', 13008, 43.2715, 5.4018
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'camille.leroy@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Moreau', 'Julie', 'julie.moreau@email.com', 'julie2023', '0690123456', '8 place du Marché', 'Rennes', 35000, 48.1173, -1.6778
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'julie.moreau@email.com');

-- NOUVEAUX PARTICIPANTS (25 participants supplémentaires)
INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Blanc', 'Pierre', 'pierre.blanc@gmail.com', 'pierre123', '0612345601', '23 rue de la Paix', 'Paris', 75001, 48.8566, 2.3522
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'pierre.blanc@gmail.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Noir', 'Emma', 'emma.noir@hotmail.fr', 'emma2024', '0623456702', '67 avenue Victor Hugo', 'Lyon', 69003, 45.7640, 4.8400
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'emma.noir@hotmail.fr');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Vert', 'Lucas', 'lucas.vert@yahoo.fr', 'lucas456', '0634567803', '89 boulevard Gambetta', 'Marseille', 13001, 43.2965, 5.3698
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'lucas.vert@yahoo.fr');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Rouge', 'Manon', 'manon.rouge@orange.fr', 'manon789', '0645678904', '12 place Stanislas', 'Nancy', 54000, 48.6937, 6.1834
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'manon.rouge@orange.fr');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Bleu', 'Hugo', 'hugo.bleu@free.fr', 'hugo012', '0656789005', '34 rue Nationale', 'Tours', 37000, 47.3941, 0.6848
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'hugo.bleu@free.fr');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Jaune', 'Léa', 'lea.jaune@sfr.fr', 'lea345', '0667890106', '56 cours Lafayette', 'Lyon', 69003, 45.7640, 4.8400
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'lea.jaune@sfr.fr');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Rose', 'Nathan', 'nathan.rose@bbox.fr', 'nathan678', '0678901207', '78 rue de la République', 'Strasbourg', 67000, 48.5734, 7.7521
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'nathan.rose@bbox.fr');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Violet', 'Chloé', 'chloe.violet@live.fr', 'chloe901', '0689012308', '90 avenue Jean Médecin', 'Nice', 06000, 43.7102, 7.2620
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'chloe.violet@live.fr');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Orange', 'Tom', 'tom.orange@gmail.com', 'tom234', '0690123409', '45 place Bellecour', 'Lyon', 69002, 45.7578, 4.8320
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'tom.orange@gmail.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Marron', 'Inès', 'ines.marron@wanadoo.fr', 'ines567', '0701234510', '23 rue Sainte-Catherine', 'Bordeaux', 33000, 44.8378, -0.5792
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'ines.marron@wanadoo.fr');

-- CANDIDATS ORGANISATEURS (qui vont faire des demandes)
INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Duval', 'Maxime', 'maxime.duval@student.fr', 'maxime123', '0712345611', '15 rue des Étudiants', 'Montpellier', 34000, 43.6108, 3.8767
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'maxime.duval@student.fr');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Chevalier', 'Sarah', 'sarah.chevalier@pro.fr', 'sarah456', '0723456712', '89 boulevard des Belges', 'Rouen', 76000, 49.4431, 1.0993
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'sarah.chevalier@pro.fr');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Lecomte', 'Benjamin', 'benjamin.lecomte@sport.com', 'benjamin789', '0734567813', '56 rue du Sport', 'Clermont-Ferrand', 63000, 45.7797, 3.0863
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'benjamin.lecomte@sport.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Roussel', 'Amélie', 'amelie.roussel@asso.org', 'amelie012', '0745678914', '34 place de la Comédie', 'Montpellier', 34000, 43.6081, 3.8790
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'amelie.roussel@asso.org');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Girard', 'Julien', 'julien.girard@nature.fr', 'julien345', '0756789015', '78 chemin des Bois', 'Besançon', 25000, 47.2378, 6.0241
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'julien.girard@nature.fr');

-- Ajout de 15 participants supplémentaires pour avoir plus de variété
INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Silva', 'Diego', 'diego.silva@runner.es', 'diego123', '0767890116', '12 calle Mayor', 'Perpignan', 66000, 42.6886, 2.8946
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'diego.silva@runner.es');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Müller', 'Anna', 'anna.muller@lauf.de', 'anna456', '0778901217', '45 rue de l''Allemagne', 'Strasbourg', 67000, 48.5734, 7.7521
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'anna.muller@lauf.de');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Johnson', 'Emily', 'emily.johnson@run.uk', 'emily789', '0789012318', '23 English Street', 'Calais', 62100, 50.9581, 1.8514
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'emily.johnson@run.uk');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Rossi', 'Marco', 'marco.rossi@corsa.it', 'marco012', '0790123419', '67 via Italia', 'Nice', 06000, 43.7102, 7.2620
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'marco.rossi@corsa.it');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Van Der Berg', 'Lisa', 'lisa.vandenberg@loop.nl', 'lisa345', '0601234520', '89 Nederlands Plein', 'Lille', 59000, 50.6292, 3.0573
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'lisa.vandenberg@loop.nl');

-- ========================================
-- ASSOCIATIONS
-- ========================================

INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Association Sportive Paris', 'Promouvoir la course à pied en Île-de-France', 'https://www.assoparis.com', '/images/logo1.png', 'contact@assoparis.com', '0145789652', '10 place de la République', 'Paris', 75011
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'contact@assoparis.com');

INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Courir Ensemble', 'Organisation de courses caritatives', 'https://www.courirensemble.org', '/images/logo2.png', 'contact@courirensemble.org', '0187654321', '15 rue Lafayette', 'Marseille', 13001
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'contact@courirensemble.org');

INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Sport & Solidarité', 'Sport solidaire et courses caritatives', 'https://www.sportsolidarite.org', '/images/logo4.png', 'hello@sportsolidarite.org', '0156789012', '35 rue de la Paix', 'Toulouse', 31001
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'hello@sportsolidarite.org');

INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Run For Fun Association', 'Courses colorées et événements ludiques', 'https://www.runforfun.fr', '/images/logo5.png', 'contact@runforfun.fr', '0134567890', '18 place des Victoires', 'Bordeaux', 33001
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'contact@runforfun.fr');

INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Courir pour la Planète', 'Courses écologiques et sensibilisation environnementale', 'https://www.courirplanete.org', '/images/logo6.png', 'contact@courirplanete.org', '0712345678', '45 rue Verte', 'Nantes', 44000
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'contact@courirplanete.org');

INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Montagne & Running', 'Courses en montagne et trails', 'https://www.montagnerunning.com', '/images/logo7.png', 'info@montagnerunning.com', '0723456789', '18 chemin des Cimes', 'Grenoble', 38000
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'info@montagnerunning.com');

INSERT INTO association (name, description, websiteLink, email, phoneNumber, address, city, zipCode)
SELECT 'Run In Lyon', 'Association sportive de Lyon pour promouvoir le sport et la culture lyonnaise.', 'https://www.runinlyon.com/fr', 'info@runinlyon.fr', '0450709654', '9 Pl. des Célestins', 'Lyon', 69002
WHERE NOT EXISTS(SELECT 1 FROM association WHERE email = 'info@runinlyon.fr');

INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Runners du Sud', 'Association de coureurs du sud de la France', 'https://www.runnersdusud.fr', '/images/logo8.png', 'contact@runnersdusud.fr', '0467890123', '78 avenue de la Méditerranée', 'Montpellier', 34000
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'contact@runnersdusud.fr');

INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Les Foulées Alsaciennes', 'Promotion de la course à pied en Alsace', 'https://www.fouleesalsaciennes.fr', '/images/logo9.png', 'contact@fouleesalsaciennes.fr', '0388765432', '56 rue de la Krutenau', 'Strasbourg', 67000
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'contact@fouleesalsaciennes.fr');

INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Trail Aventure', 'Spécialiste des courses nature et trail', 'https://www.trailaventure.org', '/images/logo10.png', 'info@trailaventure.org', '0556789012', '23 chemin des Randonneurs', 'Bordeaux', 33000
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'info@trailaventure.org');

-- ========================================
-- ASSOCIATION MEMBERS (ORGANISATEURS DANS LEURS ASSOCIATIONS)
-- ========================================

-- Alice Durand dans Run For Fun Association
INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM member m, association a
WHERE m.email = 'alice.durand@email.com' AND a.name = 'Run For Fun Association'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

-- Thomas Dubois dans Courir pour la Planète
INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM member m, association a
WHERE m.email = 'thomas.dubois@email.com' AND a.name = 'Courir pour la Planète'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

-- Antoine Lefebvre dans Montagne & Running
INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM member m, association a
WHERE m.email = 'antoine.lefebvre@email.com' AND a.name = 'Montagne & Running'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

-- Elise Varin dans Run In Lyon
INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM member m, association a
WHERE m.email = 'elisevarin@email.com' AND a.name = 'Run In Lyon'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

-- Nicolas Lambert dans Sport & Solidarité
INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM member m, association a
WHERE m.email = 'nicolas.lambert@sport.fr' AND a.name = 'Sport & Solidarité'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

-- Carmen Garcia dans Runners du Sud
INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM member m, association a
WHERE m.email = 'carmen.garcia@run.es' AND a.name = 'Runners du Sud'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

-- Klaus Fischer dans Les Foulées Alsaciennes
INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM member m, association a
WHERE m.email = 'klaus.fischer@alsace.fr' AND a.name = 'Les Foulées Alsaciennes'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

-- Léa Monet dans Association Sportive Paris
INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM member m, association a
WHERE m.email = 'lea.monet@outlook.com' AND a.name = 'Association Sportive Paris'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

-- ========================================
-- COURSES (CRÉÉES PAR DES ORGANISATEURS OU ADMINS)
-- ========================================

-- Courses créées par des admins (sans association)
INSERT INTO course (name, description, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Marathon de Paris', 'Un marathon mythique au cœur de Paris', m.id, '2025-10-10 17:00', '2025-10-10 18:00', 48.8566, 2.3522, 48.8606, 2.3376, 42.195, 'Champs Élysées', 'Paris', 75008, 5000, 1200, 50.0
FROM member m
WHERE m.email = 'jean.dupont@email.com'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Marathon de Paris');

INSERT INTO course (name, description, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Color Beer Run', 'Une course festive avec de la bière et de la couleur', m.id, '2025-08-20 16:00', '2025-08-20 19:00', 48.8566, 2.3522, 48.8606, 2.3376, 5.0, 'Parc des Buttes-Chaumont', 'Paris', 75019, 2000, 500, 20.0
FROM member m
WHERE m.email = 'alice.durand@email.com'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Color Beer Run');

-- Courses avec associations
INSERT INTO course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Course des Héros', 'Course caritative pour la bonne cause', a.id, m.id, '2025-08-15 13:00', '2025-08-15 15:30', 45.764, 4.8357, 45.7700, 4.8300, 5, 'Parc Blandant', 'Lyon', 69006, 3000, 800, 30.0
FROM association a, member m
WHERE a.name = 'Courir Ensemble' AND m.email = 'marie.rousseau@admin.com'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Course des Héros');

INSERT INTO course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Trail des Alpes', 'Une course en montagne avec des paysages magnifiques', a.id, m.id, '2025-07-20 08:00', '2025-07-20 14:00', 45.9237, 6.8694, 45.9170, 6.8720, 25.0, 'Mont Blanc', 'Chamonix', 74400, 1500, 500, 40.0
FROM association a, member m
WHERE a.name = 'Courir Ensemble' AND m.email = 'marie.rousseau@admin.com'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Trail des Alpes');

INSERT INTO course (name, description, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Course des Fleurs', 'Une course colorée et festive', m.id, '2025-06-05 10:00', '2025-06-05 12:00', 43.610769, 3.876716, 43.6150, 3.8800, 10.0, 'Jardin des Plantes', 'Montpellier', 34000, 200, 64, 25.0
FROM member m
WHERE m.email = 'lea.monet@outlook.com'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Course des Fleurs');

INSERT INTO course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Eco-Trail Nantais', 'Course écologique à travers les espaces verts de Nantes', a.id, m.id, '2025-09-15 09:00', '2025-09-15 12:00', 47.2184, -1.5536, 47.2250, -1.5421, 15.0, 'Parc de Procé', 'Nantes', 44000, 1000, 250, 35.0
FROM association a, member m
WHERE a.name = 'Courir pour la Planète' AND m.email = 'thomas.dubois@email.com'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Eco-Trail Nantais');

INSERT INTO course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Trail des Sommets', 'Parcours technique en moyenne montagne', a.id, m.id, '2025-07-12 08:30', '2025-07-12 14:00', 45.1885, 5.7245, 45.2101, 5.7689, 22.5, 'Col de Porte', 'Grenoble', 38000, 500, 120, 45.0
FROM association a, member m
WHERE a.name = 'Montagne & Running' AND m.email = 'antoine.lefebvre@email.com'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Trail des Sommets');

INSERT INTO course(name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Bocuse Color Run', 'Une course colorée et gourmande à Lyon', a.id, m.id, '2025-09-10 10:00', '2025-09-10 12:00', 45.764, 4.8357, 45.7700, 4.8300, 8.0, 'Parc de la Tête d''Or', 'Lyon', 69006, 1500, 300, 30.0
FROM association a, member m
WHERE a.name = 'Run In Lyon' AND m.email = 'elisevarin@email.com'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Bocuse Color Run');

-- Nouvelles courses pour enrichir la base
INSERT INTO course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Fun Run Bordeaux', 'Course ludique et colorée dans les vignobles', a.id, m.id, '2025-07-05 14:00', '2025-07-05 17:00', 44.8378, -0.5792, 44.8500, -0.5600, 12.0, 'Esplanade des Quinconces', 'Bordeaux', 33000, 800, 150, 28.0
FROM association a, member m
WHERE a.name = 'Run For Fun Association' AND m.email = 'alice.durand@email.com'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Fun Run Bordeaux');

INSERT INTO course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Solidarité Trail Toulouse', 'Trail solidaire pour les associations locales', a.id, m.id, '2025-09-28 09:00', '2025-09-28 13:00', 43.604652, 1.444209, 43.6200, 1.4600, 18.0, 'Canal du Midi', 'Toulouse', 31000, 600, 180, 38.0
FROM association a, member m
WHERE a.name = 'Sport & Solidarité' AND m.email = 'nicolas.lambert@sport.fr'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Solidarité Trail Toulouse');

INSERT INTO course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Color Run Méditerranée', 'Course colorée face à la mer', a.id, m.id, '2025-08-12 10:30', '2025-08-12 12:30', 43.6108, 3.8767, 43.6200, 3.8900, 6.5, 'Plage de Palavas', 'Montpellier', 34000, 1200, 340, 22.0
FROM association a, member m
WHERE a.name = 'Runners du Sud' AND m.email = 'carmen.garcia@run.es'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Color Run Méditerranée');

INSERT INTO course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Foulées de Strasbourg', 'Course traditionnelle alsacienne', a.id, m.id, '2025-10-15 08:00', '2025-10-15 11:00', 48.5734, 7.7521, 48.5800, 7.7600, 21.0, 'Place Kléber', 'Strasbourg', 67000, 900, 250, 42.0
FROM association a, member m
WHERE a.name = 'Les Foulées Alsaciennes' AND m.email = 'klaus.fischer@alsace.fr'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Foulées de Strasbourg');

INSERT INTO course (name, description, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Night Run Paris', 'Course nocturne illuminée dans Paris', m.id, '2025-11-02 19:00', '2025-11-02 21:30', 48.8566, 2.3522, 48.8606, 2.3376, 8.5, 'Tour Eiffel', 'Paris', 75007, 1500, 420, 32.0
FROM member m
WHERE m.email = 'lea.monet@outlook.com'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Night Run Paris');

-- Courses passées pour avoir de l'historique
INSERT INTO course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Spring Color Run 2025', 'Course colorée du printemps (passée)', a.id, m.id, '2025-04-15 10:00', '2025-04-15 12:00', 45.764, 4.8357, 45.7700, 4.8300, 7.5, 'Parc de Gerland', 'Lyon', 69007, 1000, 800, 25.0
FROM association a, member m
WHERE a.name = 'Run In Lyon' AND m.email = 'elisevarin@email.com'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Spring Color Run 2025');

INSERT INTO course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Marathon Vert 2025', 'Marathon écologique (passé)', a.id, m.id, '2025-05-20 08:00', '2025-05-20 12:30', 47.2184, -1.5536, 47.2400, -1.5300, 42.195, 'Parc de la Beaujoire', 'Nantes', 44300, 2000, 1650, 48.0
FROM association a, member m
WHERE a.name = 'Courir pour la Planète' AND m.email = 'thomas.dubois@email.com'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Marathon Vert 2025');

-- ========================================
-- DEMANDES D'ORGANISATEUR (RÉALISTES)
-- ========================================

-- Demandes EN COURS (PENDING)
INSERT INTO OrganizerRequest (memberId, requestType, motivation, status, requestDate, existingAssociationId)
SELECT m.id, 'BECOME_ORGANIZER', 'Je souhaite devenir organisateur pour promouvoir le sport dans ma région. J''ai de l''expérience en gestion d''événements et je pense pouvoir apporter une contribution positive à la communauté des coureurs.', 'PENDING', '2025-06-15 14:30:00', a.id
FROM member m, association a
WHERE m.email = 'sophie.martin@email.com' AND a.name = 'Run For Fun Association'
  AND NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = m.id);

INSERT INTO OrganizerRequest (memberId, motivation, existingAssociationId, newAssociationData, requestDate, status, requestType, existingAssociationName)
SELECT m.id, 'Je souhaite devenir organisateur de course, pour récolter des financements au profit de la préservation de la faune maritime.', NULL, '{"name":"Sea Shepherd","email":"seashepherd@contact.fr","description":"Protection de la faune maritime","websiteLink":"https://seashepherd.fr/","phone":"0145678901","address":"22 Rue Boulard","zipCode":"75014","city":"Paris"}', '2025-06-24 18:09:48.568678', 'PENDING', 'CREATE_ASSOCIATION', NULL
FROM member m
WHERE m.email = 'theop@mail.com'
  AND NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = m.id);

INSERT INTO OrganizerRequest (memberId, requestType, motivation, status, requestDate, existingAssociationId)
SELECT m.id, 'BECOME_ORGANIZER', 'Passionné de running depuis 5 ans, je souhaite organiser des événements sportifs pour promouvoir l''activité physique chez les jeunes de ma ville. J''ai déjà participé à l''organisation de plusieurs événements associatifs.', 'PENDING', '2025-06-20 16:45:00', a.id
FROM member m, association a
WHERE m.email = 'maxime.duval@student.fr' AND a.name = 'Runners du Sud'
  AND NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = m.id);

INSERT INTO OrganizerRequest (memberId, requestType, motivation, status, requestDate, existingAssociationId)
SELECT m.id, 'BECOME_ORGANIZER', 'En tant que professionnelle du sport, je souhaite contribuer à l''organisation d''événements sportifs de qualité. Mon expertise en communication et marketing pourrait bénéficier à votre association.', 'PENDING', '2025-06-22 11:20:00', a.id
FROM member m, association a
WHERE m.email = 'sarah.chevalier@pro.fr' AND a.name = 'Association Sportive Paris'
  AND NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = m.id);

INSERT INTO OrganizerRequest (memberId, motivation, requestDate, status, requestType, newAssociationData)
SELECT m.id, 'Je veux créer une association dédiée aux courses en montagne et aux sports d''endurance. Mon objectif est de promouvoir la pratique sportive en milieu naturel tout en sensibilisant à la protection de l''environnement.', '2025-06-25 09:15:00', 'PENDING', 'CREATE_ASSOCIATION', '{"name":"Montagne Endurance","email":"contact@montagne-endurance.fr","description":"Sports d''endurance en montagne et protection environnementale","websiteLink":"https://www.montagne-endurance.fr","phone":"0476543210","address":"123 Route des Alpes","zipCode":"38000","city":"Grenoble"}'
FROM member m
WHERE m.email = 'benjamin.lecomte@sport.com'
  AND NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = m.id);

INSERT INTO OrganizerRequest (memberId, requestType, motivation, status, requestDate, existingAssociationId)
SELECT m.id, 'BECOME_ORGANIZER', 'Bénévole active dans plusieurs associations, je souhaite maintenant m''investir dans l''organisation de courses. Mon expérience en gestion de projets et ma passion pour le sport me motivent à franchir cette étape.', 'PENDING', '2025-06-26 14:00:00', a.id
FROM member m, association a
WHERE m.email = 'amelie.roussel@asso.org' AND a.name = 'Sport & Solidarité'
  AND NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = m.id);

-- Demandes ACCEPTÉES (avec mise à jour du rôle)
INSERT INTO OrganizerRequest (memberId, requestType, motivation, status, requestDate, processedDate, processedByAdminId, adminComment, existingAssociationId)
SELECT m.id, 'BECOME_ORGANIZER', 'Organisateur expérimenté souhaitant rejoindre votre association pour développer des événements trail en montagne.', 'ACCEPTED', '2025-05-10 10:00:00', '2025-05-15 16:30:00', admin.id, 'Profil parfait pour nos événements montagne. Accepté à l''unanimité.', a.id
FROM member m, member admin, association a
WHERE m.email = 'antoine.lefebvre@email.com' AND admin.email = 'jean.dupont@email.com' AND a.name = 'Montagne & Running'
  AND NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = m.id);

INSERT INTO OrganizerRequest (memberId, requestType, motivation, status, requestDate, processedDate, processedByAdminId, adminComment, existingAssociationId)
SELECT m.id, 'BECOME_ORGANIZER', 'Passionnée de courses colorées, je veux organiser des événements fun et festifs pour toute la famille.', 'ACCEPTED', '2025-05-08 14:20:00', '2025-05-12 11:45:00', admin.id, 'Excellente motivation et expérience pertinente. Bienvenue dans l''équipe !', a.id
FROM member m, member admin, association a
WHERE m.email = 'alice.durand@email.com' AND admin.email = 'marie.rousseau@admin.com' AND a.name = 'Run For Fun Association'
  AND NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = m.id);

INSERT INTO OrganizerRequest (memberId, requestType, motivation, status, requestDate, processedDate, processedByAdminId, adminComment, existingAssociationId)
SELECT m.id, 'BECOME_ORGANIZER', 'Engagé pour l''écologie, je souhaite organiser des courses respectueuses de l''environnement.', 'ACCEPTED', '2025-04-25 09:30:00', '2025-04-30 15:20:00', admin.id, 'Valeurs parfaitement alignées avec notre mission. Accepté avec enthousiasme.', a.id
FROM member m, member admin, association a
WHERE m.email = 'thomas.dubois@email.com' AND admin.email = 'jean.dupont@email.com' AND a.name = 'Courir pour la Planète'
  AND NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = m.id);

-- Demandes REFUSÉES
INSERT INTO OrganizerRequest (memberId, requestType, motivation, status, requestDate, processedDate, processedByAdminId, adminComment, existingAssociationId)
SELECT m.id, 'BECOME_ORGANIZER', 'Je veux organiser des courses pour gagner de l''argent rapidement.', 'REJECTED', '2025-06-01 13:15:00', '2025-06-03 10:30:00', admin.id, 'Motivation commerciale non alignée avec nos valeurs associatives. Demande refusée.', a.id
FROM member m, member admin, association a
WHERE m.email = 'julien.girard@nature.fr' AND admin.email = 'marie.rousseau@admin.com' AND a.name = 'Courir Ensemble'
  AND NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = m.id);

INSERT INTO OrganizerRequest (memberId, motivation, requestDate, status, processedDate, processedByAdminId, adminComment, requestType, newAssociationData)
SELECT m.id, 'Je veux créer une association pour concurrencer toutes les autres et dominer le marché des courses.', '2025-05-28 16:00:00', 'REJECTED', '2025-05-30 09:45:00', admin.id, 'Esprit non collaboratif et motivations commerciales inadéquates. Refusé.', 'CREATE_ASSOCIATION', '{"name":"Running Empire","email":"empire@run.com","description":"Domination du marché des courses","websiteLink":"","phone":"","address":"","zipCode":"","city":""}'
FROM member m, member admin
WHERE m.email = 'diego.silva@runner.es' AND admin.email = 'jean.dupont@email.com'
  AND NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = m.id);

-- ========================================
-- COURSE MEMBERS (INSCRIPTIONS)
-- ========================================

-- Inscriptions confirmées (ACCEPTED)
INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-03-01', 'ACCEPTED', 'sess_1234567890'
FROM course c, member m
WHERE c.name = 'Marathon de Paris' AND m.email = 'sophie.martin@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-03-02', 'ACCEPTED', 'sess_2345678901'
FROM course c, member m
WHERE c.name = 'Marathon de Paris' AND m.email = 'paul.bernard@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-03-03', 'ACCEPTED', 'sess_3456789012'
FROM course c, member m
WHERE c.name = 'Marathon de Paris' AND m.email = 'claire.lemoine@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-04-01', 'ACCEPTED', 'sess_4567890123'
FROM course c, member m
WHERE c.name = 'Course des Héros' AND m.email = 'marc.petit@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-04-02', 'ACCEPTED', 'sess_5678901234'
FROM course c, member m
WHERE c.name = 'Course des Héros' AND m.email = 'sophie.roux@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-04-15', 'ACCEPTED', 'sess_6789012345'
FROM course c, member m
WHERE c.name = 'Eco-Trail Nantais' AND m.email = 'julie.moreau@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-05-02', 'ACCEPTED', 'sess_7890123456'
FROM course c, member m
WHERE c.name = 'Trail des Sommets' AND m.email = 'camille.leroy@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

-- Ajout de nombreuses inscriptions pour différentes courses
INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-05-10', 'ACCEPTED', 'sess_8901234567'
FROM course c, member m
WHERE c.name = 'Bocuse Color Run' AND m.email = 'pierre.blanc@gmail.com'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-05-11', 'ACCEPTED', 'sess_9012345678'
FROM course c, member m
WHERE c.name = 'Bocuse Color Run' AND m.email = 'emma.noir@hotmail.fr'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-05-12', 'ACCEPTED', 'sess_0123456789'
FROM course c, member m
WHERE c.name = 'Color Beer Run' AND m.email = 'lucas.vert@yahoo.fr'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

-- Inscriptions en attente (PENDING)
INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus)
SELECT c.id, m.id, '2025-06-20', 'PENDING'
FROM course c, member m
WHERE c.name = 'Fun Run Bordeaux' AND m.email = 'manon.rouge@orange.fr'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus)
SELECT c.id, m.id, '2025-06-21', 'PENDING'
FROM course c, member m
WHERE c.name = 'Solidarité Trail Toulouse' AND m.email = 'hugo.bleu@free.fr'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus)
SELECT c.id, m.id, '2025-06-22', 'PENDING'
FROM course c, member m
WHERE c.name = 'Color Run Méditerranée' AND m.email = 'lea.jaune@sfr.fr'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

-- Ajout de nombreuses inscriptions acceptées pour alimenter la base
INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-05-15', 'ACCEPTED', 'sess_1111111111'
FROM course c, member m
WHERE c.name = 'Course des Fleurs' AND m.email = 'nathan.rose@bbox.fr'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-05-16', 'ACCEPTED', 'sess_2222222222'
FROM course c, member m
WHERE c.name = 'Course des Fleurs' AND m.email = 'chloe.violet@live.fr'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-05-17', 'ACCEPTED', 'sess_3333333333'
FROM course c, member m
WHERE c.name = 'Course des Fleurs' AND m.email = 'tom.orange@gmail.com'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

-- Inscriptions pour les courses passées
INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-04-01', 'ACCEPTED', 'sess_4444444444'
FROM course c, member m
WHERE c.name = 'Spring Color Run 2025' AND m.email = 'ines.marron@wanadoo.fr'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-04-02', 'ACCEPTED', 'sess_5555555555'
FROM course c, member m
WHERE c.name = 'Spring Color Run 2025' AND m.email = 'diego.silva@runner.es'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-05-01', 'ACCEPTED', 'sess_6666666666'
FROM course c, member m
WHERE c.name = 'Marathon Vert 2025' AND m.email = 'anna.muller@lauf.de'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-05-02', 'ACCEPTED', 'sess_7777777777'
FROM course c, member m
WHERE c.name = 'Marathon Vert 2025' AND m.email = 'emily.johnson@run.uk'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = c.id AND cm.memberId = m.id
);

-- ========================================
-- DISCUSSIONS ET MESSAGES
-- ========================================

-- Discussions pour chaque course
INSERT INTO Discussion (courseId, isActive)
SELECT c.id, TRUE
FROM course c
WHERE NOT EXISTS (SELECT 1 FROM Discussion WHERE courseId = c.id);

-- Messages dans les discussions
INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
SELECT d.id, m.id, 'Bienvenue sur la discussion du Marathon de Paris ! Qui est prêt pour cette aventure incroyable ?', '2025-03-01 10:00:00', TRUE, FALSE
FROM Discussion d
         JOIN course c ON d.courseId = c.id
         JOIN member m ON m.email = 'jean.dupont@email.com'
WHERE c.name = 'Marathon de Paris'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'Bienvenue sur la discussion du Marathon%');

INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
SELECT d.id, m.id, 'Quelqu''un connaît le parcours exacte ? J''aimerais m''entraîner sur le trajet !', '2025-03-02 14:30:00', FALSE, FALSE
FROM Discussion d
         JOIN course c ON d.courseId = c.id
         JOIN member m ON m.email = 'sophie.martin@email.com'
WHERE c.name = 'Marathon de Paris'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'Quelqu''un connaît le parcours%');

INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
SELECT d.id, m.id, 'Le parcours est disponible sur le site officiel. Très beau parcours dans Paris !', '2025-03-02 15:45:00', FALSE, FALSE
FROM Discussion d
         JOIN course c ON d.courseId = c.id
         JOIN member m ON m.email = 'paul.bernard@email.com'
WHERE c.name = 'Marathon de Paris'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'Le parcours est disponible%');

INSERT INTO Message (discussionId, memberId, content, date, isPin)
SELECT d.id, m.id, 'Bienvenue sur l''Eco-Trail Nantais! N''oubliez pas votre gourde réutilisable et vos chaussures de trail.', '2025-04-20 09:00:00', TRUE
FROM Discussion d
         JOIN course c ON d.courseId = c.id
         JOIN member m ON m.email = 'thomas.dubois@email.com'
WHERE c.name = 'Eco-Trail Nantais'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'Bienvenue sur l''Eco-Trail%');

INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
SELECT d.id, m.id, 'Super initiative ! Y aura-t-il des points de ravitaillement zéro déchet ?', '2025-04-21 16:20:00', FALSE, FALSE
FROM Discussion d
         JOIN course c ON d.courseId = c.id
         JOIN member m ON m.email = 'julie.moreau@email.com'
WHERE c.name = 'Eco-Trail Nantais'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'Super initiative%');

INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
SELECT d.id, m.id, 'Absolument ! Gobelets consignés et fruits locaux uniquement. 🌱', '2025-04-21 17:15:00', FALSE, FALSE
FROM Discussion d
         JOIN course c ON d.courseId = c.id
         JOIN member m ON m.email = 'thomas.dubois@email.com'
WHERE c.name = 'Eco-Trail Nantais'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'Absolument%');

INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
SELECT d.id, m.id, 'Qui participe à la Course des Héros ? On peut faire une équipe !', '2025-04-05 11:30:00', FALSE, FALSE
FROM Discussion d
         JOIN course c ON d.courseId = c.id
         JOIN member m ON m.email = 'marc.petit@email.com'
WHERE c.name = 'Course des Héros'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'Qui participe à la Course%');

INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
SELECT d.id, m.id, 'Moi je suis partante ! Pour quelle association on court ?', '2025-04-05 12:45:00', FALSE, FALSE
FROM Discussion d
         JOIN course c ON d.courseId = c.id
         JOIN member m ON m.email = 'sophie.roux@email.com'
WHERE c.name = 'Course des Héros'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'Moi je suis partante%');

INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
SELECT d.id, m.id, 'Préparez-vous pour une explosion de couleurs ! 🌈 Course familiale et festive garantie !', '2025-05-10 10:00:00', TRUE, FALSE
FROM Discussion d
         JOIN course c ON d.courseId = c.id
         JOIN member m ON m.email = 'elisevarin@email.com'
WHERE c.name = 'Bocuse Color Run'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'Préparez-vous pour une explosion%');

INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
SELECT d.id, m.id, 'Les enfants peuvent participer à partir de quel âge ?', '2025-05-11 14:20:00', FALSE, FALSE
FROM Discussion d
         JOIN course c ON d.courseId = c.id
         JOIN member m ON m.email = 'pierre.blanc@gmail.com'
WHERE c.name = 'Bocuse Color Run'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'Les enfants peuvent%');

INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
SELECT d.id, m.id, 'À partir de 6 ans accompagnés, 12 ans seuls. Parcours adapté pour tous !', '2025-05-11 15:30:00', FALSE, FALSE
FROM Discussion d
         JOIN course c ON d.courseId = c.id
         JOIN member m ON m.email = 'elisevarin@email.com'
WHERE c.name = 'Bocuse Color Run'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'À partir de 6 ans%');

-- ========================================
-- PAIEMENTS
-- ========================================

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-03-02', 50.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Marathon de Paris' AND m.email = 'sophie.martin@email.com'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-03-03', 50.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Marathon de Paris' AND m.email = 'paul.bernard@email.com'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-03-04', 50.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Marathon de Paris' AND m.email = 'claire.lemoine@email.com'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-04-02', 30.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Course des Héros' AND m.email = 'marc.petit@email.com'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-04-03', 30.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Course des Héros' AND m.email = 'sophie.roux@email.com'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-04-16', 35.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Eco-Trail Nantais' AND m.email = 'julie.moreau@email.com'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-05-03', 45.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Trail des Sommets' AND m.email = 'camille.leroy@email.com'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-05-11', 30.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Bocuse Color Run' AND m.email = 'pierre.blanc@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-05-12', 30.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Bocuse Color Run' AND m.email = 'emma.noir@hotmail.fr'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-05-13', 20.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Color Beer Run' AND m.email = 'lucas.vert@yahoo.fr'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-05-16', 25.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Course des Fleurs' AND m.email = 'nathan.rose@bbox.fr'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-05-17', 25.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Course des Fleurs' AND m.email = 'chloe.violet@live.fr'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-05-18', 25.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Course des Fleurs' AND m.email = 'tom.orange@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

-- Paiements pour les courses passées
INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-04-02', 25.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Spring Color Run 2025' AND m.email = 'ines.marron@wanadoo.fr'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-04-03', 25.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Spring Color Run 2025' AND m.email = 'diego.silva@runner.es'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-05-03', 48.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Marathon Vert 2025' AND m.email = 'anna.muller@lauf.de'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-05-04', 48.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Marathon Vert 2025' AND m.email = 'emily.johnson@run.uk'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);

-- ========================================
-- MISE À JOUR DES COMPTEURS DE PARTICIPANTS
-- ========================================

-- Mise à jour du nombre actuel de participants pour chaque course
UPDATE course
SET currentNumberOfRunners = (
    SELECT COUNT(*)
    FROM CourseMember cm
    WHERE cm.courseId = course.id
      AND cm.registrationStatus = 'ACCEPTED'
    )
WHERE EXISTS (
    SELECT 1 FROM CourseMember cm
    WHERE cm.courseId = course.id
);