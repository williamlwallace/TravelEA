-- !Ups
-- Create test users
INSERT INTO User (username, password, salt, admin) VALUES ('dave@gmail.com', 'kI9dTQEMsmcbqxn9SBk/jUDHNz7dOBWg/rxxE2xv3cE=', 'L9vI0DLY0cmnLrXrPNKe81IHvGw5NpZ5DgxMcuAkoh4=', 1);
INSERT INTO User (username, password, salt, admin) VALUES ('bob@gmail.com', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', false);
INSERT INTO User (username, password, salt, admin) VALUES ('tester12@gmail.com', 'password', 'salt', 1);
INSERT INTO User (username, password, salt, admin) VALUES ('tester2@gmail.com', 'password', 'salt', 1);

-- Create profile for tester1@gmail.com
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (1, 'Dave', 'Jimmy', 'Smith', '1986-11-05', 'Male');
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (2, 'Steve', 'No', 'Smith', '1999-11-05', 'Female');

-- Add countries
INSERT INTO CountryDefinition (id, name) VALUES
(1, 'Russian Federation'),(2, 'Finland'),(3, 'Kazakhstan');

-- Create 2 test destinations for trips
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (1, 'Eiffel Tower', 'Monument', 'Paris', 10.0, 20.0, 1, 1);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (1, 'Sky Tower', 'Monument', 'Auckland', -36.8484, 174.76000, 2, 1);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (1, 'Britomart Monument', 'Monument', 'Akaroa', -43.81546, 172.94883, 2, 1);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (2, 'London Eye', 'Monument', 'London', 56.3453, 23.94883, 1, 1);
INSERT INTO Destination (user_id, name, type, district, latitude, longitude, country_id, is_public) VALUES (1, 'Sydney Harbour Bridge', 'Bridge', 'Sydney', 56.3453, 23.94883, 1, 0);

-- Create Trips for testing
INSERT INTO Trip (user_id) VALUES (1);
INSERT INTO Trip (user_id, is_public) VALUES (2, 1);
INSERT INTO Trip (user_id, deleted) VALUES (2, 1);
INSERT INTO Trip (user_id, is_public) VALUES (1, 0);

-- Add destinations to the Trips
-- Trip 1
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 0, 1,'2019-04-12 13:59:00', '2019-04-13 08:00:00');
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 1, 2,'2019-04-14 13:59:00', '2019-04-16 08:00:00');
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (1, 2, 3,'2019-04-17 13:59:00', '2019-04-18 08:00:00');
-- Trip 2
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (2, 2, 1,'2019-04-22 13:59:00', '2019-04-23 08:00:00');
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (2, 1, 4,'2019-04-25 13:59:00', '2019-04-26 08:00:00');
-- Trip 4
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (4, 0, 1,'2019-04-12 13:59:00', '2019-04-13 08:00:00');
INSERT INTO TripData (trip_id, position, destination_id, arrival_time, departure_time) VALUES (4, 1, 5, null, null);

INSERT INTO TravellerTypeDefinition (description) VALUES ('Backpacker'), ('Functional/Business Traveller'), ('Groupies'), ('Thrillseeker'), ('Frequent Weekender'), ('Gap Year');

-- Add sample photos
INSERT INTO Photo (user_id, filename, thumbnail_filename, is_public, used_for_profile) VALUES (1, './public/storage/photos/test/test2.jpeg', './public/storage/photos/test/thumbnails/test2.jpeg', 0, 0);

-- Add sample tags
INSERT INTO Tag (name) VALUES ('Russia'), ('sports'), ('#TravelEA');
INSERT INTO DestinationTag (tag_id, destination_id) VALUES (2, 1), (1, 1);
INSERT INTO TripTag (tag_id, trip_id) VALUES (3, 1), (2, 2), (1, 1);
INSERT INTO PhotoTag (tag_id, photo_id) VALUES (2, 1);
INSERT INTO UsedTag (tag_id, user_id) VALUES (3, 1), (2, 1), (1, 1);

-- !Downs
DELETE FROM UsedTag;
DELETE FROM PhotoTag;
DELETE FROM TripTag;
DELETE FROM DestinationTag;
DELETE FROM Tag;
DELETE FROM TreasureHunt;
DELETE FROM PendingDestinationPhoto;
DELETE FROM DestinationPhoto;
DELETE FROM TripData;
DELETE FROM Trip;
DELETE FROM DestinationTravellerTypePending;
DELETE FROM DestinationTravellerType;
DELETE FROM Destination;
DELETE FROM TravellerType;
DELETE FROM TravellerTypeDefinition;
DELETE FROM Passport;
DELETE FROM Nationality;
DELETE FROM CountryDefinition;
DELETE FROM Profile;
DELETE FROM Photo;
DELETE FROM User;