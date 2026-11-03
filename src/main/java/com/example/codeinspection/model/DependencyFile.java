package com.example.codeinspection.model;

import com.example.codeinspection.visitor.ProjectVisitor;
import java.util.List;
import java.util.Objects;

public record DependencyFile(String name, List<Dependency> dependencies) implements ProjectElement {
    public DependencyFile {
        name = Validation.name(name, "Dependency file name");
        dependencies = List.copyOf(dependencies);
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        Objects.requireNonNull(visitor, "visitor").visit(this);
    }
}
