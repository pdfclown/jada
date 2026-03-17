/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (DiagramFile.java) is part of jada-uml module in Jada project
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

import java.nio.file.Path;
import org.pdfclown.jada.uml.UmlConfig.ImageConfig;

// SourceName: nl.talsmasoftware.umldoclet.html.DiagramFile
/**
 * Generated diagram file.
 * <p>
 * Detects whether a documentation file {@linkplain #matches(Path) corresponds} to this diagram,
 * providing the {@linkplain #createInserter(String) inserter} to link that file to this diagram.
 * </p>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
abstract class DiagramFile {
  // SourceName: basedir
  protected final Path baseDir;
  protected final Path diagramFile;
  protected final ImageConfig.Format format;

  DiagramFile(Path baseDir, Path diagramFile, ImageConfig.Format format) {
    this.baseDir = baseDir;
    this.diagramFile = diagramFile;
    this.format = format;
  }

  // SourceName: newInserter
  /**
   * Creates the object responsible to insert this diagram into the corresponding documentation
   * file.
   */
  public abstract PageProcessor.Inserter createInserter(String diagramRelativePath);

  /**
   * Gets whether this diagram corresponds to a documentation file.
   */
  protected abstract boolean matches(Path htmlFile);
}
