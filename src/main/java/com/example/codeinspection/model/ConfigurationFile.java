package com.example.codeinspection.model;

import com.example.codeinspection.visitor.ProjectVisitor;
import java.util.Objects;

public record ConfigurationFile(String name, boolean production, boolean debugEnabled,
                                boolean insecureSettings, boolean exposedCredentials)
        implements ProjectElement {
    public ConfigurationFile {
        name = Validation.name(name, "Configuration name");
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        Objects.requireNonNull(visitor, "visitor").visit(this);
    }
}
