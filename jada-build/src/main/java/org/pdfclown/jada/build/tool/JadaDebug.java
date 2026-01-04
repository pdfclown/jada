/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaDebug.java) is part of jada-build module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.build.tool;

import static java.lang.System.out;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.getLastModifiedTime;
import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Objects.textLiteral;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.spi.ToolProvider;
import org.apache.commons.lang3.function.Failable;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.build.tool.Debug;
import org.pdfclown.common.build.util.system.Builds;
import org.pdfclown.common.util.ArgumentException;
import org.pdfclown.common.util.annot.InitNonNull;
import org.pdfclown.common.util.system.Clis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Javadoc launcher for Jada debugging.
 * <p>
 * Bootstraps the javadoc tool, tuning its configuration for debugging against the Jada development
 * environment.
 * </p>
 * <p>
 * The execution is based on configuration files (see {@code  @files} in
 * <a href="https://docs.oracle.com/en/java/javase/17/docs/specs/man/javadoc.html">javadoc tool
 * reference</a>), as specified by the
 * <a href="https://maven.apache.org/plugins/maven-javadoc-plugin/javadoc-mojo.html#debug">Apache
 * Maven Javadoc Plugin</a>.
 * </p>
 * <p>
 * Before launching, the execution environment MUST be prepared accordingly (here Maven is assumed
 * as build system):
 * </p>
 * <ol>
 * <li><b>create the debugging configuration</b> ("options", "packages", "argfile" files) — on the
 * target project, run the build tool to generate the debugging
 * configuration:<pre class="lang-shell"><code>
 * mvn javadoc:javadoc -Ddebug</code></pre></li>
 * <li><b>synchronize Jada</b> (all Jada subprojects MUST have their compiled classes synchronized
 * to include any change occurred to its code base) — ensure your IDE automatically compiles changed
 * classes, or manually force it; alternatively, on Jada project root,
 * run:<pre class="lang-shell"><code>
* mvn compile</code></pre></li>
 * </ol>
 *
 * @author Stefano Chizzolini
 */
public class JadaDebug extends Debug {
  /**
   * {@link JadaDebug} CLI arguments.
   */
  public static class CliArgs extends Debug.CliArgs {
    static class ArgsConverter implements ITypeConverter<List<String>> {
      public static List<String> parse(String value) {
        return Clis.parseArgs(value);
      }

      public List<String> convert(String value) {
        return parse(value);
      }
    }

    /**
     * Root directory of Jada project, where the code base to debug is present.
     */
    @Parameters(index = "0", description = "Root directory of Jada project,"
        + " where the code base to debug is present")
    @SuppressWarnings("NotNullFieldNotInitialized")
    @InitNonNull
    public Path jadaProjectDir;

    /**
     * Target directory of javadoc tool, where its configuration is present (namely, 'options' and
     * 'packages' or 'argfile' files) and its output is expected to be generated.
     */
    @Parameters(index = "1", description = "Target directory of javadoc tool,"
        + " where its configuration is present (namely, 'options' and 'packages' or 'argfile'"
        + " files) and its output is expected to be generated", arity = "0...1")
    @SuppressWarnings("NotNullFieldNotInitialized")
    @InitNonNull
    public Path javadocTargetDir;

    /**
     * Additional javadoc tool arguments.
     * <p>
     * Useful, for example, to show the javadoc help ({@code --javadoc-args "-h"}).
     * </p>
     */
    @Option(names = { "--javadoc-args" }, description = "Additional javadoc tool arguments."
        + "\nUseful, for example, to show the javadoc help (`--javadoc-args \"-h\"`).", //
        converter = ArgsConverter.class)
    public List<String> javadocArgs = List.of();

    @Override
    protected void validate() {
      if (interactive) {
        //noinspection ConstantValue
        if (javadocTargetDir == null) {
          while (true) {
            String javadocTarget = promptArg(ARGNAME__JAVADOC_TARGET_DIR, """
                either a Jada subproject (biblio, core, uml) or the full path to an external \
                project""", null);

            // Jada subproject?
            var jadaSubprojectDir = jadaProjectDir.resolve("jada-" + javadocTarget);
            if (Files.isDirectory(jadaSubprojectDir)) {
              try {
                javadocTargetDir = checkJavadocTargetDir(jadaSubprojectDir);
                break;
              } catch (IllegalArgumentException ex) {
                out.println(ex.getMessage());
                out.println();
                continue;
              }
            }

            // External project?
            try {
              javadocTargetDir = checkJavadocTargetDir(Path.of(javadocTarget));
              break;
            } catch (IllegalArgumentException ex) {
              out.println(ex.getMessage());
              out.println();
            }
          }
        }
        if (javadocArgs.isEmpty()) {
          javadocArgs = ArgsConverter.parse(promptArg("javadocArgs",
              "additional javadoc tool arguments",
              null));
        }
      }
      super.validate();
    }
  }

  private static final Logger log = LoggerFactory.getLogger(JadaDebug.class);

