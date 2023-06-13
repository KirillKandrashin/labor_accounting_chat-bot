package kirill.kandrashin.tgbot.domain.bot.roleBoss;

import kirill.kandrashin.tgbot.domain.bot.Response;
import kirill.kandrashin.tgbot.domain.employee.Employee;
import kirill.kandrashin.tgbot.domain.employee.EmployeeService;
import kirill.kandrashin.tgbot.domain.project.Project;
import kirill.kandrashin.tgbot.domain.project.ProjectService;
import kirill.kandrashin.tgbot.domain.task.Task;
import kirill.kandrashin.tgbot.domain.task.TaskService;
import kirill.kandrashin.tgbot.domain.work.Work;
import kirill.kandrashin.tgbot.domain.work.WorkService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;


@Component
@RequiredArgsConstructor
public class BossCommands {

    private final ProjectService projectService;
    private final EmployeeService employeeService;
    private final TaskService taskService;
    private final WorkService workService;



    public Response start(Long chatId) {
        employeeService.saveEmployee(chatId, "Boss", "подтвержден");
        return new Response(chatId, "Вы начали работу с ботом", "boss_main");
    }


    public List<Response> confirm(Long chatId, Long empId){
        Employee emp = employeeService.findEmployeeById(empId);
        List<Response> ans = new ArrayList<>(); 
        ans.add(new Response(chatId, "Вы предоставили доступ к боту пользователю " + emp.getEmp_name(), "empty"));
        ans.add(new Response(emp.getChatId(), "Вам предоставили доступ к боту", "worker_main"));
        employeeService.confirm(emp.getEmp_name());
        return ans;
    }

    public List<Response> reject(Long chatId ,Long empId){
        Employee emp = employeeService.findEmployeeById(empId);
        List<Response> ans = new ArrayList<>();
        ans.add(new Response(chatId, "Вы не предоставили доступ к боту пользователю " + emp.getEmp_name(), "main_boss"));
        ans.add(new Response(emp.getChatId(), "К сожалению, Вам не предоставили доступ к боту", "start"));
        employeeService.deleteEmployee(emp.getEmp_name());
        return ans;
    }

    public Response workers(Long chatId){
        return new Response(chatId, "Выберете, что именно Вы хотите сделать", "work_with_workers");
    }

    public List<Response> workersList(Long chatId){
        List<Response> ans = new ArrayList<>();
        String template;
        List<Employee> employeeList = employeeService.findAllEmployees("Worker");
        for (Employee employee : employeeList) {
            template = "Сотрудник #" + employee.getId() + " " + employee.getEmp_name();
            ans.add(new Response(chatId, template, "boss_main"));
        }
        return ans;
    }

    public Response workWithWorkers(Long chatId){
        return new Response(chatId, "Введите ФИО интересущего Вас сотрудника", "cancel");
    }

    public List<Response> fire(Long chatId, Long empId){
        List<Response> ans = new ArrayList<>();
        ans.add(new Response(chatId, "Вы удалили сорудника  " + employeeService.findEmployeeById(empId).getEmp_name(), "boss_main"));
        ans.add(new Response(empId, "Вам закрыли доступ к боту", "start"));
        employeeService.unconfirmEmployee(empId);
        return ans;
    }

    public Response employeeInfo(Long chatId, String name){
        Employee employee = employeeService.findEmployeeByName(name);

        double overdue = 0;
        int sum_difficulties = 0;
        int active_tasks = 0;
        int finished = 0;
        double worked_relatively_planned = 0.0;
        for (Task task: employee.getTasks()){
            if (projectService.findProjectByTask(task).getId() != null){
                double sum_time = 0.0;
                if (task.getEnd_date() == null){
                    if (task.getPlanned_end().before(new Date())){
                        overdue++;
                    }
                } else if (task.getPlanned_end().before(task.getEnd_date())) {
                    overdue++;
                }
                sum_difficulties += task.getDifficulty();
                if (task.getEnd_date() != null){
                    for (Work work: task.getWorks()){
                        if (work.getEnd_work() != null){
                            long duration = work.getEnd_work().getTime() - work.getStart_work().getTime();
                            sum_time += (double) duration / 1000 / 60 / 60;
                        }
                    }
                    worked_relatively_planned += (sum_time - (double) task.getPlanned_labor_costs());
                }
                if (task.getStatus().equals("Ожидание") || task.getStatus().equals("В работе")){
                    active_tasks += 1;
                } else if (task.getStatus().equals("Завершено")) {
                    finished += 1;
                }
            }
        }
        double avg_worked_rel_planned = worked_relatively_planned / finished;
        double avg_difficulty = sum_difficulties / employee.getTasks().size();
        if (avg_worked_rel_planned > 0){
            return new Response(chatId, "Сотрудник: " + employee.getEmp_name() + "\n" +
                                                "Выполнено задач: " + finished + "\n" +
                                                "Активных задач: " + active_tasks + "\n" +
                                                "Коэффициент просроченных задач: "+ overdue / employee.getTasks().size() + "\n" +
                                                "Средний уровень задач: " + avg_difficulty + "\n" +
                                                "В среднем выполняет задачи дольше плана на: " + avg_worked_rel_planned, "boss_main");
        } else {
            return new Response(chatId, "Сотрудник: " + employee.getEmp_name() + "\n" +
                    "Выполнено задач: " + finished + "\n" +
                    "Активных задач: " + active_tasks + "\n" +
                    "Коэффициент просроченных задач: "+ overdue / employee.getTasks().size() + "\n" +
                    "Средний уровень задач: " + avg_difficulty + "\n" +
                    "В среднем выполняет задачи быстрее плана на: " + avg_worked_rel_planned, "boss_main");
        }
    }

    public Response chooseTaskType(Long chatId){
        return new Response(chatId, "Выберете тип Вас интересующих задач", "tasks_type");
    }

    public List<Response> showActiveTask(Long chatId, Long employee_id){
        List<Response> responses = new ArrayList<>();
        List<Task> tasks = taskService.activeTasksByEmployeeId(employee_id);
        for (Task task: tasks){
            Project project = projectService.findProjectByTask(task);
            List<Work> works = task.getWorks();
            double sum_time = 0.0;
            for (Work work : works){
                if (work.getEnd_work() != null){
                    long duration = work.getEnd_work().getTime() - work.getStart_work().getTime();
                    sum_time += duration / 1000 / 60 / 60;
                }
            }
            if (projectService.findProjectByTask(task).getId() != null){
                responses.add(new Response(chatId, "Задача #" + task.getId() + " " + task.getDescription() + "\n" +
                                                           "Проект #" +  project.getId() + " " + project.getTitle() + "\n" +
                                                           "Статус: " + task.getStatus() + "\n" +
                                                           "Сложность: " + task.getDifficulty() + "\n" +
                                                           "Дедлайн: " + task.getPlanned_end() + "\n" +
                                                           "План: " + task.getPlanned_labor_costs() + "\n" +
                                                           "Работы: " + sum_time + " часов", "boss_main"));
            }

        }
        return responses;
    }

