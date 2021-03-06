package models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * A class that models the TreasureHunt database table.
 */
@Table(name = "TreasureHunt")
@Entity
public class TreasureHunt extends BaseModel {

    @Id
    public Long id;

    @Constraints.Required
    @ManyToOne
    @JoinTable(
        name = "User",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    public User user;

    @Constraints.Required
    @ManyToOne
    @JoinTable(
        name = "Destination",
        joinColumns = @JoinColumn(name = "destination_id", referencedColumnName = "id"))
    public Destination destination;

    @Constraints.Required
    public String riddle;

    @Constraints.Required
    @JsonDeserialize(using = LocalDateDeserializer.class)
    public LocalDate startDate;

    @Constraints.Required
    @JsonDeserialize(using = LocalDateDeserializer.class)
    public LocalDate endDate;
}
