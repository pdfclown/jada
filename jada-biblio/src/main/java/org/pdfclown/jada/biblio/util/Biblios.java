/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Biblios.java) is part of jada-biblio module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.biblio.util;

import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__XSD;
import static org.pdfclown.common.util.xml.Xmls.documentFactory;
import static org.pdfclown.common.util.xml.Xmls.schemaFactory;
import static org.pdfclown.common.util.xml.Xmls.xml;
import static org.pdfclown.jada.biblio.internal.Internals.FILE_PREFIX__BIBLIO_1_0;

import java.io.IOException;
import java.nio.file.Path;
import javax.xml.transform.stream.StreamSource;
import org.pdfclown.common.util.xml.Xmls.DocumentFactoryProfile;
import org.pdfclown.jada.biblio.BiblioExtension;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Bibliographic utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Biblios {
  private static final String FILENAME__BIBLIO_SCHEMA =
      FILE_PREFIX__BIBLIO_1_0 + FILE_EXTENSION__XSD;

  /**
   * Loads a bibliography.
   */
  public static Document biblio(Path file) throws SAXException, IOException {
    var factory = DocumentFactoryProfile.COMPACT.apply(documentFactory());
    factory.setSchema(schemaFactory().newSchema(new StreamSource(
        BiblioExtension.class.getResourceAsStream(FILENAME__BIBLIO_SCHEMA))));
    return xml(file, factory);
  }

  private Biblios() {
  }
}
