package kirill.kandrashin.tgbot.domain.bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private Long chatId;
    private String message;        //текстовый ответ
    private String keyboardType;   //тип клавиатуры, инициализируйщийся при выводе ответа
}



