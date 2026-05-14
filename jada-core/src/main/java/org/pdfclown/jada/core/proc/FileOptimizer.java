/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (FileOptimizer.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.proc;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.pdfclown.common.util.Chars.SPACE;
import static org.pdfclown.common.util.Chars.STAR;
import static org.pdfclown.common.util.Objects.nonNull;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__CSS;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__JAVASCRIPT;
import static org.pdfclown.common.util.io.Files.baseName;
import static org.pdfclown.common.util.io.Files.extension;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.LightweightMessageFormatter;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceExcerptProvider.SourceExcerpt;
import com.google.javascript.jscomp.SourceFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.tools.Diagnostic.Kind;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.LazyNonNull;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.core.internal.JadaMessage;
import org.pdfclown.jada.core.internal.temp.util.io.ResourceNames;
import org.pdfclown.jada.core.system.SystemConfig;
import org.pdfclown.jada.core.system.proc.FileProcess;
import org.pdfclown.jada.core.system.proc.FileProcess.Context;
import org.pdfclown.jada.core.system.proc.TextSerializer;

/**
 * Optimizes Javadoc tool output files.
 * <p>
 * Supported file types:
 * </p>
 * <ul>
 * <li>CSS (*.css)</li>
 * <li>Javascript (*.js)</li>
 * </ul>
 * <p>
 * Files whose basename ends with {@code ".min"} are ignored, as they are assumed to have already
 * been optimized.
 * </p>
 * <p>
 * Header comments with essential information such as copyright notice and license are automatically
 * preserved injecting the appropriate symbol:
 * </p>
 * <ul>
 * <li>CSS: <a href="https://yui.github.io/yuicompressor/css.html">special comment marker for
 * preservation</a> ({@code "/*!"})</li>
 * <li>Javascript: <a href=
 * "https://github.com/google/closure-compiler/wiki/Annotating-JavaScript-for-the-Closure-Compiler#license-preserve-description">preservation
 * tag</a> ({@code "@preserve"})</li>
 * </ul>
 * <p>
 * NOTE: In {@linkplain JadaConfig#isDebug() debug mode}, optimization is disabled to ease source
 * code inspection and stepping.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class FileOptimizer extends JadaFileProcessor<String> {
  /**
   * CSS optimizer.
   *
   * @author Stefano Chizzolini
   */
  private static class CssOptimizer implements FileTypeOptimizer {
    /**
     * <a href="https://yui.github.io/yuicompressor/css.html">Special comment marker for
     * preservation</a>.
     */
    private static final String COMMENT_MARKER__PRESERVE = "!";

    @Override
    public @Nullable String optimize(String content, Path file, Context context,
        StringBuilder problems) {
      content = preserveCopyrightComments(content, $ -> !$.startsWith(COMMENT_MARKER__PRESERVE)
          ? COMMENT_MARKER__PRESERVE
          : EMPTY);

      var out = new ByteArrayOutputStream();
      try (var writer = new OutputStreamWriter(out, UTF_8)) {
        var reader = new StringReader(content);
        var compressor = new CssCompressor(reader);

        compressor.compress(writer, 500);
      } catch (IOException ex) {
        problems.append(getStackTrace(ex));
      }
      return out.size() > 0 ? out.toString(UTF_8) : null;
    }
  }

  /**
   * File type optimizer.
   *
   * @author Stefano Chizzolini
   */
  @FunctionalInterface
  private interface FileTypeOptimizer {
    @Nullable
    String optimize(String content, Path file, Context context, StringBuilder problems);
  }

  /**
   * Javascript optimizer.
   *
   * @author Stefano Chizzolini
   */
  private static class JavascriptOptimizer implements FileTypeOptimizer {
    /**
     * <a href=
     * "https://github.com/google/closure-compiler/wiki/Annotating-JavaScript-for-the-Closure-Compiler#license-preserve-description">Closure
     * Compiler tag for comment preservation</a>.
     */
    private static final String COMMENT_MARKER__PRESERVE = "@preserve";

    @Override
    public @Nullable String optimize(String content, Path file, Context context,
        StringBuilder problems) {
      content = preserveCopyrightComments(content, $ -> (!$.startsWith(S + STAR)
          /* NOTE: JSDoc tags are recognized only within double-star comments */ ? S + STAR
          : EMPTY) + SPACE + COMMENT_MARKER__PRESERVE + SPACE);

      Compiler.setLoggingLevel(Level.WARNING);
      var compiler = new Compiler();
      var externs = List.<SourceFile>of();
      var interns = List.of(SourceFile.fromCode(file.toString(), content));
      var options = new CompilerOptions();
      {
        options.setEnvironment(CompilerOptions.Environment.BROWSER);
        /*
         * NOTE: Strict mode becomes problematic in case of loosen third-party code, so we opt for
         * relaxed syntax.
         */
        options.setEmitUseStrict(false);
        /*
         * NOTE: The compiler emits a JSC_BAD_JSDOC_ANNOTATION warning when encounters a `@callback`
         * JSDoc tag, as it's not recognised; this option suppresses such noisy warning.
         */
        options.setExtraAnnotationNames(Set.of("callback"));
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
      }

      Result result = compiler.compile(externs, interns, options);
      {
        var problemFormatter = new LightweightMessageFormatter(compiler, SourceExcerpt.FULL);
        if (result.warnings != null) {
          for (JSError warning : result.warnings) {
            problems.append(problemFormatter.formatWarning(warning));
          }
        }
        if (result.errors != null) {
          for (JSError error : result.errors) {
            problems.append(problemFormatter.formatError(error));
          }
        }
      }
      return result.success ? compiler.toSource() : null;
    }
  }

  private static final String FILE_QUALIFIER__MINIFIED = ".min";

  private static final Pattern PATTERN__COPYRIGHT_COMMENT = Pattern.compile(
      "(/\\*)(.*?(?:copyright|\\(c\\)|©|license).*?\\*/)",
      Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

  /**
   * Ensures each copyright comment in the content is marked for preservation, in order to survive
   * the optimization.
   *
   * @param preserveMarkerProvider
   *          Provides the preservation marker to inject in the comment (empty, if already present).
   * @return Updated {@code content}.
   */
  private static String preserveCopyrightComments(String content,
      UnaryOperator<String> preserveMarkerProvider) {
    var matcher = PATTERN__COPYRIGHT_COMMENT.matcher(content);
    if (matcher.find()) {
      StringBuilder b = null;
      do {
        String preserveMarker = preserveMarkerProvider.apply(matcher.group(2));
        if (!preserveMarker.isEmpty()) {
          if (b == null) {
            b = new StringBuilder();
          }
          // Add preservation marker!
          matcher.appendReplacement(b, "$1" + preserveMarker + "$2");
        }
      } while (matcher.find());
      if (b != null) {
        matcher.appendTail(b);
        content = b.toString();
      }
    }
    return content;
  }

  private final Map<String, FileTypeOptimizer> fileTypeOptimizers = Map.of(
      FILE_EXTENSION__CSS, new CssOptimizer(),
      FILE_EXTENSION__JAVASCRIPT, new JavascriptOptimizer());
  private @LazyNonNull @Nullable Predicate<String> includedFilesFilter;

  /**
   */
  public FileOptimizer() {
    super(new TextSerializer());

    /*
     * Disable Java logging!
     *
     * NOTE: Java logging backs Closure Compiler, causing compilation events to pollute the Javadoc
     * diagnostics; instead, such events are intercepted by Jada and smoothly integrated into the
     * Javadoc logging system.
     */
    LogManager.getLogManager().reset();
    var globalLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    globalLogger.setLevel(Level.OFF);
  }

  /**
   * @return {@code 1_000}
   * @implNote This processor is expected to transform files whose contents are in their final
   *           state, after being fed to all the other processors.
   */
  @Override
  public int getPriority() {
    return 1_000;
  }

  @Override
  public void init(SystemConfig config) {
    super.init(config);

    ((TextSerializer) serializer).setCharset(config.getOutputCharset());
  }

  @Override
  public boolean isProcessable(Path file, FileProcess.Context context) {
    return !getConfig().isDebug()
        && fileTypeOptimizers.containsKey(extension(file).toLowerCase())
        && isFileIncluded(context.getBaseDir(file).relativize(file));
  }

  @Override
  protected @Nullable String processContent(String content, Path file, Context context) {
    var problems = new StringBuilder();
    var ret = fileTypeOptimizers.get(extension(file).toLowerCase())
        .optimize(content, file, context, problems);
    if (ret != null) {
      if (!problems.isEmpty()) {
        getLog().print(Kind.WARNING, this, JadaMessage.OPTIMIZATION_ISSUES, file, problems);
      }
      context.changeFile();
    } else {
      getLog().print(Kind.ERROR, this, JadaMessage.OPTIMIZATION_FAILED, file, problems);
    }
    return ret;
  }

  /**
   * @param file
   *          Relative file.
   */
  private boolean isFileIncluded(Path file) {
    // Already optimized?
    if (baseName(file).endsWith(FILE_QUALIFIER__MINIFIED))
      return false;

    if (includedFilesFilter == null) {
      includedFilesFilter = getConfig().getFileOptimizationFilter().toPredicate();
    }

    var resourceName = nonNull(ResourceNames.fromPath(file, null));
    return includedFilesFilter.test(resourceName);
  }
}