    public List<Response> showFinishedTask(Long chatId, Long employee_id){
        List<Response> responses = new ArrayList<>();
        List<Task> tasks = taskService.endedTasksByEmployeeId(employee_id);
        for (Task task: tasks){
            Project project = projectService.findProjectByTask(task);
            List<Work> works = task.getWorks();
            double sum_time = 0.0;
            for (Work work : works){
                if (work.getEnd_work() != null){
                    long duration = work.getEnd_work().getTime() - work.getStart_work().getTime();
                    sum_time += duration / 1000 / 60 / 60;
                }
            }
            if (projectService.findProjectByTask(task).getId() != null){
                responses.add(new Response(chatId, "Задача #" + task.getId() + " " + task.getDescription() + "\n" +
                        "Проект #" +  project.getId() + " " + project.getTitle() + "\n" +
                        "Статус: " + task.getStatus() + "\n" +
                        "Сложность: " + task.getDifficulty() + "\n" +
                        "Дедлайн: " + task.getPlanned_end() + "\n" +
                        "Дата выполнения:" + task.getEnd_date() + "\n" +
                        "План: " + task.getPlanned_labor_costs() + "\n" +
                        "Работы: " + sum_time + " часов", "boss_main"));
            }

        }
        return responses;
    }

    public Response workWithProjects(Long chatId){
        return new Response(chatId, "Что Вы хотите сделать?", "projects_work");
    }

    public Response addingProject(Long chatId){
        return new Response(chatId, "Введите, пожалуйста, через пробел название проекта и его дату сдачи в формате дд.мм.гггг", "cancel");
    }

    public Response projectAdded(Long chatId, String message) {
        List<String> message_splitted = new ArrayList<String>(Arrays.asList(message.split(" ")));
        List<String> date_splitted = new ArrayList<String>(Arrays.asList(message_splitted.get(message_splitted.size() - 1).split("\\.")));
        Calendar calendar = new GregorianCalendar(Integer.parseInt(date_splitted.get(2)), Integer.parseInt(date_splitted.get(1)) - 1, Integer.parseInt(date_splitted.get(0)));
        Date date = calendar.getTime();
        List<String> slice = message_splitted.subList(0, message_splitted.size() - 1);
        String name = String.join(" ", slice);
        projectService.addProject(name, "В работе", date);
        Response response = new Response();
        int month = date.getMonth() + 1;
        response.setChatId(chatId);
        response.setMessage("Проект " + name + " успешно добавлен с дедлайном " + date.getDate() + "." + month + "." + String.valueOf(date.getYear()).substring(1) + " со статусом \"В работе\"");
        response.setKeyboardType("boss_main");
        return response;
    }

    public Response watchProjects(Long chatId){
        return new Response(chatId, "Какие отчеты Вас интересуют?", "project_types");
    }

    public List<Response> showProjectsByStatus(Long chatId, String status){
        List<Project> projects;
        Response response = new Response();
        List<Response> responses = new ArrayList<>();
        int done_tasks;
        if (status.equals("active")){
            projects = projectService.findActiveProjects();
        } else{
            projects = projectService.findInactiveProjects();
        }
        for (Project project : projects){
            done_tasks = 0;
            for (Task task : project.getTasks()){
                if ((task.getStatus().equals("Завершено")) || (task.getStatus().equals("Отклонено"))){
                    done_tasks += 1;
                }
            }
            response.setChatId(chatId);
            response.setMessage("Проект #" + project.getId() + " " + project.getTitle() + "\n" +
                    "Статус: " + project.getStatus() + "\n" +
                    "Дедлайн: " + project.getDeadline() + "\n" +
                    "Выполнение: " + (float) done_tasks*100/project.getTasks().size() + "%");
            response.setKeyboardType("boss_main");
            responses.add(response);
        }
        return responses;
    }

    public Response activeProjectButtonList(Long chatId){
        return new Response(chatId, "Выберите, пожалуйста, проект из активных.", "active_project_button_list");
    }

    public Response newProjectStatus(Long chatId, Project project){
        return new Response(chatId, "Проект #" + project.getId() + " " + project.getTitle() + " \n" +
                "Статус: " + project.getStatus(), "choose_project_status");
    }

    public Response statusChanged(Long chatId, Project project, String status){
        projectService.editStatus(project.getId(), status);
        return new Response(chatId, "Статус проекта #" + project.getId() + " " + project.getTitle() + " успешно изменен с \"" + project.getStatus() + "\" на \"" + project + "\"", "change_status");
    }

    public Response info(Long chatId){
        return new Response(chatId, "Введите ФИО интересущего Вас сотрудника" , "cancel");
    }

    public Response workWithTasks(Long chatId){
        return new Response(chatId, "Что Вы хотите сделать?", "tasks_type");
    }

    public List<Response> activeTasks(Long chatId){
        List<Response> responses = new ArrayList<>();
        List<Employee> employees = employeeService.findAllEmployees("Worker");
        for (Employee employee: employees){
            List<Task> tasks = taskService.activeTasksByEmployeeId(employee.getId());
            for (Task task: tasks){
                double hours= 0.0;
                for (Work work: task.getWorks()){
                    if (work.getEnd_work() != null){
                        long duration = work.getEnd_work().getTime() - work.getStart_work().getTime();
                        hours += (double) duration / 1000 / 60 / 60;
                    }
                }
                BigDecimal res_comp = new BigDecimal(hours).setScale(2, RoundingMode.UP);
                Project project = projectService.findProjectByTask(task);
                responses.add(new Response(chatId, "Задача #" + task.getId() + " " + task.getDescription() + "\n" +
                            "Проект: #" + project.getId() + " " + project.getTitle() + "\n" +
                            "Сотрудник: " + employee.getEmp_name() + "\n" +
                            "Статус: " + task.getStatus() + "\n" +
                            "Работы: " +  res_comp + "\n" +
                            "Сложность: " + task.getDifficulty() + "\n" +
                            "План: " + task.getPlanned_labor_costs() + " часов \n" +
                            "Дедлайн: " + task.getPlanned_end(), "worker_main"));
            }
        }
        return  responses;
    }

