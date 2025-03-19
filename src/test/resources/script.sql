-- src/main/resources/script.sql
CREATE TABLE User (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      role VARCHAR(255),
                      name VARCHAR(255),
                      firstname VARCHAR(255),
                      email VARCHAR(255),
                      password VARCHAR(255),
                      phoneNumber INTEGER,
                      address VARCHAR(255),
                      city VARCHAR(255),
                      zipCode INTEGER,
                      positionLatitude DOUBLE,
                      positionLongitude DOUBLE
);