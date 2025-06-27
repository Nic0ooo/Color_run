-- Création des tables si elles n'existent pas

CREATE TABLE IF NOT EXISTS member (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      role VARCHAR(20) DEFAULT 'RUNNER',
                                      name VARCHAR(255),
                                      firstname VARCHAR(255),
                                      email VARCHAR(255),
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
                                           email VARCHAR(255),
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
                                            FOREIGN KEY (memberId) REFERENCES Member(id)
);

CREATE TABLE IF NOT EXISTS AssociationMember (
                                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                 memberId INTEGER,
                                                 associationId INTEGER,
                                                 joinDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                 FOREIGN KEY (associationId) REFERENCES Association(id),
                                                 FOREIGN KEY (memberId) REFERENCES Member(id)
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
                                          FOREIGN KEY (courseId) REFERENCES Course(id)
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

-- Membres
INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ADMIN', 'Dupont', 'Jean', 'jean.dupont@email.com', 'password123', '0601020304', '12 rue de Paris', 'Paris', 75001, 48.8566, 2.3522
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'jean.dupont@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Martin', 'Sophie', 'sophie.martin@email.com', 'pass456', '0612345678', '25 avenue des Champs', 'Lyon', 69000, 45.764, 4.8357
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'sophie.martin@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Pollet', 'Theo', 'theop@mail.com', 'password123', '0765467809', '356 rue Victor Hugo', 'Dijon', 21000, 47.32136825551213, 5.041485596025622
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email =  'theop@mail.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Durand', 'Alice', 'alice.durand@email.com', 'password123', '0612345678', '45 rue des Lilas', 'Nantes', 44000, 47.218371, -1.553621
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'alice.durand@email.com');

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
SELECT 'ORGANIZER', 'Monet', 'Léa', 'lea.monet@outlook.com', 'password123', '0612425690', '16 Rue Perrière', 'Annecy', 74000, 45.89812149200697, 6.127460404462661
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'lea.monet@outlook.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Leroy', 'Camille', 'camille.leroy@email.com', 'password123', '0678901234', '45 rue des Oliviers', 'Marseille', 13008, 43.2715, 5.4018
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'camille.leroy@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Dubois', 'Thomas', 'thomas.dubois@email.com', 'secure456', '0689012345', '12 avenue de la République', 'Bordeaux', 33000, 44.8378, -0.5792
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'thomas.dubois@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'RUNNER', 'Moreau', 'Julie', 'julie.moreau@email.com', 'julie2023', '0690123456', '8 place du Marché', 'Rennes', 35000, 48.1173, -1.6778
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'julie.moreau@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Lefebvre', 'Antoine', 'antoine.lefebvre@email.com', 'antoine123', '0701234567', '23 boulevard Jean Jaurès', 'Grenoble', 38000, 45.1885, 5.7245
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'antoine.lefebvre@email.com');

INSERT INTO member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
SELECT 'ORGANIZER', 'Varin', 'Elise', 'elisevarin@email.com', 'elise123', '0781235537', '19 Pl. des Célestins','Lyon', '69002', 45.75989913978603, 4.831608799811371
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email = 'elisevarin@email.com');

-- Associations
INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Association Sportive Paris', 'Promouvoir la course à pied en Île-de-France', 'www.assoparis.com', '/images/logo1.png', 'contact@assoparis.com', '0145789652', '10 place de la République', 'Paris', 75011
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'contact@assoparis.com');

INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Courir Ensemble', 'Organisation de courses caritatives', 'www.courirensemble.org', '/images/logo2.png', 'contact@courirensemble.org', '0187654321', '15 rue Lafayette', 'Marseille', 13001
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'contact@courirensemble.org');

INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Sport & Solidarité', 'Sport solidaire et courses caritatives', 'www.sportsolidarite.org', '/images/logo4.png', 'hello@sportsolidarite.org', '0156789012', '35 rue de la Paix', 'Toulouse', 31001
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'hello@sportsolidarite.org');

INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Run For Fun Association', 'Courses colorées et événements ludiques', 'www.runforfun.fr', '/images/logo5.png', 'contact@runforfun.fr', '0134567890', '18 place des Victoires', 'Bordeaux', 33001
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'contact@runforfun.fr');

INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Courir pour la Planète', 'Courses écologiques et sensibilisation environnementale', 'www.courirplanete.org', '/images/logo6.png', 'contact@courirplanete.org', '0712345678', '45 rue Verte', 'Nantes', 44000
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'contact@courirplanete.org');

INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Montagne & Running', 'Courses en montagne et trails', 'www.montagnerunning.com', '/images/logo7.png', 'info@montagnerunning.com', '0723456789', '18 chemin des Cimes', 'Grenoble', 38000
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'info@montagnerunning.com');

INSERT INTO association (name, description, websiteLink, email, phoneNumber, address, city, zipCode)
SELECT 'Run In Lyon', 'Association sportive de Lyon pour promouvoir le sport et la culture lyonnaise.', 'https://www.runinlyon.com/fr', 'info@runinlyon.fr', '0450709654', '9 Pl. des Célestins', 'Lyon', '69002'
WHERE NOT EXISTS(SELECT 1 FROM association WHERE email = 'info@runinlyon.fr');

-- Courses
INSERT INTO course (name, description, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Marathon de Paris', 'Un marathon mythique au cœur de Paris', 1, '2025-10-10 17:00', '2025-10-10 18:00', 48.8566, 2.3522, 48.8606, 2.3376, 42.195, 'Champs Élysées', 'Paris', 75008, 5000, 1200, 50.0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE name = 'Marathon de Paris');

INSERT INTO course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Course des Héros', 'Course caritative pour la bonne cause', 2, 4, '2025-08-15 13:00', '2025-08-15 15:30', 45.764, 4.8357, 45.7700, 4.8300, 5, 'Parc Blandant', 'Lyon', 69006, 3000, 800, 30.0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE name = 'Course des Héros');

INSERT INTO course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Trail des Alpes', 'Une course en montagne avec des paysages magnifiques', 2, 4, '2025-07-20 08:00', '2025-07-20 14:00', 45.9237, 6.8694, 45.9170, 6.8720, 25.0, 'Mont Blanc', 'Chamonix', 74400, 1500, 500, 40.0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE name = 'Trail des Alpes');

INSERT INTO course (name, description, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Course des Fleurs', 'Une course colorée et festive', 9, '2025-06-05 10:00', '2025-06-05 12:00', 43.610769, 3.876716, 43.6150, 3.8800, 10.0, 'Jardin des Plantes', 'Montpellier', 34000, 200, 64, 25.0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE name = 'Course des Fleurs');

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

INSERT INTO course(name, description, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Color Beer Run', 'Une course festive avec de la bière et de la couleur', m.id, '2025-08-20 16:00', '2025-08-20 19:00', 48.8566, 2.3522, 48.8606, 2.3376, 5.0, 'Parc des Buttes-Chaumont', 'Paris', 75019, 2000, 500, 20.0
FROM member m
WHERE m.email = 'alice.durand@email.com'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Color Beer Run');

INSERT INTO course(name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Bocuse Color Run', 'Une course colorée et gourmande à Lyon', a.id, m.id, '2025-09-10 10:00', '2025-09-10 12:00', 45.764, 4.8357, 45.7700, 4.8300, 8.0, 'Parc de la Tête d''Or', 'Lyon', 69006, 1500, 300, 30.0
FROM association a, member m
WHERE a.name = 'Run In Lyon' AND m.email = 'elisevarin@email.com'
  AND NOT EXISTS (SELECT 1 FROM course WHERE name = 'Bocuse Color Run');


-- CourseMember
INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-03-01', 'ACCEPTED', '13325432'
FROM course c, member m
WHERE c.name = 'Marathon de Paris' AND m.email = 'sophie.martin@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
                      JOIN course c2 ON cm.courseId = c2.id
                      JOIN member m2 ON cm.memberId = m2.id
    WHERE c2.name = 'Marathon de Paris' AND m2.email = 'sophie.martin@email.com'
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus)
SELECT c.id, m.id, '2025-03-05', 'PENDING'
FROM course c, Member m
WHERE c.name = 'Course des Héros' AND m.email = 'paul.bernard@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
                      JOIN course c2 ON cm.courseId = c2.id
                      JOIN member m2 ON cm.memberId = m2.id
    WHERE c2.name = 'Course des Héros' AND m.email = 'paul.bernard@email.com'
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus)
SELECT c.id, m.id, '2025-04-15', 'ACCEPTED'
FROM course c, member m
WHERE c.name = 'Eco-Trail Nantais' AND m.email = 'julie.moreau@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
                      JOIN course c2 ON cm.courseId = c2.id
                      JOIN member m2 ON cm.memberId = m2.id
    WHERE c2.name = 'Eco-Trail Nantais' AND m2.email = 'julie.moreau@email.com'
);

INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus, stripeSessionId)
SELECT c.id, m.id, '2025-05-02', 'ACCEPTED', 'sess_23456789'
FROM course c, member m
WHERE c.name = 'Trail des Sommets' AND m.email = 'camille.leroy@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
                      JOIN course c2 ON cm.courseId = c2.id
                      JOIN member m2 ON cm.memberId = m2.id
    WHERE c2.name = 'Trail des Sommets' AND m2.email = 'camille.leroy@email.com'
);

