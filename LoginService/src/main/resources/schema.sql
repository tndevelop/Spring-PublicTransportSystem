-- auto-generated definition

--CREATE DATABASE login;


create table if not exists users
(
    id       bigint       not null
        primary key,
    active   boolean      not null,
    email    varchar(255) not null
        constraint uk_6dotkott2kjsp8vw4d0m25fb7
            unique
        constraint users_email_check
            check ((email)::text > ''::text),
    password varchar(255) not null
        constraint users_password_check
            check ((password)::text > ''::text),
    role      integer,
    authority      integer,
    username varchar(255) not null
        constraint uk_r43af9ap4edm43mmtq01oddj6
            unique
        constraint users_username_check
            check ((username)::text > ''::text)
);


create table if not exists activation
(
    id                  uuid    not null
        primary key,
    activation_code     integer not null,
    activation_deadline timestamp,
    attempt_counter     integer
        constraint activation_attempt_counter_check
            check (attempt_counter >= 0),
    user_id             bigint
        constraint fk962x2w5lhpof6gmaply4y55wk
            references users
);


