/*
 * Copyright 2010 Google Inc.
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
package com.google.jstestdriver.requesthandlers;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.util.Providers;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author rdionne@google.com (Robert Dionne)
 */
public class RequestDispatcherTest extends TestCase {

  private IMocksControl control;
  
  private HttpServletRequest request;
  private HttpServletResponse response;

  private RequestMatcher one;
  private RequestMatcher two;

  private RequestHandler handlerOne;
  private RequestHandler handlerTwo;

  private GatewayConfiguration gatewayConfiguration;

  private UnsupportedMethodErrorSender sender;
  
  private RequestDispatcher dispatcher;

  @Override
  protected void setUp() throws Exception {
    control = EasyMock.createControl();

    request = control.createMock(HttpServletRequest.class);
    response = control.createMock(HttpServletResponse.class);
    
    one = new RequestMatcher(HttpMethod.GET, "/one/two");
    two = new RequestMatcher(HttpMethod.POST, "/a/*");

    handlerOne = control.createMock(RequestHandler.class);
    handlerTwo = control.createMock(RequestHandler.class);

    gatewayConfiguration = control.createMock(GatewayConfiguration.class);

    sender = control.createMock(UnsupportedMethodErrorSender.class);
    
    dispatcher = new RequestDispatcher(
        request,
        response,
        ImmutableList.of(one, two),
        ImmutableMap.of(
            one, Providers.of(handlerOne),
            two, Providers.of(handlerTwo)),
        gatewayConfiguration,
        sender);
  }

  public void testDispatch_GET() throws Exception {
    expect(request.getMethod()).andReturn("GET");
    expect(request.getRequestURI()).andReturn("/one/two").anyTimes();
    /*expect*/ handlerOne.handleIt();

    control.replay();

    dispatcher.dispatch();

    control.verify();
  }

  public void testDispatch_POST() throws Exception {
    expect(request.getMethod()).andReturn("POST");
    expect(request.getRequestURI()).andReturn("/a/b").anyTimes();
    /*expect*/ handlerTwo.handleIt();

    control.replay();

    dispatcher.dispatch();

    control.verify();
  }

  public void testDispatch_POST_methodNotAllowed() throws Exception {
    expect(request.getMethod()).andReturn("POST");
    expect(request.getRequestURI()).andReturn("/one/two").anyTimes();
    expect(gatewayConfiguration.getMatchers())
        .andReturn(ImmutableList.<RequestMatcher>of());
    /*expect*/ sender.methodNotAllowed();

    control.replay();

    dispatcher.dispatch();

    control.verify();
  }

  public void testDispatch_GET_methodNotAllowed() throws Exception {
    expect(request.getMethod()).andReturn("GET");
    expect(request.getRequestURI()).andReturn("/a/b").anyTimes();
    expect(gatewayConfiguration.getMatchers())
        .andReturn(ImmutableList.<RequestMatcher>of());
    /*expect*/ sender.methodNotAllowed();

    control.replay();

    dispatcher.dispatch();

    control.verify();
  }

  public void testDispatch_unsupportedMethod() throws Exception {
    expect(request.getMethod()).andReturn("YOUR_MOM");
    /*expect*/ sender.methodNotAllowed();

    control.replay();

    dispatcher.dispatch();

    control.verify();
  }

  public void testDispatch_GET_notFound() throws Exception {
    expect(request.getMethod()).andReturn("GET");
    expect(request.getRequestURI()).andReturn("/nothing").anyTimes();
    expect(gatewayConfiguration.getMatchers())
        .andReturn(ImmutableList.<RequestMatcher>of());
    /*expect*/ response.sendError(eq(HttpServletResponse.SC_NOT_FOUND), (String) anyObject());

    control.replay();

    dispatcher.dispatch();

    control.verify();
  }
}
