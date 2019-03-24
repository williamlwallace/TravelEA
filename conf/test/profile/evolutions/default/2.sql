-- !Ups
-- Create test users
INSERT INTO User (username, password, salt) VALUES ('tester1@gmail.com', 'password', 'salt');
INSERT INTO User (username, password, salt) VALUES ('tester2@gmail.com', 'password', 'salt');

-- Insert traveller types for testing
INSERT INTO TravellerTypeDefinition (description) VALUES ('Test TravellerType 1');
INSERT INTO TravellerTypeDefinition (description) VALUES ('Test TravellerType 2');
INSERT INTO TravellerTypeDefinition (description) VALUES ('Test TravellerType 3');

-- Insert countries for testing
INSERT INTO CountryDefinition (name) VALUES ('Test Country 1');
INSERT INTO CountryDefinition (name) VALUES ('Test Country 2');
INSERT INTO CountryDefinition (name) VALUES ('Test Country 3');

-- Create profile for tester1@gmail.com
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender) VALUES (1, 'Dave', 'Jimmy', 'Smith', '1986-11-05', 'Male');

-- Add nationalities to profile
INSERT INTO Nationality (user_id, country_id) VALUES (1, 1);
INSERT INTO Nationality (user_id, country_id) VALUES (1, 3);

-- Add passports to profile
INSERT INTO Passport (user_id, country_id) values (1, 1);
INSERT INTO Passport (user_id, country_id) values (1, 2);

-- Add Traveller types to profile
INSERT INTO TravellerType (user_id, traveller_type_id) VALUES (1, 1);
INSERT INTO TravellerType (user_id, traveller_type_id) VALUES (1, 2);

-- !Downs
-- Now delete all rows from tables ( DO THIS IN THE RIGHT ORDER, THIS MEANS REVERSE OF CREATION, DON'T MAKE MY MISTAKE )
DELETE FROM TravellerType;
DELETE FROM Passport;
DELETE FROM Nationality;
DELETE FROM Profile;
DELETE FROM CountryDefinition;
DELETE FROM TravellerTypeDefinition;
DELETE FROM User;
