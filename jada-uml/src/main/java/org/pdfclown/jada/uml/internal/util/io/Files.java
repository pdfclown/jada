/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Files.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.internal.util.io;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.newInputStream;
import static org.pdfclown.common.util.io.Files.isFile;
import static org.pdfclown.common.util.io.Files.path;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;

// SourceName: nl.talsmasoftware.umldoclet.util.FileUtils
/**
 * File-related utilities.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public final class Files {
  public static final String FILE_EXTENSION__PLANTUML = ".puml";

  /**
   * Opens a reader to the URI.
   * <p>
   * If the call {@code uri.toURL().openStream()} succeeds, a reader is wrapped and returned.
   * Otherwise, if the URI is absolute, an attempt is made to open it as a file. If this also fails,
   * a last effort is made to open the uri relative to {@code baseDir}.
   * </p>
   *
   * @param baseDir
   *          The base directory to use if the URI turns out to be a relative file link.
   * @param uri
   *          The URI to read from.
   * @param charset
   *          The character set to use for reading.
   * @return The opened reader.
   * @throws IOException
   *           in case the call to {@code uri.toURL().openStream()} threw an I/O Exception.
   */
  public static Reader openReaderTo(Path baseDir, URI uri, Charset charset)
      throws IOException {
    InputStream stream;
    if (isFile(uri)) {
      var f = path(uri);
      if (!exists(f)) {
        f = baseDir.resolve(uri.toASCIIString());
      }
      stream = newInputStream(f);
    } else {
      stream = uri.toURL().openStream();
    }
    return new InputStreamReader(stream, charset);
  }

  private Files() {
  }
}
