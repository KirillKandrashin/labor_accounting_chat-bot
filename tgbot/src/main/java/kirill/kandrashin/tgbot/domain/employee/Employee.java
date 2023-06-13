package kirill.kandrashin.tgbot.domain.employee;

import kirill.kandrashin.tgbot.domain.task.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    private Long id;
    private Long chatId;
    private String emp_name;
    private String emp_mail;
    private String emp_role;
    private String emp_status;
    private String activation_code;

    private List<Task> tasks;

    public Employee(long employee_id, long chatid, String fio, String mail, String part, String emp_status, List<Task> tasks) {
        this.id = employee_id;
        this.chatId = chatid;
        this.emp_name = fio;
        this.emp_mail = mail;
        this.emp_role = part;
        this.emp_status = emp_status;
        this.tasks = tasks;
    }
}



