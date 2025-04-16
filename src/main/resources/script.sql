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
-- Insérer des membres seulement si la table est vide
    INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
    SELECT 'Admin', 'Dupont', 'Jean', 'jean.dupont@email.com', 'password123', '0601020304', '12 rue de Paris', 'Paris', 75001, 48.8566, 2.3522
    WHERE NOT EXISTS (SELECT 1 FROM Member LIMIT 1);

    INSERT INTO Member (role, name, firstname, email, password, phoneNumber, address, city, zipCode, positionLatitude, positionLongitude)
    SELECT 'Runner', 'Martin', 'Sophie', 'sophie.martin@email.com', 'pass456', '0612345678', '25 avenue des Champs', 'Lyon', 69000, 45.764, 4.8357
    WHERE NOT EXISTS (SELECT 1 FROM Member LIMIT 2) AND EXISTS (SELECT 1 FROM Member LIMIT 1);

    -- Insérer des associations seulement si la table est vide
    INSERT INTO Association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
    SELECT 'Association Sportive Paris', 'Promouvoir la course à pied en Île-de-France', 'www.assoparis.com', '/images/logo1.png', 'contact@assoparis.com', '0145789652', '10 place de la République', 'Paris', 75011
    WHERE NOT EXISTS (SELECT 1 FROM Association LIMIT 1);

    INSERT INTO Association (name, description, websiteLink, logoPath, email, phoneNumber, address, city, zipCode)
    SELECT 'Courir Ensemble', 'Organisation de courses caritatives', 'www.courirensemble.org', '/images/logo2.png', 'contact@courirensemble.org', '0187654321', '15 rue Lafayette', 'Marseille', 13001
    WHERE NOT EXISTS (SELECT 1 FROM Association LIMIT 2) AND EXISTS (SELECT 1 FROM Association LIMIT 1);

    -- Insérer des courses seulement si la table est vide
    INSERT INTO Course (name, description, associationId, memberCreatorId, startDate, endDate, startpositionLatitude, startpositionLongitude, endpositionLatitude, endpositionLongitude, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
    SELECT 'Marathon de Paris', 'Un marathon mythique au cœur de Paris', 1, 1, '2025-04-10', '2025-04-10', 48.8566, 2.3522, 48.8606, 2.3376, 'Champs Élysées', 'Paris', 75008, 5000, 1200, 50.0
    WHERE NOT EXISTS (SELECT 1 FROM Course LIMIT 1);

    INSERT INTO Course (name, description, associationId, memberCreatorId, startDate, endDate, startpositionLatitude, startpositionLongitude, endpositionLatitude, endpositionLongitude, address, city, zipCode, maxOfRunners, currentNumberOfRunners, price)
    SELECT 'Course des Héros', 'Course caritative pour la bonne cause', 2, 2, '2025-06-15', '2025-06-15', 45.764, 4.8357, 45.7700, 4.8300, 'Parc Blandant', 'Lyon', 69006, 3000, 800, 30.0
    WHERE NOT EXISTS (SELECT 1 FROM Course LIMIT 2) AND EXISTS (SELECT 1 FROM Course LIMIT 1);

    -- Insérer des membres dans les courses seulement si la table est vide
    INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus)
    SELECT 1, 2, '2025-03-01', 'Confirmed'
    WHERE NOT EXISTS (SELECT 1 FROM CourseMember LIMIT 1);

    INSERT INTO CourseMember (courseId, memberId, registrationDate, registrationStatus)
    SELECT 2, 1, '2025-03-05', 'Pending'
    WHERE NOT EXISTS (SELECT 1 FROM CourseMember LIMIT 2) AND EXISTS (SELECT 1 FROM CourseMember LIMIT 1);

    -- Insérer des membres dans les associations seulement si la table est vide
    INSERT INTO AssociationMember (memberId, associationId)
    SELECT 1, 1
    WHERE NOT EXISTS (SELECT 1 FROM AssociationMember LIMIT 1);

    INSERT INTO AssociationMember (memberId, associationId)
    SELECT 2, 2
    WHERE NOT EXISTS (SELECT 1 FROM AssociationMember LIMIT 2) AND EXISTS (SELECT 1 FROM AssociationMember LIMIT 1);

    -- Insérer des discussions pour les courses seulement si la table est vide
    INSERT INTO Discussion (courseId, isActive)
    SELECT 1, TRUE
    WHERE NOT EXISTS (SELECT 1 FROM Discussion LIMIT 1);

    INSERT INTO Discussion (courseId, isActive)
    SELECT 2, TRUE
    WHERE NOT EXISTS (SELECT 1 FROM Discussion LIMIT 2) AND EXISTS (SELECT 1 FROM Discussion LIMIT 1);

    -- Insérer des messages dans les discussions seulement si la table est vide
    INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
    SELECT 1, 1, 'Bienvenue sur la discussion du Marathon de Paris !', '2025-03-01', FALSE, FALSE
    WHERE NOT EXISTS (SELECT 1 FROM Message LIMIT 1);

    INSERT INTO Message (discussionId, memberId, content, date, isPin, isHidden)
    SELECT 2, 2, 'Qui participe à la Course des Héros ?', '2025-03-02', FALSE, FALSE
    WHERE NOT EXISTS (SELECT 1 FROM Message LIMIT 2) AND EXISTS (SELECT 1 FROM Message LIMIT 1);

    -- Insérer des paiements pour les courses seulement si la table est vide
    INSERT INTO Paiement (couseMemberId, date, amount)
    SELECT 1, '2025-03-02', 50.0
    WHERE NOT EXISTS (SELECT 1 FROM Paiement LIMIT 1);

    INSERT INTO Paiement (couseMemberId, date, amount)
    SELECT 2, '2025-03-06', 30.0
    WHERE NOT EXISTS (SELECT 1 FROM Paiement LIMIT 2) AND EXISTS (SELECT 1 FROM Paiement LIMIT 1);