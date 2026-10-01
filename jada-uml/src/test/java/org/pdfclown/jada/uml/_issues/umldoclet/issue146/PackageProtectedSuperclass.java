/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PackageProtectedSuperclass.java) is part of jada-uml module in Jada project
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
