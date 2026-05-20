/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (DependencyDiagram.java) is part of jada-uml module in Jada project
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
import static java.util.stream.Collectors.joining;
import static org.pdfclown.common.util.Chars.PIPE;
import static org.pdfclown.common.util.Conditions.requireType;
import static org.pdfclown.common.util.Exceptions.wrongState;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.jada.uml.util.Plantumls.PUML_REF__ASSOCIATES;
import static org.pdfclown.jada.uml.util.Plantumls.normalNs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.function.Failable;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.LazyNonNull;
import org.pdfclown.common.util.io.IndentWriter;
import org.pdfclown.common.util.regex.Patterns;
import org.pdfclown.jada.uml.UmlConfig;
import org.pdfclown.jada.uml.render.PackageDependency;

// SourceName: nl.talsmasoftware.umldoclet.uml.DependencyDiagram
/**
 * UML diagram representing the dependencies between the documented Java packages.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class DependencyDiagram extends Diagram {
  private static final String BACKGROUNDCOLOR_DIRECTIVE = "skinparam backgroundcolor";
  private static final String DEFAULT_BACKGROUNDCOLOR = "transparent";

  private @LazyNonNull @Nullable Pattern excludedPackageDependenciesPattern;
  private final @Nullable String moduleName;
  private @LazyNonNull @Nullable Path pumlFile;
  private final String pumlFileName;

  /**
   */
  public DependencyDiagram(UmlConfig config, @Nullable String moduleName, String pumlFileName) {
    super(config);

    this.moduleName = moduleName;
    this.pumlFileName = requireNonNull(pumlFileName, "`pumlFileName`");
  }

  /**
   * @throws IllegalArgumentException
   *           if {@code child} is not among the allowed node types ({@link Reference}).
   */
  @Override
  public void addChild(UmlNode child) {
    super.addChild(requireNonNull(requireType(child, Reference.class)));
  }

  /**
   * Sets the dependencies associated to this diagram.
   * <p>
   * The dependencies are filtered against {@linkplain UmlConfig#getExcludedPackageDependencies()
   * exclusions} and limited against {@linkplain UmlConfig#getPackageDependenciesMaxCount() max
   * count}.
   * </p>
   *
   * @throws IllegalStateException
   *           if dependencies have been already set for this diagram.
   */
  public DependencyDiagram setDependencies(Set<PackageDependency> value) {
    if (!getChildren().isEmpty())
      throw wrongState("Dependencies already set");

    // Filter non-excluded dependencies!
    Set<Reference> references = value.stream()
        .map($ -> new Reference(
            Reference.from(normalNs($.fromPackage), null),
            PUML_REF__ASSOCIATES,
            Reference.to(normalNs($.toPackage), null)))
        .filter($ -> !isPackageExcluded($.to.getQualifiedName()))
        .collect(Collectors.toSet());

    // Limit the number of dependencies!
    int maxCount = getConfig().getPackageDependenciesMaxCount();
    if (references.size() > maxCount) {
      /*
       * NOTE: Packages are inversely ordered by number of occurrences as reference targets, then
       * the `maxCount` most relevant packages are used to filter the references.
       */
      var packageUses = new TreeMap<String, MutableInt>();
      references.forEach($ -> packageUses.computeIfAbsent($.to.getQualifiedName(),
          $k -> new MutableInt()).increment());

      Set<String> relevantPackages = packageUses.entrySet().stream()
          .sorted(($1, $2) -> -$1.getValue().compareTo($2.getValue()))
          .limit(maxCount)
          .map(Map.Entry::getKey)
          .collect(Collectors.toSet());

      references = references.stream()
          .filter($ -> relevantPackages.contains($.from.getQualifiedName())
              && relevantPackages.contains($.to.getQualifiedName()))
          .collect(Collectors.toSet());
    }

    // Add relevant dependencies to diagram!
    references.forEach(this::addChild);
    return this;
  }

  @Override
  protected Path getPlantUmlFile() {
    if (pumlFile == null) {
      var b = new StringBuilder(getConfig().getConfig().getOutputDirectory().toString());
      if (b.length() > 0 && b.charAt(b.length() - 1) != '/') {
        b.append('/');
      }
      if (moduleName != null) {
        b.append(moduleName).append('/');
      }
      b.append(pumlFileName);
      pumlFile = Path.of(b.toString());
    }
    return pumlFile;
  }

  @Override
  protected IndentWriter writeChildrenTo(IndentWriter out) throws IOException {
    out.append("set namespaceSeparator none").nl()
        .append("hide circle").nl()
        .append("hide empty fields").nl()
        .append("hide empty methods").nl().nl();

    super.writeChildrenTo(out);

    writePackageLinksTo(out.nl());
    return out;
  }

  @Override
  protected IndentWriter writeCustomDirectives(List<String> customDirectives, IndentWriter out)
      throws IOException {
    boolean backgroundcolorAlreadySet = false;
    for (var customDirective : customDirectives) {
      backgroundcolorAlreadySet |= customDirective.contains(BACKGROUNDCOLOR_DIRECTIVE);
      out.writeln(customDirective);
    }
    if (!backgroundcolorAlreadySet) {
      out.append(BACKGROUNDCOLOR_DIRECTIVE).space().append(DEFAULT_BACKGROUNDCOLOR).nl();
    }
    return out;
  }

  // SourceName: isExcludedPackage
  private boolean isPackageExcluded(String packageName) {
    if (excludedPackageDependenciesPattern == null) {
      excludedPackageDependenciesPattern = Pattern.compile(
          getConfig().getExcludedPackageDependencies().stream()
              .map(Patterns::globToRegex)
              .collect(joining(S + PIPE)));
    }
    return excludedPackageDependenciesPattern.matcher(packageName).matches();
  }

  private IndentWriter writePackageLinksTo(IndentWriter out) throws IOException {
    out.writeln("' Package links");
    getChildren(Reference.class).stream()
        .flatMap($ -> Stream.of($.from.toString(), $.to.toString()))
        .distinct().map($packageName -> new Namespace(this, $packageName, null))
        .forEach(Failable.asConsumer($namespace -> writePackageLinkTo(out, $namespace)));
    return out;
  }

  private IndentWriter writePackageLinkTo(IndentWriter out, Namespace namespace)
      throws IOException {
    String link = Link.forPackage(namespace).toString().trim();
    if (!link.isEmpty()) {
      out.append("class \"").append(namespace.getName()).append("\" ").append(link)
          .append(" {").nl().append('}').nl();
    }
    return out;
  }
}
