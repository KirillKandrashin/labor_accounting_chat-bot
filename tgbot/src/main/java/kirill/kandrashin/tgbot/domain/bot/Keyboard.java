package kirill.kandrashin.tgbot.domain.bot;

import kirill.kandrashin.tgbot.domain.bot.roleBoss.BossInlineKeyboard;
import kirill.kandrashin.tgbot.domain.bot.roleWorker.WorkerInlineKeyboard;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class Keyboard {

    private final BossInlineKeyboard bossInlineKeyboard;
    private final WorkerInlineKeyboard workerInlineKeyboard;

    public SendMessage setButtons(SendMessage sendMessage, String type) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        KeyboardRow keyboardThirdRow = new KeyboardRow();

        switch (type){
            case "start":
                keyboardRow.add(new KeyboardButton("Старт"));
                keyboard.add(keyboardRow);
                replyKeyboardMarkup.setKeyboard(keyboard);
                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                break;
            case "naming":
                keyboardRow.add(new KeyboardButton("Стоп"));
                keyboard.add(keyboardRow);
                replyKeyboardMarkup.setKeyboard(keyboard);
                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                break;
            case "new_user":
                sendMessage.setReplyMarkup(workerInlineKeyboard.newUser());
                return sendMessage;
            case "ur_info":
                sendMessage.setReplyMarkup(workerInlineKeyboard.urInfo());
                return sendMessage;
            case "cancel":
                keyboardRow.add(new KeyboardButton("Отмена"));
                keyboard.add(keyboardRow);
                replyKeyboardMarkup.setKeyboard(keyboard);
                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                break;
            case "boss_main":
                keyboardFirstRow.add(new KeyboardButton("Сотрудники"));
                keyboardFirstRow.add(new KeyboardButton("Проекты"));

                keyboardSecondRow.add(new KeyboardButton("Список задач"));
                keyboardSecondRow.add(new KeyboardButton("Работа сотрудников"));

                keyboardThirdRow.add(new KeyboardButton("Сформировать отчет"));

                keyboard.add(keyboardFirstRow);
                keyboard.add(keyboardSecondRow);
                keyboard.add(keyboardThirdRow);
                replyKeyboardMarkup.setKeyboard(keyboard);
                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                break;
            case "worker_main":
                keyboardFirstRow.add(new KeyboardButton("Начать учет времени"));

                keyboardSecondRow.add(new KeyboardButton("Задачи"));
                keyboardSecondRow.add(new KeyboardButton("Добавить локальную задачу"));

                keyboardThirdRow.add(new KeyboardButton("Просмотреть трудозатраты"));

                //keyboardThirdRow.add(new KeyboardButton("Помощь"));

                keyboard.add(keyboardFirstRow);
                keyboard.add(keyboardSecondRow);
                keyboard.add(keyboardThirdRow);
                replyKeyboardMarkup.setKeyboard(keyboard);
                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                break;
            /*case "choose_task":
                sendMessage.setReplyMarkup(workerInlineKeyboard.choose_active_task());
                return sendMessage;
                break;*/
            case "edit_time_entries":
                sendMessage.setReplyMarkup(workerInlineKeyboard.editTimeEntries(sendMessage));
                break;
            case "active_tasks":
                sendMessage.setReplyMarkup(workerInlineKeyboard.active_tasks(Long.valueOf(sendMessage.getChatId())));
                return sendMessage;
            case "guessing":
                sendMessage.setReplyMarkup(bossInlineKeyboard.guess(sendMessage));
                return sendMessage;
            case "working":
                keyboardFirstRow.add(new KeyboardButton("Завершить выполнение"));
                keyboardSecondRow.add(new KeyboardButton("Начать работу по новой задаче"));
                keyboard.add(keyboardFirstRow);
                keyboard.add(keyboardSecondRow);
                replyKeyboardMarkup.setKeyboard(keyboard);
                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                break;
            case "work_with_workers":
                sendMessage.setReplyMarkup(bossInlineKeyboard.workers());
                return sendMessage;
            case "projects_work":
                sendMessage.setReplyMarkup(bossInlineKeyboard.projects_work());
                return sendMessage;
            case "project_types":
                sendMessage.setReplyMarkup(bossInlineKeyboard.project_types());
                return sendMessage;
            case "active_project_button_list":
                sendMessage.setReplyMarkup(bossInlineKeyboard.active_project_button_list());
                return sendMessage;
            case "tasks_type":
                sendMessage.setReplyMarkup(bossInlineKeyboard.tasks_type());
                return sendMessage;
            case "time_entries":
                sendMessage.setReplyMarkup(workerInlineKeyboard.tasks(Long.valueOf(sendMessage.getChatId())));
                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                return sendMessage;
            case "change_status":
                keyboardFirstRow.add(new KeyboardButton("Сменить статус у задачи"));
                keyboardFirstRow.add(new KeyboardButton("Отмена"));
                keyboard.add(keyboardFirstRow);
                replyKeyboardMarkup.setKeyboard(keyboard);
                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                break;
            case "choose_status":
                sendMessage.setReplyMarkup(workerInlineKeyboard.chooseStatus());
                return sendMessage;
            case "choose_project_status":
                sendMessage.setReplyMarkup(bossInlineKeyboard.chooseStatus());
                return sendMessage;
            case "adding_project_task":
                sendMessage.setReplyMarkup(bossInlineKeyboard.adding_project_task());
                return sendMessage;
            case "confirming":
                sendMessage.setReplyMarkup(bossInlineKeyboard.confirm(sendMessage));
                return sendMessage;
            case "delete_employee_from_info":
                sendMessage.setReplyMarkup(bossInlineKeyboard.workerInfo(sendMessage));
                return sendMessage;
            case "workers_work":
                sendMessage.setReplyMarkup(bossInlineKeyboard.workersWork());
                return sendMessage;
            case "report_type":
                sendMessage.setReplyMarkup(bossInlineKeyboard.reportType());
                return sendMessage;
            case "report_day":
                sendMessage.setReplyMarkup(bossInlineKeyboard.dayReport());
                return sendMessage;
            case "report_period":
                sendMessage.setReplyMarkup(bossInlineKeyboard.periodReport());
                return sendMessage;
            case "report_choose_project":
                sendMessage.setReplyMarkup(bossInlineKeyboard.projects_for_report());
                return sendMessage;
            case "none":
                ReplyKeyboardRemove remove = new ReplyKeyboardRemove();
                remove.setRemoveKeyboard(true);
                sendMessage.setReplyMarkup(remove);
            case "empty":
                break;
        }

        return sendMessage;
    }

    public SendDocument setButtons(SendDocument sendDocument) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        KeyboardRow keyboardThirdRow = new KeyboardRow();

        keyboardFirstRow.add(new KeyboardButton("Сотрудники"));
        keyboardFirstRow.add(new KeyboardButton("Проекты"));

        keyboardSecondRow.add(new KeyboardButton("Список задач"));
        keyboardSecondRow.add(new KeyboardButton("Работа сотрудников"));

        keyboardThirdRow.add(new KeyboardButton("Сформировать отчет"));

        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        keyboard.add(keyboardThirdRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        sendDocument.setReplyMarkup(replyKeyboardMarkup);
        return sendDocument;
    }
}
