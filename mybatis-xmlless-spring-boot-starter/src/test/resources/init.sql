create table if not exists xmlless.t_app
(
  id   int auto_increment
    primary key,
  name varchar(40) not null
);

create table if not exists xmlless.t_app_cluster
(
  id     int auto_increment
    primary key,
  app_id int          null,
  name   varchar(40)  null,
  url    varchar(255) null
);

create table if not exists xmlless.t_app_instance
(
  id         int auto_increment
    primary key,
  cluster_id int null
);

create table if not exists xmlless.t_dep
(
  id   int auto_increment
    primary key,
  name varchar(40) null
);

create table if not exists xmlless.t_dog
(
  id          int auto_increment
    primary key,
  name        varchar(100) null,
  names       json         null,
  delete_flag tinyint(1)   null,
  create_time datetime     null,
  ages        json         null
);

create table if not exists xmlless.t_role
(
  id       int auto_increment
    primary key,
  name     varchar(40) null,
  deps     json        null,
  del_flag varchar(1)  null
);

create table if not exists xmlless.t_score
(
  id         int auto_increment
    primary key,
  score      int         not null,
  student_id varchar(20) not null,
  subject_id int         not null
);

create table if not exists xmlless.t_server
(
  id          int auto_increment
    primary key,
  name        varchar(40) not null,
  ip          varchar(20) not null,
  parent_id   int         null,
  provider_id int         not null,
  `order`     int         null
);

create table if not exists xmlless.t_server_provider
(
  id   int auto_increment
    primary key,
  name varchar(40) not null,
  constraint t_server_provider_name_uindex
    unique (name)
);

create table if not exists xmlless.t_string_key_obj
(
  id   varchar(40) not null
    primary key,
  name varchar(40) null
);

create table xmlless.t_student
(
  id             varchar(20)          not null
    primary key,
  name           varchar(20)          not null,
  phone_number   varchar(20)          null,
  sex            int                  null,
  detail         json                 null,
  education      json                 null,
  favorites      json                 null,
  graduated      tinyint(1) default 0 null,
  age            int                  null,
  nick_names     json                 null,
  email          varchar(100)         null,
  birthday       date                 null,
  state          varchar(20)          null,
  create_time    datetime             null,
  user_id        int                  null,
  create_user_id int                  null,
  update_user_id int                  null,
  del_flag       tinyint(1)           null,
  school_id      varchar(40)          null
);

create table if not exists xmlless.t_subject
(
  id   int auto_increment
    primary key,
  name varchar(20) not null,
  constraint t_subject_name_uindex
    unique (name)
);

create table if not exists xmlless.t_user
(
  id      int auto_increment
    primary key,
  name    varchar(40)   not null,
  age     int default 0 not null,
  deleted tinyint(1)    not null,
  roles   json          null
);

create table xmlless.xx
(
  id         varchar(40) null,
  name       varchar(40) null,
  location   varchar(40) null,
  student_id varchar(40) null
);

