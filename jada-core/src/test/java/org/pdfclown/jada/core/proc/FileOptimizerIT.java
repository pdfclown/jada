/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (FileOptimizerIT.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.proc;

import static org.mockito.Mockito.mock;
import static org.pdfclown.common.build.test.assertion.Matchers.matchesText;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__CSS;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__JAVASCRIPT;
import static org.pdfclown.common.util.io.Files.filename;
import static org.pdfclown.common.util.io.Files.isExtension;
import static org.pdfclown.jada.core.test.JadaMocks.mockJadaConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.jada.core.__test.BaseIT;
import org.pdfclown.jada.core.system.proc.FileProcess;

public class FileOptimizerIT extends BaseIT {
  private FileOptimizer fileOptimizer;
  {
    fileOptimizer = new FileOptimizer();
    fileOptimizer.init(mockJadaConfig(null));
  }

  Stream<Path> processContent__css() {
    return files(FILE_EXTENSION__CSS);
  }

  @ParameterizedTest
  @MethodSource
  void processContent__css(Path path) throws IOException {
    processContent(path);
  }

  Stream<Path> processContent__js() {
    return files(FILE_EXTENSION__JAVASCRIPT);
  }

  @ParameterizedTest
  @MethodSource
  void processContent__js(Path path) throws IOException {
    processContent(path);
  }

  private Stream<Path> files(String extension) {
    try {
      return Files.list(getEnv().resourcePath(EMPTY))
          .filter($ -> isExtension($, extension))
          .sorted();
    } catch (IOException ex) {
      throw runtime(ex);
    }
  }

  private void processContent(Path path) throws IOException {
    String input = Files.readString(path);
    var context = mock(FileProcess.Context.class);
    String output = fileOptimizer.processContent(input, path, context);
    Path expectedPath = path.getParent().resolve("expected/" + filename(path));
    String expectedOutput = Files.readString(expectedPath);

    MatcherAssert.assertThat(output, matchesText(expectedOutput));
  }
}
