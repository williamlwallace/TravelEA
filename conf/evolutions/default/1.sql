-- !Ups

-- Create User table
CREATE TABLE IF NOT EXISTS User
  (
    id                INT NOT NULL AUTO_INCREMENT,
    username          VARCHAR(64) NOT NULL,
    password          VARCHAR(128) NOT NULL,
    salt              VARCHAR(64) NOT NULL,
    admin             BOOLEAN NOT NULL DEFAULT false,
    deleted           BOOLEAN NOT NULL DEFAULT false,
    creation_date     DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (username)
  );

-- Create Photo table, which stores the filenames and details for all photos
CREATE TABLE IF NOT EXISTS Photo
  (
    guid                  INT NOT NULL AUTO_INCREMENT,
    user_id               INT NOT NULL,
    filename              VARCHAR(256) NOT NULL,
    thumbnail_filename    VARCHAR(256) NOT NULL,
    caption               VARCHAR(256) NOT NULL DEFAULT '',
    is_public             BOOLEAN NOT NULL,
    uploaded              DATETIME DEFAULT CURRENT_TIMESTAMP,
    used_for_profile      BOOLEAN NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    PRIMARY KEY (guid)
  );

-- Create Profile table
CREATE TABLE IF NOT EXISTS Profile
  (
    user_id             INT NOT NULL AUTO_INCREMENT,
    first_name          VARCHAR(64) NOT NULL,
    middle_name         VARCHAR(64),
    last_name           VARCHAR(64) NOT NULL,
    date_of_birth       DATE,
    gender              VARCHAR(32),
    creation_date       DATETIME DEFAULT CURRENT_TIMESTAMP,
    profile_photo_guid  INT,
    cover_photo_guid    INT,
    deleted             BOOLEAN DEFAULT false,
    PRIMARY KEY (user_id),
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (profile_photo_guid) REFERENCES Photo(guid),
    FOREIGN KEY (cover_photo_guid) REFERENCES Photo(guid) ON DELETE SET NULL
  );

-- Create the country definition table, which is static and defines all possible countries
CREATE TABLE IF NOT EXISTS CountryDefinition
  (
    id                INT NOT NULL,
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
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (country_id) REFERENCES CountryDefinition(id) ON DELETE CASCADE,
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
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (country_id) REFERENCES CountryDefinition(id) ON DELETE CASCADE,
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
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (traveller_type_id) REFERENCES TravellerTypeDefinition(id) ON DELETE CASCADE,
    PRIMARY KEY (guid),
    INDEX travellertype_index (user_id, traveller_type_id),
    UNIQUE(user_id, traveller_type_id)
  );

-- Create Destination table
CREATE TABLE IF NOT EXISTS Destination
  (
    id                  INT NOT NULL AUTO_INCREMENT,
    user_id             INT NOT NULL,
    name                VARCHAR(128) NOT NULL,
    type                VARCHAR(128) NOT NULL,
    district            VARCHAR(128) NOT NULL,
    latitude            DOUBLE NOT NULL,
    longitude           DOUBLE NOT NULL,
    country_id          INT NOT NULL,
    is_public           BIT NOT NULL DEFAULT 0,
    deleted             BOOLEAN NOT NULL DEFAULT false,
    primary_photo_guid  INT,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (country_id) REFERENCES CountryDefinition(id) ON DELETE CASCADE,
    FOREIGN KEY (primary_photo_guid) REFERENCES Photo(guid) ON DELETE SET NULL,
    PRIMARY KEY (id)
  );

-- Create Follower table for users
CREATE TABLE IF NOT EXISTS FollowerUser
  (
    guid              INT NOT NULL AUTO_INCREMENT,
    user_id           INT NOT NULL,
    follower_id       INT NOT NULL,
    follow_time       DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted           BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (guid),
    FOREIGN KEY (user_id) REFERENCES Profile(user_id) ON DELETE CASCADE,
    FOREIGN KEY (follower_id) REFERENCES Profile(user_id) ON DELETE CASCADE
  );

