package com.example.codeinspection.model;

import com.example.codeinspection.visitor.ProjectVisitor;
import java.util.Objects;

/** testedSource is the exact, case-sensitive source name used by compliance. */
public record TestFile(String name, int linesOfCode, String testedSource) implements ProjectElement {
    public TestFile {
        name = Validation.name(name, "Test name");
        Validation.nonnegative(linesOfCode, "Lines of code");
        testedSource = Validation.name(testedSource, "Tested source name");
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        Objects.requireNonNull(visitor, "visitor").visit(this);
    }
}
