package com.capgemini.training.systemapi.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Day 3 — ArchUnit enforces hexagonal architecture rules.
 * Golden rule: dependencies point inward only.
 */
@AnalyzeClasses(
    packages = "com.capgemini.training.systemapi",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_must_not_depend_on_adapters =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..adapter..", "..application.service..");

    @ArchTest
    static final ArchRule domain_must_not_use_spring =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.springframework..");

    @ArchTest
    static final ArchRule domain_must_not_use_jpa =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("jakarta.persistence..");

    @ArchTest
    static final ArchRule ports_must_not_depend_on_adapters =
        noClasses().that().resideInAPackage("..application.port..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapter..");
}
