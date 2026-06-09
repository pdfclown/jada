/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (BiblioExtension.java) is part of jada-biblio module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.biblio;

import static java.nio.file.Files.isRegularFile;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.pdfclown.common.util.Exceptions.runtime;
import static org.pdfclown.common.util.Strings.EMPTY;
import static org.pdfclown.common.util.Strings.lineEnd;
import static org.pdfclown.common.util.Strings.lineStart;
import static org.pdfclown.common.util.function.Functions.to;
import static org.pdfclown.common.util.io.Files.isExtension;
import static org.pdfclown.common.util.stream.Collectors.toReversedList;
import static org.pdfclown.common.util.system.Clis.parseListIncremental;
import static org.pdfclown.jada.biblio.BiblioConfig.OPTION__STATIC_ENTRIES;
import static org.pdfclown.jada.biblio.BiblioConfig.OPTION__USED_ENTRIES_ONLY;
import static org.pdfclown.jada.biblio.internal.Internals.TAG_PREFIX__BIBLIO;
import static org.pdfclown.jada.biblio.internal.Internals.XML_ATTR__EDITION;
import static org.pdfclown.jada.biblio.internal.Internals.XML_ATTR__ID;
import static org.pdfclown.jada.biblio.internal.Internals.XML_ATTR__NAME;
import static org.pdfclown.jada.biblio.internal.Internals.XML_ATTR__VERSION;
import static org.pdfclown.jada.biblio.internal.Internals.XML_TAG__DOC;
import static org.pdfclown.jada.biblio.internal.Internals.XML_TAG__PART;
import static org.pdfclown.jada.biblio.internal.Internals.XML_TAG__REF;
import static org.pdfclown.jada.biblio.internal.Internals.XML_TAG__SEE;
import static org.pdfclown.jada.biblio.internal.Internals.XML_TAG__SPEC;
import static org.pdfclown.jada.biblio.internal.Internals.XML_TAG__VERSION;
import static org.pdfclown.jada.core.util.Objects.realSubTypes;
import static org.pdfclown.jada.core.util.lang.Javadocs.PATTERN_GROUP__INLINE_TAG__VALUE;
import static org.pdfclown.jada.core.util.lang.Javadocs.fileObject;
import static org.pdfclown.jada.core.util.lang.Javadocs.inlineTagName;
import static org.pdfclown.jada.core.util.lang.Javadocs.inlineTagPattern;
import static org.pdfclown.jada.core.util.lang.Javadocs.normal;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import org.apache.commons.lang3.function.Failable;
import org.jspecify.annotations.Nullable;
import org.pdfclown.common.util.annot.InitNonNull;
import org.pdfclown.common.util.xml.Xmls;
import org.pdfclown.jada.biblio.BiblioConfig.BiblioEntry;
import org.pdfclown.jada.biblio.internal.BiblioMessage;
import org.pdfclown.jada.biblio.render.BiblioRenderer;
import org.pdfclown.jada.biblio.taglet.BiblioTaglet;
import org.pdfclown.jada.biblio.taglet.BiblioTaglet.BiblioRef;
import org.pdfclown.jada.biblio.util.Biblios;
import org.pdfclown.jada.core.Jada;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.core.JadaExtension;
import org.pdfclown.jada.core.JadaOptions;
import org.pdfclown.jada.core.event.MainProcessEvent;
import org.pdfclown.jada.core.event.PostProcessEvent;
import org.pdfclown.jada.core.system.Message;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * {@linkplain Jada Jada doclet} extension for bibliographies.
 * <p>
 * Integrates a bibliography (or reference list) into Javadoc, along with corresponding citation
 * tags.
 * </p>
 * <p>
 * This solution provides a more flexible alternative to the standard (Java 20+)
 * <a href="https://bugs.openjdk.org/browse/JDK-8226279">{@code @spec} tag</a>: bibliographic
 * entries are listed in one or more reusable data files and cited by ID via
 * {@linkplain BiblioTaglet dedicated Javadoc tags}; they denote various documentation sources
 * ({@linkplain org.pdfclown.jada.biblio.taglet.SpecTaglet specification},
 * {@linkplain org.pdfclown.jada.biblio.taglet.DocTaglet non-normative documentation},
 * {@linkplain org.pdfclown.jada.biblio.taglet.RefTaglet generic reference}) and are richly
 * described (versions, parts, public identifiers (such as ISO codes), authors, publishers, ...).
 * </p>
 * <p>
 * Citation tags can be placed everywhere (Javadoc comments, simple comments and wherever they don't
 * conflict with source code compilation — for example, {@link Exception} messages and log entries):
 * the bibliography generator automatically scans the source code to detect citations and to report
 * broken ones. The drawback about custom tags is that IDEs aren't aware of them, so, without
 * dedicated plugins, Javadoc previews cannot resolve them; moreover, they are not integrated into
 * Javadoc search functionality.
 * </p>
 * <p>
 * The bibliography is generated from bibliographic entries defined in <code>"{@link
 * JadaConfig#OPTION__RESOURCE_DIR %JADA-DIR%}/ext/JadaBiblio/biblio.xml"</code>. By default, all
 * such entries are inserted into the generated bibliography; in order to insert only actually
 * referenced ones (that is, those cited in source code),
 * {@value BiblioConfig#OPTION__USED_ENTRIES_ONLY} option is available. The bibliography is rendered
 * as an additional Index page, similarly to the standard {@code @spec} tag.
 * </p>
 * <h4>Usage</h4>
 * <p>
 * To generate a bibliography in your project's Javadoc, follow these steps:
 * </p>
 * <ol>
 * <li>configure the <b>javadoc tool</b> in your build system:
 * <ul>
 * <li>Maven — in the {@code pom.xml} file of your project, add to {@code maven-javadoc-plugin} the
 * following configuration:
 * <ol>
 * <li><b>{@linkplain Jada Jada doclet}</b></li>
 * <li><b>bibliographic artifact</b>:<pre class="lang-xml" data-line="2-6,9-10"><code>
 * &lt;docletArtifacts&gt;
 *   <span style="background-color:yellow;color:black;">&lt;artifact&gt;
 *     &lt;groupId&gt;org.pdfclown&lt;/groupId&gt;
 *     &lt;artifactId&gt;jada-biblio&lt;/artifactId&gt;
 *     &lt;version&gt;${jada.version}&lt;/version&gt;
 *   &lt;/artifact&gt;</span>
 * &lt;/docletArtifacts&gt;
 * &lt;additionalOptions&gt;
 *   <span style=
"background-color:yellow;color:black;">&lt;option&gt;--jada-exts JadaBiblio&lt;/option&gt;
 *   &lt;option&gt;--biblio-used-only true&lt;/option&gt;</span>
 * &lt;/additionalOptions&gt;</code></pre></li>
 * <li><b>bibliographic taglets</b>:<pre class="lang-xml" data-line="2-15"><code>
 * &lt;taglets&gt;
 *   <span style="background-color:yellow;color:black;">&lt;taglet&gt;
 *     &lt;tagletClass&gt;org.pdfclown.jada.biblio.taglet.DocTaglet&lt;/tagletClass&gt;
 *     &lt;tagletArtifact&gt;
 *       &lt;groupId&gt;org.pdfclown&lt;/groupId&gt;
 *       &lt;artifactId&gt;jada-biblio&lt;/artifactId&gt;
 *       &lt;version&gt;${jada.version}&lt;/version&gt;
 *     &lt;/tagletArtifact&gt;
 *   &lt;/taglet&gt;
 *   &lt;taglet&gt;
 *     &lt;tagletClass&gt;org.pdfclown.jada.biblio.taglet.RefTaglet&lt;/tagletClass&gt;
 *   &lt;/taglet&gt;
 *   &lt;taglet&gt;
 *     &lt;tagletClass&gt;org.pdfclown.jada.biblio.taglet.SpecTaglet&lt;/tagletClass&gt;
 *   &lt;/taglet&gt;</span>
 * &lt;/taglets&gt;</code></pre></li>
 * </ol>
 * </li>
 * </ul>
 * </li>
 * <li>define a <b>bibliography data file</b> at <code>"{@link
 * JadaConfig#OPTION__RESOURCE_DIR %JADA-DIR%}/ext/JadaBiblio/biblio.xml"</code> (namespace:
 * "{@code https://pdfclown.org/ns/biblio}", version: "{@code 1.0}"), adding entries as needed</li>
 * <li>add <b>{@linkplain BiblioTaglet bibliographic tags}</b> to your source code, referencing
 * existing bibliographic entries by <b>full ID</b> (see "Bibliographic IDs" section here below for
 * further information)</li>
 * </ol>
 * <h5>Bibliographic IDs</h5>
 * <p>
 * Bibliographic IDs have twofold meaning:
 * </p>
 * <ul>
 * <li><b>simple IDs</b> (also called <b>ID segments</b>) <i>on declaration (that is, within
 * {@code biblio.xml})</i>, as {@code id} attributes of {@code <spec>}, {@code <doc>},
 * {@code <ref>}, {@code <part>}, {@code <version>} elements, and {@code version} attributes of
 * {@code <spec>}, {@code <doc>}, {@code <ref>}, {@code <part>} elements, <i>to identify a
 * bibliographic entry within its hierarchy</i>.
 * <p>
 * They SHALL conform to the following requirements:
 * </p>
 * <ul>
 * <li>use only upper-case latin letters ({@code A-Z}), numbers ({@code 0-9}), point ({@code .}),
 * hyphen ({@code -}), tilde ({@code ~}).
 * <p>
 * The tilde has a special behavior: it excludes its suffix from label representation (that is, HTML
 * {@code title} attribute) — useful to mark a version identifier with an internal suffix to
 * distinguish multiple concurrent editions of the same specification (for example,
 * {@code "PDF:1.7"} (ISO edition of PDF 1.7) and {@code "PDF:1.7~ADB"} (Adobe Inc.'s proprietary
 * edition of PDF 1.7))
 * </p>
 * </li>
 * <li>convert any separator (such as {@code ' '}, {@code '/'}, {@code ':'}) to {@code '-'} (for
 * example, {@code "PDF/UA"} to {@code "PDF-UA"}; {@code "CODE 128"} to {@code "CODE-128"})</li>
 * </ul>
 * </li>
 * <li><b>full IDs</b> <i>on usage (that is, within Javadoc comments)</i>, concatenating ID segments
 * <i>to qualify a reference to a bibliographic entry across {@code biblio.xml}</i>.
 * <p>
 * They SHALL conform to this syntax {@biblio.spec W3C-EBNF}:
 * </p>
 * <pre class="lang-ebnf"><code>
 * FullId ::= Id ( '/' Part )? ( ':' Version )?</code></pre>
 * <p>
 * where each ID segment (Id, Part, Version) corresponds to the {@code id} attribute of the
 * respective entry in {@code biblio.xml}, while Version can alternatively correspond to the
 * {@code version} attribute.
 * </p>
 * <p>
 * Examples:
 * </p>
 * <ul>
 * <li>{@code "PDF"} means bibliographic entry "PDF" (generic reference to the corpus of
 * bibliographic entries under that identifier)</li>
 * <li>{@code "PDF:2.0"} means bibliographic entry "PDF", version "2.0"</li>
 * <li>{@code "XMP/1"} means bibliographic entry "XMP", part "1"</li>
 * <li>{@code "XMP/1:2014"} means bibliographic entry "XMP", part "1", version "2014"</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author Stefano Chizzolini
 */
