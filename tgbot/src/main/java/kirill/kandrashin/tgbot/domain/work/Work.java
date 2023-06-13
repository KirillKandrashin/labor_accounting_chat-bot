package kirill.kandrashin.tgbot.domain.work;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.sql.Timestamp;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Work {
    private Long id;
    private Date start_work;
    private Date end_work;
    private String description;
}


