package kirill.kandrashin.tgbot.domain.task;

import kirill.kandrashin.tgbot.domain.work.Work;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    private Long id;
    private String description;
    private Integer difficulty;
    private Integer planned_labor_costs;
    private String status;
    private Date created_date;
    private Date start_date;
    private Date planned_end;
    private Date end_date;

    private List<Work> works = new ArrayList<>();

    public void addWork(Work work){
        this.works.add(work);
    }
}




