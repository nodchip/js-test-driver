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

import java.util.Set;

import junit.framework.TestCase;

import com.google.common.collect.Sets;

/**
 * @author corysmith@google.com (Cory Smith)
 */
public class FileSetCacheStrategyTest extends TestCase {
  public void testFileInfoExpired() throws Exception {
    Set<FileInfo> oldSet = Sets.newHashSet(new FileInfo("foo.js", 10, -1, false, false, null, "foo.js"));
    Set<FileInfo> newSet = Sets.newHashSet(new FileInfo("foo.js", 11, -1, false, false, null, "foo.js"));
    assertEquals(newSet, new FileSetCacheStrategy().createExpiredFileSet(newSet, oldSet));
  }

  public void testNoFileInfo() throws Exception {
    Set<FileInfo> oldSet = Sets.newHashSet();
    Set<FileInfo> newSet = Sets.newHashSet(new FileInfo("foo.js", 11, -1, false, false, null, "foo.js"));
    assertEquals(newSet, new FileSetCacheStrategy().createExpiredFileSet(newSet, oldSet));
  }

  public void testFileInfoUnChanged() throws Exception {
    Set<FileInfo> oldSet = Sets.newHashSet(new FileInfo("foo.js", 10, -1, false, false, null, "foo.js"));
    Set<FileInfo> newSet = Sets.newHashSet(new FileInfo("foo.js", 10, -1, false, false, null, "foo.js"));
    assertEquals(Sets.newHashSet(),
      new FileSetCacheStrategy().createExpiredFileSet(newSet, oldSet));
  }
}
