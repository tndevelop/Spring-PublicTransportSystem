insert into user_details (id, address, date_of_birth, name, password, telephone_number)
values (0, 'Via Roma 20', '2022-05-09', 'Gino', 'Gino', '12345656');



insert into ticket_purchased (ticket_id, expiry, issued_at, jws, used, user_id, zone_id)
values (0, '2023-05-09 12:46:51.000000', '2022-05-09 12:46:51.000000', '', false, 0, 0);

alter table user_details, ticket_purchased owner to postgres;