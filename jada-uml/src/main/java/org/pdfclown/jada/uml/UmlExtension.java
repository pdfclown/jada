/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UmlExtension.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml;

import static java.lang.Integer.parseInt;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.HYPHEN;
import static org.pdfclown.common.util.Chars.LF;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Exceptions.missing;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.EOL;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.Strings.lcase;
import static org.pdfclown.common.util.Strings.ucase;
import static org.pdfclown.common.util.system.Clis.parseListIncremental;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__PACKAGE_DEPS;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__CYCLIC_PACKAGE_DEPENDENCIES_CHECKED;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__EMPTY_DIAGRAM_RENDERED;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__EXCLUDED_PACKAGE_DEPENDENCIES;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__EXCLUDED_TYPE_REFERENCES;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__IMAGE_DIR;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__IMAGE_FORMAT;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__PACKAGE_DEPENDENCIES_MAX_COUNT;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__PLANTUML_CUSTOM_DIRECTIVE;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__PLANTUML_SERVER_TIMEOUT;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__PLANTUML_SERVER_URL;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__PROPERTIES_FLATTENED;
import static org.pdfclown.jada.uml.UmlConfig.OPTION__STATIC_FIELDS_MAX_COUNT;
import static org.pdfclown.jada.uml.internal.util.io.Files.FILE_EXTENSION__PLANTUML;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import jdk.javadoc.doclet.Doclet.Option;
import net.sourceforge.plantuml.cli.GlobalConfig;
import net.sourceforge.plantuml.cli.GlobalConfigKey;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.Initializer;
import org.pdfclown.common.util.io.Resource;
import org.pdfclown.jada.core.Jada;
import org.pdfclown.jada.core.JadaConfig.Attachment;
import org.pdfclown.jada.core.JadaExtension;
import org.pdfclown.jada.core.JadaOptions;
import org.pdfclown.jada.core.event.MainProcessEvent;
import org.pdfclown.jada.core.event.PostProcessEvent;
import org.pdfclown.jada.core.proc.JadaFileProcess;
import org.pdfclown.jada.uml.UmlConfig.ExternalLink;
import org.pdfclown.jada.uml.UmlConfig.ImageConfig;
import org.pdfclown.jada.uml.UmlConfig.ImageConfig.Format;
import org.pdfclown.jada.uml.internal.UmlMessage;
import org.pdfclown.jada.uml.proc.PageProcessor;
import org.pdfclown.jada.uml.render.PackageDependency;
import org.pdfclown.jada.uml.render.PackageDependencyCycle;
import org.pdfclown.jada.uml.render.PackageDependencyScanner;
import org.pdfclown.jada.uml.render.UmlFactory;
import org.pdfclown.jada.uml.render.model.DependencyDiagram;
import org.pdfclown.jada.uml.render.model.Diagram;

