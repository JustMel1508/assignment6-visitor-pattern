package com.example.codeinspection.visitor;

import com.example.codeinspection.model.ConfigurationFile;
import com.example.codeinspection.model.DependencyFile;
import com.example.codeinspection.model.Directory;
import com.example.codeinspection.model.SourceCodeFile;
import com.example.codeinspection.model.TestFile;

public interface ProjectVisitor {
    void visit(SourceCodeFile file);
    void visit(TestFile file);
    void visit(ConfigurationFile file);
    void visit(DependencyFile file);
    void visit(Directory directory);
}
