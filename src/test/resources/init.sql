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

-- create table t_server --
create table t_server
(
  id int auto_increment
    primary key,
  name varchar(40) not null,
  ip varchar(20) not null,
  parent_id int null,
  provider_id int not null
);


-- create table t_server_provider --
create table t_server_provider
(
  id int auto_increment,
  name VARCHAR(40) not null,
  constraint t_server_provider_id_pk
    primary key (id)
);

create unique index t_server_provider_name_uindex
	on t_server_provider (name);

-- create table t_score --
CREATE TABLE t_score
(
  id         INT AUTO_INCREMENT
    PRIMARY KEY,
  score      INT         NOT NULL,
  student_id VARCHAR(20) NOT NULL,
  subject_id INT         NOT NULL
);

-- create table t_subject --
CREATE TABLE t_subject
(
  id   INT AUTO_INCREMENT
    PRIMARY KEY,
  name VARCHAR(20) NOT NULL,
  CONSTRAINT t_subject_name_uindex
    UNIQUE (name)
);

CREATE TABLE t_user
(
  id      INT AUTO_INCREMENT
    PRIMARY KEY,
  name    VARCHAR(40) NOT NULL,
  age     INT         NOT NULL,
  deleted TINYINT(1)  NOT NULL
);

