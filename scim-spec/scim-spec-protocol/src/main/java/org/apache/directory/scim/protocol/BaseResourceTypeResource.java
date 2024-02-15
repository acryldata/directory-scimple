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

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.apache.directory.scim.protocol.adapter.FilterWrapper;
import org.apache.directory.scim.protocol.data.ListResponse;
import org.apache.directory.scim.protocol.data.PatchRequest;
import org.apache.directory.scim.protocol.data.SearchRequest;
import org.apache.directory.scim.protocol.exception.ScimException;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.filter.SortOrder;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.filter.attribute.AttributeReferenceListWrapper;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

@Tag(name="SCIM")
@Hidden
public interface BaseResourceTypeResource<T> {

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.1">Scim spec,
   *      retrieving known resources</a>
   * @return
   * @throws ScimException
   * @throws UnableToRetrieveResourceException
   */
  @GET
  @GetMapping("{id}")
  @Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Operation(description="Find by id")
  @ApiResponses(value={
    @ApiResponse(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                 schema = @Schema(implementation = ScimResource.class))),
    @ApiResponse(responseCode="400", description="Bad Request"),
    @ApiResponse(responseCode="404", description="Not found"),
    @ApiResponse(responseCode="500", description="Internal Server Error"),
    @ApiResponse(responseCode="501", description="Not Implemented")
  })
    default ResponseEntity<T> getById(WebRequest request, @Parameter(name="id", required=true) @PathVariable(name = "id") String id,
                             @Parameter(name="attributes") @RequestParam(name = "attributes") AttributeReferenceListWrapper attributes,
                             @Parameter(name="excludedAttributes") @RequestParam(name = "excludedAttributes") AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    return ResponseEntity.status(Status.NOT_IMPLEMENTED.getStatusCode()).build();
  }

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.2">Scim spec,
   *      query resources</a>
   * @return
   */
  @GetMapping
  @Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Operation(description="Find by a combination of query parameters")
  @ApiResponses(value={
    @ApiResponse(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                 schema = @Schema(implementation = ScimResource.class))),
    @ApiResponse(responseCode="400", description="Bad Request"),
    @ApiResponse(responseCode="404", description="Not found"),
    @ApiResponse(responseCode="500", description="Internal Server Error"),
    @ApiResponse(responseCode="501", description="Not Implemented")
  })
  default ResponseEntity<ListResponse<T>> query(@Parameter(name="attributes") @RequestParam(name = "attributes") AttributeReferenceListWrapper attributes,
                         @Parameter(name="excludedAttributes") @RequestParam(name = "excludedAttributes") AttributeReferenceListWrapper excludedAttributes,
                         @Parameter(name="filter") @RequestParam(name = "filter") FilterWrapper filterWrapper,
                         @Parameter(name="sortBy") @RequestParam(name = "sortBy") AttributeReference sortBy,
                         @Parameter(name="sortOrder") @RequestParam(name = "sortOrder") SortOrder sortOrder,
                         @Parameter(name="startIndex") @RequestParam(name = "startIndex") Integer startIndex,
                         @Parameter(name="count") @RequestParam(name = "count") Integer count) throws ScimException, ResourceException {
    return ResponseEntity.status(Status.NOT_IMPLEMENTED.getStatusCode()).build();
  }

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.3">Scim spec,
   *      query resources</a>
   * @return
   */
  @PostMapping
  @Consumes({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Operation(description = "Create")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201",
                 content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                 schema = @Schema(implementation = ScimResource.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "409", description = "Conflict"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    @ApiResponse(responseCode = "501", description = "Not Implemented") })
  default ResponseEntity<T> create(@RequestBody(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                                       schema = @Schema(implementation = ScimResource.class)),
                                       required = true) T resource,
                          @Parameter(name="attributes") @RequestParam(name = "attributes") AttributeReferenceListWrapper attributes,
                          @Parameter(name="excludedAttributes") @RequestParam(name = "excludedAttributes") AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    return ResponseEntity.status(Status.NOT_IMPLEMENTED.getStatusCode()).build();
  }

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.3">Scim spec,
   *      query with post</a>
   * @return
   */
  @PostMapping("/.search")
  @Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Operation(description = "Search")
  @ApiResponses(value = {
    @ApiResponse(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                 schema = @Schema(implementation = ScimResource.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    @ApiResponse(responseCode = "501", description = "Not Implemented") })
  default ResponseEntity<ListResponse<T>> find(@RequestBody(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                                     schema = @Schema(implementation = SearchRequest.class)),
                                     required = true) SearchRequest request) throws ScimException, ResourceException {
    return ResponseEntity.status(Status.NOT_IMPLEMENTED.getStatusCode()).build();
  }

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.5.1">Scim spec,
   *      update</a>
   * @return
   */
  @PutMapping("{id}")
  @Consumes({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Operation(description = "Update")
  @ApiResponses(value = {
    @ApiResponse(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                 schema = @Schema(implementation = ScimResource.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    @ApiResponse(responseCode = "501", description = "Not Implemented") })
  default ResponseEntity<T> update(
    WebRequest request,
    @RequestBody(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                                       schema = @Schema(implementation = ScimResource.class)),
                                       required = true) T resource,
                          @PathVariable("id") String id,
                          @Parameter(name="attributes") @RequestParam("attributes") AttributeReferenceListWrapper attributes,
                          @Parameter(name="excludedAttributes") @RequestParam("excludedAttributes") AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    return ResponseEntity.status(Status.NOT_IMPLEMENTED.getStatusCode()).build();
  }

  @PatchMapping("{id}")
  @Consumes({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Produces({Constants.SCIM_CONTENT_TYPE, MediaType.APPLICATION_JSON})
  @Operation(description = "Patch a portion of the backing store")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    @ApiResponse(responseCode = "501", description = "Not Implemented") })
  default ResponseEntity<T> patch(WebRequest request, @RequestBody(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
                                      schema = @Schema(implementation = PatchRequest.class)),
                                      required = true) PatchRequest patchRequest,
                         @PathVariable("id") String id,
                         @Parameter(name="attributes") @RequestParam("attributes") AttributeReferenceListWrapper attributes,
                         @Parameter(name="excludedAttributes") @RequestParam("excludedAttributes") AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    return ResponseEntity.status(Status.NOT_IMPLEMENTED.getStatusCode()).build();
  }

  @DeleteMapping("{id}")
  @Operation(description = "Delete from the backing store")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    @ApiResponse(responseCode = "501", description = "Not Implemented") })
  default ResponseEntity<T> delete(@Parameter(name = "id", required = true) @PathVariable("id") String id) throws ScimException, ResourceException {
    return ResponseEntity.status(Status.NOT_IMPLEMENTED.getStatusCode()).build();
  }
}
