/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PackageDependenciesFile.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.proc;

import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__HTML;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__SVG;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__MODULE_SUMMARY;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__OVERVIEW_SUMMARY;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__PACKAGE_DEPS;
import static org.pdfclown.jada.uml.proc.PackageDiagramFile.Inserter.packageStyle;

import java.nio.file.Path;
import javax.tools.Diagnostic.Kind;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jspecify.annotations.Nullable;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.uml.UmlConfig.ImageConfig;

// SourceName: nl.talsmasoftware.umldoclet.html.PackageDependenciesInserter
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
final class PackageDependenciesFile extends DiagramFile {
  static class Inserter extends PageProcessor.Inserter {
    Inserter(String diagramRelativePath) {
      super(diagramRelativePath);
    }

    @Override
    protected String getDiagramTypeDescription() {
      return "Package dependencies";
    }

    @Override
    protected @Nullable Document process(Document doc, Path docFile, JadaConfig config) {
      Element e;
      if ((e = doc.selectFirst("table")) != null) {
        e.before(getImageTag());
      } else if ((e = doc.selectFirst("div[id=all-packages-table], "
          + "div[class=module-signature]")) != null) {
        e.prepend(getImageTag());
      } else {
        /*
         * NOTE: If the file redirects to main page (index.html), then node absence is expected.
         */
        if (doc.selectFirst("link[rel=canonical][href=index.html]") == null) {
          logMissingNode(Kind.WARNING, docFile, "`table`, or `div[id=all-packages-table]`, "
              + "or `div[class=module-signature]`", "NOT PLACED", config);
        }
        doc = null;
      }
      return doc;
    }

    private String getImageTag() {
      return getImageTag("Package Dependencies", "uml-package-dependencies",
          packageStyle(diagramRelativePath));
    }
  }

  private final Path index;
  private final @Nullable Path moduleSummary;
  private final Path overviewSummary;

  PackageDependenciesFile(Path baseDir, Path diagramFile, ImageConfig.Format format) {
    super(baseDir, diagramFile, format);

    this.index = baseDir.resolve("index.html");
    this.overviewSummary = baseDir.resolve(FILENAME__OVERVIEW_SUMMARY + FILE_EXTENSION__HTML);
    this.moduleSummary = diagramFile.getFileName().toString()
        .equals(FILENAME__PACKAGE_DEPS + FILE_EXTENSION__SVG)
            ? diagramFile.getParent().resolve(FILENAME__MODULE_SUMMARY + FILE_EXTENSION__HTML)
            : null;
  }

  @Override
  public PageProcessor.Inserter createInserter(String diagramRelativePath) {
    return new Inserter(diagramRelativePath);
  }

  @Override
  boolean matches(Path htmlFile) {
    return htmlFile.equals(index)
        || htmlFile.equals(overviewSummary)
        || htmlFile.equals(moduleSummary);
  }
}
