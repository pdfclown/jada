/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PackageProtectedSuperclass.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml._issues.umldoclet.issue146;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

// SourceName: nl.talsmasoftware.umldoclet.issues.bug146.PackageProtectedSuperclass
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
class PackageProtectedSuperclass extends AbstractList<String> {
  private final List<String> delegate = new ArrayList<>();

  @Override
  public void add(int index, String value) {
    delegate.add(index, value);
  }

  @Override
  public String get(int index) {
    return delegate.get(index);
  }

  @Override
  public String remove(int index) {
    return delegate.remove(index);
  }

  @Override
  public String set(int index, String value) {
    return delegate.set(index, value);
  }

  @Override
  public int size() {
    return delegate.size();
  }
}
