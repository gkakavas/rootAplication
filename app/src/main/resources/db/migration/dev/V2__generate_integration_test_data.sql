INSERT INTO groups(group_id, group_name, group_creation_date,group_creator)
VALUES ('19dd2267-8a90-47e5-9a2d-d9e767a109de', 'group1', CURRENT_TIMESTAMP, NULL);

INSERT INTO groups(group_id, group_name, group_creation_date,group_creator)
VALUES ('f8eac3d0-dd4e-4527-90aa-8c1a9f5e7a8f', 'group2', CURRENT_TIMESTAMP, NULL);

INSERT INTO groups(group_id, group_name, group_creation_date,group_creator)
VALUES ('7a62fbce-8677-4903-bc86-d1845600e8d9', 'group3', CURRENT_TIMESTAMP, NULL);

--create and insert into users
INSERT INTO users(user_id,firstname,lastname,password,email,current_project,specialization,role_value,last_login,group_id,register_date,created_by)
VALUES ('ca2d6dfd-8a5d-40f1-8b3b-b7b176ccdbf7','firstname1', 'lastname1', 'password1', 'firstname1@email.com', 'current_project1', 'specialization1', 'ADMIN', NULL, '19dd2267-8a90-47e5-9a2d-d9e767a109de', CURRENT_TIMESTAMP, NULL);

INSERT INTO users(user_id,firstname,lastname,password,email,current_project,specialization,role_value,last_login,group_id,register_date,created_by)
VALUES ('e6e47100-2fe1-4413-89cf-2df47253a3af','firstname2', 'lastname2', 'password2', 'firstname2@email.com', 'current_project2', 'specialization2', 'ADMIN', NULL, '19dd2267-8a90-47e5-9a2d-d9e767a109de', CURRENT_TIMESTAMP, NULL);

INSERT INTO users(user_id,firstname,lastname,password,email,current_project,specialization,role_value,last_login,group_id,register_date,created_by)
VALUES ('e48cf7a3-3427-41d1-b26f-604e74cf6b8a','firstname3', 'lastname3', 'password3', 'firstname3@email.com', 'current_project3', 'specialization3', 'MANAGER', NULL, 'f8eac3d0-dd4e-4527-90aa-8c1a9f5e7a8f', CURRENT_TIMESTAMP, NULL);

INSERT INTO users(user_id,firstname,lastname,password,email,current_project,specialization,role_value,last_login,group_id,register_date,created_by)
VALUES ('45b3df4b-f5bf-49d1-b928-16bbdb8e323e','firstname4', 'lastname4', 'password4', 'firstname4@email.com', 'current_project4', 'specialization4', 'HR', NULL, 'f8eac3d0-dd4e-4527-90aa-8c1a9f5e7a8f', CURRENT_TIMESTAMP, NULL);

INSERT INTO users(user_id,firstname,lastname,password,email,current_project,specialization,role_value,last_login,group_id,register_date,created_by)
VALUES ('4d0dd9db-b777-4e8e-97ba-ef0b57534927','firstname5', 'lastname5', 'password5', 'firstname5@email.com', 'current_project5', 'specialization5', 'USER', NULL, '7a62fbce-8677-4903-bc86-d1845600e8d9', CURRENT_TIMESTAMP, NULL);

INSERT INTO users(user_id,firstname,lastname,password,email,current_project,specialization,role_value,last_login,group_id,register_date,created_by)
VALUES ('10762127-c6ec-41f7-a893-44529e7c6283','firstname6', 'lastname6', 'password6', 'firstname6@email.com', 'current_project6', 'specialization6', 'USER', NULL, '7a62fbce-8677-4903-bc86-d1845600e8d9', CURRENT_TIMESTAMP, NULL);

INSERT INTO users(user_id,firstname,lastname,password,email,current_project,specialization,role_value,last_login,group_id,register_date,created_by)
VALUES ('16beb8e8-5020-48a6-a4a5-4a4cdc014774','firstname7', 'lastname7', 'password7', 'firstname7@email.com', 'current_project7', 'specialization7', 'USER', NULL, NULL, CURRENT_TIMESTAMP, NULL);

