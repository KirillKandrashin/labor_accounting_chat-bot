package kirill.kandrashin.tgbot.domain.work;

import kirill.kandrashin.tgbot.domain.employee.Employee;
import kirill.kandrashin.tgbot.domain.task.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class WorkRepository {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    public void entryTimeEntries(Task task, String descr, Double hours){
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("date", new Date());
        paramMap.put("hours", hours);
        paramMap.put("description", descr);
        paramMap.put("task_id", task.getId());
        namedJdbc.update("insert into works(date, hours, description, task_id) values (:date, :hours, :description, :task_id)", paramMap);
    }

    public List<Work> findWorkByTaskAndDate(Task task, Date date){
        return jdbc.query("select * from works where task_id = ? and DATE(start_work) = ?", (rs, row) -> {
            return new Work(rs.getLong("id"), rs.getTimestamp("start_work"), rs.getTimestamp("end_work"), rs.getString("description"));
        }, task.getId(), date);
    }

    public List<Work> getWorkByWorker(Employee worker, Date date){
        return jdbc.query("select w.id id, w.start_work start_work, w.end_work end_work, w.description description from works as w left join tasks as t on w.task_id = t.id where t.employee_id = ? and DATE(start_work) = ?", (rs, row) -> {
            return new Work(rs.getLong("id"), rs.getTimestamp("start_work"), rs.getTimestamp("end_work"), rs.getString("description"));
        }, worker.getId(), date);
    }

    public void startWorking(Long task_id, Date time){
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("start_work", time);
        paramMap.put("task_id", task_id);
        namedJdbc.update("insert into works(start_work, task_id) values (:start_work, :task_id)", paramMap);
    }

    public Work findNewWorkByEmployee(Long chatId){
        var sql = "SELECT w.id id, w.start_work start_work, w.end_work end_work, w.description description" +
                "  FROM works as w LEFT JOIN tasks as t ON w.task_id = t.id LEFT JOIN employees as e ON t.employee_id = e.id" +
                "  WHERE e.chatid = (?) and w.description IS NULL";

        return jdbc.query(sql, (rs, row) -> {
            return new Work(rs.getLong("id"), rs.getTimestamp("start_work"), rs.getTimestamp("end_work"), rs.getString("description"));
        }, chatId).stream().findFirst().orElse(new Work());
    }
    public void endWork(Long id, Date date){
        jdbc.update("update works set end_work = ? where id = ?",
            date, id);
    }

    public void nameWork(Long id, String description){
        jdbc.update("update works set description = ? where id = ?",
                description, id);
    }

    public List<Work> findUnfinishedWork(Date date){
        return jdbc.query("select * from works where end_work is null and DATE(start_work) = DATE(?)", (rs, row) -> {
            return new Work(rs.getLong("id"), rs.getTimestamp("start_work"), rs.getTimestamp("end_work"), rs.getString("description"));
        }, date);
    }

    public Work getWorkById(Long id){
        return jdbc.query("select * from works  where id = ?", (rs, row) -> {
            return new Work(rs.getLong("id"), rs.getTimestamp("start_work"), rs.getTimestamp("end_work"), rs.getString("description"));
        }, id).stream().findFirst().orElse(new Work());
    }
}
