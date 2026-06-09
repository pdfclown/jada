/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TestObject.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml._issues.umldoclet.issue84;

import static org.pdfclown.common.util.Objects.toStringWithProperties;

import java.util.Locale;
import java.util.Objects;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.TestObject
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
@SuppressWarnings("ConstantValue")
public class TestObject implements Comparable<TestObject> {
  private final String value;

  public TestObject(String value) {
    this.value = value;
  }

  @Override
  public int compareTo(TestObject other) {
    String otherValue = other == null ? null : other.value;
    if (value == null)
      return otherValue == null ? 0 : -1;
    else if (otherValue == null)
      return 1;

    int diff = value.toLowerCase(Locale.ROOT).compareTo(other.value.toLowerCase(Locale.ROOT));
    return diff == 0 ? value.compareTo(other.value) : diff;
  }

  @Override
  public boolean equals(Object other) {
    return this == other
        || (other instanceof TestObject that && this.compareTo(that) == 0);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return toStringWithProperties(this, "value", value);
  }
}
