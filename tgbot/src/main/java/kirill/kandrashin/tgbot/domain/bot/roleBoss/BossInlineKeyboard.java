package kirill.kandrashin.tgbot.domain.bot.roleBoss;

import kirill.kandrashin.tgbot.domain.employee.EmployeeService;
import kirill.kandrashin.tgbot.domain.project.Project;
import kirill.kandrashin.tgbot.domain.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BossInlineKeyboard {
    private final EmployeeService employeeService;
    private final ProjectService projectService;

    public InlineKeyboardMarkup workers() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardSecondRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardThirdRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardFourthRow = new ArrayList<>();

        InlineKeyboardButton stats = new InlineKeyboardButton();
        stats.setText("Просмотреть статистику сотрудника");
        stats.setCallbackData("/stats");
        keyboardFirstRow.add(stats);

        InlineKeyboardButton fire = new InlineKeyboardButton();
        fire.setText("Ограничить доступ к боту");
        fire.setCallbackData("/fire");
        keyboardSecondRow.add(fire);

        InlineKeyboardButton tasks = new InlineKeyboardButton();
        tasks.setText("Задачи сотрудника");
        tasks.setCallbackData("/workers_tasks");
        keyboardThirdRow.add(tasks);

        InlineKeyboardButton cancel = new InlineKeyboardButton();
        cancel.setText("Отмена");
        cancel.setCallbackData("/cancel");
        keyboardFourthRow.add(cancel);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardFirstRow);
        rowList.add(keyboardSecondRow);
        rowList.add(keyboardThirdRow);
        rowList.add(keyboardFourthRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup confirm(SendMessage sendMessage) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();

        InlineKeyboardButton yes = new InlineKeyboardButton();
        InlineKeyboardButton no = new InlineKeyboardButton();

        String name_str = new ArrayList<>(Arrays.asList(sendMessage.getText().split("\n"))).get(1);
        String name = new ArrayList<>(Arrays.asList(sendMessage.getText().split(" "))).get(1);
        Long id = employeeService.findEmployeeByName(name).getId();

        yes.setText("Да");
        yes.setCallbackData("/yes " + id);
        no.setText("Нет");
        no.setCallbackData("/no " + id);

        keyboardFirstRow.add(yes);
        keyboardFirstRow.add(no);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardFirstRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup guess(SendMessage sendMessage) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();

        InlineKeyboardButton yes = new InlineKeyboardButton();
        InlineKeyboardButton no = new InlineKeyboardButton();

        List<String> msg_list = new ArrayList<String>(Arrays.asList(sendMessage.getText().split(" ")));
        List<String> name_list = msg_list.subList(5, msg_list.size()-10);
        String name = StringUtils.join(name_list, " ");

        yes.setText("Да");
        yes.setCallbackData("/yes_guess " + name);
        no.setText("Нет");
        no.setCallbackData("/no_guess " + name);

        keyboardFirstRow.add(yes);
        keyboardFirstRow.add(no);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardFirstRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup workerInfo(SendMessage sendMessage) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();

        InlineKeyboardButton del_empl = new InlineKeyboardButton();

        String name = sendMessage.getText();

        del_empl.setText("Удалить сотрудника");
        del_empl.setCallbackData("/delete_employee " + employeeService.findEmployeeByName(name).getId());

        keyboardFirstRow.add(del_empl);



        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardFirstRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup workersWork(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();

        InlineKeyboardButton yesterday = new InlineKeyboardButton();
        InlineKeyboardButton today = new InlineKeyboardButton();

        yesterday.setText("Вчера");
        yesterday.setCallbackData("/yesterday");
        today.setText("Сегодня");
        today.setCallbackData("/today");

        keyboardFirstRow.add(yesterday);
        keyboardFirstRow.add(today);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardFirstRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup reportType(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardSecondRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardThirdRow = new ArrayList<>();

        InlineKeyboardButton day_report = new InlineKeyboardButton();
        day_report.setText("Отчеты на текущий день");
        day_report.setCallbackData("/report_day");
        keyboardFirstRow.add(day_report);

        InlineKeyboardButton period_report = new InlineKeyboardButton();
        period_report.setText("Отчеты за период");
        period_report.setCallbackData("/report_period");
        keyboardSecondRow.add(period_report);

        InlineKeyboardButton cancel = new InlineKeyboardButton();
        cancel.setText("Отмена");
        cancel.setCallbackData("/cancel");
        keyboardThirdRow.add(cancel);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardFirstRow);
        rowList.add(keyboardSecondRow);
        rowList.add(keyboardThirdRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup dayReport(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardSecondRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardThirdRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardFourthRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardFifthRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardSixthRow = new ArrayList<>();

        InlineKeyboardButton overdue_tasks = new InlineKeyboardButton();
        overdue_tasks.setText("Просроченные задачи");
        overdue_tasks.setCallbackData("/overdue_tasks_report");
        keyboardSecondRow.add(overdue_tasks);

        InlineKeyboardButton employee_stats = new InlineKeyboardButton();
        employee_stats.setText("Общая статистика сотрудников");
        employee_stats.setCallbackData("/employee_stats_report");
        keyboardThirdRow.add(employee_stats);

        InlineKeyboardButton tasks_stats = new InlineKeyboardButton();
        tasks_stats.setText("Общая статистика по задачам");
        tasks_stats.setCallbackData("/tasks_stats_report");
        keyboardFourthRow.add(tasks_stats);

        InlineKeyboardButton time_per_task = new InlineKeyboardButton();
        time_per_task.setText("Время по задачам в проекте");
        time_per_task.setCallbackData("/time_per_task");
        keyboardFifthRow.add(time_per_task);

        InlineKeyboardButton cancel = new InlineKeyboardButton();
        cancel.setText("Отмена");
        cancel.setCallbackData("/cancel");
        keyboardSixthRow.add(cancel);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardFirstRow);
        rowList.add(keyboardSecondRow);
        rowList.add(keyboardThirdRow);
        rowList.add(keyboardFourthRow);
        rowList.add(keyboardFifthRow);
        rowList.add(keyboardSixthRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup periodReport(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardSecondRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardThirdRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardFourthRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardFifthRow = new ArrayList<>();

        InlineKeyboardButton today_timeemtries = new InlineKeyboardButton();
        today_timeemtries.setText("Трудозатраты за дату(период)");
        today_timeemtries.setCallbackData("/timeentries_for_period_report");
        keyboardFirstRow.add(today_timeemtries);

        InlineKeyboardButton overdue_tasks = new InlineKeyboardButton();
        overdue_tasks.setText("Просроченные задачи за период");
        overdue_tasks.setCallbackData("/overdue_tasks_by_period_report");
        keyboardSecondRow.add(overdue_tasks);

        InlineKeyboardButton employee_stats = new InlineKeyboardButton();
        employee_stats.setText("Статистика сотрудников за период");
        employee_stats.setCallbackData("/employee_stats_by_period_report");
        keyboardThirdRow.add(employee_stats);

        InlineKeyboardButton tasks_stats = new InlineKeyboardButton();
        tasks_stats.setText("Статистика по задачам");
        tasks_stats.setCallbackData("/tasks_stats_by_period_report");
        keyboardFourthRow.add(tasks_stats);

        InlineKeyboardButton cancel = new InlineKeyboardButton();
        cancel.setText("Отмена");
        cancel.setCallbackData("/cancel");
        keyboardFifthRow.add(cancel);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardFirstRow);
        rowList.add(keyboardSecondRow);
        rowList.add(keyboardThirdRow);
        rowList.add(keyboardFourthRow);
        rowList.add(keyboardFifthRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    /*public InlineKeyboardMarkup report(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardSecondRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardThirdRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardFourthRow = new ArrayList<>();

        InlineKeyboardButton employee_info = new InlineKeyboardButton();
        employee_info.setText("Статистика работников");
        employee_info.setCallbackData("/employee_info_report");
        keyboardFirstRow.add(employee_info);

        InlineKeyboardButton overdue_tasks = new InlineKeyboardButton();
        overdue_tasks.setText("Просроченные задачи");
        overdue_tasks.setCallbackData("/overdue_tasks_report");
        keyboardFirstRow.add(overdue_tasks);

        InlineKeyboardButton time_per_task = new InlineKeyboardButton();
        time_per_task.setText("Время по задачам в проекте");
        time_per_task.setCallbackData("/time_per_task");
        keyboardSecondRow.add(time_per_task);

        InlineKeyboardButton timeentries_for_date = new InlineKeyboardButton();
        timeentries_for_date.setText("Трудозатраты за дату");
        timeentries_for_date.setCallbackData("/timeentries_for_date");
        keyboardThirdRow.add(timeentries_for_date);

        InlineKeyboardButton cancel = new InlineKeyboardButton();
        cancel.setText("Отмена");
        cancel.setCallbackData("/cancel");
        keyboardFourthRow.add(cancel);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardFirstRow);
        rowList.add(keyboardSecondRow);
        rowList.add(keyboardThirdRow);
        rowList.add(keyboardFourthRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }*/

    public InlineKeyboardMarkup projects_for_report(){
        List<Project> projects = projectService.findActiveProjects();
        List<InlineKeyboardButton> buttons = new ArrayList<InlineKeyboardButton>();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> cancelRow = new ArrayList<>();

        for (Project project : projects){
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.valueOf(project.getTitle()));
            button.setCallbackData(project.getId() + "_report");
            buttons.add(button);
        }
        for (InlineKeyboardButton button: buttons){
            List<InlineKeyboardButton> keyboardRow = new ArrayList<>();
            keyboardRow.add(button);
            rowList.add(keyboardRow);
        }

        InlineKeyboardButton cancel = new InlineKeyboardButton();
        cancel.setText("Отмена");
        cancel.setCallbackData("/cancel");
        cancelRow.add(cancel);
        rowList.add(cancelRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup projects_work(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardSecondRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardThirdRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardFourthRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardFifthRow = new ArrayList<>();

        InlineKeyboardButton add_project = new InlineKeyboardButton();
        add_project.setText("Добавить проект");
        add_project.setCallbackData("/add_project");
        keyboardFirstRow.add(add_project);

        InlineKeyboardButton active_projects = new InlineKeyboardButton();
        active_projects.setText("Просмотр проектов");
        active_projects.setCallbackData("/watch_projects");
        keyboardSecondRow.add(active_projects);

        InlineKeyboardButton change_deadline = new InlineKeyboardButton();
        change_deadline.setText("Изменить статус проекта");
        change_deadline.setCallbackData("/new_status");
        keyboardThirdRow.add(change_deadline);

        InlineKeyboardButton add_project_task = new InlineKeyboardButton();
        add_project_task.setText("Добавить задачу по проекту");
        add_project_task.setCallbackData("/add_project_task");
        keyboardFourthRow.add(add_project_task);

        InlineKeyboardButton cancel = new InlineKeyboardButton();
        cancel.setText("Отмена");
        cancel.setCallbackData("/cancel");
        keyboardFifthRow.add(cancel);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardFirstRow);
        rowList.add(keyboardSecondRow);
        rowList.add(keyboardThirdRow);
        rowList.add(keyboardFourthRow);
        rowList.add(keyboardFifthRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup project_types(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardSecondRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardThirdRow = new ArrayList<>();

        InlineKeyboardButton active_projects = new InlineKeyboardButton();
        active_projects.setText("Активные");
        active_projects.setCallbackData("/active_projects");
        keyboardFirstRow.add(active_projects);

        InlineKeyboardButton inactive_projects = new InlineKeyboardButton();
        inactive_projects.setText("Неактивные");
        inactive_projects.setCallbackData("/inactive_projects");
        keyboardSecondRow.add(inactive_projects);

        InlineKeyboardButton cancel = new InlineKeyboardButton();
        cancel.setText("Отмена");
        cancel.setCallbackData("/cancel");
        keyboardThirdRow.add(cancel);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardFirstRow);
        rowList.add(keyboardSecondRow);
        rowList.add(keyboardThirdRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup tasks_type(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardSecondRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardThirdRow = new ArrayList<>();

        InlineKeyboardButton active_projects = new InlineKeyboardButton();
        active_projects.setText("Активные задачи");
        active_projects.setCallbackData("/active_tasks");
        keyboardFirstRow.add(active_projects);

        InlineKeyboardButton add_task_by_employee = new InlineKeyboardButton();
        add_task_by_employee.setText("Закрытые задачи");
        add_task_by_employee.setCallbackData("/closed_tasks");
        keyboardSecondRow.add(add_task_by_employee);

        InlineKeyboardButton cancel = new InlineKeyboardButton();
        cancel.setText("Отмена");
        cancel.setCallbackData("/cancel");
        keyboardThirdRow.add(cancel);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardFirstRow);
        rowList.add(keyboardSecondRow);
        rowList.add(keyboardThirdRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup active_project_button_list() {
        List<Project> projects = projectService.findActiveProjects();
        List<InlineKeyboardButton> buttons = new ArrayList<InlineKeyboardButton>();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> cancelRow = new ArrayList<>();

        for (Project project : projects){
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("#" + project.getId() + " " + project.getTitle());
            button.setCallbackData(String.valueOf(project.getId()));
            buttons.add(button);
        }
        for (InlineKeyboardButton button: buttons){
            List<InlineKeyboardButton> keyboardRow = new ArrayList<>();
            keyboardRow.add(button);
            rowList.add(keyboardRow);
        }

        InlineKeyboardButton cancel = new InlineKeyboardButton();
        cancel.setText("Отмена");
        cancel.setCallbackData("/cancel");
        cancelRow.add(cancel);
        rowList.add(cancelRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup chooseStatus(){
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> workingRow = new ArrayList<>();
        List<InlineKeyboardButton> canceledRow = new ArrayList<>();
        List<InlineKeyboardButton> endedRow = new ArrayList<>();
        List<InlineKeyboardButton> cancelRow = new ArrayList<>();

        InlineKeyboardButton working = new InlineKeyboardButton();
        working.setText("В работе");
        working.setCallbackData("/working");
        workingRow.add(working);
        rowList.add(workingRow);

        InlineKeyboardButton canceled = new InlineKeyboardButton();
        canceled.setText("Отменено");
        canceled.setCallbackData("/canceled");
        canceledRow.add(canceled);
        rowList.add(canceledRow);

        InlineKeyboardButton ended = new InlineKeyboardButton();
        ended.setText("Завершено");
        ended.setCallbackData("/finished");
        endedRow.add(ended);
        rowList.add(endedRow);

        InlineKeyboardButton cancel = new InlineKeyboardButton();
        cancel.setText("Отмена");
        cancel.setCallbackData("/cancel");
        cancelRow.add(cancel);
        rowList.add(cancelRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup adding_project_task(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardSecondRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardThirdRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardFourthRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardFifthRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardSixthRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardSeventhRow = new ArrayList<>();

        InlineKeyboardButton task_employee = new InlineKeyboardButton();
        task_employee.setText("Исполнитель");
        task_employee.setCallbackData("/task_employee");
        keyboardFirstRow.add(task_employee);

        InlineKeyboardButton task_description = new InlineKeyboardButton();
        task_description.setText("Описание задачи");
        task_description.setCallbackData("/task_description");
        keyboardSecondRow.add(task_description);

        InlineKeyboardButton task_type = new InlineKeyboardButton();
        task_type.setText("План выполнения");
        task_type.setCallbackData("/planned_labor_costs");
        keyboardThirdRow.add(task_type);

        InlineKeyboardButton planned_end = new InlineKeyboardButton();
        planned_end.setText("Дедлайн");
        planned_end.setCallbackData("/task_planned_end");
        keyboardFourthRow.add(planned_end);

        InlineKeyboardButton difficulty = new InlineKeyboardButton();
        difficulty.setText("Категория сложности");
        difficulty.setCallbackData("/task_difficulty");
        keyboardFifthRow.add(difficulty);

        InlineKeyboardButton ready = new InlineKeyboardButton();
        ready.setText("Сохранить");
        ready.setCallbackData("/save_task");
        keyboardSixthRow.add(ready);

        InlineKeyboardButton cancel = new InlineKeyboardButton();
        cancel.setText("Отмена");
        cancel.setCallbackData("/del_task");
        keyboardSeventhRow.add(cancel);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardFirstRow);
        rowList.add(keyboardSecondRow);
        rowList.add(keyboardThirdRow);
        rowList.add(keyboardFourthRow);
        rowList.add(keyboardFifthRow);
        rowList.add(keyboardSixthRow);
        rowList.add(keyboardSeventhRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }
}
