/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Jada.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core;

import static java.util.Objects.requireNonNullElse;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.pdfclown.common.util.Chars.ANGLE_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.COLON;
import static org.pdfclown.common.util.Chars.HYPHEN;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.ROUND_BRACKET_OPEN;
import static org.pdfclown.common.util.Chars.SEMICOLON;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Chars.SQUARE_BRACKET_CLOSE;
import static org.pdfclown.common.util.Chars.SQUARE_BRACKET_OPEN;
import static org.pdfclown.common.util.Exceptions.missing;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Exceptions.wrongState;
import static org.pdfclown.common.util.Objects.fqn;
import static org.pdfclown.common.util.Objects.sqn;
import static org.pdfclown.common.util.Objects.toStringWithValues;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.EOL;
import static org.pdfclown.common.util.system.Clis.parseListIncremental;
import static org.pdfclown.common.util.system.Clis.parseResource;
import static org.pdfclown.jada.core.JadaConfig.OPTION__BASE_DOCLET;
import static org.pdfclown.jada.core.JadaConfig.OPTION__DEBUG;
import static org.pdfclown.jada.core.JadaConfig.OPTION__DOCLET_EXTENSIONS;
import static org.pdfclown.jada.core.JadaConfig.OPTION__FILE_OPTIMIZATION_FILTER;
import static org.pdfclown.jada.core.JadaConfig.OPTION__HELP;
import static org.pdfclown.jada.core.JadaConfig.OPTION__HELP_EXTRA;
import static org.pdfclown.jada.core.JadaConfig.OPTION__INPUT_ENCODING;
import static org.pdfclown.jada.core.JadaConfig.OPTION__LOG_LEVEL;
import static org.pdfclown.jada.core.JadaConfig.OPTION__OUTPUT_DIR;
import static org.pdfclown.jada.core.JadaConfig.OPTION__OUTPUT_ENCODING;
import static org.pdfclown.jada.core.JadaConfig.OPTION__QUIET;
import static org.pdfclown.jada.core.JadaConfig.OPTION__REJECT_WARNINGS;
import static org.pdfclown.jada.core.JadaConfig.OPTION__RESOURCE_DIR;
import static org.pdfclown.jada.core.JadaConfig.OPTION__VERBOSE;
import static org.pdfclown.jada.core.util.Objects.realSubTypes;

import com.sun.tools.javac.util.Log;
import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import jdk.javadoc.doclet.StandardDoclet;
import jdk.javadoc.doclet.Taglet;
import org.greenrobot.eventbus.EventBus;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.ArgumentException;
import org.pdfclown.common.util.annot.Initializer;
import org.pdfclown.common.util.io.PathResource;
import org.pdfclown.common.util.io.Resource;
import org.pdfclown.common.util.reflect.Reflects;
import org.pdfclown.common.util.system.Clis.FileInclusionFilter;
import org.pdfclown.common.util.system.Clis.ListIncrementalAdapter;
import org.pdfclown.jada.core.JadaConfig.Attachment;
import org.pdfclown.jada.core.event.MainProcessEvent;
import org.pdfclown.jada.core.event.PostProcessEvent;
import org.pdfclown.jada.core.internal.Internals;
import org.pdfclown.jada.core.internal.JadaMessage;
import org.pdfclown.jada.core.proc.FileOptimizer;
import org.pdfclown.jada.core.proc.JadaFileProcess;
import org.pdfclown.jada.core.proc.JadaResourceAttach;
import org.pdfclown.jada.core.proc.PageProcessor;
import org.pdfclown.jada.core.proc.PostTagletProcessor;
import org.pdfclown.jada.core.system.Event;
import org.pdfclown.jada.core.system.Logger;
import org.pdfclown.jada.core.system.SystemConfig.LogLevel;
import org.pdfclown.jada.core.system.proc.FileProcessor;
import org.pdfclown.jada.core.taglet.JadaTaglet;
import org.pdfclown.jada.core.util.Nameable;

