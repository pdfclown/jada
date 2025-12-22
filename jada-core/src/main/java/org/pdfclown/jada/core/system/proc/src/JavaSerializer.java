/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JavaSerializer.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.system.proc.src;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaParserAdapter;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import org.pdfclown.jada.core.system.proc.FileSerializer;
import org.pdfclown.jada.core.system.proc.TextSerializer;
import org.pdfclown.jada.core.util.Encodable;

/**
 * Serializer for Java source files.
 *
 * @author Stefano Chizzolini
 */
public class JavaSerializer implements FileSerializer<CompilationUnit>, Encodable {
  private static final JavaParserAdapter PARSER = JavaParserAdapter.of(new JavaParser(
      new ParserConfiguration()
          .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)));

  private final TextSerializer base = new TextSerializer();

  @Override
  public CompilationUnit deserialize(Path file) {
    CompilationUnit obj = PARSER.parse(base.deserialize(file));
    LexicalPreservingPrinter.setup(obj);
    return obj;
  }

  @Override
  public Charset getCharset() {
    return base.getCharset();
  }

  @Override
  public void serialize(CompilationUnit obj, Path file) {
    base.serialize(LexicalPreservingPrinter.print(obj), file);
  }

  @Override
  public JavaSerializer setCharset(Charset value) {
    base.setCharset(value);
    return this;
  }
}
