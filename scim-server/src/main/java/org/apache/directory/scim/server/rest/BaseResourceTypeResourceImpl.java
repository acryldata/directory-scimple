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

package org.apache.directory.scim.server.rest;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.Status.Family;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.core.repository.Repository;
import org.apache.directory.scim.core.repository.RepositoryRegistry;
import org.apache.directory.scim.core.repository.annotations.ScimProcessingExtension;
import org.apache.directory.scim.core.repository.extensions.AttributeFilterExtension;
import org.apache.directory.scim.core.repository.extensions.ClientFilterException;
import org.apache.directory.scim.core.repository.extensions.ProcessingExtension;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.protocol.BaseResourceTypeResource;
import org.apache.directory.scim.protocol.Constants;
import org.apache.directory.scim.protocol.adapter.FilterWrapper;
import org.apache.directory.scim.protocol.data.ListResponse;
import org.apache.directory.scim.protocol.data.PatchRequest;
import org.apache.directory.scim.protocol.data.SearchRequest;
import org.apache.directory.scim.protocol.exception.ScimException;
import org.apache.directory.scim.server.exception.AttributeException;
import org.apache.directory.scim.server.exception.UnableToRetrieveResourceException;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.FilterResponse;
import org.apache.directory.scim.spec.filter.PageRequest;
import org.apache.directory.scim.spec.filter.SortOrder;
import org.apache.directory.scim.spec.filter.SortRequest;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.filter.attribute.AttributeReferenceListWrapper;
import org.apache.directory.scim.spec.filter.attribute.ScimRequestContext;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@Slf4j
public abstract class BaseResourceTypeResourceImpl<T extends ScimResource> implements BaseResourceTypeResource<T> {

  private static final Logger LOG = LoggerFactory.getLogger(BaseResourceTypeResourceImpl.class);

  private final RepositoryRegistry repositoryRegistry;

  private final  AttributeUtil attributeUtil;

  private final Class<T> resourceClass;

  public BaseResourceTypeResourceImpl(SchemaRegistry schemaRegistry, RepositoryRegistry repositoryRegistry, Class<T> resourceClass) {
    this.repositoryRegistry = repositoryRegistry;
    this.resourceClass = resourceClass;
    this.attributeUtil = new AttributeUtil(schemaRegistry);
  }

  public Repository<T> getRepository() {
    return repositoryRegistry.getRepository(resourceClass);
  }

  Repository<T> getRepositoryInternal() throws ScimException {
    Repository<T> repository = getRepository();
    if (repository == null) {
      throw new ScimException(HttpStatus.NOT_IMPLEMENTED, "Provider not defined");
    }
    return repository;
  }

  // Annotations on interfaces are not inherited by implementing classes.
  // Spring doesn't perform any lookup on interface to resolve annotation, hence need to duplicate the annotation
  // from interface
  @Override
  public ResponseEntity<T> getById(WebRequest request, @Parameter(name="id", required=true) @PathVariable(name = "id") String id,
    @Parameter(name="attributes") @RequestParam(name = "attributes", required = false) AttributeReferenceListWrapper attributes,
    @Parameter(name="excludedAttributes") @RequestParam(name = "excludedAttributes", required = false) AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    if (request.getParameter("filter") != null) {
      return ResponseEntity.status(Status.FORBIDDEN.getStatusCode()).build();
    }

    Repository<T> repository = getRepositoryInternal();

    T resource = null;
    try {
      resource = repository.get(id);
    } catch (UnableToRetrieveResourceException e2) {
      Status status = Status.fromStatusCode(e2.getStatus());
      if (status.getFamily().equals(Family.SERVER_ERROR)) {
        throw e2;
      }
    }

    if (resource == null) {
      throw notFoundException(id);
    }

    String etag = fromVersion(resource);

    if (etag != null) {

      if (request.checkNotModified(etag)) {
        return ResponseEntity.status(Status.NOT_MODIFIED.getStatusCode()).build();
      }
    }

    Set<AttributeReference> attributeReferences = AttributeReferenceListWrapper.getAttributeReferences(attributes);
    Set<AttributeReference> excludedAttributeReferences = AttributeReferenceListWrapper.getAttributeReferences(excludedAttributes);
    validateAttributes(attributeReferences, excludedAttributeReferences);

    // Process Attributes
    resource = processFilterAttributeExtensions(repository, resource, attributeReferences, excludedAttributeReferences);
    resource = attributesForDisplayThrowOnError(resource, attributeReferences, excludedAttributeReferences);

    ResponseEntity.BodyBuilder bodyBuilder =  ResponseEntity.ok()
      .location(buildLocationTag(resource));

    if (etag != null) {
      bodyBuilder.header(HttpHeaders.ETAG, etag);
    }

    return bodyBuilder.body(resource);
  }