    public Response addingTask(Long chatId, Long projectId){
        taskService.newTaskByProject(projectId);
        //return new Response(chatId, "Введите, пожалуйста, через точку с запятой (\"; \") ФИО сотрудника, которому предназначается данная задача, ее описание и дедлайн в формате дд.мм.гггг", "cancel");
        return new Response(chatId, "Укажите необходимую информацию по проекту \n" +
                                            "Исполнитель: null \n" +
                                            "Описание задачи: null \n" +
                                            "Тип задачи: null \n" +
                                            "Дедлайн: null \n" +
                                            "Сложность (1-10): null ", "adding_project_task");
    }
    public Response entryTaskDescr(Long chatId){
        return new Response(chatId, "Введите описание задачи", "none");
    }

    public Response entryTaskEmployee(Long chatId){
        return new Response(chatId, "Введите фио исполнителя", "none");
    }

    public Response entryPlannedLaborCosts(Long chatId){
        return new Response(chatId, "Введите план по выполнению задачи в человеко-часах", "none");
    }

    public Response entryTaskDeadline(Long chatId){
        return new Response(chatId, "Введите дедлайн задачи в формате дд.мм.гггг", "none");
    }

    public Response entryTaskDifficulty(Long chatId){
        return new Response(chatId, "Введите категорию сложности задачи, где 1 - самая легкая, 10 - самая сложная", "none");
    }

    public Response taskInfoEntered(Long chatId, Task task){
        return new Response(chatId, "Укажите необходимую информацию по проекту \n" +
                "Исполнитель: " + employeeService.findEmployeeByTask(task).getEmp_name() + "\n" +
                "Описание задачи: " + task.getDescription() + "\n" +
                "План выполнения: " + task.getPlanned_labor_costs() + " часов\n" +
                "Дедлайн: " + task.getPlanned_end() + "\n" +
                "Сложность(1-10): " + task.getDifficulty(), "adding_project_task");
    }

    public List<Response> saveTask(Long chatId, Task task){
        List<Response> responses = new ArrayList<>();
        Employee employee = employeeService.findEmployeeByTask(task);
        Project project = projectService.findProjectByTask(task);
        if (task.getPlanned_labor_costs() == null || task.getPlanned_end() == null || task.getDescription() == null || employee.getId() == null || task.getDifficulty() == null || task.getDifficulty() == 0){
            responses.add(new Response(chatId, "Вы не заполнили всю необходимую информацию", "empty"));
            responses.add(new Response(chatId, "Укажите необходимую информацию по проекту \n" +
                    "Исполнитель: " + employee.getEmp_name() + "\n" +
                    "Описание задачи: " + task.getDescription() + "\n" +
                    "План выполнения: " + task.getPlanned_labor_costs() + " часов \n" +
                    "Дедлайн: " + task.getPlanned_end() + "\n" +
                    "Сложность (1-10): " + task.getDifficulty(), "adding_project_task"));
        } else {
            responses.add(new Response(chatId, "Задача #" + task.getId() + " в рамках проекта #" + project.getId() + " " + project.getTitle() + "\n" +
                                                "Исполнитель: " + employee.getEmp_name() + "\n" +
                                                "Описание задачи: " + task.getDescription() + "\n" +
                                                "План выполнения: " + task.getPlanned_labor_costs() + "\n" +
                                                "Дедлайн: " + task.getPlanned_end() + "\n" +
                                                "Сложность (1-10): " + task.getDifficulty(), "boss_main"));
            responses.add(new Response(employee.getChatId(), "Вам добавлена задача #" + task.getId() + " " + task.getDescription() + "\n" +
                    "Статус: " + task.getStatus() + "\n" +
                    "Дедлайн: " + task.getPlanned_end() + "\n" +
                    "План выполнения: " + task.getPlanned_labor_costs(), "empty"));
        }
        return responses;
    }

    public Response deleteTask(Long chatId, Task task){
        taskService.deleteTask(task);
        return new Response(chatId, "Вы отменили добавление задачи", "boss_main");
    }

    public Response cancel(Long chatId){
        return new Response(chatId, "Вы отменили действие", "boss_main");
    }

    public Response workersWork(Long chatId){
        return new Response(chatId, "За какой период Вы хотите посмотреть трудозатраты?", "workers_work");
    }

    public List<Response> getTodayWork(Long chatId){
        List<Employee> employees = employeeService.findAllEmployees("Worker");
        List<Response> responses = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (Employee employee : employees){
            double hours = 0.0;
            for (Task task: employee.getTasks()){
                List<Work> today_work = workService.findWorkByTaskAndDate(task, java.sql.Date.valueOf(now));
                for (Work works : today_work){
                    System.out.println(works.getEnd_work().getTime() - works.getStart_work().getTime());
                    hours = (double) (works.getEnd_work().getTime() - works.getStart_work().getTime())  / 1000 /60 /60;
                }
            }
            responses.add(new Response(chatId, "Сотрудник: " + employee.getEmp_name() + "\n" +
                    "Время: " + hours, "boss_main"));
        }
        return responses;
    }

    public List<Response> getYesterdayWork(Long chatId){
        List<Employee> employees = employeeService.findAllEmployees("Worker");
        List<Response> responses = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (Employee employee : employees){
            List<Task> tasks = employee.getTasks();
            double hours = 0.0;
            for (Task task: tasks){
                List<Work> yesterday_work = workService.findWorkByTaskAndDate(task, java.sql.Date.valueOf(now.minusDays(1)));
                for (Work works : yesterday_work){
                    System.out.println(works.getEnd_work().getTime() - works.getStart_work().getTime());
                    hours = (double) (works.getEnd_work().getTime() - works.getStart_work().getTime())  / 1000 /60 /60;
                }
            }
            BigDecimal res_comp = new BigDecimal(hours).setScale(2, RoundingMode.UP);
            responses.add(new Response(chatId, "Сотрудник: " + employee.getEmp_name() + "\n" +
                    "Время: " + res_comp, "boss_main"));
        }

        return responses;
    }

    public Response guessEmployee(Long chatId, String name){
        return new Response(chatId, "Возможно, вы имели ввиду пользователя: " + findSimilar(name) + " ? Если нет, проверьте введенные данные и попробуйте еще раз", "guessing");
    }

    public String findSimilar(String name){
        String ans = "";
        Double max = 0.0;
        for (Employee employee: employeeService.findAllEmployees("Worker")){
            double maxLength = Double.max(name.length(), employee.getEmp_name().length());
            if (max <= (maxLength - StringUtils.getLevenshteinDistance(name.toLowerCase(), employee.getEmp_name().toLowerCase())) / maxLength){
                max = (maxLength - StringUtils.getLevenshteinDistance(name.toLowerCase(), employee.getEmp_name().toLowerCase())) / maxLength;
                ans = employee.getEmp_name();
            }
        }
        return ans;
    }

    public Response reportGeneration(Long chatId){
        return new Response(chatId, "Выберите тип отчета", "report_type");
    }

    public Response dayReport(Long chatId){
        return new Response(chatId, "Выберите тип отчета", "report_day");
    }

