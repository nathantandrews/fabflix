DROP DATABASE IF EXISTS moviedb;
CREATE DATABASE moviedb;
USE moviedb;

CREATE TABLE movies
(
    id VARCHAR(10) NOT NULL,
    title TEXT NOT NULL,
    year INT NOT NULL,
    director VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    FULLTEXT (title)
);

CREATE TABLE stars
(
    id VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    birthYear int,
    PRIMARY KEY (id)
);

CREATE TABLE stars_in_movies
(
    starId varchar(10) NOT NULL,
    movieId varchar(10) NOT NULL,
    FOREIGN KEY (starId) REFERENCES stars(id),
    FOREIGN KEY (movieId) REFERENCES movies(id),
    PRIMARY KEY(starId,movieId)
);

CREATE TABLE genres
(
    id int AUTO_INCREMENT NOT NULL,
    name varchar(32),
    PRIMARY KEY(id)
);

CREATE TABLE genres_in_movies
(
    genreId int NOT NULL,
    movieId varchar(10) NOT NULL,
    FOREIGN KEY (genreId) REFERENCES genres(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE creditcards
(
    id varchar(20) NOT NULL,
    firstName varchar(50) NOT NULL,
    lastName varchar(50) NOT NULL,
    expiration DATE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE customers
(
    id int AUTO_INCREMENT NOT NULL,
    firstName varchar(50) NOT NULL,
    lastName varchar(50) NOT NULL,
    ccId varchar(20) NOT NULL,
    address varchar(200) NOT NULL,
    email varchar(50) NOT NULL,
    password varchar(20) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (ccId) REFERENCES creditcards(id)
);

CREATE TABLE sales
(
    id int AUTO_INCREMENT NOT NULL,
    customerId int NOT NULL,
    movieId varchar(10) NOT NULL,
    saleDate DATE NOT NULL,
    FOREIGN KEY (movieId) REFERENCES movies(id),
    PRIMARY KEY(id),
    FOREIGN KEY (customerId) REFERENCES customers(id)
);


CREATE TABLE ratings
(
    movieId varchar(10) NOT NULL,
    rating float NOT NULL,
    numVotes int NOT NULL,
    FOREIGN KEY (movieId) REFERENCES movies(id)
);
CREATE TABLE employees
(
    email varchar(50) primary key,
    password varchar(20) not null,
    fullname varchar(100)
);
INSERT INTO employees (email, password, fullname) VALUES ('classta@email.edu', 'classta', 'TA CS122B');

CREATE INDEX idx_movie_ratings ON ratings(rating DESC);
# CREATE INDEX idx_movie_title ON movies(title);
CREATE INDEX idx_movie_director ON movies(director);
CREATE INDEX idx_star_name ON stars(name);