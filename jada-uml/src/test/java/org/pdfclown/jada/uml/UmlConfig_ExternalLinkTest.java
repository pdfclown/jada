/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UmlConfig_ExternalLinkTest.java) is part of jada-uml module in Jada project
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.pdfclown.common.build.test.assertion.Matchers.matchesEvent;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.jada.uml.__test.UmlTests.mockUmlConfig;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.pdfclown.jada.core.test.assertion.JadaLogCaptor;
import org.pdfclown.jada.uml.UmlConfig.ExternalLink;
import org.pdfclown.jada.uml.__test.BaseTest;
import org.slf4j.event.Level;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.ExternalLinkTest
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
class UmlConfig_ExternalLinkTest extends BaseTest {
  @RegisterExtension
  static JadaLogCaptor logged = new JadaLogCaptor();

  private UmlConfig config;

  UmlConfig_ExternalLinkTest() {
  }

  // SourceName: testIllegalUrls
  @Test
  void _illegalUris() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> new ExternalLink(config, "https://www.google.com?\nq=query", EMPTY));

    assertThat(ex.getMessage(), is("`uri` (\"https://www.google.com?\\nq=query\") INVALID"));
  }

  // SourceName: testNonExistingUrls
  @Test
  void _nonExistingUris() {
    when(config.getConfig().getOutputDirectory()).thenReturn(Path.of(EMPTY));
    var externalLink = new ExternalLink(config, "doesn't-exist", "doesn't-exist");
    Optional<URI> resolved = externalLink.resolveType("com.my.package", "MyBeautifulClass");

    assertThat(resolved, is(Optional.empty()));
    assertThat(logged.getEvents(), hasItem(matchesEvent(Level.WARN,
        "Cannot read package list \"doesn't-exist/package-list\" "
            + "(java.nio.file.NoSuchFileException: doesn't-exist/package-list)",
        null)));
    verify(config.getConfig(), atLeast(1)).getOutputDirectory();
    verify(config, atLeast(1)).getConfig();
  }

  // SourceName: testExternalLinkWithoutApidoc
  @Test
  void _withoutApidoc() {
    NullPointerException ex = assertThrows(NullPointerException.class,
        () -> new ExternalLink(config, null, "packageList"));

    assertThat(ex.getMessage(), is("`apidoc`"));
  }

  // SourceName: testExternalLinkWithoutConfig
  @Test
  void _withoutConfig() {
    NullPointerException expected = assertThrows(NullPointerException.class,
        () -> new ExternalLink(null, "apidoc", "packageList"));

    assertThat(expected.getMessage(), is("`config`"));
  }

  // SourceName: testExternalLinkWithoutPackageListLocation
  @Test
  void _withoutPackageListLocation() {
    NullPointerException ex = assertThrows(NullPointerException.class,
        () -> new ExternalLink(config, "apidoc", null));

    assertThat(ex.getMessage(), is("`packageList`"));
  }

  @AfterEach
  void onEachAfter() {
    verify(config, atLeast(0)).getLog();
    verifyNoMoreInteractions(config);
  }

  @BeforeEach
  void onEachBefore() {
    config = mockUmlConfig(logged);
  }
}
