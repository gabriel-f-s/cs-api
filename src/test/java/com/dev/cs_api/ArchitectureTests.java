package com.dev.cs_api;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ArchitectureTests {

    @Test
    void verifyModularArchitecture() {
        ApplicationModules modules = ApplicationModules.of(Startup.class);
        modules.verify();
    }

    @Test
    void writeDocumentationSnippets() {
        ApplicationModules modules = ApplicationModules.of(Startup.class);
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}
