package com.example.codeinspection.model;

/** Vulnerability metadata is supplied by the caller, never fetched from a live database. */
public record Dependency(String identifier, String version, boolean knownVulnerability) {
    public Dependency {
        identifier = Validation.name(identifier, "Dependency identifier");
        version = version == null ? null : version.strip();
    }

    public boolean hasVersion() {
        return version != null && !version.isBlank();
    }
}
