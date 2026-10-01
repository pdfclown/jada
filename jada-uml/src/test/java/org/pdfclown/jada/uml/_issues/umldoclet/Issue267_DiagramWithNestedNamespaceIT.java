/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Issue267_DiagramWithNestedNamespaceIT.java) is part of jada-uml module in Jada project
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
import static org.pdfclown.common.util.Strings.lcase;
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
      output = lcase(interceptSystemStreams(() -> {
        try {
          new SourceStringReader(puml).outputImage(out, new FileFormatOption(FileFormat.SVG));
        } catch (IOException ex) {
          fail("I/O error generating image " + svgDiagram, ex);
        }
      }));
    }

    {
      assertThat(isRegularFile(svgDiagram), is(true));
      assertThat(output, not(containsString("error")));
      assertThat(output, not(containsString("exception")));
    }
    {
      String svgcontent = lcase(readString(svgDiagram));
      assertThat(svgcontent, not(containsString("error")));
      assertThat(svgcontent, not(containsString("exception")));
    }
  }
}
