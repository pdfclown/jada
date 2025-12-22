/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ClassDiagramIT.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.render.model;

import static java.util.Collections.singleton;
import static org.apache.commons.io.file.PathUtils.touch;
import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pdfclown.jada.uml.__test.UmlMocks.mockUmlConfig;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__EXTENDED_BY;

import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.UmlConfig;
import org.pdfclown.jada.uml.UmlConfig.ImageConfig;
import org.pdfclown.jada.uml.__test.BaseIT;

// SourceName: nl.talsmasoftware.umldoclet.uml.ClassDiagramTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class ClassDiagramIT extends BaseIT {
  private UmlConfig config;
  private ImageConfig images;

  // SourceName: testClassWithSuperClassInAnotherPackageRelativePath
  @Test
  void _classWithSuperClassInAnotherPackageRelativePath() throws IOException {
    touch(getEnv().outputPath("foo/bar/Bar.html"));
    touch(getEnv().outputPath("foo/Foo.html"));

    var bar = new Type(
        new Namespace(null, "foo.bar", null),
        Type.Classification.CLASS,
        new TypeName("foo.bar", "Bar", "foo.bar.Bar"));

    var classDiagram = new ClassDiagram(config, bar);

    // Add Superclass com.foo.Foo
    var foo = new Type(new Namespace(null, "foo", null),
        Type.Classification.CLASS,
        new TypeName("foo", "Foo", "foo.Foo"));
    classDiagram.addChild(foo);
    classDiagram.addChild(new Reference(
        Reference.from("foo.Foo", null),
        PUML_REF__EXTENDED_BY,
        Reference.to("foo.bar.Bar", null)));

    classDiagram.render();

    String puml = outputContent("foo/bar/Bar.puml");

    assertThat(puml, containsString("foo.bar.Bar [[Bar.html]]"));
    assertThat(puml, containsString("foo.Foo [[../Foo.html]]"));
  }

  @AfterEach
  void onEachAfter() {
    verify(config, atLeast(1)).getImageConfig();
    verify(images, atLeast(1)).getFormats();
  }

  @BeforeEach
  void onEachBefore() {
    config = mockUmlConfig(null);
    {
      images = mock(ImageConfig.class);
      {
        when(images.getFormats()).thenReturn(singleton(ImageConfig.Format.SVG));
        when(images.getSubDirectory()).thenReturn(null);
      }
      when(config.getImageConfig()).thenReturn(images);

      var jadaConfig = config.getConfig();
      {
        when(jadaConfig.isDebug()).thenReturn(true);
        when(jadaConfig.getOutputDirectory()).thenReturn(getEnv().outputPath(EMPTY));
      }
    }
  }
}
