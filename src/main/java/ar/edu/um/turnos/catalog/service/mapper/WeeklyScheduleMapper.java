package ar.edu.um.turnos.catalog.service.mapper;

import ar.edu.um.turnos.catalog.domain.Professional;
import ar.edu.um.turnos.catalog.domain.WeeklySchedule;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalDTO;
import ar.edu.um.turnos.catalog.service.dto.WeeklyScheduleDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link WeeklySchedule} and its DTO {@link WeeklyScheduleDTO}.
 */
@Mapper(componentModel = "spring")
public interface WeeklyScheduleMapper extends EntityMapper<WeeklyScheduleDTO, WeeklySchedule> {
    @Mapping(target = "professional", source = "professional", qualifiedByName = "professionalId")
    WeeklyScheduleDTO toDto(WeeklySchedule s);

    @Named("professionalId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ProfessionalDTO toDtoProfessionalId(Professional professional);
}