-- Create Follower table for destinations
CREATE TABLE IF NOT EXISTS FollowerDestination
  (
    guid              INT NOT NULL AUTO_INCREMENT,
    destination_id    INT NOT NULL,
    follower_id       INT NOT NULL,
    follow_time       DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted           BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (guid),
    FOREIGN KEY (destination_id) REFERENCES Destination(id) ON DELETE CASCADE,
    FOREIGN KEY (follower_id) REFERENCES Profile(user_id) ON DELETE CASCADE
  );

-- Create DestinationTravellerType table, which specifies the traveller types of users
CREATE TABLE IF NOT EXISTS DestinationTravellerType
  (
    guid              INT NOT NULL AUTO_INCREMENT,
    dest_id           INT NOT NULL,
    traveller_type_definition_id INT NOT NULL,
    FOREIGN KEY (dest_id) REFERENCES Destination(id) ON DELETE CASCADE,
    FOREIGN KEY (traveller_type_definition_id) REFERENCES TravellerTypeDefinition(id) ON DELETE CASCADE,
    PRIMARY KEY (guid),
    INDEX destinationtravellertype_index (dest_id, traveller_type_definition_id),
    UNIQUE(dest_id, traveller_type_definition_id)
  );

-- Create DestinationTravellerTypePending table, which specifies the traveller types of users
CREATE TABLE IF NOT EXISTS DestinationTravellerTypePending
  (
    guid              INT NOT NULL AUTO_INCREMENT,
    dest_id           INT NOT NULL,
    traveller_type_definition_id INT NOT NULL,
    FOREIGN KEY (dest_id) REFERENCES Destination(id) ON DELETE CASCADE,
    FOREIGN KEY (traveller_type_definition_id) REFERENCES TravellerTypeDefinition(id) ON DELETE CASCADE,
    PRIMARY KEY (guid),
    INDEX destinationtravellertypepending_index (dest_id, traveller_type_definition_id),
    UNIQUE(dest_id, traveller_type_definition_id)
  );

-- Create Trip table, which maps trips to users
CREATE TABLE IF NOT EXISTS Trip
  (
    id                INT NOT NULL AUTO_INCREMENT,
    user_id           INT NOT NULL,
    is_public         BIT NOT NULL DEFAULT 0,
    deleted           BOOLEAN NOT NULL DEFAULT false,
    creation_date     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
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
    FOREIGN KEY (trip_id) REFERENCES Trip(id) ON DELETE CASCADE,
    FOREIGN KEY (destination_id) REFERENCES Destination(id) ON DELETE CASCADE,
    PRIMARY KEY (guid),
    INDEX tripdata_index (trip_id, position),
    INDEX destination_id_index (destination_id)
  );

    -- Create DestinationPhotos table, which specifies the photos of a Destinations
CREATE TABLE IF NOT EXISTS DestinationPhoto
  (
    guid                  INT NOT NULL AUTO_INCREMENT,
    photo_id              INT NOT NULL,
    destination_id         INT NOT NULL,
    deleted               BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (photo_id) REFERENCES Photo(guid) ON DELETE CASCADE,
    FOREIGN KEY (destination_id) REFERENCES Destination(id) ON DELETE CASCADE,
    PRIMARY KEY (guid),
    INDEX Destination_photo_index (photo_id, destination_id),
    UNIQUE(photo_id, destination_id)
  );

-- Create PendingDestinationPhotos table, which specifies all current requests for a change in primary photos
CREATE TABLE IF NOT EXISTS PendingDestinationPhoto
  (
    id                     INT NOT NULL AUTO_INCREMENT,
    photo_guid               INT NOT NULL,
    dest_id                INT NOT NULL,
    FOREIGN KEY (photo_guid) REFERENCES Photo(guid) ON DELETE CASCADE,
    FOREIGN KEY (dest_id)  REFERENCES Destination(id) ON DELETE CASCADE,
    PRIMARY KEY (id)
);

-- Create treasure hunt table, which stores the riddle and dates or a treasure hunt about a destination
CREATE TABLE IF NOT EXISTS TreasureHunt
  (
    id                    INT NOT NULL AUTO_INCREMENT,
    user_id               INT NOT NULL,
    destination_id        INT NOT NULL,
    riddle                VARCHAR(1024) NOT NULL,
    start_date            DATE NOT NULL,
    end_date              DATE NOT NULL,
    deleted               BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (destination_id) REFERENCES Destination(id) ON DELETE CASCADE,
    PRIMARY KEY (id)
  );

