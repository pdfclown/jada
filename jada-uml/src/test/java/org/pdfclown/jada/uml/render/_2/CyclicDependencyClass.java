/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (CyclicDependencyClass.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.render._2;

import org.pdfclown.jada.uml.render.PackageDependencyCycleIT;

// SourceName: nl.talsmasoftware.umldoclet.features.cycle.CyclicDependencyClass
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 * @implNote Class intentionally causing a cyclic package dependency.
 */
@SuppressWarnings({ "NewClassNamingConvention", "RequireExplicitNullMarking" })
public class CyclicDependencyClass extends PackageDependencyCycleIT {
}
