
--DROP DATABASE IF EXISTS catalogue;
--CREATE DATABASE catalogue;


CREATE TABLE IF NOT EXISTS ticket_catalogue(
    id          serial
        constraint ticket_catalogue_pk
            primary key,
    price       double precision not null,
    type        varchar unique not null,
    minimum_age integer,
    maximum_age integer,
    zones       varchar,
    validity_duration  double precision default 60
);

create table if not exists order_catalogue (
    status            varchar           not null,
    total_cost        double precision  not null,
    number_of_tickets integer           not null,
    user_id           double precision  not null,
    id                serial
        constraint order_catalogue_pk
            primary key,
    ticket_id         integer default 0 not null
);

--create unique index if not exists order_catalogue_id_uindex
    --on order_catalogue (id);


-- create user_details table
create table if not exists user_details
(
    id               integer
        constraint user_detail_pk
            primary key,
    address          varchar not null,
    age              integer,
    date_of_birth    varchar not null,
    name             varchar not null,
    telephone_number varchar not null
);


delete from user_details where id = 0;
insert into user_details(id, address, age, date_of_birth, name, telephone_number) values
    (0, '0', 0, '2002-12-12', 'Name', '0') ;

--create unique index if not exists user_detail_id_uindex
--    on transaction (id);


