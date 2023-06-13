package kirill.kandrashin.tgbot.domain.employee;

import kirill.kandrashin.tgbot.domain.task.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public void saveEmployee(Long chatId, String role, String emp_status){
        employeeRepository.saveEmployee(chatId, role, emp_status);
    }

    public void updateEmployeeByNameEmail(Long chatId, String name, String mail){
        String activation = UUID.randomUUID().toString();
        employeeRepository.updateEmployeeByNameEmail(chatId, name, mail, activation);
    }

    public Employee findEmployeeByCode(String code){
        return employeeRepository.findEmployeeByCode(code);
    }

    public List<Employee> findAllEmployees(String role){
        return employeeRepository.findAllEmployees(role);
    }

    public Employee findEmployeeByName(String name){
        return employeeRepository.findEmployeeByName(name);
    }

    public Employee findEmployeeByChatId(Long chatId){
        return employeeRepository.findEmployeeByChatId(chatId);
    }

    public Employee findEmployeeByRole(String role){
        return employeeRepository.findEmployeeByRole(role);
    }

    public void confirm(String name){
        employeeRepository.confirm(name);
    }

    public void deleteEmployee(String name){
        employeeRepository.deleteEmployee(name);
    }

    public void deleteEmployeeByChatId(Long chatId) {
        employeeRepository.deleteEmployeeByChatId(chatId);
    }

    public void unconfirmEmployee(Long id){
        employeeRepository.unconfirmEmployee(id);
    }

    public Employee findEmployeeByTask(Task task){
        return employeeRepository.findEmployeeByTask(task);
    }

    public Employee findEmployeeById(Long id){
        return employeeRepository.findEmployeeById(id);
    }

    public List<Employee> findEmployeesWithWorkByDate(Date from, Date to) {
        return employeeRepository.findEmployeesWithWorkByDate(from, to);
    }

    public void updateEmployeeStatus(Employee employee, String status){
        employeeRepository.updateEmployeeStatus(employee, status);
    }
}

