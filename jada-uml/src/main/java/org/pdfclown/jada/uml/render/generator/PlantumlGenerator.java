/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (PlantumlGenerator.java) is part of jada-uml module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
/*
  SPDX-FileCopyrightText: © 2016-2022 Talsma ICT

  SPDX-License-Identifier: Apache-2.0
 */
package org.pdfclown.jada.uml.render.generator;

import static org.pdfclown.jada.uml.render.generator.RemotePlantumlGenerator.PATTERN__HTTP_URL;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import net.sourceforge.plantuml.FileFormat;
import org.pdfclown.jada.uml.UmlConfig;

// SourceName: nl.talsmasoftware.umldoclet.uml.plantuml.PlantumlGenerator
/**
 * @author Sjoerd Talsma (original implementation)
 * @author Stefano Chizzolini (adaptation and redesign for Jada)
 */
public interface PlantumlGenerator {
  /**
   */
  static PlantumlGenerator getPlantumlGenerator(UmlConfig config) {
    return Optional.ofNullable(config.getPlantumlServerUrl())
        .filter($ -> PATTERN__HTTP_URL.matcher($).find())
        .map($ -> (PlantumlGenerator) new RemotePlantumlGenerator($))
        .orElseGet(BuiltinPlantumlGenerator::new);
  }

  void generatePlantumlDiagramFromSource(String plantumlSource, FileFormat format, OutputStream out)
      throws IOException;
}
