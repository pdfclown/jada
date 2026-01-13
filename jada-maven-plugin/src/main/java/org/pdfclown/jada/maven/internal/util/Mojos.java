/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Mojos.java) is part of jada-maven-plugin module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.maven.internal.util;

import java.util.Collection;
import java.util.function.Consumer;
import org.pdfclown.common.util.system.Systems;

/**
 * Mojo utilities.
 *
 * @author Stefano Chizzolini
 */
public final class Mojos {
  /**
   * Gets a collection of enum values converted from a raw source collection injected to a Mojo
   * parameter.
   * <p>
   * This method is intended for hybrid parameters supporting both explicit (for example,
   * {@code -DmyParameter=MY_ENUM_CONST_1,MY_ENUM_CONST_X}) and implicit (for example,
   * {@code -DmyParameter}) selection of enum constants.
   * </p>
   *
   * @param enumType
   *          Target enumeration type to convert source values to.
   * @param source
   *          Raw values from Mojo parameter injection.
   * @param target
   *          Collection to fill with enum values converted from {@code source}.
   * @param defaultValueHandler
   *          Handles the case of implicit parameter value (that is, parameter specified without
   *          value).
   * @return {@code target}
   * @implNote Implicit selection is detected when the first source element corresponds to
   *           {@code "true"} or empty string.
   */
  public static <E extends Enum<E>, C extends Collection<E>> C parseParameterEnumValues(
      Class<E> enumType, Collection<String> source, C target, Consumer<C> defaultValueHandler) {
    if (source.size() == 1 && Systems.parsePropertyBoolean(source.iterator().next())) {
      defaultValueHandler.accept(target);
    } else {
      for (var e : source) {
        target.add(Enum.valueOf(enumType, e));
      }
    }
    return target;
  }

  private Mojos() {
  }
}
