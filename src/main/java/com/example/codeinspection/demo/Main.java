package com.example.codeinspection.demo;

import com.example.codeinspection.model.Directory;
import com.example.codeinspection.visitor.ComplianceVisitor;
import com.example.codeinspection.visitor.ProjectStatisticsVisitor;
import com.example.codeinspection.visitor.QualityGateVisitor;
import com.example.codeinspection.visitor.SecurityAuditVisitor;
import com.example.codeinspection.visitor.TechnicalDebtVisitor;

/** Console entry point for the inspection engine. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        System.out.println("Smart Code Inspection & Compliance Engine");
        System.out.println("Synthetic fintech metadata; no filesystem parsing or live vulnerability scan.\n");
        Directory project = FintechProject.create();
        System.out.println(new SecurityAuditVisitor().analyze(project));
        System.out.println();
        System.out.println(new ProjectStatisticsVisitor().analyze(project));
        System.out.println();
        System.out.println(new QualityGateVisitor().analyze(project));
        System.out.println();
        System.out.println(new ComplianceVisitor().analyze(project));
        System.out.println();
        System.out.println(new TechnicalDebtVisitor().analyze(project));
    }
}
