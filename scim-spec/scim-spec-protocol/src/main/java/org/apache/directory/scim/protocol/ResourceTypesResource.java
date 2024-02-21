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

package org.apache.directory.scim.protocol;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.apache.directory.scim.protocol.data.ListResponse;
import org.apache.directory.scim.spec.schema.ResourceType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;


/**
 * From SCIM Protocol Specification, section 4, page 74
 * 
 * @see <a href="https://tools.ietf.org/html/rfc7644#section-4">Scim spec
 *      section 4</a>
 * 
 *      /ResourceTypes An HTTP GET to this endpoint is used to discover the
 *      types of resources available on a SCIM service provider (e.g., Users and
 *      Groups). Each resource type defines the endpoints, the core schema URI
 *      that defines the resource, and any supported schema extensions. The
 *      attributes defining a resource type can be found in Section 6 of
 *      [RFC7643], and an example representation can be found in Section 8.6 of
 *      [RFC7643].
 * 
 *      In cases where a request is for a specific "ResourceType" or "Schema",
 *      the single JSON object is returned in the same way that a single User or
 *      Group is retrieved, as per Section 3.4.1. When returning multiple
 *      ResourceTypes or Schemas, the message form described by the
 *      "urn:ietf:params:scim:api:messages:2.0:ListResponse" (ListResponse) form
 *      SHALL be used as shown in Figure 3 and in Figure 9 below. Query
 *      parameters described in Section 3.4.2, such as filtering, sorting, and
 *      pagination, SHALL be ignored. If a "filter" is provided, the service
 *      provider SHOULD respond with HTTP status code 403 (Forbidden) to ensure
 *      that clients cannot incorrectly assume that any matching conditions
 *      specified in a filter are true.
 */

@Path("ResourceTypes")
@Tag(name="SCIM-Configuration")
public interface ResourceTypesResource {

  @GET()
  @GetMapping(produces = {Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Operation(description = "Get All Resource Types")
  @ApiResponse(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
    array = @ArraySchema(schema = @Schema(implementation = ResourceType.class))))
  default ResponseEntity<ListResponse<ResourceType>> getAllResourceTypes(@QueryParam("filter") @RequestParam(name = "filter", required = false) String filter) throws Exception {

    if (filter != null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  @GET
  @GetMapping(value = "{name}", produces = {Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Path("{name}")
  @Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Operation(description = "Get Resource Type by URN")
  @ApiResponse(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE, schema = @Schema(implementation = ResourceType.class)))
  default ResponseEntity<ResourceType> getResourceType(@PathParam("name") @PathVariable(name = "name") String name) throws Exception {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }
}
