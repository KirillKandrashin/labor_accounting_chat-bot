package kirill.kandrashin.tgbot.domain.task;

import kirill.kandrashin.tgbot.domain.employee.Employee;
import kirill.kandrashin.tgbot.domain.project.Project;
import kirill.kandrashin.tgbot.domain.work.Work;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public List<Task> tasksByEmployeeId(Long id){
        return taskRepository.tasksByEmployeeId(id);
    }

    public void addTask(Employee employee, String description){
        taskRepository.addTask(employee, description);
    }

    public Task getTaskByDescr(String description){
        return taskRepository.getTaskByDescr(description);
    }

    public void changeStatusById(Long id, String status){
        if (status.equals("В работе") && taskById(id).getStart_date() == null){
            taskRepository.startTask(id, new Date());
        } else if(status.equals("Отменено") || status.equals("Завершено")) {
            taskRepository.endTask(id, new Date());
        }
        taskRepository.changeStatusById(id, status);
    }

    public Task taskById(Long id){
        return taskRepository.taskById(id);
    }

    public List<Task> getOverdueTasks(){
        return taskRepository.getOverdueTasks();
    }

    public Map<Task, String> getOverdueTasks(Date from, Date to){
        return taskRepository.getOverdueTasks(from, to);
    }

    public Map<Task, Double> getTimePerTaskPerProject(Project project){
        return taskRepository.getTimePerTaskPerProject(project);
    }

    public List<Task> getActiveTasks(){
        return taskRepository.getActiveTasks();
    }

    public Task taskByWork(Work work){
        return taskRepository.taskByWork(work);
    }

    public Task getTaskById(Long id){
        return taskRepository.getTaskById(id);
    }

    public void newTaskByProject(Long projectId){
        Date today = new Date();
        String status = "Ожидание";
        taskRepository.newTaskByProject(today, status, projectId);
    }

    public Task findNewTask(){
        return taskRepository.findNewTask();
    }

    public void updateByDescr(Long id, String description){
        taskRepository.updateByDescr(id, description);
    }

    public void updateByEmployee(Long id, Long employee_id){
        taskRepository.updateByEmployee(id, employee_id);
    }

    public void updateByPlannedLaborCosts(Long id, Integer planned_labor_costs){
        taskRepository.updateByPlannedLaborCosts(id, planned_labor_costs);
    }

    public void updateByDeadline(Long id, Date planned_end){
        taskRepository.updateByDeadline(id, planned_end);
    }

    public void updateByDifficulty(Long id, Integer difficulty){
        taskRepository.updateByDifficulty(id, difficulty);
    }

    public void deleteTask(Task task){
        taskRepository.deleteTask(task);
    }

    public List<Task> activeTasksByEmployeeId(Long employee_id){
        return taskRepository.activeTasksByEmployeeId(employee_id);
    }

    public List<Task> endedTasksByEmployeeId(Long employee_id){
        return taskRepository.endedTasksByEmployeeId(employee_id);
    }

    public List<Task> tasks(){
        return taskRepository.tasks();
    }

    public List<Task> tasks(Date from, Date to){
        return taskRepository.tasks(from, to);
    }
}
