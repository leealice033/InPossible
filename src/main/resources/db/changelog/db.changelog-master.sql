--liquibase formatted sql

--changeset swanky:1
create table HIBERNATE_SEQUENCES (
  SEQUENCE_NAME varchar(20) not null primary key,
  NEXT_VAL bigint not null
);
--rollback drop table HIBERNATE_SEQUENCES;

create table USER (
  USER_INDEX bigint not null primary key,
  ID varchar(36) not null,
  NAME VARCHAR(36) not null,
  EMAIL VARCHAR(256) not null,
  PASSWORD varchar(30) not null,
  ROLES varchar(50) not null
);
--rollback drop table USER;

create table PROJECT (
  PROJECT_INDEX bigint not null primary key,
  USER_INDEX bigint not null,
  ID varchar(100) not null,
  NAME varchar(30),
  TYPE varchar(30)
);
--rollback drop table PROJECT;

create table CSV (
  CSV_INDEX bigint not null primary key,
  PROJECT_INDEX bigint not null,
  ID varchar(100) not null,
  STAGE varchar(54) not null,
  LABEL varchar(54)
);
--rollback drop table CSV;

create table MODEL (
  MODEL_INDEX bigint not null primary key,
  PROJECT_INDEX bigint not null,
  ID varchar(54) not null,
  NAME varchar(54),
  METHOD varchar(54) not null
);
--rollback drop table MODEL;

create table API (
  API_INDEX bigint not null primary key,
  USER_INDEX bigint not null,
  ID varchar(36) not null,
  NAME varchar(54) not null,
  DESCRIPTION varchar(200),
  PATH varchar(54) not null,
);
--rollback drop table API;

