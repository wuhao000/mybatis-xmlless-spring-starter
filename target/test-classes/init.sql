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

