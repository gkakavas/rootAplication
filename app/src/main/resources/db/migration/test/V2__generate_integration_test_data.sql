INSERT INTO groups(group_id, group_name, group_creation_date,group_creator)
VALUES ('19dd2267-8a90-47e5-9a2d-d9e767a109de', 'group1', CURRENT_TIMESTAMP, NULL);

INSERT INTO groups(group_id, group_name, group_creation_date,group_creator)
VALUES ('f8eac3d0-dd4e-4527-90aa-8c1a9f5e7a8f', 'group2', CURRENT_TIMESTAMP, NULL);

INSERT INTO groups(group_id, group_name, group_creation_date,group_creator)
VALUES ('7a62fbce-8677-4903-bc86-d1845600e8d9', 'group3', CURRENT_TIMESTAMP, NULL);

--create and insert into users
INSERT INTO users(user_id,firstname,lastname,password,email,current_project,specialization,role_value,last_login,group_id,register_date,created_by)
VALUES ('ca2d6dfd-8a5d-40f1-8b3b-b7b176ccdbf7','firstname1', 'lastname1', '$2a$12$rIs7Ha9klP.7BuRqSAJBeu.O0u/nTre3fFwl1X5jGM5o2pD8e6XZG', 'firstname1@email.com', 'current_project1', 'specialization1', 'ADMIN', NULL, '19dd2267-8a90-47e5-9a2d-d9e767a109de', CURRENT_TIMESTAMP, NULL);
INSERT INTO users(user_id,firstname,lastname,password,email,current_project,specialization,role_value,last_login,group_id,register_date,created_by)
VALUES ('e6e47100-2fe1-4413-89cf-2df47253a3af','firstname2', 'lastname2', '$2a$12$UZMk.MUimwy7NYGdNMkbF.CtX9RbEE310rJ0AldHsSOPYfvZXnoza', 'firstname2@email.com', 'current_project2', 'specialization2', 'ADMIN', NULL, '19dd2267-8a90-47e5-9a2d-d9e767a109de', CURRENT_TIMESTAMP, NULL);
INSERT INTO users(user_id,firstname,lastname,password,email,current_project,specialization,role_value,last_login,group_id,register_date,created_by)
VALUES ('e48cf7a3-3427-41d1-b26f-604e74cf6b8a','firstname3', 'lastname3', '$2a$12$hlI8PeBo/u6Dv/l80HeT7.PF9T7od.A7oZRE6psxsDcZdj57jUPu.', 'firstname3@email.com', 'current_project3', 'specialization3', 'MANAGER', NULL, 'f8eac3d0-dd4e-4527-90aa-8c1a9f5e7a8f', CURRENT_TIMESTAMP, NULL);
INSERT INTO users(user_id,firstname,lastname,password,email,current_project,specialization,role_value,last_login,group_id,register_date,created_by)
VALUES ('45b3df4b-f5bf-49d1-b928-16bbdb8e323e','firstname4', 'lastname4', '$2a$12$J3GBPFVN5jODkghXKATJJuDaBtjY3owv51FIdo8nZZaev.s1AKs.m', 'firstname4@email.com', 'current_project4', 'specialization4', 'HR', NULL, 'f8eac3d0-dd4e-4527-90aa-8c1a9f5e7a8f', CURRENT_TIMESTAMP, NULL);
INSERT INTO users(user_id,firstname,lastname,password,email,current_project,specialization,role_value,last_login,group_id,register_date,created_by)
VALUES ('4d0dd9db-b777-4e8e-97ba-ef0b57534927','firstname5', 'lastname5', '$2a$12$lJQA/JBB1ZwVl3Q4HmKbNeJMdXZsx4weMUit2BuFbooikMt6K1IFK', 'firstname5@email.com', 'current_project5', 'specialization5', 'USER', NULL, '7a62fbce-8677-4903-bc86-d1845600e8d9', CURRENT_TIMESTAMP, NULL);
INSERT INTO users(user_id,firstname,lastname,password,email,current_project,specialization,role_value,last_login,group_id,register_date,created_by)
VALUES ('10762127-c6ec-41f7-a893-44529e7c6283','firstname6', 'lastname6', '$2a$12$7tSCfJOwTseGtMJteEyt4eMJnOEPJtHDqPKmwOI2iBzCVXY22RQtS', 'firstname6@email.com', 'current_project6', 'specialization6', 'USER', NULL, '7a62fbce-8677-4903-bc86-d1845600e8d9', CURRENT_TIMESTAMP, NULL);
INSERT INTO users(user_id,firstname,lastname,password,email,current_project,specialization,role_value,last_login,group_id,register_date,created_by)
VALUES ('16beb8e8-5020-48a6-a4a5-4a4cdc014774','firstname7', 'lastname7', '$2a$12$tTrXaf7KD3KD4EqLIuCi.ur.ahyhz8Hx0Rp.Fpq.Qj6HonqvVVN3C', 'firstname7@email.com', 'current_project7', 'specialization7', 'USER', NULL, 'f8eac3d0-dd4e-4527-90aa-8c1a9f5e7a8f', CURRENT_TIMESTAMP, NULL);
INSERT INTO users(user_id,firstname,lastname,password,email,current_project,specialization,role_value,last_login,group_id,register_date,created_by)
VALUES ('9681dc09-75cb-43a0-b2cf-b59c65f142e2','firstname8', 'lastname8', '$2a$12$02NcPoSh.ILOsr1ayY.hb.6kxjzpwULffayWhn9D6Hipxuo6CRoQe', 'firstname8@email.com', 'current_project8', 'specialization8', 'USER', NULL, 'f8eac3d0-dd4e-4527-90aa-8c1a9f5e7a8f', CURRENT_TIMESTAMP, NULL);
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




