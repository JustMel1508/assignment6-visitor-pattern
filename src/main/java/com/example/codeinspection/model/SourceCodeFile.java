package com.example.codeinspection.model;

import com.example.codeinspection.visitor.ProjectVisitor;
import java.util.Objects;

public record SourceCodeFile(String name, int linesOfCode, int complexity,
                             boolean hardcodedCredentials, boolean highRisk,
                             boolean production, int todoCount) implements ProjectElement {
    public SourceCodeFile {
        name = Validation.name(name, "Source name");
        Validation.nonnegative(linesOfCode, "Lines of code");
        Validation.nonnegative(complexity, "Complexity");
        Validation.nonnegative(todoCount, "TODO/FIXME count");
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        Objects.requireNonNull(visitor, "visitor").visit(this);
    }
}
