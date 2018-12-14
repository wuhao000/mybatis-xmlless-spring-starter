CREATE TABLE t_student
(
  id           VARCHAR(20) NOT NULL,
  name         VARCHAR(20) NOT NULL,
  phone_number VARCHAR(20) NOT NULL,
  sex          INT         NOT NULL,
  CONSTRAINT t_student_id_uindex
    UNIQUE (id)
);

ALTER TABLE t_student
  ADD PRIMARY KEY (id);

CREATE TABLE t_score
(
  id         INT AUTO_INCREMENT
    PRIMARY KEY,
  score      INT         NOT NULL,
  student_id VARCHAR(20) NOT NULL,
  subject_id INT         NOT NULL
);

CREATE TABLE t_subject
(
  id   INT AUTO_INCREMENT
    PRIMARY KEY,
  name VARCHAR(20) NOT NULL,
  CONSTRAINT t_subject_name_uindex
    UNIQUE (name)
);

