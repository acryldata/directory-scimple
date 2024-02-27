/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.directory.scim.server.it;

import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.protocol.data.ErrorResponse;
import org.apache.directory.scim.protocol.exception.ScimException;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

@Slf4j
@ControllerAdvice
public class GlobalControllerExceptionHandler extends DefaultHandlerExceptionResolver {

  public GlobalControllerExceptionHandler() {
    setOrder(Ordered.HIGHEST_PRECEDENCE);
    setWarnLogCategory(getClass().getName());
  }

  @ExceptionHandler({ScimException.class})
  public ResponseEntity<ErrorResponse> handleScimException(ScimException e) {
    return e.getError().toResponseEntity();
  }
  @ExceptionHandler({ResourceException.class})
  public ResponseEntity<ErrorResponse> handleResourceException(ResourceException e) {
    return new ErrorResponse(HttpStatus.valueOf(e.getStatus()), e.getMessage()).toResponseEntity();
  }

}
