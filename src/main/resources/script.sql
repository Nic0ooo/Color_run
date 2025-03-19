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
)