    public Response periodReport(Long chatId){
        return new Response(chatId, "Выберите тип отчета", "report_period");
    }

    public Response pojectListButtons(Long chatId){
        return new Response(chatId, "Какой проект Вас интересует?", "report_choose_project");
    }

    public Response timeentriesForPeriod(Long chatId){
        return new Response(chatId, "Введите интересующий Вас период в формате dd.mm.yyyy-dd.mm.yyyy. Если Вас интересует конкретный день, введите одну дату в формате dd.mm.yyyy", "cancel");
    }

    public Response overdueTasksByPeriod(Long chatId){
        return new Response(chatId, "Введите интересующий Вас период в формате dd.mm.yyyy-dd.mm.yyyy. Если Вас интересует конкретный день, введите одну дату в формате dd.mm.yyyy", "cancel");
    }

    public Response enterPeriod(Long chatId){
        return new Response(chatId, "Введите интересующий Вас период в формате dd.mm.yyyy-dd.mm.yyyy", "cancel");
    }

    public File generateOverdueTasksReport() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Overdue_Tasks");
        File file_del = new File("tgbot/src/main/java/kirill/kandrashin/tgbot/files/Статистика просроченных задач.xls");
        file_del.delete();
        List<Task> tasks = taskService.getOverdueTasks();

