package com.example.codeinspection.demo;

import com.example.codeinspection.model.ConfigurationFile;
import com.example.codeinspection.model.Dependency;
import com.example.codeinspection.model.DependencyFile;
import com.example.codeinspection.model.Directory;
import com.example.codeinspection.model.SourceCodeFile;
import com.example.codeinspection.model.TestFile;
import java.util.List;

/** In-memory fixture: all metrics and security indicators are synthetic demonstration metadata. */
public final class FintechProject {
    private FintechProject() { }

    public static Directory create() {
        return new Directory("fintech-project")
                .add(new Directory("src")
                        .add(new SourceCodeFile("PaymentService.java", 180, 12, true, true, true, 3))
                        .add(new SourceCodeFile("UserService.java", 120, 6, false, false, true, 1)))
                .add(new Directory("tests")
                        .add(new TestFile("PaymentServiceTest.java", 90, "PaymentService.java")))
                .add(new Directory("config")
                        .add(new ConfigurationFile("application.properties", true, true, true, false)))
                .add(new DependencyFile("pom.xml", List.of(
                        new Dependency("org.example:payment-sdk", "2.4.0", false),
                        new Dependency("org.example:legacy-json", "1.0.0", true),
                        new Dependency("org.example:unsafe-crypto", " ", false))));
    }
}
