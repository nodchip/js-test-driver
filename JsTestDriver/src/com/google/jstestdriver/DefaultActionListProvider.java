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
package com.google.jstestdriver;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.jstestdriver.hooks.ActionListProcessor;
import com.google.jstestdriver.output.XmlPrinter;

import java.util.List;
import java.util.Set;

/**
 * Provides a sequence of actions from a large number of arguments.
 *
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 * @author corysmith@google.com (Cory Smith)
 */
@Singleton
public class DefaultActionListProvider implements ActionListProvider {

  private final List<String> tests;
  private final List<String> arguments;
  private final boolean reset;
  private final List<String> dryRunFor;
  private final int port;
  private final int sslPort;
  private final String testOutput;
  private final Set<ActionListProcessor> processors;
  private final XmlPrinter xmlPrinter;
  private final ActionSequenceBuilder builder;
  private final boolean raiseOnFailure;

  // TODO(corysmith): Refactor this. Currently in a temporary,
  //  make dependencies visible to aid refactoring state.
  @Inject
  public DefaultActionListProvider(
      @Named("tests") List<String> tests,
      @Named("arguments") List<String> arguments,
      @Named("reset") boolean reset,
      @Named("dryRunFor") List<String> dryRunFor,
      @Named("port") int port,
      @Named("sslPort") int sslPort,
      @Named("testOutput") String testOutput,
      Set<ActionListProcessor> processors,
      XmlPrinter xmlPrinter,
      ActionSequenceBuilder builder,
      @Named("raiseOnFailure") boolean raiseOnFailure) {
    this.tests = tests;
    this.arguments = arguments;
    this.reset = reset;
    this.dryRunFor = dryRunFor;
    this.port = port;
    this.sslPort = sslPort;
    this.testOutput = testOutput;
    this.processors = processors;
    this.xmlPrinter = xmlPrinter;
    this.builder = builder;
    this.raiseOnFailure = raiseOnFailure;
  }

  @Override
  @Provides
  public List<Action> get() {
    builder.addTests(tests)
           .addCommands(arguments)
           .reset(reset)
           .asDryRunFor(dryRunFor)
           .withLocalServerPort(port)
           .withLocalServerSslPort(sslPort);
    if (raiseOnFailure) {
      builder.raiseOnFailure();
    }
    if (testOutput.length() > 0) {
      builder.printingResultsWhenFinished(xmlPrinter);
    }
    List<Action> actions = builder.build();
    for (ActionListProcessor processor : processors) {
      actions = processor.process(actions);
    }
    return actions;
  }
}
