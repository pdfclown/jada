/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UmlConfigTest.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pdfclown.common.build.test.assertion.Matchers.matchesEvent;
import static org.pdfclown.jada.core.test.JadaMocks.mockJadaConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.pdfclown.jada.core.test.assertion.JadaLogCaptor;
import org.pdfclown.jada.uml.UmlConfig.Visibility;
import org.pdfclown.jada.uml.__test.BaseTest;
import org.slf4j.event.Level;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.DocletConfigTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
class UmlConfigTest extends BaseTest {
  @RegisterExtension
  static JadaLogCaptor logged = new JadaLogCaptor();

  private UmlConfig config;

  @BeforeEach
  void onEachBefore() {
    var extension = mock(UmlExtension.class);
    {
      var jada = mockJadaConfig(logged).getJada();
      when(extension.getJada()).thenReturn(jada);
      when(extension.getConfig()).thenCallRealMethod();
    }
    config = new UmlConfig(extension);
  }

  // SourceName: testShowMembersAll
  @Test
  void showMembers__all() {
    config.showMembers("all");

    assertMemberVisibility(Visibility.PRIVATE, true);
    assertMemberVisibility(Visibility.PACKAGE_PRIVATE, true);
    assertMemberVisibility(Visibility.PROTECTED, true);
    assertMemberVisibility(Visibility.PUBLIC, true);
  }

  // SourceName: testShowMembersPackage
  @Test
  void showMembers__package() {
    config.showMembers("package");

    assertMemberVisibility(Visibility.PRIVATE, false);
    assertMemberVisibility(Visibility.PACKAGE_PRIVATE, true);
    assertMemberVisibility(Visibility.PROTECTED, true);
    assertMemberVisibility(Visibility.PUBLIC, true);
  }

  // SourceName: testShowMembersPrivate
  @Test
  void showMembers__private() {
    config.showMembers("private");

    assertMemberVisibility(Visibility.PRIVATE, true);
    assertMemberVisibility(Visibility.PACKAGE_PRIVATE, true);
    assertMemberVisibility(Visibility.PROTECTED, true);
    assertMemberVisibility(Visibility.PUBLIC, true);
  }

  // SourceName: testShowMembersProtected
  @Test
  void showMembers__protected() {
    config.showMembers("protected");

    assertMemberVisibility(Visibility.PRIVATE, false);
    assertMemberVisibility(Visibility.PACKAGE_PRIVATE, false);
    assertMemberVisibility(Visibility.PROTECTED, true);
    assertMemberVisibility(Visibility.PUBLIC, true);
  }

  // SourceName: testShowMembersPublic
  @Test
  void showMembers__public() {
    config.showMembers("public");

    assertMemberVisibility(Visibility.PRIVATE, false);
    assertMemberVisibility(Visibility.PACKAGE_PRIVATE, false);
    assertMemberVisibility(Visibility.PROTECTED, false);
    assertMemberVisibility(Visibility.PUBLIC, true);
  }

  // SourceName: testShowMembersUnknown
  /**
   * @implNote Unknown setting defaults to the Javadoc standard {@code protected}.
   */
  @Test
  void showMembers__unknown() {
    config.showMembers("unknown");

    assertMemberVisibility(Visibility.PRIVATE, false);
    assertMemberVisibility(Visibility.PACKAGE_PRIVATE, false);
    assertMemberVisibility(Visibility.PROTECTED, true);
    assertMemberVisibility(Visibility.PUBLIC, true);

    assertThat(logged.getEvents(), hasItem(matchesEvent(Level.WARN,
        "Visibility UNKNOWN (\"unknown\"). Expected: [public, protected, package, private, all]",
        null)));
  }

  private void assertMemberVisibility(Visibility visibility, boolean expected) {
    assertThat(config.getFieldConfig().visibilities.contains(visibility), is(expected));
    assertThat(config.getMethodConfig().visibilities.contains(visibility), is(expected));
  }
}
