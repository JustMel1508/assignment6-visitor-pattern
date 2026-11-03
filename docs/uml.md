# UML diagrams

These diagrams show the implemented production structure. File elements are Java
records; Directory is a mutable composite. Each analysis visitor implements all five
overloads declared by ProjectVisitor. Key properties and methods are shown; immutable
result records and Directory's private validation visitor are omitted for readability.
Every analysis visitor also exposes the `reset()` lifecycle method described in README.

## Class diagram

```mermaid
classDiagram
    direction LR
    class ProjectElement {
        <<interface>>
        +accept(ProjectVisitor) void
    }
    class ProjectVisitor {
        <<interface>>
        +visit(SourceCodeFile) void
        +visit(TestFile) void
        +visit(ConfigurationFile) void
        +visit(DependencyFile) void
        +visit(Directory) void
    }
    class SourceCodeFile {
        <<record>>
        -String name
        -int linesOfCode
        -int complexity
        -int todoCount
        +accept(ProjectVisitor) void
    }
    class TestFile {
        <<record>>
        -String name
        -int linesOfCode
        -String testedSource
        +accept(ProjectVisitor) void
    }
    class ConfigurationFile {
        <<record>>
        -String name
        -boolean production
        -boolean debugEnabled
        -boolean exposedCredentials
        +accept(ProjectVisitor) void
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
        +accept(ProjectVisitor) void
    }
    class Directory {
        -String name
        -List~ProjectElement~ children
        -Directory parent
        +add(ProjectElement) Directory
        +children()
        +accept(ProjectVisitor) void
    }
    class SecurityAuditVisitor {
        +analyze(ProjectElement)
        +report()
    }
    class ProjectStatisticsVisitor {
        +analyze(ProjectElement)
        +report()
    }
    class QualityGateVisitor {
        +analyze(ProjectElement)
        +report()
    }
    class ComplianceVisitor {
        +analyze(ProjectElement)
        +report()
    }
    class TechnicalDebtVisitor {
        +analyze(ProjectElement)
        +report()
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
    Directory "0..1" *-- "0..*" ProjectElement : children within one tree
    DependencyFile "1" o-- "0..*" Dependency : entries
    ProjectElement ..> ProjectVisitor : accept and double dispatch
    QualityGateVisitor *-- SecurityAuditVisitor : delegates visits
    QualityGateVisitor *-- ProjectStatisticsVisitor : delegates visits
```

## Sequence diagram

The security traversal below shows the first source in the actual nested fintech
fixture; subsequent children are summarized to keep the diagram readable. Directory
owns traversal; `visit(Directory)` never traverses its children. Other analyses follow
the same dispatch protocol on the same tree. Compliance defers its cross-file test
matching until `report()` after collection. Root elements have no parent; each element
instance occurs at most once within a project tree.

```mermaid
sequenceDiagram
    participant Main
    participant Root as fintech-project (Directory)
    participant Src as src (Directory)
    participant Pay as PaymentService (SourceCodeFile)
    participant Audit as SecurityAuditVisitor
    participant Report as SecurityReport
    Note over Main,Root: Main obtains the tree from FintechProject.create()
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
    Note over Root,Audit: Remaining children use the same accept/visit protocol
    Root-->>Audit: traversal complete
    Audit->>Audit: report()
    Audit->>Report: new SecurityReport(findings snapshot)
    Audit-->>Main: immutable SecurityReport
    deactivate Audit
    Main->>Report: toString() via println
    Report-->>Main: five findings, one critical, score zero
```
