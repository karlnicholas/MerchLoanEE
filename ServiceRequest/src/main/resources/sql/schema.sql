drop table if exists service_request;
create table service_request (id BINARY(16) not null, local_date_time timestamp, request text, request_type varchar(1000), retry_count integer, status integer, status_message varchar(10000), primary key (id))
