# UML diagrams

These diagrams show the implemented production structure. File elements are Java
records; Directory is a mutable composite. Each analysis visitor implements all five
overloads declared by ProjectVisitor. Only the core domain and inspection classes
are shown; immutable result records and Directory's private validation visitor are
omitted for readability.

## Class diagram

```mermaid
classDiagram
    direction TB
    class ProjectElement {
        <<interface>>
        +accept(ProjectVisitor visitor) void
    }
    class ProjectVisitor {
        <<interface>>
        +visit(SourceCodeFile file) void
        +visit(TestFile file) void
        +visit(ConfigurationFile file) void
        +visit(DependencyFile file) void
        +visit(Directory directory) void
    }
    class SourceCodeFile {
        <<record>>
        -String name
        -int linesOfCode
        -int complexity
        -boolean hardcodedCredentials
        -boolean highRisk
        -boolean production
        -int todoCount
        +accept(ProjectVisitor visitor) void
    }
    class TestFile {
        <<record>>
        -String name
        -int linesOfCode
        -String testedSource
        +accept(ProjectVisitor visitor) void
    }
    class ConfigurationFile {
        <<record>>
        -String name
        -boolean production
        -boolean debugEnabled
        -boolean insecureSettings
        -boolean exposedCredentials
        +accept(ProjectVisitor visitor) void
    }
    class Dependency {
        <<record>>
        -String identifier
        -String version
        -boolean knownVulnerability
        +hasVersion() boolean
    }
    class DependencyFile {
        <<record>>
        -String name
        -List~Dependency~ dependencies
        +accept(ProjectVisitor visitor) void
    }
    class Directory {
        -String name
        -List~ProjectElement~ children
        -Directory parent
        +name() String
        +add(ProjectElement child) Directory
        +children() List~ProjectElement~
        +accept(ProjectVisitor visitor) void
    }
    class SecurityAuditVisitor {
        +analyze(ProjectElement root) SecurityReport
        +reset() void
        +report() SecurityReport
    }
    class ProjectStatisticsVisitor {
        +analyze(ProjectElement root) StatisticsReport
        +reset() void
        +report() StatisticsReport
    }
    class QualityGateVisitor {
        -SecurityAuditVisitor security
        -ProjectStatisticsVisitor statistics
        +analyze(ProjectElement root) QualityGateReport
        +reset() void
        +report() QualityGateReport
    }
    class ComplianceVisitor {
        +Set~String~ PROHIBITED_DEPENDENCIES
        +analyze(ProjectElement root) ComplianceReport
        +reset() void
        +report() ComplianceReport
    }
    class TechnicalDebtVisitor {
        +analyze(ProjectElement root) TechnicalDebtReport
        +reset() void
        +report() TechnicalDebtReport
    }
    ProjectElement <|.. SourceCodeFile
    ProjectElement <|.. TestFile
    ProjectElement <|.. ConfigurationFile
    ProjectElement <|.. DependencyFile
    ProjectElement <|.. Directory
    ProjectVisitor <|.. SecurityAuditVisitor
    ProjectVisitor <|.. ProjectStatisticsVisitor
    ProjectVisitor <|.. QualityGateVisitor
    ProjectVisitor <|.. ComplianceVisitor
    ProjectVisitor <|.. TechnicalDebtVisitor
    Directory "1" *-- "0..*" ProjectElement : children
    DependencyFile "1" o-- "0..*" Dependency : entries
    ProjectElement ..> ProjectVisitor : accept and double dispatch
    QualityGateVisitor *-- SecurityAuditVisitor : delegates visits
    QualityGateVisitor *-- ProjectStatisticsVisitor : delegates visits
```

## Sequence diagram

The security traversal below uses the actual nested fintech fixture. Directory owns
traversal; `visit(Directory)` never traverses its children. Other analyses follow
the same dispatch protocol on the same tree. Compliance defers its cross-file test
matching until `report()` after collection.

```mermaid
sequenceDiagram
    participant Main
    participant Factory as FintechProject
    participant Root as fintech-project (Directory)
    participant Src as src (Directory)
    participant Pay as PaymentService (SourceCodeFile)
    participant User as UserService (SourceCodeFile)
    participant Tests as tests (Directory)
    participant Test as PaymentServiceTest (TestFile)
    participant ConfigDir as config (Directory)
    participant Config as application.properties (ConfigurationFile)
    participant Pom as pom.xml (DependencyFile)
    participant Audit as SecurityAuditVisitor
    participant Report as SecurityReport
    Main->>Factory: create()
    Factory-->>Main: Root with complete nested tree
    Main->>Audit: new SecurityAuditVisitor()
    Main->>Audit: analyze(Root)
    activate Audit
    Audit->>Audit: reset()
    Audit->>Root: accept(this)
    Root->>Audit: visit(Root)
    Note over Root,Audit: Directory visit does not recurse
    Root->>Src: accept(visitor)
    Src->>Audit: visit(Src)
    Src->>Pay: accept(visitor)
    Pay->>Audit: visit(Pay: SourceCodeFile)
    Audit->>Audit: collect credentials and high-risk findings
    Src->>User: accept(visitor)
    User->>Audit: visit(User: SourceCodeFile)
    Root->>Tests: accept(visitor)
    Tests->>Audit: visit(Tests)
    Tests->>Test: accept(visitor)
    Test->>Audit: visit(Test: TestFile)
    Root->>ConfigDir: accept(visitor)
    ConfigDir->>Audit: visit(ConfigDir)
    ConfigDir->>Config: accept(visitor)
    Config->>Audit: visit(Config: ConfigurationFile)
    Audit->>Audit: collect production debug and insecure-setting findings
    Root->>Pom: accept(visitor)
    Pom->>Audit: visit(Pom: DependencyFile)
    Audit->>Audit: collect vulnerable dependency finding
    Root-->>Audit: traversal complete
    Audit->>Audit: report()
    Audit->>Report: new SecurityReport(findings snapshot)
    Audit-->>Main: immutable SecurityReport
    deactivate Audit
    Main->>Report: toString() via println
    Report-->>Main: five findings, one critical, score zero
```
