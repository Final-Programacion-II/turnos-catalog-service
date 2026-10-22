package ar.edu.um.turnos.catalog.sync.infrastructure.persistence;

import ar.edu.um.turnos.catalog.domain.CatalogSyncState;
import ar.edu.um.turnos.catalog.domain.Professional;
import ar.edu.um.turnos.catalog.domain.ProfessionalCategory;
import ar.edu.um.turnos.catalog.domain.WeeklySchedule;
import ar.edu.um.turnos.catalog.domain.enumeration.Weekday;
import ar.edu.um.turnos.catalog.repository.CatalogSyncStateRepository;
import ar.edu.um.turnos.catalog.repository.ProfessionalCategoryRepository;
import ar.edu.um.turnos.catalog.repository.ProfessionalRepository;
import ar.edu.um.turnos.catalog.repository.WeeklyScheduleRepository;
import ar.edu.um.turnos.catalog.sync.domain.CatalogChange;
import ar.edu.um.turnos.catalog.sync.domain.CatalogSnapshot;
import ar.edu.um.turnos.catalog.sync.ports.out.CatalogReplica;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class JpaCatalogReplica implements CatalogReplica {

    private static final Logger LOG = LoggerFactory.getLogger(JpaCatalogReplica.class);

    private final ProfessionalCategoryRepository categoryRepository;
    private final ProfessionalRepository professionalRepository;
    private final WeeklyScheduleRepository scheduleRepository;
    private final CatalogSyncStateRepository syncStateRepository;

    public JpaCatalogReplica(
        ProfessionalCategoryRepository categoryRepository,
        ProfessionalRepository professionalRepository,
        WeeklyScheduleRepository scheduleRepository,
        CatalogSyncStateRepository syncStateRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.professionalRepository = professionalRepository;
        this.scheduleRepository = scheduleRepository;
        this.syncStateRepository = syncStateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> currentVersion() {
        return syncStateRepository.findById(CatalogSyncState.SINGLETON_ID).map(CatalogSyncState::getCatalogVersion);
    }

    // El snapshot es el catálogo completo: lo que no viene se deshabilita.
    @Override
    public void apply(CatalogSnapshot snapshot) {
        save(snapshot.categories(), snapshot.professionals(), snapshot.schedules(), true);

        CatalogSyncState state = syncStateRepository.findById(CatalogSyncState.SINGLETON_ID).orElseThrow();
        state.setCatalogVersion(snapshot.version());
        state.setLastSyncedAt(Instant.now());
        LOG.info("Snapshot {} aplicado", snapshot.version());
    }

    // Un cambio trae solo lo que cambió: lo demás queda como está.
    @Override
    public void applyChange(CatalogChange change) {
        CatalogSyncState state = syncStateRepository.findById(CatalogSyncState.SINGLETON_ID).orElseThrow();
        Long current = state.getCatalogVersion();

        if (current != null && current >= change.version()) {
            LOG.info("La versión {} ya estaba aplicada", change.version());
            return;
        }
        if (current == null || current != change.version() - 1) {
            throw new IllegalStateException("No se puede aplicar la versión " + change.version() + " sobre la " + current);
        }

        save(change.categories(), change.professionals(), change.schedules(), false);

        state.setCatalogVersion(change.version());
        state.setLastSyncedAt(Instant.now());
        LOG.info("Cambio {} aplicado", change.version());
    }

    private void save(
        List<CatalogSnapshot.Category> categoryItems,
        List<CatalogSnapshot.Professional> professionalItems,
        List<CatalogSnapshot.Schedule> scheduleItems,
        boolean disableMissing
    ) {
        Map<Long, ProfessionalCategory> categories = new HashMap<>();
        for (ProfessionalCategory category : categoryRepository.findAll()) {
            categories.put(category.getExternalId(), category);
        }
        Set<Long> receivedCategories = new HashSet<>();
        for (CatalogSnapshot.Category item : categoryItems) {
            ProfessionalCategory category = categories.get(item.id());
            if (category == null) {
                category = new ProfessionalCategory();
                categories.put(item.id(), category);
            }
            category.setExternalId(item.id());
            category.setName(item.name());
            category.setDescription(item.description());
            category.setEnabled(item.enabled());
            category.setCreatedAt(item.createdAt());
            category.setUpdatedAt(item.updatedAt());
            categoryRepository.save(category);
            receivedCategories.add(item.id());
        }

        Map<Long, Professional> professionals = new HashMap<>();
        for (Professional professional : professionalRepository.findAll()) {
            professionals.put(professional.getExternalId(), professional);
        }
        Set<Long> receivedProfessionals = new HashSet<>();
        for (CatalogSnapshot.Professional item : professionalItems) {
            ProfessionalCategory category = categories.get(item.categoryId());
            if (category == null) {
                throw new IllegalStateException("El profesional " + item.id() + " tiene una categoría inexistente: " + item.categoryId());
            }
            Professional professional = professionals.get(item.id());
            if (professional == null) {
                professional = new Professional();
                professionals.put(item.id(), professional);
            }
            professional.setExternalId(item.id());
            professional.setFirstName(item.firstName());
            professional.setLastName(item.lastName());
            professional.setEnabled(item.enabled());
            professional.setCreatedAt(item.createdAt());
            professional.setUpdatedAt(item.updatedAt());
            professional.setCategory(category);
            professionalRepository.save(professional);
            receivedProfessionals.add(item.id());
        }

        Map<Long, WeeklySchedule> schedules = new HashMap<>();
        for (WeeklySchedule schedule : scheduleRepository.findAll()) {
            schedules.put(schedule.getExternalId(), schedule);
        }
        Set<Long> receivedSchedules = new HashSet<>();
        for (CatalogSnapshot.Schedule item : scheduleItems) {
            Professional professional = professionals.get(item.professionalId());
            if (professional == null) {
                throw new IllegalStateException("El horario " + item.id() + " tiene un profesional inexistente: " + item.professionalId());
            }
            WeeklySchedule schedule = schedules.get(item.id());
            if (schedule == null) {
                schedule = new WeeklySchedule();
                schedules.put(item.id(), schedule);
            }
            schedule.setExternalId(item.id());
            schedule.setDayOfWeek(Weekday.valueOf(item.dayOfWeek().name()));
            schedule.setStartTime(item.startTime());
            schedule.setEndTime(item.endTime());
            schedule.setSlotDurationMinutes(item.slotDurationMinutes());
            schedule.setEnabled(item.enabled());
            schedule.setCreatedAt(item.createdAt());
            schedule.setUpdatedAt(item.updatedAt());
            schedule.setProfessional(professional);
            scheduleRepository.save(schedule);
            receivedSchedules.add(item.id());
        }

        if (disableMissing) {
            for (ProfessionalCategory category : categories.values()) {
                if (!receivedCategories.contains(category.getExternalId())) {
                    category.setEnabled(false);
                }
            }
            for (Professional professional : professionals.values()) {
                if (!receivedProfessionals.contains(professional.getExternalId())) {
                    professional.setEnabled(false);
                }
            }
            for (WeeklySchedule schedule : schedules.values()) {
                if (!receivedSchedules.contains(schedule.getExternalId())) {
                    schedule.setEnabled(false);
                }
            }
        }
    }
}
