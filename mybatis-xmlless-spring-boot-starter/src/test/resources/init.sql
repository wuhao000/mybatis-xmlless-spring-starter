CREATE TABLE IF NOT EXISTS xmlless.t_app
(
  id   INT AUTO_INCREMENT
    PRIMARY KEY,
  name VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS xmlless.t_app_cluster
(
  id     INT AUTO_INCREMENT
    PRIMARY KEY,
  app_id INT          NULL,
  name   VARCHAR(40)  NULL,
  url    VARCHAR(255) NULL
);

CREATE TABLE IF NOT EXISTS xmlless.t_app_instance
(
  id         INT AUTO_INCREMENT
    PRIMARY KEY,
  cluster_id INT NULL
);

CREATE TABLE IF NOT EXISTS xmlless.t_dep
(
  id   INT AUTO_INCREMENT
    PRIMARY KEY,
  name VARCHAR(40) NULL
);

CREATE TABLE IF NOT EXISTS xmlless.t_dog
(
  id          INT AUTO_INCREMENT
    PRIMARY KEY,
  name        VARCHAR(100) NULL,
  names       JSON         NULL,
  delete_flag TINYINT(1)   NULL,
  create_time DATETIME     NULL,
  ages        JSON         NULL
);

CREATE TABLE IF NOT EXISTS xmlless.t_role
(
  id       INT AUTO_INCREMENT
    PRIMARY KEY,
  name     VARCHAR(40) NULL,
  deps     JSON        NULL,
  del_flag VARCHAR(1)  NULL
);

CREATE TABLE IF NOT EXISTS xmlless.t_score
(
  id         INT AUTO_INCREMENT
    PRIMARY KEY,
  score      INT         NOT NULL,
  student_id VARCHAR(20) NOT NULL,
  subject_id INT         NOT NULL
);

CREATE TABLE IF NOT EXISTS xmlless.t_server
(
  id          INT AUTO_INCREMENT
    PRIMARY KEY,
  name        VARCHAR(40) NOT NULL,
  ip          VARCHAR(20) NOT NULL,
  parent_id   INT         NULL,
  provider_id INT         NOT NULL,
  `order`     INT         NULL
);

CREATE TABLE IF NOT EXISTS xmlless.t_server_provider
(
  id   INT AUTO_INCREMENT
    PRIMARY KEY,
  name VARCHAR(40) NOT NULL,
  CONSTRAINT t_server_provider_name_uindex
    UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS xmlless.t_string_key_obj
(
  id   VARCHAR(40) NOT NULL
    PRIMARY KEY,
  name VARCHAR(40) NULL
);

CREATE TABLE xmlless.t_student
(
  id             VARCHAR(20)          NOT NULL
    PRIMARY KEY,
  name           VARCHAR(20)          NOT NULL,
  phone_number   VARCHAR(20)          NULL,
  sex            INT                  NULL,
  detail         JSON                 NULL,
  education      JSON                 NULL,
  favorites      JSON                 NULL,
  graduated      TINYINT(1) DEFAULT 0 NULL,
  age            INT                  NULL,
  nick_names     JSON                 NULL,
  email          VARCHAR(100)         NULL,
  birthday       DATE                 NULL,
  state          VARCHAR(20)          NULL,
  create_time    DATETIME             NULL,
  user_id        INT                  NULL,
  create_user_id INT                  NULL,
  update_user_id INT                  NULL,
  del_flag       TINYINT(1)           NULL,
  school_id      VARCHAR(40)          NULL
);

CREATE TABLE IF NOT EXISTS xmlless.t_subject
(
  id   INT AUTO_INCREMENT
    PRIMARY KEY,
  name VARCHAR(20) NOT NULL,
  CONSTRAINT t_subject_name_uindex
    UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS xmlless.t_user
(
  id      INT AUTO_INCREMENT
    PRIMARY KEY,
  name    VARCHAR(40)   NOT NULL,
  age     INT DEFAULT 0 NOT NULL,
  deleted TINYINT(1)    NOT NULL,
  roles   JSON          NULL
);

CREATE TABLE xmlless.xx
(
  id         VARCHAR(40) NULL,
  name       VARCHAR(40) NULL,
  wz         VARCHAR(40) NULL,
  student_id VARCHAR(40) NULL
);
