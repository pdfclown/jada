/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (Diagram.java) is part of jada-uml module in Jada project
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

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.newOutputStream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.pdfclown.common.util.Chars.DOT;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.io.Files.cognateFile;
import static org.pdfclown.common.util.io.Files.relativize;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import javax.tools.Diagnostic.Kind;
import net.sourceforge.plantuml.FileFormat;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.LazyNonNull;
import org.pdfclown.common.util.io.IndentWriter;
import org.pdfclown.jada.uml.UmlConfig;
import org.pdfclown.jada.uml.internal.UmlMessage;
import org.pdfclown.jada.uml.internal.util.io.StringBufferWriter;
import org.pdfclown.jada.uml.render.generator.PlantumlGenerator;

// SourceName: nl.talsmasoftware.umldoclet.uml.Diagram
/**
 * Abstract UML Diagram class.
 *
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public abstract class Diagram extends UmlNode {
  private final UmlConfig config;
  private final PlantumlGenerator plantumlGenerator;
  private final FileFormat[] formats;
  private @LazyNonNull @Nullable Path diagramBaseFile;

  protected Diagram(UmlConfig config) {
    super(null);

    this.config = requireNonNull(config, "`config`");

    plantumlGenerator = PlantumlGenerator.getPlantumlGenerator(config);
    formats = config.getImageConfig().getFormats().stream()
        .map($ -> FileFormat.valueOf($.name()))
        .toArray(FileFormat[]::new);
  }

  @Override
  public UmlConfig getConfig() {
    return config;
  }

  /**
   */
  public void render() {
    // Skip empty diagram!
    if (!config.isEmptyDiagramRendered() && isEmpty()) {
      if (formats.length > 0) {
        config.getLog().print(Kind.OTHER, this, "Skipping empty diagram: {}",
            getDiagramFile(formats[0]));
      }
      return;
    }

    try {
      // 1. UML sources rendering.
      String plantumlSource = renderPlantumlSource();
      if (Link.updateBaseDirectory(getDiagramBaseFile().getParent()) || plantumlSource == null) {
        /*
         * NOTE: Must re-render in case of different link base paths.
         */
        plantumlSource = super.toString();
      }

      // 2. Diagrams rendering.
      for (FileFormat format : formats) {
        renderDiagramFile(plantumlSource, format);
      }
    } catch (IOException ex) {
      throw runtime("{} rendering FAILED", this, ex);
    } finally {
      Link.updateBaseDirectory(null);
    }
  }

  @Override
  public String toString() {
    final String name = getDiagramBaseFile().toString();
    return formats.length == 1 ? name + formats[0].getFileSuffix()
        : name + Stream.of(formats).map(FileFormat::getFileSuffix)
            .map($ -> $.substring(1))
            .collect(joining(",", ".[", "]"));
  }

  @Override
  public IndentWriter writeTo(IndentWriter out) throws IOException {
    out.append("@startuml").nl();
    out.indent();
    {
      writeCreditsComment(out);
      writeCustomDirectives(config.getPlantumlCustomDirectives(), out);
      writeChildrenTo(out);
    }
    out.undent();
    out.append("@enduml").nl();
    return out;
  }

  /**
   * PlantUML output file.
   */
  protected abstract Path getPlantUmlFile();

  protected IndentWriter writeCustomDirectives(List<String> customDirectives, IndentWriter out)
      throws IOException {
    if (!customDirectives.isEmpty()) {
      for (var e : customDirectives) {
        out.writeln(e);
      }
      out.nl();
    }
    return out;
  }

  private StringBufferWriter createBufferingPlantumlFileWriter(Path pumlFile) throws IOException {
    return new StringBufferWriter(new OutputStreamWriter(newOutputStream(pumlFile),
        config.getConfig().getOutputCharset()));
  }

  /**
   * Gets the diagram file stripped of its extension.
   *
   * @see #getDiagramFile(FileFormat)
   */
  private Path getDiagramBaseFile() {
    if (diagramBaseFile == null) {
      Path targetDir = config.getConfig().getOutputDirectory();
      Path relativeBaseFile = cognateFile(relativize(targetDir, getPlantUmlFile()), EMPTY);
      if (config.getImageConfig().getSubDirectory() != null) {
        var imageDir = targetDir.resolve(config.getImageConfig().getSubDirectory());
        diagramBaseFile = imageDir.resolve(relativeBaseFile.toString()
            .replace(File.separatorChar, DOT));
      } else {
        diagramBaseFile = targetDir.resolve(relativeBaseFile);
      }
    }
    return diagramBaseFile;
  }

  /**
   * Gets the diagram file in the given format.
   */
  private Path getDiagramFile(FileFormat format) {
    Path base = getDiagramBaseFile();
    return base.getParent().resolve(base.getFileName() + format.getFileSuffix());
  }

  private void renderDiagramFile(String plantumlSource, FileFormat format) throws IOException {
    final Path diagramFile = getDiagramFile(format);

    config.getLog().print(Kind.NOTE, this, UmlMessage.GENERATING_FILE, diagramFile);

    createDirectories(diagramFile.getParent());
    try (OutputStream out = newOutputStream(diagramFile)) {
      plantumlGenerator.generatePlantumlDiagramFromSource(plantumlSource, format, out);
    }
  }

  private @Nullable String renderPlantumlSource() throws IOException {
    return config.getConfig().isDebug() ? writePlantumlSourceToFile() : null;
  }

  private void writeCreditsComment(IndentWriter out) throws IOException {
    out.nl();
    out.writeln("""
        ' \
        Generated by JadaUML, the UML extension for Jada doclet \
        (https://github.com/pdfclown/jada) by pdfClown.org, \
        based on UMLDoclet (https://github.com/talsma-ict/umldoclet) by Talsma ICT, \
        containing parts of PlantUML (https://github.com/plantuml/plantuml) by Arnaud Roques.""");
    out.nl();
  }

  private String writePlantumlSourceToFile() throws IOException {
    Path pumlFile = getPlantUmlFile();
    config.getLog().print(Kind.NOTE, this, UmlMessage.GENERATING_FILE, pumlFile);

    createDirectories(pumlFile.getParent());
    Link.updateBaseDirectory(pumlFile.getParent());
    try (StringBufferWriter writer = createBufferingPlantumlFileWriter(pumlFile)) {
      writeTo(IndentWriter.of(writer, null));
      return writer.getBuffer().toString();
    }
  }
}
