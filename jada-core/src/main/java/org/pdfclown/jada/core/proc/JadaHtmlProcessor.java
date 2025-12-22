/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaHtmlProcessor.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.proc;

import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__HTML;
import static org.pdfclown.common.util.io.Files.isExtension;

import java.nio.file.Path;
import org.pdfclown.jada.core.system.SystemConfig;
import org.pdfclown.jada.core.system.proc.FileProcess;
import org.pdfclown.jada.core.system.proc.TextSerializer;

/**
 * Javadoc HTML post-processor.
 *
 * @author Stefano Chizzolini
 */
public abstract class JadaHtmlProcessor extends JadaFileProcessor<String> {
  protected JadaHtmlProcessor() {
    super(new TextSerializer());
  }

  @Override
  public void init(SystemConfig config) {
    super.init(config);

    ((TextSerializer) serializer).setCharset(config.getOutputCharset());
  }

  @Override
  public boolean isProcessable(Path file, FileProcess.Context context) {
    return isExtension(file, FILE_EXTENSION__HTML);
  }
}
