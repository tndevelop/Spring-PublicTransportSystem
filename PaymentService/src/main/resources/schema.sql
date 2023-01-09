
--DROP DATABASE IF EXISTS payment;
--CREATE DATABASE payment;

create table if not exists transaction
(
    id                serial
        constraint transaction_pk
            primary key,
    user_id           int              not null,
    total_cost        double precision not null,
    number_of_tickets int              not null,
    ticket_id         int              not null,
    timestamp         timestamp
);

create unique index if not exists transaction_id_uindex
    on transaction (id);


-- create user_details table
create table if not exists user_details
(
    id               integer     not null
        constraint user_detail_pk
            primary key,
    address          varchar    not null,
    age              integer,
    date_of_birth    varchar    not null,
    name             varchar    not null,
    telephone_number varchar    not null
);

delete from user_details where id = 0;
insert into user_details(id, address, age, date_of_birth, name, telephone_number) values
    (0, '0', 0, '2002-12-12', 'Name', '0') ;

create unique index if not exists user_detail_id_uindex
    on transaction (id);