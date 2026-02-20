/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ClassB.java) is part of jada-ext module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.ext.proc._DocReuseTagletProcessorIT.main.b;

import org.pdfclown.jada.ext.proc._DocReuseTagletProcessorIT.main.Main;
import org.pdfclown.jada.ext.proc._DocReuseTagletProcessorIT.main.a.ClassA;
import org.pdfclown.jada.ext.proc._DocReuseTagletProcessorIT.main.a.ClassA.InnerClassA.SubInnerClassA;

/**
 * <h4>[Name resolution 4] Module-level inner type</h4>
 * <p>
 * Referenced via fully-qualified {@link ClassA.InnerClassA.SubInnerClassA}
 * </p>
 * {@jada.reuseDoc org.pdfclown.jada.ext.proc._DocReuseTagletProcessorIT.main.a.ClassA.InnerClassA.SubInnerClassA#size(*)}
       * <p>
       * This is from {@link org.pdfclown.jada.ext.proc._DocReuseTagletProcessorIT.main.a.ClassA.InnerClassA.SubInnerClassA SubInnerClassA}{@code .}{@link org.pdfclown.jada.ext.proc._DocReuseTagletProcessorIT.main.a.ClassA.InnerClassA.SubInnerClassA#size() size()}
       * </p>
       * {@jada.reuseDoc END}
 * <h4>[Name resolution 2] Imported inner type</h4>
 * <p>
 * Referenced via imported {@link SubInnerClassA}
 * </p>
 * {@jada.reuseDoc SubInnerClassA#size(*)}
       * <p>
       * This is from {@link org.pdfclown.jada.ext.proc._DocReuseTagletProcessorIT.main.a.ClassA.InnerClassA.SubInnerClassA SubInnerClassA}{@code .}{@link org.pdfclown.jada.ext.proc._DocReuseTagletProcessorIT.main.a.ClassA.InnerClassA.SubInnerClassA#size() size()}
       * </p>
       * {@jada.reuseDoc END}
 */
public class ClassB extends Main {
}
