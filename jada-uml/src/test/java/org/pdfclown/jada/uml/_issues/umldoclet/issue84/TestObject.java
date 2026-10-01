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
  Changes: Adaptation and redesign for Jada.
 */
/*
 * Copyright 2016-2026 Talsma ICT
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pdfclown.jada.uml._issues.umldoclet.issue84;

import static org.pdfclown.common.util.Objects.toStringWithProperties;
import static org.pdfclown.common.util.Strings.lcase;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

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

    int diff = lcase(value).compareTo(lcase(other.value));
    return diff == 0 ? value.compareTo(other.value) : diff;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    return this == o || (o instanceof TestObject that
        && this.compareTo(that) == 0);
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