-- Create tags table, which stores the name of the tag
CREATE TABLE IF NOT EXISTS Tag
  (
    id                    INT NOT NULL AUTO_INCREMENT,
    name                  VARCHAR(64) UNIQUE NOT NULL,
    PRIMARY KEY (id),
    INDEX tag_name_index (name)
  );

-- Specifies the DestinationTag table, this is only done in the SQL so we can populate it in the evolutions
CREATE TABLE IF NOT EXISTS DestinationTag
  (
    guid                  INT NOT NULL AUTO_INCREMENT,
    tag_id                INT NOT NULL,
    destination_id        INT NOT NULL,
    FOREIGN KEY (tag_id)  REFERENCES Tag(id) ON DELETE CASCADE,
    FOREIGN KEY (destination_id) REFERENCES Destination(id) ON DELETE CASCADE,
    PRIMARY KEY (guid)
  );

-- Specifies the TripTag table, this is only done in the SQL so we can populate it in the evolutions
CREATE TABLE IF NOT EXISTS TripTag
  (
    guid                  INT NOT NULL AUTO_INCREMENT,
    tag_id                INT NOT NULL,
    trip_id               INT NOT NULL,
    FOREIGN KEY (tag_id) REFERENCES Tag(id) ON DELETE CASCADE,
    FOREIGN KEY (trip_id) REFERENCES Trip(id) ON DELETE CASCADE,
    PRIMARY KEY (guid)
  );

-- Specifies the PhotoTag table, this is only done in the SQL so we can populate it in the evolutions
CREATE TABLE IF NOT EXISTS PhotoTag
  (
    guid                  INT NOT NULL AUTO_INCREMENT,
    tag_id                INT NOT NULL,
    photo_id              INT NOT NULL,
    FOREIGN KEY (tag_id) REFERENCES Tag(id) ON DELETE CASCADE,
    FOREIGN KEY (photo_id) REFERENCES Photo(guid) ON DELETE CASCADE,
    PRIMARY KEY (guid)
  );

-- Specifies the UsedTag table
CREATE TABLE IF NOT EXISTS UsedTag
  (
    guid                  INT NOT NULL AUTO_INCREMENT,
    tag_id                INT NOT NULL,
    user_id               INT NOT NULL,
    time_used             DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tag_id) REFERENCES Tag(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    PRIMARY KEY (guid)
  );

-- Specifies the news feed event table
CREATE TABLE IF NOT EXISTS NewsFeedEvent
  (
    guid                    INT NOT NULL AUTO_INCREMENT,
    event_type              VARCHAR(30) NOT NULL,
    user_id                 INT,
    dest_id                 INT,
    ref_id                  INT,
    created                 DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (dest_id) REFERENCES Destination(id) ON DELETE CASCADE,
    PRIMARY KEY (guid)
  );

-- Create Likes table for news feed events
CREATE TABLE IF NOT EXISTS Likes
(
    guid              INT NOT NULL AUTO_INCREMENT,
    event_id          INT NOT NULL,
    user_id           INT NOT NULL,
    deleted           BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (event_id) REFERENCES NewsFeedEvent(guid) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    PRIMARY KEY (guid)
);

-- !Downs
DROP TABLE Likes;
DROP TABLE NewsFeedEvent;
DROP TABLE UsedTag;
DROP TABLE PhotoTag;
DROP TABLE TripTag;
DROP TABLE DestinationTag;
DROP TABLE Tag;
DROP TABLE TreasureHunt;
DROP TABLE PendingDestinationPhoto;
DROP TABLE DestinationPhoto;
DROP TABLE TripData;
DROP TABLE Trip;
DROP TABLE DestinationTravellerTypePending;
DROP TABLE DestinationTravellerType;
DROP TABLE FollowerDestination;
DROP TABLE FollowerUser;
DROP TABLE Destination;
DROP TABLE TravellerType;
DROP TABLE TravellerTypeDefinition;
DROP TABLE Passport;
DROP TABLE Nationality;
DROP TABLE CountryDefinition;
DROP TABLE Profile;
DROP TABLE Photo;
DROP TABLE User;