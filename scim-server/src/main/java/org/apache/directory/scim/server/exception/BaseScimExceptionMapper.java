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

package org.apache.directory.scim.server.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.protocol.Constants;
import org.apache.directory.scim.protocol.data.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@Slf4j
abstract class BaseScimExceptionMapper<E extends Throwable> {

  protected abstract ErrorResponse errorResponse(E throwable);

  public ResponseEntity<ErrorResponse> toResponseEntity(E throwable) {
    ResponseEntity<ErrorResponse> response = errorResponse(throwable).toResponseEntity();
    // log client errors (e.g. 404s) at debug, and anything else at warn
    if (HttpStatus.valueOf(response.getStatusCode().value()).is4xxClientError()) {
      log.debug("Returning error status: {}", response.getStatusCode(), throwable);
    } else {
      log.warn("Returning error status: {}", response.getStatusCode(), throwable);
    }

    response.getHeaders().add(HttpHeaders.CONTENT_TYPE, Constants.SCIM_CONTENT_TYPE);

    return response;
  }
}
