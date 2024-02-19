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

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.apache.directory.scim.protocol.Constants;
import org.apache.directory.scim.protocol.data.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@Provider
@Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
public class GenericExceptionMapper extends BaseScimExceptionMapper<Throwable> {

  @Override
  protected ErrorResponse errorResponse(Throwable throwable) {
    return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage());
  }
}
