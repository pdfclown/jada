/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ClassA.java) is part of jada-ext module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.ext.proc._DocReuseTagletProcessorIT.main.a;

import org.pdfclown.jada.ext.proc._DocReuseTagletProcessorIT.main.Main;

/**
 * <h4>[Name resolution 4] Module-level type</h4>
 * {@jada.reuseDoc org.pdfclown.jada.ext.proc._DocReuseTagletProcessorIT.main.Main}
 * <h4>[Name resolution 1.1] Child member</h4> {@jada.reuseDoc #methodA(*)}
 */
public class ClassA extends Main {
  /**
   * <h4>[Inner type fragment]</h4> {@jada.doc}
   * <p>
   * This is from {@link END}
   * </p>
   * {@jada.doc END}
   */
  public class END {
  }

  /**
   * <h4>[Inner type fragment, without closing tag]</h4> {@jada.doc}
   * <p>
   * This is from {@link InnerClassA}
   * </p>
   */
  public class InnerClassA {
    public class SubInnerClassA {
      /**
       * <h4>[Callable member fragment]</h4> {@jada.doc}
       * <p>
       * This is from {@link SubInnerClassA}{@code .}{@link #size()}
       * </p>
       * {@jada.doc END}
       */
      int size() {
        return 0;
      }
    }

    String getValue() {
      return "";
    }
  }

  /**
   * <h4>[Field member fragment]</h4> {@jada.doc}
   * <p>
   * This is from END field.
   * </p>
   * {@jada.doc END}
   */
  public static final String END = "Some random content";

  /**
   * <h4>[Name resolution 2] Import</h4> {@jada.reuseDoc Main}
   * <h4>[Callable member fragment]</h4> {@jada.doc}
   * <p>
   * This is from methodA()
   * </p>
   * {@jada.doc END}
   * <h4>[Name resolution 1.2] Sibling inner type</h4>
   * {@jada.reuseDoc InnerClassA.SubInnerClassA#size(*)}
   */
  public void methodA() {
  }

  /**
   * <h4>[Name resolution 3] Package-level type</h4> {@jada.reuseDoc LocalA#run(*)}
   * <h4>[Name resolution 1.2] Sibling member (inner class)</h4> {@jada.reuseDoc ClassA.END}
   * <h4>[Name resolution 1.2] Sibling member (field)</h4> {@jada.reuseDoc #END}
   * <h4>[Name resolution 1.2] Sibling member (method)</h4> {@jada.reuseDoc #methodA(*)}
   */
  public void methodB() {
  }

  /**
   * Method overload without parameters.
   * <h4>[Name resolution 0] Local callable member overload</h4> {@jada.reuseDoc }
   */
  public void overload() {
  }

  /**
   * Method overload with 1 parameter.
   * <h4>[Name resolution 0] Local callable member overload</h4> {@jada.reuseDoc :extra}
   * <h4>[Name resolution 0] Local callable member overload</h4> {@jada.reuseDoc }
   *
   * @param s
   *          String value.
   */
  public void overload(String s) {
  }

  /**
   * Method overload with 2 parameters.
   * <h4>[Callable member fragment]</h4> {@jada.doc }
   * <p>
   * This is from overload(String, int)
   * </p>
   * {@jada.doc END}
   * <h4>[Callable member fragment]</h4> {@jada.doc extra}
   * <p>
   * This is an extra fragment from overload(String, int)
   * </p>
   * {@jada.doc END}
   *
   * @param s
   *          String value.
   * @param i
   *          Index.
   */
  public void overload(String s, int i) {
  }
}
