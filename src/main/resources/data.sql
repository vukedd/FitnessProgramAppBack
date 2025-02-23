INSERT INTO users(id,username, password, firstname, lastname, email) VALUES(4,"admin", "admin", "admin", "adminkovic", "admin@mail.com");
-- First, insert sample users
INSERT INTO users (id,username, password, firstname, lastname, email)
VALUES
    (1, 'john_doe','sifra','john','doe', 'john@example.com'),
    (2, 'jane_smith','sifra','jane','smith', 'jane@example.com'),
    (3, 'mike_wilson','sifra','mike','wilson', 'mike@example.com');

-- Then, insert programs with sample image data
-- Note: Using HEXTORAW for binary data, though actual images would have much more data
INSERT INTO program (id, name, image_data, creator_id)
VALUES
    (1, 'Program 1',
     x'89504E470D0A1A0A', -- This is a minimal PNG header
     1),

    (2, 'Program 2',
     x'474946383961', -- This is a minimal GIF header
     1),

    (3, 'Program 3',
     x'89504E470D0A1A0A',
     2),

    (4, 'Program 4',
     x'474946383961',
     2),

    (5, 'Program 5',
     x'89504E470D0A1A0A',
     3);

-- Reset auto-increment
ALTER TABLE program AUTO_INCREMENT = 6;
ALTER TABLE users AUTO_INCREMENT = 5;