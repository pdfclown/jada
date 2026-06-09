/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (ClassA.java) is part of jada-ext module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.ext.proc.DocReuseTagletProcessorIT_.main.a;

import org.pdfclown.jada.ext.proc.DocReuseTagletProcessorIT_.main.Main;

/**
 * <h4>[Name resolution 4] Module-level type</h4>
 * {@jada.reuseDoc org.pdfclown.jada.ext.proc.DocReuseTagletProcessorIT_.main.Main}
 * <p>
 * This is from Main
 * </p>
 * {@jada.reuseDoc END}
 * <h4>[Name resolution 1.1] Child member</h4> {@jada.reuseDoc #methodA(*)}
   * <p>
   * This is from methodA()
   * </p>
   * {@jada.reuseDoc END}
 */
public class ClassA extends Main {
  /**
   * <h4>[Inner type fragment]</h4> {@jada.doc}
   * <p>
   * This is from {@link END}
   * </p>
   * {@jada.doc END}
   */
  public static class END {
  }

  /**
   * <h4>[Inner type fragment, without closing tag]</h4> {@jada.doc}
   * <p>
   * This is from {@link InnerClassA}
   * </p>
   {@jada.doc END}*/
  public static class InnerClassA {
    public static class SubInnerClassA {
      /**
       * <h4>[Callable member fragment]</h4> {@jada.doc}
       * <p>
       * This is from {@link SubInnerClassA}{@code .}{@link #size()}
       * </p>
       * {@jada.doc END}
       */
      public int size() {
        return 0;
      }
    }

    public String getValue() {
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
 * <p>
 * This is from Main
 * </p>
 * {@jada.reuseDoc END}
   * <h4>[Callable member fragment]</h4> {@jada.doc}
   * <p>
   * This is from methodA()
   * </p>
   * {@jada.doc END}
   * <h4>[Name resolution 1.2] Sibling inner type</h4>
   * {@jada.reuseDoc InnerClassA.SubInnerClassA#size(*)}
       * <p>
       * This is from {@link org.pdfclown.jada.ext.proc.DocReuseTagletProcessorIT_.main.a.ClassA.InnerClassA.SubInnerClassA SubInnerClassA}{@code .}{@link org.pdfclown.jada.ext.proc.DocReuseTagletProcessorIT_.main.a.ClassA.InnerClassA.SubInnerClassA#size() size()}
       * </p>
       * {@jada.reuseDoc END}
   */
  public void methodA() {
  }

  /**
   * <h4>[Name resolution 3] Package-level type</h4> {@jada.reuseDoc LocalA#run(*)}
   * <p>
   * This is from LocalA.run()
   * </p>
   * {@jada.reuseDoc END}
   * <h4>[Name resolution 1.2] Sibling member (inner class)</h4> {@jada.reuseDoc ClassA.END}
   * <p>
   * This is from {@link org.pdfclown.jada.ext.proc.DocReuseTagletProcessorIT_.main.a.ClassA.END END}
   * </p>
   * {@jada.reuseDoc END}
   * <h4>[Name resolution 1.2] Sibling member (field)</h4> {@jada.reuseDoc #END}
   * <p>
   * This is from END field.
   * </p>
   * {@jada.reuseDoc END}
   * <h4>[Name resolution 1.2] Sibling member (method)</h4> {@jada.reuseDoc #methodA(*)}
   * <p>
   * This is from methodA()
   * </p>
   * {@jada.reuseDoc END}
   */
  public void methodB() {
  }

  /**
   * Method overload without parameters.
   * <h4>[Name resolution 0] Local callable member overload</h4> {@jada.reuseDoc }
   * <p>
   * This is from overload(String, int)
   * </p>
   * {@jada.reuseDoc END}
   */
  public void overload() {
  }

  /**
   * Method overload with 1 parameter.
   * <h4>[Name resolution 0] Local callable member overload</h4> {@jada.reuseDoc :extra}
   * <p>
   * This is an extra fragment from overload(String, int)
   * </p>
   * {@jada.reuseDoc END}
   * <h4>[Name resolution 0] Local callable member overload</h4> {@jada.reuseDoc }
   * <p>
   * This is from overload(String, int)
   * </p>
   * {@jada.reuseDoc END}
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
