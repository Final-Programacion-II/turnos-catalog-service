package ar.edu.um.turnos.catalog.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ProfessionalCategoryTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static ProfessionalCategory getProfessionalCategorySample1() {
        return new ProfessionalCategory().id(1L).externalId(1L).name("name1").description("description1");
    }

    public static ProfessionalCategory getProfessionalCategorySample2() {
        return new ProfessionalCategory().id(2L).externalId(2L).name("name2").description("description2");
    }

    public static ProfessionalCategory getProfessionalCategoryRandomSampleGenerator() {
        return new ProfessionalCategory()
            .id(longCount.incrementAndGet())
            .externalId(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString());
    }
}
