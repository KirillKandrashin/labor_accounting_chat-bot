package kirill.kandrashin.tgbot.domain.bot.roleWorker;

import kirill.kandrashin.tgbot.domain.employee.Employee;
import kirill.kandrashin.tgbot.domain.employee.EmployeeService;
import kirill.kandrashin.tgbot.domain.task.Task;
import kirill.kandrashin.tgbot.domain.task.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkerInlineKeyboard {

    private final TaskService taskService;
    private final EmployeeService employeeService;

    public InlineKeyboardMarkup newUser(){
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> nameRow = new ArrayList<>();
        List<InlineKeyboardButton> mailRow = new ArrayList<>();
        List<InlineKeyboardButton> cancelRow = new ArrayList<>();

        InlineKeyboardButton name = new InlineKeyboardButton();
        name.setText("Имя");
        name.setCallbackData("/name");
        nameRow.add(name);
        rowList.add(nameRow);

        InlineKeyboardButton mail = new InlineKeyboardButton();
        mail.setText("Почта");
        mail.setCallbackData("/mail");
        mailRow.add(mail);
        rowList.add(mailRow);

        InlineKeyboardButton cancel = new InlineKeyboardButton();
        cancel.setText("Отмена");
        cancel.setCallbackData("/cancel_naming");
        cancelRow.add(cancel);
        rowList.add(cancelRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup urInfo(){
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> saveRow = new ArrayList<>();
        List<InlineKeyboardButton> nameRow = new ArrayList<>();
        List<InlineKeyboardButton> mailRow = new ArrayList<>();
        List<InlineKeyboardButton> cancelRow = new ArrayList<>();

        InlineKeyboardButton name = new InlineKeyboardButton();
        name.setText("Имя");
        name.setCallbackData("/name");
        nameRow.add(name);
        rowList.add(nameRow);

        InlineKeyboardButton mail = new InlineKeyboardButton();
        mail.setText("Почта");
        mail.setCallbackData("/mail");
        mailRow.add(mail);
        rowList.add(mailRow);

        InlineKeyboardButton save = new InlineKeyboardButton();
        save.setText("Сохранить");
        save.setCallbackData("/save_info");
        saveRow.add(save);
        rowList.add(saveRow);

        InlineKeyboardButton cancel = new InlineKeyboardButton();
        cancel.setText("Отмена");
        cancel.setCallbackData("/cancel_naming");
        cancelRow.add(cancel);
        rowList.add(cancelRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup tasks(Long chatId) {
        Employee employee = employeeService.findEmployeeByChatId(chatId);
        List<Task> tasks = taskService.tasksByEmployeeId(employee.getId());
        //List<InlineKeyboardButton> buttons = new ArrayList<InlineKeyboardButton>();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> cancelRow = new ArrayList<>();

        for (Task task : tasks){
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.valueOf(task.getId()));
            button.setCallbackData(String.valueOf(task.getId()));
            //buttons.add(button);
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

    public InlineKeyboardMarkup active_tasks(Long chatId){
        Employee employee = employeeService.findEmployeeByChatId(chatId);
        List<Task> tasks = taskService.activeTasksByEmployeeId(employee.getId());
        //List<InlineKeyboardButton> buttons = new ArrayList<InlineKeyboardButton>();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> cancelRow = new ArrayList<>();

        for (Task task : tasks){
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.valueOf(task.getDescription()));
            button.setCallbackData(String.valueOf(task.getId()));
            //buttons.add(button);
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

    public InlineKeyboardMarkup tasks_type(){
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> activeTasksRow = new ArrayList<>();
        List<InlineKeyboardButton> endedTasksRow = new ArrayList<>();
        List<InlineKeyboardButton> cancelRow = new ArrayList<>();

        InlineKeyboardButton active_tasks = new InlineKeyboardButton();
        active_tasks.setText("Активные задачи");
        active_tasks.setCallbackData("/active_tasks");
        activeTasksRow.add(active_tasks);
        rowList.add(activeTasksRow);

        InlineKeyboardButton ended_tasks = new InlineKeyboardButton();
        ended_tasks.setText("Завершенные задачи");
        ended_tasks.setCallbackData("/ended_tasks");
        endedTasksRow.add(ended_tasks);
        rowList.add(endedTasksRow);

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
        List<InlineKeyboardButton> waitingRow = new ArrayList<>();
        List<InlineKeyboardButton> workingRow = new ArrayList<>();
        List<InlineKeyboardButton> canceledRow = new ArrayList<>();
        List<InlineKeyboardButton> endedRow = new ArrayList<>();
        List<InlineKeyboardButton> cancelRow = new ArrayList<>();

        InlineKeyboardButton waiting = new InlineKeyboardButton();
        waiting.setText("Ожидание");
        waiting.setCallbackData("/waiting");
        waitingRow.add(waiting);
        rowList.add(waitingRow);

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

    public InlineKeyboardMarkup editTimeEntries(SendMessage sendMessage){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        String processing = new ArrayList<>(Arrays.asList(sendMessage.getText().split("#"))).get(1);
        String id_str = new ArrayList<>(Arrays.asList(processing.split(" "))).get(0);

        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();

        InlineKeyboardButton changeRow = new InlineKeyboardButton();

        changeRow.setText("Ввести данные");
        changeRow.setCallbackData("/change_time " + id_str);

        keyboardFirstRow.add(changeRow);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardFirstRow);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }
}