/**
 * General-purpose extensible doclet.
 * <p>
 * Combines an arbitrary {@linkplain Doclet doclet} with multiple {@linkplain JadaExtension
 * extension}s in order to transform its output (NOTE: the current implementation is focused on
 * HTML-based {@link StandardDoclet}; further doclets will be supported as they become relevant).
 * </p>
 * <h4>Introduction</h4>
 * <p>
 * Despite its ubiquity in the standard Java development environment, Javadoc is commonly perceived
 * as a dull tool of the trade whose reusability is a bit neglected if not discouraged (its clumsy,
 * minimalist public API, its overly-defensive implementation and its scarce, almost non-existent,
 * hands-on documentation make for a (to be gentle) bumpy development experience, forcing developers
 * to devise ugly workarounds even for the most obvious repurposing — for example, the standard
 * doclet doesn't tolerate injection of a custom {@code DocletEnvironment} other than internal
 * {@code DocEnvImpl}, thus obstructing useful pre-processing transformations, unless JDK internals
 * are horribly unlocked).
 * </p>
 * <p>
 * <i>The traditional design of custom doclets is monolithic</i>: main generation is delegated to
 * the standard doclet (HTML output) and pre-processed (for example, for source files with alternate
 * javadoc formats such as Markdown (before <a href="https://openjdk.org/jeps/467">Java 23</a>) or
 * AsciiDoc) or post-processed (for example, to add UML diagrams to the generated documentation
 * files), or (less frequently) the entire generation is implemented from scratch (for example, for
 * alternate output formats such as XML) — in any case, just a single hard-coded set of features can
 * be applied to a doclet. <i>This single-extensibility model can be annoyingly limiting in case
 * multiple specialized doclets need to be placed on the same Javadoc toolchain</i>. Some doclets
 * try to mitigate this by allowing users to pass their own doclet delegate; such jury-rigged
 * solutions, however, lack the consistency of a common mechanism to easily chain doclet
 * functionalities. Furthermore, the minimalism of Doclet API forces doclets to reimplement common
 * functionalities over and over.
 * </p>
 * <p>
 * Jada tries to fill this gap as a tiny framework on top of the Javadoc tool: with minor tweaks,
 * <i>existing doclets can be adapted to work together on the same Javadoc execution as specialized
 * components (<b>{@linkplain JadaExtension doclet extensions}</b>)</i>, with the additional
 * benefits of a simple, intuitive and user-friendly API (each and every object in its model extends
 * a single class, {@link JadaObject}, which provides direct access to all the relevant parts of the
 * model) which relieves them of common chores like options definition (a {@linkplain JadaOptions
 * dedicated builder} provides ready-to-use option creation with transparent option overriding,
 * parameter composition, text localization, ...), {@linkplain Logger message logging} (enhanced
 * printing with parameterized, localized and contextualized (caller's parent component, instance
 * class and stack location) messages, ...), {@linkplain JadaConfig#getResources(String) shared page
 * resources} (such as {@linkplain JadaConfig#addScriptAttachment(Attachment) javascripts} and
 * {@linkplain JadaConfig#addStylesheetAttachment(Attachment) stylesheets}, dynamically insertable
 * and optimized), input (Java source code) and output (generated documentation) transformation
 * (pluggable {@linkplain FileProcessor file processors}), {@linkplain JadaTaglet taglets}
 * integration (overcoming architectural limitations which affect the interaction between taglets
 * and doclet), Javadoc testing harness, and so on.
 * </p>
 * <h4>Usage</h4>
 * <h5>Maven</h5>
 * <p>
 * In the {@code pom.xml} file of your project, add to {@code maven-javadoc-plugin} the following
 * configuration:
 * </p>
 * <pre class="lang-xml" data-line="8-15,17-25,28-30"><code>
 * &lt;project&gt;
 *   &lt;build&gt;
 *     &lt;plugins&gt;
 *       &lt;plugin&gt;
 *         &lt;artifactId&gt;maven-javadoc-plugin&lt;/artifactId&gt;
 *         &lt;version&gt;${maven-javadoc-plugin.version}&lt;/version&gt;
 *         &lt;configuration&gt;
 *           <span style=
"background-color:yellow;color:black;">&lt;doclet&gt;org.pdfclown.jada.core.Jada&lt;/doclet&gt;
 *           &lt;docletArtifacts&gt;
 *             &lt;artifact&gt;
 *               &lt;groupId&gt;org.pdfclown&lt;/groupId&gt;
 *               &lt;artifactId&gt;jada-core&lt;/artifactId&gt;
 *               &lt;version&gt;${jada.version}&lt;/version&gt;
 *             &lt;/artifact&gt;
 *           &lt;/docletArtifacts&gt;</span>
 *           &lt;additionalOptions&gt;
 *             <span style=
"background-color:yellow;color:black;">&lt;option&gt;-jada-doclet Standard&lt;/option&gt;
 *             &lt;option&gt;-jada-dir ${rootdir}/src/main/javadoc/jada&lt;/option&gt;
 *             &lt;!--
 *               TIP: Uncomment the following option to list all the available options on next
 *               `mvn javadoc` execution.
 *             --&gt;
 *             &lt;!--
 *             &lt;option&gt;-help&lt;/option&gt;
 *             --&gt;</span>
 *           &lt;/additionalOptions&gt;
 *           &lt;additionalJOptions&gt;
 *             <span style=
"background-color:yellow;color:black;">&lt;option&gt;-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED&lt;/option&gt;
 *             &lt;option&gt;-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED&lt;/option&gt;
 *             &lt;option&gt;-J--add-exports=jdk.javadoc/jdk.javadoc.internal.tool=ALL-UNNAMED&lt;/option&gt;</span>
 *           &lt;/additionalJOptions&gt;
 *         &lt;configuration&gt;
 *       &lt;plugin&gt;
 *     &lt;plugins&gt;
 *   &lt;build&gt;
 * &lt;project&gt;</code></pre>
 *
 * @author Stefano Chizzolini
 * @see <a href=
 *      "https://docs.oracle.com/en/java/javase/17/docs/api/jdk.javadoc/jdk/javadoc/doclet/package-summary.html">Doclet
 *      API</a>
 * @see <a href="https://openjdk.org/groups/compiler/using-new-doclet.html">Using the new Doclet
 *      API</a>
 * @see <a href=
 *      "https://docs.oracle.com/en/java/javase/21/docs/specs/man/javadoc.html"><code>javadoc</code>
 *      command</a>
 * @implSpec Unfortunately, each and every bit of the doclet architecture in its
 *           {@linkplain StandardDoclet standard implementation} is defensively concealed behind the
 *           public API, severely hampering any useful interaction between taglets and custom
 *           doclets, despite {@link Taglet} API notes that "It is typical for a taglet to be
 *           designed to work in conjunction with a specific doclet [sic!]" (unintentional irony?);
 *           there is even code expressly dedicated to impede passing extra data up the stack! — for
 *           example, in JDK 17, {@code jdk.javadoc.internal.doclets.toolkit.WorkArounds} bears an
 *           irritatingly eloquent note:<pre class="lang-java"><code>
 * // Note: this one use of DocEnvImpl is what prevents us tunnelling extra
 * // info from a doclet to its taglets via a doclet-specific subtype of
 * // DocletEnvironment.</code></pre>
 *           <p>
 *           The main problem is that <i>custom taglets are isolated from the custom doclet</i>:
 *           they are loaded by {@link URLClassLoader}s, provided by
 *           {@link DocletEnvironment#getJavaFileManager()}{@link javax.tools.JavaFileManager#getClassLoader(javax.tools.JavaFileManager.Location)
 *           .getClassLoader(Location)}, which are separate from the one the doclet was initially
 *           loaded by, causing the rest of the model (that is, {@link Jada} and
 *           {@link JadaExtension} dependencies) to have its classes duplicated for each taglet (a
 *           {@link Class} is identified by its fully-qualified name (FQN) <i>and</i> its
 *           {@link ClassLoader}); consequently, the taglets cannot directly share data with it, as
 *           trying to access their interfaces causes {@link ClassCastException} due to binary
 *           incompatibility between copies of the same class.
 *           </p>
 *           <h5>Proxies</h5>
 *           <p>
 *           To work around the architectural limitations affecting the {@linkplain StandardDoclet
 *           standard implementation}, <i>shared model instances are automatically proxied</i>. In
 *           order to avoid malfunctions and erratic behavior, <i>implementers of extensions and
 *           taglets are recommended to stick to the following specification for the definition of
 *           their classes</i>:
 *           </p>
 *           <ul>
 *           <li><b>NEVER use package scope</b> to declare classes or their members (constructors
 *           and methods) — it makes them inaccessible to proxies, disrupting delegation to proxied
 *           objects!</li>
 *           <li><b>NEVER use anonymous classes to subclass shared model types</b></li>
 *           <li>keep <b>ALL fields private</b>, and implement accessor methods for them if needed —
 *           proxies are interface-based, they do not see fields!</li>
 *           <li>expose a <b>no-argument constructor</b> for each class — proxies use it for
 *           automatic instantiation;</li>
 *           <li><b>NEVER do self-calls within a constructor</b> — proxy initialization completes
 *           only AFTER its constructor returns, so any call within the constructor involving the
 *           object under construction will fail as the proxy is not ready yet!</li>
 *           <li>use <b>{@linkplain org.pdfclown.common.util.Objects#xcast(Object)
 *           cross-casting}</b> to perform calls across class loader boundaries (to and from
 *           taglets).</li>
 *           </ul>
 */
