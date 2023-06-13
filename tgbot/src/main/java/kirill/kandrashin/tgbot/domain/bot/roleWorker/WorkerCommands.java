package kirill.kandrashin.tgbot.domain.bot.roleWorker;

import kirill.kandrashin.tgbot.domain.bot.Response;
import kirill.kandrashin.tgbot.domain.bot.mail.MailSender;
import kirill.kandrashin.tgbot.domain.employee.Employee;
import kirill.kandrashin.tgbot.domain.employee.EmployeeService;
import kirill.kandrashin.tgbot.domain.task.Task;
import kirill.kandrashin.tgbot.domain.task.TaskService;
import kirill.kandrashin.tgbot.domain.work.Work;
import kirill.kandrashin.tgbot.domain.work.WorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
@RequiredArgsConstructor
public class WorkerCommands {

    private final EmployeeService employeeService;
    private final TaskService taskService;
    private final WorkService workService;
    private final MailSender mailSender;

    public Response start(Long chatId) {
        employeeService.saveEmployee(chatId, "Worker", "новый пользователь");
        return new Response(chatId, "Вы начали работу с ботом. Пожалуйста, введите необходимую информацию о Вас для формирования заявки на доступ к функционалу данного бота." +
                " Данная информация будет сохранена и использоваться исключительно в работе данного сервиса.", "new_user");
    }

    public Response nameInput(Long chatId) {
        return new Response(chatId, "Введите, пожалуйста, Ваше ФИО", "none");
    }

    public Response mailInput(Long chatId) {
        return new Response(chatId, "Введите, пожалуйста, Вашу почту", "none");
    }

    public Response urInfo(Long chatId, List<String> info){
        String name = null;
        String mail = null;
        for (String item: info){
            if (item.contains("@")){
                mail = item;
            } else {
                name = item;
            }
        }
        return new Response(chatId, "Ваши данные:\n" +
                                            "Имя: " + name + "\n" +
                                            "Почта: " + mail, "ur_info");
    }

    public Response saveInfo(Long chatId, Map<Long, List<String>> emp_info){
        String name = "";
        String mail = "";
        List<String> info = emp_info.get(chatId);
        for (String item: info){
            if (item.contains("@")){
                mail = item;
            } else {
                name = item;
            }
        }
        employeeService.updateEmployeeByNameEmail(chatId, name, mail);
        Employee worker = employeeService.findEmployeeByChatId(chatId);
        String message = "Уважаемый " + worker.getEmp_name() + ", для завершения формирования заявки, пожалуйста, активируйте свой аккаунт," +
                " перейдя по ссылке ниже. Если это были не Вы, игнорируйте данное письмо \n" +
                                                               "Ссылка: " + "http://localhost:8080/activate/" + worker.getActivation_code();

        mailSender.send(mail, message);
        return new Response(chatId, "Вам на почту отправлено письмо для подтверждения Вашего аккаунта." +
                " После подтверждения заявка будет отправлена на рассмотрение руководителю", "none");
    }

    public List<Response> returning(Long chatId){
        List<Response> responses = new ArrayList<>();
        Response responseToWorker = new Response(chatId, "С возвращением! Ваша заявка была сформирована и отправлена на подтверждение.", "naming");
        Employee worker = employeeService.findEmployeeByChatId(chatId);
        Employee boss = employeeService.findEmployeeByRole("Boss");
        Response responseToBoss = new Response(boss.getChatId(), "Предоставить пользователю " + "#" + worker.getId() + " " + worker.getEmp_name() + " повторный доступ к боту?", "confirming");
        responses.add(responseToWorker);
        responses.add(responseToBoss);
        return responses;
    }

    public Response stop(Long chatId){
        employeeService.deleteEmployeeByChatId(chatId);
        return new Response(chatId, "Вы прекратили работу с ботом", "start");
    }

    public Response chooseTaskForCounting(Long chatId){
        return new Response(chatId, "Выберете задачу, по которой хотите начать отсчет", "active_tasks");
    }