  @Override
  public ResponseEntity<ListResponse<T>> query(@Parameter(name="attributes") @RequestParam(name = "attributes", required = false) AttributeReferenceListWrapper attributes,
    @Parameter(name="excludedAttributes") @RequestParam(name = "excludedAttributes", required = false) AttributeReferenceListWrapper excludedAttributes,
    @Parameter(name="filter") @RequestParam(name = "filter", required = false) FilterWrapper filterWrapper,
    @Parameter(name="sortBy") @RequestParam(name = "sortBy", required = false) AttributeReference sortBy,
    @Parameter(name="sortOrder") @RequestParam(name = "sortOrder", required = false) SortOrder sortOrder,
    @Parameter(name="startIndex") @RequestParam(name = "startIndex", required = false) Integer startIndex,
    @Parameter(name="count") @RequestParam(name = "count", required = false) Integer count) throws ScimException, ResourceException {
    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setAttributes(AttributeReferenceListWrapper.getAttributeReferences(attributes));
    searchRequest.setExcludedAttributes(AttributeReferenceListWrapper.getAttributeReferences(excludedAttributes));

    if (filterWrapper != null) {
      searchRequest.setFilter(filterWrapper.getFilter());
    }
    else {
      searchRequest.setFilter(null);
    }
    
    searchRequest.setSortBy(sortBy);
    searchRequest.setSortOrder(sortOrder);
    searchRequest.setStartIndex(startIndex);
    searchRequest.setCount(count);

    return find(searchRequest);
  }

  @Override
  public ResponseEntity<T> create(@RequestBody(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
    schema = @Schema(implementation = ScimResource.class)),
    required = true) @org.springframework.web.bind.annotation.RequestBody T resource,
    @Parameter(name="attributes") @RequestParam(name = "attributes", required = false) AttributeReferenceListWrapper attributes,
    @Parameter(name="excludedAttributes") @RequestParam(name = "excludedAttributes", required = false) AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    Repository<T> repository = getRepositoryInternal();

    Set<AttributeReference> attributeReferences = AttributeReferenceListWrapper.getAttributeReferences(attributes);
    Set<AttributeReference> excludedAttributeReferences = AttributeReferenceListWrapper.getAttributeReferences(excludedAttributes);
    validateAttributes(attributeReferences, excludedAttributeReferences);

    T created = repository.create(resource);

    String etag = fromVersion(created);

    // Process Attributes
    created = processFilterAttributeExtensions(repository, created, attributeReferences, excludedAttributeReferences);

    try {
      created = attributesForDisplay(created, attributeReferences, excludedAttributeReferences);
    } catch (AttributeException e) {
        log.debug("Exception thrown while processing attributes", e);
    }

