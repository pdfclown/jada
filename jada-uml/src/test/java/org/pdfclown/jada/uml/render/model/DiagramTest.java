/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (DiagramTest.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.render.model;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.pdfclown.jada.uml.__test.UmlTests.mockUmlConfig;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.io.Indent;
import org.pdfclown.common.util.io.IndentWriter;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.uml.UmlConfig;
import org.pdfclown.jada.uml.UmlConfig.ImageConfig;
import org.pdfclown.jada.uml.__test.BaseTest;

// SourceName: nl.talsmasoftware.umldoclet.uml.DiagramTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
class DiagramTest extends BaseTest {
  static class TestDiagram extends Diagram {
    private final Path plantumlFile;

    private TestDiagram(UmlConfig config, Path plantumlFile) {
      super(config);

      this.plantumlFile = plantumlFile;
    }

    @Override
    protected Path getPlantUmlFile() {
      return plantumlFile;
    }
  }

  private UmlConfig config;
  private final Set<ImageConfig.Format> formats = new LinkedHashSet<>();
  private ImageConfig imageconfig;
  private JadaConfig jadaConfig;

  // SourceName: testCustomBackgroundcolor
  @Test
  void _customBackgroundcolor() throws IOException {
    when(config.getPlantumlCustomDirectives())
        .thenReturn(List.of("skinparam backgroundcolor green"));
    var output = new StringWriter();
    var testDiagram = new TestDiagram(config,
        Path.of("target/test-classes/custom-directive.puml"));
    var writer = IndentWriter.of(output, Indent.NONE);
    testDiagram.writeTo(writer);

    assertThat(asList(output.toString().split("\\n")), hasItem("skinparam backgroundcolor green"));
    verify(config).getPlantumlCustomDirectives();
  }

  // SourceName: testCustomDirective
  @Test
  void _customDirective() throws IOException {
    when(config.getPlantumlCustomDirectives())
        .thenReturn(List.of("skinparam handwritten true"));
    var output = new StringWriter();
    var testDiagram = new TestDiagram(config,
        Path.of("target/test-classes/custom-directive.puml"));
    var writer = IndentWriter.of(output, Indent.NONE);
    testDiagram.writeTo(writer);

    assertThat(asList(output.toString().split("\\n")), hasItem("skinparam handwritten true"));
    verify(config).getPlantumlCustomDirectives();
  }

  // SourceName: testDiagramWithoutConfiguration
  @Test
  void _withoutConfiguration() {
    NullPointerException expected = assertThrows(NullPointerException.class,
        () -> new TestDiagram(null, null));

    assertThat("Expected exception message", expected.getMessage(), notNullValue());
  }

  // SourceName: testWithoutImageDir
  @Test
  void _withoutImageDir() {
    reset(imageconfig);
    when(imageconfig.getFormats()).thenReturn(formats);
    when(imageconfig.getSubDirectory()).thenReturn(null);

    assertThat(new TestDiagram(config, Path.of("target/test-classes/foo/bar.puml")),
        hasToString(equalTo("target/test-classes/foo/bar.svg")));
  }

  @AfterEach
  void onEachAfter() {
    verify(config, atLeast(0)).getPlantumlServerUrl();
    verify(config, atLeast(0)).getImageConfig();
    verify(config, atLeast(0)).getConfig();
    verify(jadaConfig, atLeast(0)).getOutputDirectory();
    verify(config, atLeast(0)).getLog();
    verify(imageconfig, atLeast(0)).getFormats();
    verify(imageconfig, atLeast(0)).getSubDirectory();
    verifyNoMoreInteractions(imageconfig, config);

    formats.clear();
  }

  @BeforeEach
  void onEachBefore() {
    formats.add(ImageConfig.Format.SVG);

    config = mockUmlConfig(null);
    {
      imageconfig = mock(ImageConfig.class);
      {
        when(imageconfig.getFormats()).thenReturn(formats);
        when(imageconfig.getSubDirectory()).thenReturn("images");
      }
      when(config.getImageConfig()).thenReturn(imageconfig);
    }

    jadaConfig = config.getConfig();
    {
      when(jadaConfig.isDebug()).thenReturn(true);
      when(jadaConfig.getOutputDirectory()).thenReturn(Path.of("target/test-classes"));
    }
  }

  // SourceName: testDiagramToString
  @Test
  void toString_() {
    assertThat(new TestDiagram(config, Path.of("target/test-classes/foo/bar.puml")),
        hasToString(equalTo("target/test-classes/images/foo.bar.svg")));
  }

  // SourceName: testDiagramToStringMultipleFormats
  @Test
  void toString_multipleFormats() {
    formats.add(ImageConfig.Format.PNG);

    assertThat(new TestDiagram(config, Path.of("target/test-classes/foo/bar.puml")),
        hasToString(equalTo("target/test-classes/images/foo.bar.[svg,png]")));
  }
}
