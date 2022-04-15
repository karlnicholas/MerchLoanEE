-- noinspection SqlDialectInspectionForFile

-- noinspection SqlNoDataSourceInspectionForFile

create table account (id BINARY(16) not null, create_date date, customer varchar(255), primary key (id));
create table loan (id BINARY(16) not null, funding decimal(19,2), interest_rate decimal(19,2), loan_state integer, monthly_payments decimal(19,2), months integer, start_date date, statement_dates varchar(255), account_id BINARY(16), primary key (id));
create table loan_state (loan_id BINARY(16) not null, balance decimal(19,2), current_row_num integer, start_date date, primary key (loan_id));
create table register_entry (id BINARY(16) not null, credit decimal(19,2), date date, debit decimal(19,2), description varchar(255), loan_id BINARY(16), row_num integer, primary key (id));
create index IDXih2un6jh7r35uh1qnof7m6b4t on register_entry (loan_id);
create index IDXsx2r7a52m9mp1aeajyo7eb9pj on register_entry (row_num);
alter table register_entry add constraint UK5ib7n0y8jh6o827n8ssy2h7y7 unique (loan_id, row_num);
alter table loan add constraint FKgv9cgsh4k76wmaf83ktoekpub foreign key (account_id) references account;