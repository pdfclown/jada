/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaResourceAttach.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.proc;

import static org.pdfclown.common.util.Conditions.requireType;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.stream.Collectors.toReversedList;
import static org.pdfclown.jada.core.internal.JadaMessage.P__RESOURCE_DIR;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;
import javax.tools.Diagnostic.Kind;
import org.apache.commons.lang3.function.Failable;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pdfclown.common.util.annot.InitNonNull;
import org.pdfclown.jada.core.Jada;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.core.JadaConfig.Attachment;
import org.pdfclown.jada.core.JadaOperation;
import org.pdfclown.jada.core.internal.JadaMessage;
import org.pdfclown.jada.core.system.SystemConfig;
import org.pdfclown.jada.core.util.html.Jsoups;

/**
 * Post-process resource attach operation.
 * <p>
 * Useful to conveniently share resources in a multi-module project under common filesystem
 * directories (<code>"{@link JadaConfig#getResourceDirectories() %JADA-DIR%}/attach"</code>), along
 * with additional resources at {@linkplain JadaConfig#getAttachments() custom locations}.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class JadaResourceAttach implements JadaOperation<Void> {
  @SuppressWarnings("NotNullFieldNotInitialized")
  protected @InitNonNull JadaConfig config;

  @Override
  public JadaConfig getConfig() {
    return config;
  }

  @Override
  public Jada getJada() {
    return config.getJada();
  }

  @Override
  public void init(SystemConfig config) {
    this.config = requireType(config, JadaConfig.class, "config");
  }

  @Override
  public Void run() {
    var targetRoot = config.getOutputDirectory();
    attachDirectories(targetRoot);
    attachFiles(targetRoot);

    return null;
  }

  @Override
  public void term() {
  }

  protected void copyFile(Attachment attachment, Path targetFile, MutableInt counter) {
    doCopyFile(Failable.asRunnable(() -> {
      try (var in = attachment.getSource().openStream()) {
        Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
      }
    }), attachment.getSource().getUri().toString(), targetFile, counter);
  }

  protected void copyFile(Path sourceFile, Path targetFile, MutableInt counter) {
    doCopyFile(Failable.asRunnable(
        () -> Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING)),
        sourceFile.toString(), targetFile, counter);
  }

  /**
   * Attaches source directories.
   */
  private void attachDirectories(Path targetRoot) {
    final List<Path> sourceDirs = config.getResources("attach")
        .collect(toReversedList()) /*
                                    * Ensures the last one has the highest priority, so file copy
                                    * can overwrite lower-priority ones
                                    */;
    if (sourceDirs.isEmpty()) {
      getLog().print(Kind.NOTE, this, JadaMessage.RESOURCE_ATTACH_SKIPPED);
      return;
    }

    String javaVersionDirName = "java" + detectJavaStylesheetVersion();
    for (Path sourceDir : sourceDirs) {
      /*
       * NOTE: `sourceLocation` is the representation of `sourceDir` qualified with its filesystem
       * path, useful in case of embedded filesystems like ZIP, where `sourceDir` is a local path
       * inside an archive which isn't meaningful by itself; for example:
       *
       * sourceDir = "/org/pdfclown/common/build/conf/javadoc/jada/attach"
       *
       * filessytem =
       * "/home/stechio/.m2/repository/org/pdfclown/pdfclown-common-build/0.2.1-SNAPSHOT/pdfclown-common-build-0.2.1-SNAPSHOT.jar"
       *
       * sourceLocation =
       * "/org/pdfclown/common/build/conf/javadoc/jada/attach@/home/stechio/.m2/repository/org/pdfclown/pdfclown-common-build/0.2.1-SNAPSHOT/pdfclown-common-build-0.2.1-SNAPSHOT.jar"
       */
      String sourceLocation = sourceDir.getFileSystem().toString();
      sourceLocation = sourceDir + (sourceLocation.contains(File.separator) ? "@" + sourceLocation
          : EMPTY);
      getLog().print(Kind.NOTE, this, JadaMessage.RESOURCES_ATTACHING, sourceLocation, targetRoot);

      var counter = new MutableInt();
      Stream.of(
          sourceDir.resolve("common"),
          sourceDir.resolve(javaVersionDirName))
          .filter($ -> {
            if (Files.isDirectory($))
              return true;

            getLog().print(Kind.NOTE, this, JadaMessage.OBJECT_MISSING, P__RESOURCE_DIR, $);
            return false;
          })
          .forEachOrdered($ -> {
            try {
              Files.walkFileTree($, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                  Integer.MAX_VALUE,
                  new java.nio.file.SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                      copyFile(file, targetRoot.resolve(
                          $.relativize(file).toString() /*
                                                         * NOTE: Relativized path MUST be resolved
                                                         * as string in order not to cause
                                                         * `ProviderMismatchException`
                                                         */), counter);
                      return FileVisitResult.CONTINUE;
                    }
                  });
            } catch (IOException ex) {
              throw runtime(JadaMessage.RESOURCE_ATTACH_FAILED.toString(getConfig(), sourceDir,
                  targetRoot), ex);
            }
          });

      getLog().print(Kind.NOTE, this, JadaMessage.RESOURCE_ATTACH_COPIED_SUMMARY, counter,
          sourceDir,
          targetRoot);
    }
  }

  /**
   * Attaches additional source files.
   */
  private void attachFiles(Path targetRoot) {
    getLog().print(Kind.NOTE, this, JadaMessage.RESOURCES_ATTACHING_SINGLE);

    var counter = new MutableInt();
    for (Attachment attachment : config.getAttachments()) {
      copyFile(attachment, targetRoot.resolve(attachment.getTarget()), counter);
    }

    getLog().print(Kind.NOTE, this, JadaMessage.RESOURCE_ATTACH_SINGLE_COPIED_SUMMARY, counter,
        targetRoot);
  }

  /**
   * Detects the actual version of the default stylesheet provided by the javadoc tool.
   * <p>
   * This is crucial to select a coherent set of resources.
   * </p>
   *
   * @return {@code 0}, if the detection failed (unknown stylesheet version).
   */
  private int detectJavaStylesheetVersion() {
    try {
      String content = Files.readString(config.getOverviewOutputFile());
      var ret = doDetectJavaStylesheetVersion(content);
      if (ret > 0)
        return ret;

      /*
       * NOTE: Missing detection on overview file may be caused either by an unknown stylesheet
       * version or by placeholder overview file (i.e., empty file containing a redirect to a
       * package -- such collapse occurs if the project contains just a single package).
       */
      Document doc = Jsoups.parse(content);
      Element redirectLink = doc.selectFirst("link[rel=canonical]");
      // Empty overview with redirect to a package?
      if (redirectLink != null) {
        content = Files.readString(config.getOutputFile(redirectLink.attr("href"), EMPTY, null));
        ret = doDetectJavaStylesheetVersion(content);
      }
      if (ret == 0) {
        getLog().print(Kind.WARNING, this, JadaMessage.STYLESHEET_DETECT_FAILED);
      }
      return ret;
    } catch (IOException ex) {
      throw runtime(ex);
    }
  }

  private void doCopyFile(Runnable copier, String sourceFile, Path targetFile, MutableInt counter) {
    try {
      Files.createDirectories(targetFile.getParent());
    } catch (IOException ex) {
      throw runtime(ex);
    }

    copier.run();
    counter.increment();

    getLog().print(Kind.NOTE, this, JadaMessage.RESOURCE_ATTACH_COPIED, sourceFile, targetFile);
  }

  /**
   * @implNote Currently there are two modern versions of the default stylesheet (11 and 17),
   *           denoted by a change in the CSS class naming convention (from camel to kebab case).
   */
  private int doDetectJavaStylesheetVersion(String content) {
    if (content.indexOf("legalCopy") > 0)
      return 11;
    else if (content.indexOf("legal-copy") > 0)
      return 17;
    else
      return 0;
  }
}