        Cell cell;
        Row row;
        int rownum = 0;
        //
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);

        CellStyle cellDateStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellDateStyle.setDataFormat(createHelper.createDataFormat().getFormat("d-mmm-y"));

        row = sheet.createRow(rownum);

        // Номер
        cell = row.createCell(0);
        cell.setCellValue("Номер, #");
        cell.setCellStyle(style);
        // Описание
        cell = row.createCell(1);
        cell.setCellValue("Описание");
        cell.setCellStyle(style);
        // Статус задачи
        cell = row.createCell(2);
        cell.setCellValue("Статус");
        cell.setCellStyle(style);
        // Назначенный сотрудник
        cell = row.createCell(3);
        cell.setCellValue("Назначенный сотрудник");
        cell.setCellStyle(style);
        // Дата создания задачи
        cell = row.createCell(4);
        cell.setCellValue("Создана");
        cell.setCellStyle(style);
        // Дата завершения
        cell = row.createCell(5);
        cell.setCellValue("Начата");
        cell.setCellStyle(style);
        cell = row.createCell(6);
        cell.setCellValue("Завершена");
        cell.setCellStyle(style);
        // Дедлайн
        cell = row.createCell(7);
        cell.setCellValue("Дедлайн");
        cell.setCellStyle(style);

        cell = row.createCell(8);
        cell.setCellValue("Категория сложности");
        cell.setCellStyle(style);

        for (Task task : tasks) {
            rownum++;
            row = sheet.createRow(rownum);

            // Номер
            cell = row.createCell(0);
            cell.setCellValue(task.getId());
            // Описание
            cell = row.createCell(1);
            cell.setCellValue(task.getDescription());
            // Статус задачи
            cell = row.createCell(2);
            cell.setCellValue(task.getStatus());
            // Назначенный сотрудник
            cell = row.createCell(3);
            cell.setCellValue(employeeService.findEmployeeByTask(task).getEmp_name());
            // Дата создания задачи
            cell = row.createCell(4);
            cell.setCellValue(task.getCreated_date());
            cell.setCellStyle(cellDateStyle);

            try {
                cell = row.createCell(5);
                cell.setCellValue(task.getEnd_date());
            } catch (NullPointerException e){
                cell = row.createCell(5);
                cell.setCellValue("");
            }

            // Дата завершения
            try {
                cell = row.createCell(6);
                cell.setCellValue(task.getEnd_date());
            } catch (NullPointerException e){
                cell = row.createCell(6);
                cell.setCellValue("");
            }
            // Дедлайн
            cell = row.createCell(7);
            cell.setCellValue(task.getPlanned_end());

            cell = row.createCell(8);
            cell.setCellValue(task.getDifficulty());
        }
        File file = new File("tgbot/src/main/java/kirill/kandrashin/tgbot/files/Статистика просроченных задач.xls");
        file.getParentFile().mkdirs();

        FileOutputStream outFile;
        try {
            outFile = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            workbook.write(outFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public File generateOverdueTasksReport(Date from, Date to) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Overdue_Tasks_For");
        File file_del = new File("tgbot/src/main/java/kirill/kandrashin/tgbot/files/Просроченные задачи за период.xls");
        file_del.delete();
        Map<Task, String> list = taskService.getOverdueTasks(from, to);

        Cell cell;
        Row row;
        int rownum = 0;
        //
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);

        CellStyle cellDateStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellDateStyle.setDataFormat(createHelper.createDataFormat().getFormat("d-mmm-y"));

        row = sheet.createRow(rownum);

        // Номер
        cell = row.createCell(0);
        cell.setCellValue("Номер, #");
        cell.setCellStyle(style);
        // Описание
        cell = row.createCell(1);
        cell.setCellValue("Описание");
        cell.setCellStyle(style);
        // Статус задачи
        cell = row.createCell(2);
        cell.setCellValue("Статус");
        cell.setCellStyle(style);
        // Назначенный сотрудник
        cell = row.createCell(3);
        cell.setCellValue("Назначенный сотрудник");
        cell.setCellStyle(style);
        // Дата создания задачи
        cell = row.createCell(4);
        cell.setCellValue("Создана");
        cell.setCellStyle(style);
        // Дата завершения
        cell = row.createCell(4);
        cell.setCellValue("Начата");
        cell.setCellStyle(style);
        cell = row.createCell(6);
        cell.setCellValue("Завершена");
        cell.setCellStyle(style);
        // Дедлайн
        cell = row.createCell(7);
        cell.setCellValue("Дедлайн");
        cell.setCellStyle(style);

        for (Task task : list.keySet()) {
            rownum++;
            row = sheet.createRow(rownum);

            // Номер
            cell = row.createCell(0);
            cell.setCellValue(task.getId());
            // Описание
            cell = row.createCell(1);
            cell.setCellValue(task.getDescription());
            // Статус задачи
            cell = row.createCell(2);
            cell.setCellValue(task.getStatus());
            // Назначенный сотрудник
            cell = row.createCell(3);
            cell.setCellValue(list.get(task));
            // Дата создания задачи
            cell = row.createCell(4);
            cell.setCellValue(task.getCreated_date());
            cell.setCellStyle(cellDateStyle);

            try {
                cell = row.createCell(5);
                cell.setCellValue(task.getEnd_date());
                cell.setCellStyle(cellDateStyle);
            } catch (NullPointerException e){
                cell = row.createCell(5);
                cell.setCellValue("");
            }

            // Дата завершения
            try {
                cell = row.createCell(6);
                cell.setCellValue(task.getEnd_date());
                cell.setCellStyle(cellDateStyle);
            } catch (NullPointerException e){
                cell = row.createCell(6);
                cell.setCellValue("");
            }
            // Дедлайн
            cell = row.createCell(6);
            cell.setCellValue(task.getPlanned_end());
            cell.setCellStyle(cellDateStyle);
        }
        File file = new File("tgbot/src/main/java/kirill/kandrashin/tgbot/files/Просроченные задачи за период.xls");
        file.getParentFile().mkdirs();

        FileOutputStream outFile;
        try {
            outFile = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            workbook.write(outFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public File generateDaylyTimePerTaskReport(Project project) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("time_per_task");
        File file_del = new File("tgbot/src/main/java/kirill/kandrashin/tgbot/files/Выполнение задач по проекту " + project.getTitle() + ".xls");
        file_del.delete();

        Cell cell;
        Row row;
        int rownum = 0;
        //
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);

        row = sheet.createRow(rownum);

        // Номер
        cell = row.createCell(0);
        cell.setCellValue("Номер, #");
        cell.setCellStyle(style);
        // Описание
        cell = row.createCell(1);
        cell.setCellValue("Описание");
        cell.setCellStyle(style);
        // Статус задачи
        cell = row.createCell(2);
        cell.setCellValue("Статус");
        cell.setCellStyle(style);
        // Назначенный сотрудник
        cell = row.createCell(3);
        cell.setCellValue("Назначенный сотрудник");
        cell.setCellStyle(style);

        cell = row.createCell(4);
        cell.setCellValue("План выполнения");
        cell.setCellStyle(style);
        // Дата создания задачи
        cell = row.createCell(5);
        cell.setCellValue("Время");
        cell.setCellStyle(style);
        double sum = 0;
        double sum_by_task;
        for (Task task : project.getTasks()) {
            sum_by_task = 0;
            rownum++;
            row = sheet.createRow(rownum);

            // Номер
            cell = row.createCell(0);
            cell.setCellValue(task.getId());
            // Описание
            cell = row.createCell(1);
            cell.setCellValue(task.getDescription());
            // Статус задачи
            cell = row.createCell(2);
            cell.setCellValue(task.getStatus());
            // Назначенный сотрудник
            cell = row.createCell(3);
            cell.setCellValue(employeeService.findEmployeeByTask(task).getEmp_name());

            cell = row.createCell(4);
            cell.setCellValue(task.getPlanned_labor_costs());
            // Дата создания задачи
            for (Work work: task.getWorks()){
                if (work.getEnd_work() != null){
                    long duration = work.getEnd_work().getTime() - work.getStart_work().getTime();
                    sum_by_task += (double) duration / 1000 / 60 / 60;
                }
            }

            cell = row.createCell(5);
            cell.setCellValue(sum_by_task);
            sum += sum_by_task;
        }
        rownum++;
        row = sheet.createRow(rownum);
        cell = row.createCell(0);
        cell.setCellValue("Всего:");
        cell.setCellStyle(style);
        cell = row.createCell(1);
        cell.setCellValue(sum);

        File file = new File("tgbot/src/main/java/kirill/kandrashin/tgbot/files/Выполнение задач по проекту " + project.getTitle() + ".xls");
        file.getParentFile().mkdirs();

        FileOutputStream outFile;
        try {
            outFile = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            workbook.write(outFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public File generateEmployeeInfoReport(){
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("employee_info");
        File file_del = new File("tgbot/src/main/java/kirill/kandrashin/tgbot/files/Статистика сотрудников.xls");
        file_del.delete();
        List<Employee> employees = employeeService.findAllEmployees("Worker");

        Cell cell;
        Row row;
        int rownum = 0;
        //
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);

        row = sheet.createRow(rownum);

        // Номер
        cell = row.createCell(0);
        cell.setCellValue("Номер, #");
        cell.setCellStyle(style);
        // Описание
        cell = row.createCell(1);
        cell.setCellValue("Имя");
        cell.setCellStyle(style);
        // Статус задачи
        cell = row.createCell(2);
        cell.setCellValue("Кол-во всего задач");
        cell.setCellStyle(style);

        cell = row.createCell(3);
        cell.setCellValue("Кол-во выполненных задач");
        cell.setCellStyle(style);
        // Назначенный сотрудник
        cell = row.createCell(4);
        cell.setCellValue("Коэффициент просроченных задач");
        cell.setCellStyle(style);

        cell = row.createCell(5);
        cell.setCellValue("Среднее опережение плана");
        cell.setCellStyle(style);

        cell = row.createCell(6);
        cell.setCellValue("Кол-во задач 1-2");
        cell.setCellStyle(style);

        cell = row.createCell(7);
        cell.setCellValue("Кол-во задач 3-4");
        cell.setCellStyle(style);

        cell = row.createCell(8);
        cell.setCellValue("Кол-во задач 5-6");
        cell.setCellStyle(style);

        cell = row.createCell(9);
        cell.setCellValue("Кол-во задач 7-8");
        cell.setCellStyle(style);

        cell = row.createCell(10);
        cell.setCellValue("Кол-во задач 9-10");
        cell.setCellStyle(style);

        cell = row.createCell(11);
        cell.setCellValue("Оценка сотрудника");
        cell.setCellStyle(style);

        Map<Integer, Integer> difficulties = new HashMap<>();
        double worked_relatively_planned;
        double overdue;
        double sum_time;
        double avg_worked_rel_planned;
        for (Employee employee : employees) {
            rownum++;
            row = sheet.createRow(rownum);

            // Номер
            cell = row.createCell(0);
            cell.setCellValue(employee.getId());

            cell = row.createCell(1);
            cell.setCellValue(employee.getEmp_name());

            cell = row.createCell(2);
            cell.setCellValue(employee.getTasks().size());

            overdue = 0;
            worked_relatively_planned = 0.0;
            for (int i = 1; i<11; i++){
                difficulties.put(i, 0);
            }
            for (Task task: employee.getTasks()){
                System.out.println(task);
                if (projectService.findProjectByTask(task).getId() != null){
                    sum_time = 0.0;
                    if (task.getEnd_date() != null){
                        for (Work work: task.getWorks()){
                            if (work.getEnd_work() != null){
                                long duration = work.getEnd_work().getTime() - work.getStart_work().getTime();
                                sum_time += (double) duration / 1000 / 60 / 60;
                            }
                        }
                        if (task.getPlanned_end().before(task.getEnd_date())) {
                            overdue++;
                        }
                    }else{
                        if (task.getPlanned_end().before(new Date())){
                            overdue++;
                        }
                    }
                    if (task.getStatus().equals("Завершено")) {
                        difficulties.put(task.getDifficulty(), difficulties.get(task.getDifficulty())+1);
                        worked_relatively_planned += (double) task.getPlanned_labor_costs() - sum_time;
                    }
                }
            }

            cell = row.createCell(3);
            cell.setCellValue(difficulties.values().stream().mapToInt(Integer::intValue).sum());

            cell = row.createCell(4);
            cell.setCellValue(overdue / employee.getTasks().size());

            cell = row.createCell(5);
            try {
                avg_worked_rel_planned = worked_relatively_planned / difficulties.values().stream().mapToInt(Integer::intValue).sum();
                if (avg_worked_rel_planned != 0.0){
                    cell.setCellValue(avg_worked_rel_planned);
                }else{
                    cell.setCellValue("Нет данных");
                }
            } catch(ArithmeticException e){
                cell.setCellValue("Нет данных");
            }

            cell = row.createCell(6);
            cell.setCellValue(difficulties.get(1) + difficulties.get(2));

            cell = row.createCell(7);
            cell.setCellValue(difficulties.get(3) + difficulties.get(4));

            cell = row.createCell(8);
            cell.setCellValue(difficulties.get(5) + difficulties.get(6));

            cell = row.createCell(9);
            cell.setCellValue(difficulties.get(7) + difficulties.get(8));

            cell = row.createCell(10);
            cell.setCellValue(difficulties.get(9) + difficulties.get(10));
        }
        File file = new File("tgbot/src/main/java/kirill/kandrashin/tgbot/files/Статистика сотрудников.xls");
        file.getParentFile().mkdirs();

        FileOutputStream outFile;
        try {
            outFile = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            workbook.write(outFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public File generateEmployeeInfoReport(Date from, Date to){
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("employee_info");
        File file_del = new File("tgbot/src/main/java/kirill/kandrashin/tgbot/files/Статистика сотрудников за период.xls");
        file_del.delete();
        List<Employee> employees = employeeService.findAllEmployees("Worker");

        Cell cell;
        Row row;
        int rownum = 0;
        //
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);

        row = sheet.createRow(rownum);

        // Номер
        cell = row.createCell(0);
        cell.setCellValue("Номер, #");
        cell.setCellStyle(style);
        // Описание
        cell = row.createCell(1);
        cell.setCellValue("Имя");
        cell.setCellStyle(style);
        // Статус задачи
        cell = row.createCell(2);
        cell.setCellValue("Кол-во всего задач");
        cell.setCellStyle(style);

        cell = row.createCell(3);
        cell.setCellValue("Кол-во выполненных задач");
        cell.setCellStyle(style);
        // Назначенный сотрудник
        cell = row.createCell(4);
        cell.setCellValue("Коэффициент просроченных задач");
        cell.setCellStyle(style);

        cell = row.createCell(5);
        cell.setCellValue("Среднее опережение плана");
        cell.setCellStyle(style);

        cell = row.createCell(6);
        cell.setCellValue("Кол-во задач 1-2");
        cell.setCellStyle(style);

        cell = row.createCell(7);
        cell.setCellValue("Кол-во задач 3-4");
        cell.setCellStyle(style);

        cell = row.createCell(8);
        cell.setCellValue("Кол-во задач 5-6");
        cell.setCellStyle(style);

        cell = row.createCell(9);
        cell.setCellValue("Кол-во задач 7-8");
        cell.setCellStyle(style);

        cell = row.createCell(10);
        cell.setCellValue("Кол-во задач 9-10");
        cell.setCellStyle(style);

        cell = row.createCell(11);
        cell.setCellValue("Оценка сотрудника");
        cell.setCellStyle(style);

        double overdue;
        double worked_relatively_planned;
        for (Employee employee : employees) {
            rownum++;
            row = sheet.createRow(rownum);

            // Номер
            cell = row.createCell(0);
            cell.setCellValue(employee.getId());

            cell = row.createCell(1);
            cell.setCellValue(employee.getEmp_name());

            cell = row.createCell(2);
            cell.setCellValue(employee.getTasks().size());

            overdue = 0;
            worked_relatively_planned = 0.0;
            Map<Integer, Integer> difficulties = new HashMap<>();
            for (int i=0; i < 11; i++){
                difficulties.put(i, 0);
            }
            for (Task task: employee.getTasks()){
                System.out.println(task);
                if (task.getEnd_date() != null){
                    if (task.getEnd_date().after(from) && task.getEnd_date().before(to)){
                        if (projectService.findProjectByTask(task).getId() != null){
                            double sum_time = 0.0;
                            for (Work work: task.getWorks()){
                            if (work.getEnd_work() != null){
                                long duration = work.getEnd_work().getTime() - work.getStart_work().getTime();
                                sum_time += (double) duration / 1000 / 60 / 60;
                                }
                            }
                            if (task.getPlanned_end().before(task.getEnd_date())) {
                                overdue++;
                            }
                            if (task.getStatus().equals("Завершено")) {
                                difficulties.put(task.getDifficulty(), difficulties.get(task.getDifficulty()) + 1);
                                worked_relatively_planned += (double) task.getPlanned_labor_costs() - sum_time;
                            }
                        }
                    }
                }

            }
            //double avg_worked_rel_planned = worked_relatively_planned / difficulties.values().stream().mapToInt(Integer::intValue).sum();

            cell = row.createCell(3);
            cell.setCellValue(difficulties.values().stream().mapToInt(Integer::intValue).sum());

            cell = row.createCell(4);
            cell.setCellValue(overdue / difficulties.values().stream().mapToInt(Integer::intValue).sum());

            //cell = row.createCell(5);
            //cell.setCellValue(avg_worked_rel_planned);
            cell = row.createCell(5);
            try {
                double avg_worked_rel_planned = worked_relatively_planned / difficulties.values().stream().mapToInt(Integer::intValue).sum();
                if (avg_worked_rel_planned != 0.0){
                    cell.setCellValue(avg_worked_rel_planned);
                }else{
                    cell.setCellValue("Нет данных");
                }
            } catch(ArithmeticException e){
                cell.setCellValue("Нет данных");
            }

            cell = row.createCell(6);
            cell.setCellValue(difficulties.get(1) + difficulties.get(2));

            cell = row.createCell(7);
            cell.setCellValue(difficulties.get(3) + difficulties.get(4));

            cell = row.createCell(8);
            cell.setCellValue(difficulties.get(5) + difficulties.get(6));

            cell = row.createCell(9);
            cell.setCellValue(difficulties.get(7) + difficulties.get(8));

            cell = row.createCell(10);
            cell.setCellValue(difficulties.get(9) + difficulties.get(10));
        }
        File file = new File("tgbot/src/main/java/kirill/kandrashin/tgbot/files/Статистика сотрудников за период.xls");
        file.getParentFile().mkdirs();

        FileOutputStream outFile;
        try {
            outFile = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            workbook.write(outFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }
    public File generateTasksStatsReport(){
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("employee_info");
        File file_del = new File("tgbot/src/main/java/kirill/kandrashin/tgbot/files/Статистика задач.xls");
        file_del.delete();
        List<Task> tasks = taskService.tasks();

        Cell cell;
        Row row;
        int rownum = 0;
        //
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);

        row = sheet.createRow(rownum);

        cell = row.createCell(0);
        cell.setCellValue("Сложность:");
        cell.setCellStyle(style);

        cell = row.createCell(1);
        cell.setCellValue("1-2");
        cell.setCellStyle(style);

        cell = row.createCell(2);
        cell.setCellValue("3-4");
        cell.setCellStyle(style);

        cell = row.createCell(3);
        cell.setCellValue("5-6");
        cell.setCellStyle(style);

        cell = row.createCell(4);
        cell.setCellValue("7-8");
        cell.setCellStyle(style);

        cell = row.createCell(5);
        cell.setCellValue("9-10");
        cell.setCellStyle(style);


        Map<Integer, Integer> finished_in_time = new HashMap<>();
        Map<Integer, Integer> finished_overdue = new HashMap<>();
        Map<Integer, Integer> overdue_without_comp = new HashMap<>();
        Map<Integer, Double> work_at_plan = new HashMap<>();
        Map<Integer, Integer> difficulties = new HashMap<>();
        for (int i=1; i <11; i++){
            finished_in_time.put(i, 0);
            finished_overdue.put(i, 0);
            overdue_without_comp.put(i, 0);
            work_at_plan.put(i, 0.0);
            difficulties.put(i, 0);
        }
        long time;
        for (Task task : tasks) {
            double hours = 0.0;
            for (Work work: task.getWorks()){
                if (work.getEnd_work() != null){
                    time = work.getEnd_work().getTime() - work.getStart_work().getTime();
                    hours += (double) time / 1000 / 60 / 60;
                }
            }
            if (task.getEnd_date() != null){
                difficulties.put(task.getDifficulty(), difficulties.get(task.getDifficulty())+1);
                work_at_plan.put(task.getDifficulty(), work_at_plan.get(task.getDifficulty()) + hours - task.getPlanned_labor_costs());
                if (task.getEnd_date().before(task.getPlanned_end())){
                    finished_in_time.put(task.getDifficulty(), finished_in_time.get(task.getDifficulty()) + 1);
                } else {
                    finished_overdue.put(task.getDifficulty(), finished_overdue.get(task.getDifficulty()) + 1);
                }
            } else if (new Date().after(task.getPlanned_end())){
                difficulties.put(task.getDifficulty(), difficulties.get(task.getDifficulty())+1);
                overdue_without_comp.put(task.getDifficulty(), overdue_without_comp.get(task.getDifficulty()) + 1);
            }
        }
        rownum++;
        row = sheet.createRow(rownum);

        cell = row.createCell(0);
        cell.setCellValue("Завершено вовремя:");
        cell.setCellStyle(style);

        int j=0;
        for (int i = 1; i<6; i ++){
            cell = row.createCell(i);
            cell.setCellValue(finished_in_time.get(i+j) + finished_in_time.get(i+j+1));
            j=i;
        }

        rownum++;
        row = sheet.createRow(rownum);

        cell = row.createCell(0);
        cell.setCellValue("Просрочено, завершено:");
        cell.setCellStyle(style);

        j = 0;
        for (int i = 1; i<6; i ++){
            cell = row.createCell(i);
            cell.setCellValue(finished_overdue.get(i+j) + finished_overdue.get(i+j+1));
            j=i;
        }

        rownum++;
        row = sheet.createRow(rownum);

        cell = row.createCell(0);
        cell.setCellValue("Просрочено, не завершено:");
        cell.setCellStyle(style);

        j = 0;
        for (int i = 1; i<6; i ++){
            cell = row.createCell(i);
            cell.setCellValue(overdue_without_comp.get(i+j) + overdue_without_comp.get(i+j+1));
            j=i;
        }

        rownum++;
        row = sheet.createRow(rownum);

        cell = row.createCell(0);
        cell.setCellValue("Общая средняя работа относительно плана:");
        cell.setCellStyle(style);

        j = 0;
        for (int i = 1; i<6; i ++){
            cell = row.createCell(i);
            try{
                cell.setCellValue((work_at_plan.get(i+j) + work_at_plan.get(i+j+1)) / (difficulties.get(i+j) + difficulties.get(i+j+1)));
            }catch (ArithmeticException e){
                cell.setCellValue("0");
            }
            j = i;
        }


        File file = new File("tgbot/src/main/java/kirill/kandrashin/tgbot/files/Статистика задач.xls");
        file.getParentFile().mkdirs();

        FileOutputStream outFile;
        try {
            outFile = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            workbook.write(outFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public File generateTasksStatsReport(Date from, Date to){
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("employee_info");
        File file_del = new File("tgbot/src/main/java/kirill/kandrashin/tgbot/files/Статистика задач за период.xls");
        file_del.delete();
        List<Task> tasks = taskService.tasks(from, to);

        Cell cell;
        Row row;
        int rownum = 0;
        //
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);

        row = sheet.createRow(rownum);

        cell = row.createCell(0);
        cell.setCellValue("Сложность");
        cell.setCellStyle(style);

        cell = row.createCell(1);
        cell.setCellValue("1-2");
        cell.setCellStyle(style);

        cell = row.createCell(2);
        cell.setCellValue("3-4");
        cell.setCellStyle(style);

        cell = row.createCell(3);
        cell.setCellValue("5-6");
        cell.setCellStyle(style);

        cell = row.createCell(4);
        cell.setCellValue("7-8");
        cell.setCellStyle(style);

        cell = row.createCell(5);
        cell.setCellValue("9-10");
        cell.setCellStyle(style);


        Map<Integer, Integer> finished_in_time = new HashMap<>();
        Map<Integer, Integer> finished_overdue = new HashMap<>();
        Map<Integer, Integer> overdue_without_comp = new HashMap<>();
        Map<Integer, Double> work_at_plan = new HashMap<>();
        Map<Integer, Integer> difficulties = new HashMap<>();
        for (int i=1; i <11; i++){
            finished_in_time.put(i, 0);
            finished_overdue.put(i, 0);
            overdue_without_comp.put(i, 0);
            work_at_plan.put(i, 0.0);
            difficulties.put(i, 0);
        }
        long time;
        for (Task task : tasks) {
            double hours = 0.0;
            for (Work work: task.getWorks()){
                if (work.getEnd_work() != null){
                    time = work.getEnd_work().getTime() - work.getStart_work().getTime();
                    hours += (double) time / 1000 / 60 / 60;
                }
            }
            if (task.getEnd_date() != null){
                difficulties.put(task.getDifficulty(), difficulties.get(task.getDifficulty())+1);
                work_at_plan.put(task.getDifficulty(), work_at_plan.get(task.getDifficulty()) + hours - task.getPlanned_labor_costs());
                if (task.getEnd_date().before(task.getPlanned_end())){
                    finished_in_time.put(task.getDifficulty(), finished_in_time.get(task.getDifficulty()) + 1);
                } else {
                    finished_overdue.put(task.getDifficulty(), finished_overdue.get(task.getDifficulty()) + 1);
                }
            } else if (new Date().after(task.getPlanned_end())){
                difficulties.put(task.getDifficulty(), difficulties.get(task.getDifficulty())+1);
                overdue_without_comp.put(task.getDifficulty(), overdue_without_comp.get(task.getDifficulty()) + 1);
            }
        }
        rownum++;
        row = sheet.createRow(rownum);

        cell = row.createCell(0);
        cell.setCellValue("Завершено вовремя:");

        int j=0;
        for (int i = 1; i<6; i ++){
            cell = row.createCell(i);
            cell.setCellValue(finished_in_time.get(i+j) + finished_in_time.get(i+j+1));
            j=i;
        }

        rownum++;
        row = sheet.createRow(rownum);

        cell = row.createCell(0);
        cell.setCellValue("Просрочено, завершено:");

        j = 0;
        for (int i = 1; i<6; i ++){
            cell = row.createCell(i);
            cell.setCellValue(finished_overdue.get(i+j) + finished_overdue.get(i+j+1));
            j=i;
        }

        rownum++;
        row = sheet.createRow(rownum);

        cell = row.createCell(0);
        cell.setCellValue("Просрочено, не завершено::");

        j = 0;
        for (int i = 1; i<6; i ++){
            cell = row.createCell(i);
            cell.setCellValue((overdue_without_comp.get(i+j) + overdue_without_comp.get(i+j+1)));
            j=i;
        }

        rownum++;
        row = sheet.createRow(rownum);

        cell = row.createCell(0);
        cell.setCellValue("Общая средняя работа относительно плана:");

        j = 0;
        for (int i = 1; i<6; i ++){
            cell = row.createCell(i);
            try{
                cell.setCellValue((work_at_plan.get(i+j) + work_at_plan.get(i+j+1)) / (difficulties.get(i+j) + difficulties.get(i+j+1)));
            }catch (ArithmeticException e){
                cell.setCellValue("0");
            }
            j = i;
        }


        File file = new File("tgbot/src/main/java/kirill/kandrashin/tgbot/files/Статистика задач за период.xls");
        file.getParentFile().mkdirs();

        FileOutputStream outFile;
        try {
            outFile = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            workbook.write(outFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public File generatePeriodTimeEntries(Date from, Date to) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("time_per_task");
        File file_del = new File("tgbot/src/main/java/kirill/kandrashin/tgbot/files/Трудозатраты за дату(период).xls");
        file_del.delete();

        List<Employee> employees = employeeService.findEmployeesWithWorkByDate(from, to);

        Cell cell;
        Row row;
        int rownum = 0;
        //
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);

        row = sheet.createRow(rownum);

        // Описание
        cell = row.createCell(0);
        cell.setCellValue("Сотрудник");
        cell.setCellStyle(style);
        // Статус задачи
        cell = row.createCell(1);
        cell.setCellValue("Номер задачи");
        cell.setCellStyle(style);
        // Назначенный сотрудник
        cell = row.createCell(2);
        cell.setCellValue("Задача");
        cell.setCellStyle(style);

        cell = row.createCell(3);
        cell.setCellValue("Описание работ");
        cell.setCellStyle(style);
        // Дата создания задачи
        cell = row.createCell(4);
        cell.setCellValue("Время в основное время");
        cell.setCellStyle(style);

        cell = row.createCell(5);
        cell.setCellValue("Время переработки");
        cell.setCellStyle(style);
        List<Employee> employeeAdded = new ArrayList<>();
        double hours;
        double hours_over;
        for (Employee employee: employees){
            double sum_usual = 0.0;
            double sum_over = 0.0;

            rownum++;
            row = sheet.createRow(rownum);
            /*if (employeeAdded.contains(employee)){
                cell = row.createCell(0);
                cell.setCellValue("");
            } else {
                cell = row.createCell(0);
                cell.setCellValue("Всего:");
                cell.setCellStyle(style);
                cell = row.createCell(1);
                cell.setCellValue(sum);

                rownum++;
                cell = row.createCell(0);
                cell.setCellValue(employee.getEmp_name());
            }*/

            cell = row.createCell(0);
            cell.setCellValue(employee.getEmp_name());
            for (Task task: employee.getTasks()){
                if (task.getId() != null){
                    cell = row.createCell(1);
                    cell.setCellValue(task.getId());

                    cell = row.createCell(2);
                    cell.setCellValue(task.getDescription());

                    hours = 0.0;
                    hours_over = 0.0;
                    long duration;
                    for (Work work: task.getWorks()){
                        if (work.getEnd_work() != null){

                            cell = row.createCell(3);
                            cell.setCellValue(work.getDescription());
                            if (work.getEnd_work().getHours() < 18){
                                duration = work.getEnd_work().getTime() - work.getStart_work().getTime();
                                hours += (double) duration / 1000 / 60 / 60;
                            } else {
                                if (work.getStart_work().getHours() >= 18){
                                    duration = work.getEnd_work().getTime() - work.getStart_work().getTime();
                                    hours_over += (double) duration / 1000 / 60 / 60;
                                }else {
                                    hours_over += work.getEnd_work().getHours() - 18;
                                    hours_over += (double) work.getEnd_work().getMinutes() / 60;
                                }
                            }
                        }
                    }
                    sum_usual += hours;
                    sum_over += hours_over;
                    cell = row.createCell(4);
                    cell.setCellValue(hours);

                    cell = row.createCell(5);
                    cell.setCellValue(hours_over);
                    rownum++;
                    row = sheet.createRow(rownum);
                }
            }
            cell = row.createCell(0);
            cell.setCellValue("Всего:");
            cell.setCellStyle(style);

            cell = row.createCell(4);
            cell.setCellValue(sum_usual);

            cell = row.createCell(5);
            cell.setCellValue(sum_over);
        }

        File file = new File("tgbot/src/main/java/kirill/kandrashin/tgbot/files/Трудозатраты за дату(период).xls");
        file.getParentFile().mkdirs();

        FileOutputStream outFile;
        try {
            outFile = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            workbook.write(outFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }
}
