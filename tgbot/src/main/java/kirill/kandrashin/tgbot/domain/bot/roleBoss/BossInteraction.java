package kirill.kandrashin.tgbot.domain.bot.roleBoss;

import kirill.kandrashin.tgbot.domain.bot.Response;
import kirill.kandrashin.tgbot.domain.employee.Employee;
import kirill.kandrashin.tgbot.domain.employee.EmployeeService;
import kirill.kandrashin.tgbot.domain.project.Project;
import kirill.kandrashin.tgbot.domain.project.ProjectService;
import kirill.kandrashin.tgbot.domain.task.Task;
import kirill.kandrashin.tgbot.domain.task.TaskService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@RequiredArgsConstructor
public class BossInteraction {
    private final BossCommands bossCommands;
    private final EmployeeService employeeService;
    private final ProjectService projectService;
    private final TaskService taskService;
    private String status = "";
    Task task;
    Project project;

    public List<Response> getResponse(Update update){
        List<Response> responses = new ArrayList<>();
        if (update.hasMessage()) {

            String message = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            Employee employee;
            switch (message){
                case "фишка":
                    responses.add(new Response(chatId, "Через 2 недели срок сдачи проекта #" + 4 + " " + "Госконтракт 18" + "\n" +
                            "Дедлайн: " + "25.05.2023" + "\n" +
                            "Завершенность: " + 70 + "%", "empty"));
                    break;
                case "фишка2":
                    responses.add(new Response(chatId, "Сотрудник: Петр Петров просрочил задачу #9 Разработка плана реализации подсистемы Форватор" , "empty"));
                    break;
                case "фишка3":
                    responses.add(new Response(chatId, "Предоставить пользователю доступ к боту?" + "\n" +
                            "ФИО: " + "Кирилл Кандрашин" + "\n" +
                            "Почта: " + "kirill.kandrashin@mail.ru", "confirming"));
                    break;
                case "/start":
                case "Старт":
                    responses.add(bossCommands.start(chatId));
                    break;
                case "Сотрудники":
                    responses.add(bossCommands.workers(chatId));
                    break;
                case "Проекты":
                    responses.add(bossCommands.workWithProjects(chatId));
                    break;
                case "Список задач":
                    responses.add(bossCommands.workWithTasks(chatId));
                    status = "task_type";
                    break;
                case "Работа сотрудников":
                    responses.add(bossCommands.workersWork(chatId));
                    break;
                case "Сформировать отчет":
                    responses.add(bossCommands.reportGeneration(chatId));
                    break;
                case "Помощь":
                    break;
                case "Отмена":
                    responses.add(bossCommands.cancel(chatId));
                    break;
                default:
                    switch (status){
                        /*case "inputUrName":
                            employeeService.editWithName(chatId, message);
                            responses.add(bossCommands.naming(chatId));
                            status = "main";
                            break;*/
                        case "adding_project":
                            responses.add(bossCommands.projectAdded(chatId, message));
                            status = "main";
                            break;
                        case "worker_for_stats":
                            employee = employeeService.findEmployeeByName(message);
                            if (employee.getId() == null){
                                status = "guess_emp_for_stats";
                                responses.add(bossCommands.guessEmployee(chatId, message));
                            } else{
                                responses.add(bossCommands.employeeInfo(chatId, message));
                            }
                            break;
                        case "worker_for_fire":
                            employee = employeeService.findEmployeeByName(message);
                            if (employee.getId() == null){
                                status = "guess_emp_for_fire";
                                responses.add(bossCommands.guessEmployee(chatId, message));
                            } else{
                                responses = bossCommands.fire(chatId, employee.getId());
                            }
                            break;
                        case "worker_active_tasks":
                            employee = employeeService.findEmployeeByName(message);
                            if (employee.getId() == null){
                                status = "guess_emp_for_active_tasks";
                                responses.add(bossCommands.guessEmployee(chatId, message));
                            } else{
                                responses = bossCommands.showActiveTask(chatId, employee.getId());
                            }
                            break;
                        case "worker_closed_tasks":
                            employee = employeeService.findEmployeeByName(message);
                            if (employee.getId() == null){
                                status = "guess_emp_for_closed_tasks";
                                responses.add(bossCommands.guessEmployee(chatId, message));
                            } else{
                                responses = bossCommands.showFinishedTask(chatId, employee.getId());
                            }
                            break;
                        case "entry_task_descr":
                            taskService.updateByDescr(task.getId(), message);
                            task = taskService.taskById(task.getId());
                            responses.add(bossCommands.taskInfoEntered(chatId, task));
                            break;
                        case "entry_task_employee":
                            employee = employeeService.findEmployeeByName(message);
                            if (employee.getId() == null){
                                status = "guess_emp_for_task";
                                responses.add(bossCommands.guessEmployee(chatId, message));
                            } else{
                                taskService.updateByEmployee(task.getId(), employee.getId());
                                task = taskService.taskById(task.getId());
                                responses.add(bossCommands.taskInfoEntered(chatId, task));
                            }
                            break;
                        case "entry_planned_labor_costs":
                            taskService.updateByPlannedLaborCosts(task.getId(), Integer.parseInt(message));
                            task = taskService.taskById(task.getId());
                            responses.add(bossCommands.taskInfoEntered(chatId, task));
                            break;
                        case "entry_task_planned_end":
                            List<String> date_splitted = new ArrayList<>(Arrays.asList(message.split("\\.")));
                            Calendar calendar = new GregorianCalendar(Integer.parseInt(date_splitted.get(2)), Integer.parseInt(date_splitted.get(1))-1, Integer.parseInt(date_splitted.get(0)));
                            Date deadline = calendar.getTime();
                            taskService.updateByDeadline(task.getId(), deadline);
                            task = taskService.taskById(task.getId());
                            responses.add(bossCommands.taskInfoEntered(chatId, task));
                            break;
                        case "entry_task_difficulty":
                            taskService.updateByDifficulty(task.getId(), Integer.parseInt(message));
                            task = taskService.taskById(task.getId());
                            responses.add(bossCommands.taskInfoEntered(chatId, task));
                            break;
                        }
            }
        }
        if (update.hasCallbackQuery()){
            Long chatId = update.getCallbackQuery().getFrom().getId();
            List<String> callback_list = Arrays.asList(update.getCallbackQuery().getData().split(" "));
            System.out.println(callback_list);
            long id;
            switch (callback_list.get(0)){
                case "/yes":
                    id = Long.parseLong(callback_list.get(1));
                    responses = bossCommands.confirm(chatId, id);
                    break;
                case "/no":
                    id = Long.parseLong(callback_list.get(1));
                    responses = bossCommands.reject(chatId, id);
                    break;
                case "/yes_guess":
                    if (status.equals("guess_emp_for_task")){
                        List<String> name_list = callback_list.subList(1, callback_list.size());
                        String name = StringUtils.join(name_list, " ");
                        Employee employee = employeeService.findEmployeeByName(name);
                        taskService.updateByEmployee(task.getId(), employee.getId());
                        task = taskService.taskById(task.getId());
                        responses.add(bossCommands.taskInfoEntered(chatId, task));
                    } else if (status.equals("guess_emp_for_stats")){
                        List<String> name_list = callback_list.subList(1, callback_list.size());
                        String name = StringUtils.join(name_list, " ");
                        responses.add(bossCommands.employeeInfo(chatId, name));
                    } else if (status.equals("guess_emp_for_fire")){
                        List<String> name_list = callback_list.subList(1, callback_list.size());
                        String name = StringUtils.join(name_list, " ");
                        Employee employee = employeeService.findEmployeeByName(name);
                        responses = bossCommands.fire(chatId, employee.getId());
                    } else if (status.equals("guess_emp_for_active_tasks")){
                        List<String> name_list = callback_list.subList(1, callback_list.size());
                        String name = StringUtils.join(name_list, " ");
                        Employee employee = employeeService.findEmployeeByName(name);
                        responses = bossCommands.showActiveTask(chatId, employee.getId());
                    } else if (status.equals("guess_emp_for_closed_tasks")) {
                        List<String> name_list = callback_list.subList(1, callback_list.size());
                        String name = StringUtils.join(name_list, " ");
                        Employee employee = employeeService.findEmployeeByName(name);
                        responses = bossCommands.showFinishedTask(chatId, employee.getId());
                    }
                    break;
                case "/stats":
                    status = "worker_for_stats";
                    responses.add(bossCommands.workWithWorkers(chatId));
                    break;
                case "/fire":
                    status = "worker_for_fire";
                    responses.add(bossCommands.workWithWorkers(chatId));
                    break;
                case "/workers_tasks":
                    //responses.add(bossCommands.workWithWorkers(chatId));
                    responses.add(bossCommands.chooseTaskType(chatId));
                    break;
                case "/active_tasks":
                    if (status.equals("task_type")){
                        responses = bossCommands.activeTasks(chatId);
                    }else{
                        status = "worker_active_tasks";
                        responses.add(bossCommands.workWithWorkers(chatId));
                    }
                    break;
                case "/closed_tasks":
                    status = "worker_closed_tasks";
                    responses.add(bossCommands.workWithWorkers(chatId));
                    break;
                case "/workers": ;
                    responses = bossCommands.workersList(chatId);
                    break;
                case "/add_project":
                    responses.add(bossCommands.addingProject(chatId));
                    status = "adding_project";
                    break;
                case "/task_description":
                    responses.add(bossCommands.entryTaskDescr(chatId));
                    status = "entry_task_descr";
                    break;
                case "/task_employee":
                    responses.add(bossCommands.entryTaskEmployee(chatId));
                    status = "entry_task_employee";
                    break;
                case "/planned_labor_costs":
                    responses.add(bossCommands.entryPlannedLaborCosts(chatId));
                    status = "entry_planned_labor_costs";
                    break;
                case "/task_planned_end":
                    responses.add(bossCommands.entryTaskDeadline(chatId));
                    status = "entry_task_planned_end";
                    break;
                case "/task_difficulty":
                    responses.add(bossCommands.entryTaskDifficulty(chatId));
                    status = "entry_task_difficulty";
                    break;
                case "/save_task":
                    responses = bossCommands.saveTask(chatId, task);
                    break;
                case "/del_task":
                    responses.add(bossCommands.deleteTask(chatId, task));
                    break;
                case "/watch_projects":
                    responses.add(bossCommands.watchProjects(chatId));
                    break;
                case "/active_projects":
                    responses = bossCommands.showProjectsByStatus(chatId, "active");
                    break;
                case "/inactive_projects":
                    responses = bossCommands.showProjectsByStatus(chatId, "inactive");
                    break;
                case "/new_status":
                    responses.add(bossCommands.activeProjectButtonList(chatId));
                    status = "changing_status";
                    break;
                case "/add_project_task":
                    responses.add(bossCommands.activeProjectButtonList(chatId));
                    status = "adding_task_by_project";
                    break;
                case "/cancel":
                    responses.add(new Response());
                    status = "main";
                    break;
                case "/info":
                    responses.add(bossCommands.info(chatId));
                    status = "worker";
                    break;
                case "/delete_employee":
                    Long emp_id = Long.valueOf(callback_list.get(1));
                    responses = bossCommands.fire(chatId, emp_id);
                    break;
                case "/today":
                    responses = bossCommands.getTodayWork(chatId);
                    break;
                case "/yesterday":
                    responses = bossCommands.getYesterdayWork(chatId);
                    break;
                case "/time_per_task":
                    responses.add(bossCommands.pojectListButtons(update.getCallbackQuery().getFrom().getId()));
                    break;
                case "/working":
                    responses.add(bossCommands.statusChanged(chatId, project, "В работе"));
                    break;
                case "/canceled":
                    responses.add(bossCommands.statusChanged(chatId, project, "Отменено"));
                    break;
                case "/finished":
                    responses.add(bossCommands.statusChanged(chatId,  project, "Завершено"));
                    break;
                case "/report_day":
                    responses.add(bossCommands.dayReport(chatId));
                    break;
                case "/report_period":
                    responses.add(bossCommands.periodReport(chatId));
                    break;
                case "/timeentries_for_period_report":
                    responses.add(bossCommands.timeentriesForPeriod(chatId));
                    status = "timeentries_for_period_report";
                    break;
                case "/overdue_tasks_by_period_report":
                    status = "overdue_tasks_by_period_report";
                    responses.add(bossCommands.overdueTasksByPeriod(chatId));
                    break;
                case "/employee_stats_by_period_report":
                    status = "employee_stats_by_period_report";
                    responses.add(bossCommands.enterPeriod(chatId));
                    break;
                case "/tasks_stats_by_period_report":
                    status = "tasks_stats_by_period_report";
                    responses.add(bossCommands.enterPeriod(chatId));
                    break;
                default:
                    switch (status){
                        case "changing_status":
                            project = projectService.projectById(Long.valueOf(update.getCallbackQuery().getData()));     // error For input string: "/cancel"
                            responses.add(bossCommands.newProjectStatus(chatId, project));
                            break;
                        case "adding_task_by_project":
                            Long projectId = Long.valueOf(update.getCallbackQuery().getData());
                            responses.add(bossCommands.addingTask(chatId, projectId));
                            task = taskService.findNewTask();
                            break;
                    }
            }
        }
        return responses;
    }

