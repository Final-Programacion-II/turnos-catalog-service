package ar.edu.um.turnos.catalog.sync;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import ar.edu.um.turnos.catalog.CatalogApp;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packagesOf = CatalogApp.class, importOptions = DoNotIncludeTests.class)
class SyncArchitectureTest {

    @ArchTest
    static final ArchRule dominioSinFrameworks = noClasses()
        .that()
        .resideInAPackage("..sync.domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "com.fasterxml.jackson..", "tools.jackson..");

    @ArchTest
    static final ArchRule dominioYPuertosSinInfraestructura = noClasses()
        .that()
        .resideInAnyPackage("..sync.domain..", "..sync.ports..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..sync.infrastructure..");
}
