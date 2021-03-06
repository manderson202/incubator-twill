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
package org.apache.twill.discovery;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import org.apache.twill.common.Cancellable;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A simple in memory implementation of {@link DiscoveryService} and {@link DiscoveryServiceClient}.
 */
public class InMemoryDiscoveryService implements DiscoveryService, DiscoveryServiceClient {

  private final SetMultimap<String, Discoverable> services = LinkedHashMultimap.create();
  private final Map<String, DefaultServiceDiscovered> serviceDiscoveredMap = Maps.newHashMap();
  private final Lock lock = new ReentrantLock();

  @Override
  public Cancellable register(final Discoverable discoverable) {
    final Discoverable wrapper = new DiscoverableWrapper(discoverable);
    final String serviceName = wrapper.getName();

    lock.lock();
    try {
      services.put(serviceName, wrapper);

      DefaultServiceDiscovered serviceDiscovered = serviceDiscoveredMap.get(serviceName);
      if (serviceDiscovered != null) {
        serviceDiscovered.setDiscoverables(services.get(serviceName));
      }
    } finally {
      lock.unlock();
    }

    return new Cancellable() {
      @Override
      public void cancel() {
        lock.lock();
        try {
          services.remove(serviceName, wrapper);

          DefaultServiceDiscovered serviceDiscovered = serviceDiscoveredMap.get(serviceName);
          if (serviceDiscovered != null) {
            serviceDiscovered.setDiscoverables(services.get(serviceName));
          }
        } finally {
          lock.unlock();
        }
      }
    };
  }

  @Override
  public ServiceDiscovered discover(final String name) {
    lock.lock();
    try {
      DefaultServiceDiscovered serviceDiscovered = serviceDiscoveredMap.get(name);
      if (serviceDiscovered == null) {
        serviceDiscovered = new DefaultServiceDiscovered(name);
        serviceDiscovered.setDiscoverables(services.get(name));
        serviceDiscoveredMap.put(name, serviceDiscovered);
      }
      return serviceDiscovered;
    } finally {
      lock.unlock();
    }
  }
}
