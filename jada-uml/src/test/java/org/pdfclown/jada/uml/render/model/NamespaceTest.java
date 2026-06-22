/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (NamespaceTest.java) is part of jada-uml module in Jada project
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.uml.UmlConfig;
import org.pdfclown.jada.uml.UmlConfig.ImageConfig;
import org.pdfclown.jada.uml.__test.BaseTest;

// SourceName: nl.talsmasoftware.umldoclet.uml.NamespaceTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
class NamespaceTest extends BaseTest {
  // SourceName: nl.talsmasoftware.umldoclet.util.TestUtil.randomString()
  private static String randomString() {
    return UUID.randomUUID().toString();
  }

  private UmlConfig config;
  private ImageConfig images;

  // SourceName: testEquals
  @Test
  void equals() {
    var packageUml = new PackageDiagram(config, "a.b.c", randomString());
    var namespace = new Namespace(packageUml, "a.b.c", randomString());

    assertThat(namespace.equals(namespace), is(true));
    assertThat(namespace, is(equalTo(new Namespace(null, "a.b.c", randomString()))));
    assertThat(namespace, is(equalTo(new Namespace(packageUml, "a.b.c", randomString()))));
    assertThat(namespace, is(not(equalTo(new Namespace(packageUml, "A.B.C", randomString())))));
    verify(config, atLeastOnce()).getPlantumlServerUrl();
  }

  @AfterEach
  void onEachAfter() {
    verify(config, atLeast(0)).getImageConfig();
    verify(images, atLeast(0)).getFormats();
    verifyNoMoreInteractions(config, images);
  }

  @BeforeEach
  void onEachBefore() {
    images = mock(ImageConfig.class);
    {
      when(images.getFormats()).thenReturn(Set.of(ImageConfig.Format.SVG));
    }

    config = mock(UmlConfig.class);
    {
      when(config.getImageConfig()).thenReturn(images);
    }
  }
}
