-- AUTHOR: Matthew Minish, William Wallace
-- MODIFIED: 14/3/2019 2.00PM

-- !Ups
DROP TABLE IF EXISTS TravellerType;
DROP TABLE IF EXISTS Passport;
DROP TABLE IF EXISTS Nationality;
DROP TABLE IF EXISTS TravellerTypeDefinition;
DROP TABLE IF EXISTS TripData;
DROP TABLE IF EXISTS Destination;
DROP TABLE IF EXISTS Trip;
DROP TABLE IF EXISTS CountryDefinition;
DROP TABLE IF EXISTS Profile;
DROP TABLE IF EXISTS User;

-- Create User table
CREATE TABLE IF NOT EXISTS User
  (
    id                INT NOT NULL AUTO_INCREMENT,
    username          VARCHAR(64) NOT NULL,
    password          VARCHAR(128) NOT NULL,
    salt              VARCHAR(64) NOT NULL,
    auth_token        VARCHAR(128),
    PRIMARY KEY (id),
    UNIQUE (username),
    UNIQUE (auth_token)
  );

-- Create Profile table
CREATE TABLE IF NOT EXISTS Profile
  (
    user_id           INT NOT NULL AUTO_INCREMENT,
    first_name        VARCHAR(64) NOT NULL,
    middle_name       VARCHAR(64),
    last_name         VARCHAR(64) NOT NULL,
    date_of_birth     DATE,
    gender            VARCHAR(32),
    PRIMARY KEY (user_id),
    FOREIGN KEY (user_id) REFERENCES User(id)
  );

CREATE TABLE IF NOT EXISTS CountryDefinition
  (
    id                INT NOT NULL AUTO_INCREMENT,
    name              VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    INDEX name_index (name)
  );

-- Create Nationality table, which specifies nationalities for users
CREATE TABLE IF NOT EXISTS Nationality
  (
    guid              INT NOT NULL AUTO_INCREMENT,
    user_id           INT NOT NULL,
    country_id        INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id),
    FOREIGN KEY (country_id) REFERENCES CountryDefinition(id),
    PRIMARY KEY (guid),
    INDEX nationality_index (user_id, country_id),
    UNIQUE (user_id, country_id)
  );

-- Create Passport table, which specifies passports of users
CREATE TABLE IF NOT EXISTS Passport
  (
    guid              INT NOT NULL AUTO_INCREMENT,
    user_id           INT NOT NULL,
    country_id        INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id),
    FOREIGN KEY (country_id) REFERENCES CountryDefinition(id),
    PRIMARY KEY (guid),
    INDEX passport_index (user_id, country_id),
    UNIQUE (user_id, country_id)
  );

-- Create the traveller type definitions table (as above, static-ish table with all possible values)
CREATE TABLE IF NOT EXISTS TravellerTypeDefinition
  (
    id                INT NOT NULL AUTO_INCREMENT,
    description       VARCHAR(2048) NOT NULL,
    PRIMARY KEY (id)
  );

-- Create TravellerType table, which specifies the traveller types of users
CREATE TABLE IF NOT EXISTS TravellerType
  (
    guid              INT NOT NULL AUTO_INCREMENT,
    user_id           INT NOT NULL,
    traveller_type_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id),
    FOREIGN KEY (traveller_type_id) REFERENCES TravellerTypeDefinition(id),
    PRIMARY KEY (guid),
    INDEX travellertype_index (user_id, traveller_type_id),
    UNIQUE(user_id, traveller_type_id)
  );

-- Create Destination table
CREATE TABLE IF NOT EXISTS Destination
  (
    id                INT NOT NULL AUTO_INCREMENT,
    name              VARCHAR(128) NOT NULL,
    type              VARCHAR(128) NOT NULL, -- We may want to make a separate table which stores these
    district          VARCHAR(128) NOT NULL,
    latitude          DOUBLE NOT NULL,
    longitude         DOUBLE NOT NULL,
    country_id        INT NOT NULL,
    FOREIGN KEY (country_id) REFERENCES CountryDefinition(id),
    PRIMARY KEY (id)
  );

-- Create Trip table, which maps trips to users
CREATE TABLE IF NOT EXISTS Trip
  (
    id                INT NOT NULL AUTO_INCREMENT,
    user_id           INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id),
    PRIMARY KEY (id),
    INDEX user_id_index (user_id)
  );

-- Create TripData table, which stores the actual data (i.e destinations, times, etc.) of all trips
CREATE TABLE IF NOT EXISTS TripData
  (
    guid              INT NOT NULL AUTO_INCREMENT,
    trip_id           INT NOT NULL,
    position          INT NOT NULL,
    destination_id    INT NOT NULL,
    arrival_time      DATETIME,
    departure_time    DATETIME,
    FOREIGN KEY (trip_id) REFERENCES Trip(id),
    FOREIGN KEY (destination_id) REFERENCES Destination(id),
    PRIMARY KEY (guid),
    INDEX tripdata_index (trip_id, position),
    INDEX destination_id_index (destination_id)
  );

-- !Downs
DROP TABLE TravellerType;
DROP TABLE Passport;
DROP TABLE Nationality;
DROP TABLE TravellerTypeDefinition;
DROP TABLE TripData;
DROP TABLE Destination;
DROP TABLE Trip;
DROP TABLE CountryDefinition;
DROP TABLE Profile;
DROP TABLE User;


