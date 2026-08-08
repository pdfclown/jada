/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (BiblioConfig.java) is part of jada-biblio module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.biblio;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Objects.toStringWithProperties;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.jada.biblio.internal.Internals.XML_ATTR__ID;
import static org.pdfclown.jada.biblio.internal.Internals.XML_ATTR__VERSION;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.pdfclown.jada.core.JadaExtConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Bibliographic configuration.
 *
 * @author Stefano Chizzolini
 * @see BiblioExtension
 */
public class BiblioConfig extends JadaExtConfig {
  /**
   * Bibliographic entry.
   *
   * @author Stefano Chizzolini
   */
  public static class BiblioEntry {
    @SuppressWarnings("NotNullFieldNotInitialized")
    private String label;
    @SuppressWarnings("NotNullFieldNotInitialized")
    private Element element;

    /**
     * <span class="warning">(For internal use only)</span>
     */
    @SuppressWarnings("NullAway")
    protected BiblioEntry() {
    }

    BiblioEntry(Element element, String label) {
      this.element = element;
      this.label = label;
    }

    public Element getElement() {
      return element;
    }

    public String getLabel() {
      return label;
    }

    @Override
    public String toString() {
      String id = element.getAttribute(XML_ATTR__ID);
      if (id.isEmpty()) {
        id = element.hasAttribute(XML_ATTR__VERSION)
            ? element.getAttribute(XML_ATTR__VERSION)
            : "(unknown)";
      }
      return toStringWithProperties(this,
          "ID", id,
          "label", label);
    }
  }

  /**
   * {@link #getStaticBiblioEntryIds() staticBiblioEntryIds} option.
   */
  public static final String OPTION__STATIC_ENTRIES =
      "--biblio-static-entries";
  /**
   * {@link #isUsedBiblioEntriesOnly() usedBiblioEntriesOnly} option.
   */
  public static final String OPTION__USED_ENTRIES_ONLY =
      "--biblio-used-only";

  private @Nullable Document biblio;
  private Map<String, BiblioEntry> biblioEntries = Map.of();
  private @Nullable Path biblioOutputFile;
  private final Set<String> staticBiblioEntryIds = new HashSet<>();
  private boolean usedBiblioEntriesOnly;

  protected BiblioConfig() {
  }

  BiblioConfig(BiblioExtension extension) {
    super(extension);
  }

  /**
   * Bibliographic data.
   */
  public @Nullable Document getBiblio() {
    return biblio;
  }

  /**
   * Gets the bibliographic entry corresponding to the full ID.
   */
  public @Nullable BiblioEntry getBiblioEntry(String fullId) {
    return biblioEntries.get(fullId);
  }

  /**
   * Bibliography file path.
   */
  public Path getBiblioOutputFile() {
    if (biblioOutputFile == null) {
      biblioOutputFile = getConfig().getOutputPage("biblio", EMPTY, null);
    }
    return biblioOutputFile;
  }

  @Override
  public BiblioExtension getExtension() {
    return (BiblioExtension) super.getExtension();
  }

  /**
   * Bibliographic entries to render in the bibliography even if not cited via taglets.
   * <p>
   * CLI option: {@value #OPTION__STATIC_ENTRIES} (repeatable)
   * </p>
   */
  public Set<String> getStaticBiblioEntryIds() {
    return staticBiblioEntryIds;
  }

  /**
   * Whether only bibliographic entries actually cited via taglets are rendered into the
   * bibliography.
   * <p>
   * CLI option: {@value #OPTION__USED_ENTRIES_ONLY}
   * </p>
   */
  public boolean isUsedBiblioEntriesOnly() {
    return usedBiblioEntriesOnly;
  }

  void setBiblio(@Nullable Document value) {
    biblio = value;
  }

  void setBiblioEntries(Map<String, BiblioEntry> value) {
    biblioEntries = requireNonNull(value);
  }

  void setUsedBiblioEntriesOnly(boolean value) {
    usedBiblioEntriesOnly = value;
  }
}