    return ResponseEntity.status(Status.CREATED.getStatusCode())
      .location(buildLocationTag(created))
      .header(HttpHeaders.ETAG, etag)
      .header(HttpHeaders.CONTENT_TYPE, Constants.SCIM_CONTENT_TYPE)
      .body(created);
  }

  @Override
  public ResponseEntity<ListResponse<T>> find(@RequestBody(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
    schema = @Schema(implementation = SearchRequest.class)),
    required = true) @org.springframework.web.bind.annotation.RequestBody SearchRequest request) throws ScimException, ResourceException {
    Repository<T> repository = getRepositoryInternal();

    Set<AttributeReference> attributeReferences = Optional.ofNullable(request.getAttributes())
                                                          .orElse(Collections.emptySet());
    Set<AttributeReference> excludedAttributeReferences = Optional.ofNullable(request.getExcludedAttributes())
                                                                  .orElse(Collections.emptySet());
    validateAttributes(attributeReferences, excludedAttributeReferences);

    Filter filter = request.getFilter();
    PageRequest pageRequest = request.getPageRequest();
    SortRequest sortRequest = request.getSortRequest();

    ListResponse<T> listResponse = new ListResponse<>();

    FilterResponse<T> filterResp = repository.find(filter, pageRequest, sortRequest);

    // If no resources are found, we should still return a ListResponse with
    // the totalResults set to 0;
    // (https://tools.ietf.org/html/rfc7644#section-3.4.2)
    if (filterResp == null || filterResp.getResources() == null || filterResp.getResources()
                                                                             .isEmpty()) {
      listResponse.setTotalResults(0);
    } else {
      log.debug("Find returned " + filterResp.getResources()
                                            .size());
      listResponse.setItemsPerPage(filterResp.getResources()
                                             .size());
      int startIndex = Optional.ofNullable(filterResp.getPageRequest().getStartIndex()).orElse(1);
      listResponse.setStartIndex(startIndex);
      listResponse.setTotalResults(filterResp.getTotalResults());

      List<T> results = new ArrayList<>();

      for (T resource : filterResp.getResources()) {

        // Process Attributes
        resource = processFilterAttributeExtensions(repository, resource, attributeReferences, excludedAttributeReferences);
        resource = attributesForDisplayThrowOnError(resource, attributeReferences, excludedAttributeReferences);
        results.add(resource);
      }

      listResponse.setResources(results);
    }

    return ResponseEntity.ok()
          .body(listResponse);
  }

  @Override
  public ResponseEntity<T> update(WebRequest request,
    @RequestBody(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
      schema = @Schema(implementation = ScimResource.class)),
      required = true) @org.springframework.web.bind.annotation.RequestBody T resource,
    @PathVariable("id") String id,
    @Parameter(name="attributes") @RequestParam(name = "attributes", required = false) AttributeReferenceListWrapper attributes,
    @Parameter(name="excludedAttributes") @RequestParam(name = "excludedAttributes", required = false) AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    return update(request, attributes, excludedAttributes, (etag, includeAttributes, excludeAttributes, repository)
      -> repository.update(id, etag,resource, includeAttributes, excludeAttributes));
  }

  @Override
  public ResponseEntity<T> patch(WebRequest request, @RequestBody(content = @Content(mediaType = Constants.SCIM_CONTENT_TYPE,
    schema = @Schema(implementation = PatchRequest.class)),
    required = true) @org.springframework.web.bind.annotation.RequestBody PatchRequest patchRequest,
    @PathVariable("id") String id,
    @Parameter(name="attributes") @RequestParam(name = "attributes", required = false) AttributeReferenceListWrapper attributes,
    @Parameter(name="excludedAttributes") @RequestParam(name = "excludedAttributes", required = false) AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    return update(request, attributes, excludedAttributes, (etag, includeAttributes, excludeAttributes, repository)
      -> repository.patch(id, etag, patchRequest.getPatchOperationList(), includeAttributes, excludeAttributes));
  }

  @Override
  public ResponseEntity<T> delete(@Parameter(name = "id", required = true) @PathVariable("id") String id) throws ScimException, ResourceException {
      Repository<T> repository = getRepositoryInternal();
      repository.delete(id);
      return ResponseEntity.noContent()
        .build();
  }

  private ResponseEntity<T> update(WebRequest request, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes, UpdateFunction<T> updateFunction) throws ScimException, ResourceException {

    Repository<T> repository = getRepositoryInternal();

    Set<AttributeReference> attributeReferences = AttributeReferenceListWrapper.getAttributeReferences(attributes);
    Set<AttributeReference> excludedAttributeReferences = AttributeReferenceListWrapper.getAttributeReferences(excludedAttributes);
    validateAttributes(attributeReferences, excludedAttributeReferences);

    String requestEtag = request.getHeader("ETag");
    T updated = updateFunction.update(requestEtag, attributeReferences, excludedAttributeReferences, repository);

    // Process Attributes
    updated = processFilterAttributeExtensions(repository, updated, attributeReferences, excludedAttributeReferences);
    updated = attributesForDisplayIgnoreErrors(updated, attributeReferences, excludedAttributeReferences);

    String etag = fromVersion(updated);

    return ResponseEntity.ok()
      .location(buildLocationTag(updated))
      .header(HttpHeaders.ETAG, etag)
      .body(updated);
  }

  @SuppressWarnings("unchecked")
  private T processFilterAttributeExtensions(Repository<T> repository, T resource, Set<AttributeReference> attributeReferences, Set<AttributeReference> excludedAttributeReferences) throws ScimException {
    ScimProcessingExtension annotation = repository.getClass()
                                                 .getAnnotation(ScimProcessingExtension.class);
    if (annotation != null) {
      Class<? extends ProcessingExtension>[] value = annotation.value();
      for (Class<? extends ProcessingExtension> class1 : value) {
        ProcessingExtension processingExtension = CDI.current().select(class1).get();
        if (processingExtension instanceof AttributeFilterExtension) {
          AttributeFilterExtension attributeFilterExtension = (AttributeFilterExtension) processingExtension;
          ScimRequestContext scimRequestContext = new ScimRequestContext(attributeReferences, excludedAttributeReferences);

          try {
            resource = (T) attributeFilterExtension.filterAttributes(resource, scimRequestContext);
            log.debug("Resource now - " + resource.toString());
          } catch (ClientFilterException e) {
            throw new ScimException(HttpStatus.valueOf(e.getStatus()), e.getMessage(), e);
          }
        }
      }
    }

    return resource;
  }

  private URI buildLocationTag(T resource) {
    String id = resource.getId();
    if (id == null) {
      LOG.warn("Repository must supply an id for a resource");
      id = "unknown";
    }

    // TODO: Fix it for id in pathSegment while creating resource
    return ServletUriComponentsBuilder.fromCurrentRequestUri().replaceQuery(null)
      .build().toUri();

  }

  private <T extends ScimResource> T attributesForDisplay(T resource, Set<AttributeReference> includedAttributes, Set<AttributeReference> excludedAttributes) throws AttributeException {
    if (!excludedAttributes.isEmpty()) {
      resource = attributeUtil.setExcludedAttributesForDisplay(resource, excludedAttributes);
    } else {
      resource = attributeUtil.setAttributesForDisplay(resource, includedAttributes);
    }
    return resource;
  }

  private T attributesForDisplayIgnoreErrors(T resource, Set<AttributeReference> includedAttributes, Set<AttributeReference> excludedAttributes) {
    try {
      return attributesForDisplay(resource, includedAttributes, excludedAttributes);
    } catch (AttributeException e) {
      if (log.isDebugEnabled()) {
        log.debug("Failed to handle attribute processing in update " + e.getMessage(), e);
      } else {
        log.warn("Failed to handle attribute processing in update " + e.getMessage());
      }
    }
    return resource;
  }

  private T attributesForDisplayThrowOnError(T resource, Set<AttributeReference> includedAttributes, Set<AttributeReference> excludedAttributes) throws ScimException {
    try {
      return attributesForDisplay(resource, includedAttributes, excludedAttributes);
    } catch (AttributeException e) {
      throw new ScimException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse the attribute query value " + e.getMessage(), e);
    }
  }

  private ScimException notFoundException(String id) {
    return new ScimException(HttpStatus.NOT_FOUND, "Resource " + id + " not found");
  }

  private void validateAttributes(Set<AttributeReference> attributeReferences, Set<AttributeReference> excludedAttributeReferences) throws ScimException {
    if (!attributeReferences.isEmpty() && !excludedAttributeReferences.isEmpty()) {
      throw new ScimException(HttpStatus.BAD_REQUEST, "Cannot include both attributes and excluded attributes in a single request");
    }
  }

  private String fromVersion(ScimResource resource) {
    Meta meta = resource.getMeta();
    if (meta != null) {
      String version = meta.getVersion();
      if (version != null) {
        return version;
      }
    }
    return null;
  }

  @FunctionalInterface
  private interface UpdateFunction<T extends ScimResource> {
    T update(String etag, Set<AttributeReference> includeAttributes, Set<AttributeReference> excludeAttributes, Repository<T> repository) throws ResourceException;
  }
}