public class Jada implements Doclet, JadaComponent {
  /**
   * {@link Jada} component candidate.
   * <p>
   * Represents an element of the Jada component model (either a {@link Doclet} or a
   * {@link JadaExtension}).
   * </p>
   *
   * @param <T>
   *          Candidate type.
   * @author Stefano Chizzolini
   */
  public abstract static class JadaCandidate<T> implements Nameable {
    static JadaCandidate<?> of(Class<?> baseType) {
      if (Doclet.class.isAssignableFrom(baseType))
        return new JadaCandidate<>(newInstance(baseType, Doclet.class)) {
          @Override
          public String getName() {
            return base.getName();
          }
        };
      else if (JadaExtension.class.isAssignableFrom(baseType))
        return new JadaCandidate<>(newInstance(baseType, JadaExtension.class)) {
          @Override
          public String getName() {
            return base.getName();
          }
        };
      else
        throw wrongArg("baseType", baseType, "Component type INVALID");
    }

    private static <T> T newInstance(Class<?> baseType, Class<T> targetType) {
      try {
        return baseType.asSubclass(targetType).getConstructor().newInstance();
      } catch (Exception ex) {
        throw runtime(ex);
      }
    }

    protected final T base;

    private JadaCandidate(T base) {
      this.base = base;
    }

