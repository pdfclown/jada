/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue267_DiagramWithNestedNamespaceIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml._issues.umldoclet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.readString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.fail;
import static org.pdfclown.common.build.test.assertion.Executions.interceptSystemStreams;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__SVG;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.issues.Issue267Test
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Issue267_DiagramWithNestedNamespaceIT extends BaseIT {
  // SourceName: testDiagramWithNestedNamespace
  @Test
  void _main() throws IOException {
    String puml;
    try (var pumlStream = getClass().getResourceAsStream("issue-267-example.puml")) {
      puml = new String(pumlStream.readAllBytes(), UTF_8);
    }
    Path svgDiagram = getEnv().outputPath("example" + FILE_EXTENSION__SVG);

    String output;
    try (var out = new FileOutputStream(svgDiagram.toFile())) {
      output = interceptSystemStreams(() -> {
        try {
          new SourceStringReader(puml).outputImage(out, new FileFormatOption(FileFormat.SVG));
        } catch (IOException ex) {
          fail("I/O error generating image " + svgDiagram, ex);
        }
      }).toLowerCase();
    }

    {
      assertThat(isRegularFile(svgDiagram), is(true));
      assertThat(output, not(containsString("error")));
      assertThat(output, not(containsString("exception")));
    }
    {
      String svgcontent = readString(svgDiagram).toLowerCase();
      assertThat(svgcontent, not(containsString("error")));
      assertThat(svgcontent, not(containsString("exception")));
    }
  }
}
