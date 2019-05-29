/*
 * Copyright 2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.clouddriver.azure.config;

import okhttp3.Response;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import okhttp3.Interceptor;

@Aspect
public class AzureLogAspect {
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  //    @Pointcut("execution(* com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor.intercept(..))")
  @Pointcut("execution(* com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor.intercept(..))")
//    @Pointcut("within(com.microsoft.azure.management.resources.fluentcore.utils)")
  public void intercept() {}

  @Around("intercept()")
  public Object log(ProceedingJoinPoint pjp) throws Throwable {
    long start = System.nanoTime();
    Interceptor.Chain chain = (Interceptor.Chain) pjp.getArgs()[0];
    String url = chain.request().url().toString();
    try {
      return pjp.proceed();
    }finally {
      long end = System.nanoTime();
      logger.info("[{}ms] azureapi {}", (end - start) / 1000000, url);
    }
  }

  @AfterReturning(value="intercept()", returning = "retVal")
  public void logResponseCode(Response retVal) {
    logger.info("[{}] {}", retVal.code(), retVal.request().url().toString());
  }
}
