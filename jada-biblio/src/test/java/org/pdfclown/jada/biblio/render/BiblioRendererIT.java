/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (BiblioRendererIT.java) is part of jada-biblio module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.biblio.render;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pdfclown.common.build.test.assertion.Matchers.matchesFileContent;
import static org.pdfclown.common.util.Objects.sqn;
import static org.pdfclown.common.util.io.Files.resetDirectory;
import static org.pdfclown.jada.core.test.JadaMocks.mockJadaConfig;

import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.pdfclown.jada.biblio.BiblioConfig;
import org.pdfclown.jada.biblio.__test.BaseIT;
import org.pdfclown.jada.biblio.util.Biblios;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Stefano Chizzolini
 */
class BiblioRendererIT extends BaseIT {
  @Test
  void render__java11() throws IOException, SAXException {
    render();
  }

  @Test
  void render__java17() throws IOException, SAXException {
    render();
  }

  private void render() throws IOException, SAXException {
    final var sourceBaseDir = getEnv().resourcePath("_" + sqn(this));
    final var sourceDir = sourceBaseDir.resolve(getTestMethodName());
    final var targetDir = resetDirectory(getEnv().outputPath(getTestMethodName()));

    Files.copy(sourceDir.resolve("index_original.html"), targetDir.resolve("index.html"));
    Files.copy(sourceDir.resolve("index-all_original.html"), targetDir.resolve("index-all.html"));

    var biblioHtmlFile = targetDir.resolve("biblio.html");

    var config = mock(BiblioConfig.class);
    {
      Document biblio = Biblios.biblio(sourceBaseDir.resolve("biblio.xml"));
      //noinspection DataFlowIssue : ignore nullability
      when(config.getBiblio()).thenReturn(biblio);
      when(config.getBiblioOutputFile()).thenReturn(biblioHtmlFile);

      var jadaConfig = mockJadaConfig(null);
      {
        when(jadaConfig.getOutputDirectory()).thenReturn(targetDir);
        when(jadaConfig.getOverviewOutputFile()).thenReturn(targetDir.resolve("index.html"));
      }
      when(config.getConfig()).thenReturn(jadaConfig);
    }
    var renderer = new BiblioRenderer();
    renderer.render(config);

    assertThat(biblioHtmlFile, matchesFileContent(sourceDir.resolve("biblio_expected.html")));
    assertThat(targetDir.resolve("index-all.html"),
        matchesFileContent(sourceDir.resolve("index-all_expected.html")));
  }
}
