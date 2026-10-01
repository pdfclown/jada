/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ClassDiagramIT.java) is part of jada-uml module in Jada project
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
package org.pdfclown.jada.uml.render.model;

import static org.apache.commons.io.file.PathUtils.touch;
import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pdfclown.jada.uml.__test.UmlTests.mockUmlConfig;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__EXTENDED_BY;

import java.io.IOException;
import java.util.List;
import java.util.Set;
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
        when(images.getFormats()).thenReturn(Set.of(ImageConfig.Format.SVG));
        when(images.getSubDirectory()).thenReturn(null);
      }
      when(config.getImageConfig()).thenReturn(images);
      when(config.getPlantumlCustomDirectives()).thenReturn(
          List.of("!pragma layout smetana" /* Avoids Graphviz dependency */));

      var jadaConfig = config.getConfig();
      {
        when(jadaConfig.isDebug()).thenReturn(true);
        when(jadaConfig.getOutputDirectory()).thenReturn(getEnv().outputPath(EMPTY));
      }
    }
  }
}