-- AssociationMember
INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM member m, association a
WHERE m.email = 'thomas.dubois@email.com' AND a.name = 'Courir pour la Planète'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);

INSERT INTO AssociationMember (memberId, associationId)
SELECT m.id, a.id
FROM member m, association a
WHERE m.email = 'antoine.lefebvre@email.com' AND a.name = 'Montagne & Running'
  AND NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = m.id AND associationId = a.id);


-- Demandes d'organisateur
INSERT INTO OrganizerRequest (memberId, requestType, motivation, status, requestDate, existingAssociationId)
SELECT 2, 'BECOME_ORGANIZER', 'Je souhaite devenir organisateur pour promouvoir le sport dans ma région. J''ai de l''expérience en gestion d''événements et je pense pouvoir apporter une contribution positive à la communauté des coureurs.', 'PENDING', '2025-06-15 14:30:00', 4
WHERE NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = 2);

INSERT INTO OrganizerRequest (memberId, motivation, existingAssociationId, newAssociationData, requestDate, status, requestType, existingAssociationName)
SELECT 3, 'Je souhaite devenir organisateur de course, pour récolter des financement au profit de la préservation de la faune maritime.', NULL, '{"name":"Sea Sheperd","email":"seasheperd@contact.fr","description":"Protection de la faune maritime","websiteLink":"https://seashepherd.fr/","phone":"","address":"22 Rue Boulard","zipCode":"75014","city":"Paris"}', '2025-06-24 18:09:48.568678', 'PENDING', 'BECOME_ORGANIZER', NULL
WHERE NOT EXISTS (SELECT 1 FROM OrganizerRequest WHERE memberId = 3);


-- Discussions
INSERT INTO Discussion (courseId, isActive)
SELECT 1, TRUE
WHERE NOT EXISTS (SELECT 1 FROM Discussion WHERE courseId = 1);

INSERT INTO Discussion (courseId, isActive)
SELECT 2, TRUE
WHERE NOT EXISTS (SELECT 1 FROM Discussion WHERE courseId = 2);

INSERT INTO Discussion (courseId, isActive)
SELECT id, TRUE
FROM course
WHERE name = 'Eco-Trail Nantais'
  AND NOT EXISTS (SELECT 1 FROM Discussion WHERE courseId = id);

INSERT INTO Discussion (courseId, isActive)
SELECT id, TRUE
FROM course
WHERE name = 'Trail des Sommets'
  AND NOT EXISTS (SELECT 1 FROM Discussion WHERE courseId = id);

-- Messages
INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
SELECT 1, 1, 'Bienvenue sur la discussion du Marathon de Paris !', '2025-03-01', FALSE, FALSE
WHERE NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = 1);

INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
SELECT 2, 5, 'Qui participe à la Course des Héros ?', '2025-03-02', FALSE, FALSE
WHERE NOT EXISTS (SELECT 2 FROM Message WHERE discussionId = 5);

INSERT INTO Message (discussionId, memberId, content, date, isPin)
SELECT d.id, m.id, 'Bienvenue sur l''Eco-Trail Nantais! N''oubliez pas votre gourde réutilisable.', '2025-04-20', TRUE
FROM Discussion d
         JOIN course c ON d.courseId = c.id
         JOIN member m ON m.email = 'thomas.dubois@email.com'
WHERE c.name = 'Eco-Trail Nantais'
  AND NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = d.id AND content LIKE 'Bienvenue sur l''Eco-Trail%');

-- Paiements
INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-03-02', 50.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Marathon de Paris' AND m.email = 'sophie.martin@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM Paiement p
    WHERE p.courseMemberId = cm.id
);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-03-06', 30.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Course des Héros' AND m.email = 'paul.bernard@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM Paiement p
    WHERE p.courseMemberId = cm.id
);

INSERT INTO Paiement (courseMemberId, date, amount)
SELECT cm.id, '2025-05-02', 35.0
FROM CourseMember cm
         JOIN course c ON cm.courseId = c.id
         JOIN member m ON cm.memberId = m.id
WHERE c.name = 'Eco-Trail Nantais' AND m.email = 'julie.moreau@email.com'
  AND NOT EXISTS (SELECT 1 FROM Paiement WHERE courseMemberId = cm.id);