public class BiblioExtension extends JadaExtension {
  public static final String NAME = "JadaBiblio";

  private static final Pattern PATTERN__BIBLIO_ID = Pattern.compile(
      "^" + BiblioRef.REGEX__SEGMENT + "$");

  /**
   * Same as {@link org.pdfclown.jada.core.util.lang.Javadocs#inlineTagValue(Matcher)}, but supports
   * also tags in regular comments, outside Javadoc.
   */
  private static String inlineTagValue(Matcher matcher) {
    return normal(matcher.group(PATTERN_GROUP__INLINE_TAG__VALUE)
        .replace("//", EMPTY) /*
                               * Removes regular comment line markers in case the inline tag is
                               * spread over adjacent lines (the assumption is that "//" is NEVER
                               * present in valid bibliographic references)
                               */);
  }

  @SuppressWarnings("NotNullFieldNotInitialized")
  private @InitNonNull BiblioConfig extConfig;

  @Override
  public BiblioConfig getExtConfig() {
    return extConfig;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void init(JadaOptions options, Jada jada) {
    super.init(options, jada);

    extConfig = new BiblioConfig(this);

    options
        .add(OPTION__STATIC_ENTRIES, List.of("<entryId>(,<entryId>)*"),
            $args -> parseListIncremental($args.get(0), identity(),
                extConfig.getStaticBiblioEntryIds()))
        .add(OPTION__USED_ENTRIES_ONLY, List.of(),
            $args -> extConfig.setUsedBiblioEntriesOnly(true));
  }

  @Override
  public void onMainProcess(MainProcessEvent event) {
    loadBiblio();
  }

  @Override
  public void onPostProcess(PostProcessEvent event) {
    if (extConfig.getBiblio() != null) {
      if (extConfig.getBiblio().getDocumentElement().getChildNodes().getLength() > 0) {
        var renderer = new BiblioRenderer();
        renderer.render(extConfig);
      } else {
        getLog().print(Kind.NOTE, this, "Bibliography NOT rendered (EMPTY)");
      }
    }
  }

  /**
   * Populates bibliographic entry labels from the bibliographic element.
   *
   * @param e
   *          Source element.
   * @param parentId
   *          Parent element ID.
   * @param parentTitle
   *          Parent title.
   * @param biblioEntries
   *          Bibliographic entries.
   */
  private void buildBiblioEntries(Element e, @Nullable String parentId,
      String parentTitle, Map<String, BiblioEntry> biblioEntries) {
    requireNonNull(parentTitle, "`parentTitle`");

    final var localName = e.getLocalName();
    switch (localName) {
      // Publication tags.
      case //
          XML_TAG__DOC, //
          XML_TAG__PART, //
          XML_TAG__REF, //
          XML_TAG__SPEC, //
          XML_TAG__VERSION -> {
        // NOP
      }
      // Non-publication tags.
      case XML_TAG__SEE -> {
        return;
      }
      // Unknown tags.
      default -> {
        getLog().print(Kind.WARNING, this, BiblioMessage.BIBLIO_DATA_ELEMENT_UNKNOWN, localName,
            abbreviate(Xmls.toString(e), 80));
        return;
      }
    }

    String id = e.getAttribute(XML_ATTR__ID);
    // Bibliographic ID INVALID?
    if (!PATTERN__BIBLIO_ID.matcher(id).find())
      throw runtime(BiblioMessage.BIBLIO_ENTRY_ID_INVALID.toString(getConfig(), id,
          PATTERN__BIBLIO_ID.pattern()));

    if (parentId != null) {
      var b = new StringBuilder(parentId);
      switch (localName) {
        case XML_TAG__PART -> b.append(BiblioRef.SEPARATOR__PART);
        case XML_TAG__VERSION -> b.append(BiblioRef.SEPARATOR__VERSION);
        default -> {
          // NOP
        }
      }
      id = b.append(id).toString();
    }

    String title = e.hasAttribute("title") ? e.getAttribute("title") : parentTitle;
    String label;
    {
      var b = new StringBuilder(title);
      if (e.hasAttribute(XML_ATTR__VERSION)
          || localName.equals(XML_TAG__VERSION)) {
        String version = e.hasAttribute(XML_ATTR__VERSION)
            ? e.getAttribute(XML_ATTR__VERSION)
            : e.getAttribute(XML_ATTR__ID);
        {
          //  Internal suffix.
          int pos = version.indexOf('~');
          if (pos >= 0) {
            version = version.substring(0, pos);
          }
        }
        b.append(", version ").append(version);
      }
      if (e.hasAttribute(XML_ATTR__EDITION)) {
        b.append(", ed. ").append(e.getAttribute(XML_ATTR__EDITION));
      }
      if (e.hasAttribute(XML_ATTR__NAME)) {
        b.append(" (").append(e.getAttribute(XML_ATTR__NAME)).append(")");
      }
      label = b.toString();
    }

    biblioEntries.put(id, new BiblioEntry(e, label));

    e = (Element) e.getFirstChild();
    while (e != null) {
      buildBiblioEntries(e, id, title, biblioEntries);
      e = (Element) e.getNextSibling();
    }
  }

  /**
   * Loads bibliographic data into {@link BiblioConfig#getBiblio()}, along with its entries.
   * <p>
   * Bibliographic data is expressed as XML documents located at
   * <code>"{@link JadaConfig#OPTION__RESOURCE_DIR %JADA-DIR%}/ext/JadaBiblio/biblio.xml"</code>
   * under the respective resource directory, and loaded according to their configuration order (the
   * later its loading, the higher its priority); in case of ID collision between bibliographic
   * entries from different sources, the one with higher priority overrides the other.
   * </p>
   */
  private void loadBiblio() {
    final List<Path> biblioSourceFiles = extConfig.getResources("biblio.xml")
        .collect(toReversedList()) /*
                                    * Ensures the last one has the highest priority, so
                                    * bibliographic entries can override lower-priority ones
                                    */;
    if (biblioSourceFiles.isEmpty()) {
      getLog().print(Kind.WARNING, this, BiblioMessage.BIBLIO_NOT_FOUND);
      return;
    }

    Document biblio = null;
    final var biblioEntryIds = new HashMap<String, Set<String>>();
    {
      final var biblioEntryIndices = new HashMap<String, Integer>();
      for (Path biblioSourceFile : biblioSourceFiles) {
        Document biblioPart;
        try {
          biblioPart = Biblios.biblio(biblioSourceFile);
        } catch (IOException | SAXException ex) {
          throw runtime("Bibliography data load from {} FAILED", biblioSourceFile, ex);
        }

        try {
          loadBiblioPart(biblioPart, biblio, biblioEntryIds, biblioEntryIndices);
        } catch (RuntimeException ex) {
          throw runtime(BiblioMessage.BIBLIO_LOAD_FAILED.toString(getConfig(), biblioSourceFile),
              ex);
        }

        getLog().print(Kind.NOTE, this, BiblioMessage.BIBLIO_LOADED, biblioSourceFile);

        if (biblio == null) {
          biblio = biblioPart;
        }
      }
    }
    extConfig.setBiblio(biblio);

    // Load the bibliographic entries!
    loadBiblioEntries(biblioEntryIds);
  }

  /**
   * Loads the bibliographic entries, and scans source files looking for citations (that is,
   * bibliographic references matching the bibliographic entries).
   * <p>
   * If {@linkplain BiblioConfig#isUsedBiblioEntriesOnly() only the used entries} are requested to
   * be rendered, their citations are harvested from the source code and included, along with the
   * {@linkplain BiblioConfig#getStaticBiblioEntryIds() static ones}, in the bibliography;
   * otherwise, {@linkplain BiblioConfig#getBiblio() all the entries} are included in the
   * bibliography.
   * </p>
   *
   * @param biblioEntryIds
   *          Bibliographic entry ID sets by entry type ("doc", "ref", "spec", {@code null} (common
   *          superset)).
   */
  private void loadBiblioEntries(final Map<String, Set<String>> biblioEntryIds) {
    final Element biblioRoot = requireNonNull(extConfig.getBiblio(), "`extConfig.getBiblio()`")
        .getDocumentElement();
    final NodeList biblioEntryNodes = biblioRoot.getChildNodes();

    /*
     * BIBLIOGRAPHIC ENTRIES BUILDING
     */
    final var biblioEntries = new HashMap<String, BiblioEntry>();
    final var usedBiblioEntryIds = extConfig.isUsedBiblioEntriesOnly()
        ? new HashSet<String>()
        : null;
    {

      /*
       * Build bibliographic entries (main and descendants)!
       */
      for (int i = 0; i < biblioEntryNodes.getLength(); i++) {
        buildBiblioEntries((Element) biblioEntryNodes.item(i), null, EMPTY, biblioEntries);
      }

      // Scan source code for citations!
      scanSource(biblioEntries, biblioEntryIds, usedBiblioEntryIds);
    }

    /*
     * BIBLIOGRAPHIC ENTRIES FILTERING
     */
    if (usedBiblioEntryIds != null) {
      Set<String> staticBiblioEntryIds = extConfig.getStaticBiblioEntryIds();
      {
        // Validate static bibliographic entry IDs!
        var allBiblioEntryIds = biblioEntryIds.get(null);
        staticBiblioEntryIds.forEach($ -> {
          if (!allBiblioEntryIds.contains($)) {
            getLog().print(Kind.WARNING, this, BiblioMessage.BIBLIO_STATIC_ENTRY_MISSING, $);
          }
        });
      }

      for (int i = 0; i < biblioEntryNodes.getLength();) {
        var biblioEntryElement = (Element) biblioEntryNodes.item(i);
        var biblioEntryId = biblioEntryElement.getAttribute(XML_ATTR__ID);
        /*
         * Alive entry?
         *
         * NOTE: Entries are alive if static (that is, pinned, always rendered) or explicitly cited
         * in the source code.
         */
        if (staticBiblioEntryIds.contains(biblioEntryId)
            || usedBiblioEntryIds.contains(biblioEntryId)) {
          i++;
        }
        // Dead entry.
        else {
          // Purge bibliography from dead entry!
          biblioRoot.removeChild(biblioEntryElement);
        }
      }
    }

    extConfig.setBiblioEntries(biblioEntries);
  }

  /**
   * Loads the bibliographic data part into the main data.
   *
   * @param biblioPart
   *          Bibliographic data part to load.
   * @param biblio
   *          Main bibliographic data. Will be populated, if defined, with {@code biblioPart}
   *          entries.
   * @param biblioEntryIds
   *          Bibliographic entry ID sets by entry type ("doc", "ref", "spec", {@code null} (common
   *          superset)). Will be populated with the IDs of the loaded entries.
   * @param biblioEntryIndices
   *          Bibliographic entry indices by ID. Will be populated with the indices of the loaded
   *          entries.
   */
  private void loadBiblioPart(Document biblioPart, @Nullable Document biblio,
      Map<@Nullable String, Set<String>> biblioEntryIds, Map<String, Integer> biblioEntryIndices) {
    Element biblioRoot = null;
    NodeList biblioEntryNodes = null;
    int lastBiblioEntryIndex = 0;
    if (biblio != null) {
      biblioRoot = biblio.getDocumentElement();
      biblioEntryNodes = biblioRoot.getChildNodes();
      lastBiblioEntryIndex = biblioEntryNodes.getLength() - 1;
    }
    var allBiblioEntryIds = biblioEntryIds.computeIfAbsent(null, $ -> new HashSet<>());
    final var partEntryIds = new HashSet<String>();
    final NodeList partEntryNodes = biblioPart.getDocumentElement().getChildNodes();
    for (int i = 0; i < partEntryNodes.getLength(); i++) {
      var partEntryElement = (Element) partEntryNodes.item(i);
      var partEntryId = partEntryElement.getAttribute(XML_ATTR__ID);

      // Bibliographic entry duplicate?
      if (!partEntryIds.add(partEntryId))
        throw runtime(BiblioMessage.BIBLIO_ENTRY_DUPLICATE.toString(getConfig(), partEntryId));

      if (biblio != null) {
        Node newNode = biblio.importNode(partEntryElement, true);

        // Entry to merge?
        if (biblioEntryIndices.containsKey(partEntryId)) {
          Node oldNode = biblioEntryNodes.item(biblioEntryIndices.get(partEntryId));
          biblioRoot.insertBefore(newNode, oldNode);
          biblioRoot.removeChild(oldNode);

          getLog().print(Kind.NOTE, this, BiblioMessage.BIBLIO_ENTRY_OVERRIDDEN, partEntryId);
        }
        // Entry to add.
        else {
          biblioRoot.appendChild(newNode);
          biblioEntryIndices.put(partEntryId, lastBiblioEntryIndex++);
        }
      } else {
        biblioEntryIndices.put(partEntryId, lastBiblioEntryIndex++);
      }

      biblioEntryIds.computeIfAbsent(partEntryElement.getLocalName(), $ -> new HashSet<>())
          .add(partEntryId);
      allBiblioEntryIds.add(partEntryId);
    }
  }

  /**
   * Scans source files looking for citations (that is, bibliographic references).
   * <p>
   * Citations are validated both syntactically (legal representation) and semantically
   * (bibliographic entry matching).
   * </p>
   *
   * @param biblioEntryIds
   *          Bibliographic entry ID sets by entry type ("doc", "ref", "spec", {@code null} (common
   *          superset)).
   * @implNote Because of the intrinsic limitations of the source trees provided by the Javadoc tool
   *           (Javadoc comments of excluded elements and non-Javadoc comments are ignored),
   *           bibliographic reference validation and extraction cannot rely on them. Instead,
   *           references are harvested in the most relaxed manner, without syntactic evaluation:
   *           they may belong to Javadoc, to simple comments, to string literals, ..., whatever may
   *           be the scope of their code blocks. This makes for quite neat solutions, such as the
   *           inclusion of a bibliographic reference in an exception message (the end user will be
   *           able to conveniently retrieve the document associated to that reference in the
   *           bibliography shipped inside the generated Javadoc).
   */
  private void scanSource(Map<String, BiblioEntry> biblioEntries,
      Map<@Nullable String, Set<String>> biblioEntryIds, @Nullable Set<String> usedBiblioEntryIds) {
    // Map entry ID sets by tag!
    final Map<@Nullable String, Set<String>> biblioTagEntryIds = biblioEntryIds.entrySet().stream()
        .collect(toMap(
            $ -> to($.getKey(), $$ -> TAG_PREFIX__BIBLIO + $$),
            Map.Entry::getValue));

    final Pattern biblioTagPattern = inlineTagPattern(realSubTypes(BiblioTaglet.class)
        .map(Failable.asFunction($ -> $.getConstructor().newInstance().getName()))
        .collect(toSet()));

    /*
     * Source files scan for matches between configured bibliographic entries and actual citations
     * in source code.
     *
     * NOTE: This operation keeps track of the bibliographic entries actually used in the source
     * code (even outside Javadoc comments!) and signals invalid citations (that is, either
     * syntactically illegal or without matching bibliographic entries).
     */
    for (Path inputDirectory : getConfig().getInputDirectories()) {
      try (var inputFilesStream = Files.walk(inputDirectory)) {
        inputFilesStream
            .filter($ -> isRegularFile($) && isExtension($, JavaFileObject.Kind.SOURCE.extension))
            .forEach($ -> {
              try {
                FileObject fileObject = null;
                String source = Files.readString($);
                Matcher biblioTagMatcher = biblioTagPattern.matcher(source);
                while (biblioTagMatcher.find()) {
                  String biblioRef = inlineTagValue(biblioTagMatcher);
                  Matcher biblioRefMatcher = BiblioRef.PATTERN.matcher(biblioRef);
                  final String refId;
                  String refFullId = null;
                  if (biblioRefMatcher.find()) {
                    refId = biblioRefMatcher.group(BiblioRef.PATTERN_GROUP__REF_ID);
                    /*
                     * Bibliographic reference is fully matched?
                     *
                     * NOTE: `BiblioRef.PATTERN` is boundless, so it may match substrings ignoring
                     * the surrounding content; this condition enforces full match.
                     */
                    if (biblioRefMatcher.group().length() == biblioRef.length()) {
                      refFullId = biblioRefMatcher.group(BiblioRef.PATTERN_GROUP__REF_FULL_ID);
                    }
                  } else {
                    refId = null;
                  }
                  boolean refFullIdMatched;
                  /*
                   * Collect VALIDLY referenced bibliographic entry!
                   *
                   * NOTE: The match is twofold: full ID vs bibliographic entry, and simple ID vs
                   * bibliographic type (spec, doc, ref).
                   */
                  if ((refFullIdMatched = biblioEntries
                      .containsKey(refFullId)) /*
                                                * Matches the referenced full entry ID against
                                                * actual bibliographic entries
                                                */
                      && biblioTagEntryIds
                          .get(inlineTagName(biblioTagMatcher))
                          .contains(refId) /*
                                            * Matches the referenced identifier against its
                                            * bibliographic type (spec, doc, ref)
                                            */
                  ) {
                    if (usedBiblioEntryIds != null) {
                      //noinspection DataFlowIssue: "refId might be null" is false positive.
                      usedBiblioEntryIds.add(refId);
                    }
                  }
                  /*
                   * Log INVALID bibliographic reference!
                   *
                   * NOTE: Problematic citations are logged with their full coordinates to ease
                   * correction.
                   */
                  else {
                    if (fileObject == null) {
                      fileObject = fileObject($.toUri(), JavaFileObject.Kind.SOURCE, source);
                    }

                    Message message;
                    String ref;
                    String tagName = null;
                    // Bibliographic reference VALID?
                    if (refFullId != null) {
                      // Wrong tag for existing bibliographic entry?
                      if (refFullIdMatched) {
                        message = BiblioMessage.BIBLIO_ENTRY_TAG_INVALID;
                        tagName = biblioTagEntryIds.entrySet().stream()
                            .filter($$ -> $$.getKey() != null && $$.getValue().contains(refId))
                            .map(Map.Entry::getKey)
                            .findFirst().orElse(null);
                        ref = biblioRef;
                      }
                      // Referenced bibliographic entry not found.
                      else {
                        message = BiblioMessage.BIBLIO_ENTRY_NOT_FOUND;
                        ref = refFullId;
                      }
                    }
                    // Bibliographic reference INVALID.
                    else {
                      message = BiblioMessage.BIBLIO_REF_INVALID;
                      ref = biblioRef;
                    }

                    getLog().print(Kind.ERROR, fileObject,
                        lineStart(source, biblioTagMatcher.start()),
                        biblioTagMatcher.start(PATTERN_GROUP__INLINE_TAG__VALUE),
                        lineEnd(source, biblioTagMatcher.end()),
                        this, message, ref, tagName);
                  }
                }
              } catch (IOException ex) {
                throw new UncheckedIOException(ex);
              }
            });
      } catch (IOException | RuntimeException ex) {
        throw runtime(BiblioMessage.SOURCE_FILES_SCAN_FAILED.toString(getConfig(),
            getConfig().getInputDirectories()), ex);
      }
    }
  }
}