INSERT INTO users(user_id,firstname,lastname,password,email,current_project,specialization,role_value,last_login,group_id,register_date,created_by)
VALUES ('9681dc09-75cb-43a0-b2cf-b59c65f142e2','firstname8', 'lastname8', 'password8', 'firstname8@email.com', 'current_project8', 'specialization8', 'USER', NULL, NULL, CURRENT_TIMESTAMP, NULL);
--inserts into events

INSERT INTO events(event_id, event_description, event_body, event_date_time, event_expiration, event_creator)
VALUES ('cfe4889d-3196-4ed0-8511-58d550d6cffb', 'event_description_1', 'event_body_1', '2023-10-15T12:34:56' , '2023-10-15T02:34:56', NULL);

INSERT INTO events(event_id, event_description, event_body, event_date_time, event_expiration, event_creator)
VALUES ('dc1c9a39-4b0a-46cf-b7c7-754587ce1327', 'event_description_2', 'event_body_2', '2023-10-16 12:34:56', '2023-10-16 02:34:56', NULL);

INSERT INTO events(event_id, event_description, event_body, event_date_time, event_expiration, event_creator)
VALUES ('fbc2f68a-6bbf-45c6-b867-dc721e65fa8b', 'event_description_3', 'event_body_3', '2023-10-17 12:34:56', '2023-10-17 02:34:56', NULL);

INSERT INTO user_event_mapping(event_id, user_id) VALUES ('cfe4889d-3196-4ed0-8511-58d550d6cffb', 'ca2d6dfd-8a5d-40f1-8b3b-b7b176ccdbf7');
INSERT INTO user_event_mapping(event_id, user_id) VALUES ('cfe4889d-3196-4ed0-8511-58d550d6cffb', 'e6e47100-2fe1-4413-89cf-2df47253a3af');
INSERT INTO user_event_mapping(event_id, user_id) VALUES ('cfe4889d-3196-4ed0-8511-58d550d6cffb', 'e48cf7a3-3427-41d1-b26f-604e74cf6b8a');

INSERT INTO user_event_mapping(event_id, user_id) VALUES ('dc1c9a39-4b0a-46cf-b7c7-754587ce1327', 'e48cf7a3-3427-41d1-b26f-604e74cf6b8a');
INSERT INTO user_event_mapping(event_id, user_id) VALUES ('dc1c9a39-4b0a-46cf-b7c7-754587ce1327', '45b3df4b-f5bf-49d1-b928-16bbdb8e323e');
INSERT INTO user_event_mapping(event_id, user_id) VALUES ('dc1c9a39-4b0a-46cf-b7c7-754587ce1327', '4d0dd9db-b777-4e8e-97ba-ef0b57534927');
INSERT INTO user_event_mapping(event_id, user_id) VALUES ('dc1c9a39-4b0a-46cf-b7c7-754587ce1327', '10762127-c6ec-41f7-a893-44529e7c6283');

INSERT INTO user_event_mapping(event_id, user_id) VALUES ('fbc2f68a-6bbf-45c6-b867-dc721e65fa8b', 'ca2d6dfd-8a5d-40f1-8b3b-b7b176ccdbf7');
INSERT INTO user_event_mapping(event_id, user_id) VALUES ('fbc2f68a-6bbf-45c6-b867-dc721e65fa8b', 'e6e47100-2fe1-4413-89cf-2df47253a3af');
INSERT INTO user_event_mapping(event_id, user_id) VALUES ('fbc2f68a-6bbf-45c6-b867-dc721e65fa8b', 'e48cf7a3-3427-41d1-b26f-604e74cf6b8a');
INSERT INTO user_event_mapping(event_id, user_id) VALUES ('fbc2f68a-6bbf-45c6-b867-dc721e65fa8b', '45b3df4b-f5bf-49d1-b928-16bbdb8e323e');
INSERT INTO user_event_mapping(event_id, user_id) VALUES ('fbc2f68a-6bbf-45c6-b867-dc721e65fa8b', '4d0dd9db-b777-4e8e-97ba-ef0b57534927');
INSERT INTO user_event_mapping(event_id, user_id) VALUES ('fbc2f68a-6bbf-45c6-b867-dc721e65fa8b', '10762127-c6ec-41f7-a893-44529e7c6283');