// SourceName: nl.talsmasoftware.umldoclet.UMLDoclet
// SourceName: nl.talsmasoftware.umldoclet.javadoc.UMLOptions
/**
 * {@link Jada} doclet extension for UML diagrams.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class UmlExtension extends JadaExtension {
  public static final String NAME = "JadaUML";

  @SuppressWarnings("NotNullFieldNotInitialized")
  private UmlConfig extConfig;

  public UmlExtension() {
  }

  @Override
  public UmlConfig getExtConfig() {
    return extConfig;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Initializer
  @Override
  public void init(JadaOptions options, Jada jada) {
    super.init(options, jada);

    extConfig = new UmlConfig(this);

    String imageFormatParameter = Arrays.stream(ImageConfig.Format.values())
        .map(Format::toString)
        .map(String::toLowerCase)
        .collect(joining("|", "(", ")"));
    options
        /*
         * TOOL-DERIVED OPTIONS
         *
         * NOTE: They are purposely hidden, not to interfere with Javadoc tool's synopsis.
         */
        .add("-package", Option.Kind.OTHER, List.of(),
            $args -> extConfig.showMembers("package"))
        .add("-private", Option.Kind.OTHER, List.of(),
            $args -> extConfig.showMembers("private"))
        .add("-protected", Option.Kind.OTHER, List.of(),
            $args -> extConfig.showMembers("protected"))
        .add("-public", Option.Kind.OTHER, List.of(),
            $args -> extConfig.showMembers("public"))
        .add("--show-members", Option.Kind.OTHER, List.of("<visibility>"),
            $args -> extConfig.showMembers($args.get(0)))
        /*
         * STANDARD-DOCLET-DERIVED OPTIONS
         */
        .add("-link", List.of("<url>"),
            $args -> extConfig.getExternalLinks().add(
                new ExternalLink(extConfig, $args.get(0), $args.get(0))))
        .add("-linkoffline", List.of("<url1>", "<url2>"),
            $args -> extConfig.getExternalLinks().add(
                new ExternalLink(extConfig, $args.get(0), $args.get(1))))
        /*
         * EXTENSION-SPECIFIC OPTIONS
         */
        .add(OPTION__CYCLIC_PACKAGE_DEPENDENCIES_CHECKED, List.of(),
            $args -> extConfig.setCyclicPackageDependenciesChecked(true))
        .add(OPTION__EMPTY_DIAGRAM_RENDERED, List.of(),
            $args -> extConfig.setEmptyDiagramRendered(true))
        .add(OPTION__EXCLUDED_PACKAGE_DEPENDENCIES,
            List.of("[+-]?<package-glob>(,<package-glob>)*"),
            $args -> parseListIncremental($args.get(0), identity(),
                extConfig.getExcludedPackageDependencies()))
        .add(OPTION__EXCLUDED_TYPE_REFERENCES, List.of("[+-]?<class>(,<class>)*"),
            $args -> parseListIncremental($args.get(0), identity(),
                extConfig.getExcludedTypeReferences()))
        .add(OPTION__IMAGE_DIR, List.of("<sub-path>"),
            $args -> extConfig.getImageConfig().setSubDirectory($args.get(0)))
        .add(OPTION__IMAGE_FORMAT, options.getText(OPTION__IMAGE_FORMAT, EMPTY,
            Arrays.stream(Format.values())
                .map($ -> (S + LF + HYPHEN + SPACE + "%s (%s)").formatted(
                    lcase($.name()), $.fileExtension))
                .collect(joining())),
            List.of("%s(,%s)*".formatted(imageFormatParameter, imageFormatParameter)),
            $args -> parseListIncremental($args.get(0),
                $ -> $.map($$ -> Format.valueOf(
                    ucase($$.startsWith(S + DOT) ? $$.substring(1) : $$))),
                extConfig.getImageConfig().getFormats()))
        .add(OPTION__PACKAGE_DEPENDENCIES_MAX_COUNT, List.of("<count>"),
            $args -> extConfig.setPackageDependenciesMaxCount(parseInt($args.get(0))))
        .add(OPTION__PLANTUML_CUSTOM_DIRECTIVE, List.of("<directive>"),
            $args -> extConfig.getPlantumlCustomDirectives().add($args.get(0)))
        .add(OPTION__PLANTUML_SERVER_URL, List.of("<url>"),
            $args -> extConfig.setPlantumlServerUrl($args.get(0)))
        .add(OPTION__PLANTUML_SERVER_TIMEOUT, List.of("<seconds>"),
            $args -> GlobalConfig.getInstance().put(GlobalConfigKey.TIMEOUT_MS,
                SECONDS.toMillis(parseInt($args.get(0)))))
        .add(OPTION__PROPERTIES_FLATTENED, List.of(),
            $args -> extConfig.getMethodConfig().propertiesFlattened = true)
        .add(OPTION__STATIC_FIELDS_MAX_COUNT, List.of("<count>"),
            $args -> extConfig.setStaticFieldsMaxCount(parseInt($args.get(0))));
  }

  @Override
  public void onMainProcess(MainProcessEvent event) {
    getExtConfig().onMainProcess();

    if (getExtConfig().getImageConfig().getFormats().contains(Format.SVG)) {
      attachScript("svg-pan-zoom.js");
      attachScript("svg-inject.js");
      attachScript("main.js");
    }
  }

  @Override
  public void onPostProcess(PostProcessEvent event) {
    generateDiagrams().forEach(Diagram::render);

    getConfig().getOperation(JadaFileProcess.class)
        .addProcessor(new PageProcessor(extConfig));
  }

  private void attachScript(String resourceName) {
    getConfig().addScriptAttachment(Attachment.resource(
        Resource.of(getClass().getResource(resourceName))
            .orElseThrow(() -> missing(resourceName, "resource MISSING")),
        NAME));
  }

  private @Nullable Diagram generateDiagram(Element element, UmlFactory factory) {
    if (element instanceof PackageElement e)
      return factory.createPackageDiagram(e);
    else if (element instanceof TypeElement e
        && (element.getKind().isClass() || element.getKind().isInterface()))
      return factory.createClassDiagram(e);
    else
      return null;
  }

  private Stream<Diagram> generateDiagrams() {
    var factory = new UmlFactory(this);
    var ret = getEnv().getIncludedElements().stream()
        .map($ -> generateDiagram($, factory))
        .filter(Objects::nonNull);
    if (extConfig.getPackageDependenciesMaxCount() > 0) {
      ret = Stream.concat(ret, Stream.of(generatePackageDependencyDiagram()));
    }
    return ret;
  }

  private DependencyDiagram generatePackageDependencyDiagram() {
    var scanner = new PackageDependencyScanner(this);
    Set<PackageDependency> packageDependencies = scanner.scan(getEnv().getIncludedElements(), null);

    if (extConfig.isCyclicPackageDependenciesChecked()) {
      // SourceName: detectPackageDependencyCycles(..)
      Set<PackageDependencyCycle> cycles = PackageDependencyCycle.detectCycles(packageDependencies);
      if (!cycles.isEmpty()) {
        getLog().print(Kind.WARNING, this, UmlMessage.PACKAGE_DEPENDENCY_CYCLES, cycles.stream()
            .map($ -> " - " + $)
            .collect(joining(EOL, EOL, EMPTY)));
      }
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    var dependencyDiagram = new DependencyDiagram(extConfig, scanner.getModuleName(),
        FILENAME__PACKAGE_DEPS + FILE_EXTENSION__PLANTUML)
            .setDependencies(packageDependencies);
    return dependencyDiagram;
  }
}
