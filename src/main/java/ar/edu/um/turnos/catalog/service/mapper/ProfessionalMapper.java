package ar.edu.um.turnos.catalog.service.mapper;

import ar.edu.um.turnos.catalog.domain.Professional;
import ar.edu.um.turnos.catalog.domain.ProfessionalCategory;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalCategoryDTO;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Professional} and its DTO {@link ProfessionalDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProfessionalMapper extends EntityMapper<ProfessionalDTO, Professional> {
    @Mapping(target = "category", source = "category", qualifiedByName = "professionalCategoryName")
    ProfessionalDTO toDto(Professional s);

    @Named("professionalCategoryName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ProfessionalCategoryDTO toDtoProfessionalCategoryName(ProfessionalCategory professionalCategory);
}
