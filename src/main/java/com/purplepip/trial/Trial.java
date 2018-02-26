/*
 * Copyright (c) 2017 the original author or authors. All Rights Reserved
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.purplepip.trial;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Trial {
  private final Class<?> clazz;
  private final AtomicInteger executionCount = new AtomicInteger();
  private String filter;

  /**
   * Main execution.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    Thread.currentThread().setName("main()");
    if (args.length == 0) {
      throw new IllegalStateException("Trial arguments MUST specify a class name");
    }
    try {
      Trial trial = new Trial(Class.forName(args[0]));
      if (args.length > 1) {
        trial.filter(args[1]);
      }
      trial.run();
    } catch (ClassNotFoundException e) {
      LOG.error("Cannot find class " + args[0], e);
    }
  }

  /**
   * Allow the execution of methods trying something out.
   *
   * @param clazz class providing try methods.
   */
  public Trial(Class<?> clazz) {
    this.clazz = clazz;
  }

  public Trial filter(String filter) {
    this.filter = filter.toLowerCase();
    return this;
  }

  /**
   * Run trial.
   */
  public void run() {
    ValueLogger logger = new ValueLogger(clazz);
    Arrays.stream(clazz.getMethods())
        .filter(method -> method.getName().startsWith("try")
          && (filter == null || method.getName().toLowerCase().contains(filter)))
        .forEach(method -> {
          try {
            Object result = method.invoke(method.getDeclaringClass().newInstance());
            logger.child(method.getName()).info(result);
            executionCount.incrementAndGet();
          } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            LOG.error("Cannot invoke " + method, e);
          }
        });
  }

  public int getExecutionCount() {
    return executionCount.get();
  }
}