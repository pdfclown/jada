/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Assertions.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.test.assertion;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.pdfclown.common.build.test.Tests.testFrame;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.wrongArg;
import static org.pdfclown.common.util.Exceptions.wrongState;
import static org.pdfclown.common.util.Objects.fqn;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.io.Files.resetDirectory;
import static org.pdfclown.jada.core.JadaConfig.OPTION__OUTPUT_DIR;
import static org.pdfclown.jada.core.JadaConfig.OPTION__QUIET;
import static org.pdfclown.jada.core.JadaConfig.OPTION__SOURCE_PATH;
import static org.pdfclown.jada.core.JadaConfig.OPTION__SOURCE_PATH__L;
import static org.pdfclown.jada.core.JadaConfig.OPTION__VERBOSE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.StackWalker.StackFrame;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.spi.ToolProvider;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.build.test.assertion.TestEnvironment;
import org.pdfclown.common.util.system.Clis.Args;
import org.pdfclown.jada.core.Jada;
import org.pdfclown.jada.core.JadaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test assertions.
 *
 * @author Stefano Chizzolini
 */
public final class Assertions {
  /**
   * {@link Assertions#assertJavadoc(JavadocAssertArgs) assertJavadoc(..)} arguments.
   *
   * @author Stefano Chizzolini
   */
  public static class JavadocAssertArgs extends Args {
    /**
     * Reusable argument groups.
     *
     * @author Stefano Chizzolini
     * @see JavadocAssertArgs#args(Iterable)
     */
    public static final class ArgGroups {
      /**
       * JSR-335 tags ({@code @apiNote}, {@code @implSpec}, and {@code @implNote}) configuration.
       */
      public static final String KEY__JSR335_TAGS = "jsr335-tags";

      private static final Map<String, List<String>> base = new HashMap<>();

      static {
        register(KEY__JSR335_TAGS, List.of(
            "-tag", "implSpec:a:Implementation Requirements:",
            "-tag", "implNote:a:Implementation Note:",
            "-tag", "apiNote:a:API Note:"));
      }

      /**
       * Gets the argument group associated to the key.
       *
       * @throws IllegalArgumentException
       *           if no registered group is associated to {@code key}.
       */
      public static List<String> get(String key) {
        var ret = base.get(key);
        if (ret == null)
          throw wrongArg("key", key, "Argument group MISSING");

        return ret;
      }

      /**
       * Registers an argument group.
       */
      public static void register(String key, List<String> group) {
        base.put(key, group);
      }

      private ArgGroups() {
      }
    }

    final TestEnvironment env;
    int exitcode;
    @Nullable
    BiConsumer<JavadocAssertArgs, Path> onRunInit;
    boolean outputStreamErr;
    boolean outputStreamOut;
    boolean outputStreamsMerged;
    @Nullable
    String packageDir;
    final List<Path> typeFiles = new ArrayList<>();

    public JavadocAssertArgs(TestEnvironment env) {
      this.env = requireNonNull(env, "`env`");
    }

    @Override
    public JavadocAssertArgs arg(Object o) {
      return (JavadocAssertArgs) super.arg(o);
    }

    @Override
    public JavadocAssertArgs arg(String s) {
      // Source path?
      if (s.equals(OPTION__SOURCE_PATH) || s.equals(OPTION__SOURCE_PATH__L)) {
        if (packageDir != null)
          throw wrongState("`packageDir` ALREADY DEFINED ({})", packageDir);

        // Mark as pending assignment for next call!
        packageDir = EMPTY;
      }
      // Source path: pending assignment?
      else if (packageDir != null && packageDir.isEmpty()) {
        if (s.isEmpty())
          throw wrongArg("s", s, "INVALID (packageDir cannot be assigned an empty value)");

        packageDir = s;
      }
      return (JavadocAssertArgs) super.arg(s);
    }

    @Override
    public JavadocAssertArgs arg(String option, Object... values) {
      return (JavadocAssertArgs) super.arg(option, values);
    }

    @Override
    public JavadocAssertArgs args(Iterable<?> ee) {
      return (JavadocAssertArgs) super.args(ee);
    }

    @Override
    public JavadocAssertArgs args(Object[] ee) {
      return (JavadocAssertArgs) super.args(ee);
    }

    /**
     * Sets the expected javadoc exit code.
     */
    public JavadocAssertArgs exitcode(int value) {
      exitcode = value;
      return this;
    }

