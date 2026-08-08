/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (package-info.java) is part of jada-ext module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/**
 * This package and its subpackages provide a comprehensive test case for
 * {@link org.pdfclown.jada.ext.proc.DocReuseTagletProcessor}.
 * <h4>Legend</h4> The Javadoc comments within this test case follow this convention:
 * <ul>
 * <li>
 * <p>
 * <b>fragment sources</b> (declared by <code>@jada.doc</code>) are denoted by:
 * </p>
 * <pre><code>&lt;h4&gt;[* fragment]&lt;/h4&gt;</code></pre>
 * <p>
 * where
 * </p>
 * <ul>
 * <li><code>*</code> describes the kind of syntactic element (such as method, type, ...) the
 * fragment belongs to</li>
 * </ul>
 * </li>
 * <li>
 * <p>
 * <b>fragment targets</b> (declared by <code>@jada.reuseDoc</code>) are denoted by:
 * </p>
 * <pre><code>&lt;h4&gt;[Name resolution *] **&lt;/h4&gt;</code></pre>
 * <p>
 * where
 * </p>
 * <ul>
 * <li><code>*</code> corresponds to the fragment key case number (see "Fragment key resolution"
 * algorithm in {@code DocReuseTagletProcessor} implementation)</li>
 * <li><code>**</code> describes the kind of syntactic element (such as import, type member, ...)
 * the resolution matches</li>
 * </ul>
 * </li>
 * </ul>
 */
@NullMarked
package org.pdfclown.jada.ext.proc.DocReuseTagletProcessorIT_.main;

import org.jspecify.annotations.NullMarked;