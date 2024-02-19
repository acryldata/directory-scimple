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
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;
import org.apache.directory.scim.protocol.Constants;
import org.apache.directory.scim.protocol.ErrorMessageType;
import org.apache.directory.scim.protocol.data.ErrorResponse;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.springframework.http.HttpStatus;


@Provider
@Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
public class ResourceExceptionMapper extends BaseScimExceptionMapper<ResourceException> {

  @Override
  protected ErrorResponse errorResponse(ResourceException e) {
    HttpStatus status = HttpStatus.valueOf(e.getStatus());
    ErrorResponse errorResponse = new ErrorResponse(status, e.getMessage());

    if (status == HttpStatus.CONFLICT) {
      errorResponse.setScimType(ErrorMessageType.UNIQUENESS);

      //only use default error message if the ErrorResponse does not already contain a message
      if (e.getMessage() == null) {
        errorResponse.setDetail(ErrorMessageType.UNIQUENESS.getDetail());
      } else {
        errorResponse.setDetail(e.getMessage());
      }
    } else {
      errorResponse.setDetail(e.getMessage());
    }

    return errorResponse;
  }
}
