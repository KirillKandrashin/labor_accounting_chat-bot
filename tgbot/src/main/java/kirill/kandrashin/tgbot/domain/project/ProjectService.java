package kirill.kandrashin.tgbot.domain.project;

import kirill.kandrashin.tgbot.domain.task.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    public void addProject(String name, String status, Date deadline){
        projectRepository.addProject(name, status, deadline);
    }

    public Project findProjectByTask(Task task){
        return projectRepository.findProjectByTask(task);
    }

    public List<Project> findActiveProjects(){
        return projectRepository.findActiveProjects();
    }

    public List<Project> findInactiveProjects(){
        return projectRepository.findInactiveProjects();
    }

    public Project projectById(Long id){
        return projectRepository.projectById(id);
    }

    public void editStatus(Long id, String status){
        projectRepository.editStatus(id, status);
    }
}
