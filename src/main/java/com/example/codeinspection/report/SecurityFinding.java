package com.example.codeinspection.report;

public record SecurityFinding(String element, String rule, Severity severity, String explanation) {
    @Override
    public String toString() {
        return "[" + severity + "] " + element + " | " + rule + ": " + explanation;
    }
}