    public Response countingStarted(Long chatId, String message){
        Long task_id = Long.valueOf(message);
        Calendar cal = Calendar.getInstance();
        workService.startWorking(task_id, cal.getTime());
        Task task = taskService.getTaskById(task_id);
        if (task.getStatus().equals("В работе")){
            return new Response(chatId, "Работа по задаче #" + task_id + " " + task.getDescription() + "" +
                    " начата. Время начала: " + cal.getTime(), "working");
        }else {
            taskService.changeStatusById(task_id, "В работе");
            return new Response(chatId, "Работа по задаче #" + task_id + " " + task.getDescription() + "" +
                    " начата. Статус задачи изменен на \"В работе\". Время начала выполнения: " + cal.getTime(), "working");
        }
    }

    public Response endWork(Long chatId){
        Work work_old = workService.findNewWorkByEmployee(chatId);
        workService.endWork(work_old.getId(), new Date());
        Work work_new = workService.findNewWorkByEmployee(chatId);
        Task task = taskService.taskByWork(work_new);

        long time = work_new.getEnd_work().getTime() - work_new.getStart_work().getTime();
        int hours = (int) (time / (60 * 60 * 1000) % 24);
        double sum = (double) time / 1000 / 60 / 60;
        BigDecimal res_time = new BigDecimal(sum).setScale(2, RoundingMode.UP);
        if (hours == 0){
            return new Response(chatId, "Работа по задаче #" + task.getId() + " " + task.getDescription() + "" +
                    " завершена. Время работы : " + res_time + " часа. Введите кроткое содержание работ", "none");
        } else {
            return new Response(chatId, "Работа по задаче #" + task.getId() + " " + task.getDescription() + "" +
                    " завершена. Время работы : " + res_time + " часов. Введите кроткое содержание работ", "none");
        }
    }

    public Response workRecorded(Long chatId, String message){
        Work work_for_naming = workService.findEndedWorkNoDescByEmp(chatId);
        workService.nameWork(work_for_naming.getId(), message);
        return new Response(chatId, "Работы успешно записаны", "worker_main");
    }

    public List<Response> watchTimeEntries(Long chatId){
        List<Response> responses = new ArrayList<>();
        Employee employee = employeeService.findEmployeeByChatId(chatId);
        List<Work> today_works = workService.getWorkByWorker(employee, new Date());
        double time;
        double sum = 0.0;
        for (Work work: today_works){
            Task task = taskService.taskByWork(work);

            if (work.getEnd_work() == null){
                responses.add(new Response(chatId, "Работы #" + work.getId() + " по задаче # " + task.getId() + " " + task.getDescription() + "\n" +
                        "Время работы: задача не была завершена(учтена)", "edit_time_entries"));
            }else{
                long duration = work.getEnd_work().getTime() - work.getStart_work().getTime();
                time = (double) duration / 1000 / 60 / 60;
                sum += time;
                BigDecimal res_time = new BigDecimal(time).setScale(2, RoundingMode.UP);
                responses.add(new Response(chatId, "Работы по задаче # " + task.getId() + " " + task.getDescription() + "\n" +
                        "Время работы: " + res_time + " часов" + "\n" +
                        "Описание: " + work.getDescription(), "empty"));
            }

        }
        BigDecimal res_sum = new BigDecimal(sum).setScale(2, RoundingMode.UP);
        responses.add(new Response(chatId, "Всего отработано сегодня: " + res_sum + " часов.", "empty"));
        return responses;
    }

    public Response changeTimeEntry(Long chatId){
        return new Response(chatId, "Введите краткое описание работ и время(форма записи - десятичная дробь) в часах через точку с запятой\n" +
                                            "Пример: {описание работ}; 3.0", "none");
    }

    public List<Response> correctTimeEntry(Long chatId, Work  work, String info){
        List<Response> responses = new ArrayList<>();
        String description = new ArrayList<>(Arrays.asList(info.split("; "))).get(0);
        String time = new ArrayList<>(Arrays.asList(info.split("; "))).get(1);
        Employee boss = employeeService.findEmployeeByRole("Boss");
        Employee worker = employeeService.findEmployeeByChatId(chatId);
        responses.add(new Response(boss.getChatId(), "Сотрудник " + worker.getEmp_name() + " запрашивает изменение записи трудозатрат #" + work.getId() + " за " + work.getStart_work() + ". Введенные новые данные: \n" +
                                                             "Время: " + time + " часов\n" +
                                                             "Описание работ: " + description, "confirm_changes"));
        responses.add(new Response(chatId, "Заявка на изменение записи трудозатрат успешно сформирована и отправлена руководителю","worker_main"));
        return responses;
    }

