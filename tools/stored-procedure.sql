USE moviedb;
DROP PROCEDURE IF EXISTS add_movie;
DELIMITER $$
CREATE PROCEDURE add_movie(
    IN movie_title VARCHAR(100),
    IN movie_year INT,
    IN movie_director VARCHAR(100),
    IN movie_cost DECIMAL(5,2),
    IN new_star BOOL,
    IN star_id VARCHAR(10),
    IN star_name VARCHAR(100),
    IN star_dob INT,
    IN new_genre BOOL,
    IN genre_name VARCHAR(32))
BEGIN
    DECLARE max_movie_id VARCHAR(10);
    DECLARE max_movie_counter INT;
    DECLARE new_movie_id VARCHAR(10);
    DECLARE max_star_id VARCHAR(10);
    DECLARE max_star_counter INT;
    DECLARE new_star_id VARCHAR(10);
    DECLARE genre_id INT;
    SET max_movie_id = (SELECT MAX(id) FROM movies);
--    SELECT CONCAT('max_movie_id = ', max_movie_id);
    SET max_movie_counter = CAST(SUBSTR(max_movie_id, 3) AS UNSIGNED);
--    SELECT CONCAT('max_movie_counter = ', max_movie_counter);
    SET new_movie_id = CONCAT('tt', LPAD(CAST((max_movie_counter + 1) AS CHAR(7)), 7, '0'));
--    SELECT CONCAT('new_movie_id = ', new_movie_id);
    SELECT 'Inserting into movies';
    INSERT INTO movies (id,title,year,director,cost) VALUES (new_movie_id,movie_title,movie_year,movie_director,movie_cost);
    SELECT * FROM movies WHERE title = movie_title AND year = movie_year AND director = movie_director;
    IF new_star = TRUE THEN
        SET max_star_id = (SELECT MAX(id) FROM stars);
--        SELECT CONCAT('max_star_id = ', max_star_id);
        SET max_star_counter = CAST(SUBSTR(max_star_id, 3) AS UNSIGNED);
--        SELECT CONCAT('max_star_counter = ', max_star_counter);
        SET new_star_id = CONCAT('nm', LPAD(CAST((max_star_counter + 1) AS CHAR(7)), 7, '0'));
        SELECT CONCAT('new_star_id = ', new_star_id);
        SELECT 'Inserting into stars';
        INSERT INTO stars (id,name,birthyear) VALUES (new_star_id,star_name,star_dob);
        SELECT * FROM stars WHERE name = star_name AND birthyear = star_dob;
    ELSE
        SET new_star_id = star_id;
        SELECT CONCAT('Picked up existing star ', new_star_id);
    END IF;
    SELECT 'Inserting into stars_in_movies';
    INSERT INTO stars_in_movies (starId, movieId) VALUES (new_star_id, new_movie_id);
    SELECT * FROM stars_in_movies WHERE starId = new_star_id AND movieId = new_movie_id;
    IF new_genre = TRUE THEN
        SELECT 'Inserting into genres';
        INSERT INTO genres (name) VALUES (genre_name);
        SET genre_id = LAST_INSERT_ID();
        SELECT * FROM genres WHERE name = genre_name;
    ELSE
        SET genre_id = (SELECT id FROM genres WHERE name = genre_name);
        SELECT CONCAT('Picked up existing genre ', genre_name, ' (', genre_id, ')');
    END IF;
--    SELECT CONCAT('genre_id = ', genre_id);
    SELECT 'Inserting into genres_in_movies';
    INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, new_movie_id);
    SELECT * FROM genres_in_movies WHERE genreId = genre_id AND movieId = new_movie_id;
END $$
DELIMITER ;