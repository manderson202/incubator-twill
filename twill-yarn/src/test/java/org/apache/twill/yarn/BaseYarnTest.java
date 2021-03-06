/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.twill.yarn;

import org.apache.twill.api.TwillController;
import org.apache.twill.api.TwillRunner;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Base class for all YARN tests.
 */
public abstract class BaseYarnTest {

  private static final Logger LOG = LoggerFactory.getLogger(BaseYarnTest.class);

  @ClassRule
  public static TemporaryFolder tmpFolder = new TemporaryFolder();

  @BeforeClass
  public static void init() throws IOException {
    YarnTestUtils.initOnce();
  }

  @After
  public final void cleanupTest() {
    // Make sure all applications are stopped after a test case is executed, even it failed.
    TwillRunner twillRunner = YarnTestUtils.getTwillRunner();
    for (TwillRunner.LiveInfo liveInfo : twillRunner.lookupLive()) {
      for (TwillController controller : liveInfo.getControllers()) {
        try {
          controller.stopAndWait();
        } catch (Throwable t) {
          LOG.error("Failed to stop application {}", liveInfo.getApplicationName(), t);
        }
      }
    }
  }
}
