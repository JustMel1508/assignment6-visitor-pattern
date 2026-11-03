# UML diagram source

The submission images are `uml-class-diagram.png` and `uml-sequence-diagram.png`,
embedded directly in README. The Mermaid definitions below retain editable views
of the same implementation. File elements are Java
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
    DependencyFile "0..*" o-- "0..*" Dependency : entries
    ProjectElement ..> ProjectVisitor : accept and double dispatch
    QualityGateVisitor *-- SecurityAuditVisitor : delegates visits
    QualityGateVisitor *-- ProjectStatisticsVisitor : delegates visits
```

## Sequence diagram

The security traversal below uses the supported manual API on the fintech fixture.
The client creates a fresh visitor, invokes `root.accept(visitor)`, and requests
`report()` after traversal. Subsequent children are summarized to keep the diagram readable. Directory
owns traversal; `visit(Directory)` never traverses its children. Other analyses follow
the same dispatch protocol on the same tree. Compliance defers its cross-file test
matching until `report()` after collection. Root elements have no parent; each element
instance occurs at most once within a project tree.

```mermaid
sequenceDiagram
    actor Client
    participant Root as fintech-project (Directory)
    participant Src as src (Directory)
    participant Pay as PaymentService (SourceCodeFile)
    participant Audit as SecurityAuditVisitor
    Note over Client,Root: Root is the tree built by FintechProject.create()
    Client->>Audit: new SecurityAuditVisitor()
    Client->>Root: accept(visitor)
    activate Root
    Root->>Audit: visit(this: Directory)
    Note over Root,Audit: Directory visit does not recurse
    Root->>Src: accept(visitor)
    activate Src
    Src->>Audit: visit(Src)
    Src->>Pay: accept(visitor)
    activate Pay
    Pay->>Audit: visit(this: SourceCodeFile)
    activate Audit
    Audit->>Audit: collect credentials and high-risk findings
    Audit-->>Pay: return
    deactivate Audit
    Pay-->>Src: accept complete
    deactivate Pay
    Note over Src,Audit: UserService.java follows the same accept/visit protocol
    Src-->>Root: subtree complete
    deactivate Src
    Note over Root,Audit: Tests, configuration and dependencies use the same accept/visit protocol
    Root-->>Client: traversal complete
    deactivate Root
    Client->>Audit: report()
    activate Audit
    Audit->>Audit: create immutable SecurityReport snapshot
    Audit-->>Client: SecurityReport with five findings, one critical, score zero
    deactivate Audit
```