  private static final String ARGNAME__JADA_PROJECT_DIR = "jadaProjectDir";
  private static final String ARGNAME__JAVADOC_TARGET_DIR = "javadocTargetDir";

  private static final String FILENAME__ARGFILE = "argfile";
  private static final String FILENAME__OPTIONS = "options";
  private static final String FILENAME__PACKAGES = "packages";

  private static final String PATTERN_GROUP__ARTIFACT_ID = "artifactId";

  private static final Pattern PATTERN__JADA_ARTIFACT_FILENAME = Pattern.compile(
      "(?<%s>jada-[a-z-]+)-\\d\\S+?(?:SNAPSHOT|\\d).jar".formatted(
          PATTERN_GROUP__ARTIFACT_ID));
  private static final Pattern PATTERN__JADA_ARTIFACT_FILE = Pattern.compile(
      "(?<dir>[^'\":]+)%s".formatted(
          PATTERN__JADA_ARTIFACT_FILENAME.pattern()));

  /**
   * {@link JadaDebug} entry point.
   *
   * @param args
   *          (see {@link CliArgs})
   */
  public static void main(String[] args) {
    var cliArgs = new CliArgs();
    CommandLine cli = init(JadaDebug.class, "JADA DEBUGGING LAUNCHER", cliArgs);
    out.println("""
        IMPORTANT: In order to debug Jada, all its subprojects MUST have their classes
        freshly built before executing this launcher, to include  any change  occurred
        to its code base:  ensure your IDE automatically compiles changed classes,  or
        manually force the build; alternatively, run `mvn compile` on CLI.
        """);
    parseArgs(cli, args);

    try {
      var debug = new JadaDebug(cliArgs.jadaProjectDir, cliArgs.javadocTargetDir)
          .setJavadocArgs(cliArgs.javadocArgs);
      if (cliArgs.recording) {
        debug.record();
      }
      debug.run();
    } catch (Exception ex) {
      if (ex instanceof ArgumentException argEx) {
        switch (argEx.getArgName()) {
          case ARGNAME__JADA_PROJECT_DIR:
          case ARGNAME__JAVADOC_TARGET_DIR:
            exit(ex.getMessage(), null, cli);
            // FALLTHRU -- compiler is oblivious that at this point the process already exited.
          default:
            // NOP
        }
      }
      exit(null, ex, null);
    }
  }

  private static Path checkJadaProjectDir(Path value) {
    var ret = requireNonNull(value, ARGNAME__JADA_PROJECT_DIR).normalize();
    if (!Files.isDirectory(ret.resolve("jada-core")))
      throw wrongArg(ARGNAME__JADA_PROJECT_DIR, value, """
          INVALID (should be the root directory of Jada project, containing subprojects like \
          jada-core)""");

    return ret;
  }

  private static Path checkJavadocTargetDir(Path value) {
    var ret = requireNonNull(value, ARGNAME__JAVADOC_TARGET_DIR).normalize();
    if (!containsJavadocConfig(ret)) {
      ret = ret.resolve("target/reports/apidocs");
      if (!containsJavadocConfig(ret))
        throw wrongArg(ARGNAME__JAVADOC_TARGET_DIR, value, """
            INVALID (should be the target directory of javadoc tool, where its configuration \
            files ({} and {} or {}) are present -- run `mvn javadoc:javadoc -Ddebug` to generate \
            them)""", textLiteral(FILENAME__OPTIONS), textLiteral(FILENAME__PACKAGES),
            textLiteral(FILENAME__ARGFILE));
    }

    return ret;
  }

  private static boolean containsJavadocConfig(Path dir) {
    return exists(dir.resolve(FILENAME__OPTIONS));
  }

  private final @Nullable Path jadaProjectDir;
  private List<String> javadocArgs = List.of();
  private final @Nullable Path javadocTargetDir;

  private final Map<String, List<Path>> jadaArtifactClasspaths = new HashMap<>();
  private final @Nullable Path optionsFile;
  private final @Nullable Path sourcesFile;

  /**
   * @param jadaProjectDir
   *          Root directory of Jada project, where the code base to debug is present.
   * @param javadocTargetDir
   *          Target directory of javadoc tool, where its configuration is present (namely,
   *          {@value #FILENAME__OPTIONS} and {@value #FILENAME__PACKAGES} or
   *          {@value #FILENAME__ARGFILE} files) and its output is expected to be generated.
   */
  public JadaDebug(Path jadaProjectDir, Path javadocTargetDir) {
    this.jadaProjectDir = checkJadaProjectDir(jadaProjectDir);
    this.javadocTargetDir = checkJavadocTargetDir(javadocTargetDir);

    // Prepare the debugging configuration!
    this.optionsFile = prepareOptionsFile();
    this.sourcesFile = prepareSourcesFile();
  }

  /**
   * Extra javadoc tool arguments.
   */
  public List<String> getJavadocArgs() {
    return javadocArgs;
  }

