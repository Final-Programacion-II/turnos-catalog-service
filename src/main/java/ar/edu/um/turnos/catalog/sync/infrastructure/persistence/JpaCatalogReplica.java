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
import ar.edu.um.turnos.catalog.sync.domain.CatalogSnapshot;
import ar.edu.um.turnos.catalog.sync.ports.out.CatalogReplica;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
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

    @Override
    public void apply(CatalogSnapshot snapshot) {
        Map<Long, ProfessionalCategory> categories = applyCategories(snapshot);
        Map<Long, Professional> professionals = applyProfessionals(snapshot, categories);
        applySchedules(snapshot, professionals);

        CatalogSyncState state = syncStateRepository.findById(CatalogSyncState.SINGLETON_ID).orElseThrow();
        state.setCatalogVersion(snapshot.version());
        state.setLastSyncedAt(Instant.now());

        LOG.info("Snapshot {} aplicado", snapshot.version());
    }

    private Map<Long, ProfessionalCategory> applyCategories(CatalogSnapshot snapshot) {
        Map<Long, ProfessionalCategory> byExternalId = new HashMap<>();
        for (ProfessionalCategory category : categoryRepository.findAll()) {
            byExternalId.put(category.getExternalId(), category);
        }

        Set<Long> received = new HashSet<>();
        for (CatalogSnapshot.Category item : snapshot.categories()) {
            ProfessionalCategory category = byExternalId.get(item.id());
            if (category == null) {
                category = new ProfessionalCategory();
                byExternalId.put(item.id(), category);
            }
            category.setExternalId(item.id());
            category.setName(item.name());
            category.setDescription(item.description());
            category.setEnabled(item.enabled());
            category.setCreatedAt(item.createdAt());
            category.setUpdatedAt(item.updatedAt());
            categoryRepository.save(category);
            received.add(item.id());
        }

        for (ProfessionalCategory category : byExternalId.values()) {
            if (!received.contains(category.getExternalId())) {
                category.setEnabled(false);
            }
        }
        return byExternalId;
    }

    private Map<Long, Professional> applyProfessionals(CatalogSnapshot snapshot, Map<Long, ProfessionalCategory> categories) {
        Map<Long, Professional> byExternalId = new HashMap<>();
        for (Professional professional : professionalRepository.findAll()) {
            byExternalId.put(professional.getExternalId(), professional);
        }

        Set<Long> received = new HashSet<>();
        for (CatalogSnapshot.Professional item : snapshot.professionals()) {
            ProfessionalCategory category = categories.get(item.categoryId());
            if (category == null) {
                throw new IllegalStateException("El profesional " + item.id() + " tiene una categoría inexistente: " + item.categoryId());
            }

            Professional professional = byExternalId.get(item.id());
            if (professional == null) {
                professional = new Professional();
                byExternalId.put(item.id(), professional);
            }
            professional.setExternalId(item.id());
            professional.setFirstName(item.firstName());
            professional.setLastName(item.lastName());
            professional.setEnabled(item.enabled());
            professional.setCreatedAt(item.createdAt());
            professional.setUpdatedAt(item.updatedAt());
            professional.setCategory(category);
            professionalRepository.save(professional);
            received.add(item.id());
        }

        for (Professional professional : byExternalId.values()) {
            if (!received.contains(professional.getExternalId())) {
                professional.setEnabled(false);
            }
        }
        return byExternalId;
    }

    private void applySchedules(CatalogSnapshot snapshot, Map<Long, Professional> professionals) {
        Map<Long, WeeklySchedule> byExternalId = new HashMap<>();
        for (WeeklySchedule schedule : scheduleRepository.findAll()) {
            byExternalId.put(schedule.getExternalId(), schedule);
        }

        Set<Long> received = new HashSet<>();
        for (CatalogSnapshot.Schedule item : snapshot.schedules()) {
            Professional professional = professionals.get(item.professionalId());
            if (professional == null) {
                throw new IllegalStateException("El horario " + item.id() + " tiene un profesional inexistente: " + item.professionalId());
            }

            WeeklySchedule schedule = byExternalId.get(item.id());
            if (schedule == null) {
                schedule = new WeeklySchedule();
                byExternalId.put(item.id(), schedule);
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
            received.add(item.id());
        }

        for (WeeklySchedule schedule : byExternalId.values()) {
            if (!received.contains(schedule.getExternalId())) {
                schedule.setEnabled(false);
            }
        }
    }
}
