package com.example.codeinspection;

import com.example.codeinspection.model.ConfigurationFile;
import com.example.codeinspection.model.Dependency;
import com.example.codeinspection.model.DependencyFile;
import com.example.codeinspection.model.Directory;
import com.example.codeinspection.model.ProjectElement;
import com.example.codeinspection.model.SourceCodeFile;
import com.example.codeinspection.model.TestFile;
import com.example.codeinspection.visitor.ProjectVisitor;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModelTraversalTest {
    private static SourceCodeFile source() {
        return new SourceCodeFile("Pay.java", 20, 3, false, false, true, 0);
    }

    @Test
    void runtimeElementsDispatchToEveryConcreteOverloadInPreorder() {
        ProjectElement root = new Directory("root").add(source())
                .add(new TestFile("PayTest.java", 10, "Pay.java"))
                .add(new ConfigurationFile("app.properties", true, false, false, false))
                .add(new DependencyFile("pom.xml", List.of()));
        RecordingVisitor visitor = new RecordingVisitor();
        root.accept(visitor);
        assertEquals(List.of("directory:root", "source:Pay.java", "test:PayTest.java",
                "config:app.properties", "dependencies:pom.xml"), visitor.calls);
    }

    @Test
    void nestedAndEmptyDirectoriesAreVisitedExactlyOnce() {
        Directory root = new Directory("root").add(new Directory("nested")
                .add(new Directory("empty")).add(source()));
        RecordingVisitor visitor = new RecordingVisitor();
        root.accept(visitor);
        assertEquals(List.of("directory:root", "directory:nested", "directory:empty",
                "source:Pay.java"), visitor.calls);
    }

    @Test
    void cyclesDuplicatesAndSharedSubdirectoriesAreRejectedBeforeMutation() {
        Directory root = new Directory("root");
        Directory child = new Directory("child");
        root.add(child);
        assertThrows(IllegalArgumentException.class, () -> root.add(root));
        assertThrows(IllegalArgumentException.class, () -> child.add(root));
        assertThrows(IllegalArgumentException.class, () -> root.add(child));
        assertThrows(IllegalArgumentException.class, () -> new Directory("other").add(child));
        assertEquals(List.of(child), root.children());
        assertTrue(child.children().isEmpty());
    }

    @Test
    void childrenAndDependencyEntriesAreDefensiveSnapshots() {
        Directory root = new Directory("root");
        List<ProjectElement> snapshot = root.children();
        root.add(source());
        assertTrue(snapshot.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> root.children().clear());
        List<Dependency> entries = new ArrayList<>();
        entries.add(new Dependency("org.example:sdk", "1", false));
        DependencyFile manifest = new DependencyFile("pom.xml", entries);
        entries.clear();
        assertEquals(1, manifest.dependencies().size());
        assertThrows(UnsupportedOperationException.class, () -> manifest.dependencies().clear());
    }

    @Test
    void invalidNamesNullsAndNegativeMetricsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Directory(" "));
        assertThrows(IllegalArgumentException.class,
                () -> new SourceCodeFile("x", -1, 0, false, false, true, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new SourceCodeFile("x", 0, -1, false, false, true, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new SourceCodeFile("x", 0, 0, false, false, true, -1));
        assertThrows(IllegalArgumentException.class, () -> new TestFile("t", -1, "x"));
        assertThrows(IllegalArgumentException.class, () -> new TestFile("t", 0, " "));
        assertThrows(IllegalArgumentException.class, () -> new ConfigurationFile("", false, false, false, false));
        assertThrows(IllegalArgumentException.class, () -> new Dependency(" ", "1", false));
        assertThrows(NullPointerException.class, () -> new Directory("d").add(null));
        assertThrows(NullPointerException.class, () -> source().accept(null));
        assertThrows(NullPointerException.class, () -> new DependencyFile("p", null));
    }

    @Test
    void practicalDeepHierarchyTraversesWithoutDuplicateVisits() {
        Directory root = new Directory("root");
        Directory current = root;
        for (int i = 0; i < 200; i++) {
            Directory next = new Directory("d" + i);
            current.add(next);
            current = next;
        }
        current.add(source());
        RecordingVisitor visitor = new RecordingVisitor();
        root.accept(visitor);
        assertEquals(202, visitor.calls.size());
        assertEquals("source:Pay.java", visitor.calls.get(201));
    }

    private static final class RecordingVisitor implements ProjectVisitor {
        private final List<String> calls = new ArrayList<>();
        @Override public void visit(SourceCodeFile file) { calls.add("source:" + file.name()); }
        @Override public void visit(TestFile file) { calls.add("test:" + file.name()); }
        @Override public void visit(ConfigurationFile file) { calls.add("config:" + file.name()); }
        @Override public void visit(DependencyFile file) { calls.add("dependencies:" + file.name()); }
        @Override public void visit(Directory directory) { calls.add("directory:" + directory.name()); }
    }
}
