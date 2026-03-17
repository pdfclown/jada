/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ClassDiagramFile.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.proc;

import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__HTML;
import static org.pdfclown.common.util.io.Files.baseName;
import static org.pdfclown.common.util.io.Files.extension;
import static org.pdfclown.common.util.io.Files.relativize;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Pattern;
import javax.tools.Diagnostic.Kind;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jspecify.annotations.Nullable;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.uml.UmlConfig.ImageConfig;

// SourceName: nl.talsmasoftware.umldoclet.html.ClassDiagramInserter
/**
 * Generated class diagram file.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
final class ClassDiagramFile extends DiagramFile {
  static class Inserter extends PageProcessor.Inserter {
    Inserter(String diagramRelativePath) {
      super(diagramRelativePath);
    }

    @Override
    protected String getDiagramTypeDescription() {
      return "Class";
    }

    @Override
    protected @Nullable Document process(Document doc, Path docFile, JadaConfig config) {
      Element e;
      if ((e = doc.selectFirst("hr")) == null) {
        logMissingNode(Kind.WARNING, docFile, "`hr`", "NOT PLACED", config);
        return null;
      }
      e.before(getImageTag());

      /*
       * NOTE: The original `pre` node selector has changed to support Javadoc 17+ which dropped it.
       */
      if ((e = e.nextElementSibling()) == null) {
        logMissingNode(Kind.WARNING, docFile, "next to `hr`", "white-space styling NOT APPLIED",
            config);
        return doc;
      }
      e.attr("style", "white-space:pre-wrap;");

      /*
       * NOTE: The original `div` node selector has changed to support Javadoc 17+ which replaced it
       * with `section` node.
       */
      if ((e = doc.selectFirst("*[class=summary]")) == null) {
        /*
         * NOTE: Lowest-level log because the node is typically missing in case of memberless class.
         */
        logMissingNode(Kind.OTHER, docFile, "`*[class=summary]`", "float clearing NOT APPLIED",
            config);
        return doc;
      }
      e.attr("style", "clear:right;");

      return doc;
    }

    private String getImageTag() {
      return getImageTag(baseName(diagramRelativePath, true), "uml-class",
          "max-width:60%;float:right;");
    }
  }

  private static final String REGEX__FILE_MATCH_REPLACE = Pattern.quote(FILE_EXTENSION__HTML) + "$";

  private final String extension;
  private final String pathToCompare;

  ClassDiagramFile(Path baseDir, Path diagramFile, ImageConfig.Format format,
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
  protected boolean matches(Path htmlFile) {
    return pathToCompare.equals(relativize(baseDir, htmlFile).toString()
        .replaceFirst(REGEX__FILE_MATCH_REPLACE, extension));
  }
}
