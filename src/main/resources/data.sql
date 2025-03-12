INSERT INTO users(id,username, password, firstname, lastname, email, enabled) VALUES(4,'admin', '$2a$10$sk586e6x3Xn/Y1ZmgQGZf.Q3MbRrt0WjZuvgFZ75YYiXtX3rsI4lW', 'admin', 'adminkovic', 'admin@mail.com', true);
-- First, insert sample users
INSERT INTO users (id,username, password, firstname, lastname, email, enabled)
VALUES
    (1, 'john_doe','sifra','john','doe', 'john@example.com', false),
    (2, 'jane_smith','sifra','jane','smith', 'jane@example.com', false),
    (3, 'mike_wilson','sifra','mike','wilson', 'mike@example.com', false);

-- Then, insert programs with sample image data
-- Note: Using HEXTORAW for binary data, though actual images would have much more data
insert into fitnessprogramapp.program (followers_number, creator_id, id, title, image_data)
values  (1000, 1, 1, 'Program 1', 0x89504E470D0A1A0A),
        (2000, 1, 2, 'Program 2', 0x474946383961),
        (3000, 2, 3, 'Program 3', 0x89504E470D0A1A0A),
        (4000, 2, 4, 'Program 4', 0x474946383961),
        (5000, 3, 5, 'Program 5', 0x89504E470D0A1A0A),
        (1000, 1, 6, 'Program 6', 0x89504E470D0A1A0A),
        (2000, 1, 7, 'Program 7', 0x474946383961),
        (3000, 2, 8, 'Program 8', 0x89504E470D0A1A0A),
        (4000, 2, 9, 'Program 9', 0x474946383961),
        (5000, 3, 10, 'Program 10', 0x89504E470D0A1A0A),
        (1000, 1, 11, 'Program 11', 0x89504E470D0A1A0A),
        (2000, 1, 12, 'Program 12', 0x474946383961),
        (3000, 2, 13, 'Program 13', 0x89504E470D0A1A0A),
        (4000, 2, 14, 'Program 14', 0x474946383961),
        (5000, 3, 15, 'Program 15', 0x89504E470D0A1A0A),
        (0, 1, 16, 'asdfas', null);

-- Reset auto-increment
ALTER TABLE program AUTO_INCREMENT = 6;
ALTER TABLE users AUTO_INCREMENT = 5;

INSERT INTO review (id, comment, rating,  deleted, created_at, reviewer_id) VALUES
                                                                                       (41, 'Amazing experience! Learned so much about cutting-edge technology.', 5, false, '2023-09-15 10:00:00', 1),
                                                                                       (42, 'Well-organized but I felt the sessions were too long.', 4, false, '2023-09-16 11:00:00', 2),
                                                                                       (43, 'The location was too crowded for the number of attendees.', 3,  false, '2023-09-17 12:00:00', 1),
                                                                                       (44, 'Great event! Excellent speakers and engaging sessions.', 5,  false, '2023-09-18 13:00:00', 1);
insert into program_reviews(program_id,review_id) values
                                                      (1,41),
                                                      (1,42),
                                                      (2,43),
                                                      (2,44);
insert into volume_metric(id,is_range,title,metric_symbol) values
                                                                                            (1,false,'Reps','reps'),
                                                                                            (2,true,'Reps Range','reps');
insert into intensity_metric(id,minimum_intensity,maximum_intensity,title,metric_symbol,is_range) values
                                                                                            (1,0,10,'Rpe','rpe',false),
                                                                                            (2,0,10,'Rpe Range','rpe',true);
insert into exercise(id,title,description) values
                                               (1,'Ez bar curl','curl description'),
                                               (2,'Leg extension','extension description'),
                                               (3,'Let curls','leg curls extension');