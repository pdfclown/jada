## v0.4.1 (2026-05-14)

### Fix

- **core**: fix copyright comments preservation in `FileOptimizer`

## v0.4.0 (2026-05-12)

### BREAKING CHANGE

- `--jada-file-optimize-exclude` CLI argument replaced
with `--jada-file-optimize-filter`

### Feat

- **core**: replace `--jada-file-optimize-exclude` CLI argument with `--jada-file-optimize-filter`

### Fix

- **deps**: bump org.pdfclown:pdfclown-common-base from 0.6.0 to 0.7.0
- update code to org.pdfclown:pdfclown-common-base dependency change (commit 5f02c68)
- update code to org.pdfclown:pdfclown-common-base dependency change (commit bcd0be3)
- update code to org.pdfclown:pdfclown-common-base dependency change (commit 5e2be86)
- **uml**: reconcile codebase with upstream project (commit ca9cac7)
- update code to org.pdfclown:pdfclown-common-base dependency change (commit 22d00c9)
- update code to org.pdfclown:pdfclown-common-base dependency change (commit a186f7e)
- **uml**: reconcile codebase with upstream project (commit 9bd5908)
- **uml**: reconcile codebase with upstream project (commit 43cd08a)
- **deps**: bump net.sourceforge.plantuml:plantuml-asl from 1.2025.10 to 1.2026.2 (#12)
- **deps**: bump org.pdfclown:pdfclown-common-base from 0.5.0 to 0.6.0

### Refactor

- remove redundant `NonNull` annotations
- remove `@SuppressWarnings` for potential nulls
- specify `ExpectedGeneration` type parameter
- **core**: remove temporary pdfclown-common code

## v0.3.0 (2026-02-26)

### BREAKING CHANGE

- `JadaScriptExtension` removed (see `JadaScriptContext`
instead).

### Feat

- **core**: replace script extensions with script hooks

### Fix

- harmonize lists in messages
- **ext**: fix infinite loop exception (`DocReuseTagletProcessor`)
- **core**: support generic argument placeholders (`MessageManager`)

## v0.2.2 (2026-01-17)

### Fix

- **deps**: bump org.pdfclown:pdfclown-common-base from 0.4.0 to 0.5.0 (#10)
- **build**: fix Javadoc classpath (`JadaDebug`)
- **core**: fix `JadaScriptExtension` classpath
- **maven**: improve `ProcessSourceMojo`
- **uml**: reconcile codebase with upstream project (commit 355c127)

## v0.2.1 (2026-01-11)

### Fix

- **deps**: bump org.pdfclown:pdfclown-common-base from 0.3.0 to 0.4.0 (#8)
- **deps**: bump org.pdfclown:pdfclown-common-base from 0.2.2 to 0.3.0 (#7)

### Refactor

- normalize derived fields
- **core**: simplify script loading code

## v0.2.0 (2025-12-30)

### Feat

- **core**: support incremental `--jada-exts` option

### Fix

- **deps**: bump org.pdfclown:pdfclown-common-base from 0.2.1 to 0.2.2 (#6)
- **uml**: fix touch input for UML diagrams
- **deps**: bump net.sourceforge.plantuml:plantuml-asl from 1.2024.7 to 1.2025.10 (#4)
- **deps**: bump org.apache.commons:commons-compress from 1.27.1 to 1.28.0 (#3)

## v0.1.3 (2025-12-22)

### Fix

- **deps**: bump org.pdfclown:pdfclown-common-* from 0.2.0 to 0.2.1

## v0.1.2 (2025-12-22)

### Fix

- **core**: fix `@docRoot` in files at Javadoc root directory/2
- **core**: fix `@docRoot` in files at Javadoc root directory

## v0.1.1 (2025-12-22)

### Fix

- **deps**: bump org.pdfclown:pdfclown-common-* to 0.2.0

## v0.1.0 (2025-12-22)
