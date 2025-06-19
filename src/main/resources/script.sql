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
                                                 FOREIGN KEY (associationId) REFERENCES Association(id),
                                                 FOREIGN KEY (memberId) REFERENCES Member(id)
);

CREATE TABLE IF NOT EXISTS OrganizerRequest (
                                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                memberId INTEGER NOT NULL ,
                                                motivation TEXT NOT NULL ,
                                                existingAssociationId BIGINT,
                                                newAssociationData TEXT, // Json pour les données de la nouvelle association
                                                requestDate TIMESTAMP NOT NULL,
                                                status VARCHAR(32) DEFAULT 'PENDING',
                                                adminComment TEXT,
                                                processedByAdminId BIGINT,
                                                processedDate TIMESTAMP,
                                                FOREIGN KEY (memberId) REFERENCES Member(id),
                                                FOREIGN KEY (existingAssociationId) REFERENCES Association(id)
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
                                       date VARCHAR(32),
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
SELECT 'ORGANIZER', 'Pollet', 'Theo', 'theop@mail.com', 'password123', '0765467809', '356 rue Victor Hugo', 'Dijon', 21000, 47.32136825551213, 5.041485596025622
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email =  'theop@mail.com');

-- Associations
INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Association Sportive Paris', 'Promouvoir la course à pied en Île-de-France', 'www.assoparis.com', '/images/logo1.png', 'contact@assoparis.com', '0145789652', '10 place de la République', 'Paris', 75011
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'contact@assoparis.com');

INSERT INTO association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
SELECT 'Courir Ensemble', 'Organisation de courses caritatives', 'www.courirensemble.org', '/images/logo2.png', 'contact@courirensemble.org', '0187654321', '15 rue Lafayette', 'Marseille', 13001
WHERE NOT EXISTS (SELECT 1 FROM association WHERE email = 'contact@courirensemble.org');

-- Courses
INSERT INTO course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Marathon de Paris', 'Un marathon mythique au cœur de Paris', 1, 1, '2025-10-10 17:00', '2025-10-10 18:00', 48.8566, 2.3522, 48.8606, 2.3376, 42.195, 'Champs Élysées', 'Paris', 75008, 5000, 1200, 50.0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE name = 'Marathon de Paris');

INSERT INTO course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Course des Héros', 'Course caritative pour la bonne cause', 2, 2, '2025-08-15 13:00', '2025-08-15 15:30', 45.764, 4.8357, 45.7700, 4.8300, 5, 'Parc Blandant', 'Lyon', 69006, 3000, 800, 30.0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE name = 'Course des Héros');

INSERT INTO course (name, description, associationId, memberCreatorId, startDate, endDate, startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude, distance, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
SELECT 'Trail des Alpes', 'Une course en montagne avec des paysages magnifiques', 2, 1, '2025-07-20 08:00', '2025-07-20 14:00', 45.9237, 6.8694, 45.9170, 6.8720, 25.0, 'Mont Blanc', 'Chamonix', 74400, 1500, 500, 40.0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE name = 'Trail des Alpes');

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
WHERE c.name = 'Course des Héros' AND m.email = 'jean.dupont@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM CourseMember cm
                      JOIN course c2 ON cm.courseId = c2.id
                      JOIN member m2 ON cm.memberId = m2.id
    WHERE c2.name = 'Course des Héros' AND m.email = 'jean.dupont@email.com'
);

-- AssociationMember
INSERT INTO AssociationMember (memberId, associationId)
SELECT 1, 1
WHERE NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = 1 AND associationId = 1);

INSERT INTO AssociationMember (memberId, associationId)
SELECT 2, 2
WHERE NOT EXISTS (SELECT 1 FROM AssociationMember WHERE memberId = 2 AND associationId = 2);

-- Discussions
INSERT INTO Discussion (courseId, isActive)
SELECT 1, TRUE
WHERE NOT EXISTS (SELECT 1 FROM Discussion WHERE courseId = 1);

INSERT INTO Discussion (courseId, isActive)
SELECT 2, TRUE
WHERE NOT EXISTS (SELECT 1 FROM Discussion WHERE courseId = 2);

-- Messages
INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
SELECT 1, 1, 'Bienvenue sur la discussion du Marathon de Paris !', '2025-03-01', FALSE, FALSE
WHERE NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = 1);

INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
SELECT 2, 2, 'Qui participe à la Course des Héros ?', '2025-03-02', FALSE, FALSE
WHERE NOT EXISTS (SELECT 1 FROM Message WHERE discussionId = 2);

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
WHERE c.name = 'Course des Héros' AND m.email = 'jean.dupont@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM Paiement p
    WHERE p.courseMemberId = cm.id
);