    /**
     * Component.
     */
    public T getBase() {
      return base;
    }

    /**
     * Component name.
     */
    @Override
    public abstract String getName();

    /**
     * Component type.
     */
    public Class<?> getType() {
      return base.getClass();
    }

    @Override
    public String toString() {
      return toStringWithValues(this, base);
    }
  }

  private class JadaCandidates implements AutoCloseable {
    private final Map<Class<?>, List<JadaCandidate<?>>> base = new LinkedHashMap<>();
    private final Set<Class<?>> electedTypes = new HashSet<>();

    @Override
    public void close() {
      base.clear();
      electedTypes.clear();
    }

    /**
     * Picks the candidates associated to the type, marking them as {@linkplain #isDone(Class)
     * done}.
     *
     * @throws IllegalArgumentException
     *           if {@code type} is not among the candidate types.
     */
    public <T> List<JadaCandidate<T>> elect(Class<T> type) {
      var ret = get(type);
      electedTypes.add(type);
      return ret;
    }

    /**
     * Gets the candidates associated to the type.
     *
     * @throws ArgumentException
     *           if {@code type} is not among the candidate types.
     */
    @SuppressWarnings("unchecked")
    public <T> List<JadaCandidate<T>> get(Class<T> type) {
      return Optional.ofNullable(((List<JadaCandidate<T>>) (Object) base.get(type)))
          .orElseThrow(() -> wrongArg("type", type));
    }

    /**
     * Gets the candidate types.
     */
    public Set<Class<?>> getTypes() {
      return base.keySet();
    }

    /**
     * Whether the candidates associated to the type have already been {@linkplain #elect(Class)
     * elected}.
     */
    public boolean isDone(Class<?> type) {
      return electedTypes.contains(type);
    }

    /**
     * Loads the candidates associated to the type.
     *
     * @param type
     *          Candidate type.
     * @param filter
     *          Candidate filter.
     * @param defaultType
     *          Alternate candidate (in case no candidate is discovered).
     * @return Loaded candidates.
     */
    @SuppressWarnings("unchecked")
    public <T> List<JadaCandidate<T>> load(Class<T> type, Predicate<Class<?>> filter,
        @Nullable Class<? extends T> defaultType) {
      List<JadaCandidate<?>> typeCandidates;
      base.put(type, typeCandidates = realSubTypes(type)
          .filter(filter)
          .map(JadaCandidate::of)
          .collect(toCollection(ArrayList::new)));
      if (typeCandidates.isEmpty() && defaultType != null) {
        typeCandidates.add(JadaCandidate.of(defaultType));
      }
      return (List<JadaCandidate<T>>) (Object) typeCandidates;
    }
  }

  public static final String NAME = "Jada";

  @SuppressWarnings("UnusedReturnValue")
  private static StringBuilder appendComponentTo(StringBuilder b, Object component,
      @Nullable Collection<?> selection) {
    return appendComponentTo(b, component, selection, Integer.MIN_VALUE, Integer.MIN_VALUE);
  }

  /**
   * Prints the Jada components list entry (name and status) of the component.
   *
   * @param errorCount
   *          ({@code -1}, if status is unknown; {@link Integer#MIN_VALUE}, to suppress status
   *          printing).
   */
  private static StringBuilder appendComponentTo(StringBuilder b, Object component,
      @Nullable Collection<?> selection, int errorCount, int warningCount) {
    /*
     * COMPONENT NAME
     */
    b.append(EOL)
        .append(HYPHEN).append(HYPHEN)
        .append(selection != null && selection.contains(component) ? ANGLE_BRACKET_CLOSE : HYPHEN)
        .append(SPACE)
        .append(SQUARE_BRACKET_OPEN)
        .append(Reflects.<String>get(component, "getName"))
        .append(SQUARE_BRACKET_CLOSE).append(SPACE)
        .append(fqn(component));

    /*
     * COMPONENT STATUS
     */
    if (errorCount >= -1) {
      var status = errorCount == -1 ? null
          : errorCount > 0 ? Status.ERROR
          : warningCount > 0 ? Status.WARNING
          : Status.SUCCESS;
      b.append(COLON).append(SPACE).append(requireNonNullElse(status, "UNKNOWN"));
      if (errorCount > 0 || warningCount > 0) {
        b.append(SPACE).append(ROUND_BRACKET_OPEN);
        if (errorCount > 0) {
          b.append("errors").append(COLON).append(SPACE).append(errorCount);
        }
        if (warningCount > 0) {
          if (errorCount > 0) {
            b.append(SEMICOLON).append(SPACE);
          }
          b.append("warnings").append(COLON).append(SPACE).append(warningCount);
        }
        b.append(ROUND_BRACKET_CLOSE);
      }
    }
    return b;
  }

