package kirill.kandrashin.tgbot.domain.bot.mail;

import kirill.kandrashin.tgbot.domain.bot.Bot;
import kirill.kandrashin.tgbot.domain.bot.Keyboard;
import kirill.kandrashin.tgbot.domain.bot.Response;
import kirill.kandrashin.tgbot.domain.bot.roleWorker.WorkerCommands;
import kirill.kandrashin.tgbot.domain.employee.Employee;
import kirill.kandrashin.tgbot.domain.employee.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MailController {
    private final EmployeeService employeeService;
    private final Bot bot;

    @GetMapping(value = "/activate/{code}")
    public ResponseEntity<?> activate(@PathVariable String code){
        Employee employee = employeeService.findEmployeeByCode(code);
        employeeService.updateEmployeeStatus(employee, "подтвержден");
        String message = "Вы подтвердили свою личность. Пожалуйста, дождитесь обработки Вашей заявки руководителем";
        Response emp_response = new Response(employee.getChatId(), message, "empty");
        Employee boss = employeeService.findEmployeeByRole("Boss");
        Response responseToBoss = new Response(boss.getChatId(), "Предоставить пользователю доступ к боту?" + "\n" +
                "ФИО: " + employee.getEmp_name() + "\n" +
                "Почта: " + employee.getEmp_mail(), "confirming");
        List<Response> responses = new ArrayList<>();
        responses.add(emp_response);
        responses.add(responseToBoss);
        bot.response(responses);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}