    public Response infoForAddingTask(Long chatId){
        return new Response(chatId, "Вы можете добавить задачу для учета трудозатрат по рабочей деятельности, не отсносящейся к выполнению основных задач. \n" +
                                            "Введите, пожалуйста, описание задачи", "cancel");
    }

    public Response taskAdded(Long chatId, String description){
        Employee employee = employeeService.findEmployeeByChatId(chatId);
        taskService.addTask(employee, description);
        Task task = taskService.getTaskByDescr(description);

        return new Response(chatId, "Задача успешно добавлена под номером #" + task.getId() + " со статусом \"Ожидание\"", "worker_main");
    }

    public Response tasks(Long chatId){
        return new Response(chatId, "Какие задачи Вас интересуют?", "tasks_type");
    }

    public List<Response> activeTasksInfo(Long chatId){
        List<Response> responses = new ArrayList<>();
        Employee employee = employeeService.findEmployeeByChatId(chatId);
        List<Task> active_tasks = taskService.activeTasksByEmployeeId(employee.getId());
        long duration;
        double sum_hours;
        for (Task task : active_tasks){
            sum_hours = 0.0;
            for (Work work : task.getWorks()){
                if (work.getEnd_work() != null){
                    duration = work.getEnd_work().getTime() - work.getStart_work().getTime();
                    sum_hours += (double) duration / 1000 / 60 / 60;
                }
            }
            BigDecimal res_time = new BigDecimal(sum_hours).setScale(2, RoundingMode.UP);
            responses.add(new Response(chatId, "Задача #" + task.getId() + " " + task.getDescription() + "\n" +
                                                       "Статус: " + task.getStatus() + "\n" +
                                                       "План: " + task.getPlanned_labor_costs() + " часов\n" +
                                                       "Работа по задаче: " + res_time + " часов \n" +
                                                       "Дедлайн: " + task.getPlanned_end(), "change_status"));
        }
        return responses;
    }


    public List<Response> endedTasksInfo(Long chatId){
        List<Response> responses = new ArrayList<>();
        Employee employee = employeeService.findEmployeeByChatId(chatId);
        List<Task> ended_tasks = taskService.endedTasksByEmployeeId(employee.getId());
        long duration;
        double sum_hours;
        for (Task task : ended_tasks){
            sum_hours = 0.0;
            for (Work work : task.getWorks()){
                if (work.getEnd_work() != null){
                    duration = work.getEnd_work().getTime() - work.getStart_work().getTime();
                    sum_hours += (double) duration / 1000 / 60 / 60;
                }
            }
            BigDecimal res_time = new BigDecimal(sum_hours).setScale(2, RoundingMode.UP);
            responses.add(new Response(chatId, "Задача #" + task.getId() + " " + task.getDescription() + "\n" +
                    "Статус: " + task.getStatus() + "\n" +
                    "План: " + task.getPlanned_labor_costs() + " часов\n" +
                    "Работа по задаче: " + res_time + " часов \n" +
                    "Дедлайн: " + task.getPlanned_end(), "change_status"));
        }
        return responses;
    }

    public Response exitToMain(Long chatId){
        Response response = new Response();
        response.setChatId(chatId);
        response.setMessage("Вы вернулись на основную вкладку");
        response.setKeyboardType("worker_main");
        return response;
    }

    public Response changingStatus(Long chatId){
        return new Response(chatId, "Укажите номер задачи", "none");
    }

    public Response chooseStatus(Long chatId, Task task){
        if (employeeService.findEmployeeByTask(task).getId() == null){
            return new Response(chatId, "У Вас нет задачи под данным номером. Пожалуйста, укажите номер задачи из Ваших задач", "empty");
        } else {
            return new Response(chatId, "Задача #" + task.getId() + " " + task.getDescription() + " \n" +
                                                "Статус: " + task.getStatus(), "choose_status");
        }
    }

    public Response statusChanged(Long chatId, Task task, String status){
        taskService.changeStatusById(task.getId(), status);

        return new Response(chatId, "Статус задачи #" + task.getId() + " " + task.getDescription() + " успешно изменен с \"" + task.getStatus() + "\" на \"" + status + "\"", "change_status");
    }
}
