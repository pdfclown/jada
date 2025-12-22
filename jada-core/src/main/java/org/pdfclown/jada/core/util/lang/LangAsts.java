/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (LangAsts.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.util.lang;

import static org.pdfclown.common.util.Chars.HASH;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Strings.EMPTY;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

/**
 * Java abstract syntax tree utilities.
 *
 * @author Stefano Chizzolini
 */
public final class LangAsts {
  /**
   * Gets the compilation unit associated to a node.
   */
  public static @Nullable CompilationUnit compilationUnit(@Nullable Node node) {
    while (node != null) {
      if (node instanceof CompilationUnit ret)
        return ret;

      node = node.getParentNode().orElse(null);
    }
    return null;
  }

  /**
   * Evaluates the ancestors of a node with the function until a non-null value is returned.
   *
   * @param <R>
   *          Result type.
   * @param selfInclusive
   *          Whether also {@code node} is evaluated.
   * @param evaluator
   *          Evaluates a node, returning {@code null} until the intended value is found.
   * @return {@code null}, if no value was found.
   */
  public static <R> @Nullable R evalAncestors(@Nullable Node node, boolean selfInclusive,
      Function<Node, @Nullable R> evaluator) {
    while (node != null) {
      if (selfInclusive) {
        var ret = evaluator.apply(node);
        if (ret != null)
          return ret;
      } else {
        selfInclusive = true;
      }

      node = node.getParentNode().orElse(null);
    }
    return null;
  }

  /**
   * Gets the fully-qualified name corresponding to a node.
   */
  public static String fqn(Node node) {
    return fqn(node, true);
  }

  /**
   * Gets the fully-qualified name corresponding to a node.
   *
   * @param signed
   *          Whether member signature is included (otherwise, {@code "(..)"}).
   * @throws IllegalArgumentException
   *           if {@code node} is a {@link CompilationUnit} with multiple top-level types and no
   *           primary one.
   * @implNote If {@code node} is a {@link CompilationUnit} parsed from string, file information is
   *           lost, causing the parser not to recognize its
   *           {@linkplain CompilationUnit#getPrimaryType() primary type}; in such case, the first
   *           element in the {@linkplain CompilationUnit#getTypes() contained top-level types} is
   *           picked as primary; if multiple top-level types are present, an exception is thrown.
   */
  public static String fqn(Node node, boolean signed) {
    var b = new StringBuilder();
    evalAncestors(node, true, $ -> {
      if ($ instanceof CompilationUnit e) {
        b.insert(0, e.getPrimaryType()
            .map($$ -> $$.getFullyQualifiedName().orElseThrow())
            .or(() -> {
              return switch (e.getTypes().size()) {
                case 0 -> e.getPackageDeclaration().map(PackageDeclaration::getNameAsString);
                case 1 -> e.getTypes().get(0).getFullyQualifiedName();
                default -> throw wrongArg("node", null,
                    "Multiple primary types in compilation unit containing {}",
                    e.getTypes().get(0).getFullyQualifiedName().orElse("(N/A)"));
              };
            })
            .orElse(EMPTY));
        return EMPTY;
      } else if ($ instanceof TypeDeclaration<?> e) {
        b.insert(0, e.getFullyQualifiedName().orElseThrow());
        return EMPTY;
      } else if ($ instanceof CallableDeclaration<?> e) {
        if (signed) {
          b.insert(0, e.getSignature());
        } else {
          b.insert(0, "(*)");
          b.insert(0, e.getNameAsString());
        }
        b.insert(0, HASH);
      } else if ($ instanceof NodeWithName<?> e) {
        b.insert(0, e.getNameAsString());
        b.insert(0, HASH);
      }
      return null;
    });
    return b.toString();
  }

  /**
   * Gets the top-level type associated to a node.
   */
  public static @Nullable TypeDeclaration<?> topType(@Nullable Node node) {
    while (node != null) {
      var parent = node.getParentNode().orElse(null);
      if (node instanceof TypeDeclaration<?> ret && !(parent instanceof TypeDeclaration))
        return ret;

      node = parent;
    }
    return null;
  }

  private LangAsts() {
  }
}
