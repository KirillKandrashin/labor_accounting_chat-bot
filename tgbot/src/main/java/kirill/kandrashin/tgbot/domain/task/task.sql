create table tasks
(
    id             bigserial not null primary key,
    description   text,
    difficulty int,
    planned_labor_costs int,
    status  text,
    created_date date,
    start_date date,
    planned_end date,
    end_date date,
    employee_id bigint references employees(id),
    project_id bigint references projects(id)
);


