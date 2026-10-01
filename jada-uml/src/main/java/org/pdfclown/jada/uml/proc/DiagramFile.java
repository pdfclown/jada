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
  Changes: Adaptation and redesign for Jada.
 */
/*
 * Copyright 2016-2026 Talsma ICT
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
