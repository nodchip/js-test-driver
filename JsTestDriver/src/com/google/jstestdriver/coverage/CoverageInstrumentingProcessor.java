/*
 * Copyright 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.coverage;

import com.google.inject.Inject;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.Time;
import com.google.jstestdriver.hooks.FileLoadPostProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Instruments the javascript code found in the FileInfo.
 * 
 * @author corysmith@google.com (Cory Smith)
 * 
 */
public class CoverageInstrumentingProcessor implements FileLoadPostProcessor {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(CoverageInstrumentingProcessor.class);
  private final Instrumentor decorator;
  private final Set<String> excludes;
  private final CoverageAccumulator accumulator;
  private final Time time;

  @Inject
  public CoverageInstrumentingProcessor(Instrumentor decorator,
                                        @Coverage("coverageExcludes") Set<String> excludes,
                                        CoverageAccumulator accumulator,
                                        Time time) {
    this.decorator = decorator;
    this.excludes = excludes;
    this.accumulator = accumulator;
    this.time = time;
  }

  public FileInfo process(FileInfo file) {
    if (file.getFilePath().contains("LCOV.js") ||
        !file.canLoad() ||
        file.isServeOnly() ||
        excludes.contains(file.getFilePath()) ||
        file.getData().trim().isEmpty()) {
      return file;
    }
    long start = System.currentTimeMillis();
    InstrumentedCode decorated = decorator.instrument(new Code(file.getFilePath(),
                                                      file.getData()));
    LOGGER.debug(String.format("Instrumented %s in %ss",
        file.getFilePath(),
        (System.currentTimeMillis() - start)/1000.0
    ));
    decorated.writeInitialLines(accumulator);
    return file.load(decorated.getInstrumentedCode(), time.now().getMillis());
  }
}
