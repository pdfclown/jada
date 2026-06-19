/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (LangModels.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.util.lang;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Strings.EMPTY;

import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.util.DocTreeScanner;
import java.util.function.Function;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.jspecify.annotations.Nullable;

/**
 * Java language model utilities.
 *
 * @author Stefano Chizzolini
 */
public final class LangModels {
  /**
   * Fully-qualified name.
   *
   * @author Stefano Chizzolini
   */
  public static final class FQName {
    public final @Nullable String localName;
    public final @Nullable String memberName;
    public final @Nullable String moduleName;
    public final @Nullable String packageName;
    public final @Nullable String signature;

    /**
     */
    public FQName(@Nullable String moduleName, @Nullable String packageName,
        @Nullable String localName, @Nullable String memberName, @Nullable String signature) {
      this.moduleName = moduleName;
      this.packageName = packageName;
      this.localName = localName;
      this.memberName = memberName;
      this.signature = signature;
      if (this.signature != null) {
        requireNonNull(this.memberName, "`memberName`");
      }
    }

    @Override
    public String toString() {
      var b = new StringBuilder();
      if (packageName != null) {
        b.append(packageName);
      }
      if (localName != null) {
        if (!b.isEmpty()) {
          b.append(DOT);
        }
        b.append(localName);
      }
      if (memberName != null) {
        b.append(DOT).append(memberName);
      }
      if (signature != null) {
        b.append(signature);
      }
      return b.toString();
    }
  }

  /**
   * Unnamed module name.
   */
  public static final String MODULE__UNNAMED = EMPTY;
  /**
   * Default package name.
   */
  public static final String PACKAGE__DEFAULT = EMPTY;
  /**
   * {@linkplain #PACKAGE__DEFAULT Default package} alias.
   */
  public static final String PACKAGE__UNNAMED = PACKAGE__DEFAULT;

  /**
   * Gets the type the element belongs to.
   *
   * @return {@code element}, if {@code element} is a type itself; otherwise, its enclosing type.
   */
  public static @Nullable TypeElement currentType(@Nullable Element element) {
    while (element != null) {
      if (element instanceof TypeElement ret)
        return ret;

      element = element.getEnclosingElement();
    }
    return null;
  }

  /**
   * Evaluates the ancestors of the element with the function until a non-null value is returned.
   *
   * @param <R>
   *          Result type.
   * @param selfInclusive
   *          Whether also {@code element} is evaluated.
   * @return {@code null}, if no value was returned.
   */
  public static <R> @Nullable R evalAncestors(@Nullable Element element, boolean selfInclusive,
      Function<Element, @Nullable R> evaluator) {
    while (element != null) {
      if (selfInclusive) {
        var ret = evaluator.apply(element);
        if (ret != null)
          return ret;
      } else {
        selfInclusive = true;
      }

      element = element.getEnclosingElement();
    }
    return null;
  }

  /**
   * Gets the fully-qualified name corresponding to the element.
   */
  public static FQName fqName(@Nullable Element element) {
    String moduleName = null;
    String packageName = null;
    String localName = null;
    String memberName = null;
    String signature = null;
    {
      TypeElement currentType = null;
      while (element != null) {
        if (element instanceof TypeElement e) {
          if (currentType == null) {
            currentType = e;
          }
        } else if (element instanceof PackageElement) {
          packageName = element.toString();
          if (currentType != null) {
            localName = currentType.toString().substring(packageName.length() + 1);
          }
        } else if (element instanceof ModuleElement e) {
          if (!e.isUnnamed()) {
            moduleName = e.toString();
          }
        } else if (element instanceof ExecutableElement e) {
          memberName = e.getSimpleName().toString();
          signature = e.toString().substring(memberName.length());
        }
        element = element.getEnclosingElement();
      }
    }
    return new FQName(moduleName, packageName, localName, memberName, signature);
  }

  /**
   * Gets the fully-qualified name corresponding to the element.
   */
  public static String fqn(Element element) {
    return fqn(element, true);
  }

  /**
   * Gets the fully-qualified name corresponding to the element.
   *
   * @param signed
   *          Whether member signature is included (otherwise, {@code "(..)"}).
   */
  public static String fqn(Element element, boolean signed) {
    var b = new StringBuilder();
    evalAncestors(element, true, $ -> {
      switch ($.getKind()) {
        case CLASS:
        case INTERFACE:
          b.insert(0, $);
          return EMPTY;
        case CONSTRUCTOR:
        case METHOD:
          if (signed) {
            b.insert(0, $);
          } else {
            b.insert(0, "(*)");
            b.insert(0, $.getSimpleName());
          }
          b.insert(0, DOT);
          return null;
        default:
          b.insert(0, $);
          return null;
      }
    });
    return b.toString();
  }

  /**
   * Gets the text content of the node.
   */
  public static String text(DocTree node) {
    var b = new StringBuilder();
    node.accept(new DocTreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitText(TextTree node, Void p) {
        b.append(node.getBody());
        return null;
      }
    }, null);
    return b.toString();
  }

  /**
   * Gets the top-level type element associated to the given one.
   */
  public static @Nullable TypeElement topType(@Nullable Element element) {
    while (element != null) {
      var parent = element.getEnclosingElement();
      if (element instanceof TypeElement ret && !(parent instanceof TypeElement))
        return ret;

      element = parent;
    }
    return null;
  }

  private LangModels() {
  }
}
