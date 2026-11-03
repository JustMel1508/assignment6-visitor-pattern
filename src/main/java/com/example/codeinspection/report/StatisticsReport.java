package com.example.codeinspection.report;

import java.util.Locale;

public record StatisticsReport(long fileCount, long linesOfCode, long testFileCount,
                               long dependencyCount, long sourceFileCount,
                               long productionSourceCount, long directoryCount, long complexitySum) {
    public double averageComplexity() {
        return sourceFileCount == 0 ? 0.0 : (double) complexitySum / sourceFileCount;
    }

    public double testSourceRatio() {
        return sourceFileCount == 0 ? 0.0 : (double) testFileCount / sourceFileCount;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "PROJECT STATISTICS REPORT%nTotal files: %d%nTotal LOC (source + tests): %d%n"
                        + "Test files: %d%nDependencies (entries): %d%nAverage complexity: %.2f%n"
                        + "Test/source ratio: %.2f%nSource files: %d | Production sources: %d | Directories: %d",
                fileCount, linesOfCode, testFileCount, dependencyCount, averageComplexity(),
                testSourceRatio(), sourceFileCount, productionSourceCount, directoryCount);
    }
}
