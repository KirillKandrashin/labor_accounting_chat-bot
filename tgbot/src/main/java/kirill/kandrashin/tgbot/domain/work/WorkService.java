package kirill.kandrashin.tgbot.domain.work;

import kirill.kandrashin.tgbot.domain.employee.Employee;
import kirill.kandrashin.tgbot.domain.task.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkService {
    private final WorkRepository workRepository;

    public void entryWork(Task task, String descr, Double hours){
        workRepository.entryTimeEntries(task, descr, hours);
    }

    public List<Work> findWorkByTaskAndDate(Task task, Date date){
        return workRepository.findWorkByTaskAndDate(task, date);
    }

    public List<Work> getWorkByWorker(Employee worker, Date date){
        return workRepository.getWorkByWorker(worker, date);
    }

    public void startWorking(Long task_id, Date time){
        workRepository.startWorking(task_id, time);
    }

    public Work findNewWorkByEmployee(Long chatId){
        return workRepository.findNewWorkByEmployee(chatId);
    }

    public void endWork(Long id, Date date){
        workRepository.endWork(id, date);
    }
    public void nameWork(Long id, String description){
        workRepository.nameWork(id, description);
    }

    public Work findEndedWorkNoDescByEmp(Long chatId){
        return workRepository.findNewWorkByEmployee(chatId);
    }

    public List<Work> findUnfinishedWork(Date date){
        return workRepository.findUnfinishedWork(date);
    }

    public Work getWorkById(Long id){
        return workRepository.getWorkById(id);
    }

}