  /**
   * Sets {@link #getJavadocArgs() javadocArgs}.
   */
  public JadaDebug setJavadocArgs(List<String> value) {
    javadocArgs = value;
    return this;
  }

  @Override
  protected void doRun() {
    log.info("javadoc RUNNING...");

    // Launch the javadoc tool!
    var args = new ArrayList<String>();
    {
      args.add("--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED");
      args.add("--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED");
      args.add("--add-exports=jdk.javadoc/jdk.javadoc.internal.tool=ALL-UNNAMED");
      args.add("@" + optionsFile);
      args.add("@" + sourcesFile);
      args.addAll(javadocArgs);
    }

    log.debug("javadoc arguments: {}", args);

    ToolProvider.findFirst("javadoc").orElseThrow()
        .run(out, System.err, args.toArray(String[]::new));

    log.info("javadoc FINISHED");
  }

  /**
   * Gets the options file prepared for debugging.
   * <p>
   * The original {@value #FILENAME__OPTIONS} file is copied to prepare a debug-specific version
   * whose Jada-related artifacts referenced on classpath are replaced with local instances, to
   * ensure source code synchronization during debugging.
   * </p>
   */
  private Path prepareOptionsFile() {
    assert jadaProjectDir != null;
    assert javadocTargetDir != null;

    final Path ret = javadocTargetDir.resolve(FILENAME__OPTIONS + "-debug");
    final Path originalOptionsFile = javadocTargetDir.resolve(FILENAME__OPTIONS);
    try {
      // Debugging options already up to date?
      if (exists(ret)
          && getLastModifiedTime(originalOptionsFile).compareTo(getLastModifiedTime(ret)) < 0) {
        log.debug("`options` file '{}' ALREADY UPDATED", ret);

        return ret;
      }

      String originalOptionsContent = Files.readString(originalOptionsFile);
      Matcher m = PATTERN__JADA_ARTIFACT_FILE.matcher(originalOptionsContent);
      StringBuilder optionsContentBuilder = null;
      while (m.find()) {
        if (optionsContentBuilder == null) {
          optionsContentBuilder = new StringBuilder();
        }

        var originalJadaArtifactFile = Path.of(m.group()).normalize();

        List<Path> jadaArtifactClasspath = jadaArtifactClasspaths.computeIfAbsent(
            m.group(PATTERN_GROUP__ARTIFACT_ID), Failable.asFunction(
                $k -> Builds.classpath(jadaProjectDir.resolve($k), "runtime")));

        // Append build classes in place of the corresponding Jada artifact!
        {
          Path jadaArtifactClassesPath = jadaArtifactClasspath.get(0);
          m.appendReplacement(optionsContentBuilder, jadaArtifactClassesPath.toString());

          log.debug("Classpath reference UPDATED:\n{} -> {}", originalJadaArtifactFile,
              jadaArtifactClassesPath);
        }

        /*
         * Append additional dependencies of the Jada artifact!
         *
         * NOTE: Because of package transformations like shading, the original artifact may omit
         * certain dependencies, merged inside itself; replacing such artifact with the bare build
         * classes of the corresponding subproject, those implicit dependencies emerge back to
         * sunlight and must be added to the classpath in order to make the build classes work.
         */
        final var b = optionsContentBuilder;
        jadaArtifactClasspath.stream()
            .skip(1)
            .map(Path::toString)
            .filter($ -> !originalOptionsContent.contains($))
            .forEachOrdered($ -> {
              b.append(File.pathSeparator).append($);

              log.debug("Classpath reference ADDED:\n{}", $);
            });
      }

      // Original options updated for debugging?
      if (optionsContentBuilder != null) {
        m.appendTail(optionsContentBuilder);

        Files.writeString(ret, optionsContentBuilder);
      }
      // Original options suitable for debugging as-is.
      else {
        Files.copy(originalOptionsFile, ret, StandardCopyOption.REPLACE_EXISTING);
      }

      log.debug("javadoc options file saved at '{}' ({})", ret,
          optionsContentBuilder != null ? "modified" : "as-is");

      return ret;
    } catch (IOException ex) {
      throw runtime(ex);
    }
  }

  /**
   * Gets the sources file prepared for debugging.
   * <p>
   * Can be either {@value #FILENAME__ARGFILE} or {@value #FILENAME__PACKAGES}.
   * </p>
   *
   * @throws ArgumentException
   *           if no file was found.
   */
  private Path prepareSourcesFile() {
    assert javadocTargetDir != null;

    var ret = javadocTargetDir.resolve(FILENAME__ARGFILE);
    if (exists(ret))
      return ret;

    ret = javadocTargetDir.resolve(FILENAME__PACKAGES);
    if (exists(ret))
      return ret;

    throw wrongArg(ARGNAME__JAVADOC_TARGET_DIR, javadocTargetDir,
        "INVALID (sources file ({} or {}) MISSING)",
        textLiteral(FILENAME__PACKAGES), textLiteral(FILENAME__ARGFILE));
  }
}
