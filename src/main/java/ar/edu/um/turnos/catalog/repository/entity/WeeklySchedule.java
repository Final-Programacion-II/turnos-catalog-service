package ar.edu.um.turnos.catalog.repository.entity;

import ar.edu.um.turnos.catalog.repository.entity.enumeration.Weekday;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.time.LocalTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Un horario semanal de atención de un profesional, de la copia local del catálogo.
 */
@Entity
@Table(name = "weekly_schedule")
public class WeeklySchedule extends CatalogEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private Weekday dayOfWeek;

    // Son horas de agenda (09:00 es 09:00 en cualquier lado): se guardan tal cual, sin pasarlas a UTC.
    // Sin esto, hibernate.jdbc.time_zone las corre 3 horas y un horario que termina a las 21 queda guardado como 00:00.
    @NotNull
    @JdbcTypeCode(SqlTypes.LOCAL_TIME)
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @JdbcTypeCode(SqlTypes.LOCAL_TIME)
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotNull
    @Min(value = 1)
    @Column(name = "slot_duration_minutes", nullable = false)
    private Integer slotDurationMinutes;

    @ManyToOne(optional = false)
    @NotNull
    private Professional professional;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Weekday getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Weekday dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public void setSlotDurationMinutes(Integer slotDurationMinutes) {
        this.slotDurationMinutes = slotDurationMinutes;
    }

    public Professional getProfessional() {
        return professional;
    }

    public void setProfessional(Professional professional) {
        this.professional = professional;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WeeklySchedule)) {
            return false;
        }
        return getId() != null && getId().equals(((WeeklySchedule) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "WeeklySchedule{id=" + id + ", externalId=" + getExternalId() + ", " + dayOfWeek + " " + startTime + "-" + endTime + "}";
    }
}
