create table employees
(
    id      bigserial not null primary key,
    chatid      bigint,
    emp_name    text,
    emp_mail    text,
    emp_role    text,
    emp_status  text,
    activation_code text
);



