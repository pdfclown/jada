/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Jsoups.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.util.html;

import java.io.IOException;
import java.nio.file.Path;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

/**
 * Jsoup utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Jsoups {
  /**
   * Gets whether the nodes are equivalent.
   * <p>
   * Nodes are equivalent if they are identical, or belong to the same type, with the same node name
   * and equivalent attributes and child nodes.
   * </p>
   */
  public static boolean equal(Node n1, Node n2) {
    if (n1 == n2)
      return true;
    else if (n1.getClass() != n2.getClass() || !n1.nodeName().equals(n2.nodeName()))
      return false;

    if (n1.attributesSize() != n2.attributesSize())
      return false;

    for (Attribute attr1 : n1.attributes()) {
      if (!attr1.getValue().equals(n2.attr(attr1.getKey())))
        return false;
    }

    if (n1 instanceof Element e1) {
      var e2 = (Element) n2;
      if (e1.childNodeSize() != e2.childNodeSize())
        return false;

      for (int i = 0, l = e1.childNodeSize(); i < l; i++) {
        if (!equal(e1.childNode(i), e1.childNode(i)))
          return false;
      }
    }
    return true;
  }

  /**
   * Loads the HTML file.
   */
  public static Document parse(Path path) throws IOException {
    return doParse(Jsoup.parse(path));
  }

  /**
   * Loads the HTML content.
   */
  public static Document parse(String html) throws IOException {
    return doParse(Jsoup.parse(html));
  }

  private static Document doParse(Document doc) {
    doc.outputSettings().prettyPrint(false);
    return doc;
  }

  private Jsoups() {
  }
}
