package kirill.kandrashin.tgbot.domain.project;

import kirill.kandrashin.tgbot.domain.task.Task;
import kirill.kandrashin.tgbot.domain.work.Work;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private Long id;

    private String title;
    private String status;
    private Date start_date;
    private Date end_date;
    private Date deadline;

    private List<Task> tasks;

    public void addTask(Task task){
        this.tasks.add(task);
    }
}



