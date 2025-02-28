INSERT INTO users(id,username, password, firstname, lastname, email) VALUES(4,"admin", "admin", "admin", "adminkovic", "admin@mail.com");
-- First, insert sample users
INSERT INTO users (id,username, password, firstname, lastname, email)
VALUES
    (1, 'john_doe','sifra','john','doe', 'john@example.com'),
    (2, 'jane_smith','sifra','jane','smith', 'jane@example.com'),
    (3, 'mike_wilson','sifra','mike','wilson', 'mike@example.com');

-- Then, insert programs with sample image data
-- Note: Using HEXTORAW for binary data, though actual images would have much more data
INSERT INTO program (id, name, image_data, creator_id,followers_number)
VALUES
    (1, 'Program 1',
     x'89504E470D0A1A0A', -- This is a minimal PNG header
     1,1000),

    (2, 'Program 2',
     x'474946383961', -- This is a minimal GIF header
     1,2000),

    (3, 'Program 3',
     x'89504E470D0A1A0A',
     2,3000),

    (4, 'Program 4',
     x'474946383961',
     2,4000),

    (5, 'Program 5',
     x'89504E470D0A1A0A',
     3,5000),
    (6, 'Program 6',
     x'89504E470D0A1A0A', -- This is a minimal PNG header
     1,1000),

    (7, 'Program 7',
     x'474946383961', -- This is a minimal GIF header
     1,2000),

    (8, 'Program 8',
     x'89504E470D0A1A0A',
     2,3000),

    (9, 'Program 9',
     x'474946383961',
     2,4000),

    (10, 'Program 10',
     x'89504E470D0A1A0A',
     3,5000),
    (11, 'Program 11',
     x'89504E470D0A1A0A', -- This is a minimal PNG header
     1,1000),

    (12, 'Program 12',
     x'474946383961', -- This is a minimal GIF header
     1,2000),

    (13, 'Program 13',
     x'89504E470D0A1A0A',
     2,3000),

    (14, 'Program 14',
     x'474946383961',
     2,4000),

    (15, 'Program 15',
     x'89504E470D0A1A0A',
     3,5000);

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
                                                                                            (1,true,false,'Rpe','rpe',false),
                                                                                            (2,true,true,'Rpe Range','rpe',true);