/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Type.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.render.model;

import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Chars.UNDERSCORE;
import static org.pdfclown.common.util.Strings.lcase;

import java.io.IOException;
import java.util.Collection;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.MonotonicNonNull;
import org.pdfclown.common.util.io.IndentWriter;
import org.pdfclown.jada.uml.UmlConfig.TypeMode;

// SourceName: nl.talsmasoftware.umldoclet.uml.Type
/**
 * Type.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class Type extends UmlNode {
  /**
   * Classification of a UML Type.
   *
   * @author Sjoerd Talsma (original implementation)
   * @author Stefano Chizzolini (adaptation and redesign for Jada)
   */
  public enum Classification {
    ENUM,
    INTERFACE,
    ANNOTATION,
    ABSTRACT_CLASS,
    CLASS;

    public String toUml() {
      return lcase(name()).replace(UNDERSCORE, SPACE);
    }
  }

  private final Classification classfication;
  private TypeName name;
  // SourceName: packageNamespace
  private final Namespace namespace;
  // SourceName: isDeprecated
  private boolean deprecated;
  // SourceName: includePackagename
  private boolean packageNameInclusive;
  private @MonotonicNonNull @Nullable Link link;

  @SuppressWarnings("this-escape")
  public Type(Namespace namespace, Classification classification, TypeName name) {
    this(namespace, classification, name, false, false, null);
  }

  @SuppressWarnings("this-escape")
  private Type(Namespace namespace, Classification classification, TypeName name,
      boolean deprecated, boolean packageNameInclusive,
      @Nullable Collection<? extends UmlNode> children) {
    super(namespace);

    this.namespace = requireNonNull(namespace, "`namespace`");
    this.classfication = requireNonNull(classification, "`classification`");
    this.name = requireNonNull(name, "`name`");
    this.deprecated = deprecated;
    this.packageNameInclusive = packageNameInclusive;
    if (children != null) {
      children.forEach(this::addChild);
    }
  }

  /*
   * SourceName: deprecated -- I'm not a fan of fuzzy fluent naming (IMO, adjectives should NEVER
   * stay alone as interface members because of their inherent ambiguity: most of the time, their
   * no-parameter signature implies a reading accessor (getter), whilst in this case it is actually
   * the opposite!).
   */
  public Type deprecate() {
    deprecated = true;
    return this;
  }

  /**
   * @implNote Marked as final to enforce equivalence symmetry.
   */
  @Override
  public final boolean equals(@Nullable Object o) {
    return this == o || (o instanceof Type that
        && this.name.equals(that.name));
  }

  public Classification getClassfication() {
    return classfication;
  }

  // SourceName: getModulename
  /**
   * Module name.
   *
   * @return Empty, if unnamed module.
   */
  public String getModuleName() {
    return namespace.getModuleName();
  }

  /**
   * Type name.
   */
  public TypeName getName() {
    return name;
  }

  // SourceName: getPackagename
  /**
   * Package name.
   *
   * @return Empty, if default package.
   */
  public String getPackageName() {
    return namespace.getName();
  }

  /**
   * @implNote Marked as final to enforce equivalence symmetry.
   */
  @Override
  public final int hashCode() {
    return name.hashCode();
  }

  // SourceName: setIncludePackagename
  public void setIncludePackageName(boolean value) {
    packageNameInclusive = value;
  }

  /**
   * Updates generic type variables.
   */
  public void updateGenericTypeVariables(@Nullable TypeName name) {
    if (name != null && name.getQualifiedName().equals(this.name.getQualifiedName())) {
      final TypeName[] generics = this.name.getGenerics();
      this.name = name;
      if (generics.length == name.getGenerics().length) {
        getChildren().stream()
            .filter(TypeMember.class::isInstance).map(TypeMember.class::cast)
            .forEach($member -> {
              for (int i = 0; i < generics.length; i++) {
                $member.replaceParameterizedType(generics[i], name.getGenerics()[i]);
              }
            });
      }
    }
  }

  @Override
  public IndentWriter writeChildrenTo(IndentWriter out) throws IOException {
    if (!getChildren().isEmpty() && !Classification.ANNOTATION.equals(classfication)) {
      out.append('{').nl();
      super.writeChildrenTo(out.withIndent());
      out.append('}');
    }
    return out;
  }

  @Override
  public IndentWriter writeTo(IndentWriter out) throws IOException {
    out.append(classfication.toUml()).space();
    writeNameTo(out).space();
    if (deprecated) {
      out.append("<<deprecated>>").space();
    }
    link().writeTo(out).space();
    writeChildrenTo(out);
    out.nl();
    return out;
  }

  /*
   * TODO: this method sounds redundant (name should always be qualified by its package, and its
   * local name should always correspond to simpleName): verify why upstream implementation needed
   * it.
   */
  String getPackageLocalName() {
    return isPackageLocalNamed()
        ? name.getQualifiedName().substring(namespace.getName().length() + 1)
        : name.getSimpleName();
  }

  boolean isPackageLocalNamed() {
    return name.getQualifiedName().startsWith(namespace.getName() + DOT);
  }

  private Link link() {
    if (link == null) {
      link = Link.forType(this);
    }
    return link;
  }

  private IndentWriter writeNameTo(IndentWriter out) throws IOException {
    if (packageNameInclusive && isPackageLocalNamed()) {
      out.append("\"<size:14>").append(getPackageLocalName())
          .append("\\n<size:10>").append(namespace.getName())
          .append("\" as ");
    }

    /*
     * Namespace aware compensation
     *
     * TODO: Simplify this package logic and make sure all is still needed!
     */
    Namespace namespace = findParent(Namespace.class).orElse(null);
    out.append(name.toUml(TypeMode.QUALIFIED, namespace));
    return out;
  }
}
