package ar.edu.um.turnos.catalog.service.mapper;

import ar.edu.um.turnos.catalog.domain.ProfessionalCategory;
import ar.edu.um.turnos.catalog.service.dto.ProfessionalCategoryDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ProfessionalCategory} and its DTO {@link ProfessionalCategoryDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProfessionalCategoryMapper extends EntityMapper<ProfessionalCategoryDTO, ProfessionalCategory> {}
