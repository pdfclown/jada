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
import static org.pdfclown.common.util.Exceptions.unexpected;
import static org.pdfclown.common.util.Objects.anyThat;
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
import java.util.Set;
import java.util.function.Predicate;
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
 * preserved applying the conventional minification preventer (that is, opening comment marker
 * followed by exclamation mark, {@code "/*!"}).
 * </p>
 * <p>
 * NOTE: In {@linkplain JadaConfig#isDebug() debug mode}, optimization is disabled to ease source
 * code inspection and stepping.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class FileOptimizer extends JadaFileProcessor<String> {
  private static final Pattern PATTERN__COPYRIGHT_COMMENT = Pattern.compile(
      "/\\*([^!].*?(?:copyright|\\(c\\)|©|license).*?\\*/)",
      Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

  private static final String FILE_QUALIFIER__MINIFIED = ".min";

  private @LazyNonNull @Nullable Predicate<String> includedFilesPredicate;

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
        && anyThat(extension(file), String::equalsIgnoreCase, FILE_EXTENSION__CSS,
            FILE_EXTENSION__JAVASCRIPT)
        && isFileIncluded(context.getBaseDir(file).relativize(file));
  }

  @Override
  protected @Nullable String processContent(String content, Path file, Context context) {
    // Ensure copyright notices preservation!
    var matcher = PATTERN__COPYRIGHT_COMMENT.matcher(content);
    if (matcher.find()) {
      var b = new StringBuilder();
      do {
        // Replace ordinary comment opening marker with preservation marker!
        matcher.appendReplacement(b, "/*!$1");
      } while (matcher.find());
      matcher.appendTail(b);
      content = b.toString();
    }

    String ret = null;
    var problems = new StringBuilder();
    String extension = extension(file).toLowerCase();
    switch (extension) {
      case FILE_EXTENSION__CSS: {
        var out = new ByteArrayOutputStream();
        try (var writer = new OutputStreamWriter(out, UTF_8)) {
          var reader = new StringReader(content);
          var compressor = new CssCompressor(reader);

          compressor.compress(writer, 500);
        } catch (IOException ex) {
          problems.append(getStackTrace(ex));
        }
        if (out.size() > 0) {
          ret = out.toString(UTF_8);
        }
      }
        break;
      case FILE_EXTENSION__JAVASCRIPT: {
        var externs = List.<SourceFile>of();
        var interns = List.of(SourceFile.fromPath(file, UTF_8));
        var options = new CompilerOptions();
        {
          options.setEnvironment(CompilerOptions.Environment.BROWSER);
          /*
           * NOTE: Strict mode becomes problematic in case of loosen third-party code, so we opt for
           * relaxed syntax.
           */
          options.setEmitUseStrict(false);
          /*
           * NOTE: The compiler emits a JSC_BAD_JSDOC_ANNOTATION warning when encounters a
           * `@callback` JSDoc tag, as it's not recognised; this option suppresses such noisy
           * warning.
           */
          options.setExtraAnnotationNames(Set.of("callback"));
          CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        }
        Compiler.setLoggingLevel(Level.WARNING);
        var compiler = new Compiler();

        Result result = compiler.compile(externs, interns, options);
        {
          var formatter = new LightweightMessageFormatter(compiler, SourceExcerpt.FULL);
          if (result.warnings != null) {
            for (JSError warning : result.warnings) {
              problems.append(formatter.formatWarning(warning));
            }
          }
          if (result.errors != null) {
            for (JSError error : result.errors) {
              problems.append(formatter.formatError(error));
            }
          }
        }
        if (result.success) {
          ret = compiler.toSource();
        }
      }
        break;
      default:
        throw unexpected(extension);
    }
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
      return false /* NOTE: By definition, already-optimized files are excluded */;

    if (includedFilesPredicate == null) {
      includedFilesPredicate = getConfig().getFileOptimizationFilter().toPredicate();
    }

    var resourceName = ResourceNames.fromPath(file, null);
    return includedFilesPredicate.test(resourceName);
  }
}
