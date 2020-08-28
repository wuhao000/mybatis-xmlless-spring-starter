CREATE TABLE IF NOT EXISTS t_score
(
  id         INT AUTO_INCREMENT
    PRIMARY KEY,
  score      INT         NOT NULL,
  student_id VARCHAR(20) NOT NULL,
  subject_id INT         NOT NULL
);

CREATE TABLE IF NOT EXISTS t_server
(
  id          INT AUTO_INCREMENT
    PRIMARY KEY,
  name        VARCHAR(40) NOT NULL,
  ip          VARCHAR(20) NOT NULL,
  parent_id   INT         NULL,
  provider_id INT         NOT NULL,
  CONSTRAINT t_server_name_uindex
    UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS t_server_provider
(
  id   INT AUTO_INCREMENT
    PRIMARY KEY,
  name VARCHAR(40) NOT NULL,
  CONSTRAINT t_provider_name_uindex
    UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS t_student
(
  id           VARCHAR(20)          NOT NULL,
  name         VARCHAR(40)          NOT NULL,
  phone_number VARCHAR(20)          NOT NULL,
  sex          INT                  NOT NULL,
  graduated    TINYINT(1) DEFAULT 0 NOT NULL,
  CONSTRAINT t_student_id_uindex
    UNIQUE (id)
);

ALTER TABLE t_student
  ADD PRIMARY KEY (id);

CREATE TABLE IF NOT EXISTS t_subject
(
  id   INT AUTO_INCREMENT
    PRIMARY KEY,
  name VARCHAR(20) NOT NULL,
  CONSTRAINT t_subject_name_uindex
    UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS t_user
(
  id      INT AUTO_INCREMENT
    PRIMARY KEY,
  name    VARCHAR(40) NOT NULL,
  age     INT         NOT NULL,
  deleted TINYINT(1)  NOT NULL
);
