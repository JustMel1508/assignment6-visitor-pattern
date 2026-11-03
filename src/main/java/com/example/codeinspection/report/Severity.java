package com.example.codeinspection.report;

public enum Severity {
    CRITICAL(40), HIGH(20), MEDIUM(10), LOW(5);

    private final int scorePenalty;

    Severity(int scorePenalty) {
        this.scorePenalty = scorePenalty;
    }

    public int scorePenalty() {
        return scorePenalty;
    }

    /** Only CRITICAL blocks the security gate; HIGH is reported but does not block it. */
    public boolean blocksRelease() {
        return this == CRITICAL;
    }
}
