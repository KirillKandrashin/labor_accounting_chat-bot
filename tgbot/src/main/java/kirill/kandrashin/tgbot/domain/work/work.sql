create table works
(
    id          bigserial not null primary key,
    start_work        timestamp,
    end_work        timestamp,
    description text,
    task_id bigint references tasks(id)
);