    /**
     * Arguments.
     */
    public List<String> getBase() {
      return base;
    }

    /**
     * @see #setOnRunInit(BiConsumer)
     */
    public @Nullable BiConsumer<JavadocAssertArgs, Path> getOnRunInit() {
      return onRunInit;
    }

    /**
     * Roots the javadoc tool output at the subpath under the current testing folder.
     * <p>
     * Useful to define an arbitrary output location.
     * </p>
     *
     * @param value
     *          ({@linkplain Class#getResource(String) Java resource name})
     */
    public JavadocAssertArgs outputDir(Path value) {
      int argIndex;
      if ((argIndex = base.indexOf(OPTION__OUTPUT_DIR)) >= 0) {
        base.set(argIndex + 1, value.toString());
      } else {
        arg(OPTION__OUTPUT_DIR, value);
      }
      return this;
    }

    /**
     * Redirects the javadoc tool's {@code err} stream output to its corresponding
     * {@linkplain JavadocAssertResult#err result}.
     *
     * @see #outputStreams(boolean)
     */
    public JavadocAssertArgs outputStreamErr() {
      outputStreamErr = true;
      return this;
    }

    /**
     * Redirects the javadoc tool's {@code out} stream output to its corresponding
     * {@linkplain JavadocAssertResult#out result}.
     *
     * @see #outputStreams(boolean)
     */
    public JavadocAssertArgs outputStreamOut() {
      outputStreamOut = true;
      return this;
    }

    /**
     * Redirects the javadoc tool's output streams' outputs to their corresponding results
     * ({@link JavadocAssertResult#out out} and {@link JavadocAssertResult#err err}).
     *
     * @param merged
     *          Whether the outputs must be combined as a single stream.
     */
    public JavadocAssertArgs outputStreams(boolean merged) {
      outputStreamOut();
      outputStreamErr();
      outputStreamsMerged = merged;
      return this;
    }

    /**
     * Sets the packages source directory.
     */
    public JavadocAssertArgs packageDir(Object value) {
      return arg(OPTION__SOURCE_PATH, value);
    }

    /**
     * Adds the name of a package to document.
     */
    public JavadocAssertArgs packageName(String value) {
      /*
       * NOTE: Invalid `packageDir` may be either `null` (undefined) or empty (pending assignment).
       */
      if (StringUtils.isEmpty(packageDir))
        throw wrongState("`packageDir` UNDEFINED");

      arg(value);
      return this;
    }

    /**
     * Adds the names of packages to document.
     */
    public JavadocAssertArgs packageNames(Collection<String> value) {
      value.forEach(this::packageName);
      return this;
    }

    /**
     * Sets the handler for javadoc run initialization.
     * <p>
     * Called after the output directory has been prepared for javadoc execution; useful to adjust
     * the configuration (for example, creating additional input files, with corresponding
     * arguments) before javadoc execution.
     * </p>
     */
    public JavadocAssertArgs setOnRunInit(@Nullable BiConsumer<JavadocAssertArgs, Path> value) {
      onRunInit = value;
      return this;
    }

    /**
     * Adds a type to document.
     */
    public JavadocAssertArgs type(Class<?> value) {
      typeFiles.add(env.typeSrcPath(value));
      return this;
    }
  }

  /**
   * {@link Assertions#assertJavadoc(JavadocAssertArgs) assertJavadoc(..)} result.
   *
   * @author Stefano Chizzolini
   */
  public static class JavadocAssertResult {
    /**
     * Javadoc tool's {@code err} stream output.
     *
     * @see JavadocAssertArgs#outputStreamErr()
     */
    public final @Nullable String err;
    /**
     * Javadoc tool's {@code out} (or merged) stream output.
     *
     * @see JavadocAssertArgs#outputStreamOut()
     */
    public final @Nullable String out;
    /**
     * Output directory of the javadoc run.
     */
    public final Path outputDir;

    JavadocAssertResult(Path outputDir, @Nullable String out, @Nullable String err) {
      this.outputDir = outputDir;
      this.out = out;
      this.err = err;
    }
  }

  private static final Logger log = LoggerFactory.getLogger(Assertions.class);