    public File getFile(Update update) throws ParseException {
        if (update.hasMessage()){
            String message = update.getMessage().getText();
            List<String> dates = Arrays.asList(message.split("-"));
            SimpleDateFormat formatter = new SimpleDateFormat("dd.M.yyyy");
            Date from;
            Date to;
            switch (status) {
                case "timeentries_for_period_report":
                    if (dates.size() == 1) {
                        Date date = formatter.parse(dates.get(0));
                        return bossCommands.generatePeriodTimeEntries(date, date);
                    } else {
                        from = formatter.parse(dates.get(0));
                        to = formatter.parse(dates.get(1));
                        return bossCommands.generatePeriodTimeEntries(from, to);
                    }
                case "overdue_tasks_by_period_report":
                    if (dates.size() == 1) {
                        Date date = formatter.parse(dates.get(0));
                        return bossCommands.generateOverdueTasksReport(date, date);
                    } else {
                        from = formatter.parse(dates.get(0));
                        to = formatter.parse(dates.get(1));
                        return bossCommands.generateOverdueTasksReport(from, to);
                    }
                case "employee_stats_by_period_report":
                    from = formatter.parse(dates.get(0));
                    to = formatter.parse(dates.get(1));
                    return bossCommands.generateEmployeeInfoReport(from, to);
                default:
                    from = formatter.parse(dates.get(0));
                    to = formatter.parse(dates.get(1));
                    return bossCommands.generateTasksStatsReport(from, to);
            }
        } else {
            switch (update.getCallbackQuery().getData()) {
            /*case "/overdue_tasks_report":
                return bossCommands.generateOverdueTasksReport();
            case "/employee_info_report":
                return bossCommands.generateEmployeeInfoReport();
            default:
                String id = Arrays.asList(update.getCallbackQuery().getData().split(" ")).get(1);
                Project project_rep = projectService.projectById(Long.valueOf(id));
                return bossCommands.generateTimePerTaskReport(project_rep);*/
                case "/overdue_tasks_report":
                    return bossCommands.generateOverdueTasksReport();
                case "/employee_stats_report":
                    return bossCommands.generateEmployeeInfoReport();
                case "/tasks_stats_report":
                    return bossCommands.generateTasksStatsReport();
                default:
                    String id = Arrays.asList(update.getCallbackQuery().getData().split("_")).get(0);
                    Project project_rep = projectService.projectById(Long.valueOf(id));
                    return bossCommands.generateDaylyTimePerTaskReport(project_rep);
            }
        }
    }
}
