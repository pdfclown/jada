/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (RemotePlantumlGeneratorIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.render.generator;

import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.isRegularFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__SVG;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import net.sourceforge.plantuml.FileFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.__test.BaseIT;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

// SourceName: nl.talsmasoftware.umldoclet.uml.plantuml.RemotePlantumlGeneratorTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
@Testcontainers
public class RemotePlantumlGeneratorIT extends BaseIT {
  static final String testUml = "@startuml\r\nBob -> Alice : hello\r\n@enduml";

  @Container
  @SuppressWarnings("rawtypes")
  static final GenericContainer PLANTUML_SERVER = new GenericContainer(
      DockerImageName.parse("plantuml/plantuml-server")).withExposedPorts(8080);

  PlantumlGenerator subject;

  @Test
  void _exceptionsAreHandled() throws IOException {
    var mockOutput = mock(OutputStream.class);
    var ioException = new IOException("Stream already closed!");
    doThrow(ioException).when(mockOutput).write(any(byte[].class), anyInt(), anyInt());

    RuntimeException expected = assertThrows(RuntimeException.class,
        () -> subject.generatePlantumlDiagramFromSource(testUml, FileFormat.SVG, mockOutput));
    assertThat(expected.getCause(), sameInstance(ioException));
  }

  @Test
  void _nonHttpBaseUrlsAreRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> new RemotePlantumlGenerator("file:///etc/passwd"));
  }

  @Test
  void _simpleDiagramCanBeGenerated() throws IOException {
    final var testDiagram = getEnv().outputPath("testUml" + FILE_EXTENSION__SVG);
    deleteIfExists(testDiagram);
    try (var out = new FileOutputStream(testDiagram.toFile())) {
      subject.generatePlantumlDiagramFromSource(testUml, FileFormat.SVG, out);
    }

    assertThat(isRegularFile(testDiagram), is(true));
  }

  @BeforeEach
  void onEachBefore() {
    subject = new RemotePlantumlGenerator("http://%s:%s/".formatted(
        PLANTUML_SERVER.getHost(), PLANTUML_SERVER.getMappedPort(8080)));
  }
}
