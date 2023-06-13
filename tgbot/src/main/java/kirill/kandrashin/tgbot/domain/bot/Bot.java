package kirill.kandrashin.tgbot.domain.bot;

import kirill.kandrashin.tgbot.config.BotConfig;
import kirill.kandrashin.tgbot.domain.bot.roleBoss.BossInteraction;
import kirill.kandrashin.tgbot.domain.bot.roleWorker.WorkerInteraction;
import kirill.kandrashin.tgbot.domain.employee.Employee;
import kirill.kandrashin.tgbot.domain.employee.EmployeeService;
import kirill.kandrashin.tgbot.domain.project.Project;
import kirill.kandrashin.tgbot.domain.project.ProjectService;
import kirill.kandrashin.tgbot.domain.task.Task;
import kirill.kandrashin.tgbot.domain.task.TaskService;
import kirill.kandrashin.tgbot.domain.work.Work;
import kirill.kandrashin.tgbot.domain.work.WorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Component
@RequiredArgsConstructor
@EnableScheduling
public class Bot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final BossInteraction bossInteraction;
    private final WorkerInteraction workerInteraction;
    private final Keyboard keyboard;

    private final EmployeeService employeeService;
    private final WorkService workService;
    private final TaskService taskService;
    private final ProjectService projectService;
    private String status;


    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @PostConstruct
    public void botInit() {
        try {
            status = "";
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if ((update.getMessage().getFrom().getUserName() == null) || (!update.getMessage().getFrom().getUserName().equals(botConfig.getBossUsername()))){
                response(workerInteraction.getResponse(update));
            }else {
                if (status.equals("period")){
                    try {
                        sendDocUploadingFile(update, bossInteraction.getFile(update));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    status = "";
                } else {
                    response(bossInteraction.getResponse(update));
                }

            }
        } else if (update.hasCallbackQuery()) {
            callbackResponse(update);
        }
    }

    public void response(List<Response> responses) {
        for (Response response : responses) {
            System.out.println(response);
            SendMessage sendMessage = new SendMessage();
            sendMessage.enableMarkdownV2(true);
            sendMessage.enableHtml(true);
            sendMessage.setChatId(response.getChatId());
            sendMessage.setText(response.getMessage());
            try {
                sendMessage = keyboard.setButtons(sendMessage, response.getKeyboardType());
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void callbackResponse(Update update){
        DeleteMessage deleteMessage = new DeleteMessage(update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId());
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        if (update.getCallbackQuery().getFrom().getUserName() == null || !update.getCallbackQuery().getFrom().getUserName().equals(botConfig.getBossUsername())){                          //.getMessage().getFrom().getUserName().equals(botConfig.getBossUsername())){
            response(workerInteraction.getResponse(update));
        } else {
            if (update.getCallbackQuery().getData().contains("_report")){
                if (update.getCallbackQuery().getData().contains("_period_")){
                    status = "period";
                    response(bossInteraction.getResponse(update));
                }else{
                    try {
                        sendDocUploadingFile(update, bossInteraction.getFile(update));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            }else {
                response(bossInteraction.getResponse(update));
            }
        }
    }

    private void sendDocUploadingFile(Update update, File file){
        SendDocument sendDocumentRequest = new SendDocument();
        if (update.hasCallbackQuery()){
            sendDocumentRequest.setChatId(update.getCallbackQuery().getFrom().getId());
        }else{
            sendDocumentRequest.setChatId(update.getMessage().getFrom().getId());
            sendDocumentRequest = keyboard.setButtons(sendDocumentRequest);
        }
        sendDocumentRequest.setDocument(new InputFile(file));
        try {
            execute(sendDocumentRequest);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "${bot.cron.yesterday}")
    public void sendMsgYesterday() {
        LocalDate now = LocalDate.now();
        LocalDate date;
        if (now.getDayOfWeek().toString().equals("MONDAY")){
            date = LocalDate.now().minusDays(3);
        }else{
            date = LocalDate.now().minusDays(1);
        }

        sendReport(Date.from(date.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant()), Date.from(date.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant()));
    }

    @Scheduled(cron = "${bot.cron.today}")
    public void sendMsgToday() {
        Date date = new Date();
        sendReport(date, date);
    }

    private void sendReport(Date from, Date to) {
        List<Employee> employees = employeeService.findEmployeesWithWorkByDate(from, to);
        for (Employee employee : employees) {
            double sum_hours = 0.0;
            SendMessage sendMessage = new SendMessage();
            sendMessage.enableMarkdown(true);
            sendMessage.setChatId(employee.getChatId());
            SimpleDateFormat DateFormat = new SimpleDateFormat("dd.MM.yyyy");
            String day = DateFormat.format(from);

            for (Task task : employee.getTasks()) {
                for (Work work : task.getWorks()){
                    if (work.getEnd_work() != null){
                        long time = work.getEnd_work().getTime() - work.getStart_work().getTime();
                        sum_hours += (double) time / 1000 / 60/ 60;
                    }
                }
            }
            BigDecimal hours = new BigDecimal(sum_hours).setScale(2, RoundingMode.UP);
            sendMessage.setText("Отчет за ".concat(day).concat(". Итого часов: " + hours));
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Scheduled(cron = "${bot.cron.dblistener.error}")
    private void listenerTasks(){
        List<Response> responses = new ArrayList<>();
        Date now = new Date();
        List<Work> unfinishedWorks = workService.findUnfinishedWork(now);
        long duration;
        double hours;
        for (Work work: unfinishedWorks){
            duration = now.getTime() - work.getStart_work().getTime();
            hours = (double) duration / 1000 / 60;
            if (hours > 1.0){
                workService.endWork(work.getId(), null);
                Task task = taskService.taskByWork(work);
                Employee employee = employeeService.findEmployeeByTask(task);
                responses.add(new Response(employee.getChatId(), "Отсчет времени автоматически прекращен, пожалуйста, уточните информацию во вкладке \"Просмотреть трудозатраты\"", "worker_main"));
            }
        }
        response(responses);
    }

    @Scheduled(cron = "${bot.cron.dblistener.deadline}")
    private void listenerDeadline(){
        List<Response> responses = new ArrayList<>();
        Date now = new Date();
        List<Task> unfinishedTasks = taskService.getActiveTasks();
        List<Project> unfinishedProject = projectService.findActiveProjects();
        for (Task task: unfinishedTasks) {
            double diff = (double) (task.getPlanned_end().getTime() - now.getTime()) / 1000 / 60 / 60 / 24;
            if (diff == 1.0) {
                Employee employee = employeeService.findEmployeeByTask(task);
                responses.add(new Response(employee.getId(), "Завтра срок сдачи задачи #" + task.getId() + "\n" +
                        "Описание: " + task.getDescription() + "\n" +
                        "Статус: " + task.getStatus() + "\n" +
                        "Дедлайн: " + task.getPlanned_end(), "empty"));
            } else if (diff == 3.0) {
                Employee employee = employeeService.findEmployeeByTask(task);
                responses.add(new Response(employee.getId(), "Через 3 дня срок сдачи задачи #" + task.getId() + "\n" +
                        "Описание: " + task.getDescription() + "\n" +
                        "Статус: " + task.getStatus() + "\n" +
                        "Дедлайн: " + task.getPlanned_end(), "empty"));
            } else if (diff == 7.0) {
                Employee employee = employeeService.findEmployeeByTask(task);
                responses.add(new Response(employee.getId(), "Через неделю срок сдачи задачи #" + task.getId() + "\n" +
                        "Описание: " + task.getDescription() + "\n" +
                        "Статус: " + task.getStatus() + "\n" +
                        "Дедлайн: " + task.getPlanned_end(), "empty"));
            }
        }
        Employee boss = employeeService.findEmployeeByRole("Boss");
        for (Project project: unfinishedProject){
            Period period = Period.between(Instant.ofEpochMilli(now.getTime()).atZone(ZoneId.systemDefault()).toLocalDate(),
                    Instant.ofEpochMilli(project.getDeadline().getTime()).atZone(ZoneId.systemDefault()).toLocalDate());
            int months = Math.abs(period.getMonths());
            int days = Math.abs(period.getDays());
            int unfinished_amount = 0;
            if (months == 1 && days == 0){
                List<Task> tasks = project.getTasks();
                for (Task task: tasks){
                    if (task.getStatus().equals("В работе")){
                        unfinished_amount++;
                    }
                }
                double completeness_old = ((double) project.getTasks().size() - (double) unfinished_amount) / project.getTasks().size();
                BigDecimal res_comp = new BigDecimal(completeness_old).setScale(2, RoundingMode.UP);
                int completeness_new = res_comp.multiply(new BigDecimal(100)).intValue();
                responses.add(new Response(boss.getChatId(), "Через месяц срок сдачи проекта #" + project.getId() + " " + project.getTitle() + "\n" +
                                                                     "Дедлайн: " + project.getDeadline() + "\n" +
                                                                     "Завершенность: " + completeness_new + "%", "empty"));
            } else if (months == 0 && days == 14){
                List<Task> tasks = project.getTasks();
                for (Task task: tasks){
                    if (task.getStatus().equals("В работе") || task.getStatus().equals("Ожидание")){
                        unfinished_amount++;
                    }
                }
                double completeness_old = ((double) project.getTasks().size() - (double) unfinished_amount) / project.getTasks().size();
                BigDecimal res_comp = new BigDecimal(completeness_old).setScale(2, RoundingMode.UP);
                int completeness_new = res_comp.multiply(new BigDecimal(100)).intValue();
                responses.add(new Response(boss.getChatId(), "Через 2 недели срок сдачи проекта #" + project.getId() + " " + project.getTitle() + "\n" +
                        "Дедлайн: " + project.getDeadline() + "\n" +
                        "Завершенность: " + completeness_new + "%", "empty"));
            } else if (months== 0 && days == 7){
                List<Task> tasks = project.getTasks();
                for (Task task: tasks){
                    if (task.getStatus().equals("В работе") || task.getStatus().equals("Ожидание")){
                        unfinished_amount++;
                    }
                }
                double completeness_old = ((double) project.getTasks().size() - (double) unfinished_amount) / project.getTasks().size();
                BigDecimal res_comp = new BigDecimal(completeness_old).setScale(2, RoundingMode.UP);
                int completeness_new = res_comp.multiply(new BigDecimal(100)).intValue();
                responses.add(new Response(boss.getChatId(), "Через неделю срок сдачи проекта #" + project.getId() + " " + project.getTitle() + "\n" +
                        "Дедлайн: " + project.getDeadline() + "\n" +
                        "Завершенность: " + completeness_new + "%", "empty"));
            }
        }
        response(responses);
    }
}