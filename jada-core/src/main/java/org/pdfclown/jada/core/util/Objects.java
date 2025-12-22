/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Objects.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.util;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

/**
 * Object utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Objects {
  private enum TypesSingleton {
    INSTANCE;

    final ScanResult value = new ClassGraph()
        .enableClassInfo()
        .addClassLoader(Objects.class.getClassLoader())
        .scan();
  }

  /**
   * Gets concrete type descendants in Jada doclet classpath.
   * <p>
   * Useful to discover implementations of Jada-related types.
   * </p>
   *
   * @param type
   *          Type (either class or interface) whose descendants are searched.
   * @implNote For the sake of efficiency, system types are excluded from the classpath search.
   */
  public static <T> Stream<Class<? extends T>> realSubTypes(Class<T> type) {
    //noinspection Convert2MethodRef
    return org.pdfclown.common.util.Objects.subTypes(type, types())
        .filter($ -> !Modifier.isAbstract($.getModifiers()))
        .map($ -> org.pdfclown.common.util.Objects.xcast($) /*
                                                             * WARN: DO NOT convert to
                                                             * `Objects::xcast`
                                                             */);
  }

  /**
   * Types available in Jada doclet classpath.
   *
   * @implNote For the sake of efficiency, system types are excluded from the classpath search.
   */
  public static ScanResult types() {
    return TypesSingleton.INSTANCE.value;
  }

  private Objects() {
  }
}
