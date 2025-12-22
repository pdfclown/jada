/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (IncludeDocFilter.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.internal;

import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Exceptions.wrongState;

import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.UnknownInlineTagTree;
import com.sun.source.util.SimpleDocTreeVisitor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.tools.Diagnostic.Kind;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.Ref;
import org.pdfclown.jada.core.Jada;
import org.pdfclown.jada.core.JadaEnvironment.JadaDocFilter;
import org.pdfclown.jada.core.taglet.IncludeTaglet;

/**
 * Content inclusion pre-process filter.
 * <p>
 * Replaces {@link IncludeTaglet @jada.include} tags with the corresponding Javadoc content.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class IncludeDocFilter extends JadaDocFilter {
  /**
   * <span class="warning">(For internal use only)</span>
   */
  public IncludeDocFilter() {
  }

  public IncludeDocFilter(Jada jada) {
    super(jada);
  }

  @Override
  public List<? extends DocTree> filterDocFragment(List<? extends DocTree> nodes,
      DocFragmentRole role, Object location) {
    var newNodesRef = new Ref<List<DocTree>>();
    new SimpleDocTreeVisitor<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitUnknownInlineTag(UnknownInlineTagTree node, @Nullable Void p) {
        if (node.getTagName().equals(IncludeTaglet.NAME)) {
          String includeFileName = node.getContent().get(0).toString();
          Path includeFile = getConfig().getResource(includeFileName)
              .orElseThrow(() -> wrongState("Include file \"{}\" NOT FOUND", includeFileName));

          String includeContent;
          try {
            includeContent = Files.readString(includeFile);
          } catch (IOException ex) {
            throw runtime("Content inclusion from \"{}\" FAILED", includeFile, ex);
          }

          replaceNode(includeContent, node);

          getLog().print(Kind.NOTE, this, JadaMessage.FILE_CONTENT_INCLUDED, includeFile);
        }
        return null;
      }

      void replaceNode(String newContent, DocTree oldNode) {
        List<? extends DocTree> newContentNodes = getEnv().getDocTrees()
            .getDocFragment(newContent);

        if (newNodesRef.isEmpty()) {
          newNodesRef.set(new ArrayList<>(nodes));
        }
        int index = newNodesRef.get().indexOf(oldNode);
        newNodesRef.get().remove(index);
        newNodesRef.get().addAll(index, newContentNodes);
      }
    }.visit(nodes, null);

    return newNodesRef.isPresent() ? newNodesRef.get() : nodes;
  }
}
