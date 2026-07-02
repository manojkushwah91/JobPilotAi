package com.jobpilot.domain;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

class ArchitectureTest {

    @Test
    void domainShouldNotDependOnSpring() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.jobpilot.domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta..");

        var classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.jobpilot.domain");
        rule.check(classes);
    }

    @Test
    void domainShouldOnlyDependOnJavaAndCommon() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.jobpilot.domain..")
            .should().dependOnClassesThat()
            .resideOutsideOfPackages(
                "com.jobpilot.domain..",
                "com.jobpilot.common..",
                "java.."
            );

        var classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.jobpilot.domain");
        rule.check(classes);
    }
}
