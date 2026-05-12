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

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.jada.core.test.JadaMocks.mockJadaConfig;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.core.__test.BaseTest;
import org.pdfclown.jada.core.internal.temp.util.system.Clis.FileInclusionFilter;
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

  Stream<Arguments> isProcessable() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // file[0]: "C:\\MyProject\\target\\reports\\apidocs\\sample.js"
            // -- fileOptimizationFilter[0]: "JadaConfig.FileInclusionFilter[includes=[sample.*], excludes=[]]"
            // [1] debug[0]: false
            false,
            // [2] debug[1]: true
            false,
            // -- fileOptimizationFilter[1]: "JadaConfig.FileInclusionFilter[includes=[**/sample.*], excludes=[]]"
            // [3] debug[0]: false
            true,
            // [4] debug[1]: true
            false,
            // -- fileOptimizationFilter[2]: "JadaConfig.FileInclusionFilter[includes=[], excludes=[/styles/**]]"
            // [5] debug[0]: false
            true,
            // [6] debug[1]: true
            false,
            // -- fileOptimizationFilter[3]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[]]"
            // [7] debug[0]: false
            false,
            // [8] debug[1]: true
            false,
            // -- fileOptimizationFilter[4]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[**/other*]]"
            // [9] debug[0]: false
            false,
            // [10] debug[1]: true
            false,
            //
            // file[1]: "C:\\MyProject\\target\\reports\\apidocs\\sample.css"
            // -- fileOptimizationFilter[0]: "JadaConfig.FileInclusionFilter[includes=[sample.*], excludes=[]]"
            // [11] debug[0]: false
            false,
            // [12] debug[1]: true
            false,
            // -- fileOptimizationFilter[1]: "JadaConfig.FileInclusionFilter[includes=[**/sample.*], excludes=[]]"
            // [13] debug[0]: false
            true,
            // [14] debug[1]: true
            false,
            // -- fileOptimizationFilter[2]: "JadaConfig.FileInclusionFilter[includes=[], excludes=[/styles/**]]"
            // [15] debug[0]: false
            true,
            // [16] debug[1]: true
            false,
            // -- fileOptimizationFilter[3]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[]]"
            // [17] debug[0]: false
            false,
            // [18] debug[1]: true
            false,
            // -- fileOptimizationFilter[4]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[**/other*]]"
            // [19] debug[0]: false
            false,
            // [20] debug[1]: true
            false,
            //
            // file[2]: "C:\\MyProject\\target\\reports\\apidocs\\sample.min.js"
            // -- fileOptimizationFilter[0]: "JadaConfig.FileInclusionFilter[includes=[sample.*], excludes=[]]"
            // [21] debug[0]: false
            false,
            // [22] debug[1]: true
            false,
            // -- fileOptimizationFilter[1]: "JadaConfig.FileInclusionFilter[includes=[**/sample.*], excludes=[]]"
            // [23] debug[0]: false
            false,
            // [24] debug[1]: true
            false,
            // -- fileOptimizationFilter[2]: "JadaConfig.FileInclusionFilter[includes=[], excludes=[/styles/**]]"
            // [25] debug[0]: false
            false,
            // [26] debug[1]: true
            false,
            // -- fileOptimizationFilter[3]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[]]"
            // [27] debug[0]: false
            false,
            // [28] debug[1]: true
            false,
            // -- fileOptimizationFilter[4]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[**/other*]]"
            // [29] debug[0]: false
            false,
            // [30] debug[1]: true
            false,
            //
            // file[3]: "C:\\MyProject\\target\\reports\\apidocs\\sample.min.css"
            // -- fileOptimizationFilter[0]: "JadaConfig.FileInclusionFilter[includes=[sample.*], excludes=[]]"
            // [31] debug[0]: false
            false,
            // [32] debug[1]: true
            false,
            // -- fileOptimizationFilter[1]: "JadaConfig.FileInclusionFilter[includes=[**/sample.*], excludes=[]]"
            // [33] debug[0]: false
            false,
            // [34] debug[1]: true
            false,
            // -- fileOptimizationFilter[2]: "JadaConfig.FileInclusionFilter[includes=[], excludes=[/styles/**]]"
            // [35] debug[0]: false
            false,
            // [36] debug[1]: true
            false,
            // -- fileOptimizationFilter[3]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[]]"
            // [37] debug[0]: false
            false,
            // [38] debug[1]: true
            false,
            // -- fileOptimizationFilter[4]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[**/other*]]"
            // [39] debug[0]: false
            false,
            // [40] debug[1]: true
            false,
            //
            // file[4]: "C:\\MyProject\\target\\reports\\apidocs\\sample.html"
            // -- fileOptimizationFilter[0]: "JadaConfig.FileInclusionFilter[includes=[sample.*], excludes=[]]"
            // [41] debug[0]: false
            false,
            // [42] debug[1]: true
            false,
            // -- fileOptimizationFilter[1]: "JadaConfig.FileInclusionFilter[includes=[**/sample.*], excludes=[]]"
            // [43] debug[0]: false
            false,
            // [44] debug[1]: true
            false,
            // -- fileOptimizationFilter[2]: "JadaConfig.FileInclusionFilter[includes=[], excludes=[/styles/**]]"
            // [45] debug[0]: false
            false,
            // [46] debug[1]: true
            false,
            // -- fileOptimizationFilter[3]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[]]"
            // [47] debug[0]: false
            false,
            // [48] debug[1]: true
            false,
            // -- fileOptimizationFilter[4]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[**/other*]]"
            // [49] debug[0]: false
            false,
            // [50] debug[1]: true
            false,
            //
            // file[5]: "C:\\MyProject\\target\\reports\\apidocs\\styles\\sample.css"
            // -- fileOptimizationFilter[0]: "JadaConfig.FileInclusionFilter[includes=[sample.*], excludes=[]]"
            // [51] debug[0]: false
            false,
            // [52] debug[1]: true
            false,
            // -- fileOptimizationFilter[1]: "JadaConfig.FileInclusionFilter[includes=[**/sample.*], excludes=[]]"
            // [53] debug[0]: false
            true,
            // [54] debug[1]: true
            false,
            // -- fileOptimizationFilter[2]: "JadaConfig.FileInclusionFilter[includes=[], excludes=[/styles/**]]"
            // [55] debug[0]: false
            false,
            // [56] debug[1]: true
            false,
            // -- fileOptimizationFilter[3]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[]]"
            // [57] debug[0]: false
            true,
            // [58] debug[1]: true
            false,
            // -- fileOptimizationFilter[4]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[**/other*]]"
            // [59] debug[0]: false
            true,
            // [60] debug[1]: true
            false,
            //
            // file[6]: "C:\\MyProject\\target\\reports\\apidocs\\styles\\other.css"
            // -- fileOptimizationFilter[0]: "JadaConfig.FileInclusionFilter[includes=[sample.*], excludes=[]]"
            // [61] debug[0]: false
            false,
            // [62] debug[1]: true
            false,
            // -- fileOptimizationFilter[1]: "JadaConfig.FileInclusionFilter[includes=[**/sample.*], excludes=[]]"
            // [63] debug[0]: false
            false,
            // [64] debug[1]: true
            false,
            // -- fileOptimizationFilter[2]: "JadaConfig.FileInclusionFilter[includes=[], excludes=[/styles/**]]"
            // [65] debug[0]: false
            false,
            // [66] debug[1]: true
            false,
            // -- fileOptimizationFilter[3]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[]]"
            // [67] debug[0]: false
            true,
            // [68] debug[1]: true
            false,
            // -- fileOptimizationFilter[4]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[**/other*]]"
            // [69] debug[0]: false
            false,
            // [70] debug[1]: true
            false,
            //
            // file[7]: "C:\\MyProject\\target\\reports\\apidocs\\project\\my\\sub\\dir\\sample.js"
            // -- fileOptimizationFilter[0]: "JadaConfig.FileInclusionFilter[includes=[sample.*], excludes=[]]"
            // [71] debug[0]: false
            false,
            // [72] debug[1]: true
            false,
            // -- fileOptimizationFilter[1]: "JadaConfig.FileInclusionFilter[includes=[**/sample.*], excludes=[]]"
            // [73] debug[0]: false
            true,
            // [74] debug[1]: true
            false,
            // -- fileOptimizationFilter[2]: "JadaConfig.FileInclusionFilter[includes=[], excludes=[/styles/**]]"
            // [75] debug[0]: false
            true,
            // [76] debug[1]: true
            false,
            // -- fileOptimizationFilter[3]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[]]"
            // [77] debug[0]: false
            false,
            // [78] debug[1]: true
            false,
            // -- fileOptimizationFilter[4]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[**/other*]]"
            // [79] debug[0]: false
            false,
            // [80] debug[1]: true
            false,
            //
            // file[8]: "C:\\MyProject\\target\\reports\\apidocs\\project\\my\\sub\\another\\scripts\\sample.js"
            // -- fileOptimizationFilter[0]: "JadaConfig.FileInclusionFilter[includes=[sample.*], excludes=[]]"
            // [81] debug[0]: false
            false,
            // [82] debug[1]: true
            false,
            // -- fileOptimizationFilter[1]: "JadaConfig.FileInclusionFilter[includes=[**/sample.*], excludes=[]]"
            // [83] debug[0]: false
            true,
            // [84] debug[1]: true
            false,
            // -- fileOptimizationFilter[2]: "JadaConfig.FileInclusionFilter[includes=[], excludes=[/styles/**]]"
            // [85] debug[0]: false
            true,
            // [86] debug[1]: true
            false,
            // -- fileOptimizationFilter[3]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[]]"
            // [87] debug[0]: false
            false,
            // [88] debug[1]: true
            false,
            // -- fileOptimizationFilter[4]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[**/other*]]"
            // [89] debug[0]: false
            false,
            // [90] debug[1]: true
            false,
            //
            // file[9]: "C:\\MyProject\\target\\reports\\apidocs\\resources\\another\\sub\\sample.js"
            // -- fileOptimizationFilter[0]: "JadaConfig.FileInclusionFilter[includes=[sample.*], excludes=[]]"
            // [91] debug[0]: false
            false,
            // [92] debug[1]: true
            false,
            // -- fileOptimizationFilter[1]: "JadaConfig.FileInclusionFilter[includes=[**/sample.*], excludes=[]]"
            // [93] debug[0]: false
            true,
            // [94] debug[1]: true
            false,
            // -- fileOptimizationFilter[2]: "JadaConfig.FileInclusionFilter[includes=[], excludes=[/styles/**]]"
            // [95] debug[0]: false
            true,
            // [96] debug[1]: true
            false,
            // -- fileOptimizationFilter[3]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[]]"
            // [97] debug[0]: false
            false,
            // [98] debug[1]: true
            false,
            // -- fileOptimizationFilter[4]: "JadaConfig.FileInclusionFilter[includes=[/styles/**], excludes=[**/other*]]"
            // [99] debug[0]: false
            false,
            // [100] debug[1]: true
            false),
        // file
        List.of(
            PROCESS_BASE_DIR + "sample.js",
            PROCESS_BASE_DIR + "sample.css",
            PROCESS_BASE_DIR + "sample.min.js",
            PROCESS_BASE_DIR + "sample.min.css",
            PROCESS_BASE_DIR + "sample.html",
            PROCESS_BASE_DIR + "styles\\sample.css",
            PROCESS_BASE_DIR + "styles\\other.css",
            PROCESS_BASE_DIR + "project\\my\\sub\\dir\\sample.js",
            PROCESS_BASE_DIR + "project\\my\\sub\\another\\scripts\\sample.js",
            PROCESS_BASE_DIR + "resources\\another\\sub\\sample.js"),
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

  @ParameterizedTest
  @MethodSource
  void isProcessable(Expected<Boolean> expected, String file,
      FileInclusionFilter fileOptimizationFilter, boolean debug) {
    configDebug = debug;

    var fileOptimizer = new FileOptimizer();
    {
      when(jadaConfig.getFileOptimizationFilter()).thenReturn(fileOptimizationFilter);

      fileOptimizer.init(jadaConfig);
    }

    assertParameterizedOf(
        () -> fileOptimizer.isProcessable(fs.getPath(file), processContext),
        expected,
        () -> this.<Boolean>expectedGeneration(file, fileOptimizationFilter, debug)
            .setMaxArgCommentLength(150));
  }
}
