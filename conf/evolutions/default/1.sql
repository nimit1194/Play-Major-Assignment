# --- !Ups

CREATE TABLE if not exists userdata (
id      serial not null primary key,
firstname varchar(500) not null,
middlename varchar(500),
lastname varchar(500) not null,
useremail varchar(500) not null,
password varchar(500) not null,
mobileno bigint not null,
gender varchar(8) not null,
age int not null,
isAdmin boolean not null,
isEnable boolean not null
);

CREATE table assignment(
id serial not null primary key,
objective  varchar(500),
instructions varchar(100000)
);

CREATE TABLE hobby(
id serial primary key,
hobbyText varchar(400) not null
);

CREATE TABLE usertohobby(
id serial not null primary key,
userId  int REFERENCES userdata(id),
hobbyid int REFERENCES hobby(id)
);

# --- !Downs

DROP TABLE userData;
DROP TABLE assignment;
DROP TABLE hobby;
DROP TABLE usertohobby;