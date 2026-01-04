/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PageProcessor.java) is part of jada-uml module in Jada project
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

import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Objects.objTo;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__SVG;
import static org.pdfclown.common.util.io.Files.filename;
import static org.pdfclown.common.util.io.Files.isExtension;
import static org.pdfclown.common.util.io.Files.relativize;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import javax.tools.Diagnostic.Kind;
import org.jsoup.nodes.Document;
import org.jspecify.annotations.Nullable;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.core.proc.JadaHtmlProcessor;
import org.pdfclown.jada.core.system.proc.FileProcess.Context;
import org.pdfclown.jada.core.util.html.Jsoups;
import org.pdfclown.jada.uml.UmlConfig;

// SourceName: nl.talsmasoftware.umldoclet.html.HtmlPostprocessor
// SourceName: nl.talsmasoftware.umldoclet.html.Postprocessor
/**
 * Javadoc HTML post-processor for UML diagram insertion.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class PageProcessor extends JadaHtmlProcessor {
  // SourceName: nl.talsmasoftware.umldoclet.html.Postprocessor.Inserter
  /**
   * Inserts a diagram into the corresponding documentation file.
   *
   * @author Sjoerd Talsma (original implementation)
   * @author Stefano Chizzolini (adaptation and redesign for Jada)
   */
  abstract static class Inserter {
    private static final String IMAGE_CLASS = "uml";
    private static final String IMAGE_ID = "uml";

    protected final String diagramRelativePath;

    Inserter(String diagramRelativePath) {
      this.diagramRelativePath = diagramRelativePath;
    }

    protected abstract String getDiagramTypeDescription();

    protected void logMissingNode(Kind kind, Path file, String nodeDescription,
        String message, JadaConfig config) {
      config.getLog().print(kind, this,
          "{0} diagram \"{1}\" {2} (node {3} MISSING from \"{4}\")",
          getDiagramTypeDescription(), filename(diagramRelativePath), message, nodeDescription,
          file);
    }

    /**
     * Inserts the provided diagram into a documentation file.
     *
     * @return {@code null}, if no change to {@code doc} occurred.
     */
    protected abstract @Nullable Document process(Document doc, Path docFile, JadaConfig config);

    /**
     * @param alt
     *          Alternate description.
     * @param umlClass
     *          UML-specific CSS class.
     * @param style
     *          Tag style.
     * @return {@code <object>} tag for {@code SVG} diagrams (link-aware), otherwise {@code <img>}
     *         tag (without links).
     */
    String getImageTag(String alt, String umlClass, String style) {
      return "<img id=\"%s\" class=\"%s %s\" src=\"%s\" style=\"%s\" alt=\"%s UML Diagram\"%s/>"
          .formatted(IMAGE_ID, IMAGE_CLASS, umlClass, diagramRelativePath, style, alt,
              isExtension(diagramRelativePath, FILE_EXTENSION__SVG) ? " onload=\"SVGInject(this)\""
                  : EMPTY);
    }
  }

  private final Collection<DiagramFile> diagrams;

  /**
   */
  public PageProcessor(UmlConfig config) {
    try {
      diagrams = new DiagramCollector(config).collectDiagrams();
    } catch (IOException ex) {
      throw runtime("Diagrams collection from HTML files in %s FAILED",
          config.getConfig().getOutputDirectory(), ex);
    }
  }

  /**
   * @return {@code -50}
   */
  @Override
  public int getPriority() {
    return -50;
  }

  @Override
  protected @Nullable String processContent(String content, Path file, Context context) {
    for (DiagramFile diagram : diagrams) {
      if (diagram.matches(file)) {
        String newFileContent = insertDiagram(diagram, content, file);
        if (newFileContent != null) {
          content = newFileContent;
          context.changeFile();
        }
        break;
      }
    }
    return content;
  }

  /**
   * Links a Javadoc document to the corresponding diagram.
   *
   * @return Updated {@code docContent}; {@code null}, if no change occurred.
   */
  private @Nullable String insertDiagram(DiagramFile diagram, String docContent, Path docFile) {
    try {
      String diagramRelativePath = relativize(docFile, diagram.diagramFile).toString();
      Inserter inserter = diagram.createInserter(diagramRelativePath);
      Document doc = Jsoups.parse(docContent);
      return objTo(inserter.process(doc, docFile, getConfig()), Document::outerHtml);
    } catch (IOException ex) {
      throw runtime(ex);
    }
  }
}
