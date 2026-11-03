package com.example.codeinspection.model;

import com.example.codeinspection.visitor.ProjectVisitor;

public interface ProjectElement {
    void accept(ProjectVisitor visitor);
}
