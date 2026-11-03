package com.example.codeinspection.model;

import com.example.codeinspection.visitor.ProjectVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Composite with preorder traversal. Visitors must not traverse its children themselves. */
public final class Directory implements ProjectElement {
    private final String name;
    private final List<ProjectElement> children = new ArrayList<>();
    private Directory parent;

    public Directory(String name) {
        this.name = Validation.name(name, "Directory name");
    }

    public String name() {
        return name;
    }

    public List<ProjectElement> children() {
        return List.copyOf(children);
    }

    public Directory add(ProjectElement child) {
        Objects.requireNonNull(child, "child");
        if (children.contains(child)) {
            throw new IllegalArgumentException("Child is already in " + name);
        }
        // Use dispatch to inspect the candidate subtree without conditional type inspection.
        Directory[] candidateRoot = new Directory[1];
        child.accept(new ProjectVisitor() {
            @Override public void visit(SourceCodeFile file) { }
            @Override public void visit(TestFile file) { }
            @Override public void visit(ConfigurationFile file) { }
            @Override public void visit(DependencyFile file) { }
            @Override public void visit(Directory directory) {
                if (directory == Directory.this) {
                    throw new IllegalArgumentException("Directory cycles are not allowed");
                }
                if (candidateRoot[0] == null) {
                    candidateRoot[0] = directory;
                }
            }
        });
        if (candidateRoot[0] != null) {
            if (candidateRoot[0].parent != null) {
                throw new IllegalArgumentException("Directory already has a parent");
            }
            candidateRoot[0].parent = this;
        }
        children.add(child);
        return this;
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        Objects.requireNonNull(visitor, "visitor").visit(this);
        for (ProjectElement child : children()) {
            child.accept(visitor);
        }
    }
}
