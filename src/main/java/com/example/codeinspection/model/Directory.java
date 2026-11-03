package com.example.codeinspection.model;

import com.example.codeinspection.visitor.ProjectVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
        Directory treeRoot = this;
        while (treeRoot.parent != null) {
            treeRoot = treeRoot.parent;
        }
        // Check the whole containing tree: a leaf shared with a sibling also causes double counting.
        StructureVisitor existing = new StructureVisitor();
        treeRoot.accept(existing);
        StructureVisitor candidate = new StructureVisitor();
        child.accept(candidate);
        for (ProjectElement element : candidate.elements) {
            if (existing.elements.contains(element)) {
                throw new IllegalArgumentException("Cycles and shared element instances are not allowed in one tree");
            }
        }
        if (candidate.rootDirectory != null) {
            if (candidate.rootDirectory.parent != null) {
                throw new IllegalArgumentException("Directory already has a parent");
            }
            candidate.rootDirectory.parent = this;
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

    /** Identity matters: equal immutable records may represent distinct files in different directories. */
    private static final class StructureVisitor implements ProjectVisitor {
        private final Set<ProjectElement> elements = Collections.newSetFromMap(new IdentityHashMap<>());
        private Directory rootDirectory;

        private void record(ProjectElement element) {
            if (!elements.add(element)) {
                throw new IllegalArgumentException("Element occurs more than once in the subtree");
            }
        }

        @Override public void visit(SourceCodeFile file) { record(file); }
        @Override public void visit(TestFile file) { record(file); }
        @Override public void visit(ConfigurationFile file) { record(file); }
        @Override public void visit(DependencyFile file) { record(file); }
        @Override public void visit(Directory directory) {
            record(directory);
            if (rootDirectory == null) {
                rootDirectory = directory;
            }
        }
    }
}
