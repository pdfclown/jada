/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (FileOptimizerTest.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.proc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pdfclown.common.build.test.assertion.Verifiers.VERIFIER__COMBINATION;
import static org.pdfclown.jada.core.test.JadaTests.mockJadaConfig;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.pdfclown.common.util.system.Clis.FileInclusionFilter;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.core.__test.BaseTest;
import org.pdfclown.jada.core.system.proc.FileProcess;

class FileOptimizerTest extends BaseTest {
  private static final String PROCESS_BASE_DIR = "C:\\MyProject\\target\\reports\\apidocs\\";

  private boolean configDebug;
  private FileSystem fs;
  private JadaConfig jadaConfig;
  private FileProcess.Context processContext;

  @AfterAll
  public void onAllAfter() throws IOException {
    fs.close();
  }

  @BeforeAll
  public void onAllBefore() {
    jadaConfig = mockJadaConfig(null);
    {
      when(jadaConfig.isDebug()).then($ -> configDebug);
    }

    fs = Jimfs.newFileSystem(Configuration.windows());

    processContext = mock(FileProcess.Context.class);
    {
      when(processContext.getBaseDir(ArgumentMatchers.any()))
          .thenReturn(fs.getPath(PROCESS_BASE_DIR));
    }
  }

  @Test
  void isProcessable() {
    VERIFIER__COMBINATION.verify(
        (file, fileOptimizationFilter, debug) -> {
          configDebug = debug;

          var fileOptimizer = new FileOptimizer();
          {
            when(jadaConfig.getFileOptimizationFilter())
                .thenReturn((FileInclusionFilter) fileOptimizationFilter);

            fileOptimizer.init(jadaConfig);
          }

          return fileOptimizer.isProcessable(fs.getPath(PROCESS_BASE_DIR + file), processContext);
        },
        List.of("file", "fileOptimizationFilter", "debug"),
        // file
        List.of(
            "sample.js",
            "sample.css",
            "sample.min.js",
            "sample.min.css",
            "sample.html",
            "styles\\sample.css",
            "styles\\other.css",
            "project\\my\\sub\\dir\\sample.js",
            "project\\my\\sub\\another\\scripts\\sample.js",
            "resources\\another\\sub\\sample.js"),
        // fileOptimizationFilter
        List.of(
            new FileInclusionFilter().include("sample.*"),
            new FileInclusionFilter().include("**/sample.*"),
            new FileInclusionFilter().exclude("/styles/**"),
            new FileInclusionFilter().include("/styles/**"),
            new FileInclusionFilter().include("/styles/**").exclude("**/other*")),
        // debug
        List.of(
            false,
            true));
  }
}
