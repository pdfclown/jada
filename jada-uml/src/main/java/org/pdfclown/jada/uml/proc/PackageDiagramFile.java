/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PackageDiagramFile.java) is part of jada-uml module in Jada project
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

import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__HTML;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__SVG;
import static org.pdfclown.common.util.io.Files.baseName;
import static org.pdfclown.common.util.io.Files.extension;
import static org.pdfclown.common.util.io.Files.isExtension;
import static org.pdfclown.common.util.io.Files.relativize;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__PACKAGE_SUMMARY;
import static org.pdfclown.jada.uml.internal.Internals.FILENAME__PACKAGE;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Pattern;
import javax.tools.Diagnostic.Kind;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jspecify.annotations.Nullable;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.uml.UmlConfig.ImageConfig;

// SourceName: nl.talsmasoftware.umldoclet.html.PackageDiagramInserter
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
final class PackageDiagramFile extends DiagramFile {
  static class Inserter extends PageProcessor.Inserter {
    protected static String packageStyle(String imagePath) {
      return "display:block;margin-left:auto;margin-right:auto;%s:100%%;".formatted(
          /*
           * NOTE: In case of SVG, its viewport is expanded to the full client width to provide room
           * for zooming and panning.
           */
          isExtension(imagePath, FILE_EXTENSION__SVG) ? "width" : "max-width");
    }

    Inserter(String diagramRelativePath) {
      super(diagramRelativePath);
    }

    @Override
    protected String getDiagramTypeDescription() {
      return "Package";
    }

    @Override
    protected @Nullable Document process(Document doc, Path docFile, JadaConfig config) {
      Element e;
      if ((e = doc.selectFirst("table")) != null) {
        e.before(getImageTag());
      } else if ((e = doc.selectFirst("section[class=summary]")) != null) {
        e.prepend(getImageTag());
      } else {
        logMissingNode(Kind.WARNING, docFile, "`table`, or `section[class=summary]`", "NOT PLACED",
            config);
        doc = null;
      }
      return doc;
    }

    private String getImageTag() {
      return getImageTag("Package Summary", "uml-package", packageStyle(diagramRelativePath));
    }
  }

  private static final String REGEX__FILE_MATCH_REPLACE = Pattern.quote(FILENAME__PACKAGE_SUMMARY
      + FILE_EXTENSION__HTML) + "$";

  private final String extension;
  private final String pathToCompare;

  PackageDiagramFile(Path baseDir, Path diagramFile, ImageConfig.Format format,
      boolean hasImagesDir) {
    super(baseDir, diagramFile, format);

    this.extension = extension(diagramFile);
    this.pathToCompare = hasImagesDir
        ? baseName(diagramFile).replace(DOT, File.separatorChar) + this.extension
        : relativize(this.baseDir, this.diagramFile).toString();
  }

  @Override
  public PageProcessor.Inserter createInserter(String diagramRelativePath) {
    return new Inserter(diagramRelativePath);
  }

  @Override
  boolean matches(Path htmlFile) {
    return pathToCompare.equals(relativize(baseDir, htmlFile).toString()
        .replaceFirst(REGEX__FILE_MATCH_REPLACE, FILENAME__PACKAGE + extension));
  }
}
