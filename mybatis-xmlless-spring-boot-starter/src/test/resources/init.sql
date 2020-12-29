create table t_app
(
  id int auto_increment
    primary key,
  name varchar(40) not null
);

create table t_app_cluster
(
  id int auto_increment
    primary key,
  app_id int null,
  name varchar(40) null,
  url varchar(255) null
);

create table t_app_instance
(
  id int auto_increment
    primary key,
  cluster_id int null
);

create table t_score
(
  id int auto_increment
    primary key,
  score int not null,
  student_id varchar(20) not null,
  subject_id int not null
);

create table t_server
(
  id int auto_increment
    primary key,
  name varchar(40) not null,
  ip varchar(20) not null,
  parent_id int null,
  provider_id int not null
);

create table t_server_provider
(
  id int auto_increment
    primary key,
  name varchar(40) not null,
  constraint t_server_provider_name_uindex
    unique (name)
);

create table t_student
(
  id varchar(20) not null
    primary key,
  name varchar(20) not null,
  phone_number varchar(20) not null,
  sex int not null,
  detail json null,
  education json null,
  favorites json null,
  graduated tinyint(1) default 0 not null,
  nick_names json null,
  email varchar(100) null,
  birthday date null,
  state varchar(20) null
);

create table t_subject
(
  id int auto_increment
    primary key,
  name varchar(20) not null,
  constraint t_subject_name_uindex
    unique (name)
);

create table t_user
(
  id int auto_increment
    primary key,
  name varchar(40) not null,
  age int default 0 not null,
  deleted tinyint(1) not null
);

