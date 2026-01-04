/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (UmlConfig.java) is part of jada-uml module in Jada project
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.exists;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Chars.SLASH;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Objects.nonNull;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__HTML;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__PNG;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__SVG;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__ELEMENT_LIST;
import static org.pdfclown.jada.core.util.lang.Javadocs.FILENAME__PACKAGE_LIST;
import static org.pdfclown.jada.uml.internal.util.io.Files.openReaderTo;
import static org.pdfclown.jada.uml.internal.util.net.Uris.addHttpParam;
import static org.pdfclown.jada.uml.internal.util.net.Uris.addPathComponent;

import java.io.BufferedReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.tools.Diagnostic.Kind;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.LazyNonNull;
import org.pdfclown.common.util.annot.UnmodifiableView;
import org.pdfclown.jada.core.JadaExtConfig;
import org.pdfclown.jada.uml.internal.UmlMessage;

// SourceName: nl.talsmasoftware.umldoclet.configuration.Configuration
// SourceName: nl.talsmasoftware.umldoclet.javadoc.DocletConfig
/**
 * {@link UmlExtension} configuration.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public class UmlConfig extends JadaExtConfig {
  // SourceName: nl.talsmasoftware.umldoclet.configuration.FieldConfig
  // SourceName: nl.talsmasoftware.umldoclet.javadoc.DocletConfig.FieldCfg
  /**
   * Configuration for field rendering in UML diagrams.
   *
   * @author Sjoerd Talsma (original implementation)
   * @author Stefano Chizzolini (adaptation and redesign for Jada)
   */
  public static class FieldConfig {
    // SourceName: typeDisplay
    TypeMode typeMode = TypeMode.SIMPLE;
    Set<Visibility> visibilities = EnumSet.of(Visibility.PROTECTED, Visibility.PUBLIC);

    // SourceName: typeDisplay
    /**
     * How field types are rendered in UML diagrams.
     */
    public TypeMode getTypeMode() {
      return typeMode;
    }

    // SourceName: include
    /**
     * Gets whether to include fields with the given visibility in UML diagrams.
     */
    public boolean includes(Visibility visibility) {
      return visibilities.contains(visibility);
    }
  }

  // SourceName: nl.talsmasoftware.umldoclet.configuration.ImageConfig
  // SourceName: nl.talsmasoftware.umldoclet.javadoc.DocletConfig.ImageCfg
  /**
   * Configuration for generated UML diagram images.
   *
   * @author Sjoerd Talsma (original implementation)
   * @author Stefano Chizzolini (adaptation and redesign for Jada)
   */
  public static class ImageConfig {
    /**
     * Image format for UML diagrams rendering.
     */
    public enum Format {
      /**
       * <a href="https://wikipedia.org/wiki/Scalable_Vector_Graphics">Scalable Vector Graphics</a>
       * (SVG).
       * <p>
       * Default and recommended format, because of its advantages over other formats:
       * </p>
       * <ul>
       * <li>high-quality image scaling</li>
       * <li>hyperlinks embedding</li>
       * <li>smaller file size</li>
       * </ul>
       */
      SVG(FILE_EXTENSION__SVG),
      /**
       * <a href="https://wikipedia.org/wiki/Portable_Network_Graphics">Portable Network
       * Graphics</a> (PNG).
       * <p>
       * Raster-graphics format that supports lossless compression, recommended only if {@link #SVG}
       * is not applicable (raster images don't scale well and their file size is substantially
       * larger than SVG).
       * </p>
       */
      PNG(FILE_EXTENSION__PNG);

      /**
       * The file extension for images of this format.
       */
      public final String fileExtension;

      Format(String fileExtension) {
        this.fileExtension = fileExtension;
      }
    }

    private Set<Format> formats = new LinkedHashSet<>();
    private @Nullable String subDirectory;

    // SourceName: formats
    /**
     * Image format(s) to generate UML diagrams in.
     * <p>
     * CLI option: {@value UmlConfig#OPTION__IMAGE_FORMAT} (incremental)
     * </p>
     * <p>
     * Default: {@link Format#SVG SVG} (resulting images are much smaller than, say,
     * {@link Format#PNG PNG} images and can include links to Javadoc HTML pages).
     * </p>
     */
    public @UnmodifiableView Set<Format> getFormats() {
      return formats;
    }

    // SourceName: directory
    /**
     * Directory where UML diagram images are generated within the Javadoc tool output.
     * <p>
     * CLI option: {@value UmlConfig#OPTION__IMAGE_DIR}
     * </p>
     * <p>
     * Default: empty (the image will be generated within the same directory as the corresponding
     * Javadoc HTML).
     * </p>
     */
    public @Nullable String getSubDirectory() {
      return subDirectory;
    }

    void onMainProcess() {
      if (formats.isEmpty()) {
        formats.add(Format.SVG);
      }
      formats = unmodifiableSet(formats);
    }

    void setSubDirectory(String value) {
      if (Path.of(value).isAbsolute())
        throw wrongArg(null, value, "MUST be relative");

      subDirectory = value;
    }
  }

  // SourceName: nl.talsmasoftware.umldoclet.configuration.MethodConfig
  // SourceName: nl.talsmasoftware.umldoclet.javadoc.DocletConfig.MethodCfg
  /**
   * Configuration for method rendering in UML diagrams.
   *
   * @author Sjoerd Talsma (original implementation)
   * @author Stefano Chizzolini (adaptation and redesign for Jada)
   */
  public static class MethodConfig {
    // SourceName: nl.talsmasoftware.umldoclet.configuration.MethodConfig.ParamNames
    /**
     * How method parameters are rendered in UML diagrams.
     *
     * @author Sjoerd Talsma (original implementation)
     * @author Stefano Chizzolini (adaptation and redesign for Jada)
     */
    public enum ParamNameMode {
      /**
       * Omits parameter names from methods altogether.
       */
      NONE,
      /**
       * Renders method parameter names before their respective types.
       */
      BEFORE_TYPE,
      /**
       * Renders method parameter names after their respective types (java-style).
       */
      AFTER_TYPE
    }

    // SourceName: paramNames
    ParamNameMode paramNameMode = ParamNameMode.NONE;
    // SourceName: paramTypes
    TypeMode paramTypeMode = TypeMode.SIMPLE;
    // SourceName: javaBeanPropertiesAsFields
    boolean propertiesFlattened;
    // SourceName: returnType
    TypeMode returnTypeMode = TypeMode.SIMPLE;
    Set<Visibility> visibilities = EnumSet.of(Visibility.PROTECTED, Visibility.PUBLIC);

    // SourceName: paramNames
    /**
     * How method parameter names are rendered in UML diagrams.
     */
    public ParamNameMode getParamNameMode() {
      return paramNameMode;
    }

    // SourceName: paramTypes
    /**
     * How method parameter types are rendered in UML diagrams.
     */
    public TypeMode getParamTypeMode() {
      return paramTypeMode;
    }

    // SourceName: returnType
    /**
     * How method return types are rendered in UML diagrams.
     */
    public TypeMode getReturnTypeMode() {
      return returnTypeMode;
    }

    // SourceName: include
    /**
     * Gets whether a method with the given visibility must be included in UML diagrams.
     */
    public boolean includes(Visibility visibility) {
      return visibilities.contains(visibility);
    }

    // SourceName: javaBeanPropertiesAsFields
    /**
     * Whether properties should be rendered in UML diagrams as accessor methods (such as
     * {@code getXyz()}, {@code isXyz()}, {@code setXyz(Xyz)}) rather than fields.
     * <p>
     * CLI option: {@value UmlConfig#OPTION__PROPERTIES_FLATTENED}
     * </p>
     */
    public boolean isPropertiesFlattened() {
      return propertiesFlattened;
    }
  }

  // SourceName: nl.talsmasoftware.umldoclet.configuration.TypeDisplay
  /**
   * How type names are rendered in UML diagrams.
   *
   * @author Sjoerd Talsma (original implementation)
   * @author Stefano Chizzolini (adaptation and redesign for Jada)
   */
  public enum TypeMode {
    /**
     * Omits the type name.
     */
    NONE,
    /**
     * Uses the simple type name without the containing package.
     */
    SIMPLE,
    /**
     * Uses the qualified type name.
     */
    QUALIFIED,
    /**
     * Uses the qualified type name, also for its generic type variables.
     */
    QUALIFIED_GENERICS
  }

  // SourceName: nl.talsmasoftware.umldoclet.configuration.Visibility
  /**
   * Visibility for classes, methods and fields.
   * <p>
   * In this extension, the visibility is used for two purposes:
   * </p>
   * <ol>
   * <li>to enable, via configuration, the rendering of classes, methods or fields belonging to a
   * certain visibility</li>
   * <li>to represent the visibility of classes, methods and fields in rendered diagrams</li>
   * </ol>
   *
   * @author Sjoerd Talsma (original implementation)
   * @author Stefano Chizzolini (adaptation and redesign for Jada)
   * @see <A href=
   *      "https://docs.oracle.com/javase/tutorial/java/javaOO/accesscontrol.html">Controlling
   *      Access to Members of a Class</a>
   */
  public enum Visibility {
    /**
     * The visibility corresponding with the Java {@link java.lang.reflect.Modifier#PRIVATE private}
     * modifier.
     */
    PRIVATE,
    /**
     * The visibility corresponding with the Java {@link java.lang.reflect.Modifier#PROTECTED
     * protected} modifier.
     */
    PROTECTED,
    /**
     * The visibility corresponding with the Java default {@link java.lang.reflect.Modifier}.
     */
    PACKAGE_PRIVATE,
    /**
     * The visibility corresponding with the Java {@link java.lang.reflect.Modifier#PUBLIC public}
     * modifier.
     */
    PUBLIC
  }

  // SourceName: nl.talsmasoftware.umldoclet.javadoc.ExternalLink
  /**
   * Processes {@code -link} and {@code -linkoffline} Javadoc options and contains functionality to
   * read a set of externally documented packages.
   * <p>
   * Since the {@code -link} option only has a single URI parameter, this uri must be used as both
   * {@code docUri} and {@code packageListUri}.
   * </p>
   *
   * @author Sjoerd Talsma (original implementation)
   * @author Stefano Chizzolini (adaptation and redesign for Jada)
   */
  static final class ExternalLink {
    // SourceName: nl.talsmasoftware.umldoclet.javadoc.PackagenameValidator
    /**
     * Java package name validator.
     *
     * @author Sjoerd Talsma (original implementation)
     * @author Stefano Chizzolini (adaptation and redesign for Jada)
     */
    static class PackageNameValidator implements Predicate<@Nullable String> {
      // SourceName: PACKAGENAME_PATTERN
      private static final Pattern PATTERN__PACKAGE_NAME = Pattern.compile(
          "([a-zA-Z_]+(\\.[a-zA-Z_]+)*)?");

      /**
       * Gets whether the value is a valid java package name.
       */
      @Override
      public boolean test(@Nullable String value) {
        return value != null && PATTERN__PACKAGE_NAME.matcher(value).matches();
      }
    }

    private static final PackageNameValidator PACKAGENAME_VALIDATOR = new PackageNameValidator();

    // SourceName: createUri
    private static URI uri(String uri) {
      try {
        return new URI(uri);
      } catch (URISyntaxException ex) {
        var file = Path.of(uri);
        if (exists(file))
          return file.toUri();

        throw wrongArg("uri", uri, null, ex);
      }
    }

    private final URI baseUri;
    private final UmlConfig config;
    private final URI docUri;
    private @LazyNonNull @Nullable Map<String, Set<String>> modules;
    private final Map<String, URI> packageUriCache = new HashMap<>();

    ExternalLink(UmlConfig config, String apidoc, String packageList) {
      this.config = requireNonNull(config, "`config`");
      this.docUri = uri(requireNonNull(apidoc, "`apidoc`"));
      this.baseUri = uri(requireNonNull(packageList, "`packageList`"));
    }

    Optional<URI> resolveType(String packageName, String typeName) {
      return getModules().entrySet().stream()
          .filter($ -> $.getValue().contains(packageName))
          .findFirst()
          .map($ -> cached(packageName, () -> findPackageUri($.getKey(), packageName)))
          .map($ -> addPathComponent($, typeName + FILE_EXTENSION__HTML))
          .map($ -> addHttpParam($, "is-external", "true"));
    }

    // SourceName: makeAbsolute
    private URI asAbsolute(URI uri) {
      return uri.isAbsolute() ? uri
          : config.getConfig().getOutputDirectory().resolve(uri.toASCIIString()).toUri()
              .normalize();
    }

    private URI cached(String packageName, Supplier<URI> uri) {
      synchronized (packageUriCache) {
        if (!packageUriCache.containsKey(packageName)) {
          packageUriCache.put(packageName, uri.get());
        }
      }
      return packageUriCache.get(packageName);
    }

    private URI findPackageUri(String moduleName, String packageName) {
      String packagePath = packageName.replace(DOT, SLASH);
      if (!moduleName.isEmpty()) {
        packagePath = moduleName + SLASH + packagePath;
      }
      return nonNull(addPathComponent(asAbsolute(docUri), packagePath));
    }

    // SourceName: modules
    private @UnmodifiableView Map<String, Set<String>> getModules() {
      if (modules == null) {
        synchronized (this) {
          var modules = readModules();
          this.modules = !modules.isEmpty() ? modules : singletonMap(EMPTY, readPackages());
        }
      }
      return modules;
    }

    // SourceName: tryReadModules
    private @UnmodifiableView Map<String, Set<String>> readModules() {
      final URI elementListUri = nonNull(addPathComponent(baseUri, FILENAME__ELEMENT_LIST));
      final var modules = new LinkedHashMap<String, Set<String>>();
      try (var reader = new BufferedReader(openReaderTo(config.getConfig().getOutputDirectory(),
          elementListUri, UTF_8))) {
        String module = EMPTY /* Defaults to unnamed module */;
        String line;
        while ((line = reader.readLine()) != null) {
          line = line.trim();
          if (module.isEmpty() && line.contains("<")) {
            config.getLog().print(Kind.OTHER, this, UmlMessage.ELEMENT_LIST_HTML_IGNORED,
                elementListUri);

            return emptyMap();
          } else if (line.startsWith("module:")) {
            module = line.substring("module:".length()).trim();
          } else if (PACKAGENAME_VALIDATOR.test(line)) {
            if (!modules.containsKey(module)) {
              modules.put(module, new LinkedHashSet<>());
            }
            modules.get(module).add(line);
          } else {
            config.getLog().print(Kind.OTHER, this, UmlMessage.SKIPPING_INVALID_PACKAGE_NAME,
                module, line);
          }
        }
      } catch (Exception ex) {
        config.getLog().print(Kind.WARNING, this, UmlMessage.CANNOT_READ_ELEMENT_LIST,
            elementListUri, ex);
      }
      return modules.isEmpty() ? emptyMap() : unmodifiableMap(modules);
    }

    // SourceName: tryReadPackages
    private @UnmodifiableView Set<String> readPackages() {
      final URI packageListUri = nonNull(addPathComponent(baseUri, FILENAME__PACKAGE_LIST));
      final var packages = new LinkedHashSet<String>();
      try {
        try (var reader = new BufferedReader(openReaderTo(config.getConfig().getOutputDirectory(),
            packageListUri, UTF_8))) {
          String line;
          while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (PACKAGENAME_VALIDATOR.test(line)) {
              packages.add(line);
            }
          }
        }
      } catch (Exception ex) {
        config.getLog().print(Kind.WARNING, this, UmlMessage.CANNOT_READ_PACKAGE_LIST,
            packageListUri, ex);
      }
      return packages.isEmpty() ? emptySet() : unmodifiableSet(packages);
    }
  }

  /**
   * @see #isCyclicPackageDependenciesChecked()
   */
  public static final String OPTION__CYCLIC_PACKAGE_DEPENDENCIES_CHECKED =
      "--uml-package-deps-check-cyclic";
  /**
   * @see #getExcludedPackageDependencies()
   */
  public static final String OPTION__EXCLUDED_PACKAGE_DEPENDENCIES =
      "--uml-package-deps-exclude";
  /**
   * @see #getExcludedTypeReferences()
   */
  public static final String OPTION__EXCLUDED_TYPE_REFERENCES =
      "--uml-type-refs-exclude";
  /**
   * @see ImageConfig#getSubDirectory() getImageConfig().getDirectory()
   */
  public static final String OPTION__IMAGE_DIR =
      "--uml-image-dir";
  /**
   * @see ImageConfig#getFormats() getImageConfig().getFormats()
   */
  public static final String OPTION__IMAGE_FORMAT =
      "--uml-image-format";
  /**
   * @see #getPackageDependenciesMaxCount()
   */
  public static final String OPTION__PACKAGE_DEPENDENCIES_MAX_COUNT =
      "--uml-package-deps-max-count";
  /**
   * @see #getPlantumlCustomDirectives()
   */
  public static final String OPTION__PLANTUML_CUSTOM_DIRECTIVE =
      "--uml-custom-directive";
  /**
   * PlantUML server timeout.
   */
  public static final String OPTION__PLANTUML_SERVER_TIMEOUT =
      "--uml-server-timeout";
  /**
   * @see #getPlantumlServerUrl()
   */
  public static final String OPTION__PLANTUML_SERVER_URL =
      "--uml-server-url";
  /**
   * @see MethodConfig#isPropertiesFlattened() getMethodConfig().isPropertiesFlattened()
   */
  public static final String OPTION__PROPERTIES_FLATTENED =
      "--uml-properties-flatten";
  /**
   * @see #getStaticFieldsMaxCount()
   */
  public static final String OPTION__STATIC_FIELDS_MAX_COUNT =
      "--uml-static-fields-max-count";

  public static final List<String> EXCLUDED_PACKAGE_DEPENDENCIES__DEFAULT = List.of(
      "java.*", "javax.*");
  public static final List<String> EXCLUDED_TYPE_REFERENCES__DEFAULT = List.of(
      "java.lang.Object", "java.lang.Enum", "java.lang.annotation.Annotation");
  public static final int PACKAGE_DEPENDENCIES_MAX_COUNT__DEFAULT = 10;
  public static final int STATIC_FIELDS_MAX_COUNT__DEFAULT = 10;

  private boolean cyclicPackageDependenciesChecked;
  private final List<String> excludedPackageDependencies = new ArrayList<>(
      EXCLUDED_PACKAGE_DEPENDENCIES__DEFAULT);
  // SourceName: excludedReferences
  private final List<String> excludedTypeReferences =
      new ArrayList<>(EXCLUDED_TYPE_REFERENCES__DEFAULT);
  private final List<ExternalLink> externalLinks = new ArrayList<>();
  private final FieldConfig fieldConfig = new FieldConfig();
  // SourceName: images
  private final ImageConfig imageConfig = new ImageConfig();
  private final MethodConfig methodConfig = new MethodConfig();
  private int packageDependenciesMaxCount = PACKAGE_DEPENDENCIES_MAX_COUNT__DEFAULT;
  // SourceName: customPlantumlDirectives
  private List<String> plantumlCustomDirectives = new ArrayList<>();
  private @Nullable String plantumlServerUrl;
  private int staticFieldsMaxCount = STATIC_FIELDS_MAX_COUNT__DEFAULT;

  /**
   */
  public UmlConfig(UmlExtension extension) {
    super(extension);
  }

  /**
   * <span class="warning">(For internal use only)</span>
   */
  protected UmlConfig() {
  }

  // SourceName: excludedPackageDependencies()
  /**
   * Packages excluded from the package dependencies diagram.
   * <p>
   * Names can be expressed as globs (with {@code '*'} and {@code '?'} wildcards).
   * </p>
   * <p>
   * CLI option: {@value #OPTION__EXCLUDED_PACKAGE_DEPENDENCIES} (incremental)
   * </p>
   * <p>
   * Default: {@linkplain #EXCLUDED_PACKAGE_DEPENDENCIES__DEFAULT "java.*,javax.*"}
   * </p>
   */
  public List<String> getExcludedPackageDependencies() {
    return excludedPackageDependencies;
  }

  // SourceName: excludedTypeReferences()
  /**
   * Fully-qualified names of the types excluded as reference.
   * <p>
   * Types can be any java type, such as classes and interfaces.
   * </p>
   * <p>
   * CLI option: {@value #OPTION__EXCLUDED_TYPE_REFERENCES} (incremental)
   * </p>
   * <p>
   * Default: {@linkplain #EXCLUDED_TYPE_REFERENCES__DEFAULT
   * "java.lang.Object,java.lang.Enum,java.lang.annotation.Annotation"}
   * </p>
   */
  public List<String> getExcludedTypeReferences() {
    return excludedTypeReferences;
  }

  @Override
  public UmlExtension getExtension() {
    return (UmlExtension) super.getExtension();
  }

  // SourceName: fields()
  /**
   * Configuration for generated UML fields.
   */
  public FieldConfig getFieldConfig() {
    return fieldConfig;
  }

  // SourceName: images()
  /**
   * Configuration for generated images.
   */
  public ImageConfig getImageConfig() {
    return imageConfig;
  }

  // SourceName: methods()
  /**
   * Configuration for generated UML methods.
   */
  public MethodConfig getMethodConfig() {
    return methodConfig;
  }

  /**
   * Maximum number of packages included in the package dependencies diagram.
   * <p>
   * CLI option: {@value #OPTION__PACKAGE_DEPENDENCIES_MAX_COUNT}
   * </p>
   * <p>
   * Default: {@value #PACKAGE_DEPENDENCIES_MAX_COUNT__DEFAULT}.
   * </p>
   */
  public int getPackageDependenciesMaxCount() {
    return packageDependenciesMaxCount;
  }

  // SourceName: customPlantumlDirectives()
  /**
   * Custom directives to include in rendered PlantUML diagram sources.
   * <p>
   * Custom directives are rendered as-is at the top of each PlantUML diagram (for example, to
   * render handwritten diagrams, use the {@code "skinparam handwritten true"} custom directive).
   * </p>
   * <p>
   * CLI option: {@value #OPTION__PLANTUML_CUSTOM_DIRECTIVE} (repeatable)
   * </p>
   */
  public @UnmodifiableView List<String> getPlantumlCustomDirectives() {
    return plantumlCustomDirectives;
  }

  // SourceName: plantumlServerUrl()
  /**
   * The base URL of the <a href="https://www.plantuml.com/plantuml">PlantUML server</a> to generate
   * diagrams with.
   * <p>
   * NOTE: Despite not strictly forbidden by PlantUML's author, it is recommended NOT to use the
   * public, central PlantUML server at
   * <a href="https://www.plantuml.com/plantuml">https://www.plantuml.com/plantuml</a> to generate
   * your Javadoc diagrams, as doing so poses additional load on that server and is a lot slower
   * than running your own local server.
   * </p>
   * <p>
   * Using docker to run a local PlantUML server can be a simple as:
   * </p>
   * <pre class="lang-shell"><code>
   * docker run -d -p 8080:8080 plantuml/plantuml-server:latest</code></pre>
   * <p>
   * After that, you can run the UMLDoclet with {@code plantumlServerUrl = "http://localhost:8080/"}
   * </p>
   * <p>
   * CLI option: {@value #OPTION__PLANTUML_SERVER_URL}
   * </p>
   */
  public @Nullable String getPlantumlServerUrl() {
    return plantumlServerUrl;
  }

  /**
   * Maximum number of static fields included in class diagrams.
   * <p>
   * CLI option: {@value #OPTION__STATIC_FIELDS_MAX_COUNT}
   * </p>
   * <p>
   * Default: {@value #STATIC_FIELDS_MAX_COUNT__DEFAULT}.
   * </p>
   */
  public int getStaticFieldsMaxCount() {
    return staticFieldsMaxCount;
  }

  /**
   * Whether cyclic package dependencies are checked.
   * <p>
   * In case of detection, a warning is logged.
   * </p>
   * <p>
   * CLI option: {@value #OPTION__CYCLIC_PACKAGE_DEPENDENCIES_CHECKED}
   * </p>
   */
  public boolean isCyclicPackageDependenciesChecked() {
    return cyclicPackageDependenciesChecked;
  }

  /**
   * Resolves an external link to the type.
   *
   * @param packageName
   *          The package of the type.
   * @param type
   *          The type name within the package.
   */
  public Optional<URI> resolveExternalLinkToType(String packageName, String type) {
    return externalLinks.stream()
        .map($ -> $.resolveType(packageName, type))
        .filter(Optional::isPresent).map(Optional::get)
        .findFirst();
  }

  List<ExternalLink> getExternalLinks() {
    return externalLinks;
  }

  void onMainProcess() {
    /*
     * Smetana layout engine (see <https://plantuml.com/smetana02>).
     */
    ensureCustomDirective("!pragma layout", "smetana");
    /*
     * Preserve the aspect ratio of the diagram, scaling it up as much as the entire viewBox is
     * still visible within the viewport (see
     * <https://developer.mozilla.org/en-US/docs/Web/SVG/Reference/Attribute/preserveAspectRatio>).
     */
    ensureCustomDirective("skinparam preserveAspectRatio", "xMidYMid meet");
    plantumlCustomDirectives = unmodifiableList(plantumlCustomDirectives);

    imageConfig.onMainProcess();
  }

  void setCyclicPackageDependenciesChecked(boolean value) {
    cyclicPackageDependenciesChecked = value;
  }

  void setPackageDependenciesMaxCount(int value) {
    packageDependenciesMaxCount = checkMaxCount(value);
  }

  void setPlantumlServerUrl(String value) {
    plantumlServerUrl = value;
  }

  void setStaticFieldsMaxCount(int value) {
    staticFieldsMaxCount = checkMaxCount(value);
  }

  void showMembers(String value) {
    Set<Visibility> visibility = parseVisibility(value);
    fieldConfig.visibilities = visibility;
    methodConfig.visibilities = visibility;
  }

  private int checkMaxCount(int value) {
    return value >= 0 ? value : Integer.MAX_VALUE;
  }

  private void ensureCustomDirective(String name, String defaultValue) {
    if (plantumlCustomDirectives.stream().noneMatch($ -> $.startsWith(name))) {
      plantumlCustomDirectives.add(0, name + SPACE + defaultValue);
    }
  }

  private Set<Visibility> parseVisibility(String value) {
    return switch (value) {
      case "private", "all" -> EnumSet.allOf(Visibility.class);
      case "package" -> EnumSet.of(Visibility.PACKAGE_PRIVATE, Visibility.PROTECTED,
          Visibility.PUBLIC);
      case "protected" -> EnumSet.of(Visibility.PUBLIC, Visibility.PROTECTED);
      case "public" -> EnumSet.of(Visibility.PUBLIC);
      default -> {
        getLog().print(Kind.WARNING, this, UmlMessage.UNKNOWN_VISIBILITY, value);

        yield parseVisibility("protected" /* Javadoc default */);
      }
    };
  }
}
