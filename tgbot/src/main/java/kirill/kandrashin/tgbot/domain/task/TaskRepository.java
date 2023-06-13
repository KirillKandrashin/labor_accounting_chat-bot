package kirill.kandrashin.tgbot.domain.task;

import kirill.kandrashin.tgbot.domain.employee.Employee;
import kirill.kandrashin.tgbot.domain.project.Project;
import kirill.kandrashin.tgbot.domain.work.Work;
import kirill.kandrashin.tgbot.domain.work.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class TaskRepository {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    public void addTask(Employee employee, String description){
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("description", description);
        paramMap.put("created_date", new Date());
        paramMap.put("status", "Ожидание");
        paramMap.put("employee_id", employee.getId());
        namedJdbc.update("insert into tasks(description, created_date, status, employee_id) values (:description, :created_date, :status, :employee_id)", paramMap);
    }

    public Task getTaskByDescr(String description){
        return jdbc.query("select * from tasks where description = ?", (rs, row) -> {
            return new Task(rs.getLong("id"), rs.getString("description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"), rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<Work>());
        }, description).stream().findFirst().orElse(new Task());
    }

    public void newTaskByProject(Date created_date, String status, Long project_id){
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("status", status);
        paramMap.put("created_date", created_date);
        paramMap.put("project_id", project_id);
        namedJdbc.update("insert into tasks(status, created_date, project_id) values (:status, :created_date, :project_id)", paramMap);
    }

    public Task findNewTask(){
        return jdbc.query("select * from tasks where description IS NULL", (rs, row) -> {
            return new Task(rs.getLong("id"), rs.getString("description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"), rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<Work>());
        }).stream().findFirst().orElse(new Task());
    }

    public void updateByDescr(Long id, String description){
        jdbc.update("update tasks set description = ? where id = ?",
                description, id);
    }

    public void updateByPlannedLaborCosts(Long id, Integer planned_labor_costs){
        jdbc.update("update tasks set planned_labor_costs = ? where id = ?",
                planned_labor_costs, id);
    }

    public void updateByEmployee(Long id, Long employee_id){
        jdbc.update("update tasks set employee_id = ? where id = ?",
                employee_id, id);
    }

    public void updateByDeadline(Long id, Date planned_end){
        jdbc.update("update tasks set planned_end = ? where id = ?",
                planned_end, id);
    }

    public void deleteTask(Task task){
        jdbc.update("delete from tasks where id = (?)",
                task.getId());
    }

    public void updateByDifficulty(Long id, Integer difficulty){
        jdbc.update("update tasks set difficulty = ? where id = ?",
                difficulty, id);
    }

    public List<Task> tasksByEmployeeId(Long id){
        return jdbc.query("select * from tasks where employee_id = ?", (rs, row) -> {
            return new Task(rs.getLong("id"), rs.getString("description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"),rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<Work>());
        }, id);
    }

    public void changeStatusById(Long id, String status){
        jdbc.update("update tasks set status = ? where id = ?",
                status, id);
    }

    public Task taskById(Long id){
        return jdbc.query("select * from tasks where id = ?", (rs, row) -> {
            return new Task(rs.getLong("id"), rs.getString("description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"),rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<Work>());
        }, id).stream().findFirst().orElse(new Task());
    }

    public List<Task> getOverdueTasks(){
        return jdbc.query("select * from tasks where ((end_date is null and planned_end < DATE(?)) or (end_date > planned_end))", (rs, row) -> {
            return new Task(rs.getLong("id"), rs.getString("description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"),rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<Work>());
        }, new Date());

    }

    public Map<Task, String> getOverdueTasks(Date from, Date to){
        List<Task> overdue_tasks = jdbc.query("select * from tasks where (DATE(planned_end) between ? and ?) and ((end_date is null and planned_end < ?) or (end_date > planned_end))", (rs, row) -> {
            return new Task(rs.getLong("id"), rs.getString("description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"),rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<Work>());
        }, from, to, new Date());
        Map<Task, String> tasks = new HashMap<Task, String>();
        for (Task task : overdue_tasks){
            String name = jdbc.query("select e.emp_name from employees as e left join tasks as t on e.id = t.employee_id where t.id = (?)", (rs, row) -> {
                return rs.getString("emp_name");
            }, task.getId()).stream().findFirst().orElse("");
            tasks.put(task, name);
        }
        return tasks;
    }

    public Map<Task, Double> getTimePerTaskPerProject(Project project){
        List<Task> all_tasks = jdbc.query("select * from tasks where project_id = (?)", (rs, row) -> {
            return new Task(rs.getLong("id"), rs.getString("description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"),rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<Work>());
        }, project.getId());
        Map<Task, Double> tasks = new HashMap<Task, Double>();
        for (Task task : all_tasks){
            List<Double> hours = jdbc.query("select w.hours from works as w left join tasks as t on t.id = w.task_id where t.id = (?)", (rs, row) -> {
                return rs.getDouble("hours");
            }, task.getId());
            tasks.put(task, hours.stream().mapToDouble(i -> i).sum());
        }
        return tasks;
    }

    public List<Task> getActiveTasks(){
        var sql = "SELECT t.id task_id, t.description task_description, t.difficulty difficulty," +
                " t.planned_labor_costs planned_labor_costs, t.status status, t.created_date created_date," +
                " t.start_date start_date, t.planned_end planned_end, t.end_date end_date," +
                " w.id work_id, w.start_work start_work, w.end_work end_work, w.description work_description" +
                "  FROM tasks as t LEFT JOIN works as w ON t.id=w.task_id" +
                "  WHERE status = (?) and status = ?" +
                "  ORDER BY task_id";

        return jdbc.query(sql, rs -> {
            Map<Long, Task> taskMap = new LinkedHashMap<>();
            while (rs.next()) {
                var task = taskMap.getOrDefault(rs.getLong("task_id"),
                        new Task(rs.getLong("task_id"), rs.getString("task_description"),
                                rs.getInt("difficulty"), rs.getInt("planned_labor_costs"),
                                rs.getString("status"), rs.getDate("created_date"),
                                rs.getDate("start_date"), rs.getDate("planned_end"),
                                rs.getDate("end_date"), new ArrayList<>()));
                if (Optional.ofNullable(rs.getString("work_id")).isPresent()) {
                    task.addWork(new Work(rs.getLong("work_id"), rs.getDate("start_work"),
                            rs.getDate("end_work"), rs.getString("work_description")));
                }
                taskMap.put(rs.getLong("task_id") ,task);
            }
            return new ArrayList<>(taskMap.values());
        }, "В работе", "Ожидание");
    }

    public Task taskByWork(Work work){
        return jdbc.query("select t.id id, t.description description, t.difficulty difficulty, t.planned_labor_costs planned_labor_costs, t.status status, t.created_date created_date, t.start_date start_date, t.planned_end planned_end, t.end_date end_date from tasks as t left join works as w on t.id = w.task_id where w.id = (?)", (rs, row) -> {
            return new Task(rs.getLong("id"), rs.getString("description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"),rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<Work>());
        }, work.getId()).stream().findFirst().orElse(new Task());
    }

    public Task getTaskById(Long id){
        return jdbc.query("select * from tasks where id = (?)", (rs, row) -> {
            return new Task(rs.getLong("id"), rs.getString("description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"),rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<Work>());
        }, id).stream().findFirst().orElse(new Task());
    }

    public void startTask(Long id, Date date){
        jdbc.update("update tasks set start_date = ?, end_date = null where id = ?",
                date, id);
    }

    public void endTask(Long id, Date date){
        jdbc.update("update tasks set end_date = ? where id = ?",
                date, id);
    }

    public List<Task> activeTasksByEmployeeId(Long employee_id){
        var sql = "SELECT t.id task_id, t.description task_description, t.difficulty difficulty, t.planned_labor_costs planned_labor_costs, t.status status, t.created_date created_date, t.start_date start_date, t.planned_end planned_end, t.end_date end_date," +
                " w.id work_id, w.start_work start_work, w.end_work end_work, w.description work_description" +
                "  FROM tasks as t LEFT JOIN works as w ON t.id=w.task_id" +
                "  WHERE t.employee_id = (?) and (status = ? or status = ?)" +
                "  ORDER BY task_id";

        return jdbc.query(sql, rs -> {
            Map<Long, Task> taskMap = new LinkedHashMap<>();
            while (rs.next()) {
                var task = taskMap.getOrDefault(rs.getLong("task_id"), new Task(rs.getLong("task_id"), rs.getString("task_description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"), rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<>()));
                task.addWork(new Work(rs.getLong("work_id"), rs.getDate("start_work"), rs.getDate("end_work"), rs.getString("work_description")));
                taskMap.put(rs.getLong("task_id") ,task);
            }
            return new ArrayList<>(taskMap.values());
        }, employee_id, "В работе", "Ожидание");
    }

    public List<Task> endedTasksByEmployeeId(Long employee_id){
        var sql = "SELECT t.id task_id, t.description task_description, t.difficulty difficulty, t.planned_labor_costs planned_labor_costs, t.status status, t.created_date created_date, t.start_date start_date, t.planned_end planned_end, t.end_date end_date," +
                " w.id work_id, w.start_work start_work, w.end_work end_work, w.description work_description" +
                "  FROM tasks as t LEFT JOIN works as w ON t.id=w.task_id" +
                "  WHERE t.employee_id = (?) and (status = ? or status = ?)" +
                "  ORDER BY task_id";

        return jdbc.query(sql, rs -> {
            Map<Long, Task> taskMap = new LinkedHashMap<>();
            while (rs.next()) {
                var task = taskMap.getOrDefault(rs.getLong("task_id"), new Task(rs.getLong("task_id"), rs.getString("task_description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"), rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<>()));
                if (Optional.ofNullable(rs.getString("work_id")).isPresent()) {
                    task.addWork(new Work(rs.getLong("work_id"), rs.getTimestamp("start_work"), rs.getTimestamp("end_work"), rs.getString("work_description")));
                }
                taskMap.put(rs.getLong("task_id") ,task);
            }
            return new ArrayList<>(taskMap.values());
        }, employee_id, "Отменено", "Завершено");
    }

    public List<Task> tasks(){
        var sql = "SELECT t.id task_id, t.description task_description, t.difficulty difficulty, t.planned_labor_costs planned_labor_costs, t.status status, t.created_date created_date, t.start_date start_date, t.planned_end planned_end, t.end_date end_date," +
                " w.id work_id, w.start_work start_work, w.end_work end_work, w.description work_description" +
                "  FROM tasks as t LEFT JOIN works as w ON t.id=w.task_id" +
                "  ORDER BY task_id";

        return jdbc.query(sql, rs -> {
            Map<Long, Task> taskMap = new LinkedHashMap<>();
            while (rs.next()) {
                var task = taskMap.getOrDefault(rs.getLong("task_id"), new Task(rs.getLong("task_id"), rs.getString("task_description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"), rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<>()));
                if (Optional.ofNullable(rs.getString("work_id")).isPresent()) {
                    task.addWork(new Work(rs.getLong("work_id"), rs.getTimestamp("start_work"), rs.getTimestamp("end_work"), rs.getString("work_description")));
                }
                taskMap.put(rs.getLong("task_id") ,task);
            }
            return new ArrayList<>(taskMap.values());
        });
    }

    public List<Task> tasks(Date from, Date to){
        var sql = "SELECT t.id task_id, t.description task_description, t.difficulty difficulty, t.planned_labor_costs planned_labor_costs, t.status status, t.created_date created_date, t.start_date start_date, t.planned_end planned_end, t.end_date end_date," +
                " w.id work_id, w.start_work start_work, w.end_work end_work, w.description work_description" +
                "  FROM tasks as t LEFT JOIN works as w ON t.id=w.task_id" +
                "  WHERE DATE(end_date) BETWEEN ? AND ?" +
                "  ORDER BY task_id";

        return jdbc.query(sql, rs -> {
            Map<Long, Task> taskMap = new LinkedHashMap<>();
            while (rs.next()) {
                var task = taskMap.getOrDefault(rs.getLong("task_id"), new Task(rs.getLong("task_id"), rs.getString("task_description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"), rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<>()));
                if (Optional.ofNullable(rs.getString("work_id")).isPresent()) {
                    task.addWork(new Work(rs.getLong("work_id"), rs.getTimestamp("start_work"), rs.getTimestamp("end_work"), rs.getString("work_description")));
                }
                taskMap.put(rs.getLong("task_id") ,task);
            }
            return new ArrayList<>(taskMap.values());
        }, from, to);
    }

}
