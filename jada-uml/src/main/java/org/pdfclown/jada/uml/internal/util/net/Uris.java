/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Uris.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.internal.util.net;

import static java.lang.Math.max;
import static org.pdfclown.common.util.Chars.PERCENT;
import static org.pdfclown.common.util.Chars.SLASH;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Strings.S;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.PolyNull;

// SourceName: nl.talsmasoftware.umldoclet.util.UriUtils
/**
 * URI utilities.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public final class Uris {
  /**
   * For simple roll-our-own hex encoding.
   */
  private static final char[] HEX =
      { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

  /**
   * Adds a query parameter to the URI.
   * <p>
   * Since query parameters are scheme-specific, this method only applies to URI's with the
   * following schemes:
   * </p>
   * <ol>
   * <li>{@code "http"}</li>
   * <li>{@code "https"}</li>
   * </ol>
   *
   * @param name
   *          Parameter name.
   * @param value
   *          Parameter value.
   */
  public static @PolyNull @Nullable URI addHttpParam(@PolyNull @Nullable URI uri,
      @Nullable String name, @Nullable String value) {
    if (uri != null && name != null && value != null
        && ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))) {
      final String base = uri.toASCIIString();
      final int queryIdx = base.indexOf('?');
      final int fragmentIdx = base.indexOf('#', max(queryIdx, 0));
      var b = new StringBuilder(fragmentIdx >= 0 ? base.substring(0, fragmentIdx) : base);
      b.append(queryIdx < 0 ? '?' : '&');
      appendEncoded(b, name);
      b.append('=');
      appendEncoded(b, value);
      if (fragmentIdx >= 0) {
        b.append(base, fragmentIdx, base.length());
      }
      return URI.create(b.toString());
    }
    return uri;
  }

  /**
   * Adds a component to the path of the URI.
   */
  public static @PolyNull @Nullable URI addPathComponent(@PolyNull @Nullable URI uri,
      @Nullable String component) {
    if (uri != null && component != null) {
      try {
        return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
            Optional.ofNullable(uri.getPath()).map(path -> join(path, component, SLASH))
                .orElse(component),
            uri.getQuery(), uri.getFragment());
      } catch (URISyntaxException ex) {
        throw runtime("Path concatenation between \"{}\" and \"{}\" FAILED", uri, component, ex);
      }
    }
    return uri;
  }

  /*
   * TODO use Unicode codepoints before escaping, this only works for BMP (which is all we currently
   * need).
   */
  private static void appendEncoded(StringBuilder b, String value) {
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (isUnreserved(c)) {
        b.append(c);
      } else {
        appendEscapedByte(b, (byte) c);
      }
    }
  }

  /**
   * Appends the byte as percent-encoded hex value.
   */
  private static void appendEscapedByte(StringBuilder b, byte value) {
    b.append(PERCENT).append(HEX[(value >> 4) & 0x0f]).append(HEX[value & 0x0f]);
  }

  /**
   * Gets whether the URI character is unreserved.
   * <p>
   * In the URI specification {@biblio.spec URI}, unreserved characters are defined as
   * {@code ALPHA / DIGIT / "-" / "." / "_" / "~"}.
   * </p>
   */
  private static boolean isUnreserved(char ch) {
    return Character.isLetterOrDigit(ch) || ch == '-' || ch == '.' || ch == '_' || ch == '~';
  }

  private static String join(String left, String right, char separator) {
    if (left.isEmpty())
      return right;
    else if (right.isEmpty())
      return left;
    String sep = S + separator;
    return left.endsWith(sep) || right.startsWith(sep) ? left + right : left + separator + right;
  }

  private Uris() {
  }
}
