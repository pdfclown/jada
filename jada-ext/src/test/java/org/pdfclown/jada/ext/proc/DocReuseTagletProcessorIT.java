/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (DocReuseTagletProcessorIT.java) is part of jada-ext module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.ext.proc;

import static java.nio.file.Files.exists;
import static org.apache.commons.io.file.PathUtils.touch;
import static org.mockito.Mockito.when;
import static org.pdfclown.common.build.test.assertion.Verifiers.FILE;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__JAVA;
import static org.pdfclown.common.util.io.Files.copyDirectory;
import static org.pdfclown.common.util.io.Files.resetDirectory;
import static org.pdfclown.jada.core.test.JadaTests.mockSystemConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.io.ResourceNames;
import org.pdfclown.jada.core.system.proc.src.SrcFileProcess;
import org.pdfclown.jada.ext.proc.DocReuseTagletProcessorIT_.alreadyProcessed.ClassA;
import org.pdfclown.jada.ext.proc.DocReuseTagletProcessorIT_.main.Main;
import org.pdfclown.jada.ext.proc.__test.BaseIT;

/**
 * @author Stefano Chizzolini
 */
class DocReuseTagletProcessorIT extends BaseIT {
  /**
   * Simulates no change can be applied, as the fragments are already in their canonical state;
   * consequently, all source files are expected to be processed without further changes.
   */
  @Test
  void alreadyProcessed() throws IOException {
    SrcFileProcess process = prepareTest(
        ClassA.class,
        false /* timeThresholdInitialized */);

    verifyFileTree(process);
  }

  /**
   * Simulates the first run over a code base, when no time threshold is set yet; consequently, all
   * source files are expected to be processed.
   */
  @Test
  void main__withoutTimeThreshold() throws IOException {
    SrcFileProcess process = prepareTest(
        Main.class,
        false /* timeThresholdInitialized */);

    verifyFileTree(process);
  }

  /**
   * Simulates a subsequent run over a code base, when the time threshold is already set;
   * consequently, only changed source files (that is, those younger than the threshold) are
   * expected to be processed.
   */
  @Test
  void main__withTimeThreshold() throws IOException {
    SrcFileProcess process = prepareTest(
        Main.class,
        true /* timeThresholdInitialized */);

    // Force `ClassA` processing!
    var classAFile = process.getConfig().getBuildDirectory()
        .resolve(org.pdfclown.jada.ext.proc.DocReuseTagletProcessorIT_.main.a.ClassA.class.getName()
            .replace(DOT, File.separatorChar) + FILE_EXTENSION__JAVA);
    if (exists(classAFile)) {
      touch(classAFile);
    } else
      throw new FileNotFoundException(classAFile.toString());

    verifyFileTree(process);
  }

  private SrcFileProcess prepareTest(Class<?> baseType, boolean timeThresholdInitialized)
      throws IOException {
    final var sourceDir = getEnv().typeSrcPath(baseType).getParent();
    final var targetBaseDir = getEnv().outputPath(getTestName());
    final var targetDir = targetBaseDir.resolve(ResourceNames.relBased(EMPTY, baseType));
    copyDirectory(sourceDir, resetDirectory(targetDir));

    var ret = new SrcFileProcess();
    {
      var configMock = mockSystemConfig(null);
      {
        when(configMock.getBuildDirectory()).thenReturn(targetBaseDir);
      }
      if (timeThresholdInitialized) {
        // Set time threshold!
        var thresholdFile = configMock.getBuildDirectory().resolve(
            DocReuseTagletProcessor.class.getName());
        touch(thresholdFile);
      }
      ret.init(configMock);

      ret.getDirectories().add(configMock.getBuildDirectory());
      ret.addProcessor(new DocReuseTagletProcessor());
    }
    return ret;
  }

  private void verifyFileTree(SrcFileProcess process) {
    process.run();

    FILE.verify(process.getConfig().getBuildDirectory());
  }
}
