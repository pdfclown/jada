/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PackageDependencyScanner.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.render;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner9;
import javax.tools.Diagnostic.Kind;
import org.jspecify.annotations.Nullable;
import org.pdfclown.jada.uml.UmlExtension;
import org.pdfclown.jada.uml.internal.UmlMessage;

// SourceName: nl.talsmasoftware.umldoclet.javadoc.dependencies.DependenciesElementScanner
/**
 * Javadoc ElementScanner to detect dependencies.
 * <p>
 * Dependency packages are remembered and the result of the scan is a (mutable) set of
 * {@link PackageDependency} objects.
 * </p>
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class PackageDependencyScanner
    extends ElementScanner9<Set<PackageDependency>, @Nullable String> {
  private final UmlExtension extension;
  private @Nullable String moduleName;

  public PackageDependencyScanner(UmlExtension extension) {
    super(new LinkedHashSet<>());

    this.extension = requireNonNull(extension, "`extension`");
  }

  public @Nullable String getModuleName() {
    return moduleName;
  }

  /**
   * Visits an executable element (method, constructor or initializer) to add its package dependency
   * to the current set.
   * <p>
   * First, the package of the <em>return type</em> of the executable is added as a package
   * dependency. Then, the packages of all thrown exception types are added as package dependencies.
   * Finally, all child elements of the executable (for example, parameters) are visited for package
   * dependencies.
   * </p>
   *
   * @param visitedExecutable
   *          Visited executable.
   * @param fromPackage
   *          Current package.
   * @return Package dependencies found after scanning the visited executable.
   */
  @Override
  public Set<PackageDependency> visitExecutable(ExecutableElement visitedExecutable,
      @Nullable String fromPackage) {
    addDependency(fromPackage, visitedExecutable.getReturnType());
    visitedExecutable.getThrownTypes().forEach($ -> addDependency(fromPackage, $));
    return super.visitExecutable(visitedExecutable, fromPackage) /* Adds argument dependencies */;
  }

  @Override
  public Set<PackageDependency> visitModule(ModuleElement visitedModule,
      @Nullable String fromPackage) {
    moduleName = visitedModule.getQualifiedName().toString();
    return super.visitModule(visitedModule, fromPackage);
  }

  /**
   * Visits a package to evaluate all dependencies from its elements to other packages.
   * <p>
   * All elements within the package are visited, with the new {@code fromPackage} set to the
   * qualified name of the visited package.
   * </p>
   *
   * @param visitedPackage
   *          Visited package.
   * @param fromPackage
   *          'From' package (possibly from parent elements). Ignored in this method, as the scan
   *          will continue from the visited package.
   * @return Package dependencies found after scanning {@code visitedPackage}.
   */
  @Override
  public Set<PackageDependency> visitPackage(PackageElement visitedPackage,
      @Nullable String fromPackage) {
    boolean included = extension.getEnv().isIncluded(visitedPackage);
    String packageName = visitedPackage.getQualifiedName().toString();
    if (!included) {
      extension.getLog().print(Kind.OTHER, this, UmlMessage.PACKAGE_VISITED_BUT_UNDOCUMENTED,
          packageName);
      return DEFAULT_VALUE;
    }

    return super.visitPackage(visitedPackage, packageName);
  }

  /**
   * Visits a type element to add their package dependencies to the current set.
   * <p>
   * First, the package of the superclass is added as a dependency. Then, the package of each
   * implemented interface is added as a dependency. Finally, all contained elements within the type
   * are visited for package dependencies.
   * </p>
   * <p>
   * NOTE: At the moment, there is no metadata available in Javadoc listing the <em>imports</em> of
   * a type. So, unfortunately, the imports of a type are currently not included in the package
   * dependencies.
   * </p>
   *
   * @param visitedType
   *          Visited type.
   * @param fromPackage
   *          Current package (optional, will be resolved from the visited type if null).
   * @return Package dependencies found after scanning {@code visitedType}.
   */
  @Override
  public Set<PackageDependency> visitType(TypeElement visitedType, @Nullable String fromPackage) {
    String pkg = fromPackage == null && extension.getEnv().isIncluded(visitedType)
        ? PackageElementVisitor.INSTANCE.visit(visitedType)
        : fromPackage;
    addDependency(pkg, visitedType.getSuperclass());
    visitedType.getInterfaces().forEach($ -> addDependency(pkg, $));
    // TODO: figure out if there is a way to add the class' imports dependencies!
    return super.visitType(visitedType, pkg);
  }

  /**
   * Visits a type parameter element to add its package dependency to the current set.
   * <p>
   * First, the package of the <em>generic type</em> is added as a package dependency. Then, the
   * packages of all declared bounds are added as package dependencies.
   * </p>
   *
   * @param visitedTypeParameter
   *          Visited parameter element.
   * @param fromPackage
   *          Current package.
   * @return Package dependencies found after scanning the visited executable.
   */
  @Override
  public Set<PackageDependency> visitTypeParameter(TypeParameterElement visitedTypeParameter,
      @Nullable String fromPackage) {
    addDependency(fromPackage, visitedTypeParameter.getGenericElement());
    visitedTypeParameter.getBounds().forEach($ -> addDependency(fromPackage, $));
    return super.visitTypeParameter(visitedTypeParameter, fromPackage);
  }

  /**
   * Overrides visiting any <em>unknown</em> element.
   * <p>
   * The default visitor throws exception on unknown elements, this visitor just returns the current
   * package dependencies (without adding any).
   * </p>
   *
   * @param visitedUnknown
   *          The visited unknown element.
   * @param fromPackage
   *          The current package (ignored, as unknown elements are not processed any further).
   * @return The found package dependencies before the unknown element, without adding any.
   */
  @Override
  public Set<PackageDependency> visitUnknown(Element visitedUnknown, @Nullable String fromPackage) {
    return DEFAULT_VALUE;
  }

  /**
   * Visits a variable element (field, constant or method parameter) to add its package dependency
   * to the current set.
   * <p>
   * The package of the <em>type</em> of the variable is added as a package dependency.
   * </p>
   *
   * @param visitedVariable
   *          Visited variable.
   * @param fromPackage
   *          Current package.
   * @return Package dependencies found after scanning {@code visitedVariable}.
   */
  @Override
  public Set<PackageDependency> visitVariable(VariableElement visitedVariable,
      @Nullable String fromPackage) {
    addDependency(fromPackage, visitedVariable.asType());
    return super.visitVariable(visitedVariable, fromPackage);
  }

  private void addDependency(@Nullable String fromPackage, Element toElement) {
    String toPackage = PackageElementVisitor.INSTANCE.visit(toElement);
    addDependency(fromPackage, toPackage);
  }

  private void addDependency(@Nullable String fromPackage, @Nullable String toPackage) {
    if (fromPackage != null && toPackage != null && !fromPackage.equals(toPackage)) {
      DEFAULT_VALUE.add(new PackageDependency(fromPackage, toPackage));
    }
  }

  private void addDependency(@Nullable String fromPackage, TypeMirror toType) {
    String toPackage = PackageTypeVisitor.INSTANCE.visit(toType);
    addDependency(fromPackage, toPackage);
  }
}