  /**
   * @implNote Match by name, by fully-qualified type name, or by simple type name.
   */
  private static boolean matchesComponentName(JadaCandidate<?> component, String value) {
    return component.getName().equals(value)
        || fqn(component.getBase()).equals(value)
        || sqn(component.getBase()).equals(value);
  }

  private static <T> T selectCandidate(String selectedName, List<JadaCandidate<T>> candidates) {
    return candidates.stream()
        .filter($ -> matchesComponentName($, selectedName))
        .findFirst()
        .map(JadaCandidate::getBase)
        .orElseThrow(() -> missing(selectedName));
  }

  int extErrorCount;
  int extWarningCount;

  @SuppressWarnings("NotNullFieldNotInitialized")
  private Doclet base;
  private final JadaCandidates candidates = new JadaCandidates();
  @SuppressWarnings("NotNullFieldNotInitialized")
  private JadaConfig config;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private JadaEnvironment env;
  @SuppressWarnings("NotNullFieldNotInitialized")
  private EventBus eventBus;
  @SuppressWarnings("this-escape")
  private final JadaScriptManager scriptManager = new JadaScriptManager(this);
  @SuppressWarnings("NotNullFieldNotInitialized")
  private Set<? extends Option> supportedOptions;

  public Jada() {
  }

  /**
   * Underlying doclet, delegated to generate the Javadoc documentation.
   * <p>
   * CLI option: {@value JadaConfig#OPTION__BASE_DOCLET}
   * </p>
   */
  public Doclet getBase() {
    return base;
  }

  /**
   * Number of errors encountered so far by the {@linkplain #getBase() base doclet}.
   */
  public int getBaseErrorCount() {
    return getErrorCount() - extErrorCount;
  }

  /**
   * Number of warnings encountered so far by the {@linkplain #getBase() base doclet}.
   */
  public int getBaseWarningCount() {
    return getWarningCount() - extWarningCount;
  }

  @Override
  public JadaConfig getConfig() {
    return config;
  }

  /*
   * WARNING: During bootstrap `env` may not be initialized yet, so internal calls to this getter
   * should be checked against nullness.
   */
  @Override
  public JadaEnvironment getEnv() {
    return env;
  }

  @Override
  public int getErrorCount() {
    return getBaseLog().nerrors;
  }

  @Override
  public Jada getJada() {
    return this;
  }