INSERT INTO files(approved,approved_date, file_size, upload_date, approved_by, file_id, user_id, access_url, file_kind, file_type, filename)
VALUES (NULL, NULL, 8530, CURRENT_TIMESTAMP, NULL, 'e5321153-5aae-4821-9175-16dbd7f00996','4d0dd9db-b777-4e8e-97ba-ef0b57534927', 'testUploads\timesheets\4d0dd9db-b777-4e8e-97ba-ef0b57534927\testExcelFile.xlsx', 'TIMESHEET', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'testExcelFile.xlsx');
INSERT INTO files(approved,approved_date, file_size, upload_date, approved_by, file_id, user_id, access_url, file_kind, file_type, filename)
VALUES (NULL, NULL, 8417, CURRENT_TIMESTAMP, NULL, 'a75e40f7-6978-4c1e-accd-363fffdadca1','10762127-c6ec-41f7-a893-44529e7c6283', 'testUploads\timesheets\10762127-c6ec-41f7-a893-44529e7c6283\testExcelFile2.xlsx', 'TIMESHEET', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'testExcelFile2.xlsx');
INSERT INTO files(approved,approved_date, file_size, upload_date, approved_by, file_id, user_id, access_url, file_kind, file_type, filename)
VALUES (NULL, NULL, 8417, CURRENT_TIMESTAMP, NULL, '9a3d633e-96a9-44c4-bc5c-e9e4d8a77fcc','16beb8e8-5020-48a6-a4a5-4a4cdc014774', 'testUploads\timesheets\16beb8e8-5020-48a6-a4a5-4a4cdc014774\testExcelFile3.xlsx', 'TIMESHEET', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'testExcelFile3.xlsx');
INSERT INTO files(approved,approved_date, file_size, upload_date, approved_by, file_id, user_id, access_url, file_kind, file_type, filename)
VALUES (NULL, NULL, 11981, CURRENT_TIMESTAMP, NULL, '31ff8538-a270-4b88-aff2-13c8c4b1f3a2','4d0dd9db-b777-4e8e-97ba-ef0b57534927', 'testUploads\evaluations\4d0dd9db-b777-4e8e-97ba-ef0b57534927\testWordFile.docx', 'EVALUATION', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'testWordFile.docx');
INSERT INTO files(approved,approved_date, file_size, upload_date, approved_by, file_id, user_id, access_url, file_kind, file_type, filename)
VALUES (NULL, NULL, 11981, CURRENT_TIMESTAMP, NULL, 'c9bd5106-4688-4d23-a553-e09656123e9e','10762127-c6ec-41f7-a893-44529e7c6283', 'testUploads\evaluations\10762127-c6ec-41f7-a893-44529e7c6283\testWordFile2.docx', 'EVALUATION', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'testWordFile2.docx');
INSERT INTO files(approved,approved_date, file_size, upload_date, approved_by, file_id, user_id, access_url, file_kind, file_type, filename)
VALUES (NULL, NULL, 11981, CURRENT_TIMESTAMP, NULL, 'f0135ace-4458-47c1-af42-40570c181f90','16beb8e8-5020-48a6-a4a5-4a4cdc014774', 'testUploads\evaluations\16beb8e8-5020-48a6-a4a5-4a4cdc014774\testWordFile3.docx', 'EVALUATION', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'testWordFile3.docx');




INSERT INTO leaves(approved, leave_ends, leave_starts, approved_on, approved_by, leave_id, user_id, leave_type)
VALUES (FALSE,'2023-11-7', '2023-11-1', NULL, NULL, '640f3ded-a09b-4fb0-a6e5-161460b90e3f', '16beb8e8-5020-48a6-a4a5-4a4cdc014774', 'SICK_LEAVE');
INSERT INTO leaves(approved, leave_ends, leave_starts, approved_on, approved_by, leave_id, user_id, leave_type)
VALUES (FALSE, '2023-12-20', '2023-12-15', NULL, NULL, '279feddf-d4b5-444f-a5e7-5c7ccbe504f6', '9681dc09-75cb-43a0-b2cf-b59c65f142e2', 'HOLIDAY');
INSERT INTO leaves(approved, leave_ends, leave_starts, approved_on, approved_by, leave_id, user_id, leave_type)
VALUES (FALSE, '2024-1-7', '2023-12-25', NULL, NULL, '7e995082-ce4c-40d2-8a6d-54a3d3b48c69', '4d0dd9db-b777-4e8e-97ba-ef0b57534927', 'BRIDGE_DAY_LEAVE');
INSERT INTO leaves(approved, leave_ends, leave_starts, approved_on, approved_by, leave_id, user_id, leave_type)
VALUES (FALSE, '2024-4-1', '2024-3-20', NULL, NULL, 'd2fc77e5-b2df-45b0-a90a-3fccf073125a', '10762127-c6ec-41f7-a893-44529e7c6283', 'MATERNITY_LEAVE');
INSERT INTO leaves(approved, leave_ends, leave_starts, approved_on, approved_by, leave_id, user_id, leave_type)
VALUES (FALSE, '2024-2-10', '2024-2-1', NULL, NULL, '897a632c-b06f-404c-9431-6eec41c2994a', 'ca2d6dfd-8a5d-40f1-8b3b-b7b176ccdbf7', 'SICK_LEAVE');
INSERT INTO leaves(approved, leave_ends, leave_starts, approved_on, approved_by, leave_id, user_id, leave_type)
VALUES (FALSE, '2024-8-16', '2024-7-25', NULL, NULL, '5bf731cc-1a74-4e16-ade2-b649ef79a125', 'e6e47100-2fe1-4413-89cf-2df47253a3af', 'HOLIDAY');