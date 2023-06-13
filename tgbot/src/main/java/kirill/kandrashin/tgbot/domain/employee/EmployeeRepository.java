package kirill.kandrashin.tgbot.domain.employee;

import kirill.kandrashin.tgbot.domain.task.Task;
import kirill.kandrashin.tgbot.domain.work.Work;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class EmployeeRepository {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    public void saveEmployee(Long chatId, String emp_role, String emp_status){
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("chatid", chatId);
        paramMap.put("emp_role", emp_role);
        paramMap.put("emp_status", emp_status);
        namedJdbc.update("insert into employees(chatid, emp_role, emp_status) values (:chatid, :emp_role, :emp_status)", paramMap);
    }

    public void updateEmployeeByNameEmail(Long chatId, String emp_name, String emp_mail, String activation_code){
        jdbc.update("update employees set emp_name = ? where chatId = ?",
                emp_name, chatId);
        jdbc.update("update employees set emp_mail = ? where chatId = ?",
                emp_mail, chatId);
        jdbc.update("update employees set activation_code = ? where chatId = ?",
                activation_code, chatId);
    }

    public void updateEmployeeStatus(Employee employee ,String status){
        jdbc.update("update employees set emp_status = ? where id = ?",
                status, employee.getId());
    }

    public Employee findEmployeeByCode(String code){
        return jdbc.query("select * from employees where activation_code = (?)", (rs, row) -> {
            return new Employee(rs.getLong("id"), rs.getLong("chatId"),
                    rs.getString("emp_name"), rs.getString("emp_mail"),
                    rs.getString("emp_role"), rs.getString("emp_status"), new ArrayList<>());
        }, code).stream().findFirst().orElse(new Employee());
    }

    public List<Employee> findAllEmployees(String emp_role){
        var sql = "SELECT e.id employee_id, e.chatid chatid, e.emp_name fio, e.emp_mail mail, e.emp_role part, e.emp_status emp_status, " +
                " t.id task_id, t.description task_description, t.difficulty difficulty, t.planned_labor_costs planned_labor_costs," +
                " t.status status, t.created_date created_date, t.start_date start_date, t.planned_end planned_end, t.end_date end_date," +
                " w.id work_id, w.start_work start_work, w.end_work end_work, w.description work_description" +
                "  FROM employees as e LEFT JOIN tasks as t ON e.id = t.employee_id LEFT JOIN works as w ON t.id=w.task_id" +
                "  WHERE e.emp_role = (?)" +
                "  ORDER BY employee_id";

        return jdbc.query(sql, rs -> {
            Map<Long, Employee> employeeMap = new LinkedHashMap<>();
            Map<Long, Task> taskMap = new LinkedHashMap<>();
            Employee employee;
            Task task;
            while (rs.next()){
                if (employeeMap.containsKey(rs.getLong("employee_id"))){
                    employee = employeeMap.get(rs.getLong("employee_id"));
                } else{
                    employee = new Employee(rs.getLong("employee_id") ,rs.getLong("chatid"),
                            rs.getString("fio"), rs.getString("mail"), rs.getString("part"),
                            rs.getString("emp_status"), new ArrayList<>());
                    taskMap = new LinkedHashMap<>();
                    employeeMap.put(rs.getLong("employee_id"), employee);
                }
                if (rs.getLong("task_id") != 0) {
                    task = taskMap.getOrDefault(rs.getLong("task_id"), new Task(rs.getLong("task_id"),
                            rs.getString("task_description"), rs.getInt("difficulty"),
                            rs.getInt("planned_labor_costs"), rs.getString("status"),
                            rs.getDate("created_date"), rs.getDate("start_date"),
                            rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<>()));
                    task.addWork(new Work(rs.getLong("work_id"), rs.getTimestamp("start_work"),
                            rs.getTimestamp("end_work"), rs.getString("work_description")));
                    taskMap.put(rs.getLong("task_id"), task);
                    employee.setTasks(new ArrayList<>(taskMap.values()));
                    employeeMap.put((rs.getLong("employee_id")), employee);
                }
            }
            return new ArrayList<>(employeeMap.values());
        }, emp_role);
    }

    public Employee findEmployeeByName(String emp_name){
        var sql = "SELECT e.id employee_id, e.chatid chatid, e.emp_name fio, e.emp_mail mail, e.emp_role part, e.emp_status emp_status, " +
                " t.id task_id, t.description task_description, t.difficulty difficulty, t.planned_labor_costs planned_labor_costs," +
                " t.status status, t.created_date created_date, t.start_date start_date, t.planned_end planned_end, t.end_date end_date," +
                " w.id work_id, w.start_work start_work, w.end_work end_work, w.description work_description" +
                "  FROM employees as e LEFT JOIN tasks as t ON e.id = t.employee_id LEFT JOIN works as w ON t.id=w.task_id" +
                "  WHERE e.emp_name = (?)";

        return jdbc.query(sql, rs -> {
            Map<Long, Task> taskMap = new LinkedHashMap<>();
            Employee employee = new Employee();
            while (rs.next()) {
                var task = taskMap.getOrDefault(rs.getLong("task_id"), new Task(rs.getLong("task_id"),
                        rs.getString("task_description"), rs.getInt("difficulty"),
                        rs.getInt("planned_labor_costs"), rs.getString("status"),
                        rs.getDate("created_date"), rs.getDate("start_date"),
                        rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<>()));
                employee.setId(rs.getLong("employee_id"));
                employee.setChatId(rs.getLong("chatid"));
                employee.setEmp_name(rs.getString("fio"));
                employee.setEmp_mail(rs.getString("mail"));
                employee.setEmp_role(rs.getString("part"));
                employee.setEmp_status(rs.getString("emp_status"));
                employee.setActivation_code(rs.getString("activation_code"));
                if (Optional.ofNullable(rs.getString("work_id")).isPresent()) {
                    task.addWork(new Work(rs.getLong("work_id"),
                            rs.getTimestamp("start_work"), rs.getTimestamp("end_work"),
                            rs.getString("work_description")));
                }
                taskMap.put(rs.getLong("task_id") ,task);
            }
            employee.setTasks(new ArrayList<>(taskMap.values()));
            return employee;
        }, emp_name);
    }

    public Employee findEmployeeByChatId(Long chatId){
        /*return jdbc.query("select * from employees where chatId = ?", (rs, row) -> {
            return new Employee(rs.getLong("id"), rs.getLong("chatId"), rs.getString("emp_name"), rs.getString("emp_role"), rs.getBoolean("confirmed"), new ArrayList<>());
        }, chatId).stream().findFirst().orElse(new Employee());*/
        var sql = "SELECT e.id employee_id, e.chatid chatid, e.emp_name fio, e.emp_mail mail, e.emp_role part, e.emp_status emp_status, e.activation_code activation_code, e.activation_code activation_code," +                        //emp_name и emp_role названы fio и part соответсвенно из-за конфликтов sql-строки
                "t.id task_id, t.description task_description, t.difficulty difficulty, t.planned_labor_costs planned_labor_costs, t.status status, t.created_date created_date, t.start_date start_date, t.planned_end planned_end, t.end_date end_date, " +
                "w.id work_id, w.start_work start_work, w.end_work end_work, w.description work_description" +
                "  FROM employees as e LEFT JOIN tasks as t ON e.id = t.employee_id LEFT JOIN works as w ON t.id=w.task_id" +
                "  WHERE e.chatid = (?)";

        return jdbc.query(sql, rs -> {
            Map<Long, Task> taskMap = new LinkedHashMap<>();
            Employee employee = new Employee();
            while (rs.next()) {
                var task = taskMap.getOrDefault(rs.getLong("task_id"), new Task(rs.getLong("task_id"), rs.getString("task_description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"), rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<>()));
                employee.setId(rs.getLong("employee_id"));
                employee.setChatId(rs.getLong("chatid"));
                employee.setEmp_name(rs.getString("fio"));
                employee.setEmp_mail(rs.getString("mail"));
                employee.setEmp_role(rs.getString("part"));
                employee.setEmp_status(rs.getString("emp_status"));
                employee.setActivation_code(rs.getString("activation_code"));
                if (Optional.ofNullable(rs.getString("work_id")).isPresent()) {
                    task.addWork(new Work(rs.getLong("work_id"), rs.getTimestamp("start_work"), rs.getTimestamp("end_work"), rs.getString("work_description")));
                }
                taskMap.put(rs.getLong("task_id") ,task);
            }
            employee.setTasks(new ArrayList<>(taskMap.values()));
            return employee;
        }, chatId);
    }

    /*public List<Employee> catchEmployeesWithoutApprove(){
        return jdbc.query("select * from employees where confirmed is NULL",
                (rs, row) -> {
                    return new Employee(rs.getLong("id"), rs.getLong("chatId"), rs.getString("emp_name"), rs.getString("emp_role"), rs.getBoolean("confirmed"), new ArrayList<>());
                });
    }*/

    public Employee findEmployeeByRole(String emp_role){
        /*return jdbc.query("select * from employees where emp_role = ? and confirmed = (?)", (rs, row) -> {
            return new Employee(rs.getLong("id"), rs.getLong("chatId"), rs.getString("emp_name"), rs.getString("emp_role"), rs.getBoolean("confirmed"), new ArrayList<>());
        }, emp_role, true).stream().findFirst().orElse(new Employee());*/
        var sql = "SELECT e.id employee_id, e.chatid chatid, e.emp_name fio, e.emp_mail mail, e.emp_role part, e.emp_status emp_status, " +
                " t.id task_id, t.description task_description, t.difficulty difficulty, t.planned_labor_costs planned_labor_costs, t.status status, t.created_date created_date, t.start_date start_date, t.planned_end planned_end, t.end_date end_date," +
                " w.id work_id, w.start_work start_work, w.end_work end_work, w.description work_description" +
                "  FROM employees as e LEFT JOIN tasks as t ON e.id = t.employee_id LEFT JOIN works as w ON t.id=w.task_id" +
                "  WHERE e.emp_role = (?)";

        return jdbc.query(sql, rs -> {
            Map<Long, Task> taskMap = new LinkedHashMap<>();
            Employee employee = new Employee();
            while (rs.next()) {
                var task = taskMap.getOrDefault(rs.getLong("task_id"), new Task(rs.getLong("task_id"), rs.getString("task_description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"), rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<>()));
                employee.setId(rs.getLong("employee_id"));
                employee.setChatId(rs.getLong("chatid"));
                employee.setEmp_name(rs.getString("fio"));
                employee.setEmp_mail(rs.getString("mail"));
                employee.setEmp_role(rs.getString("part"));
                employee.setEmp_status(rs.getString("emp_status"));
                if (Optional.ofNullable(rs.getString("work_id")).isPresent()) {
                    task.addWork(new Work(rs.getLong("work_id"), rs.getTimestamp("start_work"), rs.getTimestamp("end_work"), rs.getString("work_description")));
                }
                taskMap.put(rs.getLong("task_id") ,task);
            }
            employee.setTasks(new ArrayList<>(taskMap.values()));
            return employee;
        }, emp_role);
    }

    public List<Employee> findEmployeesWithWorkByDate(Date from, Date to) {
        //List<Employee> ans = new ArrayList<>();
        var sql = "SELECT e.id employee_id, e.chatid chatid, e.emp_name fio, e.emp_mail mail, e.emp_role part, e.emp_status emp_status, " +
                " t.id task_id, t.description task_description, t.difficulty difficulty, t.planned_labor_costs planned_labor_costs, t.status status, t.created_date created_date, t.start_date start_date, t.planned_end planned_end, t.end_date end_date," +
                " w.id work_id, w.start_work start_work, w.end_work end_work, w.description work_description" +
                "  FROM employees as e LEFT JOIN tasks as t ON e.id = t.employee_id LEFT JOIN works as w ON t.id=w.task_id" +
                "  WHERE DATE(w.start_work) BETWEEN ? AND ?" +
                "  ORDER BY employee_id";
        return jdbc.query(sql,  rs -> {
            Map<Long, Employee> employeeMap = new LinkedHashMap<>();
            Map<Long, Task> taskMap = new LinkedHashMap<>();
            Employee employee;
            Task task;
            while (rs.next()){
                if (employeeMap.containsKey(rs.getLong("employee_id"))){
                    employee = employeeMap.get(rs.getLong("employee_id"));
                } else{
                    employee = new Employee(rs.getLong("employee_id") ,rs.getLong("chatid"), rs.getString("fio"), rs.getString("mail"), rs.getString("part"), rs.getString("emp_status"), new ArrayList<>());
                    taskMap = new LinkedHashMap<>();
                    employeeMap.put(rs.getLong("employee_id"), employee);
                }
                if (rs.getLong("task_id") != 0) {
                    task = taskMap.getOrDefault(rs.getLong("task_id"), new Task(rs.getLong("task_id"), rs.getString("task_description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"), rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<>()));
                    task.addWork(new Work(rs.getLong("work_id"), rs.getTimestamp("start_work"), rs.getTimestamp("end_work"), rs.getString("work_description")));
                    taskMap.put(rs.getLong("task_id"), task);
                    employee.setTasks(new ArrayList<>(taskMap.values()));
                    employeeMap.put((rs.getLong("employee_id")), employee);
                }
            }
            return new ArrayList<>(employeeMap.values());

            /*Map<Long, Task> taskMap = new LinkedHashMap<>();
            Employee employee = new Employee();
            while (rs.next()) {
                var task = taskMap.getOrDefault(rs.getLong("task_id"), new Task(rs.getLong("task_id"), rs.getString("task_description"), rs.getInt("difficulty"), rs.getInt("planned_labor_costs"), rs.getString("status"), rs.getDate("created_date"), rs.getDate("start_date"), rs.getDate("planned_end"), rs.getDate("end_date"), new ArrayList<>()));
                employee.setId(rs.getLong("employee_id"));
                employee.setChatId(rs.getLong("chatid"));
                employee.setEmp_name(rs.getString("fio"));
                employee.setEmp_role(rs.getString("part"));
                employee.setConfirmed(rs.getBoolean("confirmed"));
                if (Optional.ofNullable(rs.getString("work_id")).isPresent()) {
                    task.addWork(new Work(rs.getLong("work_id"), rs.getTimestamp("start_work"), rs.getTimestamp("end_work"), rs.getString("work_description")));
                }
                taskMap.put(rs.getLong("task_id") ,task);
            }
            employee.setTasks(new ArrayList<>(taskMap.values()));
            return employee;*/
        }, from, to);
    }

    public void confirm(String emp_name){
        jdbc.update("update employees set confirmed = ? where emp_name = ?",
                true, emp_name);
    }

    public void deleteUnconfirmed(){
        jdbc.update("delete from employees where confirmed = (?)",
                false);
    }

    public void deleteEmployee(String emp_name){
        jdbc.update("delete from employees where emp_name = (?)",
                emp_name);
    }

    public void deleteEmployeeByChatId(Long chatId){
        jdbc.update("delete from employees where chatid = (?)",
                chatId);
    }

    public void unconfirmEmployee(Long id){
        jdbc.update("update employees set confirmed = ? where chatid = ?",
                false, id);
    }

    public List<Employee> findByConfirmation(Boolean confirmation){
        return jdbc.query("select * from employees where confirmed = ? and emp_role = (?)", (rs, row) -> {
            return new Employee(rs.getLong("id"), rs.getLong("chatId"), rs.getString("emp_name"), rs.getString("emp_mail"), rs.getString("emp_role"), rs.getString("emp_status"), new ArrayList<>());
        }, confirmation, "Worker");
    }

    public Employee findEmployeeByTask(Task task){
        return jdbc.query("select * from employees as e left join tasks as t on e.id = t.employee_id where t.id = (?)", (rs, row) -> {
            return new Employee(rs.getLong("id"), rs.getLong("chatId"), rs.getString("emp_name"), rs.getString("emp_mail"), rs.getString("emp_role"), rs.getString("emp_status"), new ArrayList<>());
        }, task.getId()).stream().findFirst().orElse(new Employee());
    }

    public Employee findEmployeeById(Long id){
        return jdbc.query("select * from employees where id = (?)", (rs, row) -> {
            return new Employee(rs.getLong("id"), rs.getLong("chatId"), rs.getString("emp_name"), rs.getString("emp_mail"), rs.getString("emp_role"), rs.getString("emp_status"), new ArrayList<>());
        }, id).stream().findFirst().orElse(new Employee());
    }


}