  /**
   * Asserts the javadoc tool is executed with the arguments
   * {@linkplain JavadocAssertArgs#exitcode(int) as expected}.
   * <p>
   * {@code args} is inspected for the {@linkplain JadaConfig#OPTION__OUTPUT_DIR output directory}
   * option: if missing, it is {@linkplain TestEnvironment#outputPath(String) synthesized} from the
   * name of the current test (that is, the method on the stack trace marked by {@link Test @Test});
   * consequently, if invoked outside a test method, the output directory falls back to the base
   * output directory of the test environment (that is, {@link TestEnvironment#outputPath(String)
   * args.env.outputPath("")}). The output directory is automatically initialized, cleaning any
   * previous content; it is available {@linkplain JavadocAssertArgs#getOnRunInit() on run
   * initialization}, and {@linkplain JavadocAssertResult#outputDir returned} at the end of the
   * execution.
   * </p>
   * <p>
   * In case of assertion failure, the {@linkplain JavadocAssertArgs#outputStreams(boolean)
   * intercepted output} is sent to the respective standard stream (or just to {@link System#out
   * stdout} if the streams were merged).
   * </p>
   */
  public static JavadocAssertResult assertJavadoc(JavadocAssertArgs args) {
    Path outputDir;
    var javadocArgs = new ArrayList<String>();
    {
      // Doclet.
      javadocArgs.add("-doclet");
      javadocArgs.add(fqn(Jada.class));

      // Output directory.
      try {
        int argIndex;
        // Explicit?
        if ((argIndex = args.getBase().indexOf(OPTION__OUTPUT_DIR)) >= 0) {
          outputDir = Path.of(args.getBase().get(argIndex + 1)).toAbsolutePath().normalize();
        }
        // Implicit.
        else {
          outputDir = args.env.outputPath(testFrame().map(StackFrame::getMethodName).orElse(EMPTY));

          javadocArgs.add(OPTION__OUTPUT_DIR);
          javadocArgs.add(outputDir.toString());
        }

        // Prepare the output directory!
        resetDirectory(outputDir);
      } catch (IOException ex) {
        throw runtime(ex);
      }
      if (args.onRunInit != null) {
        args.onRunInit.accept(args, outputDir);
      }

      // Quiet execution (if not already specified).
      if (!args.contains(OPTION__QUIET) && !args.contains(OPTION__VERBOSE)) {
        javadocArgs.add(OPTION__QUIET);
      }

      // Additional arguments.
      javadocArgs.addAll(args.getBase());

      /*
       * Output suppression.
       *
       * NOTE: For the sake of efficiency, unnecessary content is not generated (this reduced about
       * 70% both the execution time and the generated files).
       */
      ensureArg(javadocArgs, "-nohelp");
      ensureArg(javadocArgs, "-noindex");
      ensureArg(javadocArgs, "-nonavbar");
      ensureArg(javadocArgs, "-notree");

      // Source files.
      javadocArgs.addAll(args.typeFiles.stream()
          .map(Path::toString)
          .collect(toList()));
    }

    var outStream = args.outputStreamOut || args.outputStreamsMerged
        ? new ByteArrayOutputStream()
        : null;
    var errStream = args.outputStreamErr && !args.outputStreamsMerged
        ? new ByteArrayOutputStream()
        : null;
    try {
      PrintStream out = outStream != null ? new PrintStream(outStream, true, UTF_8)
          : System.out;
      PrintStream err = errStream != null ? new PrintStream(errStream, true, UTF_8)
          : args.outputStreamsMerged ? out : System.err;

      assertThat("Javadoc result", ToolProvider.findFirst("javadoc").orElseThrow()
          .run(out, err, javadocArgs.toArray(String[]::new)), is(args.exitcode));
    } catch (Throwable ex) {
      /*
       * If the assertion fails, we have to dump the buffered output in order not to lose its
       * content.
       */
      if (outStream != null) {
        try {
          outStream.writeTo(System.out);
        } catch (IOException ex1) {
          log.warn("Buffered stdout dump on assertion failure FAILED", ex1);
        }
      }
      if (errStream != null) {
        try {
          errStream.writeTo(System.err);
        } catch (IOException ex1) {
          log.warn("Buffered stderr dump on assertion failure FAILED", ex1);
        }
      }
      throw ex;
    }

    return new JavadocAssertResult(outputDir, outStream != null ? outStream.toString(UTF_8) : null,
        errStream != null ? errStream.toString(UTF_8) : null);
  }

  private static void ensureArg(List<String> args, String value) {
    if (!args.contains(value)) {
      args.add(value);
    }
  }

  private Assertions() {
  }
}
