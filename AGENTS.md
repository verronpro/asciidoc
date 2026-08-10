# AsciiDoc — AGENTS.md

Agent-agnostic guidance for working in the **asciidoc** project. This file is the standalone
guidance for this project; it does not assume any relationship to other projects.

## Project layout

Five Maven modules, layered:

`asciidoc-core` (data model) → `asciidoc-converters` (HTML/SVG/Text rendering) →
`asciidoc-docx` (DOCX output) → `asciidoc-compiler` (high-level API + PNG export) →
`asciidoc-preview` (AsciidoctorJ extension).

## General development standards

This project uses **Java 25** and **Maven**.

### Build
- `mvn clean install` — build the whole project.
- `mvn clean install -DskipTests` — build without running tests.
- `mvn clean install -pl <module>` — build a single Maven module within this project.
- `mvn clean install -pl <module> -am` — build a module and its dependencies.
- `mvn site` — generate the Maven documentation site from `src/site/asciidoc/`.

### Testing
- JUnit 5 (Jupiter); use `@DisplayName` for descriptive names; follow Arrange-Act-Assert.
- Tests live in `src/test/java/`.
- Coverage via JaCoCo, mutation testing via Pitest, architecture constraints via ArchUnit.
- `java.awt.headless=true` is set automatically by the Surefire plugin.

### Code style
- Java 25 features in use: records, JPMS modules (`module-info.java`), modern APIs.
- Soft line-length limit: 120 characters. Indentation: 4 spaces.
- Opening braces on the same line; `else` on a new line.
- Naming: PascalCase for classes/interfaces, camelCase for methods/variables,
  UPPER_SNAKE_CASE for constants.
- Prefer composition to inheritance; static factory methods or builders for complex objects;
  constructor injection for dependencies.
- Javadoc required for all public elements; use Markdown syntax inside Javadoc.
- Custom exceptions; do not swallow exceptions; use try-with-resources.

## Notes

- AsciiDoc supports AsciiDoc→DOCX embedding; keep the public API stable for DOCX-embedding
  consumers when changing the converters/docx modules.
- The preview output schema is governed by an ADR under `src/site/.../adr/` (e.g.
  `001-asciidoc-preview-schema.adoc`). Consult it before changing preview output.
- `font-requirements.adoc` and `snapshot-testing.adoc` are published docs under
  `src/site/asciidoc/` (reachable from the site nav as "Font Requirements" / "Snapshot Testing");
  link to them from module docs rather than duplicating their content.
