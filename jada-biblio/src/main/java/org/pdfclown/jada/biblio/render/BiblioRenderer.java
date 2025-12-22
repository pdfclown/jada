/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (BiblioRenderer.java) is part of jada-biblio module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.biblio.render;

import static java.nio.file.Files.writeString;
import static java.util.Objects.requireNonNull;
import static org.pdfclown.common.util.Chars.HYPHEN;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Strings.S;
import static org.pdfclown.common.util.io.Files.FILE_EXTENSION__XSL;
import static org.pdfclown.common.util.xml.Xmls.fragmentTransformer;
import static org.pdfclown.jada.biblio.internal.Internals.FILE_PREFIX__BIBLIO_1_0;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.tools.Diagnostic.Kind;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.pdfclown.jada.biblio.BiblioConfig;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.core.util.html.Jsoups;

/**
 * Bibliography renderer.
 * <p>
 * Transforms the {@linkplain BiblioConfig#getBiblio() bibliographic data} to the
 * {@linkplain JadaConfig#getOutputFormat() target format}, and generates the corresponding
 * {@linkplain BiblioConfig#getBiblioOutputFile() output file} using the
 * {@linkplain JadaConfig#getOverviewOutputFile() Overview} page as template and linking the Index
 * page to it.
 * </p>
 *
 * @author Stefano Chizzolini
 */
public class BiblioRenderer {
  /**
   * Biblio XSL filename format.
   * <p>
   * Parameters:
   * </p>
   * <ol>
   * <li>output file format's simple name (for example, {@code "html"})</li>
   * </ol>
   */
  private static final String FORMAT__BIBLIO_XSL =
      FILE_PREFIX__BIBLIO_1_0 + HYPHEN + "%s" + FILE_EXTENSION__XSL;

  /**
   * Renders the bibliography according to the configuration.
   */
  public void render(BiblioConfig biblioConfig) {
    try {
      Path overviewFile = biblioConfig.getConfig().getOverviewOutputFile();
      Path biblioFile = biblioConfig.getBiblioOutputFile();

      Document doc = Jsoups.parse(overviewFile);

      // Replace page title!
      {
        Element title = requireNonNull(doc.selectFirst("html > head > title"), "`title`");
        String oldTitle = title.text();
        String newTitle = oldTitle.replace("Overview", "Bibliography");

        title.text(newTitle);
        /*
         * NOTE: javadoc 11 dynamically enforces title through script (ugh!), so we have to update
         * it too (it would have been much straightforward to do a global replace on doc.html(), but
         * unfortunately doc.html(..) setter is buggy (weirdly removes <html> <head> and <body>
         * tags!)).
         */
        doc.select("script:containsData(%s)".formatted(oldTitle))
            .forEach($ -> $.childNodes().stream()
                .map(DataNode.class::cast)
                .forEach($$ -> $$.setWholeData($$.getWholeData().replace(oldTitle, newTitle))));
      }

      // Deactivate overview navigation buttons!
      {
        Elements elements = doc.select(S
            + "li[class=navBarCell1Rev]," /* javadoc 11 */
            + "li[class=nav-bar-cell1-rev]" /* javadoc 17 */);
        if (!elements.isEmpty()) {
          elements.forEach($ -> {
            $.clearAttributes();
            $.html("<a href=\"index.html\">Overview</a>");
          });
        } else {
          biblioConfig.getLog().print(Kind.WARNING, "Overview navigation buttons NOT FOUND (may "
              + "be due to an unsupported javadoc stylesheet version)");
        }
      }

      // Replace body!
      {
        Element main = doc.selectFirst("main");
        if (main == null)
          throw runtime("main block NOT FOUND in {}", overviewFile);

        var targetStream = new ByteArrayOutputStream();
        {
          var source = new DOMSource(biblioConfig.getBiblio());
          var target = new StreamResult(targetStream);
          var style = new StreamSource(getClass().getResourceAsStream(
              FORMAT__BIBLIO_XSL.formatted("html")));
          try {
            fragmentTransformer(style).transform(source, target);
          } catch (TransformerException ex) {
            throw runtime(ex);
          }
        }
        String biblioFragment = targetStream.toString(StandardCharsets.UTF_8);

        main.html(biblioFragment);
      }

      writeString(biblioFile, doc.outerHtml());

      updatePages(biblioConfig.getConfig().getOutputDirectory(),
          biblioConfig.getBiblioOutputFile());
    } catch (IOException ex) {
      throw runtime(ex);
    }
  }

  private void updatePages(Path outputPath, Path biblioOutputFile) throws IOException {
    // Index page.
    Path indexFile = outputPath.resolve("index-all.html");
    if (Files.isRegularFile(indexFile)) {
      Document doc = Jsoups.parse(indexFile);

      /*
       * Bibliography links.
       *
       * NOTE: The Index page typically contains a summary structure, repeated on header and
       * (possibly) footer, with a one-line sequence of page links (whose first one is typically
       * "allclasses-index.html") separated by nodes which may vary according to the javadoc version
       * (for example, in javadoc 11 it is just a bare space character, whilst in javadoc 17 it is a
       * pipe character within a styled span element): the bibliography link must be appended to
       * such sequences.
       */
      doc.select("a[href=allclasses-index.html]").forEach($ -> {
        /*
         * Fetching separator nodes to reuse for bibliography link...
         */
        var separatorNodes = new ArrayList<Node>();
        Node node = $;
        while (true) {
          node = requireNonNull(node.nextSibling(), "`node`");
          // Separator ended (next page link reached)?
          if (node instanceof Element element && element.tagName().equals("a")) {
            break;
          }

          separatorNodes.add(node);
        }

        /*
         * Skipping all separator-link pairs to append the bibliography link...
         */
        int separatorNodesIndex = 0;
        while (true) {
          Node nextNode = node.nextSibling();
          // Link?
          if (separatorNodesIndex == separatorNodes.size()) {
            if (!(nextNode instanceof Element element) || !element.tagName().equals("a"))
              throw runtime("Unexpected node (should be <a>): {}", nextNode);

            // Start next separator-link pair!
            separatorNodesIndex = 0;
          }
          // Separator node?
          else if (nextNode != null
              && Jsoups.equal(nextNode, separatorNodes.get(separatorNodesIndex))) {
            // Move to next separator node!
            separatorNodesIndex++;
          }
          // Separator-link pairs sequence ended.
          else {
            if (separatorNodesIndex > 0)
              throw runtime("Unexpected separator node (should be '{}'): '{}'",
                  separatorNodes.get(separatorNodesIndex), nextNode);

            Path relBiblioOutputFile = indexFile.getParent().relativize(biblioOutputFile);

            // Append bibliography link!
            for (Node e : separatorNodes) {
              node.after(e = e.clone());
              node = e;
            }
            node.after("<a href=\"%s\">Bibliography</a>".formatted(relBiblioOutputFile));
            break;
          }
          node = nextNode;
        }
      });

      writeString(indexFile, doc.outerHtml());
    }
  }
}
