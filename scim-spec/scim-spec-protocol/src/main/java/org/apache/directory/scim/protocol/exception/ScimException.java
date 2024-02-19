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

package org.apache.directory.scim.protocol.exception;

import jakarta.ws.rs.core.Response.Status;

import org.apache.directory.scim.protocol.data.ErrorResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;


@Data
@EqualsAndHashCode(callSuper=true)
public class ScimException extends Exception {

  private static final long serialVersionUID = 3643485564325176463L;
  private final ErrorResponse error;
  private final HttpStatus status;

  public ScimException(HttpStatus status, String message, Throwable cause) {
    super(message, cause);
    this.error = new ErrorResponse(status, message);
    this.status = status;
  }

  public ScimException(HttpStatus status, String message) {
    this(new ErrorResponse(status, message), status);
  }

  public ScimException(ErrorResponse error, HttpStatus status) {
    this.error = error;
    this.status = status;
  }
}
