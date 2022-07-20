drop table if exists statement;
create table statement (id BINARY(16) not null, ending_balance decimal(19,2), loan_id BINARY(16), starting_balance decimal(19,2), statement text, statement_date date, primary key (id));
alter table statement add constraint UKqta0kx9qxw2yrb8cmgg0t4d0c unique (loan_id, statement_date);