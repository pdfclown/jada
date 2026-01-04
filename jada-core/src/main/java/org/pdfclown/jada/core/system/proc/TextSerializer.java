/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TextSerializer.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.system.proc;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.runtime;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import org.pdfclown.jada.core.util.Encodable;

/**
 * Serializer for plain text files.
 *
 * @author Stefano Chizzolini
 */
public class TextSerializer implements FileSerializer<String>, Encodable {
  private Charset charset = UTF_8;

  @Override
  public String deserialize(Path file) {
    try {
      return Files.readString(file, charset);
    } catch (IOException ex) {
      throw runtime(ex);
    }
  }

  @Override
  public Charset getCharset() {
    return charset;
  }

  @Override
  public void serialize(String obj, Path file) {
    try {
      Files.writeString(file, obj, charset);
    } catch (IOException ex) {
      throw runtime(ex);
    }
  }

  @Override
  public TextSerializer setCharset(Charset value) {
    charset = requireNonNull(value);
    return this;
  }
}
