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
import static java.util.List.of;
import static org.mockito.Mockito.when;
import static org.pdfclown.common.build.test.assertion.Assertions.ArgumentsStreamStrategy.cartesian;
import static org.pdfclown.common.build.test.assertion.Assertions.argumentsStream;
import static org.pdfclown.common.build.test.assertion.Assertions.assertParameterizedOf;
import static org.pdfclown.common.util.Aggregations.entry;
import static org.pdfclown.jada.core.test.JadaMocks.mockJadaConfig;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pdfclown.common.build.test.assertion.Assertions.Expected;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.core.__test.BaseTest;

class FileOptimizerTest extends BaseTest {
  private boolean configDebug;
  private final FileOptimizer fileOptimizer;
  {
    fileOptimizer = new FileOptimizer();
    JadaConfig jadaConfig = mockJadaConfig(null);
    {
      when(jadaConfig.isDebug()).then($ -> configDebug);

      var excludedOptimizationFiles = List.of("/m?/*/dir", "sam*e.css");
      when(jadaConfig.getExcludedOptimizationFiles()).thenReturn(excludedOptimizationFiles);
    }
    fileOptimizer.init(jadaConfig);
  }

  Stream<Arguments> isProcessable() {
    return argumentsStream(
        cartesian(),
        // expected
        asList(
            // filename[0]: "sample.js"
            // [1] debug[0]: false
            true,
            // [2] debug[1]: true
            false,
            //
            // filename[1]: "scripts/sample.css"
            // [3] debug[0]: false
            false,
            // [4] debug[1]: true
            false,
            //
            // filename[2]: "scripts/other.css"
            // [5] debug[0]: false
            true,
            // [6] debug[1]: true
            false,
            //
            // filename[3]: "sample.html"
            // [7] debug[0]: false
            false,
            // [8] debug[1]: true
            false,
            //
            // filename[4]: "project/my/sub/dir/scripts/sample.js"
            // [9] debug[0]: false
            false,
            // [10] debug[1]: true
            false,
            //
            // filename[5]: "project/my/sub/sample.js"
            // [11] debug[0]: false
            true,
            // [12] debug[1]: true
            false,
            //
            // filename[6]: "sample.min.js"
            // [13] debug[0]: false
            false,
            // [14] debug[1]: true
            false,
            //
            // filename[7]: "sample.min.css"
            // [15] debug[0]: false
            false,
            // [16] debug[1]: true
            false),
        // filename
        asList(
            "sample.js",
            "scripts/sample.css",
            "scripts/other.css",
            "sample.html",
            "project/my/sub/dir/scripts/sample.js",
            "project/my/sub/sample.js",
            "sample.min.js",
            "sample.min.css"),
        asList(
            false,
            true));
  }

  @ParameterizedTest
  @MethodSource
  void isProcessable(Expected<Boolean> expected, String filename, boolean debug) {
    configDebug = debug;
    assertParameterizedOf(
        () -> fileOptimizer.isProcessable(Path.of(filename), null),
        expected,
        () -> expectedGeneration(of(
            entry("filename", filename),
            entry("debug", debug))));
  }
}
