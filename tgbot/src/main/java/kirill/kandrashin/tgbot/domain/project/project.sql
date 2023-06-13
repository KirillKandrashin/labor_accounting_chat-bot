create table projects
(
    id      bigserial not null primary key,
    title    text,
    status  text,
    start_date    date,
    end_date date,
    deadline      date
);