  @Override
  public Logger getLog() {
    return config.getLog();
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Set<? extends Option> getSupportedOptions() {
    return supportedOptions;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return base.getSupportedSourceVersion();
  }

  @Override
  public int getWarningCount() {
    return getBaseLog().nwarnings;
  }

  @Initializer
  @Override
  public void init(Locale locale, Reporter reporter) {
    Internals.registerComponentName(this);

    /*
     * EVENT BUS
     *
     * NOTE: Failure in subscriber event handler purposely propagates causing javadoc tool to exit
     * (it doesn't make sense to keep running when the execution is broken).
     */
    eventBus = EventBus.builder().throwSubscriberException(true).build();

    /*
     * CONFIGURATION
     */
    config = new JadaConfig(this, locale, reporter);

    /*
     * OPTIONS
     */
    var options = new JadaOptions(config);
    {
      /*
       * COMPONENT OPTIONS
       */
      initCandidates(options);

      /*
       * JADA OPTIONS
       */
      supportedOptions = options.setOwner(JadaCandidate.of(getClass()))
          /*
           * TOOL-DERIVED OPTIONS
           *
           * NOTE: They are purposely hidden, not to interfere with javadoc tool's synopsis.
           */
          .add(List.of(OPTION__HELP, "-help", "-?", "-h"), Option.Kind.OTHER, List.of(),
              $args -> config.setHelp(true))
          .add(List.of(OPTION__HELP_EXTRA, "-X"), Option.Kind.OTHER, List.of(),
              $args -> config.setHelp(true))
          .add(OPTION__QUIET, Option.Kind.OTHER, List.of(),
              $args -> config.setQuiet(true))
          .add(OPTION__VERBOSE, Option.Kind.OTHER, List.of(),
              $args -> config.setVerbose(true))
          .add(OPTION__REJECT_WARNINGS, Option.Kind.OTHER, List.of(),
              $args -> config.setWarningsRejected(true))
          /*
           * STANDARD-DOCLET-DERIVED OPTIONS
           */
          .add(OPTION__INPUT_ENCODING, List.of("<name>"),
              $args -> config.setInputEncoding($args.get(0)))
          .add(OPTION__OUTPUT_DIR, List.of("<directory>"),
              $args -> config.setOutputDirectory(Path.of($args.get(0))))
          .add(OPTION__OUTPUT_ENCODING, List.of("<name>"),
              $args -> config.setOutputEncoding($args.get(0)))
          /*
           * JADA-SPECIFIC OPTIONS
           */
          .add(OPTION__BASE_DOCLET, options.getText(OPTION__BASE_DOCLET, EMPTY,
              getCandidatesDescription(Doclet.class)), List.of("(<name>|<class>)"),
              $args -> base = selectCandidate($args.get(0), candidates.elect(Doclet.class)))
          .add(OPTION__DEBUG, List.of(),
              $args -> config.setDebug(true))
          .add(OPTION__DOCLET_EXTENSIONS, options.getText(OPTION__DOCLET_EXTENSIONS, EMPTY,
              getCandidatesDescription(JadaExtension.class)),
              List.of("[+-]?(<name>|<class>)(,(<name>|<class>))*"),
              $args -> {
                var extensions = candidates.elect(JadaExtension.class);
                parseListIncremental($args.get(0),
                    $ -> $.map($$ -> selectCandidate($$, extensions)),
                    new ListIncrementalAdapter<JadaExtension>() {
                      @Override
                      public boolean add(JadaExtension e) {
                        config.addExtension(e);
                        return true;
                      }

                      @Override
                      public void clear() {
                        config.clearExtensions();
                      }

                      @Override
                      public boolean remove(Object o) {
                        return config.removeExtension((JadaExtension) o);
                      }
                    });
              })
          .add(OPTION__FILE_OPTIMIZATION_FILTER, List.of("[+-]?<path-glob>(,<path-glob>)*"),
              $args -> parseListIncremental($args.get(0), identity(),
                  new ListIncrementalAdapter<String>() {
                    final FileInclusionFilter base = config.getFileOptimizationFilter();

                    @Override
                    public boolean add(String e) {
                      return base.getIncludes().add(e);
                    }

                    @Override
                    public void clear() {
                      base.clear();
                    }

                    @Override
                    public boolean remove(Object o) {
                      return base.getExcludes().add((String) o);
                    }
                  }))
          .add(OPTION__LOG_LEVEL, List.of(Arrays.stream(LogLevel.values())
              .map(LogLevel::toString)
              .collect(joining("|", "(", ")"))),
              $args -> config.setLogLevel(LogLevel.valueOf($args.get(0))))
          .add(OPTION__RESOURCE_DIR, List.of("(<classpath>|<path>)"),
              $args -> {
                var dirValue = $args.get(0);
                Resource dir = parseResource(dirValue);
                if (dir == null)
                  throw wrongArg("args[0]", dirValue, "Resource directory NOT FOUND");
                else if (!(dir instanceof PathResource))
                  throw wrongArg("args[0]", dirValue, "Resource directory INVALID");

                config.addResourceDirectory(((PathResource) dir).getPath());
              })
          .build();
    }
  }

  /**
   * Broadcasts the event.
   */
  public void post(Event<?> event) {
    eventBus.post(event);
  }

  @Initializer
  @Override
  public final boolean run(DocletEnvironment env) {
    this.env = new JadaEnvironment(env, this);

    // Initialize execution!
    initRun();

    notifyMainProcess();

    // Doclet execution succeeded?
    if (base.run(this.env)
        && getBaseErrorCount() == 0 /*
                                     * NOTE: `StandardDoclet.run(..)` returns `true` even if errors
                                     * occurred, so we have to check also its error count.
                                     */) {
      notifyPostProcess();

      // Execute post-process operations!
      config.getOperations().values().forEach(JadaOperation::run);

      // Terminate post-process operations!
      config.getOperations().values().forEach(JadaOperation::term);
    }

    // Terminate execution!
    return termRun();
  }

  /**
   * Subscribes the object to event broadcasting.
   */
  public void subscribe(Object o) {
    eventBus.register(o);
  }

  /**
   * Unsubscribes the object from event broadcasting.
   */
  public void unsubscribe(Object o) {
    eventBus.unregister(o);
  }

  /**
   * Initializes the doclet session.
   */
  protected void initRun() {
    /*
     * INPUT DIRECTORIES
     */
    {
      final var fm = (StandardJavaFileManager) env.getJavaFileManager();
      Iterable<? extends File> location;
      try {
        /*
         * NOTE: According to API, `StandardJavaFileManager.getLocation(..)` should return `null` if
         * no search path is associated to the given location; however, in case of non-modular
         * source, `IllegalStateException` is unexpectedly thrown, despite no meaningful value can
         * be returned -- smells badly like a bug.
         */
        location = fm.getLocation(StandardLocation.MODULE_SOURCE_PATH);
      } catch (IllegalStateException ex) {
        location = null;
      }
      // Modular source (JDK 9+)?
      if (location != null) {
        /*
         * NOTE: Building doclet options, maven-javadoc-plugin (version 3.10.1) places module
         * sources in `--patch-module` option rather than `--module-source-path` option:
         *
         * --patch-module mymodule.name='/home/.../mymodule.name/src/main/java'
         *
         * --module-source-path '/home/.../mymodule.name/target/reports/apidocs/src'
         *
         * For resilience, both paths are harvested.
         */
        {
          location.forEach($ -> config.addInputDirectory($));

          try {
            fm.listLocationsForModules(StandardLocation.PATCH_MODULE_PATH).forEach(
                $ -> $.forEach(
                    $$ -> fm.getLocation($$).forEach(
                        $$$ -> config.addInputDirectory($$$))));
          } catch (IOException e) {
            getLog().print(Kind.ERROR, this, StandardLocation.PATCH_MODULE_PATH + " FAILED");
          }
        }
      }
      // Classic, non-modular source (JDK 8-).
      else {
        location = fm.getLocation(StandardLocation.SOURCE_PATH);
        if (location != null) {
          location.forEach($ -> config.addInputDirectory($));
        }
      }
    }

    /*
     * POST-PROCESS OPERATIONS
     */
    config
        .registerOperation(new JadaResourceAttach())
        .registerOperation(new JadaFileProcess());

    config.getOperation(JadaFileProcess.class)
        .addProcessor(new PostTagletProcessor())
        .addProcessor(new PageProcessor())
        .addProcessor(new FileOptimizer());

    /*
     * COMPONENTS
     */
    initComponents();
  }

  /**
   * Terminates the doclet session.
   *
   * @return Whether the execution succeeded.
   */
  protected boolean termRun() {
    boolean ret;
    var status = getStatus();
    var b = new StringBuilder();
    {
      /*
       * BASE DOCLET
       */
      {
        b.append(EOL).append(HYPHEN).append(SPACE).append(Doclet.class.getName())
            .append(COLON);

        appendComponentTo(b, getBase(), null, getBaseErrorCount(), getBaseWarningCount());

        ret = getBaseErrorCount() == 0;
      }

      /*
       * DOCLET EXTENSIONS
       */
      if (!config.getExtensions().isEmpty()) {
        b.append(EOL).append(HYPHEN).append(SPACE).append(JadaExtension.class.getName())
            .append(COLON);

        for (var ext : config.getExtensions().values()) {
          ext.term();

          var extStatus = ext.getStatus();
          if (extStatus.compareTo(status) < 0) {
            status = extStatus;
          }

          appendComponentTo(b, ext, null,
              /*
               * NOTE: In case of base doclet errors, the extensions may have not completed their
               * own execution, so the lack of problems cannot be assumed as success.
               */
              getBaseErrorCount() == 0 || ext.getErrorCount() > 0 || ext.getWarningCount() > 0
                  ? ext.getErrorCount()
                  : -1,
              ext.getWarningCount());

          ret &= ext.isSuccess();
        }
      }
    }
    getLog().print(status.getLevel(), this, JadaMessage.COMPONENTS_RESULT, b);

    return ret;
  }

  /**
   * @implNote Till now (JDK 25), javadoc tool's {@link Reporter} has always extended
   *           {@code com.sun.tools.javac.util.Log}.
   */
  private Log getBaseLog() {
    Reporter reporter = getLog().getBase();
    if (reporter instanceof Log log)
      return log;
    else
      throw unexpected("Base log type doesn't extends any of the expected types ({})", Log.class);
  }

  private <T> String getCandidatesDescription(Class<T> type) {
    return candidates.get(type).stream()
        .map($ -> (EOL + HYPHEN + SPACE + "'%s' ('%s')").formatted($.getName(),
            $.getBase().getClass().getName()))
        .collect(collectingAndThen(joining(), $ -> !$.isEmpty() ? $ : "(NONE)"));
  }

  /**
   * Loads the Jada components available on the classpath, candidates for activation.
   *
   * @see #initComponents()
   * @implNote {@value JadaConfig#OPTION__QUIET} option is filtered out from {@link #getBase() base
   *           doclet} configuration to ensure log interception of generated documentation file
   *           paths (see {@link JadaConfig}{@code .LogWriterInterceptor} for more information).
   */
  private void initCandidates(JadaOptions options) {
    /*
     * BASE DOCLETS
     */
    candidates.load(Doclet.class, $ -> $ != getClass(), StandardDoclet.class).forEach($ -> {
      Internals.registerComponentName($.getBase());
      $.getBase().init(config.getLocale(), getLog().getBase());

      /*
       * NOTE: "-quiet" option is filtered out (see implNote).
       */
      options.setOwner($)
          .addAll($.getBase().getSupportedOptions().stream()
              .filter($$ -> !$$.getNames().contains(OPTION__QUIET))
              .collect(toList()));
    });

    /*
     * DOCLET EXTENSIONS
     */
    candidates.load(JadaExtension.class, $ -> true, null).forEach($ -> {
      Internals.registerComponentName($.getBase());
      $.getBase().init(options.setOwner($), this);
    });
  }

  /**
   * Completes the activation of Jada components in case no selection was done via respective CLI
   * option, and logs the status of the active components.
   * <ul>
   * <li>{@linkplain #getBase() Jada base doclet} is automatically activated in case a single one
   * (typically {@link StandardDoclet}) is available on the classpath; otherwise, in case of
   * multiple doclets available, users MUST explicitly select one via
   * {@value JadaConfig#OPTION__BASE_DOCLET} CLI option</li>
   * <li>{@linkplain JadaConfig#getExtensions() Jada extensions} MUST be explicitly selected via
   * {@value JadaConfig#OPTION__DOCLET_EXTENSIONS} CLI option; otherwise, they are ignored, leaving
   * an informative log message.</li>
   * </ul>
   *
   * @see #initCandidates(JadaOptions)
   * @implNote Jada components can be activated in two ways:
   *           <ul>
   *           <li>manual — in case the corresponding CLI option was specified by users (see
   *           {@code candidates.elect(..)} in {@link #init(Locale, Reporter)})</li>
   *           <li>automatic — default, in case no option was specified (see
   *           {@code candidates.elect(..)} in this method)</li>
   *           </ul>
   * @throws IllegalStateException
   *           if no candidate base doclet is available, or multiple are available without
   *           selection.
   */
  private void initComponents() {
    /*
     * BASE DOCLET
     */
    if (!candidates.isDone(Doclet.class)) {
      var baseCandidates = candidates.elect(Doclet.class);
      switch (baseCandidates.size()) {
        case 0 -> throw wrongState(
            "No {} candidate available: ensure any implementation is loaded into "
                + "the classpath",
            fqn(Doclet.class));
        case 1 -> base = baseCandidates.get(0).getBase();
        default -> throw wrongState("Multiple {} candidates available: use {} option to choose one",
            fqn(Doclet.class), OPTION__BASE_DOCLET);
      }
    }

    /*
     * DOCLET EXTENSIONS
     */
    if (!candidates.isDone(JadaExtension.class)) {
      // Complete extensions candidacy without activating them (no user selection via CLI option)!
      candidates.elect(JadaExtension.class);

      getLog().print(Kind.NOTE, this,
          "No extension was selected (use \"{0}\" option to specify them)",
          OPTION__DOCLET_EXTENSIONS);
    } else {
      config.getExtensions().values().forEach(this::subscribe);
    }

    /*
     * SUMMARY LOG
     */
    {
      var b = new StringBuilder();
      for (var candidateType : candidates.getTypes()) {
        b.append(EOL).append(HYPHEN).append(SPACE).append(candidateType.getName()).append(COLON);

        Collection<?> selection;
        if (candidateType == Doclet.class) {
          selection = List.of(base);
        } else if (candidateType == JadaExtension.class) {
          selection = config.getExtensions().values();
        } else
          throw unexpected(candidateType);

        var typeCandidates = candidates.get(candidateType);
        if (typeCandidates.isEmpty()) {
          b.append(EOL).append(SPACE).append("(NONE)");
        } else {
          for (var candidate : typeCandidates) {
            appendComponentTo(b, candidate.getBase(), selection);
          }
        }
      }
      getLog().print(Kind.OTHER, this, JadaMessage.COMPONENTS, b);
    }

    candidates.close();
  }

  private void notifyMainProcess() {
    post(new MainProcessEvent(this));

    scriptManager.run("onMainProcess");
  }

  private void notifyPostProcess() {
    post(new PostProcessEvent(this));

    scriptManager.run("onPostProcess");
  }
}
