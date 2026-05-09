/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (FileSerializer.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.system.proc;

import java.nio.file.Path;

/**
 * File serializer.
 *
 * @param <T>
 *          Deserialized object type.
 * @author Stefano Chizzolini
 */
public interface FileSerializer<T> {
  /**
   * Deserializes the object contained in the file path.
   *
   * @param file
   *          Source file.
   * @return Object deserialized from {@code path}.
   */
  T deserialize(Path file);

  /**
   * Serializes the object to the file path.
   *
   * @param obj
   *          Object to serialize.
   * @param file
   *          Target file.
   */
  void serialize(T obj, Path file);
}
