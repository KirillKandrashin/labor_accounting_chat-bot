package kirill.kandrashin.tgbot.domain.bot.roleWorker;

import kirill.kandrashin.tgbot.domain.bot.Response;
import kirill.kandrashin.tgbot.domain.employee.Employee;
import kirill.kandrashin.tgbot.domain.employee.EmployeeService;
import kirill.kandrashin.tgbot.domain.task.Task;
import kirill.kandrashin.tgbot.domain.task.TaskService;
import kirill.kandrashin.tgbot.domain.work.Work;
import kirill.kandrashin.tgbot.domain.work.WorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

@Component
@RequiredArgsConstructor
public class WorkerInteraction {
    private final WorkerCommands workerCommands;
    private final EmployeeService employeeService;
    private final TaskService taskService;
    private final WorkService workService;
    private Map<Long, String> status = new HashMap<>();
    private Map<Long, List<String>> emp_info = new LinkedHashMap<>();
    private Task task;
    private Work work;


    public List<Response> getResponse(Update update) {
        List<Response> responses = new ArrayList<>();

        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            String message = update.getMessage().getText();

              switch (message) {
                  case "фишка":
                      responses.add(new Response(chatId, "Вам добавлена задача #6" + " " + "Подготовить презентацию нового функционала модуля \"ЛК\"" + "\n" +
                              "Статус: " + "Ожидание" + "\n" +
                              "Дедлайн: " + "01.06.2023" + "\n" +
                              "План выполнения: " + 6 + "часов", "empty"));
                      break;
                  case "фишка2":
                      responses.add(new Response(chatId, "Отчет за 10.05.2023. Итого часов: " + 8.1, "empty"));
                      break;
                    case "/start":
                    case "Старт":
                        if (employeeService.findEmployeeByChatId(chatId).getChatId() == null) {
                            responses.add(workerCommands.start(chatId));
                        } else {
                            responses = workerCommands.returning(chatId);
                        }
                        break;
                    case "Стоп":
                        responses.add(workerCommands.stop(chatId));
                        status.put(chatId, "start");
                        break;
                    case "Начать учет времени":
                        responses.add(workerCommands.chooseTaskForCounting(chatId));
                        status.put(chatId, "task_for_counting");
                        break;
                    case "Завершить выполнение":
                        responses.add(workerCommands.endWork(chatId));
                        status.put(chatId, "work_ended");
                        break;
                    case "Начать работу по новой задаче":
                        responses.add(workerCommands.endWork(chatId));
                        status.put(chatId, "work_ended_start_new");
                        break;
                    case "Добавить локальную задачу":
                        responses.add(workerCommands.infoForAddingTask(chatId));
                        status.put(chatId, "adding_task");
                        break;
                    case "Отмена":
                        responses.add(workerCommands.exitToMain(chatId));
                        status.put(chatId, "");
                        break;
                    case "Задачи":
                        responses.add(workerCommands.tasks(chatId));
                        break;
                    case "Просмотреть трудозатраты":
                        responses = workerCommands.watchTimeEntries(chatId);
                        break;
                    case "Сменить статус у задачи":
                        responses.add(workerCommands.changingStatus(chatId));
                        status.put(chatId, "changing_status");
                        break;
                    default:
                        switch (status.get(chatId)) {
                            case "inputUrName":
                            case "inputUrMail":
                                //emp_info.put(chatId, new ArrayList<>());
                                List<String> info = emp_info.getOrDefault(chatId, new ArrayList<>());
                                //List<String> name = emp_info.get(chatId);
                                info.add(message);
                                //emp_info.put(chatId, name);
                                responses.add(workerCommands.urInfo(chatId, info));
                                emp_info.put(chatId, info);
                                break;
                            /*case "inputUrMail":
                                //emp_info.put(chatId, new ArrayList<>());
                                List<String> info = emp_info.getOrDefault(chatId, new ArrayList<>());
                                *//*List<String> mail = emp_info.get(chatId);
                                mail.add(message);*//*
                                info.add(message);
                                responses.add(workerCommands.urInfo(chatId, info));
                                break;*/
                            case "work_ended":
                                responses.add(workerCommands.workRecorded(chatId, message));
                                break;
                            case "work_ended_start_new":
                                responses.add(workerCommands.workRecorded(chatId, message));
                                responses.add(workerCommands.chooseTaskForCounting(chatId));
                                status.put(chatId, "task_for_counting");
                                break;
                            case "correct_time_entry":
                                responses = workerCommands.correctTimeEntry(chatId, work, message);
                                break;
                            case "adding_task":
                                responses.add(workerCommands.taskAdded(chatId, message));
                                break;
                            case "changing_status":
                                task = taskService.taskById(Long.valueOf(message));
                                responses.add(workerCommands.chooseStatus(chatId, task));
                                break;
                        }
                        break;
                }
        } else if (update.hasCallbackQuery()) {
            System.out.println(update.getCallbackQuery().getData());
            Long chatId = update.getCallbackQuery().getFrom().getId();
            List<String> callback_list = Arrays.asList(update.getCallbackQuery().getData().split(" "));
            switch (callback_list.get(0)){
                case "/name":
                    responses.add(workerCommands.nameInput(chatId));
                    status.put(chatId, "inputUrName");
                    break;
                case "/mail":
                    responses.add(workerCommands.mailInput(chatId));
                    status.put(chatId, "inputUrMail");
                    break;
                case "/save_info":
                    responses.add(workerCommands.saveInfo(chatId, emp_info));
                    emp_info.remove(chatId);
                    break;
                case "/cancel_naming":
                    responses.add(workerCommands.stop(chatId));
                    emp_info.remove(chatId);
                    status.put(chatId, "start");
                case "/cancel":
                    responses.add(new Response());
                    status.put(chatId, "main");
                    break;
                case "/active_tasks":
                    responses = workerCommands.activeTasksInfo(chatId);
                    break;
                case "/ended_tasks":
                    responses = workerCommands.endedTasksInfo(chatId);
                    break;
                case "/waiting":
                    responses.add(workerCommands.statusChanged(chatId, task, "Ожидание"));
                    break;
                case "/working":
                    responses.add(workerCommands.statusChanged(chatId, task, "В работе"));
                    break;
                case "/canceled":
                    responses.add(workerCommands.statusChanged(chatId, task, "Отменено"));
                    break;
                case "/finished":
                    responses.add(workerCommands.statusChanged(chatId,  task, "Завершено"));
                    break;
                case "/change_time":
                    work = workService.getWorkById(Long.valueOf(callback_list.get(1)));
                    responses.add(workerCommands.changeTimeEntry(chatId));
                    status.put(chatId, "correct_time_entry");
                    break;
                default:
                    switch (status.get(chatId)){
                        case "task_for_counting":
                            responses.add(workerCommands.countingStarted(chatId, update.getCallbackQuery().getData()));
                            //status = "worker_counting";
                            status.put(chatId, "worker_counting");
                            break;
                    }
            }
        }

        return responses;
    }
}
