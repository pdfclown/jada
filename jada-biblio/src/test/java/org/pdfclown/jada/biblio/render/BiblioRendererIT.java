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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pdfclown.common.build.test.assertion.Verifiers.VERIFIER__FILE;
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
    final var sourceDir = getEnv().resourcePath(getTestName());
    final var targetDir = resetDirectory(getEnv().outputPath(getTestName()));

    Files.copy(sourceDir.resolve("index.html"), targetDir.resolve("index.html"));
    Files.copy(sourceDir.resolve("index-all.html"), targetDir.resolve("index-all.html"));

    var targetBiblioHtmlFile = targetDir.resolve("biblio.html");

    var config = mock(BiblioConfig.class);
    {
      Document biblio = Biblios.biblio(getEnv().resourcePath("biblio.xml"));
      when(config.getBiblio()).thenReturn(biblio);
      when(config.getBiblioOutputFile()).thenReturn(targetBiblioHtmlFile);

      var jadaConfig = mockJadaConfig(null);
      {
        when(jadaConfig.getOutputDirectory()).thenReturn(targetDir);
        when(jadaConfig.getOverviewOutputFile()).thenReturn(targetDir.resolve("index.html"));
      }
      when(config.getConfig()).thenReturn(jadaConfig);
    }
    var renderer = new BiblioRenderer();
    renderer.render(config);

    VERIFIER__FILE.verify(targetBiblioHtmlFile);
    VERIFIER__FILE.verify(targetDir.resolve("index-all.html"));
  }
}
