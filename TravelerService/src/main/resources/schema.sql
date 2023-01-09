-- auto-generated definition
SELECT 'CREATE DATABASE traveler' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'traveler');

\connect traveler; -- This isn't recognised but is meant to be the way to change to the traveler database


-- create traveler database
--create database traveler if not exists;
comment on database traveler is 'Traveler Database';

-- create public schema
create schema public;
comment on schema public is 'standard public schema';
alter schema public owner to postgres;

-- create user_details table
create table user_details
(
    id               bigint       not null
        constraint user_detail_pk
            primary key,
    address          varchar(255) not null,
    age              integer,
    date_of_birth    varchar(255) not null,
    name             varchar(255) not null,
    telephone_number varchar(255) not null
);

INSERT INTO user_details (id, address, age, date_of_birth, name, telephone_number) VALUES (1, '0', 0, '2022-05-27', 'Name1', '0');

comment on table user_details is 'Users of traveler service referenced to the users in login service by id';

-- create ticket_purchased table
create table ticket_purchased
(
    ticket_id bigint       not null
        constraint ticket_purchased_pk
            primary key,
    expiry    timestamp    not null,
    issued_at timestamp    not null,
    jws     text         not null,
    type      varchar(255),
    zone_id   varchar(255) not null,
    user_id   bigint       not null,
    used      boolean      not null
);

comment on table ticket_purchased is 'Tickets purchased by users';