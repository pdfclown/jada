/*
  SPDX-FileCopyrightText: © 2025 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (JadaFileProcessor.java) is part of jada-core module in Jada project
  <https://github.com/pdfclown/jada>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.jada.core.proc;

import org.greenrobot.eventbus.Subscribe;
import org.pdfclown.jada.core.Jada;
import org.pdfclown.jada.core.JadaConfig;
import org.pdfclown.jada.core.JadaObject;
import org.pdfclown.jada.core.event.PostProcessEvent;
import org.pdfclown.jada.core.system.SystemConfig;
import org.pdfclown.jada.core.system.proc.FileProcessor;
import org.pdfclown.jada.core.system.proc.FileSerializer;

/**
 * File post-processor for Javadoc output.
 *
 * @param <T>
 *          Deserialized file content type.
 * @author Stefano Chizzolini
 */
public abstract class JadaFileProcessor<T> extends FileProcessor<T> implements JadaObject {
  protected JadaFileProcessor(FileSerializer<T> serializer) {
    super(serializer);
  }

  @Override
  public JadaConfig getConfig() {
    return (JadaConfig) super.getConfig();
  }

  @Override
  public Jada getJada() {
    return getConfig().getJada();
  }

  @Override
  public void init(SystemConfig config) {
    super.init(config);

    getJada().subscribe(this);
  }

  @Subscribe
  public void onPostProcess(PostProcessEvent event) {
  }
}
