/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaTaglet.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.taglet;

import static org.pdfclown.common.util.Objects.nonNull;
import static org.pdfclown.common.util.Objects.xcast;

import java.util.EnumSet;
import java.util.Set;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Taglet;
import org.pdfclown.common.util.annot.InitNonNull;
import org.pdfclown.jada.core.Jada;
import org.pdfclown.jada.core.JadaEnvironment;
import org.pdfclown.jada.core.JadaObject;

/**
 * Base Jada taglet.
 *
 * @author Stefano Chizzolini
 * @implSpec Implementers are recommended to derive their taglets from its subclasses:
 *           <ul>
 *           <li>{@link MainTaglet} for main processing (and pre-processing)</li>
 *           <li>{@link PostTaglet} for post-processing</li>
 *           </ul>
 * @see Jada
 */
public abstract class JadaTaglet implements Taglet, JadaObject {
  /**
   * Gets the representation of the faulty tag to be included in the generated output.
   */
  protected static String toFailureString(String tagString) {
    return "{If you see this message, \"%s\" tag was not resolved}".formatted(tagString);
  }

  @SuppressWarnings("NotNullFieldNotInitialized")
  private @InitNonNull Jada jada;

  @Override
  public Set<Location> getAllowedLocations() {
    return EnumSet.allOf(Location.class);
  }

  @Override
  public Jada getJada() {
    return jada;
  }

  @Override
  public abstract String getName();

  /**
   * @implSpec Implementers must call the overridden method.
   */
  @Override
  public void init(DocletEnvironment env, Doclet doclet) {
    jada = nonNull((JadaEnvironment) xcast(env)).getJada();
    getConfig().registerTaglet(this);
  }

  @Override
  public boolean isInlineTag() {
    return true;
  }

  public void term() {
  }
}
