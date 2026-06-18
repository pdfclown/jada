/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (RemotePlantumlGenerator.java) is part of jada-uml module in Jada project
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

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.wrongArg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Pattern;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.code.ArobaseStringCompressor2;
import net.sourceforge.plantuml.code.AsciiEncoder;
import net.sourceforge.plantuml.code.CompressionZlib;
import net.sourceforge.plantuml.code.Transcoder;
import net.sourceforge.plantuml.code.TranscoderImpl;

/*
 * @SuppressFBWarnings(value = "URLCONNECTION_SSRF_FD", justification = "We only allow http(s)
 * urls.")
 */
// SourceName: nl.talsmasoftware.umldoclet.uml.plantuml.RemotePlantumlGenerator
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class RemotePlantumlGenerator implements PlantumlGenerator {
  // SourceName: HTTP_URLS
  public static final Pattern PATTERN__HTTP_URL = Pattern.compile(
      "^https?://");

  // SourceName: DEFAULT_PLANTUML_BASE_URL
  private static final String PLANTUML_BASE_URL__DEFAULT = "https://www.plantuml.com/plantuml/";
  private static final Transcoder TRANSCODER = TranscoderImpl.utf8(new AsciiEncoder(),
      new ArobaseStringCompressor2(), new CompressionZlib());

  private final String baseUrl;

  /**
   */
  public RemotePlantumlGenerator(String baseUrl) {
    baseUrl = Objects.toString(baseUrl, PLANTUML_BASE_URL__DEFAULT);
    if (!PATTERN__HTTP_URL.matcher(baseUrl).find())
      throw wrongArg("baseUrl", baseUrl, "PlantUML server address UNSUPPORTED");

    if (!baseUrl.endsWith("/")) {
      baseUrl += "/";
    }
    this.baseUrl = baseUrl;
  }

  @Override
  public void generatePlantumlDiagramFromSource(String plantumlSource, FileFormat format,
      OutputStream out) {
    final String encodedDiagram = encodeDiagram(plantumlSource);
    final String diagramUrl = baseUrl + format.name().toLowerCase() + '/' + encodedDiagram;
    try (InputStream in = new URL(diagramUrl).openConnection().getInputStream()) {
      final byte[] buf = new byte[4096];
      for (int read = in.read(buf); read >= 0; read = in.read(buf)) {
        out.write(buf, 0, read);
      }
    } catch (IOException ex) {
      throw runtime(ex);
    }
  }

  private String encodeDiagram(final String diagramSource) {
    try {
      // TODO internalize transcoder to be able to remove PlantUML dependency altogether.
      return TRANSCODER.encode(requireNonNull(diagramSource, "`diagramSource`"));
    } catch (IOException ex) {
      throw runtime(ex);
    }
  }
}
