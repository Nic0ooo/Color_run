-- src/main/resources/script.sql
CREATE TABLE Member (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      role VARCHAR(255),
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

CREATE TABLE Association (
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

CREATE TABLE Course (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255),
                description VARCHAR(255),
                associationId INTEGER,
                memberCreatorId INTEGER,
                startDate VARCHAR(255),
                endDate VARCHAR(255),
                startpositionLatitude DOUBLE,
                startpositionLongitude DOUBLE,
                endpositionLatitude DOUBLE,
                endpositionLongitude DOUBLE,
                address VARCHAR(255),
                city VARCHAR(255),
                zipCode INTEGER,
                maxOfRunners INTEGER,
                currentNumberOfRunners INTEGER,
                price DOUBLE,
                FOREIGN KEY (associationId) REFERENCES Association(id),
                FOREIGN KEY (memberCreatorId) REFERENCES Member(id)
);

CREATE TABLE CourseMember (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                courseId INTEGER,
                memberId INTEGER,
                registrationDate VARCHAR(255),
                registrationStatus VARCHAR(255),
                FOREIGN KEY (courseId) REFERENCES Course(id),
                FOREIGN KEY (memberId) REFERENCES Member(id)
);

CREATE TABLE AssociationMember (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                memberId INTEGER,
                associationId INTEGER,
                FOREIGN KEY (associationId) REFERENCES Association(id),
                FOREIGN KEY (memberId) REFERENCES Member(id)
);

CREATE TABLE Discussion (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                courseId INTEGER,
                isActive BOOLEAN DEFAULT TRUE,
                FOREIGN KEY (courseId) REFERENCES Course(id)
);

CREATE TABLE Message (
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

CREATE TABLE Paiement (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                couseMemberId INTEGER,
                date VARCHAR(32),
                amount DOUBLE,
                FOREIGN KEY  (couseMemberId) REFERENCES CourseMember(id)
);

-- Insérer des membres
INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
VALUES
    ('Admin', 'Dupont', 'Jean', 'jean.dupont@email.com', 'password123', '0601020304', '12 rue de Paris', 'Paris', 75001, 48.8566, 2.3522),
    ('Runner', 'Martin', 'Sophie', 'sophie.martin@email.com', 'pass456', '0612345678', '25 avenue des Champs', 'Lyon', 69000, 45.764, 4.8357);

-- Insérer des associations
INSERT INTO Association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
VALUES
    ('Association Sportive Paris', 'Promouvoir la course à pied en Île-de-France', 'www.assoparis.com', '/images/logo1.png', 'contact@assoparis.com', '0145789652', '10 place de la République', 'Paris', 75011),
    ('Courir Ensemble', 'Organisation de courses caritatives', 'www.courirensemble.org', '/images/logo2.png', 'contact@courirensemble.org', '0187654321', '15 rue Lafayette', 'Marseille', 13001);

-- Insérer des courses
INSERT INTO Course (name, description, associationId, memberCreatorId, startDate, endDate, startpositionLatitude, startpositionLongitude, endpositionLatitude, endpositionLongitude, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
VALUES
    ('Marathon de Paris', 'Un marathon mythique au cœur de Paris', 1, 1, '2025-04-10', '2025-04-10', 48.8566, 2.3522, 48.8606, 2.3376, 'Champs Élysées', 'Paris', 75008, 5000, 1200, 50.0),
    ('Course des Héros', 'Course caritative pour la bonne cause', 2, 2, '2025-06-15', '2025-06-15', 45.764, 4.8357, 45.7700, 4.8300, 'Parc de la Tête d’Or', 'Lyon', 69006, 3000, 800, 30.0);

-- Insérer des membres dans les courses
INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus)
VALUES
    (1, 2, '2025-03-01', 'Confirmed'),
    (2, 1, '2025-03-05', 'Pending');

-- Insérer des membres dans les associations
INSERT INTO AssociationMember (memberId, associationId)
VALUES
    (1, 1),
    (2, 2);

-- Insérer des discussions pour les courses
INSERT INTO Discussion (courseId, isActive)
VALUES
    (1, TRUE),
    (2, TRUE);

-- Insérer des messages dans les discussions
INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
VALUES
    (1, 1, 'Bienvenue sur la discussion du Marathon de Paris !', '2025-03-01', FALSE, FALSE),
    (2, 2, 'Qui participe à la Course des Héros ?', '2025-03-02', FALSE, FALSE);

-- Insérer des paiements pour les courses
INSERT INTO Paiement (couseMemberId, date, amount)
VALUES
    (1, '2025-03-02', 50.0),
    (2, '2025-03-06', 30.0);