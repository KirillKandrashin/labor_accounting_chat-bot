package kirill.kandrashin.tgbot.domain.project;

import kirill.kandrashin.tgbot.domain.employee.Employee;
import kirill.kandrashin.tgbot.domain.task.Task;
import kirill.kandrashin.tgbot.domain.work.Work;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class ProjectRepository {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    public void addProject(String title, String status, Date deadline) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("title", title);
        paramMap.put("status", status);
        paramMap.put("start_date", new Date());
        paramMap.put("deadline", deadline);
        namedJdbc.update("insert into projects(title, status, start_date, deadline) values (:title, :status, :start_date, :deadline)", paramMap);
    }

    public List<Project> findActiveProjects(){
        var sql = "SELECT p.id project_id, p.title title ,p.status project_status, p.start_date project_start_date," +
                " p.end_date project_end_date, p.deadline deadline, " +
                " t.id task_id, t.description task_description, t.difficulty difficulty," +
                " t.planned_labor_costs planned_labor_costs, t.status task_status," +
                " t.created_date task_created_date, t.start_date task_start_date," +
                " t.planned_end planned_end, t.end_date end_date" +
                "  FROM projects as p LEFT JOIN tasks as t ON p.id = t.project_id" +
                "  WHERE p.status = (?)";

        return jdbc.query(sql, rs -> {
            Map<Long, Project> projectMap = new LinkedHashMap<>();
            while (rs.next()) {
                if (rs.getLong("project_id") != 0) {
                    var project = projectMap.getOrDefault(rs.getLong("project_id"),
                            new Project(rs.getLong("project_id"), rs.getString("title"),
                                    rs.getString("project_status"),
                                    rs.getDate("project_start_date"), rs.getDate("project_end_date"),
                                    rs.getDate("deadline"), new ArrayList<>()));
                    if (Optional.ofNullable(rs.getString("task_id")).isPresent()) {
                        project.addTask(new Task(rs.getLong("task_id"),
                                rs.getString("task_description"), rs.getInt("difficulty"),
                                rs.getInt("planned_labor_costs"), rs.getString("task_status"),
                                rs.getDate("task_created_date"), rs.getDate("task_start_date"),
                                rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<>()));
                    }
                    projectMap.put(rs.getLong("project_id"), project);
                }
            }
            return new ArrayList<>(projectMap.values());
        }, "В работе");
    }

    public List<Project> findInactiveProjects(){
        var sql = "SELECT p.id project_id, p.title title ,p.status project_status, p.start_date project_start_date, p.end_date project_end_date, p.deadline deadline, " +
                " t.id task_id, t.description task_description, t.difficulty difficulty, t.planned_labor_costs planned_labor_costs, t.status task_status, t.created_date task_created_date, t.start_date task_start_date, t.planned_end planned_end, t.end_date end_date" +
                "  FROM projects as p LEFT JOIN tasks as t ON p.id = t.project_id" +
                "  WHERE p.status = (?) or p.status = (?)";

        return jdbc.query(sql, rs -> {
            Map<Long, Project> projectMap = new LinkedHashMap<>();
            while (rs.next()) {
                if (rs.getLong("project_id") != 0) {
                    var project = projectMap.getOrDefault(rs.getLong("project_id"), new Project(rs.getLong("project_id"), rs.getString("title"), rs.getString("project_status"), rs.getDate("project_start_date"), rs.getDate("project_end_date"), rs.getDate("deadline"), new ArrayList<>()));
                    if (Optional.ofNullable(rs.getString("task_id")).isPresent()) {
                        project.addTask(new Task(rs.getLong("task_id"), rs.getString("task_description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("task_status"), rs.getDate("task_created_date"), rs.getDate("task_start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<>()));
                    }
                    projectMap.put(rs.getLong("project_id"), project);
                }
            }
            return new ArrayList<>(projectMap.values());
        }, "Сдано", "Отменено");
    }

    public Project projectById(Long id){
        var sql = "SELECT p.id project_id, p.title title, p.status project_status, p.start_date project_start_date, p.end_date project_end_date, p.deadline deadline, " +
                " t.id task_id, t.description task_description, t.difficulty difficulty, t.planned_labor_costs planned_labor_costs, t.status task_status, t.created_date task_created_date, t.start_date task_start_date, t.planned_end planned_end, t.end_date end_date, " +
                " w.id work_id, w.start_work start_work, w.end_work end_work, w.description work_description" +
                "  FROM projects as p LEFT JOIN tasks as t ON p.id = t.project_id LEFT JOIN works as w ON t.id=w.task_id" +
                "  WHERE project_id = (?)";
        return jdbc.query(sql, rs -> {
                Map<Long, Task> taskMap = new LinkedHashMap<>();
                Project project = new Project();
                while (rs.next()) {
                    var task = taskMap.getOrDefault(rs.getLong("task_id"), new Task(rs.getLong("task_id"), rs.getString("task_description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("task_status"), rs.getDate("task_created_date"), rs.getDate("task_start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<>()));
                    project.setId(rs.getLong("project_id"));
                    project.setTitle(rs.getString("title"));
                    project.setStatus(rs.getString("project_status"));
                    project.setStart_date(rs.getDate("project_start_date"));
                    project.setEnd_date(rs.getDate("project_end_date"));
                    project.setDeadline(rs.getDate("deadline"));
                    if (Optional.ofNullable(rs.getString("work_id")).isPresent()) {
                        task.addWork(new Work(rs.getLong("work_id"), rs.getTimestamp("start_work"), rs.getTimestamp("end_work"), rs.getString("work_description")));
                    }
                    taskMap.put(rs.getLong("task_id") ,task);
                }
                project.setTasks(new ArrayList<>(taskMap.values()));
                return project;
        }, id);
    }

    public Project findProjectByTask(Task task){
        return jdbc.query("select p.id project_id, p.title title, p.status project_status, p.start_date project_start_date, p.end_date project_end_date, p.deadline deadline from projects as p left join tasks as t on p.id = t.project_id where t.id = (?)", (rs, row) -> {
            return new Project(rs.getLong("project_id"), rs.getString("title"), rs.getString("project_status"), rs.getDate("project_start_date"), rs.getDate("project_end_date"), rs.getDate("deadline"), new ArrayList<>());
        }, task.getId()).stream().findFirst().orElse(new Project());
    }

    public void editStatus(Long id, String status){
        jdbc.update("update projects set status = ? where id = ?",
                status, id);
        if (status.equals("Отменен") || status.equals("Завершен")){
            jdbc.update("update projects set status = ? where id = ?",
                    new Date(), id);
        }

    }
}
