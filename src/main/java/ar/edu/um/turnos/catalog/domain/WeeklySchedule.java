package ar.edu.um.turnos.catalog.domain;

import ar.edu.um.turnos.catalog.domain.enumeration.Weekday;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalTime;

/**
 * A WeeklySchedule.
 */
@Entity
@Table(name = "weekly_schedule")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class WeeklySchedule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "external_id", nullable = false, unique = true)
    private Long externalId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private Weekday dayOfWeek;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotNull
    @Min(value = 1)
    @Column(name = "slot_duration_minutes", nullable = false)
    private Integer slotDurationMinutes;

    @NotNull
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "category" }, allowSetters = true)
    private Professional professional;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public WeeklySchedule id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExternalId() {
        return this.externalId;
    }

    public WeeklySchedule externalId(Long externalId) {
        this.setExternalId(externalId);
        return this;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public Weekday getDayOfWeek() {
        return this.dayOfWeek;
    }

    public WeeklySchedule dayOfWeek(Weekday dayOfWeek) {
        this.setDayOfWeek(dayOfWeek);
        return this;
    }

    public void setDayOfWeek(Weekday dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return this.startTime;
    }

    public WeeklySchedule startTime(LocalTime startTime) {
        this.setStartTime(startTime);
        return this;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return this.endTime;
    }

    public WeeklySchedule endTime(LocalTime endTime) {
        this.setEndTime(endTime);
        return this;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getSlotDurationMinutes() {
        return this.slotDurationMinutes;
    }

    public WeeklySchedule slotDurationMinutes(Integer slotDurationMinutes) {
        this.setSlotDurationMinutes(slotDurationMinutes);
        return this;
    }

    public void setSlotDurationMinutes(Integer slotDurationMinutes) {
        this.slotDurationMinutes = slotDurationMinutes;
    }

    public Boolean getEnabled() {
        return this.enabled;
    }

    public WeeklySchedule enabled(Boolean enabled) {
        this.setEnabled(enabled);
        return this;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public WeeklySchedule createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public WeeklySchedule updatedAt(Instant updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Professional getProfessional() {
        return this.professional;
    }

    public void setProfessional(Professional professional) {
        this.professional = professional;
    }

    public WeeklySchedule professional(Professional professional) {
        this.setProfessional(professional);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

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

    // prettier-ignore
    @Override
    public String toString() {
        return "WeeklySchedule{" +
            "id=" + getId() +
            ", externalId=" + getExternalId() +
            ", dayOfWeek='" + getDayOfWeek() + "'" +
            ", startTime='" + getStartTime() + "'" +
            ", endTime='" + getEndTime() + "'" +
            ", slotDurationMinutes=" + getSlotDurationMinutes() +
            ", enabled='" + getEnabled() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}
