/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaMessage.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.internal;

import org.pdfclown.jada.core.Jada;
import org.pdfclown.jada.core.system.Message;

/**
 * {@link Jada} message.
 *
 * @author Stefano Chizzolini
 */
public enum JadaMessage implements Message {
  COMPONENTS,
  COMPONENTS_RESULT,
  ELEMENT_REGISTERED,
  ELEMENT_PATHS_UNAVAILABLE,
  FILE_CONTENT_INCLUDED,
  FILE_PROCESS_INFINITE_LOOP,
  FILE_PROCESS_ITEM_COMPLETE,
  FILE_PROCESSOR_ITEM_COMPLETE,
  INPUT_DIR_NOT_FOUND,
  OBJECT_MISSING,
  OPTIMIZATION_FAILED,
  OPTIMIZATION_ISSUES,
  POST_TAG_UNKNOWN,
  RESOURCE_ATTACH_COPIED,
  RESOURCE_ATTACH_COPIED_SUMMARY,
  RESOURCE_ATTACH_FAILED,
  RESOURCE_ATTACH_SINGLE_COPIED_SUMMARY,
  RESOURCE_ATTACH_SKIPPED,
  RESOURCE_MISSING,
  RESOURCES_ATTACHING,
  RESOURCES_ATTACHING_SINGLE,
  STYLESHEET_DETECT_FAILED,
  TAG_INVALID,
  P__CHANGED,
  P__MISSING,
  P__OPERATION,
  P__OVERVIEW,
  P__PROCESSOR,
  P__RENDERER,
  P__RESOURCE_DIR,
  P__UNCHANGED;

  @Override
  public String getKey() {
    return name();
  }
}
